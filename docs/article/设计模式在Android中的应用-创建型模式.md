---
title: 设计模式在Android中的应用(创建型模式)
date: 2019-01-17 21:55:51 
categories: [Android, 设计模式]
tags: [Android, 设计模式]
---

### 1.单例模式

单例模式的实现方式有多种形式，常见的有饿汉式、懒汉式、单重检锁，双重检锁，内部类，枚举等等实现方式。对于双重检锁的实现方式，理论上是非常好的，但实际情况会受到硬件，操作系统等因素的影响，并不推荐使用双重检锁也实现单例。在Android源码中，单例的应用非常广泛，实现方式也有多种。

#### 1.1ArgbEvaluator饿汉式单例

```java
public class ArgbEvaluator implements TypeEvaluator {
    private static final ArgbEvaluator sInstance = new ArgbEvaluator();

    /** @hide */
    public static ArgbEvaluator getInstance() {
        return sInstance;
    }
}
```

<!-- more -->

#### 1.2ResourcesManager懒汉式单重检锁

```java
/** @hide */
public class ResourcesManager {
    private static ResourcesManager sResourcesManager;

    public static ResourcesManager getInstance() {
        synchronized (ResourcesManager.class) {
            if (sResourcesManager == null) {
                sResourcesManager = new ResourcesManager();
            }
            return sResourcesManager;
        }
    }
}
```

#### 1.3其它几种单例的实现

双重检锁单例实现，不推荐此种方式。

```java
public final class Instance {
    private static Instance instance;

    private Instance() {
    }

    public static Instance getInstance() {
        if (instance == null) {
            synchronized (Instance.class) {
                if (instance == null) {
                    instance = new Instance();
                }
            }
        }
        return instance;
    }
}
```

内部类单例实现

```java
public final class Instance {
    private Instance() {
    }

    public static Instance getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final Instance INSTANCE = new Instance();
    }
}
```

枚举类单例实现

```java
public enum Instance {
    INSTANCE
}
```

#### 1.4单例模式的应用

在项目开发中，有些对象我们只需要一个，这个时候使用单例来实现再适合不过了。比如项目中的Application对象，Toast提示等等，实现方式如下

```java
public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }
}
```

Toast提示单例实现如下

```java
public final class Toasts {
    private static Toast toast;

    private Toasts() {
    }

    public static void show(String txt) {
        if (toast == null) {
            synchronized (Toasts.class) {
                toast = Toast.makeText(App.getInstance(), "", Toast.LENGTH_SHORT);
            }
        }
        toast.setText(txt);
        toast.show();
    }
}
```

### 2.工厂模式

工厂模式的实现方式有简单工厂和抽象工厂，简单工厂只能创建固定的对象，简单但可扩展性低，抽象工厂复杂但可扩展性好，能动态创建对象。

#### 2.1BitmapFactory简单工厂实现

```java
public class BitmapFactory {
    public static Bitmap decodeFile(String pathName, Options opts) {
        validate(opts);
        Bitmap bm = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(pathName);
            bm = decodeStream(stream, null, opts);
        } catch (Exception e) {
        /*  do nothing.
            If the exception happened on open, bm will be null.
        */
            Log.e("BitmapFactory", "Unable to decode stream: " + e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // do nothing here
                }
            }
        }
        return bm;
    }
}
```

#### 2.2其它工厂模式的实现

```java
public final class Contract {
    public class FirstFragment extends Fragment {
    }

    public class SecondFragment extends Fragment {
    }

    public interface FragmentFactory {
        Fragment createFragment();
    }

    public class FirstFragmentFactory implements FragmentFactory {
        @Override
        public Fragment createFragment() {
            return new FirstFragment();
        }
    }

    public class SecondFragmentFactory implements FragmentFactory {
        @Override
        public Fragment createFragment() {
            return new SecondFragment();
        }
    }
}
```

#### 2.3工厂模式的应用

项目开发中难免会遇到很多Fragment的对象，到处创建对象不得于后期的维护，我们可以使用简单工厂模式来创建对象，示例如下

