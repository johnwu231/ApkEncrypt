package com.crystal.encrypt.utils

import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 * @description
 * @author XiXu
 * on 2023/4/19
 */
class EncryptUtils private constructor() {
    private val ALGORITHM = "AES/CBC/PKCS5Padding" // 加密算法
    private val KEY = "QUmkLrrISiud6RPU".toByteArray() // 加密使用的key

    private val IV = "eh7aJlOdHCNsGNcD".toByteArray() // 偏移值

    private var encryptCipher // 加密
            : Cipher = Cipher.getInstance(ALGORITHM)
    private var decryptCipher // 解密
            : Cipher = Cipher.getInstance(ALGORITHM)

    init {
        //初始化加密算法
        val key = SecretKeySpec(KEY, "AES")
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(IV))
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(IV))
    }

    companion object {
        fun getInstance() = SingletonHolder.instance
    }

    private object SingletonHolder {
        val instance = EncryptUtils()
    }


    /**
     * 加密apk
     * @param  srcApkFile  源apk文件的地址
     * @param dstApkFile 新apk文件的地址
     */
    fun encryptApkFile(srcApkFile: File, dstApkFile: File) {
        if (!srcApkFile.exists()) {
            LogUtils.e("source apk file not exist")
            return
        }
        //解压apk到指定文件夹
        ZipUtils.unZip(srcApkFile, dstApkFile)
        //获取所有的dex
        val dexFiles = dstApkFile.listFiles { p0, s -> s.endsWith(".dex") }
        if (dexFiles == null || dexFiles.isEmpty()) {
            LogUtils.i("this apk is invalidate")
            return
        }

        for (dexFile in dexFiles) {
            //读取dex中的数据
            val buffer = FileUtils.getBytes(dexFile)
            val encryptBytes = encrypt(buffer)
            //修改.dex名为_.dex，避免等会与aar中的.dex重名
            val indexOf = dexFile.name.indexOf(".dex")
            val newName =
                dexFile.parent + File.separator + dexFile.name.substring(0, indexOf) + "_.dex"
            //写数据，替换原来的数据
            FileUtils.write(File(newName), encryptBytes)
            dexFile.delete()
        }
    }

    /**
     * 加密
     */
    private fun encrypt(buffer: ByteArray): ByteArray {
        return encryptCipher.doFinal(buffer)
    }

}