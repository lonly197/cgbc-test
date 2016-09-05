package com.systex.cgbc.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 批量导入数据到搜索集群
 */
public class LoadData {
    private static Logger LOG = LoggerFactory.getLogger(LoadData.class);

    private String inputPath = "G:/data/zhihu/100/";
    private String indexName = "guangfa-mem-20160316";
    private int threadSize = 10;
    private int bulkSize = 1000;

    public LoadData(String[] params) {
        inputPath = params[0].trim();
        threadSize = Integer.parseInt(params[1]);
        bulkSize = Integer.parseInt(params[2]);
    }

    public void run() {
        long start = System.currentTimeMillis();
        LOG.info("批量插入的索引名称:{}", indexName);

        File file = new File(inputPath);
        LOG.info("程序加载的数据目录是:{}", inputPath);

        Stack<File> files = new Stack<File>();
        // 将文件加入set
        if (file.isDirectory()) {
            File[] fileAry = file.listFiles();
            for (int i = 0; i < fileAry.length; i++) {
                LOG.info("加载的文件:{}", fileAry[i].getAbsolutePath());
                files.push(fileAry[i]);
            }
        }

        LOG.info("加载的文件个数是:{}", files.size());

        // 初始化线程文件栈
        LoadDataTask.init(files, bulkSize);

        // 初始化多线程执行
        ExecutorService executor = Executors.newFixedThreadPool(threadSize);
        CountDownLatch countDownLatch = new CountDownLatch(threadSize);
        for (int i = 0; i < threadSize; i++) {
            LoadDataTask bulkThread = new LoadDataTask(countDownLatch);
            executor.submit(bulkThread);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        executor.shutdown();
        LOG.info("程序运行总时间:{}ms", (System.currentTimeMillis() - start));
    }
}
