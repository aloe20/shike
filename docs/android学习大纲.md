---
lang: zh-CN
title: Android学习大纲
---

## 数据结构与算法

### 数据结构

- 数组
- 集合
  - ArrayList
  - LinkedList
- 堆栈
- 队列
  - Queue
  - Deque
  - 阻塞队列
- 树
  - 平衡二叉树
  - 红黑树
- 映射表
  - HashTable
  - HashMap
  - SparseArray
  - ArrayMap
- 字符串

### 算法

- 排序算法
  - 冒泡排序
  - 选择排序
  - 插入排序
  - 快速排序
  - 希尔排序
  - 归并排序
  - 基数排序
  - 计数排序
  - 桶排序
  - 堆排序
- 查找算法
  - 二分查找
  - 树形查找
  - hash查找

## Java知识

### 泛型

- 泛型的作用与定义
- 泛型的真协变与逆变
- PCES法则：泛型上下边界
- 泛型擦除，泛型桥方法
- 实战：RxJava泛型分析

### 注解

- 自定义注解
- 注解的使用
- 注解原理
- 实战：Retrofit注解分析

### 反射

- 反射基本概念与Class
  - 三种获取Class对象的方式
  - 获取构造器实例化与属性信息
  - 包信息和方法
  - Hook技术动态编程
- 类加载器ClassLoader
  - 动态代理模式
  - Davilk和ART
  - PathClassLoader、DexClassLoader和BootClassLoader
  - 双亲委托机制
- 动态代理
  - 静态代理与动态代理区别
  - JDK动态代理原理
  - CGLIB动态代理原理
  - 实战：Retrofit动态代理分析
- 开闭原则的IOC事件注入
- 插件化与热修复

### 数据流与序列化

- Java IO
  - 装饰者模式
  - InputStream与OutputStream
  - Reader与Writer
- 序列化
  - Serializable
  - Parcelable
  - Json
    - Adapter设计模式
    - Json解析原理
  - XML
  - Protobuf
- 文件操作
  - FileChannel
  - 内存映射
- 实战：IO操作Dex文件加密，APK加固

### 多线程并发

- 线程协作
  - CPU核心数、线程数、时间轮询机制
  - synchronized、Lock、volatile、ThreadLocal
  - wait、notify/notifyAll、join
  - AQS
- 线程的深入理解
  - 线程的生命周期
  - 死锁与并发安全
  - ThreadLocal深入理解
- CAS
  - CAS原理
  - CAS带来的3大问题
  - 原子操作类的正确使用
- 线程池
  - 阻塞队列
  - 线程池实现
  - 线程池排队机制
  - Executor分析
  - 实战：手写线程池
- Java内存模型JMM

### 虚拟机

#### Java虚拟机

- 运行时数据区
  - JAVA虚拟机栈
    - 虚拟机栈的作用以及数据结构
    - 程序计数器(PC寄存器)
    - 栈帧与方法字节码执行原理
    - 本地方法栈的定义
  - JAVA堆与对象分配
    - 堆空间储储分代划分
    - 对象的内存分配
      - 指针碰撞
      - 空闲列表
      - 内存分配的并发：CAS机制方案
      - 内存分配的并发：TLAB分配缓冲方案
      - 对象内存布局
    - 直接内存(堆外内存)
  - 方法区：永久代和元空间
-垃圾回收器
  - 垃圾判定算法之引用计数与可达性分析法
  - Java强软弱虚四大引用与Finalize
  - GC回收算法之标记-清除、复制、标记-整理算法
  - 内存碎片、内存抖动与内存溢出
  - 分代垃圾回收机制
    - 分代回收理论
    - 逃逸分析：对象的栈上分配
    - 对象年龄动态判定
    - 空间分配担保
  - GC分类与垃圾收集器
    - Minor GC/Major GC/Full GC
    - Stop The World(STW)
    - 常见垃圾收集器
      - Serial/Serial Old
      - Parallel Scavenge (ParallerGC)/Parallel Old
      - ParNew、G1与CMS

#### Dalvik/ART虚拟机

- 基于寄存器的虚拟机
- Dalvik与ART的区别
- ART程序安装与运行机制
- ART虚拟机的垃圾回收
  - CC并发复制垃圾收集器
  - 需要整理碎片的CMS

## Kotlin知识

