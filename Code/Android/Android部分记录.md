# **Java部分记录**

### 1. Calender中的月份是零基的即获取后需要+1为当前月份。
### 2. Activity在返回时的方法为onRestart->onStart->onResume。可以尝试在onRestart中刷新数据，onResume因为首次启动与返回都会被执行，反而不是很方便。  
### 3. singleTop启动模式，当Activity在栈顶时start该Activity不会创建新的Activity但是会调用onNewIntent。  
### 4. 系统销毁Activity时会调用onSaveInstanceState，用于存储数据方便之后创建调用。  
### 5. 单例模式如果传入的Context为Activity会使Activity无法被回收从而造成内存泄漏。  
### 6. 直接实例化抽象类或接口容易造成Activity被持有，如果传入异步线程会出现Activity泄漏。  
### 7. bitmap需要先调用recycle之后再设置为null
### 8. 如果内部类的存在时间要比Activity要长，则使用静态内部类并使用弱引用指向当前类。  
### 9. Recyclerview中如果不设置isLayoutFrozen为true则列表的空白部分点击无效。如果设置为true则会出现Glide图片设置的Size偏小，暂时的解决，调用Recyclerview的viewTreeObserver冰添加onGlobalLayoutListener，同时在设置时使用延时设置，目前使用延时100ms无问题。犹豫Imageview高度为wrap_content，因此受回收影响滑动会出现界面跳动。 
### 10. Android Studio gradle 阿里云镜像 [详情](https://help.aliyun.com/document_detail/102512.html?spm=a2c40.aliyun_maven_repo.0.0.361830549jTnxB)
```
maven{ url'http://maven.aliyun.com/nexus/content/groups/public/' }//google()
maven{ url'http://maven.aliyun.com/nexus/content/groups/public/' }  //central仓和jcenter仓的聚合仓
maven{ url'http://maven.aliyun.com/nexus/content/repositories/jcenter'}//jcenter()
```  
### 11. Realm kotlin 出现 错误: 无法从最终xxxmodel进行继承的错误解决，将model变为open