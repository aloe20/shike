---
title: Coil用法详解
date: 2021-04-18 08:15:35
categories: [Android]
tags: [Android, Coil]
---
### 概述

**Coil**是一个Android图片加载库，通过Kotlin协程的方式加载图片。特点如下

* **更快**: Coil在性能上有很多优化，包括内存缓存和磁盘缓存，把缩略图存保存在内存中，循环利用bitmap，自动暂停和取消图片网络请求等
* **更轻量**: Coil只有2000个方法（前提是你的APP里面集成了 OkHttp和Coroutines），Coil和Picasso的方法数差不多，相比 Glide和Fresco要轻量很多
* **更简单**: Coil 的 API 充分利用了 Kotlin 语言的新特性，简化和减少了很多样板代码
* **更流行**: Coil 首选 Kotlin 语言开发并且使用包含 Coroutines, OkHttp, Okio 和 AndroidX Lifecycles 在内最流行的开源库

Coil 名字的由来：取 **Co**routine **I**mage **L**oader 首字母得来

### 下载

Coil可以在`mavenCentral()`下载

```groovy
implementation("io.coil-kt:coil:1.2.0")
```

### 快速上手

可以使用`ImageView`的扩展函数`load`加载一张图片：

```kotlin
// URL
imageView.load("https://www.example.com/image.jpg")

// Resource
imageView.load(R.drawable.image)

// File
imageView.load(File("/path/to/image.jpg"))
```

支持的类型有

* String
* HttpUrl
* Uri(仅支持`android.resource`, `content`, `file`, `http`和`https`类型的schemes)
* File
* @DrawableRes Int
* Drawable
* Bitmap
