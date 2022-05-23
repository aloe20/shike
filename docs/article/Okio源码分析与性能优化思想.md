---
title: Okio源码分析与性能优化思想 
date: 2019-03-16 21:57:45 
categories: [Android, 源码分析]
---

### 1.Okio的优势

```java
public class Demo {
    private static final ByteString PNG_HEADER = ByteString.decodeHex("89504e470d0a1a0a");

    public void decodePng(InputStream in) throws IOException {
        BufferedSource pngSource = Okio.buffer(Okio.source(in));

        ByteString header = pngSource.readByteString(PNG_HEADER.size());
        if (!header.equals(PNG_HEADER)) {
            throw new IOException("Not a PNG.");
        }
    }
}
```

这个是Okio官方提供了一个png图片的解码的例子，我们知道一般判断一个文件的格式就是依靠前面的校验码，比如class文件中前面的16进制代码就是以 cafebabe
开头，同样的常规的png，jpg，gif之类的都可以通过前面的魔数来进行判断文件类型，这里就以一个图片输入流转换成一个BufferedSource，并且通过 readByteString 方法拿到一个字节串 ByteString
这样就能验证这个文件是不是一个png的图片，同样的方法也能用在其他文件的校验上。
Okio除了这些外还有很多额外的功能，而且官方也提供了许多包括对于zip文件的处理，各种MD5，SHA-1.SHA256，Base64之类编码的处理，如果需要额外的一些操作，也可以自己实现Sink，Source对应的方法。

<!-- more -->

### 2.读写文件

```java
public class Demo {
    public void use() {
        Okio.buffer(Okio.sink(file)).writeUtf8("龙儿筝").flush();
        String txt = Okio.buffer(Okio.source(file)).readUtf8();
    }
}
```

我们在创建BufferSink和BufferSource时，先得创建Sink和Source。Sink代表输出流，Source代表输入流，所有只用一个来进行分析。  
我们先看Sink代码，Sink是一个接口。

```java
public interface Sink extends Closeable, Flushable {

    void write(Buffer source, long byteCount) throws IOException;

    @Override
    void flush() throws IOException;

    Timeout timeout();

    @Override
    void close() throws IOException;
}
```

Sink只包含一些最简单的方法，以及一个Timeout超时，找到Sink的子类BufferedSink，发现也是一个接口，里面提供了更多的方法，再找到BufferedSick的子类Buffer和RealBufferedSink。

```java
public class Demo {
    public static BufferedSink buffer(Sink sink) {
        return new RealBufferedSink(sink);
    }
}
```

由buffer方法可知真正创建的是RealBufferedSink。RealBufferedSink类有两个属性buffer和sink以及一些方法。

```java
public class Demo {
    @Override
    public BufferedSink writeUtf8(String string, int beginIndex, int endIndex)
            throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeUtf8(string, beginIndex, endIndex);
        return emitCompleteSegments();
    }
}
```

进入writeUtf8方法可知，虽然RealBufferedSink是BufferedSink的真正实现，但写数据代理交给buffer属性来完成。buffer并不是直接将数据写入文件中，而是调用flush方法才写入文件中。

```java
public class Demo {
    @Override
    public void flush() throws IOException {
        if (closed) throw new IllegalStateException("closed");
        if (buffer.size > 0) {
            sink.write(buffer, buffer.size);
        }
        sink.flush();
    }
}
```

flush方法中是由sink来完成的。由前面创建BufferedSink可知sink是Okio的sink方法创建的。

### 3.Okio中的超时机制

Okio的超时机制让io不会因为异常阻塞在某个未知的错误上，Okio的基础超时机制是采用同步超时。

