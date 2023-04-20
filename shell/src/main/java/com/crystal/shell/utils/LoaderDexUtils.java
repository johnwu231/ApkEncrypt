package com.crystal.shell.utils;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * date 2020/8/7.
 * description： 加载dex文件
 */
public class LoaderDexUtils {
    public static void loader(ClassLoader loader, ArrayList<File> dexList, File dir) {

        try {
            /**
             * 1. 通过反射找到BaseDexClassLoader中的pathList属性，pathList是DexPathList类型的对象。
             * DexPathList中维护了一个dex文件数组（dexElements数组），ClassLoader加载类的时候就会从这dex数组中去查找。
             * 我们需要将解密出来的dex重新插入到这个数组里面。
             */
            // 这里的loader是PathClassLoader，PathClassLoader继承自BaseDexClassLoader
            Class<?> baseDexClassLoaderClass = loader.getClass().getSuperclass();
            Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(loader);


            /**
             * 2. 创建我们自己的dex文件数组，可查看源码中的makeDexElements方法
             */
            ArrayList suppressedExceptions = new ArrayList();
            Class<?> dexPathListClass = pathList.getClass();
            Object[] elements = null;
            if (Build.VERSION.SDK_INT >= 24) {
                Method makeDexElementsMethod = dexPathListClass.getDeclaredMethod("makeDexElements", List.class, File.class, List.class, ClassLoader.class);
                makeDexElementsMethod.setAccessible(true);
                elements = (Object[]) makeDexElementsMethod.invoke(pathList, dexList, dir, suppressedExceptions, loader);
            } else if (Build.VERSION.SDK_INT >= 23) {
                Method makeDexElementsMethod = dexPathListClass.getDeclaredMethod("makePathElements", List.class, File.class, List.class);
                makeDexElementsMethod.setAccessible(true);
                elements = (Object[]) makeDexElementsMethod.invoke(pathList, dexList, dir, suppressedExceptions);
            } else {
                Method makeDexElementsMethod = dexPathListClass.getDeclaredMethod("makeDexElements", ArrayList.class, File.class, ArrayList.class);
                makeDexElementsMethod.setAccessible(true);
                elements = (Object[]) makeDexElementsMethod.invoke(pathList, dexList, dir, suppressedExceptions);
            }

            if (elements == null) {
                LogUtils.e("makeDexElements fail");
                return;
            }
            /**
             * 3. 将解密后的dex文件插入到DexPathList的dexElements数组中。
             */
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object[] oldDexElements = (Object[]) dexElementsField.get(pathList);
            Object[] newDexElements = (Object[]) (Array.newInstance(oldDexElements.getClass()
                    .getComponentType(), oldDexElements.length + elements.length));
            System.arraycopy(oldDexElements, 0, newDexElements, 0, oldDexElements.length);
            System.arraycopy(elements, 0, newDexElements, oldDexElements.length, elements.length);
            dexElementsField.set(pathList, newDexElements);

            // 异常处理
            if (suppressedExceptions.size() > 0) {
                Iterator iterator = suppressedExceptions.iterator();

                while (iterator.hasNext()) {
                    IOException dexElementsSuppressedExceptions = (IOException)
                            iterator.next();
                    Log.w("MultiDex", "Exception in makeDexElement",
                            dexElementsSuppressedExceptions);
                }

                Field suppressedExceptionsField = dexPathListClass.getDeclaredField("dexElementsSuppressedExceptions");
                suppressedExceptionsField.setAccessible(true);
                IOException[] dexElementsSuppressedExceptions = (IOException[])
                        suppressedExceptionsField.get(pathList);
                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions = (IOException[]) suppressedExceptions
                            .toArray(new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined = new IOException[suppressedExceptions.size() +
                            dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }

                suppressedExceptionsField.set(pathList, dexElementsSuppressedExceptions);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
