package com.crystal.encrypt.utils

import java.io.File

/**
 * 签名工具类
 * @description
 * @author XiXu
 * on 2023/4/19
 */
object SignUtils {


    fun signature(unSignApk: File, signApk: File) {
        if (!unSignApk.exists()){
            LogUtils.e("The Apk not exist.")
            return
        }
//        val command = "jarsigner -sigalg SHA1withRSA -digestalg SHA1 " +
//                "-keystore /Users/john/.android/debug.keystore -storepass android -keypass android " +
//                "-signedjar " + signApk.absolutePath + " " + unSignApk.absolutePath + " androiddebugkey"

        val command = "apksigner sign --ks encrypt/source/signature.jks --ks-pass=pass:zaiqiang --ks-key-alias=zaiqiang --key-pass=pass:zaiqiang --out " + signApk.absolutePath + " " + unSignApk.absolutePath

        CmdUtils.execCommand(command)
    }
}