package com.systex.cgbc.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 批量加载任务入口类
 */
public class Main {
    private static Logger LOG = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        //        String[] args = new String[] { "G:/data/zhihu/100/", "10", "100" };
        String USAGE = "请提供输入参数:input path,threadSize,bulkSize";
        if (args.length < 3) {
            System.err.println(USAGE);
            System.exit(1);
        }
        int i = 0;
        for (String param : args) {
            i++;
            LOG.info("第{}个输入参数的值是:{}", i, param);
            if (param.trim() == "") {
                System.err.println("所有参数不能为空,请正确输入程序参数");
                System.exit(1);
            }
        }

        // 批量加载数据
        LoadData load = new LoadData(args);
        load.run();

        System.exit(0);
    }
}
