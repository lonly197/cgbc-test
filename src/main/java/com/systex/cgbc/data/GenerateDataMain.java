package com.systex.cgbc.data;

import com.systex.cgbc.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 产生模拟数据类
 */
public class GenerateDataMain {
    private static Logger LOG = LoggerFactory.getLogger(GenerateDataMain.class);


    public static void main(String[] args) throws Exception {
        // 设置默认参数
        String outputDir = "G:/";
        int count = 25000;
        int threadCount = 1;

        // 使用用户配置的参数
        if (args.length > 2) {
            outputDir = args[0];
            count = Integer.parseInt(args[1]);
            threadCount = Integer.parseInt(args[2]);
        }

        long start = System.currentTimeMillis();
        LOG.info("程序启动时间:{}", DateUtil.getNowTime());
        run(outputDir, threadCount, count);
        LOG.info("程序结束时间:{}", DateUtil.getNowTime());
        LOG.info("程序耗时:{}毫秒", System.currentTimeMillis() - start);
    }


    /**
     * 程序的主体逻辑
     * 
     * @param outputDir
     * @param threadCount
     * @param count
     * @throws IOException
     */
    public static void run(String outputDir, int threadCount, int count) throws IOException {
        String sampleFile = "sample-data.csv";
        String outFilePrefix = "guangfa-dat-";

        // 创建线程池
        ExecutorService exector = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 初始化数据
        List<String> data = loadSampleData(sampleFile);

        // 添加产生模拟数据的任务,每个线程的结果输出到一个文件
        for (int i = 0; i < threadCount; i++) {
            String filename = outputDir + File.separator + outFilePrefix + i + ".csv";
            GenerateDataTask task = new GenerateDataTask(data, filename, count, i + 1, latch);
            exector.execute(task);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("程序出现异常:{}", e.getMessage());
            e.printStackTrace();
        } finally {
            exector.shutdown();
        }
    }


    /**
     * 加载样例数据
     * 
     * @param sampleFile
     * @throws IOException
     */
    public static List<String> loadSampleData(String sampleFile) throws IOException {
        LOG.info("读取样例数据开始");
        BufferedReader br =
            new BufferedReader(new InputStreamReader(GenerateDataMain.class.getClassLoader()
                .getResourceAsStream(sampleFile), "utf-8"));

        List<String> data = new ArrayList<String>();
        String line = "";
        while ((line = br.readLine()) != null) {
            data.add(line);
        }
        br.close();
        LOG.info("读取样例数据结束");
        return data;
    }
}
