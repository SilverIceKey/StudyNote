# **关于浏览器唤醒App**
### 浏览器唤醒App需要在Manifest中想要唤醒的Activity标签下添加如下代码：
```
<intent-filter>
    <action android:name="android.intent.action.VIEW"/>
    <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE"/>
    <data android:scheme="链接协议如：http,data" android:host="域名即可"/>
</intent-filter>
```