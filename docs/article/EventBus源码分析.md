---
title: EventBus源码分析 
date: 2019-03-16 22:11:23 
categories: [Android, 源码分析]
tags: [源码分析]
---
本文分为以下几个部分：创建、注册、发送事件、粘性事件来讲解它的实现原理,本文使用Eventbus版本为3.1.1。

### 1.注册

在使用EventBus时第一步得注册一下  
`EventBus.getDefault().register(this);`  
我们先看getDefault()的源码，**EventBus#getDefault()**。

#### 1.1getDefault

```java
public class Demo {
    public static EventBus getDefault() {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus();
                }
            }
        }
        return defaultInstance;
    }
}
```

这里使用了双重检锁加同步的方式实现单例对象，确保在不同线程中只有一个实例。  
除了使用单例的方式创建对象外，我们发现Eventbus还提供了一个静态的builder()来创建实例对象，通过建造者方式来创建具有不同功能的Eventbus实例。先看一下EventBusBuilder源码中的属性。

#### 1.2EventBusBuilder

```java
public class EventBusBuilder {
    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    boolean logSubscriberExceptions = true;
    boolean logNoSubscriberMessages = true;
    boolean sendSubscriberExceptionEvent = true;
    boolean sendNoSubscriberEvent = true;
    boolean throwSubscriberException;
    boolean eventInheritance = true;
    boolean ignoreGeneratedIndex;
    boolean strictMethodVerification;
    ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;
    List<Class<?>> skipMethodVerificationForClasses;
    List<SubscriberInfoIndex> subscriberInfoIndexes;
    Logger logger;
    MainThreadSupport mainThreadSupport;
}
```

通过建造者方式来配置各种日志打印，消息事件的处理。我们可能通过具体事件单独创建一个实例来发送消息，这样可以避免一些不必要的处理判断。创建EventBus实例可能通过这两种方式来创建，再看一下EventBus构造方法，对属性做了一系列的初始化，我们以部分属性来分析。

<!-- more -->

#### 1.3EventBus

```java
public class EventBus {
    EventBus(EventBusBuilder builder) {
        subscriptionsByEventType = new HashMap<>();
        typesBySubscriber = new HashMap<>();
        stickyEvents = new ConcurrentHashMap<>();
        subscriberMethodFinder = new SubscriberMethodFinder(builder.subscriberInfoIndexes,
                builder.strictMethodVerification, builder.ignoreGeneratedIndex);
        eventInheritance = builder.eventInheritance;
    }
}
```

**subscriptionsByEventType**:
以事件类为key，以订阅列表为value，支持多个订阅方法。事件发送后，在这里寻找订阅者，而Subscription是CopyOnWriteArrayList，线程安全的容器，封装了订阅者，订阅方法。  
**typesBySubscriber**:这是一个用HashMap实现的订阅管理类，负责register与unregister  
**stickyEvents**:使用ConcurrentHashMa来保存粘性事件  
**subscriberMethodFinder**:用于查找订阅类中的Subscribe注解方法  
**eventInheritance**:Eventbus默认会考虑事件的父类，如果事件继承自父类，那么该父类也会作为事件发送给订阅者，设为false则不考虑父类,接下来再看一下注册方法**EventBus#register()**

#### 1.4register

```java
public class Demo {
    public void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }
}
```

首先是获取订阅类的class,接着是查找订阅类中的注解方法，并保存在List集合中，再分析**SubscriberMethodFinder#findSubscriberMethods**方法。

#### 1.5findSubscriberMethods

```java
public class Demo {
    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
        if (subscriberMethods != null) {
            return subscriberMethods;
        }

        if (ignoreGeneratedIndex) {
            subscriberMethods = findUsingReflection(subscriberClass);
        } else {
            subscriberMethods = findUsingInfo(subscriberClass);
        }
        if (subscriberMethods.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass
                    + " and its super classes have no public methods with the @Subscribe annotation");
        } else {
            METHOD_CACHE.put(subscriberClass, subscriberMethods);
            return subscriberMethods;
        }
    }
}
```

首先从缓存中获取，若缓存中有，则直接返回。我们在初始化时一般也不设置ignoreGeneratedIndex的值，findUsingReflection()方法是通过反射获取注解方法，所以我们直接分析findUsingInfo()方法。

