package com.crystal.apkencrypt

import android.app.Application

/**
 * @description
 * @author XiXu
 * on 2023/4/19
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        LogUtils.e("real app onCreate")
    }
}