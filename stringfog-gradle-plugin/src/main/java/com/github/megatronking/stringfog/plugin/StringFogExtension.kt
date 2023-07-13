package com.github.megatronking.stringfog.plugin

import com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator
import java.io.Serializable

abstract class StringFogExtension {

    companion object {
        val base64 = StringFogMode.base64
        val bytes = StringFogMode.bytes
    }

    /**
     * The algorithm implementation for String encryption and decryption.
     * It is required.
     */
    var implementation: String? = null

    /**
     * A generator to generate a security key for the encryption and decryption.
     *
     * StringFog use a 8 length random key generator default.
     */
    var kg = RandomKeyGenerator()

    /**
     * How the encrypted string presents in java class, default is base64.
     */
    var mode = base64

    /**
     * Enable or disable the StringFog plugin. Default is enabled.
     */
    var enable = true

    /**
     * Enable or disable the StringFog debug message print. Default is disabled.
     */
    var debug = false

    /**
     * The java packages will be applied. Default is effect on all packages.
     */
    var fogPackages = emptyArray<String>()

    /**
     * 当前应用的包名
     */
    var packageName: String? = null

}