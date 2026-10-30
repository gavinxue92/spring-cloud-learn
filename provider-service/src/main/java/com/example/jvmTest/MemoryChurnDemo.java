package com.example.jvmTest;

import java.util.ArrayList;
import java.util.List;

public class MemoryChurnDemo {

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            // 每次循环新建集合，循环结束就失去引用，等待GC回收
            List<String> tempList = new ArrayList<>();
            for (int i = 0; i < 2000; i++) {
                tempList.add("temp_data_" + i);
            }
            Thread.sleep(1);
        }
    }
}
