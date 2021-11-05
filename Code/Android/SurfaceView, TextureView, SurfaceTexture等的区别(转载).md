# [SurfaceView, TextureView, SurfaceTexture等的区别(转载)](https://www.cnblogs.com/wytiger/p/5693569.html)  
# 转载自：SurfaceView, TextureView, SurfaceTexture等的区别（https://www.cnblogs.com/wytiger/p/5693569.html）
SurfaceView, GLSurfaceView, SurfaceTexture以及TextureView是Android当中名字比较绕，关系又比较密切的几个类。本文基于Android 5.0(Lollipop)的代码理一下它们的基本原理，联系与区别。  
## SurfaceView  
从Android 1.0(API level 1)时就有 。它继承自类View，因此它本质上是一个View。但与普通View不同的是，它有自己的Surface。我们知道，一般的Activity包含的多个View会组成View hierachy的树形结构，只有最顶层的DecorView，也就是根结点视图，才是对WMS可见的。这个DecorView在WMS中有一个对应的WindowState。相应地，在SF中对应的Layer。而SurfaceView自带一个Surface，这个Surface在WMS中有自己对应的WindowState，在SF中也会有自己的Layer。如下图所示：  
![SurfaceView](.\images\20150304164219975.png)  
也就是说，虽然在Client端(App)它仍在View hierachy中，但在Server端（WMS和SF）中，它与宿主窗口是分离的。这样的好处是对这个Surface的渲染可以放到单独线程去做，渲染时可以有自己的GL context。这对于一些游戏、视频等性能相关的应用非常有益，因为它不会影响主线程对事件的响应。但它也有缺点，因为这个Surface不在View hierachy中，它的显示也不受View的属性控制，所以不能进行平移，缩放等变换，也不能放在其它ViewGroup中，一些View中的特性也无法使用。  
## GLSurfaceView  
从Android 1.5(API level 3)开始加入，作为SurfaceView的补充。它可以看作是SurfaceView的一种典型使用模式。在SurfaceView的基础上，它加入了EGL的管理，并自带了渲染线程。另外它定义了用户需要实现的Render接口，提供了用Strategy pattern更改具体Render行为的灵活性。作为GLSurfaceView的Client，只需要将实现了渲染函数的Renderer的实现类设置给GLSurfaceView即可。如：  
```
public class TriangleActivity extends Activity {  
    protected void onCreate(Bundle savedInstanceState) {  
        mGLView = new GLSurfaceView(this);  
        mGLView.setRenderer(new RendererImpl(this));  
```  
相关类图如下。其中SurfaceView中的SurfaceHolder主要是提供了一坨操作Surface的接口。GLSurfaceView中的EglHelper和GLThread分别实现了上面提到的管理EGL环境和渲染线程的工作。GLSurfaceView的使用者需要实现Renderer接口。  
![GLSurfaceView](.\images\20150305081903285.png)  
## SurfaceTexture  
从Android 3.0(API level 11)加入。和SurfaceView不同的是，它对图像流的处理并不直接显示，而是转为GL外部纹理，因此可用于图像流数据的二次处理（如Camera滤镜，桌面特效等）。比如Camera的预览数据，变成纹理后可以交给GLSurfaceView直接显示，也可以通过SurfaceTexture交给TextureView作为View heirachy中的一个硬件加速层来显示。首先，SurfaceTexture从图像流（来自Camera预览，视频解码，GL绘制场景等）中获得帧数据，当调用updateTexImage()时，根据内容流中最近的图像更新SurfaceTexture对应的GL纹理对象，接下来，就可以像操作普通GL纹理一样操作它了。从下面的类图中可以看出，它核心管理着一个BufferQueue的Consumer和Producer两端。Producer端用于内容流的源输出数据，Consumer端用于拿GraphicBuffer并生成纹理。SurfaceTexture.OnFrameAvailableListener用于让SurfaceTexture的使用者知道有新数据到来。JNISurfaceTextureContext是OnFrameAvailableListener从Native到Java的JNI跳板。其中SurfaceTexture中的attachToGLContext()和detachToGLContext()可以让多个GL context共享同一个内容源。  
![SurfaceTexture](.\images\20150305082032854.png)  
Android 5.0中将BufferQueue的核心功能分离出来，放在BufferQueueCore这个类中。BufferQueueProducer和BufferQueueConsumer分别是它的生产者和消费者实现基类（分别实现了IGraphicBufferProducer和IGraphicBufferConsumer接口）。它们都是由BufferQueue的静态函数createBufferQueue()来创建的。Surface是生产者端的实现类，提供dequeueBuffer/queueBuffer等硬件渲染接口，和lockCanvas/unlockCanvasAndPost等软件渲染接口，使内容流的源可以往BufferQueue中填graphic buffer。GLConsumer继承自ConsumerBase，是消费者端的实现类。它在基类的基础上添加了GL相关的操作，如将graphic buffer中的内容转为GL纹理等操作。到此，以SurfaceTexture为中心的一个pipeline大体是这样的：  
![SurfaceTexture](.\images\20150305082434209.png)  
## TextureView  
在4.0(API level 14)中引入。它可以将内容流直接投影到View中，可以用于实现Live preview等功能。和SurfaceView不同，它不会在WMS中单独创建窗口，而是作为View hierachy中的一个普通View，因此可以和其它普通View一样进行移动，旋转，缩放，动画等变化。值得注意的是TextureView必须在硬件加速的窗口中。它显示的内容流数据可以来自App进程或是远端进程。从类图中可以看到，TextureView继承自View，它与其它的View一样在View hierachy中管理与绘制。TextureView重载了draw()方法，其中主要把SurfaceTexture中收到的图像数据作为纹理更新到对应的HardwareLayer中。SurfaceTexture.OnFrameAvailableListener用于通知TextureView内容流有新图像到来。SurfaceTextureListener接口用于让TextureView的使用者知道SurfaceTexture已准备好，这样就可以把SurfaceTexture交给相应的内容源。Surface为BufferQueue的Producer接口实现类，使生产者可以通过它的软件或硬件渲染接口为SurfaceTexture内部的BufferQueue提供graphic buffer。  
![TextureView](.\images\20150305082558158.jpg)  
下面以VideoDumpView.java（位于/frameworks/base/media/tests/MediaDump/src/com/android/mediadump/）为例分析下SurfaceTexture的使用。这个例子的效果是从MediaPlayer中拿到视频帧，然后显示在屏幕上，接着把屏幕上的内容dump到指定文件中。因为SurfaceTexture本身只产生纹理，所以这里还需要GLSurfaceView配合来做最后的渲染输出。
 
