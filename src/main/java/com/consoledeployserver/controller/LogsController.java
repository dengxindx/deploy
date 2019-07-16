package com.consoledeployserver.controller;

import com.consoledeployserver.model.ProcessCode;
import com.consoledeployserver.model.Return;
import com.consoledeployserver.service.LogsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class LogsController {

    @Autowired
    private LogsService logsService;

    /**
     * 获取通过当前部署过的项目的日志
     * @return
     */
    @GetMapping("/deployLogs")
    public Return deployLogs(){
        return logsService.deployLogs();
    }

    @GetMapping("/logs")
    public Return logs(@RequestParam String name){
        return logsService.logs(name);
    }

    /**
     * 分页显示日志，默认展示第一页
     * @param name
     * @return
     */
    @GetMapping("/showLog")
    public Return showLog(@RequestParam String jarName,
                          @RequestParam String name,
                          @RequestParam(required = false, defaultValue = "1") int pageCount){
        try {
            return logsService.showLog(jarName, name, pageCount);
        } catch (IOException e) {
            log.error("获取日志异常,jarName={}, name={}, pageNum={}", jarName, name, pageCount, e);
            return Return.FAIL(ProcessCode.API_INVOKE_FAIL);
        }
    }
}
