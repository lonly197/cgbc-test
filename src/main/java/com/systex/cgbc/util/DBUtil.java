package com.systex.cgbc.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DBUtil {
    private static Logger log = LoggerFactory.getLogger(DBUtil.class);

    private static Map<String, DruidDataSource> dataSources =
        new ConcurrentHashMap<String, DruidDataSource>();

    static {
        Map<String, Properties> pros = init();
        try {
            if (pros != null) {
                Set<String> keys = pros.keySet();
                for (String key : keys) {
                    DruidDataSource dds = (DruidDataSource) DruidDataSourceFactory
                        .createDataSource(pros.get(key));
                    dataSources.put(key, dds);
                }
            }
        } catch (Exception e) {
            log.error("创建数据库连接失败{}", e.getMessage());
            e.printStackTrace();
        }
    }

    private DBUtil() {

    }

    public static Connection getConnection(String dataSource) {
        Connection conn = null;
        if (dataSources.keySet().contains(dataSource)) {
            try {
                conn = dataSources.get(dataSource).getConnection();
            } catch (SQLException e) {
                log.error("获取数据库连接失败{}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.error("===数据源配置有误===");
        }
        return conn;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Properties> init() {
        Map<String, Properties> pros = new HashMap<String, Properties>();
        Element root = getRootElement();
        Element dbEle = root.element("db");
        List<Element> dsEles = dbEle.elements("datasource");
        for (Element element : dsEles) {
            String dataId = element.attributeValue("id");
            Properties p = new Properties();
            p.setProperty("driverClassName",
                getText(element, "driverClassName"));
            p.setProperty("url", getText(element, "url"));
            p.setProperty("username", getText(element, "username"));
            p.setProperty("password", getText(element, "password"));
            p.setProperty("initialSize", getText(element, "initialSize", "2"));
            p.setProperty("minIdle", getText(element, "minIdle", "2"));
            p.setProperty("maxActive", getText(element, "maxActive", "10"));
            p.setProperty("maxWait", getText(element, "maxWait", "600000"));
            p.setProperty("timeBetweenEvictionRunsMillis",
                getText(element, "timeBetweenEvictionRunsMillis", "60000"));
            p.setProperty("minEvictableIdleTimeMillis",
                getText(element, "minEvictableIdleTimeMillis", "60000"));
            p.setProperty("validationQuery",
                getText(element, "validationQuery"));
            p.setProperty("testWhileIdle",
                getText(element, "testWhileIdle", "true"));
            p.setProperty("testOnBorrow",
                getText(element, "testOnBorrow", "false"));
            p.setProperty("testOnReturn",
                getText(element, "testOnReturn", "false"));
            p.setProperty("poolPreparedStatements",
                getText(element, "poolPreparedStatements", "false"));
            p.setProperty(
                "maxPoolPreparedStatementPerConnectionSize",
                getText(element,
                    "maxPoolPreparedStatementPerConnectionSize", "200"));
            p.setProperty("filters", getText(element, "filters", "config"));
            p.setProperty("connectionProperties",
                getText(element, "connectionProperties", "config.decrypt=true"));
            pros.put(dataId, p);
        }
        return pros;
    }

    private static Element getRootElement() {
        Element root = null;
        SAXReader reader = new SAXReader(); // 构造解析器
        try {
            InputStream is = DBUtil.class.getResourceAsStream("/db.xml");
            Document doc = reader.read(is);
            root = doc.getRootElement();
        } catch (Exception e) {
            log.error("解析配置文件失败{}", e.getMessage());
            e.printStackTrace();
        }
        return root;
    }

    private static String getText(Element element, String elementStr,
        String defaultValue) {
        String value = getText(element, elementStr);
        return (value == null || "".equals(value.trim())) ? defaultValue
            : value;
    }

    private static String getText(Element element, String elementStr) {
        return element.elementTextTrim(elementStr);
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("关闭数据库连接失败{}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void closeRs(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeStmt(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("关闭数据库连接失败{}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void closeAll(Connection conn, Statement stmt, ResultSet rs) {
        closeRs(rs);
        closeStmt(stmt);
        close(conn);
    }

}
