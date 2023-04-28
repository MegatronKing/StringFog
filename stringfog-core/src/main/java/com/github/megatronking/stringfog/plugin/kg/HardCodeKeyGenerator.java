package com.github.megatronking.stringfog.plugin.kg;

import com.github.megatronking.stringfog.IKeyGenerator;

import java.nio.charset.StandardCharsets;

/**
 * Hard code a binary security key for encryption.
 *
 * @author Megatron King
 * @since 2022/2/14 22:34
 */
public class HardCodeKeyGenerator implements IKeyGenerator {

    private final byte[] mKey;

    public HardCodeKeyGenerator(String key) {
        this(key.getBytes(StandardCharsets.UTF_8));
    }

    public HardCodeKeyGenerator(byte[] key) {
        mKey = key;
    }

    @Override
    public byte[] generate(String text) {
        return mKey;
    }
    
}
