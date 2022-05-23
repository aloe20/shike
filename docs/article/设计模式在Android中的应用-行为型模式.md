---
title: 设计模式在Android中的应用(行为型模式)
date: 2019-01-12 21:52:46
categories: [Android, 设计模式]
tags: [Android, 设计模式]
---
[设计模式在Android中的应用(创建型模式)](设计模式在Android中的应用-创建型模式.md)
[设计模式在Android中的应用(结构型模式)](设计模式在Android中的应用-结构型模式.md)

### 12.策略模式

Android中的动画插值器使用策略模式来管理不同的插值效果，有线性插值器，减速插值器，加速减速插值器等等来实现不同的动画效果。

#### 12.1Android动画中的策略模式

我们以ValueAnimator中的插值器来分析

```java
public class ValueAnimator extends Animator implements AnimationHandler.AnimationFrameCallback {
    private TimeInterpolator mInterpolator = sDefaultInterpolator;

    public void setInterpolator(TimeInterpolator value) {
        if (value != null) {
            mInterpolator = value;
        } else {
            mInterpolator = new LinearInterpolator();
        }
    }

    public TimeInterpolator getInterpolator() {
        return mInterpolator;
    }

    void animateValue(float fraction) {
        fraction = mInterpolator.getInterpolation(fraction);
        mCurrentFraction = fraction;
        int numValues = mValues.length;
        for (int i = 0; i < numValues; ++i) {
            mValues[i].calculateValue(fraction);
        }
        if (mUpdateListeners != null) {
            int numListeners = mUpdateListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                mUpdateListeners.get(i).onAnimationUpdate(this);
            }
        }
    }
}
```

Animator将不同的动画行为定义成接口`TimeInterpolator`，动画随着时间如何变化由各子类来控件，在执行动画时直接获取各插值器。

<!-- more -->

#### 12.2策略模式的应用

我们在做用户注册登录时，有不同的方式，可能是手机号验证码登录，或者邮箱密码，也有第三方QQ，微信等登录方式，我们可以用不同的策略来封装不同的登录方式，示例如下:

```java
public class Login {
    private LoginStrategy loginStrategy;

    public void setLoginStrategy(LoginStrategy loginStrategy) {
        this.loginStrategy = loginStrategy;
    }

    public void login() {
        if (loginStrategy != null) {
            LoginBean bean = loginStrategy.getLoginBean();
            System.out.println(bean.toString());
        }
    }

    public static class LoginBean {
        String type;
        String id;
        String pwd;

        public LoginBean(String type, String id, String pwd) {
            this.type = type;
            this.id = id;
            this.pwd = pwd;
        }

        @Override
        public String toString() {
            return "LoginBean{type='" + type + "\', id='" + id + "\', pwd='" + pwd + "\'}";
        }
    }

    public interface LoginStrategy {
        LoginBean getLoginBean();
    }

    public static class PhoneLogin implements LoginStrategy {
        @Override
        public LoginBean getLoginBean() {
            return new LoginBean("1", "phone", "code");
        }
    }

    public static class EmailLogin implements LoginStrategy {
        @Override
        public LoginBean getLoginBean() {
            return new LoginBean("2", "123@123.com", "password");
        }
    }
}
```

### 13.模板方法模式

Android中的模板方法模式用的比较多，比如常见的Activity，fragment组件，或者AsyncTask等等，在完成一个任务时，都会经过一些步骤，我们以activity来说明。

#### 13.1Activity中的模板方法模式

```java
public class Activity extends ContextThemeWrapper {
    public void onCreate(@Nullable Bundle savedInstanceState,
                         @Nullable PersistableBundle persistentState) {

    }

    protected void onStart() {

    }

    protected void onStop() {

    }
}
```

我们可以在`onCreate()`Activity创建时做一些初始化工作，`onStart()`页面加载可见后做一些逻辑处理，在`onStop()`页面不可见时做一些数据保存等等。

#### 13.2模板方法模式的应用

我们可以在基类Activity中继续定义一个模板方法，如初始化View，初始化配置信息，设置标题，请求网络数据等等，除此之外我们在处理文件下载时，也可以使用模板方法模式，定义模板方法开始下载；正在下载中，监听下载进度；取消下载；下载成功；下载失败；下载完成等等。这些场景比较常见，此处省略示例代码，大家可以自己尝试着写一写。

### 14.观察者模式

