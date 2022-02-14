
# StringFog
一款自动对dex/aar/jar文件中的字符串进行加密Android插件工具，正如名字所言，给字符串加上一层雾霭，使人难以窥视其真面目。

- 支持app打包生成的apk加密。
- 支持aar和jar等库文件加密。
- 支持加解密算法的自主扩展。
- 支持配置可选代码加密。
- 完全Gradle自动化集成。
- 不支持InstantRun。

### 原理

![](https://github.com/MegatronKing/StringFog/blob/master/assets/flow.png)<br>

- 加密前：
```
String a = "This is a string!";
```

- 加密后：
```
String finalStaticStr = StringFog.decrypt(new byte[]{-113, 71...}, new byte[]{-23, 53});

```

- 运行时：
```
decrypt: new byte[]{-113, 71...} => "This is a string!"
```

### 混淆
StringFog和混淆完全不冲突，也不需要配置反混淆，实际上StringFog配上混淆效果会更好！

### 使用
由于开发了gradle插件，所以在集成时非常简单，不会影响到打包的配置。插件已经上传到jcenter，直接引用依赖就可以。

##### 1、在根目录build.gradle中引入插件依赖。
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        ...
        classpath 'com.github.megatronking.stringfog:gradle-plugin:2.2.1'
        // 选用加解密算法库，默认实现了xor和aes-cbc两种简单算法，也可以使用自己的加解密库。
        classpath 'com.github.megatronking.stringfog:xor:1.1.0'
    }
}
```

##### 2、在app或lib的build.gradle中配置插件。
```
apply plugin: 'stringfog'

stringfog {
    // 这是加解密随机key长度，可以自由定义。
   keyLength 2
    // 开关
    enable true
    // 加解密库的实现类路径，需和上面配置的加解密算法库一致。
    implementation 'com.github.megatronking.stringfog.xor.StringFogImpl'
    // 指定需加密的代码包路径，可配置多个，未指定将默认全部加密。
    fogPackages = ['com.xxx.xxx']
}
```

##### 3、在app或lib的build.gradle中引入加解密库依赖。
```
dependencies {
      ...
      // 这里要和上面选用的加解密算法库一致，用于运行时解密。
      compile 'com.github.megatronking.stringfog:xor:1.1.0'
}
```

### 扩展

#### 注解反加密
如果开发者有不需要自动加密的类，可以使用注解StringFogIgnore来忽略：
```
@StringFogIgnore
public class Test {
    ...
}
```
#### 自定义加解密算法实现
实现IStringFog接口，参考stringfog-ext目录下面的两种算法实现。注意某些算法在不同平台上会有差异，可能出现在运行时无法正确解密的问题。如何集成请参考下方范例！
```
public final class StringFogImpl implements IStringFog {

    @Override
    public String encrypt(String data, String key) {
        // 自定义加密
    }

    @Override
    public String decrypt(String data, String key) {
        // 自定义解密
    }

    @Override
    public boolean overflow(String data, String key) {
        // 最大字符串长度为65536，这里要校验加密后是否出现长度溢出，如果溢出将不进行加密。
        // 这里可以控制符合某些条件的字符串不加密。
    }

}

```

#### Mapping文件
加解密的字符串明文和暗文会自动生成mapping映射文件，位于outputs/mapping/stringfog.txt。

## 范例
- 默认加解密算法集成，参考[sample1](https://github.com/MegatronKing/StringFog-Sample1)
- 自定义加解密算法集成，参考[sample2](https://github.com/MegatronKing/StringFog-Sample2)

## 更新日志

### v2.2.1
- 修复module-info类导致的报错问题

### v2.2.0
- 支持AGP(Android Gradle Plugin) 3.3.0+版本

### v2.1.0
- 修复kotlin打包的bug

### v2.0.1
- 增加implementation自定义算法实现类详细报错信息

### v2.0.0
- 修改gradle配置（必须配置implementation指定算法实现）。
- 修复大字符串编译失败的问题。
- 新增自定义加解密算法扩展。
- 新增生成mapping映射表文件。

### v1.4.1
- 修复使用Java 8时出现的ZipException编译错误

### v1.4.0
- 新增指定包名加密的配置项：fogPackages
- 移除指定包名不加密的配置项：exclude

### v1.3.0
- 修复gradle 3.0+编译报错的bug

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