#### 1.6findUsingInfo

```java
public class Demo {
    private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
        FindState findState = prepareFindState();
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
            findState.subscriberInfo = getSubscriberInfo(findState);
            if (findState.subscriberInfo != null) {
                SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
                for (SubscriberMethod subscriberMethod : array) {
                    if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                        findState.subscriberMethods.add(subscriberMethod);
                    }
                }
            } else {
                findUsingReflectionInSingleClass(findState);
            }
            findState.moveToSuperclass();
        }
        return getMethodsAndRelease(findState);
    }
}
```

这里有个FindState类，Eventbus会将注册后的订阅信息保存在其中，接着再分析initForSubscriber()方法。

#### 1.7 initForSubscriber

```java
public class Demo {
    void initForSubscriber(Class<?> subscriberClass) {
        this.subscriberClass = clazz = subscriberClass;
        skipSuperClasses = false;
        subscriberInfo = null;
    }
}
```

这里是对FindState部分属性赋值，其中subscriberInfo初始化为null,再看findState.subscriberInfo = getSubscriberInfo(findState)
;这一步是查找当前类以及父类中的subscriberInfo的值，由此可知，若有多个子类需要订阅处理消息，可以直接在父类中进行注册。由前面可知findState.subscriberInfo的值为null，我们再接着看findUsingReflectionInSingleClass()
方法

#### 1.8 findUsingReflectionInSingleClass

```java
public class Demo {
    private void findUsingReflectionInSingleClass(FindState findState) {
        Method[] methods;
        try {
            // This is faster than getMethods, especially when subscribers are fat classes like Activities
            methods = findState.clazz.getDeclaredMethods();
        } catch (Throwable th) {
            // Workaround for java.lang.NoClassDefFoundError, see https://github.com/greenrobot/EventBus/issues/149
            methods = findState.clazz.getMethods();
            findState.skipSuperClasses = true;
        }
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if (subscribeAnnotation != null) {
                        Class<?> eventType = parameterTypes[0];
                        if (findState.checkAdd(method, eventType)) {
                            ThreadMode threadMode = subscribeAnnotation.threadMode();
                            findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
                                    subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                        }
                    }
                } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                    throw new EventBusException("@Subscribe method " + methodName +
                            "must have exactly 1 parameter but has " + parameterTypes.length);
                }
            } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                throw new EventBusException(methodName +
                        " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
            }
        }
    }
}
```

这里利用反射的方式，对订阅类进行扫描，找出符合要求的订阅方法，并用Map进行保存。订阅方法需要是public，参数为1，并且用Subscribe注解修饰。这里会将相关数据保存在findState中，数据包括符合要求的方法，事件类型，线程，优先级以及sticky事件等等。在保存前还做了一些检测，我们接着分析checkAdd()
方法。

#### 1.9checkAdd

```java
public class Demo {
    boolean checkAdd(Method method, Class<?> eventType) {
        // 2 level check: 1st level with event type only (fast), 2nd level with complete signature when required.
        // Usually a subscriber doesn't have methods listening to the same event type.
        Object existing = anyMethodByEventType.put(eventType, method);
        if (existing == null) {
            return true;
        } else {
            if (existing instanceof Method) {
                if (!checkAddWithMethodSignature((Method) existing, eventType)) {
                    // Paranoia check
                    throw new IllegalStateException();
                }
                // Put any non-Method object to "consume" the existing Method
                anyMethodByEventType.put(eventType, this);
            }
            return checkAddWithMethodSignature(method, eventType);
        }
    }
}
```

这里做了双重检测，第一次是判断eventType的类型，而第二次检验是判断方法的完整签名。首先通过anyMethodByEventType.put(eventType, method)
将eventType以及method放进anyMethodByEventType这个Map中,同时该put方法会返回同一个key的上一个value值，所以如果之前没有别的方法订阅了该事件，那么existing应该为null，可以直接返回true；否则为某一个订阅方法的实例，要进行下一步的判断。接着分析checkAddWithMethodSignature()
方法。

#### 1.10 checkAddWithMethodSignature

