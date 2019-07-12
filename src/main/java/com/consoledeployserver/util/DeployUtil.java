package com.consoledeployserver.util;

import com.consoledeployserver.service.ProcessService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 部署进程管理
 */
public class DeployUtil {
    /**
     * 存储运行的jar包名-》所属进程
     */
    public static Map<String, ProcessService> deployMap = new ConcurrentHashMap();
}