首先，VideoDumpView是GLSurfaceView的继承类。在构造函数VideoDumpView()中会创建VideoDumpRenderer，也就是GLSurfaceView.Renderer的实例，然后调setRenderer()将之设成GLSurfaceView的Renderer。  
```
public VideoDumpView(Context context) {  
...  
        mRenderer = new VideoDumpRenderer(context);  
        setRenderer(mRenderer);  
    }  
```  
随后，GLSurfaceView中的GLThread启动，创建EGL环境后回调VideoDumpRenderer中的onSurfaceCreated()。  
```
public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {  
...  
    // Create our texture. This has to be done each time the surface is created.  
    int[] textures = new int[1];  
    GLES20.glGenTextures(1, textures, 0);  
  
    mTextureID = textures[0];  
    GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);  
...  
    mSurface = new SurfaceTexture(mTextureID);  
    mSurface.setOnFrameAvailableListener(this);  
  
    Surface surface = new Surface(mSurface);  
    mMediaPlayer.setSurface(surface);
```  
这里，首先通过GLES创建GL的外部纹理。外部纹理说明它的真正内容是放在ion分配出来的系统物理内存中，而不是GPU中，GPU中只是维护了其元数据。接着根据前面创建的GL纹理对象创建SurfaceTexture。流程如下：  
![GL纹理对象创建SurfaceTexture](.\images\20150305082941877.jpg)  
SurfaceTexture的参数为GLES接口函数glGenTexture()得到的纹理对象id。在初始化函数SurfaceTexture_init()中，先创建GLConsumer和相应的BufferQueue，再将它们的指针通过JNI放到SurfaceTexture的Java层对象成员中。  
```
static void SurfaceTexture_init(JNIEnv* env, jobject thiz, jboolean isDetached,  
        jint texName, jboolean singleBufferMode, jobject weakThiz)  
{  
...  
    BufferQueue::createBufferQueue(&producer, &consumer);  
...  
    sp<GLConsumer> surfaceTexture;  
    if (isDetached) {  
        surfaceTexture = new GLConsumer(consumer, GL_TEXTURE_EXTERNAL_OES,  
                true, true);  
    } else {  
        surfaceTexture = new GLConsumer(consumer, texName,  
                GL_TEXTURE_EXTERNAL_OES, true, true);  
    }  
...  
    SurfaceTexture_setSurfaceTexture(env, thiz, surfaceTexture);  
    SurfaceTexture_setProducer(env, thiz, producer);  
...  
    sp<JNISurfaceTextureContext> ctx(new JNISurfaceTextureContext(env, weakThiz,  
            clazz));  
    surfaceTexture->setFrameAvailableListener(ctx);  
    SurfaceTexture_setFrameAvailableListener(env, thiz, ctx);  
```  
由于直接的Listener在Java层，而触发者在Native层，因此需要从Native层回调到Java层。这里通过JNISurfaceTextureContext当了跳板。JNISurfaceTextureContext的onFrameAvailable()起到了Native和Java的桥接作用：  
```
void JNISurfaceTextureContext::onFrameAvailable()  
...  
    env->CallStaticVoidMethod(mClazz, fields.postEvent, mWeakThiz);  
```  
其中的fields.postEvent早在SurfaceTexture_classInit()中被初始化为SurfaceTexture的postEventFromNative()函数。这个函数往所在线程的消息队列中放入消息，异步调用VideoDumpRenderer的onFrameAvailable()函数，通知VideoDumpRenderer有新的数据到来。
 