```java
public class Demo {
    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (out == null) throw new IllegalArgumentException("out == null");
        if (timeout == null) throw new IllegalArgumentException("timeout == null");

        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                checkOffsetAndCount(source.size, 0, byteCount);
                while (byteCount > 0) {
                    timeout.throwIfReached();
                    Segment head = source.head;
                    int toCopy = (int) Math.min(byteCount, head.limit - head.pos);
                    out.write(head.data, head.pos, toCopy);

                    head.pos += toCopy;
                    byteCount -= toCopy;
                    source.size -= toCopy;

                    if (head.pos == head.limit) {
                        source.head = head.pop();
                        SegmentPool.recycle(head);
                    }
                }
            }

            @Override
            public void flush() throws IOException {
                out.flush();
            }

            @Override
            public void close() throws IOException {
                out.close();
            }

            @Override
            public Timeout timeout() {
                return timeout;
            }

            @Override
            public String toString() {
                return "sink(" + out + ")";
            }
        };
    }
}
```

这里创建Sink时传入了OutputStream和Timeout，可以看到write方法中实际上有一个while循环，在每个开始写的时候就调用了timeout.throwIfReached()
方法，这个方法里面去判断时间是否超时，按序执行，同样的Source也是一样的操作。  
当我们创建Sink时传入的是一个Socket对象时，会发现这里使用的是异步超时机制。

```java
public class Demo {
    public static Sink sink(Socket socket) throws IOException {
        if (socket == null) throw new IllegalArgumentException("socket == null");
        AsyncTimeout timeout = timeout(socket);
        Sink sink = sink(socket.getOutputStream(), timeout);
        return timeout.sink(sink);
    }
}
```

AsyncTimeout继承于TimeOut使用WatchDog机制来实现异步超时。

### 4.Segment和SegmentPool解析

前面在while循环中写数据时会先创建一个Segment。Okio将数据分割成一块块的片段，同时segment拥有前置节点和后置节点，构成一个双向循环链表。这样采取分片使用链表，片中使用数组存储，兼具读的连续性和写的可插入性，是一种折中的方案，读写更快，而且可以根据需求改动分片的大小来权衡读写的业务操作。另外，segment也有一些内置的优化操作，综合这些Okio才能大话异彩。

#### 4.1Segment属性分析

```java
final class Segment {
    static final int SIZE = 8192;

    static final int SHARE_MINIMUM = 1024;

    final byte[] data;

    int pos;

    int limit;

    boolean shared;

    boolean owner;

    Segment next;

    Segment prev;

    //...
}
```

SIZE就是一个segment的最大字节数，其中还有一个SHARE_MINIMUM，这个涉及到segment优化中的另一个技巧，共享内存，然后data就是保存的字节数组，pos，limit就是开始和结束点的index，shared和owner用来设置状态判断是否可写，一个有共享内存的segment是不能写入的，pre，next就是前置后置节点。

#### 4.2Segment方法分析

```java
public class Demo {
    public void writeTo(Segment sink, int byteCount) {
        if (!sink.owner) throw new IllegalArgumentException();
        if (sink.limit + byteCount > SIZE) {
            // We can't fit byteCount bytes at the sink's current position. Shift sink first.
            if (sink.shared) throw new IllegalArgumentException();
            if (sink.limit + byteCount - sink.pos > SIZE) throw new IllegalArgumentException();
            System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
            sink.limit -= sink.pos;
            sink.pos = 0;
        }

        System.arraycopy(data, pos, sink.data, sink.limit, byteCount);
        sink.limit += byteCount;
        pos += byteCount;
    }
}
```

owner和Shared这两个状态目前看来是完全相反的，赋值都是同步赋值的，这里有点不明白存在两个参数的意义，现在的功能主要是用来判断如果是共享就无法写，以免污染数据，会抛出异常。当然，如果要写的字节大小加上原来的字节数大于单个segment的最大值也是会抛出异常，也存在一种情况就是虽然尾节点索引和写入字节大小加起来超过，但是由于前面的pos索引可能因为read方法取出数据，pos索引后移这样导致可以容纳数据，这时就先执行移动操作，使用系统的
System.arraycopy
方法来移动到pos为0的状态，更改pos和limit索引后再在尾部写入byteCount数的数据，写完之后实际上原segment读了byteCount的数据，所以pos需要后移这么多。过程十分的清晰，比较好理解。  
除了写入数据之外，segment还有一个优化的技巧，因为每个segment的片段size是固定的，为了防止经过长时间的使用后，每个segment中的数据千疮百孔，可能十分短的数据却占据了一整个segment，所以有了一个压缩机制。

