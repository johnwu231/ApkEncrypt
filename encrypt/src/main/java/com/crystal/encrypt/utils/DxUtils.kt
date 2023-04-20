package com.crystal.encrypt.utils

import java.io.File

/**
 * @description dx工具类 ，主要讲jar转化为dex
 * @author XiXu
 * on 2023/4/19
 */
object DxUtils {

    /**
     * 解压arr并将jar转化为dex
     */
    fun jar2Dex(srcFile: File, dstFile: File) {
        if (!srcFile.exists()) {
            LogUtils.e("shell arr file not exist")
            return
        }
        //解压apk到指定文件夹
        ZipUtils.unZip(srcFile, dstFile)
        //获取所有的jar
        val jarFiles = dstFile.listFiles { p0, s -> s.endsWith(".jar") }
        if (jarFiles == null || jarFiles.isEmpty()) {
            LogUtils.i("this arr is invalidate")
            return
        }
        //一般情况下这个壳aar中只会有一个classes.jar文件
        val classes_jar = jarFiles[0]
        //将classes_jar转为classes.dex
        val dstDex = File(classes_jar.parent + File.separator + "classes.dex")
        //使用android tools里面的dx.bat命令将jar转换为dex
        dxCommand(classes_jar, dstDex)
        //拷贝aar中的classes.dex到apk/temp目录下
        val copyDstFile = File("encrypt/source/apk/temp/classes.dex")
        FileUtils.copyFile(dstDex, copyDstFile)
    }

    private fun dxCommand(jarFile: File, dstDex: File) {
        val command = "dx --dex --min-sdk-version=26 --output=" + dstDex.absolutePath + " " + jarFile.absolutePath
        CmdUtils.execCommand(command)
    }
}