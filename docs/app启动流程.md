---
lang: zh-CN
title: App启动流程
---

::: tip 提示
不同版本的实现细节有所区别，本文使用的版本是Android12
:::

## ZygoteInit程序的入口

当手机开机后，首先会运行bootloader程序，bootloader会对硬件初始化，加载Linux内核，之后会加载init进程做相应的初始化工作，init进程会去加载Zygote进程我们可以在Android源码中找到`/system/core/rootdir/init.zygote64_32.rc`这个文件，内容如下

```rc
service zygote /system/bin/app_process64 -Xzygote /system/bin --zygote --start-system-server --socket-name=zygote
    class main
    priority -20
    user root
    group root readproc reserved_disk
    socket zygote stream 660 root system
    socket usap_pool_primary stream 660 root system
    onrestart exec_background - system system -- /system/bin/vdc volume abort_fuse
    onrestart write /sys/power/state on
    onrestart restart audioserver
    onrestart restart cameraserver
    onrestart restart media
    onrestart restart netd
    onrestart restart wificond
    task_profiles ProcessCapacityHigh MaxPerformance
    critical window=${zygote.critical_window.minute:-off} target=zygote-fatal
 
service zygote_secondary /system/bin/app_process32 -Xzygote /system/bin --zygote --socket-name=zygote_secondary --enable-lazy-preload
    class main
    priority -20
    user root
    group root readproc reserved_disk
    socket zygote_secondary stream 660 root system
    socket usap_pool_secondary stream 660 root system
    onrestart restart zygote
    task_profiles ProcessCapacityHigh MaxPerformance
```

加载这个文件后，会去执行`ZygoteInit#main`，main方法会根据传过来的参数做相应的处理，主要代码如下

```java{12-17,22,35,48}
// ZygoteInit.java
public static void main(String[] argv) {
    ZygoteServer zygoteServer = null;
    //...
    Runnable caller;
    try {
        //...
        boolean startSystemServer = false;
        String zygoteSocketName = "zygote";
        String abiList = null;
        boolean enableLazyPreload = false;
        for (int i = 1; i < argv.length; i++) {
            if ("start-system-server".equals(argv[i])) {
                startSystemServer = true;
            }
            //...
        }
        //...
        zygoteServer = new ZygoteServer(isPrimaryZygote);

        if (startSystemServer) {
            Runnable r = forkSystemServer(abiList, zygoteSocketName, zygoteServer);

            // {@code r == null} in the parent (zygote) process, and {@code r != null} in the
            // child (system_server) process.
            if (r != null) {
                r.run();
                return;
            }
        }
        Log.i(TAG, "Accepting command socket connections");

        // The select loop returns early in the child process after a fork and
        // loops forever in the zygote.
        caller = zygoteServer.runSelectLoop(abiList);
    } catch (Throwable ex) {
        Log.e(TAG, "System zygote died with fatal exception", ex);
        throw ex;
    } finally {
        if (zygoteServer != null) {
            zygoteServer.closeServerSocket();
        }
    }

    // We're in the child process and have exited the select loop. Proceed to execute the
    // command.
    if (caller != null) {
        caller.run();
    }
}
```

for循环里面是解析init传过来的参数，例如是否加载系统服务`SystemServer`等等，若需要，则通过`forkSystemServer`来加载系统服务，最后是通过`zygoteServer.runSelectLoop`创建一个Runnable对象，要想弄明白后面的run做了哪些事件，得先确定这个Runnable究竟是什么对象，`ZygoteServer#runSelectLoop`内容如下

```java{14-15,29-30}
// ZygoteServer.java
Runnable runSelectLoop(String abiList) {
    //...
    while (true) {
        //...
        if (mUsapPoolRefillAction != UsapPoolRefillAction.NONE) {
            int[] sessionSocketRawFDs =
            socketFDs.subList(1, socketFDs.size())
                    .stream()
                    .mapToInt(FileDescriptor::getInt$)
                    .toArray();
            final boolean isPriorityRefill =
                    mUsapPoolRefillAction == UsapPoolRefillAction.IMMEDIATE;
            final Runnable command =
                    fillUsapPool(sessionSocketRawFDs, isPriorityRefill);
            if (command != null) {
                return command;
            } else if (isPriorityRefill) {
                // Schedule a delayed refill to finish refilling the pool.
                mUsapPoolRefillTriggerTimestamp = System.currentTimeMillis();
            }
        }
    }
}

Runnable fillUsapPool(int[] sessionSocketRawFDs, boolean isPriorityRefill) {
    //...
    while (--numUsapsToSpawn >= 0) {
        Runnable caller =
                Zygote.forkUsap(mUsapPoolSocket, sessionSocketRawFDs, isPriorityRefill);
        if (caller != null) {
            return caller;
        }
    }
    // Re-enable runtime services for the Zygote.  Services for unspecialized app process
    // are re-enabled in specializeAppProcess.
    ZygoteHooks.postForkCommon();
    resetUsapRefillState();
    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
    return null;
}
```

在`runSelectLoop`中没有直接创建对象，而是调用`fillUsapPool`方法来创建，而在`fillUsapPool`方法中又走到了Zygote中的forkUsap，forkUsap又走到了childMain

