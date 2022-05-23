---
title: Android开发小技巧 
date: 2018-12-18 14:23:22 
categories: [Android]
tags: [Android]
---

1. 不要直接使用HashMap,HashSet,Android对Map,Set做了很多优化，可以使用ArrayMap,ArraySet。当key为int时，可以使用SparseArray,当value为boolean,int
   long时，可以使用SparseBooleanArray,SparseIntArray,SparseLongArray.
2. 有时方法需要返回两个值时，可以用Pair封装一下
3. 当我们需要对数值在某个范围内做校验时，可以使用Range,如判断是否在一个区间，若小于最小值时取最小值，大于最大值时取最大值
4. 需要对View做一些动画处理时，可以使用ViewCompat类，里面封装了常用的平移，旋转，绽放，透明度等动画

<!-- more -->

5. 使用`Resources.getSystem()`获取系统Resources对象，可以直接获取一些系统xml资源，如color,String等等，调用`getDisplayMetrics()`
   可以直接获取屏幕宽高，像素密度等，并不需要Context对象
6. 使用`TypedValue.applyDimension(int, float, DisplayMetrics);`可以直接获取px,sp,dp等值，结合上一条也不需要Context,以后再写dp2px,px2dp就轻松多了
7. 为了避免Service执行完还占用资源又不想每次停止关闭Service,可以尝试使用IntentService
8. 使用Shape及其子类，能减少很多xml相关的shape资源文件
9. 使用StateListDrawable类，能减少很多xml相关的选择器资源文件
10. 使用`Paint.setxfermode(PorterDuffXfermode);`能实现很多意想不到的效果
11. 使用PathMeasure可以实现很多酷炫的动画效果，使用前先了解一些贝赛尔曲线相关知识
12. 对时间日期进行格式化处理时使用DateFormat会非常方便
13. 使用SpannableString和SpannableStringBuilder可以实现丰富的富文本显示效果
14. TextUtils中有很多实用的字符串处理方法，可以让你少写很多代码
15. 大子线程中更新UI其实可以不用handler来处理，可以使用view.post来更新UI，延时更新UI可以使用view.postDelayed来实现。举个例子，我们实现倒计时按钮可以这么做:

```java
public class Demo {
    /**
     * 实现按钮倒计时功能.
     * @param textView View按钮
     * @param time 总倒计时
     * @param stop 是否停止倒计时
     */
    public void countDown(final TextView textView, int time, boolean stop) {
        final int key = textView.getId();
        if (stop) {
            textView.setTag(key, null);
            return;
        } else {
            if (!textView.isEnabled()) {
                return;
            }
            textView.setEnabled(false);
            textView.setTag(key, "");
        }
        final CharSequence text = textView.getText();
        final int[] temp = {time};
        textView.post(new Runnable() {
            @Override
            public void run() {
                temp[0]--;
                Log.e("aloe", "--> " + temp[0]);
                if (textView.getTag(key) != null && temp[0] > 0) {
                    textView.setText(String.format("%1$s s", temp[0]));
                    textView.postDelayed(this, 1000);
                } else {
                    textView.setText(text);
                    textView.setEnabled(true);
                }
            }
        });
    }
}
```

16. 动态获取一个唯一id`ViewCompat.generateViewId()`，这样就不用在ids.xml中写id了
17. `ViewCompat.animate(view)`可以很方便的实现平移，旋转，绽放透明度等渐变动画，注意使用后面带By的方法，不然重复执行没有效果。
