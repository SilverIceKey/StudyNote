# 关于jitpack发布的gradle(DSL)的配置

### Jitpack中的JDK版本定义

- 定义编译的JDK版本，默认jitpack使用JDK8来进行编译发布，如果要使用高版本的话，需要在项目根目录创建jitpack.yml文件，并且在其中指定JDK版本，示例：

  ```yml
  jdk:
    - openjdk17
  ```

### Gradle的配置

目前比较新创建的项目中依赖的详细一般会写在根目录下的gradle/libs.versions.toml中，我们也用这种方式配置：

在[libararies]分类下增加依赖：

```toml
//[libararies]
gradle = { module = "com.android.tools.build:gradle", version.ref = "gradle" }
android-maven-gradle-plugin = { module = "com.github.dcendents:android-maven-gradle-plugin", version.ref = "androidMavenGradlePlugin" }
```

在[versions]分类下面增加版本号：

```toml
//[versions]
androidMavenGradlePlugin = "2.1"
gradle = "7.4.2"
```

具体的版本号可以自行查询

#### gradle配置：

- 项目gradle，在顶部增加如下代码

  ```kotlin
  buildscript {
      dependencies {
          classpath(libs.android.maven.gradle.plugin)
          classpath(libs.gradle)
      }
  }
  ```

- 需要发布的模块的gradle配置

  ```kotlin
  android{
      ...
  }
  publishing {
      publications {
          register<MavenPublication>("release") {
              afterEvaluate {
                  from(components["release"])
                  groupId = project.findProperty("GROUP_ID") as String // 使用 GROUP_ID 属性
                  artifactId = "模块名称"
                  version = project.findProperty("VERSION") as String // 使用 VERSION_NAME 属性
              }
          }
      }
  }
  ```

- 如果是单模块发布，那么groupId和version可以直接在要发布的模块中编写，如果是多模块发布，为了保证groupId和version的统一，那么可以在根目录的gradle.properties文件中进行声明，可以增加

  ```properties
  GROUP_ID=groupId
  VERSION=版本号
  ```

  key-value值，内容可自行替换，然后在要引用的模块中使用，

  ```kotlin
  project.findProperty("key") as String
  ```

  进行引用

- 配置完成之后推送到github，然后进入jitpack中，输入项目URL点击Look Up查看项目，点击Commits标签可以先试发布，能正常发布之后，再回到github进行Release，然后在回来进行正式的一个发布就完成了