```java
public class Demo {
    public void compact() {
        if (prev == this) throw new IllegalStateException();
        if (!prev.owner) return; // Cannot compact: prev isn't writable.
        int byteCount = limit - pos;
        int availableByteCount = SIZE - prev.limit + (prev.shared ? 0 : prev.pos);
        if (byteCount > availableByteCount) return; // Cannot compact: not enough writable space.
        writeTo(prev, byteCount);
        pop();
        SegmentPool.recycle(this);
    }
}
```

照例如果前面是共享的那么不可写，也就不能压缩了，然后判断前一个的剩余大小是否比当前的大，有足够的空间来容纳数据，调用前面的 writeTo 方法来写数据，写完后移除当前segment，然后通过 SegmentPool 来回收。  
另一个技巧就是共享机制，为了减少数据复制带来的性能开销，segment存在一个共享机制。

```java
public class Demo {
    public Segment split(int byteCount) {
        if (byteCount <= 0 || byteCount > limit - pos) throw new IllegalArgumentException();
        Segment prefix;

        // We have two competing performance goals:
        //  - Avoid copying data. We accomplish this by sharing segments.
        //  - Avoid short shared segments. These are bad for performance because they are readonly and
        //    may lead to long chains of short segments.
        // To balance these goals we only share segments when the copy will be large.
        if (byteCount >= SHARE_MINIMUM) {
            prefix = new Segment(this);
        } else {
            prefix = SegmentPool.take();
            System.arraycopy(data, pos, prefix.data, 0, byteCount);
        }

        prefix.limit = prefix.pos + byteCount;
        pos += byteCount;
        prev.push(prefix);
        return prefix;
    }
}
```

为了防止一个很小的片段就进行共享，我们知道共享之后为了防止数据污染就无法写了，如果存在大片的共享小片段，实际上是很浪费资源的，所以通过这个对比可以看出这个最小数的意义。为了效率在移动大数据的时候直接移动整个segment而不是data，这样在写数据上能达到很高的效率。

#### 4.3SegmentPool属性分析

```java
final class SegmentPool {
    /** The maximum number of bytes to pool. */
    // TODO: Is 64 KiB a good maximum size? Do we ever have that many idle segments?
    static final long MAX_SIZE = 64 * 1024; // 64 KiB.

    /** Singly-linked list of segments. */
    static @Nullable
    Segment next;

    /** Total bytes in this pool. */
    static long byteCount;

    //...
}
```

SegmentPool实际上是Segment的对象池，这个池子的上限是64K，相当于8个segment，next这个节点可以看出这个SegmentPool是按照单链表的方式进行存储的，byteCount则是已有的大小。SegmentPool采用栈的方式来管理Segment对象。

### 5.ByteString分析

在BufferedSink中可以直接写入ByteString，ByteString是不可变的。不可变的对象有许多的好处，首先本质是线程安全的，不要求同步处理，也就是没有锁之类的性能问题，而且可以被自由的共享内部信息。

```java
public class ByteString implements Serializable, Comparable<ByteString> {
    static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final long serialVersionUID = 1L;

    /** A singleton empty {@code ByteString}. */
    public static final ByteString EMPTY = ByteString.of();

    final byte[] data;
    transient int hashCode; // Lazily computed; 0 if unknown.
    transient String utf8; // Lazily computed.

    //...
}
```

ByteString内部有两个属性，byte数组和String，这样能够让这个类在Byte和String转换上基本没有开销，同样的也需要保存两份引用，这是明显的空间换时间的方式，为了性能Okio做了很多的事情。

### 6.Buffer分析

前面提到RealBufferedSink的操作实际上是由Buffer代理完成的。 整个Buffer持有了一个Segment的引用，通过这个引用能拿到整个链表中所有的数据。
Buffer一共实现了三个接口，读，写，以及clone。先从最简单的clone说起，clone是一种对象生成的方式，是除了常规的new·关键字以及反序列化之外的一种方式，主要分为深拷贝和浅拷贝两种，Buffer采用的是深拷贝的方式。