- Kotlin基础
  - 基本语法
    - 基本数据类型
    - 引用类型
    - 只读变量
    - 类型推断
    - 编译时常量
    - 字节码
    - range表达式
    - when表达式
    - String模板
    - lateinit与by lazy
  - 函数与Lambda
    - 函数头
    - 函数参数
    - Unit函数
    - Nothing类型
    - 反引号函数
    - 匿名函数
    - 函数类型与隐式返回
    - apply、also、run、let、with
    - 函数内联
  - 内置函数
    - substring
    - split
    - replace
    - takeIf与takeUnless
    - ==与===
  - 集合框架
    - List
    - Set
    - Map
  - 对象类型
    - 接口与抽象类
    - 继承与重载
    - Any
    - 对象表达式
    - 伴生对象
    - 嵌套类
    - 数据类
    - copy函数
    - 解构声明
    - 运算符重载
    - 枚举类
    - 密封类
  - 高级特性
    - 泛型
    - vararg
    - out协变与in逆变
    - 扩展属性、函数与reified、infix
    - map与flatMap
    - filter
    - zip
    - folder
    - @JvmName、@JvmField、@JvmOverloads、@JvmStatic、@Throws
- Kotlin进阶
  - Lambda与高阶函数
    - Lambda深入学习
    - 高阶函数深入学习
    - Lambda与高阶函数的运用
  - DSL
    - DSL的理解
    - 自定义DSL
  - 高级语法
    - 泛型
    - 反射
    - 注解
- Kotlin协程
  - 源码分析
  - 作用域
  - 协程切换与挂起
  - Flow

## 高级UI与FrameWork

### UI剖析与自定义

- 自定义ViewGroup
  - UI基础
    - 坐标系
    - 角度
    - 颜色
  - 绘制原理
    - 绘制流程
    - MeasureSpec
    - LayoutParams
  - 事件分发机制
- 自定义View
  - Canvas与Paint
  - 绘制点、线、面与图形
  - 画布与图层
  - Path、PathMeasure、Matrix与贝塞乐曲线
  - 手势处理
    - MotionEvent与多点触控
    - GestureDetector ScaleGestureDetector
- 实战
  - 文字渐变特效
    - 绘制文字
    - 视图动画与属性动画
  - RecyclerView吸顶
    - ItemDecoration
    - LayoutManager
    - ViewHolder
    - 源码分析
  - Banner控件
    - ViewPager
      - 加载机制与优化
      - 加载Fragment
    - ViewPager2源码
- MD组件
  - Toolbar
  - FloatingActionButton
  - Snackbar
  - AppbarLayout
  - DrawLayout
  - CardView
  - TabLayout
  - CollapsingToolbarLayout
  - NavigationView
  - BottomNavigationView
  - BottomSheet
  - CoordinatorLayout
    - 自定义behavior
  - NestScrollView
    - 原理解析
- WebView交互
  - 使用与原理
  - JS与Java交互
  - 多进程WebView使用
  - WebView与Native通信
- 插件化换肤
  - LayoutInflater布局加载
  - 资源加载
  - Resource与AssetManager

### Jetpack架构组件

- Lifecycle
- LiveData
  - 粘性事件
- DataBinding
- ViewModel
  - MVVM
- ROOM
- Navigation
- WorkManager
  - 定时周期任务
  - 多任务管理
  - 并发有序执行
  - 原理解析
- Hilt
  - Dagger2原理
  - Dagger2与Hilt关系
  - Hilt设计原理
- Paging
  - 滑动分页加载
  - 缓存策略
  - 源码解析

### FrameWork内核

- Binder机制
  - Linux知识
    - 进程隔离
    - 进程空间(用户空间/内核空间)
    - 系统调用(用户态/内核态)
    - Linux的IPC机制
      - 共享内存
      - Socket/管道/共享内存/信号量/消息队列
  - 传统Linux进程通信原理
    - copy_from_user/copy_to_user
  - Binder通信原理
    - Binder跨进程通讯原理
      - 动态内核加载模块
      - 内存映射mmmap原理解析
      - Binder IPC实现原理
    - Binder通信模型
      - Client/Server/ServiceManager/驱动
    - Binder Driver浅析
      - Binder线程池
    - 启动ServerManager
    - 获取ServerManager
    - 注册服务
    - 获取服务
    - Binder通信中的代理模式
  - Binder Java层实现
    - IBinder/IInterface/Binder/Stub
    - AIDL使用以及原理
  - 常见面试题
    - 为什么Android要采用Binder
    - Binder到底是什么
    - Binder是如何跨进程的
    - 一次Binder通信的基本流程是什么样
    - 为什么Activity间传递对象需要序列化
    - 四大组件底层的通信机制
    - AIDL内部实现原理
