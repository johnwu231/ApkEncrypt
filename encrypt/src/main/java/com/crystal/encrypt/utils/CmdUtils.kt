package com.crystal.encrypt.utils

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * cmd命令行
 * @description
 * @author XiXu
 * on 2023/4/19
 */
object CmdUtils {

    fun execCommand(command: String) {
        val runtime = Runtime.getRuntime()
        var process: Process? = null
        var errorBuffer: BufferedReader? = null
        process = runtime.exec(command)
        var line: String?
        val successBuffer = BufferedReader(InputStreamReader(process.inputStream))
        while (successBuffer.readLine().also { line = it } != null) {
            LogUtils.i(line!!)
        }
        process.waitFor()
        if (process.exitValue() != 0) {
            LogUtils.e("exec fail --- $command")
            errorBuffer = BufferedReader(InputStreamReader(process.errorStream))
            while (errorBuffer.readLine().also { line = it } != null) {
                LogUtils.e(line!!)
            }
        } else {
            LogUtils.i("exec success --- $command")
        }
        process?.destroy()
        successBuffer.close()
        errorBuffer?.close()
    }
}