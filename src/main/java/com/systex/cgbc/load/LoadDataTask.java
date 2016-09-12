package com.systex.cgbc.load;

import com.google.common.base.Strings;
import com.systex.cgbc.search.util.ClientAPI;
import com.systex.cgbc.util.Config;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


/**
 * 批量加载数据的任务
 */
public class LoadDataTask implements Callable<Object> {
    private static Logger LOG = LoggerFactory.getLogger(LoadDataTask.class);
    private static Config config = Config.getInstance();

    private static int bulkSize;
    private static String[] indexFields;
    private static String indexName;
    private static String type;
    private static Stack<File> files;
    private static String sep = ",";

    private Client client;
    private CountDownLatch countDownLatch;


    /**
     * 构造函数
     *
     * @param countDownLatch
     */
    public LoadDataTask(CountDownLatch countDownLatch) {
        client = new ClientAPI().getClient();
        this.countDownLatch = countDownLatch;
    }

    /**
     * 初始化静态变量
     *
     * @param _files
     * @param _bulkSize
     * @param _indexName
     */
    public static void init(Stack<File> _files, int _bulkSize, String _indexName) {
        LOG.info("初始化LoadDataTask静态数据");
        bulkSize = _bulkSize;
        indexName =
            Strings.isNullOrEmpty(_indexName) ? config.getProperty("indexName") : _indexName;
        type = config.getProperty("indexType");
        indexFields = config.getArrays("fields");
        files = _files;
    }

    /**
     * 任务主体逻辑
     */
    @Override
    public String call() throws Exception {
        while (true) {
            File file = getFile();
            if (file != null) {
                long startTime = System.currentTimeMillis();
                LOG.info("线程{}加载文件{}开始", Thread.currentThread().getName(), file.getName());

                // 索引单个文件
                bulkload(file);
                long endTime = System.currentTimeMillis();
                LOG.info("线程{}加载文件{}结束,耗时:{}毫秒", Thread.currentThread().getName(), file.getName(),
                    (endTime - startTime));
            } else {
                break;
            }
        }
        // 释放资源
        clear();
        countDownLatch.countDown();
        return Thread.currentThread().getName() + "完成数据加载";
    }


    /**
     * 针对单个文件进行批量加载处理
     * 
     * @param file
     */
    private void bulkload(File file) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        BufferedReader reader = null;
        int num = 1;
        try {
            reader =
                new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    new FileInputStream(file)), "utf-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] fieldValues = line.split(sep);
                XContentBuilder builder = buildRequest(fieldValues);
                bulkRequest.add(client.prepareIndex(indexName, type).setSource(builder));
                if (num >= bulkSize) {
                    bulkRequest.execute().actionGet();
                    LOG.info("线程{}针对文件{}处理了{}条", Thread.currentThread().getName(), file.getName(),
                        num);
                    num = 1;
                    bulkRequest = client.prepareBulk();
                }
                num++;
            }
        } catch (Exception e) {
            LOG.error("处理文件{}出现异常:{}", file.getName(), e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LOG.error("关闭文件{}出现异常:{}", file.getName(), e.getMessage());
                e.printStackTrace();
            }
        }
        // 如果bulkRequest还有未提交的请求，将其提交
        if (bulkRequest.numberOfActions() > 0) {
            bulkRequest.execute().actionGet();
        }
        LOG.info("线程{}针对文件{}共处理{}条", Thread.currentThread().getName(), file.getName(), num);
    }


    /**
     * 生成索引Request
     * 
     * @param fieldValues
     * @return
     */
    public XContentBuilder buildRequest(String[] fieldValues) {
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder().startObject();
            int len = indexFields.length;
            for (int i = 0; i < len; i++) {
                builder.field(indexFields[i], fieldValues[i]);
            }
            builder.endObject();
        } catch (IOException e) {
            LOG.error("创建builder出现异常:{}", e.getMessage());
            e.printStackTrace();
        }
        return builder;
    }


    private synchronized File getFile() {
        LOG.info("目前文件数{}", files.size());
        if (files.size() == 0) {
            return null;
        } else {
            return files.pop();
        }
    }


    /**
     * 资源释放
     */
    private void clear() {
        if (client != null) {
            client.close();
        }
    }
}