- Handler机制
  - Linux的epoll机制
  - MessageQueue添加数据如何确保线程安全
  - Looper.loop()为什么不会阻塞主线程
  - Message的数据结构是什么样子
  - IntentService源码
  - HandlerThread源码
- Dalvik VM进程系统
  - Zygote基础
  - 分析Zygote的启动过程
  - 启动SystemServer进程
  - 应用程序进程
    - 创建应用程序
    - 启动线程池
    - 创建消息循环
- AMS
  - AMS的启动过程
  - AMS重要数据结构解析
    - ActivityRecord
    - TaskRecord
    - ActivityStack
  - Activity栈管理
    - Activity任务栈模型
    - Launch Mode
    - Intent的Flag与taskAffinity
  - Activity启动流程
    - Hook实现启动未注册Activity
  - Activity管理
    - activity运行机制
    - adj内存管理机制
    - activity内核管理方案
  - 实战：Hook AMS实现集中登录
- WMS
  - Window的创建过程
  - Window的添加过程
  - Window的删除过程
  - Dialog与Toast源码解析
  - 剖析Activity、View和Window的关系
  - 从WMS角度分析Activity启动流程
- PMS
  - APP安装流程分析
  - APP解析流程
  - Manifect清单文件的价值
  - 权限管理源码解析

### Android组件内核

- Activity与调用栈
  - 四大启动模式与Intent Flag
  - APK启动流程与ActivityThread解析
  - Activity生命周期源码解析
- Fragment和管理与内核
  - Fragment事务管理机制
  - Fragment转场动画
  - Fragment嵌套处理
- Service内核原理
  - start与bind区别和原理
  - 自带工作线程的IntentService
  - 前台服务与Notify
- 组件间通信方案
  - Activity和Fragment低耦合通信设计
  - Android与Service通信
  - Intent数据传输与限制
  - ViewModel通信方案
  - 事件总线EventBus源码解析

### 数据持久化

- Android文件系统
  - sdcard与内部存储
  - Android R分区存储
- 轻量级kv持久化
  - Shared Preference原理
  - 微信MMKV原理与实现
    - MMAP内存映射
    - 文件数据结构
    - 增量更新与全量更新
    - 多进程设计
- 嵌入式Sqlite数据库
  - SqliteOpenHelper
  - Sqlite升级与数据迁移
  - 注解ORM数据库框架

## 腾讯性能优化

### 性能优化实战

- OOM原理分析
  - adj内存管理机制
  - JVM内存回收机制与GC算法分析
  - 生命周期相关问题总结
  - Bitmap压缩
- ANR分析
  - AMS系统时间调节原理
  - 程序等待原理
  - ANR问题解决
- Crash监控方案
  - Java层监控方案
  - Nativie层监控方案
- 启动速度与执行效率
  - 冷暖热启动耗时检测
  - 启动黑白屏解决
  - 卡顿分析
  - StickMode严苛模式
  - Systrace、TraceView与Profile工具
- 布局检测与优化
  - 布局层级优化
  - 过度渲染检测
  - Hierarchy Viewer与Layout Inspactor
- 内存优化
  - 内存抖动和内存泄漏
  - Bitmap内存优化
  - Profile内存监测工具
  - Mat大对象与泄漏检测
- 耗电优化
  - Doze和Standby
  - Battery Historian
  - JobScheduler和WorkManager
- 网络数据优化
  - protobuf
  - 7z压缩
  - webp图片
- APK大小优化
  - APK瘦身
  - 资源混淆
- 屏幕适配
  - 屏幕适配方案总结
  - hook技术实现屏幕适配
- APM性能监控
  - 内存监控
    - JVMTI工作机制
    - 类加载器如何加载so
    - MMAP完成高效日志记录
    - 动态内存分配与GC监控
  - 卡顿监控
    - Looper机制监控方案
    - Choreographer监控方案
    - Matrix无侵入埋点监控函数耗时
  - ANR监控
    - 信号机制
    - Handler WatchDog

### 项目管理优化

- 版本控制Git
- 自动化构建Gradle
  - Groovy基础
    - 常用数据结构
    - 接口与闭包
    - 面向对象
    - Json与xml解析
    - 文件操作
  - Gradle与Android插件
    - Gradle是什么
    - Gradle生命周期
    - Project与Task
  - Transform API
    - Transform执行机制与配置
    - 字节码增强技术
    - 修改无源码第三方SDK代码
  - 自定义插件开发
    - build script脚本
    - buildSrc目录
    - 独立项目开发插件
    - 上传本地仓库与jcenter仓库
    - Artifactory私服仓库搭建
  - 插件实战
    - 自动化加固插件
    - AOP编程字节码插桩
    - 多渠道打包
    - 发版自动钉钉

