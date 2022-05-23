---
title: RxJava常用操作符 
date: 2018-12-26 21:37:17
---

### 1.创建类操作符

#### 1.1 create

创建一个数据源。

```java
public class Demo {
    public void use() {
        Observable.create((ObservableOnSubscribe<String>) emitter -> emitter.onNext("aloe"))
                .subscribe(System.out::println);
    }
}
```

#### 1.2 just

使用`create`发射数据还是有点麻烦的，我们可以使用`just`来创建数据源。

```java
public class Demo {
    public void use() {
        Observable.just("hello", "aloe").subscribe(System.out::print);
    }
}
```

#### 1.3 fromArray

当我们要遍历一个对象数据时，可以使用`fromArray`

```java
public class Demo {
    public void use() {
        Observable.fromArray(new String[]{"aloe1", "aloe2"}).subscribe(System.out::println);
    }
}
```

也可以直接将多个元素当成一个数组进行分发，因此上面可以不用创建数组，直接传数组中的元素。

<!-- more -->

#### 1.4 fromIterable

遍历一个迭代器

```java
public class Demo {
    public void use() {
        Observable.fromIterable(list).subscribe(System.out::print);
    }
}
```

#### 1.5 timer

延时分发一个数据0，如下延时1秒分发数据0

```java
public class Demo {
    public void use() {
        Observable.timer(1, TimeUnit.SECONDS).subscribe(System.out::println);
    }
}
```

#### 1.6 interval

定时分发数据，如延时1秒，每隔200毫秒分发一次数据

```java
public class Demo {
    public void use() {
        Observable.interval(1000, 200, TimeUnit.MILLISECONDS).subscribe(System.out::println);
    }
}
```

#### 1.7 range和rangeLong

递增分发多个数据，如想分发1，2，3，4，5可以这样做

```java
public class Demo {
    public void use() {
        Observable.range(1, 5).subscribe(System.out::println);
    }
}
```

第一个参数是分发的起始数据，第二个参数是递增多少个，`rangeLong`是分发long类型的数据

#### 1.8 empty

创建一个空的数据源进行分发,`empty`操作符不会执行`onNext`，会直接执行`onComplete`

```java
public class Demo {
    public void use() {
        Observable.empty().subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        });
    }
}
```

### 2.转换类操作符

#### 2.1 map和flatMap

都用于数据转换，`map`在同一发射源，`flatMap`为重新定义发射源

```java
public class Demo {
    public void use() {
        Observable.just("aloe").map(String::toUpperCase).subscribe(System.out::print);
        Observable.just("aloe").flatMap(String::toUpperCase).subscribe(System.out::print);
    }
}
```

#### 2.2 switchMap

当数据在同一线程中时和`flatMap`相同，当在不同线程中时，后面的任务会把前面的任务覆盖

```java
public class Demo {
    public void use() {
        Observable.range(1, 4)
                .switchMap((Function<Integer, ObservableSource<Integer>>) integer ->
                        Observable.just(integer).subscribeOn(Schedulers.newThread()))
                .subscribe(System.out::println);
    }
}
```

当不加`subscribeOn`切换线程时分发的数据为1, 2, 3, 4；当加上`subscribeOn`切换线程时分发的数据为4

#### 2.3 concatMap

当数据在同一线程中时和`flatMap`相同，当在不同线程中时,`flatMap`不会保证数据分发的顺序，而`concatMap`会按原来的顺序继续分发

```java
public class Demo {
    public void use() {
        Observable.range(1, 5)
                .concatMap((Function<Integer, ObservableSource<Integer>>) integer ->
                        Observable.just(integer).subscribeOn(Schedulers.newThread()))
                .subscribe(System.out::println);
    }
}
```

使用`concatMap`后，数据顺序和原来一样。

#### 2.4 flatMapIterable

`flatMapIterable`和`flatMap`类似，不同的是`flatMapIterable`只能转成迭代器进行分发，并且不改变数据源