```java
public class Demo {
    private boolean checkAddWithMethodSignature(Method method, Class<?> eventType) {
        methodKeyBuilder.setLength(0);
        methodKeyBuilder.append(method.getName());
        methodKeyBuilder.append('>').append(eventType.getName());

        String methodKey = methodKeyBuilder.toString();
        Class<?> methodClass = method.getDeclaringClass();
        Class<?> methodClassOld = subscriberClassByMethodKey.put(methodKey, methodClass);
        if (methodClassOld == null || methodClassOld.isAssignableFrom(methodClass)) {
            // Only add if not already found in a sub class
            return true;
        } else {
            // Revert the put, old class is further down the class hierarchy
            subscriberClassByMethodKey.put(methodKey, methodClassOld);
            return false;
        }
    }
}
```

从上面的代码看出，该方法首先获取了当前方法的methodKey、methodClass等，并赋值给subscriberClassByMethodKey，如果方法签名相同，那么返回旧值给methodClassOld，接着是if判断，判断methodClassOld是否为空，由于第一次调用该方法的时候methodClassOld肯定是null，此时就可以直接返回true了。但是，后面还有一个判断即methodClassOld.isAssignableFrom(
methodClass)，这个的意思是：methodClassOld是否是methodClass的父类或者同一个类。如果这两个条件都不满足，则会返回false，那么当前方法就不会添加为订阅方法了。  
那么这两个方法到底有什么作用呢？从这两个方法的逻辑来看，第一层判断根据eventType来判断是否有多个方法订阅该事件，而第二层判断根据完整的方法签名来判断。
**第一种情况**
：比如一个类有多个订阅方法，方法名不同，但它们的参数类型都是相同的，那么遍历这些方法的时候，会多次调用到checkAdd方法，由于existing不为null，那么会进而调用checkAddWithMethodSignature方法，但是由于每个方法的名字都不同，因此methodClassOld会一直为null，因此都会返回true。也就是说，**
允许一个类有多个参数相同的订阅方法**。

**第二种情况**
：类B继承自类A，而每个类都是有相同订阅方法，它们都有着一样的方法签名。方法的遍历会从子类开始，即B类，在checkAddWithMethodSignature方法中，methodClassOld为null，那么B类的订阅方法会被添加到列表中。接着，向上找到类A的订阅方法，由于methodClassOld不为null而且显然类B不是类A的父类，methodClassOld.isAssignableFrom(
methodClass)也会返回false，那么会返回false。也就是说，**子类继承并重写了父类的订阅方法，那么只会把子类的订阅方法添加到订阅者列表，父类的方法会忽略**。 分析完findSubscriberMethods()
逻辑，我们再接着分析subscribe()方法。

#### 1.11subscribe

```java
public class Demo {
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.eventType;
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<>();
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
            if (subscriptions.contains(newSubscription)) {
                throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
                        + eventType);
            }
        }

        int size = subscriptions.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
                subscriptions.add(i, newSubscription);
                break;
            }
        }

        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        subscribedEvents.add(eventType);
        //处理粘性事件
        //...
    }
}
```

该方法主要实现了订阅方法与事件直接的关联。以事件为key，方法为value保存在subscriptionsByEventType中。处理订阅事件的优先级，优先级高的会先被通知，最后处理sticky事件。

### 2.注销

注册完我们还得注销订阅  
**EventBus.getDefault().unregister(this)**  
我们再分析一下注销逻辑EventBus#unregister

### 2.1unregister

```java
public class Demo {
    public synchronized void unregister(Object subscriber) {
        List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
        if (subscribedTypes != null) {
            for (Class<?> eventType : subscribedTypes) {
                unsubscribeByEventType(subscriber, eventType);
            }
            typesBySubscriber.remove(subscriber);
        } else {
            logger.log(Level.WARNING, "Subscriber to unregister was not registered before: " + subscriber.getClass());
        }
    }
}
```

首先是获取所有订阅事件，再遍历订阅事件进行注销，注销完后移除订阅者，我们再看一下unsubscribeByEventType()方法。

#### 2.2unsubscribeByEventType

```java
public class Demo {
    private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
        List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions != null) {
            int size = subscriptions.size();
            for (int i = 0; i < size; i++) {
                Subscription subscription = subscriptions.get(i);
                if (subscription.subscriber == subscriber) {
                    subscription.active = false;
                    subscriptions.remove(i);
                    i--;
                    size--;
                }
            }
        }
    }
}
```

