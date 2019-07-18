package com.consoledeployserver.task;

import com.consoledeployserver.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 定时任务
 * @Service
 */
@Slf4j
@Service
public class TaskService {

    @Autowired
    private LogsService logsService;

    /**
     * 每天0:1定时清理一个超过一个星期的日志
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void cleanLog(){
        log.info("定时任务开始清理超过一个星期的日志....");
        logsService.cleanLog();
        log.info("清理日志完成....");
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void rollingLog(){
        log.info("定时任务开始生成新的日志....");
        logsService.rollingLog();
        log.info("滚动日志完成....");
    }
}
