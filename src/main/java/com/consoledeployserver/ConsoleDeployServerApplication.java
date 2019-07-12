package com.consoledeployserver;

import com.consoledeployserver.service.DeployService;
import com.consoledeployserver.util.UnzipUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class ConsoleDeployServerApplication {

	public static void main(String[] args) {
		// 启动前工作
		init();

		SpringApplication.run(ConsoleDeployServerApplication.class, args);
	}

	private static void init() {
		/**
		 * 项目启动，先清除掉原来部署过的文件，只保留jar包
		 */
		UnzipUtil.deleteDirectory(DeployService.path);
		File file = new File(DeployService.path);
		if (!file.exists())
			file.mkdir();
	}
}
