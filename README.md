
# StringFog
一款自动对dex/aar/jar文件中的字符串进行加密Android插件工具，正如名字所言，给字符串加上一层雾霭，使人难以窥视其真面目。

- 支持java/kotlin。
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
String a = StringFog.decrypt(new byte[]{-113, 71...}, new byte[]{-23, 53});

```

- 运行时：
```
decrypt: new byte[]{-113, 71...} => "This is a string!"
```

### 混淆
StringFog和混淆完全不冲突，也不需要配置反混淆，实际上StringFog配上混淆效果会更好！

### 使用
由于开发了gradle插件，所以在集成时非常简单，不会影响到打包的配置。插件已经上传到MavenCentral，直接引用依赖就可以。
**jcenter已经废弃，3.0+版本取消发布**

##### 1、在根目录build.gradle中引入插件依赖。
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        ...
        classpath 'com.github.megatronking.stringfog:gradle-plugin:4.0.0'
        // 选用加解密算法库，默认实现了xor算法，也可以使用自己的加解密库。
        classpath 'com.github.megatronking.stringfog:xor:4.0.0'
    }
}
```

##### 2、在app或lib的build.gradle中配置插件。
```
apply plugin: 'stringfog'

// 导入RandomKeyGenerator类，如果不使用RandomKeyGenerator，可以删除此行
import com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator

stringfog {
    // 必要：加解密库的实现类路径，需和上面配置的加解密算法库一致。
    implementation 'com.github.megatronking.stringfog.xor.StringFogImpl'
    // 可选：加密开关，默认开启。
    enable true
    // 可选：指定需加密的代码包路径，可配置多个，未指定将默认全部加密。
    fogPackages = ['com.xxx.xxx']
    // 可选（3.0版本新增）：指定密钥生成器，默认使用长度8的随机密钥（每个字符串均有不同随机密钥）,
    // 也可以指定一个固定的密钥：HardCodeKeyGenerator("This is a key")
    kg new RandomKeyGenerator()
    // 可选（4.0版本新增）：用于控制字符串加密后在字节码中的存在形式, 默认为base64，
    // 也可以使用text或者bytes
    mode base64
}
```

##### 3、在app或lib的build.gradle中引入加解密库依赖。
```
dependencies {
      ...
      // 这里要和上面选用的加解密算法库一致，用于运行时解密。
      compile 'com.github.megatronking.stringfog:xor:4.0.0'
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
实现IStringFog接口，参考stringfog-ext目录下面的xor算法实现。
注意某些算法在不同平台上会有差异，可能出现在运行时无法正确解密的问题。如何集成请参考下方范例！
```
public final class StringFogImpl implements IStringFog {

    @Override
    public byte[] encrypt(String data, byte[] key) {
        // 自定义加密
    }

    @Override
    public String decrypt(byte[] data, byte[] key) {
        // 自定义解密
    }

    @Override
    public boolean shouldFog(String data) {
        // 控制指定字符串是否加密
        // 建议过滤掉不重要或者过长的字符串
        return true;
    }

}

```

#### 自定义密钥生成器
实现IKeyGenerator接口，参考RandomKeyGenerator的实现。

#### Mapping文件
加解密的字符串明文和暗文会自动生成mapping映射文件，位于outputs/mapping/stringfog.txt。

## 范例
- 默认加解密算法集成，参考[sample1](https://github.com/MegatronKing/StringFog-Sample1)
- 自定义加解密算法集成，参考[sample2](https://github.com/MegatronKing/StringFog-Sample2)

## 更新日志

### v4.0.0
- 使用ASM7以支持Android 12。
- 支持AGP(Android Gradle Plugin) 7.x版本。
- DSL新增StringFogMode选项，用于控制字符串加密后在字节码中的存在形式，支持base64和bytes两种模式，默认使用base64。
    - base64模式：将字符串加密后的字节序列使用base64编码，行为同1.x和2.x版本。
    - bytes模式：将字符串加密后的字节序列直接呈现在字节码中，行为同3.x版本。

### v3.0.0
- 密文不再以String形式存在，改为直接字节数组，感谢PR #50。
- 重构公开API相关代码（不兼容历史版本）。
- 删除AES加密实现，考虑到存在bug和性能问题且意义不大。
- xor算法移除base64编码。
- 固定加密字符串key改为随机key，且提供IKeyGenerator接口支持自定义实现。
- 插件依赖的ASM库由5.x升级到9.2。

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

    Copyright (C) 2022, Megatron King

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
