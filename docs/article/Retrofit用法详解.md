---
title: Retrofit用法详解 
date: 2018-12-27 21:28:06 
categories: [网络]
---

### 1.七种请求方式

首先定义一个接口类

```java
public interface Api {
    @GET("get")
    Call<ResponseBody> getData();

    @POST("post")
    Call<ResponseBody> postData();

    @DELETE("delete")
    Call<ResponseBody> deleteData();

    @HEAD("head")
    Call<Void> headData();

    @PATCH("patch")
    Call<ResponseBody> patchData();

    @PUT("put")
    Call<ResponseBody> putData();

    @OPTIONS("options")
    Call<Void> optionsData();
}
```

异步请求网络

```java
public class Demo {
    public void use() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://httpbin.org/").build();
        Api api = retrofit.create(Api.class);
        api.getData().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                if (response.code() == 200) {
                    if (body != null) {
                        try {
                            String string = body.string();
                            String string1 = body.string();
                            Log.i("aloe", string);
                            System.out.println(string1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        });
    }
}
```

同步请求调用`execute()`方法获取`Response`进行解析。从源码可知有以下两点需要注意:

1. baseUrl需要以`/`结尾
2. `string()`只能调用一次

<!-- more -->

### 2.@Path和@Url

`@Path`是动态替换路径中的参数，`baseUrl`不变，而`@Url`是替换整个链接

```java
public interface Api {
    @GET("{path}")
    Call<ResponseBody> getData(@Path("path") String path);

    @POST
    Call<ResponseBody> postData(@Url String url);
}
```

### 3.FORM表单请求

`@FormUrlEncoded`标记以FORM表单的方式请求服务器，与`@Field`或`@FieldMap`一起使用

```java
public interface Api {
    @POST("post")
    @FormUrlEncoded
    Call<ResponseBody> postData(@Field("name") String name, @FieldMap Map<String, String> map);
}
```

### 4.请求链接中加入参数

`@QueryName`是直接在链接后加入参数，比如<http://httpbin.org/get?name1&name2&name3>  
`@Query`是以键值对的形式在链接后加入参数，比如<http://httpbin.org/get?name=aloe&age=18>  
`@QueryMap`也是以键值对加入参数，但键值对的个数不确定

```java
public interface Api {
    @GET("get")
    Call<ResponseBody> getData1(@QueryName String... params);

    @GET("get")
    Call<ResponseBody> getData2(@Query("name") String name);

    @GET("get")
    Call<ResponseBody> getData3(@QueryMap Map<String, String> map);
}
```

### 5.请求头

`@Headers`注解方法添加请求头  
`@Header`注解参数，以键值对形式添加请求头  
`@HeaderMap`注解参数，以键值对形式，不确定个数添加请求头

```java
public interface Api {
    @POST("post")
    @Headers({"key1:value1", "key2:value2"})
    Call<ResponseBody> postData4(@Header("name") String name, @HeaderMap Map<String, String> map);
}
```

### 6.请求体传参

`@Body`将参数放在请求体中传给服务器

```java
public interface Api {
    @POST("post")
    Call<ResponseBody> postData(@Body String params);

    @POST("post")
    Call<ResponseBody> postData(@Body RequestBody params);
}
```

需要注意的时`@Body`注解的参数若不是`RequestBody`对象，则需要添加json解析器，`Retrofit`提供了`Gson`解析`json`。`@Body`只允许注解一个参数

### 7.上传文件

`@Multipart`注解方法声明上传文件  
`@Part`注解`MultipartBody.Part`用来上传文件  
`@Part`注解`RequestBody`用来发送参数，此时`@Part`需添加一个key值  
`@PartMap`用来上传多个文件

```java
public interface Api {
    @POST("post")
    @Multipart
    Call<ResponseBody> getData1(@Part("name") RequestBody params, @Part MultipartBody.Part file);

    @POST("post")
    @Multipart
    Call<ResponseBody> getData1(@PartMap Map<String, RequestBody> map);
}
```

调用方式如下

```java
public class Demo {
    public void use() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://httpbin.org/").build();
        Api api = retrofit.create(Api.class);
        Callback<ResponseBody> callback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody body = response.body();
                if (response.code() == 200) {
                    if (body != null) {
                        try {
                            String string = body.string();
                            Log.i("aloe", string);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println(t.getMessage());
            }
        };
        RequestBody params = MultipartBody.create(MultipartBody.FORM, "upload file!");
        RequestBody body = MultipartBody.create(MultipartBody.FORM, file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);
        api.getData1(params, part).enqueue(callback);

        Map<String, RequestBody> map = new ArrayMap<>();
        map.put("name", params);
        map.put("file", body);
        api.getData1(map).enqueue(callback);
    }
}
```

### 8.文件下载

```java
public interface Api {
    @GET
    @Streaming
    Call<ResponseBody> download(@Url String url);
}
```

`@Streaming`当文件过大时，一定得加上这个注解，防止内存溢出，调用方式如下

```java
public class Demo {
    public void use() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://httpbin.org/").build();
        Api api = retrofit.create(Api.class);
        api.download("http://httpbin.org/image/jpeg").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        writeFile(response);
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }

            private void writeFile(Response<ResponseBody> response) {
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "aloe.jpg"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                byte[] bytes = new byte[1024];
                ResponseBody body = response.body();
                if (body == null) {
                    return;
                }
                InputStream stream = body.byteStream();
                int read;
                try {
                    while ((read = stream.read(bytes)) != -1) {
                        fos.write(bytes, 0, read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                closeStream(stream);
                closeStream(fos);
            }

            private void closeStream(Closeable closeable) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }
}
```

### 9.数据解析与RxJava的结合

Retrofit提供了Gson来解析json字符串，我们可以参考源码实现fastjson来解析json字符串,添加Gson解析如下

```groovy
implementation 'com.squareup.retrofit2:converter-gson:2.5.0'
```

创建实现时添加解析器，代码如下

```java
public class Demo {
    public void use() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://httpbin.org/").build();
    }
}
```

官方还提供了其它的数据解析，如字符串,xml等等,技术的解析器如下

* [Gson](https://github.com/google/gson): com.squareup.retrofit2:converter-gson
* [Jackson](https://github.com/FasterXML/jackson): com.squareup.retrofit2:converter-jackson
* [Moshi](https://github.com/square/moshi/): com.squareup.retrofit2:converter-moshi
* [Protobuf](https://github.com/protocolbuffers/protobuf): com.squareup.retrofit2:converter-protobuf
* [Wire](https://github.com/square/wire): com.squareup.retrofit2:converter-wire
* [Simple XML](http://simple.sourceforge.net/): com.squareup.retrofit2:converter-simplexml
* [JAXB](https://docs.oracle.com/javase/tutorial/jaxb/intro/index.html): com.squareup.retrofit2:converter-jaxb
* Scalars (primitives, boxed, and String): com.squareup.retrofit2:converter-scalars

使用[RxJava](https://github.com/ReactiveX/RxJava)来处理数据会给你带来意想不到的效果，添加依赖如下  
初始化方式如下

```java
public class Demo {
    public void use() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://httpbin.org/").build();
    }
}
```