```java{18,62-65}
// Zygote.java
static @Nullable Runnable forkUsap(LocalServerSocket usapPoolSocket,
                                   int[] sessionSocketRawFDs,
                                   boolean isPriorityFork) {
    FileDescriptor readFD;
    FileDescriptor writeFD;
    try {
        FileDescriptor[] pipeFDs = Os.pipe2(O_CLOEXEC);
        readFD = pipeFDs[0];
        writeFD = pipeFDs[1];
    } catch (ErrnoException errnoEx) {
        throw new IllegalStateException("Unable to create USAP pipe.", errnoEx);
    }
    int pid = nativeForkApp(readFD.getInt$(), writeFD.getInt$(),
                            sessionSocketRawFDs, /*argsKnown=*/ false, isPriorityFork);
    if (pid == 0) {
        IoUtils.closeQuietly(readFD);
        return childMain(null, usapPoolSocket, writeFD);
    } else if (pid == -1) {
        // Fork failed.
        return null;
    } else {
        // readFD will be closed by the native code. See removeUsapTableEntry();
        IoUtils.closeQuietly(writeFD);
        nativeAddUsapTableEntry(pid, readFD.getInt$());
        return null;
    }
}

private static Runnable childMain(@Nullable ZygoteCommandBuffer argBuffer,
                                  @Nullable LocalServerSocket usapPoolSocket,
                                  FileDescriptor writePipe) {
    //...
    if (argBuffer == null) {
        //...
        while (true) {
            ZygoteCommandBuffer tmpArgBuffer = null;
            try {
                sessionSocket = usapPoolSocket.accept();
                usapOutputStream =
                        new DataOutputStream(sessionSocket.getOutputStream());
                Credentials peerCredentials = sessionSocket.getPeerCredentials();
                tmpArgBuffer = new ZygoteCommandBuffer(sessionSocket);
                args = ZygoteArguments.getInstance(argBuffer);
                applyUidSecurityPolicy(args, peerCredentials);
                // TODO (chriswailes): Should this only be run for debug builds?
                validateUsapCommand(args);
                break;
            } catch (Exception ex) {
                Log.e("USAP", ex.getMessage());
            }
            // Re-enable SIGTERM so the USAP can be flushed from the pool if necessary.
            unblockSigTerm();
            IoUtils.closeQuietly(sessionSocket);
            IoUtils.closeQuietly(tmpArgBuffer);
            blockSigTerm();
        }
    }
    //...
    try {
        //...
        return ZygoteInit.zygoteInit(args.mTargetSdkVersion,
                                     args.mDisabledCompatChanges,
                                     args.mRemainingArgs,
                                     null /* classLoader */);
    } finally {
        // Unblock SIGTERM to restore the process to default behavior.
        unblockSigTerm();
    }
}
```

在childMain中，有一个while(true)循环接受参数，直到拿到有效的参数为止，这个参数是通过socket传过来的，告诉Zygote接下来去执行谁，提前透露下这个参数的值就是后面非常重要的ActivityThread。我们接着往下看

```java{5-6}
// ZygoteInit.java
public static Runnable zygoteInit(int targetSdkVersion, long[] disabledCompatChanges,
        String[] argv, ClassLoader classLoader) {
    //...
    return RuntimeInit.applicationInit(targetSdkVersion, disabledCompatChanges, argv,
            classLoader);
}
```

在zygoteInit中是直接进入到了RuntimeInit，真正的实现都在RuntimeInit中

```java{5}
// RuntimeInit.java
protected static Runnable applicationInit(int targetSdkVersion, long[] disabledCompatChanges,
        String[] argv, ClassLoader classLoader) {
    //...
    return findStaticMain(args.startClass, args.startArgs, classLoader);
}

protected static Runnable findStaticMain(String className, String[] argv,
        ClassLoader classLoader) {
    Class<?> cl;
    try {
        cl = Class.forName(className, true, classLoader);
    } catch (ClassNotFoundException ex) {
        throw new RuntimeException(
                "Missing class when invoking static main " + className,
                ex);
    }
    Method m;
    try {
        m = cl.getMethod("main", new Class[] { String[].class });
    } catch (NoSuchMethodException ex) {
        throw new RuntimeException(
                "Missing static main on " + className, ex);
    } catch (SecurityException ex) {
        throw new RuntimeException(
                "Problem getting static main on " + className, ex);
    }
    //...
    return new MethodAndArgsCaller(m, argv);
}

static class MethodAndArgsCaller implements Runnable {
    /** method to call */
    private final Method mMethod;
    /** argument array */
    private final String[] mArgs;
    public MethodAndArgsCaller(Method method, String[] args) {
        mMethod = method;
        mArgs = args;
    }
    public void run() {
        try {
            mMethod.invoke(null, new Object[] { mArgs });
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException(ex);
        }
    }
}
```

到这里我们终于知道最开始的Runnable实现上是MethodAndArgsCaller对象，通过socket传过来的ActivityThread类名反射调用main方法。到此为止，Zygote进程任务已经完成，后面将进入ActivityThread执行相应的逻辑
