package com.crystal.encrypt

import com.crystal.encrypt.utils.*
import java.io.File

object ApkEncryptMain {
    private const val SOURCE_APK_PATH = "encrypt/source/apk/app-debug.apk"
    private const val SHELL_APK_PATH = "encrypt/source/arr/shell-release.aar"

    @JvmStatic
    fun main(args: Array<String>) {
        LogUtils.i("start encrypt")
        init()
        //解压源apk文件到../source/apk/temp目录下，并加密dex文件
        val sourceApk = File(SOURCE_APK_PATH)
        val newApkDir = File(sourceApk.parent + File.separator + "temp")
        if (!newApkDir.exists()) {
            newApkDir.mkdirs()
        }
        //解压apk并加密dex文件
        EncryptUtils.getInstance().encryptApkFile(sourceApk, newApkDir)
        //解压arr文件（不加密的部分），并将其中的dex文件拷贝到apk/temp目录下
        val shellApk = File(SHELL_APK_PATH)
        val newShellDir = File(shellApk.parent + File.separator + "temp")
        if (!newShellDir.exists()){
            newShellDir.mkdirs()
        }
        //解压aar文件，并将aar中的jar文件转换为dex文件，然后拷贝aar中的classes.dex到apk/temp目录下
        DxUtils.jar2Dex(shellApk,newShellDir)

        //3.打包apk/temp目录生成新的未签名的apk文件
        val unsignedApk = File("encrypt/result/apk-unsigned.apk")
        unsignedApk.parentFile.mkdirs()
        unsignedApk.delete()
        ZipUtils.zip(newApkDir,unsignedApk)

        //4.对齐
        val unAlignApk = File("encrypt/result/apk-unAlign.apk")
        unAlignApk.parentFile.mkdirs()
        unAlignApk.delete()
        ZipUtils.zipalign(unsignedApk,unAlignApk)

        //5.给新的apk添加签名，生成签名apk
        val signedApk = File("encrypt/result/apk-signed.apk")
        signedApk.parentFile.mkdirs()
        signedApk.delete()
        SignUtils.signature(unAlignApk,signedApk)
    }


    //初始化
    private fun init() {
        FileUtils.deleteFolder("encrypt/source/apk/temp")
        FileUtils.deleteFolder("encrypt/source/arr/temp")
    }
}