```java
public class Demo {
    @Override
    public Buffer clone() {
        Buffer result = new Buffer();
        if (size == 0) return result;

        result.head = new Segment(head);
        result.head.next = result.head.prev = result.head;
        for (Segment s = head.next; s != head; s = s.next) {
            result.head.prev.push(new Segment(s));
        }
        result.size = size;
        return result;
    }
}
```

对应实现的clone方法，如果整个Buffer的size为0，也就是没有数据，那么就返回一个新建的Buffer对象，如果不为空就是遍历所有的segment并且都创建一个对应的Segment，这样clone出来的对象就是一个全新的毫无关系的对象。前面分析segment的时候有讲到是一个双向循环链表，但是segment自身构造的时候却没有形成闭环，其实就是在Buffer中产生的。  
除了clone接口外，同时还有两个接口BufferedSink，BufferedSource。Buffer实现了这两个接口的所有方法，所以既然读也有写的方法。

```java
public class Demo {
    @Override
    public Buffer writeShort(int s) {
        Segment tail = writableSegment(2);
        byte[] data = tail.data;
        int limit = tail.limit;
        data[limit++] = (byte) ((s >>> 8) & 0xff);
        data[limit++] = (byte) (s & 0xff);
        tail.limit = limit;
        size += 2;
        return this;
    }
}
```

writeShort用来给Buffer中写入一个short的数据，首先通过writableSegment拿到一个能够有2个字节空间的segment，tail中的data就是字节数组，limit则是数据的尾部索引，写数据就是在尾部继续往后写，直接设置在data通过limit自增后的index，然后重置尾部索引，并且buffer的size大小加2。

```java
public class Demo {
    @Override
    public short readShort() {
        if (size < 2) throw new IllegalStateException("size < 2: " + size);

        Segment segment = head;
        int pos = segment.pos;
        int limit = segment.limit;

        // If the short is split across multiple segments, delegate to readByte().
        if (limit - pos < 2) {
            int s = (readByte() & 0xff) << 8
                    | (readByte() & 0xff);
            return (short) s;
        }

        byte[] data = segment.data;
        int s = (data[pos++] & 0xff) << 8
                | (data[pos++] & 0xff);
        size -= 2;

        if (pos == limit) {
            head = segment.pop();
            SegmentPool.recycle(segment);
        } else {
            segment.pos = pos;
        }

        return (short) s;
    }
}
```

读的方法相对于写的方法就复杂一些，因为buffer是分块的，读数据的过程就有可能是跨segment的，比如前面一个字节，下一个segment一个字节，这种情况就转化为readbyte，读两个字节后合成一个short对象，对于连续的读可以直接通过pos索引自增达到目的，读完后Buffer的size减2。并且会有当前的segment会出现读完后数据为null的情况，此时头部索引pos和尾部索引limit就重合了，通过pop方法可以把这个segment分离出来，并且将下一个segment设置为Buffer的head，然后将分离出来的segment回收到对象池中。

### 7.总结

Okio这个库的精髓，第一就是快，Okio采取了空间换时间的方式比如Segment和ByteString之类的存储来让IO操作尽可能不成为整个系统的瓶颈，虽然采取这种方式但是在内存上也是极致的优化，使用的片段共享以及整体的读写共享来加快大字节数组的读写，第二就是稳定，Okio提供了超时机制，不仅在IO操作上加上超时的判定，包括close，flush之类的方法中都有超时机制，这让上层不会错过一个可能导致系统崩溃的超时异常，第三就是方便，Sink，Source两个包装了写和读，区别于传统的IO各种不同的输入输出流，这里只有一种而且支持socket，十分的方便。当然Okio还有很多其他的好处，易于扩展，代码量小易于阅读，我想这就是许多上层库选择Okio来作为IO操作的原因。

### 8.参考地址

[大概是最完全的Okio源码解析文章](https://www.jianshu.com/p/f033a64539a1)
