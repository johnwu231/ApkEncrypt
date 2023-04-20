package com.crystal.encrypt.utils

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.*

/**
 * @description
 * @author XiXu
 * on 2023/4/18
 */
object ZipUtils {
    /**
     * 解压zip文件
     * @param srcFile 需要解压的zip文件夹
     * @param dstFile 解压后的文件
     */
    fun unZip(srcFile: File, dstFile: File) {
        if (srcFile == null) {
            LogUtils.e("unZip: srcFile is null")
            return
        }
        try {
            val zipFile = ZipFile(srcFile)
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val zipEntry = entries.nextElement()
                val name = zipEntry.name
                // 原来的签名文件不需要了。
                if ((name == "META-INF/CERT.RSA") || (name == "META-INF/CERT.SF") || (name
                            == "META-INF/MANIFEST.MF")
                ) {
                    continue
                }
                if (!zipEntry.isDirectory) {
                    val file = File(dstFile, name)
                    if (!file.parentFile.exists()) file.parentFile.mkdirs()
                    val fos = FileOutputStream(file)
                    val `is` = zipFile.getInputStream(zipEntry)
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (`is`.read(buffer).also { len = it } != -1) {
                        fos.write(buffer, 0, len)
                    }
                    `is`.close()
                    fos.close()
                }
            }
            zipFile.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    /**
     * 压缩
     * @param sourceFile
     * @param zipFile
     */
    fun zip(sourceFile: File, zipFile: File) {
        zipFile.delete()
        //对输出的文件做CRC32校验
        val zos = ZipOutputStream(CheckedOutputStream(FileOutputStream(zipFile), CRC32()))
        compress(sourceFile, zos, "")
        zos.flush()
        zos.close()
    }

    private fun compress(sourceFile: File, zos: ZipOutputStream, dir: String) {
        if (sourceFile.isDirectory) {
            compressDir(sourceFile, zos, dir)
        } else {
            compressFile(sourceFile, zos, dir)
        }
    }

    private fun compressFile(sourceFile: File, zos: ZipOutputStream, dir: String) {
        val dirName = dir + sourceFile.name
        val dirNames = dirName.replace(File.separator, "|").split("\\|".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val sb = StringBuffer()
        if (dirNames.size > 1) {
            for (i in 1 until dirNames.size) {
                sb.append(File.separator)
                sb.append(dirNames[i])
            }
        } else {
            sb.append(File.separator)
        }
        val entry = ZipEntry(sb.toString().substring(1))
        zos.putNextEntry(entry)
        val bis = BufferedInputStream(FileInputStream(sourceFile))
        var count: Int
        val bytes = ByteArray(1024)
        while (bis.read(bytes).also { count = it } != -1) {
            zos.write(bytes, 0, count)
        }
        bis.close()
        zos.closeEntry()
    }

    private fun compressDir(sourceFile: File, zos: ZipOutputStream, dir: String) {
        val listFiles = sourceFile.listFiles()
        if (listFiles == null || listFiles.isEmpty()) {
            val entry = ZipEntry(dir + sourceFile.name + File.separator)
            zos.putNextEntry(entry)
            zos.closeEntry()
        }
        for (file in listFiles) {
            // 递归压缩
            compress(file, zos, dir + sourceFile.name + File.separator)
        }
    }


    /**
     * 对齐
     *
     * @param unsignedApk
     * @param unAlignApk
     */
    fun zipalign(unsignedApk: File?, unAlignApk: File) {
        if (unsignedApk == null || !unsignedApk.exists()) {
            LogUtils.e("The APK that needs to be align does not exist")
            return
        }
        // 这里使用的是zipalign.ext的绝对路径.请根据实际情况填写。
        val command =
            ("zipalign -v -p 4 " + unsignedApk.absolutePath
                    + " " + unAlignApk.absolutePath)
        CmdUtils.execCommand(command)
    }
}