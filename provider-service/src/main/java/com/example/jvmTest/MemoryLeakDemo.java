package com.example.jvmTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存泄漏Demo：静态List，GC Root，持续持有byte数组，无法回收
 * VM Options:
 * -Xms256m -Xmx256m -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=D:/dump/heap.hprof
 */
public class MemoryLeakDemo {
    // static 静态集合，属于GC Root，一旦add，对象永远被引用，无法GC
    private static final List<byte[]> STATIC_CACHE = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        int sizeKb = 10; // 每个数组 10KB
        int count = 0;
        while (true) {
            byte[] data = new byte[sizeKb * 1024];
            STATIC_CACHE.add(data);
            count++;
            if (count % 200 == 0) {
                System.out.println("已经添加：" + count + " 个对象");
            }
            Thread.sleep(2); // 2ms一个，速度适中，既能较快堆满，又留给你dump时间
        }
    }
}
