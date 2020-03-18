# **Kotlin记录**

1. Android内有部分的方法是类似function(String... data)的，这时在Kotlin中需要在arrayOf前面加*将数组变成可变数组。当需要在kotlin中创建这样的方法时可以在变量前面加入vararg让参数变为可变数组，例如：function(vararg strings:String)。