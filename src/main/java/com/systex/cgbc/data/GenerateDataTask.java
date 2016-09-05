package com.systex.cgbc.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * 产生模拟数据线程
 */
public class GenerateDataTask implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(GenerateDataTask.class);

    // 输出路径
    private String output;
    private CountDownLatch latch;
    private int num;
    private GenerateDataUtil gen;


    /**
     *
     * @param data
     *            样例数据集合
     * @param output
     *            输出路径
     * @param num
     *            产生模拟数据条数
     * @param latch
     */
    public GenerateDataTask(List<String> data, String output, int num, int threadNum,
        CountDownLatch latch) {
        this.output = output;
        this.num = num;
        this.latch = latch;
        gen = new GenerateDataUtil(data, threadNum);

    }


    @Override
    public void run() {
        LOG.info("线程" + Thread.currentThread().getName() + "执行开始..");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File(output)), "utf-8"));
            for (int i = 0; i < num; i++) {
                String dat = gen.generateData();
                bw.write(dat);
                bw.newLine();
            }
        } catch (IOException e) {
            LOG.error("产生模拟数据线程出现异常:{}", e.getMessage());
            e.printStackTrace();
        } finally {
            this.latch.countDown();
            try {
                bw.flush();
                bw.close();
            } catch (IOException e) {
                LOG.error("产生模拟数据线程关闭文件出现异常:{}", e.getMessage());
                e.printStackTrace();
            }
            LOG.info("线程" + Thread.currentThread().getName() + "执行结束");
        }
    }
}