```java
public class FragmentFactory {
    public static <T extends Fragment> T createFragment(Context context, Class<T> clz, Bundle bundle) {
        return (T) Fragment.instantiate(context, clz.getName(), bundle);
    }
}
```

### 3.建造者模式

建造模式将对象的创建与对象的功能进行分离，使创建者与使用者的职责更清晰，Android中最典型的建造模式应用有Dialog

#### 3.1Dialog建造者模式

````java
public static class Builder {
    private final AlertController.AlertParams P;

    public Builder(@NonNull Context context, @StyleRes int themeResId) {
        P = new AlertController.AlertParams(new ContextThemeWrapper(
                context, resolveDialogTheme(context, themeResId)));
        mTheme = themeResId;
    }

    public Builder setTitle(@Nullable CharSequence title) {
        P.mTitle = title;
        return this;
    }

    public Builder setMessage(@StringRes int messageId) {
        P.mMessage = P.mContext.getText(messageId);
        return this;
    }

    public AlertDialog create() {
        // We can't use Dialog's 3-arg constructor with the createThemeContextWrapper param,
        // so we always have to re-set the theme
        final AlertDialog dialog = new AlertDialog(P.mContext, mTheme);
        P.apply(dialog.mAlert);
        dialog.setCancelable(P.mCancelable);
        if (P.mCancelable) {
            dialog.setCanceledOnTouchOutside(true);
        }
        dialog.setOnCancelListener(P.mOnCancelListener);
        dialog.setOnDismissListener(P.mOnDismissListener);
        if (P.mOnKeyListener != null) {
            dialog.setOnKeyListener(P.mOnKeyListener);
        }
        return dialog;
    }
}
````

#### 3.2建造者模式的应用

我们在加载网络图片时，有很多参数都不确定是否存在，这个时候使用建造者模式能够很好的解决这个问题，下面是Glide加载网络图片的示例

```java
public class Images {
    private boolean skipMemory;
    private int errorDrawableRes;
    private Drawable errorDrawable;
    private DiskCacheStrategy strategy;
    private Bitmap.CompressFormat format;

    private Images() {
    }

    public void loadImg(ImageView view, String url) {
        RequestOptions options = new RequestOptions()
                .skipMemoryCache(skipMemory)
                .encodeFormat(format)
                .diskCacheStrategy(strategy);
        if (errorDrawable == null) {
            if (errorDrawableRes != 0) {
                options = options.error(errorDrawableRes);
            }
        } else {
            options = options.error(errorDrawable);
        }
        Glide.with(view.getContext()).load(url).apply(options).into(view);
    }

    public static class Builder {
        private boolean skipMemory;
        private int errorDrawableRes;
        private Drawable errorDrawable;
        private DiskCacheStrategy strategy;
        private Bitmap.CompressFormat format;

        public Builder setErrorDrawable(Drawable errorDrawable) {
            this.errorDrawable = errorDrawable;
            return this;
        }

        public Builder setErrorDrawableRes(int errorDrawableRes) {
            this.errorDrawableRes = errorDrawableRes;
            return this;
        }

        public Builder setStrategy(DiskCacheStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder setSkipMemory(boolean skipMemory) {
            this.skipMemory = skipMemory;
            return this;
        }

        public Builder setFormat(Bitmap.CompressFormat format) {
            this.format = format;
            return this;
        }

        public Images create() {
            Images images = new Images();
            images.skipMemory = skipMemory;
            images.errorDrawable = errorDrawable;
            images.errorDrawableRes = errorDrawableRes;
            if (strategy == null) {
                images.strategy = DiskCacheStrategy.DATA;
            } else {
                images.strategy = strategy;
            }
            if (format == null) {
                images.format = Bitmap.CompressFormat.PNG;
            } else {
                images.format = format;
            }
            return images;
        }
    }
}
```

### 4.原型模式

原型模式在Android中的应用非常广泛，有实现`Cloneable`的，有自己赋值的，当我们需要频繁创建对象或创建对象有太多的属性需要初始时，用原型模式会帮我们减少很多工作。

#### 4.1Intent和Animator实现原型模式

`Intent`实现原型模式