## 源码解读

### 架构师必备设计思想

- 程序设计原则
  - 六大原则
    - 单一职责原则
    - 开闭原则
    - 里氏替换原则
    - 接口隔离原则
    - 迪米特法则
  - 设计模式
    - 结构型模式
      - 桥接模式(WMS)
      - 适配器模式(RecyclerView、Gson)
      - 装饰器模式(IO文件操作)
      - 代理模式(Retrofit、Binder)
      - 外观模式(Retrofit、Glide)
      - 组合模式(View)
      - 享元模式(内存池、Handler)
    - 创建型模式
      - 建造者模式(Retrofit)
      - 单例模式(Eventbus)
      - 抽象工厂模式
      - 工厂方法模式(Retrofit)
      - 原型模式(Intent)
    - 行为型模式
      - 模板方法模式(Activity)
      - 策略模式(Rxjava、Retrofit)
      - 观察者模式(Lifecycle、Rxjava)
      - 责任链模式(Rxjava)
      - 命令模式(Android事件输入)
      - 访问者模式(APT)
      - 迭代器模式(List集合)
      - 备忘录模式(Activity数据恢复)
      - 状态模式(Wifi状态管理)
      - 中介者模式(keyguard锁屏)
      - 解释器模式(PackageParser)
- MVC、MVP和MVVM
- 模块化、组件化与插件化

### 网络框架解析

- 网络通信基础
  - Restful url
  - HTTP协议与TCP/IP协议
  - SSL握手与加密
  - DNS解析
  - Socket通信
    - Socket代理
    - Http普通代理与隧道代理
- Okhttp解析
  - Socket连接池复用
  - Http协议重定向与缓存处理
  - 高并发请求队列：任务分发
  - 拦截器设计
- Retrofit源码分析
- 高性能网络模块架构设计

### 图片加载框架解析

- 图片加载框架选型
  - Glide
  - Picasso
  - Fresco
  - Coil
- Glide原理分析
  - Fragment感知生命周期
  - 自动图片大小计算
  - 图片解码
  - 优先级请求队列
  - ModelLoader与Registry
  - 内存缓存
    - LRU内存缓存
    - 引用计数与弱引用活跃缓存
    - Bitmap复用池
    - 缓存大小配置
  - 磁盘文件缓存
    - 原始图像文件缓存
    - 解码图像文件缓存
- Coil原理分析
  - Coil对比Glide
  - Coil流程分析
    - MemoryCache
    - DiskCache
    - 动态采样
    - Coroutine
- 手写图片加载框架

- Coil图片加载

### APP热修复

- AOT/JIT、dexopt与dex2oat
- CLASS_ISPREVERIFIED问题与解决
- Android N混编对热修复的影响
- 即时生效与重启生效热修复原理
- Gradle自动补丁包生成

### APP插件化

- Class文件加载Dex原理
- Android资源加载与管理
- 四大组件的加载与管理
- so库的加载原理
- Android系统服务的运行原理
- 实现插件化框架

### APP组件化

- ARouter原理
- APT技术生动生成代码与动态类加载
- Java SPI机制实现组件服务调用
- 拦截器AOP编程，IOC注入

### 响应式编程

- 链式调用
- 扩展的观察者模式
- 事件变换设计
- Scheduler线程控制

## NDK与音视频

### NDK基础知识

- C与C++
  - 数据类型
  - 内存结构与管理
  - 预处理指令、Typedef别名
  - 结构体与共用体
  - 指针、智能指针、方法指针
  - 线程
  - 类
    - 函数、虚函数、纯虚函数与析构函数
    - 初始化列表
- JNI开发
  - 静态与动态注册
  - 方法签名与Java通信
  - 本地引用与全局引用
- Native开发工具
  - CPU架构与注意事项
  - gcc/g++/clang编译器
  - 静态库与动态库
  - 交叉编译移植
  - 构架脚本与构建工具
    - Cmake
    - Makefile
  - AS构建NDK项目
- Linux编程
  - Linux环境搭建，系统管理，权限系统和工具使用
  - Shell脚本编程
- APK增量更新

### 音视频知识

