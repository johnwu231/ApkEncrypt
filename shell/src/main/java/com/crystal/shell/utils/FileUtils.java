package com.crystal.shell.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * date 2020/8/6.
 * description：
 */
public class FileUtils {
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param path 将要删除的文件目录地址
     */
    public static void delFolder(String path) {
        File delFile = new File(path);
        if (delFile.exists()) {
            File[] files = delFile.listFiles();
            //递归删除目录中的子目录下
            for (File file : files) {
                if (file.isDirectory()) {
                    //递归直到目录下没有文件
                    delFolder(file.getAbsolutePath());
                } else {
                    //删除
                    file.delete();
                }
            }
            delFile.delete();
        }
    }

    /**
     * 写入数据
     *
     * @param file 写入文件地址
     * @param data 需要写入的数据
     */
    public static void wirte(File file, byte[] data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] getBytes(File dexFile) {
        try {
            RandomAccessFile fis = new RandomAccessFile(dexFile, "r");
            byte[] buffer = new byte[(int) fis.length()];
            fis.readFully(buffer);
            fis.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 文件拷贝
     *
     * @param srcFile 原文件
     * @param dstFile 目标文件
     */
    public static void copyFile(File srcFile, File dstFile) {
        if (srcFile == null || !srcFile.exists()) {
            LogUtils.e("Copy the original file does not exist");
            return;
        }
        FileOutputStream fos = null;
        byte[] bytes = FileUtils.getBytes(srcFile);
        try {
            fos = new FileOutputStream(dstFile);
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
