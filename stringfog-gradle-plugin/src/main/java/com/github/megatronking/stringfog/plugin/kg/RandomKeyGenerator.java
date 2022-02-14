package com.github.megatronking.stringfog.plugin.kg;

import com.github.megatronking.stringfog.IKeyGenerator;

import java.security.SecureRandom;

/**
 * Dynamic random security key for encryption.
 *
 * @author Megatron King
 * @since 2022/2/14 22:40
 */
public class RandomKeyGenerator implements IKeyGenerator {

    private static final int DEFAULT_LENGTH = 2;

    private final SecureRandom mSecureRandom;
    private final int mLength;

    public RandomKeyGenerator() {
        this(DEFAULT_LENGTH);
    }

    public RandomKeyGenerator(int length) {
        mLength = length;
        mSecureRandom = new SecureRandom();
    }

    @Override
    public byte[] generate(String text) {
        byte[] key = new byte[mLength];
        mSecureRandom.nextBytes(key);
        return key;
    }

}
