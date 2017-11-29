
# StringFog
一款自动对dex文件中的字符串进行加密Android插件工具，正如名字所言，给字符串加上一层雾霭，使人难以窥视其真面目，且支持在app和sdk中独立使用。


### 原理
在java文件编译成class字节码后，class字节码压缩成dex文件前，利用ASM库对class字节码中的字符串进行加密，同时将解密调用自动写进class字节码中，做到在运行时还原字符串内容，举个栗子：
- 原文：
```
String a = "This is a string!";
```
- 加密：
```
String a = Decoder.decode("ABCDEFGHIJKLMN");
```
### 优缺点
将dex中的字符串进行加密，可以提高反编译的难度，对于类似appId、appKey等敏感字符串，进行自动加密后，逆向应用时在使用jadx这一类反编译工具定位查找这些字符串的难度将加大，比如微信的appId前缀是"wx"，不加密基本上直接就可以搜索定位出来。当然，这里需要声明一点，没有绝对的安全，由于解密Key和算法是同样写在dex里面的，逆向时处理一下是可以还原出字符串内容的。另外，在运行时解密字符串会相应地降低性能，不过由于算法简单，影响不大。<br>不支持Instant Run
### 演示
加密前：<br>
![](https://github.com/MegatronKing/StringFog/blob/master/assets/before.png)<br>
加密后：<br>
![](https://github.com/MegatronKing/StringFog/blob/master/assets/after.png)<br>

### 使用
由于开发了gradle插件，所以在集成时非常简单，不会影响到打包的配置。插件已经上传到jcenter，直接引用依赖就可以。

##### 1、在根目录build.gradle中引入插件依赖
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        ...
        classpath 'com.github.megatronking.stringfog:gradle-plugin:1.2.2'
    }
}
```
##### 2、在app的build.gradle中配置插件
```
apply plugin: 'stringfog'

stringfog {
    key 'Hello World'  // 这是加解密key，可以自由定义
    enable true // 开关
}
```
##### 3、在app的build.gradle中引入加解密库依赖
```
dependencies {
      ...
      compile 'com.github.megatronking.stringfog:lib:1.2.2'
}
```

### 补充
由于没有必要对dex中所有字符串加密，所以我们采取了白名单机制，对于以下常用的库和类，处理过程中会自动忽略：
```
// default packages in white list.
addWhiteList("android.support", FLAG_PACKAGE);
addWhiteList("com.google", FLAG_PACKAGE);
addWhiteList("com.facebook", FLAG_PACKAGE);
addWhiteList("com.baidu", FLAG_PACKAGE);
addWhiteList("com.alipay", FLAG_PACKAGE);
addWhiteList("com.alibaba", FLAG_PACKAGE);
addWhiteList("com.tencent", FLAG_PACKAGE);
addWhiteList("de.greenrobot", FLAG_PACKAGE);
addWhiteList("com.qq", FLAG_PACKAGE);
addWhiteList("rx", FLAG_PACKAGE);
addWhiteList("com.squareup", FLAG_PACKAGE);

// default short class names in white list.
addWhiteList("BuildConfig", FLAG_CLASS);
addWhiteList("R", FLAG_CLASS);
```
当然，如果开发者有不需要自动加密的类，可以使用注解StringFogIgnore来忽略：
```
@StringFogIgnore
public class Test {
    ...
}
```

## 更新日志

### v1.2.2
- 修复windows下打包后报错的bug

### v1.2.1
- 修复windows下文件分隔符的bug
- 修复applicationId和packageName不一致导致无法编译的bug
- 优化功能，不需要再手动exclude已使用StringFog的库

### v1.2.0
- 支持在library中使用，每个library可以使用不同key
- 支持exclude指定包名不进行加密
- 修复一些已知bug


--------

    Copyright (C) 2017, Megatron King

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
