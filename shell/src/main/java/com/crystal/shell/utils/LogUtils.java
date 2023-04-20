package com.crystal.shell.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * date 2020/8/6.
 * description： 日志输出工具
 */
public class LogUtils {
    private static String tagPrefix = "";
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    /**
     * 得到tag（所在类.方法（L:行））
     *
     * @return
     */
    private static String generateTag() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        String callerClazzName = stackTraceElement.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        String tag = "%s.%s(L:%d)";
        tag = format.format(new Date()) + " " + String.format(tag, callerClazzName,
                stackTraceElement.getMethodName(), Integer.valueOf(stackTraceElement.getLineNumber()));
        // 给tag设置前缀
        if (tagPrefix != null && !tagPrefix.equals("")) {
            tag = tagPrefix + ":" + tag;
        }
        return tag + String.valueOf(" ");
    }

    public static void i(String message) {
        System.out.println(generateTag() + "----- " + message + " -----");
    }

    public static void e(String message) {
        System.err.println(generateTag() + "----- " + message + " -----");
    }
}
