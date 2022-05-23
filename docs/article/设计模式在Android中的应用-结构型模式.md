---
title: 设计模式在Android中的应用(结构型模式)
date: 2019-01-17 22:07:28 
categories: [Android, 设计模式]
tags: [Android, 设计模式]
---
[设计模式在Android中的应用(创建型模式)](设计模式在Android中的应用-创建型模式.md)

### 5.适配器模式

Android中最典型的适配器模式是在`ListView`和`RecyclerView`中的应用，Adapter将网络等数据与UI页面进行适配。

#### 5.1RecyclerView中的Adapter中的适配器模式

示例代码如下

```java
public class QuickAdapter extends RecyclerView.Adapter<QuickAdapter.ViewHolder> {
    private List<User> users;

    public QuickAdapter(List<User> users) {
        if (users == null) {
            this.users = new ArrayList<>();
        } else {
            this.users = users;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvAge.setText("" + users.get(position).age);
        holder.tvName.setText(users.get(position).name);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAge;
        private TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAge = itemView.findViewById(R.id.tv_age);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    public static class User {
        private int age;
        private String name;

        public User(int age, String name) {
            this.age = age;
            this.name = name;
        }
    }
}
```

在`onBindViewHolder()`方法中将数据与UI控件适配绑定

<!-- more -->

#### 5.2适配器模式的应用

当我们在写自定义View时，可以参考RecyclerView的Adapter来将数据与UI绑定，除此之外，我们在做聚合类APP时，可能会将第三方接口返回的数据格式转换成我们自己定义的数据，此时可以使用适配器模式来转换，示例代码如下:

```java
public class Data {
    public interface DataAdapter<S, T> {
        T convert(S s);
    }

    public static class Data1 {
        private String name;
    }

    public static class Data2 {
        private String name;
    }

    public static class UserAdapter implements DataAdapter<Data1, Data2> {
        @Override
        public Data2 convert(Data1 data1) {
            Data2 data = new Data2();
            data.name = data1.name;
            return data;
        }
    }
}
```

### 6.代理模式

Android中的`Binder`采用了代理的模式来实现APP客户端与系统服务之前的通讯，也常用于Android中的跨进程通讯。

#### 6.1Binder中的代理模式

`Binder`作为APP应用程序客户端通讯的桥梁，实现了`IBinder`接口，其代理为`BinderProxy`,`BinderProxy`救命代码如下:

```java
final class BinderProxy implements IBinder {
    private static ProxyMap sProxyMap = new ProxyMap();

    /** @hide */
    private static void dumpProxyDebugInfo() {
        if (Build.IS_DEBUGGABLE) {
            sProxyMap.dumpProxyInterfaceCounts();
        }
    }
}
```

`Binder`中的部分功能为`BinderProxy`来代理完成，详细代码可看源码。

#### 6.2代理模式的应用

在项目开发中，我们的数据会从多个不同的地方获取，比如从网络上的Http服务器，TCP/UDP服务器获取，或者本地缓存，SP中获取。此时将这些写在一些实现比较混乱，分开写调用比较混乱，我们可以将不同的实现分开写，再通过代理统一调用。示例如下:

```java
public class Data {
    interface NetData {
        String getNetData();
    }

    public static class NetImpl implements NetData {

        @Override
        public String getNetData() {
            return "net data.";
        }
    }

    interface SpData {
        String getSpData();
    }

    public static class SpImpl implements SpData {

        @Override
        public String getSpData() {
            return "sp data.";
        }
    }

    public static class DataProxy implements NetData, SpData {
        private NetData netData;
        private SpData spData;

        public DataProxy() {
            netData = new NetImpl();
            spData = new SpImpl();
        }

        @Override
        public String getNetData() {
            return netData.getNetData();
        }

        @Override
        public String getSpData() {
            return spData.getSpData();
        }
    }
}
```

通过代理调用时，不必关心各数据获取的实现细节了。

### 7.装饰器模式

Android中装饰器典型的例子是Context的应用，`ContextWrapper`对`ContextImpl`的装饰。

#### 7.1Context中的装饰器模式

`ContextImpl`是实现在系统源码中，`ContextWrapper`的代码示例如下:

```java
public class ContextWrapper extends Context {
    Context mBase;

    public ContextWrapper(Context base) {
        mBase = base;
    }

    protected void attachBaseContext(Context base) {
        if (mBase != null) {
            throw new IllegalStateException("Base context already set");
        }
        mBase = base;
    }

    public Context getBaseContext() {
        return mBase;
    }

    @Override
    public AssetManager getAssets() {
        return mBase.getAssets();
    }

    @Override
    public Resources getResources() {
        return mBase.getResources();
    }
}
```

mBase为真正的实现对象，用来被装饰。我们不用关心系统中的`ContextImpl`的实现细节，必要时还可以对系统功能进行增强。

#### 7.2装饰器模式的应用

在数据安全性方面，我们会对数据做不同的加密处理，例如我们现在要将`BASE64`加密的数据先进行反转，再进行加密处理，示例如下:

