package com.systex.cgbc.search.query.batch;

import com.alibaba.fastjson.JSONObject;
import com.systex.cgbc.search.bean.Rule;
import com.systex.cgbc.search.util.CgbcConstants;
import com.systex.cgbc.search.util.QueryAPI;
import com.systex.cgbc.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 查询线程，负责查询
 */
public class QueryThread implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(QueryThread.class);
    private static ThreadPoolExecutor queryThreadPool;
    private static List<Rule> rules;
    private ConcurrentLinkedQueue<JSONObject> requestQueue;// 查询请求任务队列
    private ConcurrentLinkedQueue<String> resultQueue;
    private QueryAPI queryAPI = new QueryAPI();
    private CountDownLatch countDownLatch;
    private AtomicInteger precisyCount;
    private AtomicInteger fuzzyyCount;
    private AtomicInteger nonCount;
    private String batchType;

    /**
     * 空构造函
     */
    public QueryThread() {
    }

    /**
     * @param logFilePath       结果写入文件地址
     * @param _requestTaskQueue 请求任务队列
     * @param _resultQueue      查询结果队列
     * @param countDownLatch
     * @param _precisyCount     精确查询次数
     * @param _fuzzyyCount      模糊查询次数
     * @param _nonCount         未查询到的次数
     */
    public QueryThread(ThreadPoolExecutor _queryThreadPool, List<Rule> _rules, String _batchType,
        ConcurrentLinkedQueue<JSONObject> _requestTaskQueue,
        ConcurrentLinkedQueue<String> _resultQueue,
        CountDownLatch countDownLatch, AtomicInteger _precisyCount, AtomicInteger _fuzzyyCount,
        AtomicInteger _nonCount) {
        queryThreadPool = _queryThreadPool;
        rules = _rules;
        batchType = _batchType;
        requestQueue = _requestTaskQueue;
        resultQueue = _resultQueue;
        this.countDownLatch = countDownLatch;
        precisyCount = _precisyCount;
        fuzzyyCount = _fuzzyyCount;
        nonCount = _nonCount;
        queryAPI.init();
    }

	/**
   * 初始化
	 */
	public static void init(String inexName, String _indexType) {
      CgbcConstants.indexName = inexName;
      CgbcConstants.indexType = _indexType;
  }

    /**
     * 具体查询每个请求，然后将查询结果写入到文件 结果：匹配到字符，是否匹配（Y和N）
     *
     * @param requestEntity
     * @return
     * @throws Exception
     */
    private String response(JSONObject requestEntity) throws Exception {
        SingleQueryThread t =
            new SingleQueryThread(queryAPI, requestEntity, rules, batchType, precisyCount,
                fuzzyyCount, nonCount);
        Future<String> result = queryThreadPool.submit(t);
        return result.get();
    }

    /**
     * 线程执行
     */
    public void run() {
        /**
         * 1，线程的生命周期在于requestQueue为空
         */
        JSONObject requestEntity = requestQueue.poll();
        while (true) {
            // 1，不为空，查询数据
            if (requestEntity != null) {
                // 进行查询操作
                long start = System.currentTimeMillis();
                String result;
                try {
                    result = this.response(requestEntity);
                    resultQueue.add(result);
                } catch (Exception e) {
                    LOG.error("查询出现异常:", e.getMessage());
                    e.printStackTrace();
                }
                long elapsed = System.currentTimeMillis() - start;
                String billId = requestEntity.getString("billId");
                LOG.info("进件单【{}】完成查询,耗时:{} ms", billId, elapsed);

                // 2，不停的去任务队列里面去取任务
                requestEntity = requestQueue.poll();
                // 前台输出，便于查看进度
                if (resultQueue.size() % 500 == 0) {
                    LOG.info(DateUtil.getNowTime() + ":已完成" + resultQueue.size() + "个请求");
                }
            } else if (requestQueue.size() <= 0) {
                break;
            }
        }
        countDownLatch.countDown();
        LOG.info("线程 " + Thread.currentThread().getName() + " 执行完毕！");
    }
}