回到onSurfaceCreated()，接下来创建供外部生产者使用的Surface类。Surface的构造函数之一带有参数SurfaceTexture。
```
public Surface(SurfaceTexture surfaceTexture) {  
...  
    setNativeObjectLocked(nativeCreateFromSurfaceTexture(surfaceTexture));  
```  
它实际上是把SurfaceTexture中创建的BufferQueue的Producer接口实现类拿出来后创建了相应的Surface类。  
```
static jlong nativeCreateFromSurfaceTexture(JNIEnv* env, jclass clazz,  
        jobject surfaceTextureObj) {  
    sp<IGraphicBufferProducer> producer(SurfaceTexture_getProducer(env, surfaceTextureObj));  
...  
    sp<Surface> surface(new Surface(producer, true));  
```  
这样，Surface为BufferQueue的Producer端，SurfaceTexture中的GLConsumer为BufferQueue的Consumer端。当通过Surface绘制时，SurfaceTexture可以通过updateTexImage()来将绘制结果绑定到GL的纹理中。
 
回到onSurfaceCreated()函数，接下来调用setOnFrameAvailableListener()函数将VideoDumpRenderer（实现SurfaceTexture.OnFrameAvailableListener接口）作为SurfaceTexture的Listener，因为它要监听内容流上是否有新数据。接着将SurfaceTexture传给MediaPlayer，因为这里MediaPlayer是生产者，SurfaceTexture是消费者。后者要接收前者输出的Video frame。这样，就通过Observer pattern建立起了一条通知链：MediaPlayer -> SurfaceTexture -> VideDumpRenderer。在onFrameAvailable()回调函数中，将updateSurface标志设为true，表示有新的图像到来，需要更新Surface了。为毛不在这儿马上更新纹理呢，因为当前可能不在渲染线程。SurfaceTexture对象可以在任意线程被创建（回调也会在该线程被调用），但updateTexImage()只能在含有纹理对象的GL context所在线程中被调用。因此一般情况下回调中不能直接调用updateTexImage()。
 
与此同时，GLSurfaceView中的GLThread也在运行，它会调用到VideoDumpRenderer的绘制函数onDrawFrame()。  
```
public void onDrawFrame(GL10 glUnused) {  
...  
    if (updateSurface) {  
...  
        mSurface.updateTexImage();  
        mSurface.getTransformMatrix(mSTMatrix);  
        updateSurface = false;  
...  
        // Activate the texture.  
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);  
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);  
...  
        // Draw a rectangle and render the frame as a texture on it.  
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);  
...  
    DumpToFile(frameNumber);  
```  
这里，通过SurfaceTexture的updateTexImage()将内容流中的新图像转成GL中的纹理，再进行坐标转换。绑定刚生成的纹理，画到屏幕上。整个流程如下：  
![SurfaceTexture](.\images\20150305083843743.jpg)  
最后onDrawFrame()调用DumpToFile()将屏幕上的内容倒到文件中。在DumpToFile()中，先用glReadPixels()从屏幕中把像素数据存到Buffer中，然后用FileOutputStream输出到文件。
 