```java
public class Data {
    interface Encrypt {
        String encrypt(String data);
    }

    public static class Base64Encrypt implements Encrypt {
        @Override
        public String encrypt(String data) {
            return Base64.encodeToString(data.getBytes(), Base64.DEFAULT);
        }
    }

    public static class EncryptWrapper implements Encrypt {
        private Encrypt encrypt;

        public EncryptWrapper(Encrypt encrypt) {
            this.encrypt = encrypt;
        }

        @Override
        public String encrypt(String data) {
            return encrypt.encrypt(new StringBuilder(data).reverse().toString());
        }
    }
}
```

此时我们在原来数据加密的基础上做了进一步的反转处理，加强了原有的密码功能。使用装饰者模式使原有加密实现不用做任何修改，而使所有的加密数据在加密前都做了反转处理。

#### 7.3装饰者模式与代理模式的比较

这两种模式非常相似，代理模式包含功能接口类，功能实现类和代理类，装饰者模式包含功能接口类，功能实现类和装饰类。在某些情况下，两者可以等价替换。
但两者的出发点不同，代理模式用于功能的限制，代理模式告诉你哪些是我代理的，你能够使用，哪些是我没有代理的，你无法使用。而装饰者模式用于功能的增强，现有功能无法满足业务需求，需要进行扩展增加。  
比较有争议的地方在于我既不限制功能，也不增强功能，保持原有功能不变。那么该用代理模式还是装饰者模式呢？这种情况就是仁者见仁，智者见智了，但我们应遵守规范，遵守习惯。以代理的习惯命名书写，就是代理模式，此时不要做功能的增强；若以装饰者的习惯命名，就是装饰者模式，不应做功能的限制。这就好比你把首页View命名成HomeBean,想想是什么感受呢？

### 8.外观模式

Android中最直观的外观模式就属于`Intent`了，系统将拍照，打电话，安装软件，闹钟等子系统功能封装在`Intent`中让用户不会关注各子系统而直接使用子系统的功能

#### 8.1Intent外观模式

`intent`实现各功能代码片段示例

```java
public final class IntentUtil {
    public void openCamera(Activity activity) {
        if (activity == null) {
            return;
        }
        Uri tmpUri;
        File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DCIM), "tmp.jpg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            tmpUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            tmpUri = Uri.fromFile(file);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUri);
        activity.startActivityForResult(intent, 100);
    }

    public void call(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:13000000000"));
        activity.startActivity(intent);
    }
}
```

#### 8.2外观模式的应用

我们可以将项目常用的功能如Log打印，Toast提示，Dialog弹窗等封装到一个library库中，暴露一个外观类让外部调用，示例如下

```java
public final class Common {
    private Toasts toasts;
    private Logs logs;

    public Common() {
        toasts = new Toasts();
        logs = new Logs();
    }

    public void showToast(String txt) {
        toasts.showToast(txt);
    }

    public void log(String txt) {
        logs.log(txt);
    }

    private static class Toasts {
        private static Toast toast;

        void showToast(String txt) {
            if (toast == null) {
                synchronized (Common.class) {
                    toast = Toast.makeText(App.getInstance(), "", Toast.LENGTH_SHORT);
                }
            }
            toast.setText(txt);
            toast.show();
        }
    }

    private static class Logs {
        void log(String txt) {
            Log.d("aloe", txt);
        }
    }
}
```

外部不用关注Toast提示，日志打印是如何实现的，只需要通过Common就可以使用这些功能，大大减少了调用成本。

### 9.桥接模式

Android中的`Window`与`WindowManager`是一个典型的桥接模式，`Window`负责窗口的样式控制，包括窗口的背景大小标题等，而`WindowManager`
负责窗口的行为控制，包括窗口的打开，关闭，添加移除`View`等等。

#### 9.1Window与WindowManager桥接模式

抽象类`Window`中持有接口`WindowManager`的引用，子类`PhoneWindow`实现了`Window`抽象方法，添加删除`View`由`WindowManager`子类来控件，示例代码如下

```java
public class PhoneWindow extends Window implements MenuBuilder.Callback {
    private void openPanel(final PanelFeatureState st, KeyEvent event) {
        //...
        final WindowManager wm = getWindowManager();
        if (wm == null) {
            return;
        }
        //...
        lp.windowAnimations = st.windowAnimations;
        wm.addView(st.decorView, lp);
        st.isOpen = true;
    }
}

public class WindowManagerImpl implements WindowManager {
    @Override
    public void addView(View arg0, android.view.ViewGroup.LayoutParams arg1) {
        // pass
    }
}
```

窗口的样式与行为都在变化，各自的变化都交给子类来控件，这样可以实现一个丰富多样的窗口了。

#### 9.2桥接模式的应用

我们在请求网络数据并缓存数据时也可以使用桥接模式，抽象类提供请求数据方法，具体请求方式为子类来完成，比如使用`OkHttp`来请求，或者是`HttpURLConnection`
来完成，缓存行为由缓存接口提供，缓存接口子类来决定怎么缓存，缓存策略等等。示例代码如下:

