package com.consoledeployserver;

import com.consoledeployserver.service.DeployService;
import com.consoledeployserver.util.TimeUtil;
import com.consoledeployserver.util.UnzipUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
@EnableScheduling
public class ConsoleDeployServerApplication {

	public static void main(String[] args) {
		// 启动前工作
		init();

		SpringApplication.run(ConsoleDeployServerApplication.class, args);
	}

	private static void init() {
		/**
		 * 项目启动，先清除掉原来部署过的文件，只保留log
		 */
		SimpleDateFormat sdf = TimeUtil.threadLocal_yyyymmdd.get();
		String todayStr = sdf.format(new Date());
		UnzipUtil.deleteDirectory2(DeployService.path, todayStr);
		File file = new File(DeployService.path);
		if (!file.exists())
			file.mkdir();
	}
}