上面讲了SurfaceTexture，下面看看TextureView是如何工作的。还是从例子着手，Android的关于TextureView的官方文档(http://developer.android.com/reference/android/view/TextureView.html)给了一个简洁的例子LiveCameraActivity。它它可以将Camera中的内容放在View中进行显示。在onCreate()函数中首先创建TextureView，再将Activity(实现了TextureView.SurfaceTextureListener接口)传给TextureView，用于监听SurfaceTexture准备好的信号。  
```
protected void onCreate(Bundle savedInstanceState) {  
    ...  
    mTextureView = new TextureView(this);  
    mTextureView.setSurfaceTextureListener(this);  
    ...  
}  
```  
TextureView的构造函数并不做主要的初始化工作。主要的初始化工作是在getHardwareLayer()中，而这个函数是在其基类View的draw()中调用。TextureView重载了这个函数：  
```
HardwareLayer getHardwareLayer() {  
...  
    mLayer = mAttachInfo.mHardwareRenderer.createTextureLayer();  
    if (!mUpdateSurface) {  
        // Create a new SurfaceTexture for the layer.  
        mSurface = new SurfaceTexture(false);  
        mLayer.setSurfaceTexture(mSurface);  
    }  
    mSurface.setDefaultBufferSize(getWidth(), getHeight());  
    nCreateNativeWindow(mSurface);  
  
    mSurface.setOnFrameAvailableListener(mUpdateListener, mAttachInfo.mHandler);  
  
    if (mListener != null && !mUpdateSurface) {  
        mListener.onSurfaceTextureAvailable(mSurface, getWidth(), getHeight());  
    }  
...  
    applyUpdate();  
    applyTransformMatrix();  
  
    return mLayer;  
}  
```  
因为TextureView是硬件加速层（类型为LAYER_TYPE_HARDWARE），它首先通过HardwareRenderer创建相应的HardwareLayer类，放在mLayer成员中。然后创建SurfaceTexture类，具体流程见前文。之后将HardwareLayer与SurfaceTexture做绑定。接着调用Native函数nCreateNativeWindow，它通过SurfaceTexture中的BufferQueueProducer创建Surface类。注意Surface实现了ANativeWindow接口，这意味着它可以作为EGL Surface传给EGL接口从而进行硬件绘制。然后setOnFrameAvailableListener()将监听者mUpdateListener注册到SurfaceTexture。这样，当内容流上有新的图像到来，mUpdateListener的onFrameAvailable()就会被调用。然后需要调用注册在TextureView中的SurfaceTextureListener的onSurfaceTextureAvailable()回调函数，通知TextureView的使用者SurfaceTexture已就绪。整个流程大体如下：  
![流程](.\images\20150305085308993.jpg)  
注意这里这里为TextureView创建了DeferredLayerUpdater，而不是像Android 4.4(Kitkat)中返回GLES20TextureLayer。因为Android 5.0(Lollipop)中在App端分离出了渲染线程，并将渲染工作放到该线程中。这个线程还能接收VSync信号，因此它还能自己处理动画。事实上，这里DeferredLayerUpdater的创建就是通过同步方式在渲染线程中做的。DeferredLayerUpdater，顾名思义，就是将Layer的更新请求先记录在这，当渲染线程真正要画的时候，再进行真正的操作。其中的setSurfaceTexture()会调用HardwareLayer的Native函数nSetSurfaceTexture()将SurfaceTexture中的surfaceTexture成员（类型为GLConsumer）传给DeferredLayerUpdater，这样之后要更新纹理时DeferredLayerUpdater就知道从哪里更新了。
 
前面提到初始化中会调用onSurfaceTextureAvailable()这个回调函数。在它的实现中，TextureView的使用者就可以将准备好的SurfaceTexture传给数据源模块，供数据源输出之用。如：  
```
public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {  
    mCamera = Camera.open();  
        ...  
        mCamera.setPreviewTexture(surface);  
        mCamera.startPreview();  
        ...  
}  
```  
看一下setPreviewTexture()的实现，其中把SurfaceTexture中初始化时创建的GraphicBufferProducer拿出来传给Camera模块。  
```
static void android_hardware_Camera_setPreviewTexture(JNIEnv *env,  
    jobject thiz, jobject jSurfaceTexture)  
...  
    producer = SurfaceTexture_getProducer(env, jSurfaceTexture);  
...  
    if (camera->setPreviewTarget(producer) != NO_ERROR) {  
```  
到这里，一切都初始化地差不多了。接下来当内容流有新图像可用，TextureView会被通知到（通过SurfaceTexture.OnFrameAvailableListener接口）。SurfaceTexture.OnFrameAvailableListener是SurfaceTexture有新内容来时的回调接口。TextureView中的mUpdateListener实现了该接口：  
```
public void onFrameAvailable(SurfaceTexture surfaceTexture) {  
    updateLayer();  
    invalidate();  
}  
```  
可以看到其中会调用updateLayer()函数，然后通过invalidate()函数申请更新UI。updateLayer()会设置mUpdateLayer标志位。这样，当下次VSync到来时，Choreographer通知App通过重绘View hierachy。在UI重绘函数performTranversals()中，作为View hierachy的一分子，TextureView的draw()函数被调用，其中便会相继调用applyUpdate()和HardwareLayer的updateSurfaceTexture()函数。  
```
public void updateSurfaceTexture() {  
    nUpdateSurfaceTexture(mFinalizer.get());  
    mRenderer.pushLayerUpdate(this);  
}  
```  
updateSurfaceTexture()实际通过JNI调用到android_view_HardwareLayer_updateSurfaceTexture()函数。在其中会设置相应DeferredLayerUpdater的标志位mUpdateTexImage，它表示在渲染线程中需要更新该层的纹理。  
![流程图](.\images\20150305084333832.jpg)  
前面提到，Android 5.0引入了渲染线程，它是一个更大的topic，超出本文范围，这里只说相关的部分。作为背景知识，下面只画出了相关的类。可以看到，ThreadedRenderer作为新的HardwareRenderer替代了Android 4.4中的Gl20Renderer。其中比较关键的是RenderProxy类，需要让渲染线程干活时就通过这个类往渲染线程发任务。RenderProxy中指向的RenderThread就是渲染线程的主体了，其中的threadLoop()函数是主循环，大多数时间它会poll在线程的Looper上等待，当有同步请求（或者VSync信号）过来，它会被唤醒，然后处理TaskQueue中的任务。TaskQueue是RenderTask的队列，RenderTask代表一个渲染线程中的任务。如DrawFrameTask就是RenderTask的继承类之一，它主要用于渲染当前帧。而DrawFrameTask中的DeferredLayerUpdater集合就存放着之前对硬件加速层的更新操作申请。  
![DrawFrameTask](.\images\20150305085434529.jpg)  
当主线程准备好渲染数据后，会以同步方式让渲染线程完成渲染工作。其中会先调用processLayerUpdate()更新所有硬件加速层中的属性，继而调用到DeferredLayerUpdater的apply()函数，其中检测到标志位mUpdateTexImage被置位，于是会调用doUpdateTexImage()真正更新GL纹理和转换坐标。  
![流程](.\images\20150305084333832.jpg)  
最后，总结下这几者的区别和联系。简单地说:
SurfaceView是一个有自己独立Surface的View, 它的渲染可以放在单独线程而不是主线程中, 其缺点是不能做变形和动画。
SurfaceTexture可以用作非直接输出的内容流，这样就提供二次处理的机会。与SurfaceView直接输出相比，这样会有若干帧的延迟。同时，由于它本身管理BufferQueue，因此内存消耗也会稍微大一些。
TextureView是一个可以把内容流作为外部纹理输出在上面的View, 它本身需要是一个硬件加速层。
事实上TextureView本身也包含了SurfaceTexture, 它与SurfaceView+SurfaceTexture组合相比可以完成类似的功能（即把内容流上的图像转成纹理，然后输出）, 区别在于TextureView是在View hierachy中做绘制，因此一般它是在主线程上做的（在Android 5.0引入渲染线程后，它是在渲染线程中做的）。而SurfaceView+SurfaceTexture在单独的Surface上做绘制，可以是用户提供的线程，而不是系统的主线程或是渲染线程。另外，与TextureView相比，它还有个好处是可以用Hardware overlay进行显示。