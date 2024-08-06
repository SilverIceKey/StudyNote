# 关于编译Android使用的LibreOffice

编译环境：Ubuntu 22.04

假如编译目录：/usr/local/project/android

接下来包括我的踩坑操作，有些操作顺序你可以按照你的想法去更改

### 1、安装依赖

```bash
sudo apt-get update
sudo apt-get install -y build-essential perl python3 libtool autoconf automake \
                        unzip libboost-dev libboost-filesystem-dev \
                        libboost-iostreams-dev libboost-locale-dev \
                        libboost-regex-dev libboost-system-dev libboost-thread-dev \
                        libclucene-dev libcups2-dev libfontconfig1-dev \
                        libgconf2-dev libgstreamer-plugins-base1.0-dev \
                        libgstreamer1.0-dev libgtk-3-dev libharfbuzz-dev \
                        libxml2-utils libxslt1-dev m4 nasm openjdk-8-jdk \
                        g++ git zip bison flex libcppunit-dev libpoppler-dev \
                        libpoppler-private-dev libcairo2-dev libxinerama-dev \
                        libgl1-mesa-dev libglu1-mesa-dev libfontconfig1-dev \
                        libxslt1-dev xsltproc ant
```

## 2、安装和配置Android SDK和NDK

- #### 下载NDK

  因为官方推荐NDK-r23所以我这边使用NDK-r23c

  从下面的地址：

  ```
  https://github.com/android/ndk/wiki/Unsupported-Downloads
  ```

  找到r23c,右键linux版本复制下载链接![image-20240806181106971](C:\Users\zhouwj\AppData\Roaming\Typora\typora-user-images\image-20240806181106971.png)
  
  到编译目录中，执行以下命令
  
  ```bash
  wget https://dl.google.com/android/repository/android-ndk-r23c-linux.zip
  # 下载完成之后执行
  unzip android-ndk-r23c-linux.zip
  ```
  

​	之后编译目录下有android-ndk-r23c目录，至此NDK下载完成

- #### 下载SDK

	执行以下命令

  ```bash
	mkdir -p ~/android-sdk
	cd ~/android-sdk
	wget https://dl.google.com/android/repository/commandlinetools-linux-6858069_latest.zip
	unzip commandlinetools-linux-6858069_latest.zip
	```

​	如果你是zsh终端则在~/.zshrc最后配置如下代码，如果是bash终端则在~/.bashrc最后配置如下代码

  ```bash
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/bin:$PATH
source ~/.bashrc
source ~/.zshrc
sdkmanager --sdk_root=$ANDROID_HOME "platform-tools" "platforms;android-30" "build-tools;30.0.3"
  ```

### 3、升级gcc

```bash
sudo apt-get install software-properties-common
sudo add-apt-repository ppa:ubuntu-toolchain-r/test
sudo apt-get update
sudo apt-get install gcc-12 g++-12
sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-12 60 --slave /usr/bin/g++ g++ /usr/bin/g++-12
sudo update-alternatives --config gcc
gcc --version
g++ --version
```

### 4、拉取LibreOffice代码

```bash
cd 编译地址
git clone https://gerrit.libreoffice.org/core libreoffice
```

### 5、切换用户

因为不支持使用root用户编译所以要切换用户

```bash
sudo adduser libreuser
su - root
visudo
#在最后添加
libreuser ALL=(ALL) NOPASSWD:ALL
su - libreuser
# 切换到具有sudo权限的用户或root用户
sudo chown -R libreuser:libreuser /usr/local/project/android/libreoffice
cd /usr/local/project/android/libreoffice
make clean
./autogen.sh --with-distro=LibreOfficeAndroid --with-android-sdk=$ANDROID_HOME --with-android-ndk=/usr/local/project/android/android-ndk-r23c --with-ant-home=/usr/share/ant
make
```

目前到此能正常进入编译，然后最后会因为gradle的版本和jdk的版本不兼容问题报错，建议使用jdk11测试。先溜。

