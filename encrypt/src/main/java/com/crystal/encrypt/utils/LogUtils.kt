package com.crystal.encrypt.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @description
 * @author XiXu
 * on 2023/4/18
 */
object LogUtils {
    private const val tagPrefix = ""
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm::ss:SSSS")

    /**
     * 得到tag【所在类、方法（L：行）】
     */
    private fun generateTag(): String {
        val stackTraceElement = Thread.currentThread().stackTrace[3]
        var className = stackTraceElement.className
        className = className.substring(className.lastIndexOf(".") + 1)
        var tag = "%s.%s(L:%d)"
        tag = format.format(Date()) + " " + String.format(
            tag,
            className,
            stackTraceElement.methodName,
            Integer.valueOf(stackTraceElement.lineNumber)
        )
        //给tag设置前缀
        if (tagPrefix != null && tagPrefix != "") {
            tag = "$tagPrefix:$tag"
        }
        return "$tag "
    }

    fun i(msg: String) {
        println(generateTag() + "---------------" + msg + "-----------")
    }

    fun e(msg: String) {
        System.err.println(generateTag() + "-------------" + msg + "--------------")
    }


}