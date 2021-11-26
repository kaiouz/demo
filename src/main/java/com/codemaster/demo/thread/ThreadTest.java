package com.codemaster.demo.thread;

import java.util.concurrent.TimeUnit;

public class ThreadTest {

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getId() + "：线程");
                TimeUnit.SECONDS.sleep(10);
                throw new RuntimeException("错误");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread.setDefaultUncaughtExceptionHandler((t1, e) -> {
            System.out.println(Thread.currentThread().getId() + "：错误");
        });

        t.setDaemon(false);

        t.start();

        System.out.println(Thread.currentThread().getId() + "：住");

//        TimeUnit.SECONDS.sleep(5);
    }
}