- H264规范解析
  - 图像的物理现象
  - 图像的表示RGB与YUV
  - H264编码框架与原理
    - 预测、变换、量化与熵编码
    - 帧内预测与帧间预测
    - IPB帧区别与原理
    - PTS DST与视频GOP序列
    - 编码规范Profile档次与Level级别解析
  - H264码流分析
    - SPS、PPS、与IDR(多帧预测)
    - 视频编码层VCL与网络提取层NAL
    - Annexb格式解析
    - 规范之外实际实现的EBSP——“防止竞争”策略解析
  - Android Camera/Camera2/CameraX演进
- H265规范解析
  - H264宏块到编码树单元(CTU)与编码树块(CTB)
  - 图像分割模式：编码单元(CU)，预测单元(PU)和变换单元(TU)
  - H265编码框架
  - H264与H265编码比较
- 音频编解码
  - 声音的物理性质
  - 模拟信号与数字音频
  - 采样与量化
  - Audacity波形图、频谱图与语谱图分析
  - 音频数据采集与播放：AudioRecord与AudioTrack
  - OpenSL ES最佳实践
  - AAC编码规范
  - Speex编码实践
- FFmpeg
  - FFmpeg交叉编译移植Android平台
  - FFmpeg与libx264的集成
  - FFmpeg常见结构体分析
  - FFPlay简析
  - 基于FFmpeg的Android视频压缩
    - 播放器架构设计
    - 解码与播放模块
    - AVSync音视频同步模块设计与实现
- OpenMax(OMX)框架
  - AwesomePlayer到OMX服务
  - OMXCodec与OMX实践回调流程
  - MediaCodec编解码器
  - MediaMuxer复用
  - MediaExtractor解复用

### OCR图像识别

- OpenCV图像预处理
  - 灰度化、二值化
  - 模糊、高斯模糊
  - 图像形态学操作：腐蚀、膨胀与开闭操作
  - 轮廓查找
- 人脸检测与跟踪
  - haar模型
  - lbp特征提取
  - 物体检测模型训练
- 身份证识别

### 全格式播放器

- 播放器架构设计
- 解码模块的实现
- 音视频播放模块
- 音视频同步模块AVSync的设计
- ijkPlayer解析

### 抖音短视频

- 音频效果器
  - 均衡效果器
  - 压缩效果器
  - 混响效果器
  - SOX效果器
- 视频效果器
  - 亮度、对比度、饱和度调节
  - 高斯模糊算法
  - FFmpeg内部的视频滤镜
  - OpenGL ES
  - 水印、自定义文字
  - 视觉特效处理
    - 美颜
    - 分屏
    - 责任链设计

### 斗鱼直播

- 直播场景分析
  - 交互式直播与非交互式直播
  - RTMP/HLS/HDL/RTP协议
  - CDN厂商服务
  - 直播系统架构模块分析
  - 流媒体服务器搭建
- 推流器的构建
  - 推流过程分析
  - 网络超时处理
  - 网络抖动、弱网环境丢帧策略
  - 自适应码率设计与MediaCodec动态码率配置
- 录屏直播推流
  - MediaProjection获取屏幕图像
  - MediaCodec与MediaProjection的结合
  - librtmp推流斗鱼直播间
- 摄像头直播推流
  - libx264的码率控制

### 微信音视频通话

- WebRTC架构与运行机制
  - NAT打洞原理与类型检测
  - STUN与TURN协议
  - ICE框架
- 通话服务器搭建
  - 房间服务器搭建
  - 信令服务器搭建
  - 打洞服务器搭建
- P2P语音视频通话
  - Android端WebRtc底层中间件设计与封装
  - WebSocket与SpringBoot实现信令服务器
  - SignalingServer信令服务器与ROOM管理

### 串口编程

- 波特率、起始化、数据位、奇偶校验位与停止位
- 串口指令如何被执行
- 寻找串口过程演绎
- 串口编程实战

## Flutter

### Flutter基础知识

- 认识Flutter
  - 原生开发与跨平台技术
  - 初识Flutter
  - Flutter环境
- Dart语言
  - 变量、函数、操作符、异常
  - 类加载机制
  - 初始化列表规则
  - 命名构造方式
  - Mixin
- Flutter使用
  - 常用widget讲解
  - 常用布局分析
  - 如何自定义View
  - 动画、手势交互
  - 与原生交互

### Flutter启动分析

- Dart虚拟机
  - Dart虚拟机概述
  - Dart虚拟机创建
  - Isolate创建
  - DartIsolate运行
