package com.crystal.shell;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.crystal.shell.utils.EncryptUtils;
import com.crystal.shell.utils.FileUtils;
import com.crystal.shell.utils.LoaderDexUtils;
import com.crystal.shell.utils.LogUtils;
import com.crystal.shell.utils.ZipUtils;


import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 *
 * date 2020/8/7.
 * description：
 */
public class ProxyApplication extends Application {
    private String applicationName;
    private boolean isRestoreRealApp;
    private Application realApp;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        PackageManager packageManager = base.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            if (metaData.containsKey("application_name")) {
                applicationName = metaData.getString("application_name");
            }

            //应用最后一次更新时间
            long lastUpdateTime = packageInfo.lastUpdateTime;

            // 获取当前应用的apk文件
            File apkFile = new File(getApplicationInfo().sourceDir);
            // 在应用私有存储空间创建一个存放解压apk后的文件地址。
            File unZipFile = getDir("fake_apk", MODE_PRIVATE);
            /**
             * 这里根据 "app_" + 应用最后一次更新的时间 作为解压的文件目录
             * 作用：  应用每更新一次时，我们都需要重新解压apk文件。
             *        当应用没有更新是，如果apk已经解压就不需要再次解压，加快第二次启动的时间。
             *
             */
            File app = new File(unZipFile, "app_" + lastUpdateTime);
            unZipAndDecryptDex(apkFile, app);
            // 存放所有的dex文件
            ArrayList<File> dexList = new ArrayList<>();
            for (File file : app.listFiles()) {
                if (file.getName().endsWith(".dex")) {
                    dexList.add(file);
                }
            }
            LogUtils.i(dexList.toString());
            // 注意这里通过getClassLoader()获取的ClassLoader是PathClassLoader，而PathClassLoader是
            // BaseDexClassLoader的子类。
            LoaderDexUtils.loader(getClassLoader(), dexList, unZipFile);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 解压apk并解密被加密了的dex文件
     *
     * @param apkFile 被加密了的 apk 文件
     * @param app     存放解压和解密后的apk文件目录
     */
    private void unZipAndDecryptDex(File apkFile, File app) {
        if (!app.exists() || app.listFiles().length == 0) {
            // 当app文件不存在，或者 app 文件是一个空文件夹是需要解压。

            // 解压apk到指定目录
            ZipUtils.unZip(apkFile, app);
            // 获取所有的dex
            File[] dexFiles = app.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    // 提取所有的.dex文件
                    return s.endsWith(".dex");
                }
            });

            if (dexFiles == null || dexFiles.length <= 0) {
                LogUtils.i("this apk is invalidate");
                return;
            }

            for (File file : dexFiles) {
                if (file.getName().equals("classes.dex")) {
                    /**
                     * 我们在加密的时候将不能加密的壳dex命名为classes.dex并拷贝到新apk中打包生成新的apk中了。
                     * 所以这里我们做脱壳，壳dex不需要进行解密操作。
                     */
                } else {
                    /**
                     * 加密的dex进行解密，对应加密流程中的_.dex文件
                     */
                    byte[] buffer = FileUtils.getBytes(file);
                    if (buffer != null) {
                        // 解密
                        byte[] decryptBytes = EncryptUtils.getInstance().decrypt(buffer);
                        if (decryptBytes != null) {
                            //修改.dex名为_.dex,避免等会与aar中的.dex重名
                            int indexOf = file.getName().indexOf(".dex");
                            String newName = file.getParent() + File.separator +
                                    file.getName().substring(0, indexOf) + "new.dex";
                            // 写数据， 替换原来的数据
                            FileUtils.wirte(new File(newName), decryptBytes);
                            file.delete();
                        } else {
                            LogUtils.e("Failed to encrypt dex data");
                            return;
                        }
                    } else {
                        LogUtils.e("Failed to read dex data");
                        return;
                    }
                }
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 替换真实的Application，不然壳的入侵性太强，而且原apk的Application不能运行。
        restoreRealApp();
    }

    private void restoreRealApp() {
        if (isRestoreRealApp) {
            return;
        }
        if (TextUtils.isEmpty(applicationName)) {
            return;
        }

        try {

            // 得到 attachBaseContext(context) 传入的上下文 ContextImpl
            Context baseContext = getBaseContext();
            // 拿到真实 APK Application 的 class
            Class<?> realAppClass = Class.forName(applicationName);
            // 反射实例化，其实 Android 中四大组件都是这样实例化的。
            realApp = (Application) realAppClass.newInstance();

            // 得到 Application attach() 方法 也就是最先初始化的
            Method attach = Application.class.getDeclaredMethod("attach", Context.class);
            attach.setAccessible(true);
            //执行 Application#attach(Context)
            //将真实的 Application 和假的 Application 进行替换。想当于自己手动控制 真实的 Application 生命周期
            attach.invoke(realApp, baseContext);


            // ContextImpl---->mOuterContext(app)   通过Application的attachBaseContext回调参数获取
            Class<?> contextImplClass = Class.forName("android.app.ContextImpl");
            // 获取 mOuterContext 属性
            Field mOuterContextField = contextImplClass.getDeclaredField("mOuterContext");
            mOuterContextField.setAccessible(true);
            mOuterContextField.set(baseContext, realApp);

            //拿到 ActivityThread 变量
            Field mMainThreadField = contextImplClass.getDeclaredField("mMainThread");
            mMainThreadField.setAccessible(true);
            // 拿到 ActivityThread 对象
            Object mMainThread = mMainThreadField.get(baseContext);

            //  ActivityThread--->>mInitialApplication
            //  反射拿到 ActivityThread class
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            // 得到当前加载的 Application 类
            Field mInitialApplicationField = activityThreadClass.getDeclaredField("mInitialApplication");
            mInitialApplicationField.setAccessible(true);
            // 将 ActivityThread 中的 mInitialApplication 替换为 真实的 Application 可以用于接收相应的声明周期和一些调用等
            mInitialApplicationField.set(mMainThread, realApp);


            //   ActivityThread--->mAllApplications(ArrayList)       ContextImpl的mMainThread属性
            //   拿到 ActivityThread 中所有的 Application 集合对象
            Field mAllApplicationsField = activityThreadClass.getDeclaredField("mAllApplications");
            mAllApplicationsField.setAccessible(true);
            ArrayList<Application> mAllApplications = (ArrayList<Application>) mAllApplicationsField.get(mMainThread);
            // 删除 ProxyApplication
            mAllApplications.remove(this);
            //  添加真实的 Application
            mAllApplications.add(realApp);

            //  LoadedApk------->mApplication         ContextImpl的mPackageInfo属性
            Field mPackageInfoField = contextImplClass.getDeclaredField("mPackageInfo");
            mPackageInfoField.setAccessible(true);
            Object mPackageInfo = mPackageInfoField.get(baseContext);
            Class<?> loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mApplicationField = loadedApkClass.getDeclaredField("mApplication");
            mApplicationField.setAccessible(true);
            //将 LoadedApk 中的 mApplication 替换为 真实的 Application
            mApplicationField.set(mPackageInfo, realApp);

            //修改ApplicationInfo className   LooadedApk
            // 拿到 LoadApk 中的 mApplicationInfo 变量
            Field mApplicationInfoField = loadedApkClass.getDeclaredField("mApplicationInfo");
            mApplicationInfoField.setAccessible(true);
            ApplicationInfo mApplicationInfo = (ApplicationInfo) mApplicationInfoField.get(mPackageInfo);
            // 将我们真实的 Application ClassName 名称赋值于它
            mApplicationInfo.className = applicationName;

            // 执行真实 Application onCreate 声明周期
            realApp.onCreate();

            //解码完成
            isRestoreRealApp = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPackageName() {
        if (!TextUtils.isEmpty(applicationName)) {
            return "";
        }
        return super.getPackageName();
    }

    /**
     * 这个函数是如果在 AndroidManifest.xml 中定义了 ContentProvider 那么就会执行此处 : installProvider，简介调用该函数
     *
     * @param packageName
     * @param flags
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        if (TextUtils.isEmpty(applicationName)) {
            return super.createPackageContext(packageName, flags);
        }
        try {
            restoreRealApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realApp;
    }
}
