---
title: StartUp用法详解与原理分析
date: 2021-04-16 22:46:36
categories: [Android, Jetpack]
tags: [Android, Jetpack, StartUp]
---
Startup主要用于管理初始化第三库，组件和一些配置信息，使配置信息更清晰明了。

### 添加依赖

在gradle中添加依赖，本文使用的版本为`1.0.0`。

```groovy
dependencies {
    implementation "androidx.startup:startup-runtime:1.0.0"
}
```

### 实现Initializer接口

`Initializer`接口有两个方法，`create`方法完成具体的初始化逻辑，`dependencies`确定依赖关系及初始化顺序。

```kotlin
class WorkManagerInitializer : Initializer<WorkManager> {
    override fun create(context: Context): WorkManager {
        val configuration = Configuration.Builder().build()
        WorkManager.initialize(context, configuration)
        return WorkManager.getInstance(context)
    }
    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
```

```kotlin
class ExampleLoggerInitializer : Initializer<ExampleLogger> {
    override fun create(context: Context): ExampleLogger {
        return ExampleLogger(WorkManager.getInstance(context))
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf(WorkManagerInitializer::class.java)
    }
}
```

在初始化`ExampleLoggerInitializer`前先初始化`WorkManagerInitializer`

### 自动初始化

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data  android:name="com.example.ExampleLoggerInitializer"
          android:value="@string/androidx_startup" />
</provider>
```

StartUp是通过ContentProvider实现的，自动初始时需要在Manifest中申明注册，provider的name为`androidx.startup.InitializationProvider`，当有冲突时，配置node为`merge`，最后配置`meta-data`信息，name为最后初始化的Initializer，value为`androidx.startup`。

<!-- more -->

### 延迟初始化

若不想初始化，而是在以后的某一时间进行初始化，可以将`meta-data`的node值设置为`remove`

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data  android:name="com.example.ExampleLoggerInitializer"
                tools:node="remove" />
</provider>
```

并在代码中手动初始化

```kotlin
val result = AppInitializer.getInstance(context)
    .initializeComponent(ExampleLoggerInitializer::class.java)
```

`result`值为Initializer的create方法返回。

### 源码分析

#### 注册ContentProvider

StartUp利用ContentProvider自动注册完成自动初始化。我们先看注册的provider`InitializationProvider`,`InitializationProvider`只实现了`onCreate`。

```java
public final class InitializationProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            AppInitializer.getInstance(context).discoverAndInitialize();
        } else {
            throw new StartupException("Context cannot be null");
        }
        return true;
    }
}
```

先通过双重检锁单例模式创建`AppInitializer`对象，最后执行`discoverAndInitialize`方法

#### 解析metadata信息

```java
public final class AppInitializer {
    void discoverAndInitialize() {
        try {
            Trace.beginSection(SECTION_NAME);
            ComponentName provider = new ComponentName(mContext.getPackageName(),
                    InitializationProvider.class.getName());
            ProviderInfo providerInfo = mContext.getPackageManager()
                    .getProviderInfo(provider, GET_META_DATA);
            Bundle metadata = providerInfo.metaData;
            String startup = mContext.getString(R.string.androidx_startup);
            if (metadata != null) {
                Set<Class<?>> initializing = new HashSet<>();
                Set<String> keys = metadata.keySet();
                for (String key : keys) {
                    String value = metadata.getString(key, null);
                    if (startup.equals(value)) {
                        Class<?> clazz = Class.forName(key);
                        if (Initializer.class.isAssignableFrom(clazz)) {
                            Class<? extends Initializer<?>> component =
                                    (Class<? extends Initializer<?>>) clazz;
                            mDiscovered.add(component);
                            if (StartupLogger.DEBUG) {
                                StartupLogger.i(String.format("Discovered %s", key));
                            }
                            doInitialize(component, initializing);
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException | ClassNotFoundException exception) {
            throw new StartupException(exception);
        } finally {
            Trace.endSection();
        }
    }
}
```

先是通过`PackageManager`解析`metadata`，再遍历`metadata`找到值为`androidx.startup`的`Initializer`，最后执行`doInitialize``进行初始化。

#### 进行初始化

```java
public final class AppInitializer {
    <T> T doInitialize(
            @NonNull Class<? extends Initializer<?>> component,
            @NonNull Set<Class<?>> initializing) {
        synchronized (sLock) {
            boolean isTracingEnabled = Trace.isEnabled();
            try {
                if (isTracingEnabled) {
                    // Use the simpleName here because section names would get too big otherwise.
                    Trace.beginSection(component.getSimpleName());
                }
                if (initializing.contains(component)) {
                    String message = String.format(
                            "Cannot initialize %s. Cycle detected.", component.getName()
                    );
                    throw new IllegalStateException(message);
                }
                Object result;
                if (!mInitialized.containsKey(component)) {
                    initializing.add(component);
                    try {
                        Object instance = component.getDeclaredConstructor().newInstance();
                        Initializer<?> initializer = (Initializer<?>) instance;
                        List<Class<? extends Initializer<?>>> dependencies =
                                initializer.dependencies();

                        if (!dependencies.isEmpty()) {
                            for (Class<? extends Initializer<?>> clazz : dependencies) {
                                if (!mInitialized.containsKey(clazz)) {
                                    doInitialize(clazz, initializing);
                                }
                            }
                        }
                        if (StartupLogger.DEBUG) {
                            StartupLogger.i(String.format("Initializing %s", component.getName()));
                        }
                        result = initializer.create(mContext);
                        if (StartupLogger.DEBUG) {
                            StartupLogger.i(String.format("Initialized %s", component.getName()));
                        }
                        initializing.remove(component);
                        mInitialized.put(component, result);
                    } catch (Throwable throwable) {
                        throw new StartupException(throwable);
                    }
                } else {
                    result = mInitialized.get(component);
                }
                return (T) result;
            } finally {
                Trace.endSection();
            }
        }
    }
}
```

`initializing`用于装载`Initializer`，若正在进行初始化过，则不再进行二次初始化。先通过反射创建`Initializer`对象，调用`dependencies`方法获取前置依赖项，若依赖不为空，则先进行初始化。前置依赖完成后调用`create`方法执行初始化逻辑。

#### 手动初始化逻辑

手动初始化逻辑，同自动初始一样，先通过双重检锁创建`AppInitializer`对象，手动初始化不需要解析`metadata`数据，直接调用`initializeComponent`方法进行初始化逻辑。

### 总结

通过源码分析可知，自动初始化先解析`metadata`，遍历找到`androidx.startup`对应`Initializer`，再通过反射创建`Initializer`对象。无论是自动注册还是手动注册，都是通过反射创建对象。由此可知：**当初始化配置不是很多时，使用StartUp并不能减少初始化时间，StartUp只是将多个ContentProvider合并成一个。StartUp的优势在于管理方便，依赖顺序清晰。当初始化信息过多，需要创建大量的ContentProvider时，才能体现时间的减少。**
