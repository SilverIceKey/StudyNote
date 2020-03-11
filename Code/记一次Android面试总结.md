# **记一次Android面试总结**  
这次面试大多是关于基础知识的询问。基础知识薄弱的我当然失败了，所以来做个总结，本次面试主要是自定义View、动画以及Java基础，总结是我查询网上之后加上自己的一个认知。  
> ### **1.  插值器(Interpolator)和估值器(TypeEvaluator)的作用**  
经过网上的查询，先说我的认知，我认为插值器用于描述动画的运动规律，估值器用于计算动画的具体数值。  
以下说明为网上的摘抄已经我个人的理解。  
首先插值器和估值器均为接口，系统内置部分插值器和估值器供用户使用，如需实现较为复杂的动画场景的话需要自行实现插值器和估值器的接口。  
系统的内置9种插值器有：  
|作用|资源ID|对应Java类|
|:-:|:-:|:-:|
|加速骤停|@android:anim/accelerate_interpolator|AccelerateInterpolator|
|快速完成，单次回弹|@android:anim/overshoot_interpolator|OvershootInterpolator|
|先加速再减速|@android:anim/accelerate_decelerate_interpolator|AccelerateDecelerateInterpolator|
|先后退再加速前进骤停|@android:anim/anticipate_interpolator|AnticipateInterpolator|
|先后退再加速超出单次回弹|@android:anim/anticipate_overshoot_interpolator|AnticipateOvershootInterpolator|
|加速，结束弹簧|@android:anim/bounce_interpolator|BounceInterpolator|
|周期回程超出边界|@android:anim/cycle_interpolator|CycleInterpolator|
|减速|@android:anim/decelerate_interpolator|DecelerateInterpolator|
|匀速(线性动画)|@android:anim/linear_interpolator|LinearInterpolator|  
自定义插值器可以实现Interpolator或者实现TimeInterpolator。  
源码中Interpolator继承自TimeInterpolator并且其中没有任何方法，因此相当于实现TimeInterpolator中的getInterpolation方法。  
此处贴出Interpolator的官方说明：  
``` 
    // A new interface, TimeInterpolator, was introduced for the new android.animation
    // package. This older Interpolator interface extends TimeInterpolator so that users of
    // the new Animator-based animations can use either the old Interpolator implementations or
    // new classes that implement TimeInterpolator directly.
```  
由此可见，TimeInterpolator为Interpolator的升级版（加了getInterpolation方法）。补充一点，在不设置插值器的情况下，官方默认使用AccelerateDecelerateInterpolator插值器。  
接下来说说估值器，官方内置的估值器有以下三种：  
|类型|Java类|初始化|
|:-:|:-:|:-:|
|整型|IntEvaluator|ObjectAnimator.ofInt()|
|浮点型|FloatEvaluator|ObjectAnimator.ofFloat()|
|矩阵浮点类型?|??|ObjectAnimator.ofMultiFloat()|
|矩阵整型类型?|??|ObjectAnimator.ofMultiInt()|
|argb类型|ArgbEvaluator|ObjectAnimator.ofArgb()| 
当以上类型无法满足需求时就需要用到自定义估值器，实现TypeEvaluator<T>接口并且使用ObjectAnimator.ofObject()中添加自定义估值器的实例化类。  
自定义估值器需要实现TypeEvaluator接口中的evaluate方法。  
同时可以调用Animator.addUpdateListener来监听估值器在每次刷新所产生的值，并且调用invalidate方法来刷新界面（在View中）。  
说到invalidate方法就不得不说requestLayout、invalidate、postInvalidate三个方法的区别了。  
> ### **2. requestLayout、invalidate、postInvalidate三个方法的区别**  
同样经过查询，我先说我的认知，大家都知道View的绘制流程有3个步骤：Measure、Layout、Draw。requestLayout刷新为从Measure开始，而invalidate只是重新执行Draw,postInvalide相同，只不过postInvalidate是从异步线程调用ui线程使用，原理是使用handler,今天先到这里。