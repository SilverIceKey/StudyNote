# **关于Handler**

### handler在Android中十分常见，也基本为面试必问，这里简单总结下。  
> handle最常见的使用场景是在非ui线程中更新ui。  关于handler的创建在非ui线程中需要先创建Looper，说到Looper这里就顺便说一下，每个线程只有一个Looper，每个Looper存在Looper的sThreadLocal这个静态变量中，每当执行Looper.prepare()会自动调用sThreadLocal.get()来判断当前线程是否存在Looper，不存在则创建新的Looper存入，如果存在则会抛出`Only one Looper may be created per thread`异常。  
> 在非ui线程中new Handle之前需要先使用`Looper.prepare()`创建Looper之后  
> 调用 `new Handler(Looper.myLooper())`  
> 之后再调用`Looper.loop()`让线程的Looper运行，不然handler是无法发送消息的。  
> 创建的Handler可以重写handleMessage方法来处理消息。
> 主线程创建Handler无需执行`Looper.prepare()`和`Looper.loop()`，实例化Handler时不需要放入Looper。  
> 一个线程只能有一个Looper但是一个Looper能有无数个Handler，发送Message时，因为Message中有target变量用来判断Handler所以不用担心Message会发错。