```java
public class Intent implements Parcelable, Cloneable {
    @Override
    public Object clone() {
        return new Intent(this);
    }

    public Intent(Intent o) {
        this(o, COPY_MODE_ALL);
    }

    private Intent(Intent o, @CopyMode int copyMode) {
        this.mAction = o.mAction;
        this.mData = o.mData;
        this.mType = o.mType;
        this.mPackage = o.mPackage;
        this.mComponent = o.mComponent;

        if (o.mCategories != null) {
            this.mCategories = new ArraySet<>(o.mCategories);
        }

        if (copyMode != COPY_MODE_FILTER) {
            this.mFlags = o.mFlags;
            this.mContentUserHint = o.mContentUserHint;
            this.mLaunchToken = o.mLaunchToken;
            if (o.mSourceBounds != null) {
                this.mSourceBounds = new Rect(o.mSourceBounds);
            }
            if (o.mSelector != null) {
                this.mSelector = new Intent(o.mSelector);
            }

            if (copyMode != COPY_MODE_HISTORY) {
                if (o.mExtras != null) {
                    this.mExtras = new Bundle(o.mExtras);
                }
                if (o.mClipData != null) {
                    this.mClipData = new ClipData(o.mClipData);
                }
            } else {
                if (o.mExtras != null && !o.mExtras.maybeIsEmpty()) {
                    this.mExtras = Bundle.STRIPPED;
                }

                // Also set "stripped" clip data when we ever log mClipData in the (broadcast)
                // history.
            }
        }
    }
}
```

`ValueAnimator`实现原型模式

```java
public class ValueAnimator extends Animator implements AnimationHandler.AnimationFrameCallback {
    @Override
    public ValueAnimator clone() {
        final ValueAnimator anim = (ValueAnimator) super.clone();
        if (mUpdateListeners != null) {
            anim.mUpdateListeners = new ArrayList<AnimatorUpdateListener>(mUpdateListeners);
        }
        anim.mSeekFraction = -1;
        anim.mReversing = false;
        anim.mInitialized = false;
        anim.mStarted = false;
        anim.mRunning = false;
        anim.mPaused = false;
        anim.mResumed = false;
        anim.mStartListenersCalled = false;
        anim.mStartTime = -1;
        anim.mStartTimeCommitted = false;
        anim.mAnimationEndRequested = false;
        anim.mPauseTime = -1;
        anim.mLastFrameTime = -1;
        anim.mFirstFrameTime = -1;
        anim.mOverallFraction = 0;
        anim.mCurrentFraction = 0;
        anim.mSelfPulse = true;
        anim.mSuppressSelfPulseRequested = false;

        PropertyValuesHolder[] oldValues = mValues;
        if (oldValues != null) {
            int numValues = oldValues.length;
            anim.mValues = new PropertyValuesHolder[numValues];
            anim.mValuesMap = new HashMap<String, PropertyValuesHolder>(numValues);
            for (int i = 0; i < numValues; ++i) {
                PropertyValuesHolder newValuesHolder = oldValues[i].clone();
                anim.mValues[i] = newValuesHolder;
                anim.mValuesMap.put(newValuesHolder.getPropertyName(), newValuesHolder);
            }
        }
        return anim;
    }
}
```

#### 4.2原型模式的应用

有些情况下，我们在java代码中动态创建View时，需要做很多样式的处理，这个时候我们可以提前创建一个对象进行样式初始化，然后再通过原型模式来创建其它需要的对象。我们现在需要创建一个`TextView`
，保存其它属性样式，清除Text,示例代码如下:

```java
public class CloneTextView extends AppCompatTextView implements Cloneable {
    private User user;

    public CloneTextView(Context context) {
        super(context);
    }

    public CloneTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    protected CloneTextView clone() throws CloneNotSupportedException {
        CloneTextView view = (CloneTextView) super.clone();
        view.setText("");
        return view;
    }

    public static class User {
        private int age;
        private String name;

        public User(int age, String name) {
            this.age = age;
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }
    }
}
```

[设计模式在Android中的应用(结构型模式)](设计模式在Android中的应用-结构型模式.md)  
[设计模式在Android中的应用(行为型模式)](设计模式在Android中的应用-行为型模式.md)
