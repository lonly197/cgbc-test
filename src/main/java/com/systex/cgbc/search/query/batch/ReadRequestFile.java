package com.systex.cgbc.search.query.batch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.systex.cgbc.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 负责从请求文件中读取内容，然后生成查询请求，并将其放入到任务队列之中
 */
public class ReadRequestFile {

    private static Logger LOG = LoggerFactory.getLogger(ReadRequestFile.class);

    private ConcurrentLinkedQueue<JSONObject> requestTaskQueue;// 查询请求队列
    private String reqFilePath;// 查询请求文件路径
    private int lineNum = 1000;
    private String fields;


    public ReadRequestFile() {

    }


    /**
     * 构造函数，实例化查询请求文件路径
     */
    public ReadRequestFile(String _reqFilePath, int lineNum,
        ConcurrentLinkedQueue<JSONObject> _requestTaskQueue) {
        this.reqFilePath = _reqFilePath;
        this.lineNum = lineNum;
        requestTaskQueue = _requestTaskQueue;
        Config config = Config.getInstance();
        fields = config.getProperty("fields");
    }


    /**
     * 解析文件，生成请求队列
     */
    public void genRequestQueue() {
        LOG.info("read File start");
        File file = new File(reqFilePath);
        int i = 1;
        if (file.isFile()) {
            try {
                BufferedReader bufferedReader =
                    new BufferedReader(
                        new InputStreamReader(new FileInputStream(new File(reqFilePath)),
                            "utf-8"));
                String line = bufferedReader.readLine();
                while (line != null) {
                    if (i > lineNum) {
                        break;
                    }
                    i++;
                    // 将每行数据解析成为查询请求，将放入到任务队列之中
                    requestTaskQueue.add(this.parseRequest(line));
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                LOG.info("read File finish!,generate " + lineNum + "个请求");
            } catch (Exception e) {
                LOG.error("read File occure exception:", e.getMessage());
                e.printStackTrace();
            }
        }
    }


    /**
     * 解析每行数据，将其解析成一个json
     *
     * @param line 行数据
     */
    private JSONObject parseRequest(String line) {
        Map<String, String> resMap = changeFormat(line);
        String jsonStr = JSON.toJSONString(resMap);
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        return jsonObject;
    }


    /**
     * 将查询字符串转换为Key-Value
     *
     * @param searchData
     * @return
     */
    public Map<String, String> changeFormat(String searchData) {
        Map<String, String> map = new HashMap<String, String>();
        String[] values = searchData.split(",");
        // 随机生成UUID作为申请单ID
        String billId = UUID.randomUUID().toString();
        String[] index_fields = fields.split(",");
        for (int i = 0; i < index_fields.length; i++) {
            map.put(index_fields[i], values[i]);
        }
        map.put("billId", billId);
        return map;
    }
}