根据事件类型获取订阅信息subscriptions集合，对其进行遍历移除，相比注册简单了很多。

### 3.发送事件

注册注销分析完后，我们再看一下发送消息逻辑，以最简单的发送字符串为例  
`EventBus.getDefault().post("aloe");`  
查看post()方法。

#### 3.1post

```java
public class Demo {
    public void post(Object event) {
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);

        if (!postingState.isPosting) {
            postingState.isMainThread = isMainThread();
            postingState.isPosting = true;
            if (postingState.canceled) {
                throw new EventBusException("Internal error. Abort state was not reset");
            }
            try {
                while (!eventQueue.isEmpty()) {
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }
}
```

第一行里的PostingThreadState封装了当前线程信息，订阅者以及订阅事件，currentPostingThreadState是ThreadLocal对象，是线程安全的。后面是将事件放入消息队列中。我们再看一下postSingleEvent()
方法。

#### 3.2postSingleEvent

```java
public class Dmeo {
    private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
        Class<?> eventClass = event.getClass();
        boolean subscriptionFound = false;
        if (eventInheritance) {
            List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
            int countTypes = eventTypes.size();
            for (int h = 0; h < countTypes; h++) {
                Class<?> clazz = eventTypes.get(h);
                subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
            }
        } else {
            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
        }
        if (!subscriptionFound) {
            if (logNoSubscriberMessages) {
                logger.log(Level.FINE, "No subscribers registered for event " + eventClass);
            }
            if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
                    eventClass != SubscriberExceptionEvent.class) {
                post(new NoSubscriberEvent(this, event));
            }
        }
    }
}
```

对于一个事件，默认地会搜索出它的父类，并把父类也作为事件之一发送给订阅者，我们再看一下postSingleEventForEventType()方法。

#### 3.3postSingleEventForEventType

```java
public class Demo {
    private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
            subscriptions = subscriptionsByEventType.get(eventClass);
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted = false;
                try {
                    postToSubscription(subscription, event, postingState.isMainThread);
                    aborted = postingState.canceled;
                } finally {
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                if (aborted) {
                    break;
                }
            }
            return true;
        }
        return false;
    }
}
```

这里获取subscriptions并调用postToSubscription()发送事件。

#### 3.4postToSubscription

```java
public class Demo {
    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
                invokeSubscriber(subscription, event);
                break;
            case MAIN:
                if (isMainThread) {
                    invokeSubscriber(subscription, event);
                } else {
                    mainThreadPoster.enqueue(subscription, event);
                }
                break;
            case MAIN_ORDERED:
                if (mainThreadPoster != null) {
                    mainThreadPoster.enqueue(subscription, event);
                } else {
                    // temporary: technically not correct as poster not decoupled from subscriber
                    invokeSubscriber(subscription, event);
                }
                break;
            case BACKGROUND:
                if (isMainThread) {
                    backgroundPoster.enqueue(subscription, event);
                } else {
                    invokeSubscriber(subscription, event);
                }
                break;
            case ASYNC:
                asyncPoster.enqueue(subscription, event);
                break;
            default:
                throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
        }
    }
}
```

首先获取订阅方法运行的线程，如果是POSTING，那么直接调用invokeSubscriber()方法即可，如果是MAIN，则要判断当前线程是否是MAIN线程，如果是也是直接调用invokeSubscriber()
方法，否则会交给mainThreadPoster来处理，其他情况相类似。最后利用反射的方式来调用订阅方法，将事件发送给订阅者。

### 4.粘性事件的发送及接收

粘性事件与一般的事件不同，粘性事件是先发送出去，然后让后面注册的订阅者能够收到该事件。粘性事件的发送是通过EventBus#postSticky方法进行发送的  
`EventBus.getDefault().postSticky("aloe");`  
我们看一下postSticky()源码。

#### 4.1 postSticky

```java
public class Demo {
    public void postSticky(Object event) {
        synchronized (stickyEvents) {
            stickyEvents.put(event.getClass(), event);
        }
        // Should be posted after it is putted, in case the subscriber wants to remove immediately
        post(event);
    }
}
```

