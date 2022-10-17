package com.github.megatronking.stringfog.plugin;

/**
 * Define how the encrypted string presents in java class.
 */
public enum StringFogMode {

    /**
     * Replace the origin string with an encrypted and base64 encoded string.
     */
    base64,

    /**
     * Replace the origin string with an encrypted byte array.
     *
     * Warning: this mode will increase the apk file size.
     */
    bytes
}