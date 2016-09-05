package com.systex.cgbc.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

    private static ThreadPoolExecutor tpe;

    public static void initPool(int workQueueSize, int corePoolSize, int maxPoolSize) {
        LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>(workQueueSize);
        tpe = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, lbq,
            new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor getTpe() {
        return tpe;
    }

    public static void setTpe(ThreadPoolExecutor tpe) {
        ThreadPool.tpe = tpe;
    }
}