把该事件放进了 stickyEvents这个map中，接着调用了post()
方法，那么流程和上面分析的一样了，只不过是找不到相应的subscriber来处理这个事件罢了。那么当注册订阅者的时候是怎么匹配的呢？我们再来看一下subscribe()方法。

#### 4.2subscribe

```java
public class Demo {
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        //...
        if (subscriberMethod.sticky) {
            if (eventInheritance) {
                // Existing sticky events of all subclasses of eventType have to be considered.
                // Note: Iterating over all events may be inefficient with lots of sticky events,
                // thus data structure should be changed to allow a more efficient lookup
                // (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
                Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
                for (Map.Entry<Class<?>, Object> entry : entries) {
                    Class<?> candidateEventType = entry.getKey();
                    if (eventType.isAssignableFrom(candidateEventType)) {
                        Object stickyEvent = entry.getValue();
                        checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                    }
                }
            } else {
                Object stickyEvent = stickyEvents.get(eventType);
                checkPostStickyEventToSubscription(newSubscription, stickyEvent);
            }
        }
    }
}
```

EventBus并不知道当前的订阅者对应了哪个粘性事件，因此需要全部遍历一次，找到匹配的粘性事件后，会调用checkPostStickyEventToSubscription()
方法，内部又调用了postToSubscription。因此无论对于普通事件还是粘性事件，都会根据threadMode来选择对应的线程来执行订阅方法，而切换线程的关键就是mainThreadPoster、backgroundPoster和asyncPoster。

### 5.HandlerPoster

我们先看mainThreadPoster，在EventBus构造方法中初始化了mainThreadSupport，分析createPoster可知mainThreadSupport是HanlderPoster对象。

#### 5.1HanlderPoster

```java
public class HandlerPoster extends Handler implements Poster {

    private final PendingPostQueue queue;
    private final int maxMillisInsideHandleMessage;
    private final EventBus eventBus;
    private boolean handlerActive;
    //...
}
```

HandlerPoster内部有一个PendingPostQueue，这是一个队列，保存了PendingPost，即待发送的post，该PendingPost封装了event和subscription，方便在线程中进行信息的交互。在postToSubscription方法中，当前线程如果不是主线程的时候，会调用**
HandlerPoster#enqueue**方法。

#### 5.2enqueue

```java
public class Demo {
    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this) {
            queue.enqueue(pendingPost);
            if (!handlerActive) {
                handlerActive = true;
                if (!sendMessage(obtainMessage())) {
                    throw new EventBusException("Could not send handler message");
                }
            }
        }
    }
}
```

首先会从PendingPostPool中获取一个可用的PendingPost，接着把该PendingPost放进PendingPostQueue，发送消息，那么由于该HandlerPoster在初始化的时候获取了UI线程的Looper，所以它的handleMessage()
方法运行在UI线程。

#### 5.3handleMessage

```java
public class Demo {
    public void handleMessage(Message msg) {
        boolean rescheduled = false;
        try {
            long started = SystemClock.uptimeMillis();
            while (true) {
                //...
                eventBus.invokeSubscriber(pendingPost);
                //...
            }
        } finally {
            handlerActive = rescheduled;
        }
    }
}
```

这里调用了**EventBus#invokeSubscriber**方法，在这个方法里面，将PendingPost解包，进行正常的事件分发。

#### 5.4BackgroundPoster

BackgroundPoster继承自Runnable，与HandlerPoster相似的，它内部都有PendingPostQueue这个队列，当调用到它的enqueue的时候，会将subscription和event打包成。

#### 5.5enqueue

```java
public class Demo {
    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this) {
            queue.enqueue(pendingPost);
            if (!executorRunning) {
                executorRunning = true;
                eventBus.getExecutorService().execute(this);
            }
        }
    }
}
```

该方法通过Executor来运行run()方法，run()方法内部也是调用到了**EventBus#invokeSubscriber**方法。

#### 5.6AsyncPoster

与BackgroundPoster类似，它也是一个Runnable，实现原理与BackgroundPoster大致相同，但有一个不同之处，就是它内部不用判断之前是否已经有一条线程已经在运行了，它每次post事件都会使用新的一条线程。

### 6.参考链接

[EventBus 3.0进阶：源码及其设计模式 完全解析](https://www.jianshu.com/p/bda4ed3017ba)
