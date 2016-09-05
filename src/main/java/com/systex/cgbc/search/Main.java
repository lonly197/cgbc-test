package com.systex.cgbc.search;

import com.systex.cgbc.util.DateUtil;
import com.systex.cgbc.util.ProgramDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序入口类
 */
public class Main {
	private static Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 * 程序入口
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		int exitCode = -1;
		ProgramDriver pgd = new ProgramDriver();

		long start = System.currentTimeMillis();
		LOG.info("程序启动运行,时间是:{}", DateUtil.getNowTime());
		try {
			pgd.addClass("BatchProcess", BatchProcess.class, "执行批量查询测试");
			exitCode = 0;
			pgd.driver(args);
		} catch (Throwable e) {
			LOG.error("程序异常退出,出错信息是:{}", e.getMessage());
			e.printStackTrace();
			exitCode = 1;
		}
		LOG.info("程序运行完成,时间是:{}", DateUtil.getNowTime());
		LOG.info("程序执行耗时:{}", System.currentTimeMillis() - start);
		System.exit(exitCode);
	}
}
