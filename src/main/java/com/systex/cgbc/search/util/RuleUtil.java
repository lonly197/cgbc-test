package com.systex.cgbc.search.util;

import com.systex.cgbc.search.bean.Rule;
import com.systex.cgbc.util.Config;
import com.systex.cgbc.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RuleUtil {

    private static Config config = Config.getInstance();

    private static List<Rule> rules = new ArrayList<Rule>();

    private static Logger logger = LoggerFactory.getLogger(RuleUtil.class);

    private static Connection getConnection() {
        Connection conn = DBUtil.getConnection(config.getProperty("datasource"));
        return conn;
    }

    public static void init() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            logger.info("sql:" + config.getRuleSql());
            pstmt = conn.prepareStatement(config.getRuleSql());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Rule rule = new Rule();
                rule.setColName(rs.getString("colName"));
                rule.setTakeNum(rs.getInt("takeNum"));
                rule.setPatternType(rs.getByte("patternType"));
                rule.setScore(rs.getFloat("score"));
                rules.add(rule);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public static List<Rule> getRules() {
        if (rules == null) {
            init();
        }
        return rules;
    }
}
