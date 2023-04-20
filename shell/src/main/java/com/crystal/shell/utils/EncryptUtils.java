package com.crystal.shell.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * date 2020/8/6.
 * description： 解密工具类
 */
public class EncryptUtils {
    private final byte[] KEY = "QUmkLrrISiud6RPU".getBytes(); // 加密使用的key
    private final byte[] IV = "eh7aJlOdHCNsGNcD".getBytes(); // 偏移值
    private final String ALGORITHM = "AES/CBC/PKCS5Padding"; // 加密算法
    private Cipher decryptCipher; // 解密

    /**
     * 使用单例
     */
    private EncryptUtils() {
        try {
            // 初始化加密算法
            decryptCipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(KEY, "AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SingletonHolder {
        private static final EncryptUtils INSTANCE = new EncryptUtils();
    }

    public static EncryptUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 解密
     *
     * @param data
     * @return
     */
    public byte[] decrypt(byte[] data) {
        try {
            return decryptCipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
