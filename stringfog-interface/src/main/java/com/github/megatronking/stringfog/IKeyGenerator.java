package com.github.megatronking.stringfog;

/**
 * A generator uses to generate security keys.
 *
 * @author Megatron King
 * @since 2022/1/14 22:15
 */
public interface IKeyGenerator {

    /**
     * Generate a security key.
     *
     * @param text The content text will be encrypted.
     * @return A security key for the encryption.
     */
    byte[] generate(String text);

}