```java
public class NetData {
    public static abstract class AbstNetData {
        private CacheData cacheData;

        public void setCacheData(CacheData cacheData) {
            this.cacheData = cacheData;
        }

        public CacheData getCacheData() {
            return cacheData;
        }

        abstract void requestData();
    }

    public static class OkHttpData extends AbstNetData {
        public OkHttpData() {
            setCacheData(new LruCache());
        }

        @Override
        void requestData() {
            String data = "get server data.";
            getCacheData().cacheData(data);
        }
    }

    public static interface CacheData {
        void cacheData(String data);
    }

    public static class LruCache implements CacheData {

        @Override
        public void cacheData(String data) {
            // TODO: 2019/1/18 save data
        }
    }
}
```

### 10.组合模式

组合模式为将对象组合在一起以表示部分-整体的结构，使对单个对象与整体组合的操作具有一致性，最典型的组合模式有文件和文件夹的关系，文件夹可以当成一个文件来处理，也可以将多个文件组合在一起进行处理，Android中的`View`
和`ViewGroup`也具体这种特征。

#### 10.1View和ViewGroup组合模式

```java
public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    public void addView(View child, int index, LayoutParams params) {
        if (DBG) {
            System.out.println(this + " addView");
        }
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        requestLayout();
        invalidate(true);
        addViewInner(child, index, params, false);
    }

    public void removeView(View view) {
        if (removeViewInternal(view)) {
            requestLayout();
            invalidate(true);
        }
    }
}
```

我们知道`ViewGroup`本身是`View`的子类，可以当成View来处理，自己也提供了方法来组合View，使其整体也部分的操作具有一致性。

#### 10.2组合模式的应用

当我们在做聊天项目时，处理好友与好友群的关系非常适合组合模式，除此之外，还在做购物车时，处理单个商品和商品组时也适合，我们在实现商品组的添加，删除，付款时与单个商品的处理是一样的，聊天项目中，对好友与好友群的操作也是一样的，以下为好友与好友群的处理示例：

```java
public class Friend {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public static class FriendGroup extends Friend {
        List<Friend> friends;

        public FriendGroup() {
            friends = new ArrayList<>();
        }

        public void addFriend(Friend friend) {
            friends.add(friend);
        }

        public void removeFriend(int index) {
            friends.remove(index);
        }

        public void removeFriend(Friend friend) {
            friends.remove(friend);
        }
    }
}
```

### 11.享元模式

当我们需要频繁的创建管理多个对象时，为了减少创建销毁对象的成本，我们可以使用享元模式来共享这些对象。Android中的线程池是一个典型的享元模式设计方式，我们页面需要加载大量的网络图片，而每张图片都需要开个线程从网络上加载，加载完后又要释放线程，短时间内创建销毁线程对CPU的压力非常大，此时我们用线程队列来处理非常合适了。在内存中先创建若干个线程用来共享，以减少线程的频繁创建与销毁。除了线程池外，Android中的`Message`
也用到了享元模式接下来我们具体分析一下`Message`中的享元模式。

#### 11.1 Message中的享元模式

`Message`是单链表结构，内部有sPool来管理Message，我们先看一下Message的创建与销毁。

```java
public final class Message implements Parcelable {
    int flags;
    Message next;
    public static final Object sPoolSync = new Object();
    private static Message sPool;

    public static Message obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                Message m = sPool;
                sPool = m.next;
                m.next = null;
                m.flags = 0; // clear in-use flag
                sPoolSize--;
                return m;
            }
        }
        return new Message();
    }

    public void recycle() {
        if (isInUse()) {
            if (gCheckRecycle) {
                throw new IllegalStateException("This message cannot be recycled because it "
                        + "is still in use.");
            }
            return;
        }
        recycleUnchecked();
    }
}
```

Message通过静态方法`obtain()`来创建Message对象，当消息队列中没有时，则创建对象，有则从队列中取出。用完后调用`recycle()`
方法回收Message对象,将Message对象添加到消息队列中以达到共享，减少创建，销毁对象成本的目的。

#### 11.2享元模式的应用

假如我们的项目中需要频繁的操作不同的配置文件，此时我们可以用享元模式来管理创建SP对象，示例如下:

```java
public class Sp {
    private static final Map<String, SharedPreferences> MAP = new ArrayMap<>();

    private static SharedPreferences getSp(String name) {
        SharedPreferences sp = MAP.get(name);
        if (sp == null) {
            sp = App.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
            MAP.put(name, sp);
        }
        return sp;
    }

    public static void removeSp(String name) {
        MAP.remove(name);
    }

    public String get(String name, String key, String defValue) {
        return getSp(name).getString(key, defValue);
    }

    public void put(String name, String key, String value) {
        getSp(name).edit().putString(key, value).apply();
    }
}
```

[设计模式在Android中的应用(行为型模式)](设计模式在Android中的应用-行为型模式.md)
