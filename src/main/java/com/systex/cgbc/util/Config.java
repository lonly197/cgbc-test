package com.systex.cgbc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;


/**
 * 读取配置类
 */
public class Config {
    private static Config instance;
    private static Logger LOG = LoggerFactory.getLogger(Config.class);
    private Properties properties = new Properties();


    /**
     * 私有构造函数，生成Property对象
     *
     * @throws ClassNotFoundException
     */
    private Config() {
        try {
            InputStream input = null;
            input = Config.class.getResourceAsStream("/config.properties");
            properties.load(input);
            LOG.info("load config file config.properties success!");
        } catch (Exception e) {
            LOG.error("init config file config.properties failed", e);
            e.printStackTrace();
        }
    }

    /**
     * 返回Config对象
     *
     * @return
     * @throws ClassNotFoundException
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    // 获取用于取得规则配置数据的SQL语句
    public String getRuleSql() {
        Config config = getInstance();
        return config.getProperty("ruledef");
    }


    /**
     * 获取String型变量
     *
     * @param name
     * @return
     */
    public String getProperty(String name) {
        return properties.getProperty(name);
    }


    /**
     * 获取Long型变量
     *
     * @param name
     * @return
     */
    public long getLong(String name) {
        return Long.parseLong(this.getProperty(name));
    }


    /**
     * 获取String型变量
     *
     * @param name
     * @return
     */
    public int getInt(String name) {
        return Integer.parseInt(this.getProperty(name));
    }


    /**
     * 获取String[] 型 变量
     *
     * @param name
     * @return
     */
    public String[] getArrays(String name) {
        String value = this.getProperty(name);
        try {
            LOG.info("value:" + value + " name:" + name);
            return value.split(",");
        } catch (Exception e) {
            LOG.error("拆分fields时出现异常:", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 打倒输出Property文件内容，测试之用。
     *
     * @param properties
     */
    public void printProperty(Properties properties) {
        StringBuffer info = new StringBuffer("\n#####################\n");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            info.append("\t|").append(entry.getKey()).append(" : ").append(entry.getValue())
                .append("\n");
        }
        info.append("#########END###########\n");
        LOG.info(info.toString());
    }

}