观察者模式在项目开发中用的非常频繁，常见的有接口回调，事件监听等等，Android中的各种点击事件监听使用的就是观察者模式，还有BroadcastReceiver广播机制，适配器刷新数据等等。以下是RecyclerView.Adapter使用观察者模式来刷新数据示例。

#### 14.1RecyclerView.Adapter中的观察者模式

```java
public abstract static class Adapter<VH extends ViewHolder> {
    private final AdapterDataObservable mObservable = new AdapterDataObservable();

    public final void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        public void notifyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }
    }
}
```

当adapter中的数据源发生改变时，调用`notifyDataSetChanged()`方法来通知adapter来更新界面。

#### 14.2观察者模式应用

当我们在一个页面修改完用户信息，资料后，需要更新上一个页面的界面，这个时候我们可以使用观察者模式来通知上一个页面来更新界面，我们在使用MVP模式时，也可以使用接口回调来通知UI界面更新等等。

### 15.迭代子模式

最常见的迭代子模式有List，Set，Map等迭代器，在Android中Cursor也使用了迭代子模式。

### 16.责任链模式

Android中的事件分发机制使用的是责任链模式，Activity，ViewGroup，View之间处理点击事件各司其职。

#### 16.1Android事件分发责任链模式

首先是Activity处理点击事件，若不处理则传给ViewGroup来处理，若ViewGroup不处理则传给View来处理，若View也不处理，则回传给ViewGroup，Activity，最后传给window来处理。代码如下。

```java
public class Activity extends ContextThemeWrapper {
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO: 2019/1/12 
    }

    public boolean onTouchEvent(MotionEvent event) {
        // TODO: 2019/1/12 
    }
}

public abstract class ViewGroup extends View implements ViewParent, ViewManager {
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO: 2019/1/12 
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO: 2019/1/12 
    }

    public boolean onTouchEvent(MotionEvent event) {
        // TODO: 2019/1/12  
    }

    public class View implements Drawable.Callback, KeyEvent.Callback, AccessibilityEventSource {
        public boolean dispatchTouchEvent(MotionEvent event) {
            // TODO: 2019/1/12  
        }

        public boolean onTouchEvent(MotionEvent event) {
            // TODO: 2019/1/12  
        }
    }
}
```

#### 16.2责任链模式的应用

当我们在处理网络数据时，可以使用责任链模式来处理请求参数和返回数据，比如全局添加参数，添加cookie，日志打印等请求部分的处理，返回数据的缓存，错误处理日志打印等等，详细示例可参考[OkHttp](https://github.com/square/okhttp)的拦截器机制。

### 17备忘录模式

备忘录模式在游戏开发中用的比较多，部分单机游戏玩家会在关键阶段对游戏内容进行备份，一旦闯关失败，则可通过备份重新闯关无不用从新开始，而Android中的Activity也用到的备忘录模式，示例如下

#### 17.1Activity中的备忘录模式

```java
public class Activity extends ContextThemeWrapper {
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(WINDOW_HIERARCHY_TAG, mWindow.saveHierarchyState());

        outState.putInt(LAST_AUTOFILL_ID, mLastAutofillId);
        Parcelable p = mFragments.saveAllState();
        if (p != null) {
            outState.putParcelable(FRAGMENTS_TAG, p);
        }
        if (mAutoFillResetNeeded) {
            outState.putBoolean(AUTOFILL_RESET_NEEDED, true);
            getAutofillManager().onSaveInstanceState(outState);
        }
        getApplication().dispatchActivitySaveInstanceState(this, outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mWindow != null) {
            Bundle windowState = savedInstanceState.getBundle(WINDOW_HIERARCHY_TAG);
            if (windowState != null) {
                mWindow.restoreHierarchyState(windowState);
            }
        }
    }
}
```

当横竖屏切换时，activity通过`onSaveInstanceState()`方法保存备份页面数据，用`onRestoreInstanceState()`来恢复备份的数据。

#### 17.2备忘录模式应用

当我们在处理用户一些配置信息时可以使用备忘录模式来实现，将配置信息备份到本地，下次打开APP时，从本地获取备份数据等等。

### 18状态模式

像Android中的View，Wifi等都使用了状态模式，它们在不同的状态下表现方式有所不同，如View的选中，按压，不可点击等状态，显示样式有所区别，Wifi在关闭中，已关闭，开启中，已开启也是一样。
我们在调试线上环境与测试环境时也可以使用状态模式，以及处理游客模式与已登录模式等等。