```java
public class Demo {
    public void use() {
        List<String> list1 = new ArrayList<>(Arrays.asList("a1", "a2"));
        List<String> list2 = new ArrayList<>(Arrays.asList("b1", "b2"));
        List<List<String>> lists = new ArrayList<>(Arrays.asList(list1, list2));
        Observable.fromIterable(lists)
                .flatMapIterable((Function<List<String>, Iterable<String>>) strings -> strings)
                .subscribe(System.out::println);
    }
}
```

#### 2.5 scan

会将处理的结果当数据源继续分发

```java
public class Demo {
    public void use() {
        Observable.just(1, 2, 3, 4)
                .scan((integer1, integer2) -> integer1 * integer2)
                .subscribe(integer3 -> System.out.println(integer3));
    }
}
```

第一个元素直接作为结果返回给interger3,integer3的值会作为下一次分发时integer1的值，每一步的值如下

|integer1|integer2|integer3|
|:---:|:---:|:---:|
| | | 1|
|1|2|2|
|2|3|6|
|6|4|24|

#### 2.6 collect

将零散的数据收集为一个整体，比如我们遍历一个集合将元素乘以10再用一个新的集合来接收

```java
public class Demo {
    public void use() {
        Observable.fromIterable(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)))
                .collect((Callable<List<Integer>>) ArrayList::new, (integers, integer) -> integers.add(integer * 10))
                .subscribe(System.out::println);
    }
}
```

#### 2.7 groupBy

将一组数据按指定的规则进行分组，如下对数据进行奇偶分组

```java
public class Demo {
    public void use() {
        Observable.fromArray(1, 2, 3, 4, 5)
                .groupBy(integer -> integer % 2 == 0 ? "even" : "odd")
                .subscribe(stringIntegerGroupedObservable ->
                        stringIntegerGroupedObservable.collect((Callable<List<Integer>>) ArrayList::new, List::add)
                                .subscribe(integers -> {
                                    System.out.println(stringIntegerGroupedObservable.getKey() + "->" + integers.toString());
                                }));
    }
}
```

### 3.过滤类操作符

#### 3.1 filter和switchIfEmpty

`filter`为条件过滤，满足条件继续分发，不满足条件执行`switchIfEmpty`，相当于if-esle

```java
public class Demo {
    public void use() {
        Observable.just(3)
                .filter(integer -> integer % 2 == 0)
                .switchIfEmpty(new Observable<Integer>() {
                    @Override
                    protected void subscribeActual(Observer<? super Integer> observer) {
                        System.out.println(observer);
                    }
                })
                .subscribe(System.out::println);
    }
}
```

#### 3.2 take和takeLast

`take`过滤前n个数据源，比如我们有10个数据进行分发，但只想要前5个

```java
public class Demo {
    public void use() {
        Observable.range(2, 10).take(5).subscribe(System.out::println);
    }
}
```

`takeLast`为过滤后n个数据

#### 3.3 takeWhile和takeUntil

`takeWhile`满足条件的进行分发，当条件不满足时，则停止分发，无论后面的条件是否还满足

```java
public class Demo {
    public void use() {
        Observable.just(2, 10, 3).takeWhile(integer -> integer < 5).subscribe(System.out::println);
    }
}
```

结果只有2，`takeUntil`为条件成立后，不在分发数据，成立时会继续颁发

```java
public class Demo {
    public void use() {
        Observable.just(2, 10, 3).takeUntil(integer -> integer > 5).subscribe(System.out::println);
    }
}
```

结果为2,10。条件10>5成立时会分发数据，之后不再分发。

#### 3.4 skip和skipLast

`skip`为跳过前n个数据，`skipLast`为忽略最后n个数据

```java
public class Demo {
    public void use() {
        Observable.range(2, 10).skip(3).skipLast(3).subscribe(System.out::println);
    }
}
```

#### 3.5 elementAt