- Flutter启动引擎
  - FlutterApplicaion启动流程
  - FlutterActivity启动流程
  - Flutter引擎启动流程
  Dart代码加载
- Flutter应用启动分析
  - runApp启动流程
  - 实例初始化
  - Widget绑定
  - 视图树挂载
  - 调度预热帧

### Flutter架构分析

- TaskRunner工作原理
- Widget架构
  - StatelessWidget
  - StatefulWidget
- Flutter动画原理
  - 补间动画原理
  - 物理动画原理
  - 动画流程
- Flutter渲染机制
  - UI线程渲染
  - GPU线程渲染

### Flutter通信原理

- Flutter消息机制
  - MessageLoop启动
  - TaskRunner
- Flutter的Platform Channel原理
  - MethodChannel
  - 引擎层原理
  - 宿主层原理
- Flutter异步Future机制
  - Future创建过程
  - 任务发送
  - 任务接收与管理

### Flutter进阶

- Flutter混合开发
  - 组件化Flutter工程
- Flutter 2.0新特性
- Flutter性能监控

## 微信小程序

- 小程序简介
  - 小程序技术方案
  - 平台注册与配置
  - 开发工具
  - MINA框架解析
  - 应用程序配置详解
  - 逻辑与界面分享
  - 单向数据流
- UI开发
  - 复杂页面布局
  - 图文呈现
  - 表单交互
  - 对话框交互
  - 下拉刷新与上拉加载
  - 图形与动画
  - 页面跳转过渡
  - 界面事件处理
- API操作
  - 多媒体操作
  - 网络通信
  - 本地存储及文件操作
  - 地理位置信息
  - 设备信息获取
  - 系统功能(扫码、打电话)
  - 界面交互
- 微信对接
  - 微信登录
  - 用户信息获取
  - 微信支付
  - 客服消息
  - 开放数据
  - 小程序更新
  - 第三方平台

## 鸿蒙系统

### 基础功能

- Ability
  - Page Ability
    - Page与Ability生命周期
    - AbilitySlice路由与导航
    - 使用Intent完成页面跳转
    - 使用IAbilityContinuation实现Page跨设备迁移
  - Service Ability
    - 服务的创建与生命周期
    - 前台服务与后台服务
    - 启动本地服务与启动远程设备服务
    - Service通信
  - Data Ability
    - 数据持久化
    - 使用UserDataAbility创建数据提供方
      - 数据管理
        - 事务、数据库加密
        - 关系型数据库
        - 对象映射关系型数据库
        - 轻量级偏好数据库
        - 分布式数据库
        - 分布式文件服务
        - 融合搜索与数据存储管理
      - 使用DataAbilityHelper与数据提供方通信
  - Ability Form
- UI
  - 组件与布局
    - UI组件类型
    - 使用代码创建布局
    - 使用xml创建布局
  - 常用组件与布局
    - Text、Button与Image
    - DirectionLayout
    - DependentLayout
  - 动画
    - 数值动画
    - 属性动画
    - 动画集合
  - 多模输入
- CES公共事件服务
  - 系统公共事件与自定义公共事件
  - 带权、有序与粘性公共事件
  - 通知栏功能NotificationHelper
  - 通知点击效果IntentAgent
- 剪切板
  - 跨应用数据传递
- 线程与进程
  - TaskDispatcher任务分发器
    - 全局并发任务分发器GlobalTaskDispatcher
    - 并发任务分发器ParallelTaskDispatcher
    - 串行任务分发器SerialTaskDispatcher
    - 传有任务分发器SpecTaskDispatcher
      - UiTaskDispatcher与MainTaskDispatcher
  - 线程通信
    - EventHandler机制

### 多媒体

- 音视频开发
  - 音视频编解码
  - 图像编解码与位图操作
  - 相机CameraKit与声音AudioCapturer
  - 视频编解码、播放、录制与提取
  - 音频播放采集、音量管理与短音
- 网络与近场通信
  - NFC控制
  - 传统蓝牙与BLE低功耗
  - 无线局域网WLAN与P2P点对点通信
  - 网络管理
    - Socket通信
    - 流量统计与Http缓存
  - 电话服务
    - 音频呼叫与视频呼叫
    - 短信服务
- 设备管理
  - 传感器
    - 运动类传感器：计步器、陀螺仪
    - 环境类传感器：温度、湿度
    - 方向类传感器：屏幕旋转、方向
    - 光线类传感器：环境光、RGB颜色
    - 健康类与其它：心率、按压
  - LED灯与振动器
  - 位置服务
    - 定位
    - 地理编码转化
