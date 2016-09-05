package com.systex.cgbc.search;

import com.alibaba.fastjson.JSONObject;
import com.systex.cgbc.search.bean.Rule;
import com.systex.cgbc.search.query.batch.QueryThread;
import com.systex.cgbc.search.query.batch.ReadRequestFile;
import com.systex.cgbc.search.util.RuleUtil;
import com.systex.cgbc.util.Config;
import com.systex.cgbc.util.DateUtil;
import com.systex.cgbc.util.FileUtil;
import com.systex.cgbc.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 批量查询处理程序
 */
public class BatchProcess {
    private static Logger LOG = LoggerFactory.getLogger(BatchProcess.class);
    private Config config = Config.getInstance();

    private ConcurrentLinkedQueue<String> resultQueue = new ConcurrentLinkedQueue<String>();

    private AtomicInteger precisyCount = new AtomicInteger(0);
    private AtomicInteger fuzzyyCount = new AtomicInteger(0);
    private AtomicInteger nonCount = new AtomicInteger(0);


    public BatchProcess() {
        init();
    }



    /**
     * 主函数，批量处理程序入口
     */
    public static void main(String args[]) {
        int lineNum = 1000;
        String batchType = "stress";
        if (args.length > 1) {
            lineNum = Integer.parseInt(args[0]);
            batchType = args[1];
        }

        BatchProcess bp = new BatchProcess();

        String startTime = DateUtil.getNowTime();
        LOG.info("批处理开始时间:{}", startTime);
        bp.run(lineNum, batchType);
        String endTime = DateUtil.getNowTime();
        LOG.info("批处理结束时间:{}", endTime);

        // 输出批量处理汇总信息
        bp.outputSummaryResult(startTime, endTime);
        // 输出批量处理明细信息
        bp.outputDetailResult();
    }



    /**
     * 初始化
     */
    public void init() {
        String indexName = config.getProperty("indexName");
        String indexType = config.getProperty("indexType");
        // 查询线程初始化
        QueryThread.init(indexName, indexType);

        // 初始化配置规则
        RuleUtil.init();

        // 初始化线程池
        int workQueueSize = config.getInt("workQueueSize");
        int corePoolSize = config.getInt("corePoolSize");
        int maxPoolSize = config.getInt("maxPoolSize");

        ThreadPool.initPool(workQueueSize, corePoolSize, maxPoolSize);
    }

    /**
     * 批量处理程序的主体逻辑部分
     */
    public void run(int lineNum, String batchType) {
        String inputPath = config.getProperty("inputPath");
        int queryThreadSize = config.getInt("queryThreadSize");

        ConcurrentLinkedQueue<JSONObject> requestTaskQueue =
            new ConcurrentLinkedQueue<JSONObject>();

        // 读取申请件文件
        ReadRequestFile readReqFileThread =
            new ReadRequestFile(inputPath, lineNum, requestTaskQueue);
        readReqFileThread.genRequestQueue();

        // 2，启动多个查询请求执行查询批处理任务
        // 线程从任务队列中去取数据，如果一个请求被多个线程并发的去执行的话，如何去将各个线程的执行结果返回 �? �?起，
        CountDownLatch countDownLatch = new CountDownLatch(queryThreadSize);
        ExecutorService executorService = Executors.newFixedThreadPool(queryThreadSize);

        ThreadPoolExecutor queryThreadPool = ThreadPool.getTpe();
        List<Rule> rules = RuleUtil.getRules();
        for (int i = 0; i < queryThreadSize; i++) {
            // 启动查询线程
            Runnable r =
                new QueryThread(queryThreadPool, rules, batchType, requestTaskQueue, resultQueue,
                    countDownLatch, precisyCount, fuzzyyCount, nonCount);
            Thread queryThread = new Thread(r);
            executorService.submit(queryThread);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error("程序处理异常,{}", e.getMessage());
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    /**
     * 输出批量处理的汇总信息
     * 
     * @param startTime
     * @param endTime
     */
    public void outputSummaryResult(String startTime, String endTime) {
        String outputPath = config.getProperty("outputPath");
        PrintWriter pw = FileUtil.getPrintWriter(outputPath + "summary.txt");

        pw.println("开始处理:" + startTime);
        pw.println("完成处理:" + endTime);
        pw.println(
            String.format("精确查询次数:%s,模糊查询次数:%s,未查询到的次数", precisyCount.get(), fuzzyyCount.get(),
                nonCount.get()));
        pw.flush();
        pw.close();
    }

    /**
     * 输出批量处理的明细处理结果
     */
    public void outputDetailResult() {
        String outputPath = config.getProperty("outputPath");
        PrintWriter pw = FileUtil.getPrintWriter(outputPath + "detail.txt");
        while (resultQueue.size() > 0) {
            String result = resultQueue.poll();
            pw.println(result);
        }
        pw.flush();
        pw.close();
    }
}
