package com.systex.cgbc.load;

import com.systex.cgbc.util.Config;
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
    private static Config config = Config.getInstance();

    private String inputPath = "G:/data/zhihu/100/";
    private String indexName = "guangfa-mem-20160316";
    private int threadSize = 10;
    private int bulkSize = 1000;

    public LoadData(String[] params) {
        indexName = config.getProperty("indexName");
        inputPath = params[0].trim();
        threadSize = Integer.parseInt(params[1]);
        bulkSize = Integer.parseInt(params[2]);

        LOG.info("批量插入的索引名称:{}", indexName);
        LOG.info("程序加载的数据目录是:{}", inputPath);
        LOG.info("程序配置线程数是:{}", threadSize);
        LOG.info("每个线程（文件）生成的记录数是:{}", bulkSize);
    }

    public void run() {
        long start = System.currentTimeMillis();

        File file = new File(inputPath);

        Stack<File> files = new Stack<File>();
        // 将文件加入set
        if (file.isDirectory()) {
            File[] fileAry = file.listFiles();
            if (fileAry != null) {
                for (int i = 0; i < fileAry.length; i++) {
                    LOG.info("加载的文件:{}", fileAry[i].getAbsolutePath());
                    files.push(fileAry[i]);
                }
            } else {
                LOG.error("需要加载的文件不存在:{}", file.isDirectory());
            }

        }

        LOG.info("加载的文件个数是:{}", files.size());

        // 初始化线程文件栈
        LoadDataTask.init(files, bulkSize, indexName);

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
