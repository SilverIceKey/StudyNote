# **Java部分记录**

### 1. Calender中的月份是零基的即获取后需要+1为当前月份。
### 2. Activity在返回时的方法为onRestart->onStart->onResume。可以尝试在onRestart中刷新数据，onResume因为首次启动与返回都会被执行，反而不是很方便。  
### 3. singleTop启动模式，当Activity在栈顶时start该Activity不会创建新的Activity但是会调用onNewIntent。  
### 4. 系统销毁Activity时会调用onSaveInstanceState，用于存储数据方便之后创建调用。  
### 5. 单例模式如果传入的Context为Activity会使Activity无法被回收从而造成内存泄漏。  