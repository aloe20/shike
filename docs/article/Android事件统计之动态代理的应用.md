---
title: Android事件统计之动态代理的应用 
date: 2019-03-13 21:54:47 
categories: [Android]
tags: [Android]
---
Android中的事件统计，我们通常会在onClick方法中来处理，若是对页面事件统计，通常会在Activity,Fragment的生命周期中来处理。这样处理有很多缺陷，如不好管理，与其它逻辑高度耦合。而我们细想一下就会发现OnClickListener是个接口，这完全可以使用动态代理的方式来统一处理。代理类示例代码如下:

```java
public class ClickProxy implements InvocationHandler {
    private View.OnClickListener listener;
    private static ClickProxy instance;

    public static ClickProxy getInstance() {
        if (instance == null) {
            synchronized (ClickProxy.class) {
                if (instance == null) {
                    instance = new ClickProxy();
                }
            }
        }
        return instance;
    }

    public View.OnClickListener create(View.OnClickListener listener) {
        this.listener = listener;
        return (View.OnClickListener) Proxy.newProxyInstance(listener.getClass().getClassLoader(), listener.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.i("aloe", "点击事件，处理事件统计");
        return method.invoke(listener, args);
    }
}
```

我们应如何区分不同的点击事件做不同的事件统计呢？其中method为`onClick`方法，args为View参数。有了View参数，我们可以通过tag来传递不同的事件类型ID，invoke方法示例如下:

<!-- more -->

```java
public class Demo {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object tag = ((View) args[0]).getTag(R.id.tag);
        if (tag != null) {
            Log.i("aloe", "处理统计事件:" + tag);
        }
        return method.invoke(listener, args);
    }
}
```

为了避免与其它tag冲突，此处使用带参的方式设置tag,在activity中通过设置代理监听器就可，示例如下:

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.tv);
        tv.setTag(R.id.tag, "首页点击统计");
        tv.setOnClickListener(ClickProxy.getInstance().create(v -> {
            Log.i("aloe", "处理点击逻辑");
        }));
    }
}
```

在项目中我们除了统计点击事件以外，还需要统计页面事件，如进入了哪个页面，离开了哪个页面等等，此时没有响应点击事件。不过我们可以通过手动触发`onClick`
方法来实现代理效果，在activity中我们可以将事件参数放到DecorView的tag中来传递，在fragment中可以通过根View来传参数，此处为activity中的示例，示例如下:

```java
public class MainActivity extends AppCompatActivity {

    private View view;
    private View.OnClickListener listener = ClickProxy.getInstance().create(v -> {
        if (v.getId() == R.id.tv) {
            Log.i("aloe", "处理点击逻辑");
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = getWindow().getDecorView();
        TextView tv = findViewById(R.id.tv);
        tv.setTag(R.id.tag, "主页点击统计");
        tv.setOnClickListener(listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        view.setTag(R.id.tag, "进入主页");
        listener.onClick(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        view.setTag(R.id.tag, "离开主页");
        listener.onClick(view);
    }
}
```

这样处理我们就将统计逻辑与业务逻辑完全分隔开了。
