package com.crystal.encrypt.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile

/**
 * 文件工具类
 * @description
 * @author XiXu
 * on 2023/4/18
 */
object FileUtils {

    /**
     * 递归删除目录下的所有文件以及子目录下所有文件
     */
    fun deleteFolder(path: String) {
        val deleteFile = File(path)
        if (deleteFile.exists()) {
            var listFiles = deleteFile.listFiles()
            //递归删除目录下的子目录
            for (file in listFiles) {
                if (file.isDirectory) {
                    deleteFolder(file.absolutePath)
                } else {
                    file.delete()
                }
            }
            deleteFile.delete()
        }
    }

    /**
     * 写入数据
     */
    fun write(file: File, data: ByteArray) {
        val fos = FileOutputStream(file)
        fos.write(data)
        fos.flush()
        fos.close()
    }

    /**
     * 读取文件为bytes数据
     */
    fun getBytes(dexFile: File): ByteArray {
        val fis = RandomAccessFile(dexFile, "r")
        val buffer = ByteArray(fis.length().toInt())
        fis.readFully(buffer)
        fis.close()
        return buffer
    }

    /**
     * 文件拷贝
     */
    fun copyFile(srcFile: File, dstFile: File) {
        if (!srcFile.exists()) {
            LogUtils.e("unZip :scrFile is null")
            return
        }
        val bytes = getBytes(srcFile)
        val fos = FileOutputStream(dstFile)
        fos.write(bytes)
        fos.flush()
        fos.close()
    }
}