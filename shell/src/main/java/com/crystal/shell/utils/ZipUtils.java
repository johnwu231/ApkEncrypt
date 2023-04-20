package com.crystal.shell.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * date 2020/8/6.
 * description： zip解压工具类
 */
public class ZipUtils {
    /**
     * 解压zip文件
     *
     * @param srcFile 需要解压的zip文件
     * @param dstFile 解压后的文件
     */
    public static void unZip(File srcFile, File dstFile) {
        if (srcFile == null) {
            LogUtils.e("unZip: srcFile is null");
            return;
        }
        try {
            ZipFile zipFile = new ZipFile(srcFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.equals("META-INF/CERT.RSA") || name.equals("META-INF/CERT.SF") || name
                        .equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                if (!zipEntry.isDirectory()) {
                    File file = new File(dstFile, name);
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = zipFile.getInputStream(zipEntry);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩
     * @param sourceFile
     * @param zipFile
     */
    public static void zip(File sourceFile, File zipFile) {
        if (sourceFile == null) {
            LogUtils.e("The original file that needs to be compressed does not exist");
            return;
        }
        zipFile.delete();
        // 对输出文件做CRC32校验
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(zipFile), new CRC32()));
            compress(sourceFile, zos, "");
            zos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                    zos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void compress(File srcFile, ZipOutputStream zos, String dir) throws IOException {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, dir);
        } else {
            compressFile(srcFile, zos, dir);
        }
    }

    private static void compressDir(File srcFile, ZipOutputStream zos, String dir) throws IOException {
        File[] files = srcFile.listFiles();
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(dir + srcFile.getName() + File.separator);
            zos.putNextEntry(entry);
            zos.closeEntry();
        }
        for (File file : files) {
            // 递归压缩
            compress(file, zos, dir + srcFile.getName() + File.separator);
        }
    }


    private static void compressFile(File file, ZipOutputStream zos, String dir) throws IOException {
        String dirName = dir + file.getName();
        String[] dirNames = dirName.replace(File.separator,"|").split("\\|");
        StringBuffer sb = new StringBuffer();
        if (dirNames.length > 1) {
            for (int i = 1; i < dirNames.length; i++) {
                sb.append(File.separator);
                sb.append(dirNames[i]);
            }
        } else {
            sb.append(File.separator);
        }
        ZipEntry entry = new ZipEntry(sb.toString().substring(1));
        zos.putNextEntry(entry);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int count;
        byte[] bytes = new byte[1024];
        while ((count = bis.read(bytes)) != -1) {
            zos.write(bytes, 0, count);
        }
        bis.close();
        zos.closeEntry();
    }
}
