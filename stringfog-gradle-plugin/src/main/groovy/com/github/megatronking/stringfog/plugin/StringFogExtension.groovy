/*
 * Copyright (C) 2017, Megatron King
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.megatronking.stringfog.plugin

import com.github.megatronking.stringfog.IKeyGenerator
import com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator
import com.github.megatronking.stringfog.plugin.StringFogMode

/**
 * StringFog extension.
 * <p>
 * <code>
 * apply plugin: 'stringfog'
 *
 * stringfog {
 *     implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
 * }
 * </code>
 *
 * @author Megatron King
 * @since 2017/3/7 17:44
 */

public class StringFogExtension {

    static def base64 = StringFogMode.base64
    static def bytes = StringFogMode.bytes

    /**
     * The algorithm implementation for String encryption and decryption.
     * It is required.
     */
    String implementation

    /**
     * A generator to generate a security key for the encryption and decryption.
     *
     * StringFog use a 8 length random key generator default.
     */
    IKeyGenerator kg = new RandomKeyGenerator()

    /**
     * How the encrypted string presents in java class, default is base64.
     */
    StringFogMode mode = base64

    /**
     * Enable or disable the StringFog plugin. Default is enabled.
     */
    boolean enable = true

    /**
     * Enable or disable the StringFog debug message print. Default is disabled.
     */
    boolean debug = false

    /**
     * The java packages will be applied. Default is effect on all packages.
     */
    String[] fogPackages = []

}