`elementAt`为获取第n个位置的元素，从0开始计算，没有找到可以设置一个默认值

```java
public class Demo {
    public void use() {
        Observable.range(2, 10).elementAt(3, 6).subscribe(System.out::println);
    }
}
```

#### 3.6 debounce

在规定时间段内只允许分发一次数据，若在该时间段内有多次分发，则以最后一次数据重新计时。

```java
public class Demo {
    public void use() {
        Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .take(5)
                .debounce(150, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println);
    }
}
```

分发数据0，1，2，3，4。若时间设为300毫秒则只返回4

#### 3.7 distinct

过滤重新数据，所以重复数据只分发一次

```java
public class Demo {
    public void use() {
        Observable.just(1, 2, 3, 3, 2, 4).distinct().subscribe(System.out::println);
    }
}
```

结果为1,2,3,4。`distinct`可以传一个参数`Function`，根据方法返回值来判断是否相同

```java
public class Demo {
    public void use() {
        Observable.just(1, 2, 3, 3, 2, 4).distinct(new Function<Integer, Integer>() {
            int limit = 0;

            @Override
            public Integer apply(Integer integer) throws Exception {
                limit++;
                return limit + integer;
            }
        }).subscribe(System.out::println);
    }
}
```

`apply`方法的返回值依次为2,4,6,7,7,10。因此最终打印结果为1,2,3,3,4。

#### 3.8 distinctUntilChanged

`distinctUntilChanged和distinct`类似，都是去重。`distinctUntilChanged`比较的是前一个元素与当前元素是否相同，`Function`可以自己定义匹配规则

```java
public class Demo {
    public void use() {
        Observable.just(1, 2, 3, 3, 2, 4)
                .distinctUntilChanged()
                .subscribe(System.out::println);
    }
}
```

返回结果为1,2,3,2,4。

#### 3.9 first和firstElement

过滤符合条件的第一个元素，当没有符合条件的元素时，`first`可以设置一个默认元素，而`firstElement`则没有数据

```java
public class Demo {
    public void use() {
        Observable.just(1, 2, 3, 3, 2, 4)
                .filter(integer -> integer > 4)
                .first(10)
                .subscribe(System.out::println);
    }
}
```

#### 3.10 last和lastElement

过滤符合条件的最后一个元素，当没有符合条件的元素时，`last`可以设置一个默认元素，而`lastElement`则没有数据

```java
public class Demo {
    public void use() {
        Observable.just(1, 2, 3, 3, 2, 4)
                .filter(integer -> integer > 3)
                .last(10)
                .subscribe(System.out::println);
    }
}
```

### 4.组合操作符

#### 4.1.merge和mergeArray

都是将多个数据源合并成一个进行分发，`merge`最多只能合并4个数据源，`mergeArray`则不受个数限制

```java
public class Demo {
    public void use() {
        Observable.merge(Observable.range(1, 2), Observable.range(4, 2)).subscribe(System.out::println);
    }
}
```

#### 4.2 concat和concatArray

`concat`和`merge`一样都是将多个数据源合并成一个进行分发，`concat`会保证数据顺序，`merge`则不会

```java
public class Demo {
    public void use() {
        Observable.concat(Observable.range(1, 2), Observable.range(4, 2)).subscribe(System.out::println);
    }
}
```

#### 4.3 startWith和startWithArray

在数据源之前插入新的数据并组合在一起

```java
public class Demo {
    public void use() {
        Observable.just(1, 2).startWithArray(4).subscribe(System.out::println);
    }
}
```

结果打印4，1，2。

#### 4.4 zip

将多个数据源合并成一个，当一个出现异常或结束时，其它也跟着结束

```java
public class Demo {
    public void use() {
        Observable.zip(Observable.just("a", "b"), Observable.just(4, 5, 6), (s, integer) -> s + "," + integer).subscribe(System.out::println);
    }
}
```

结果打印a,4和b,5。
