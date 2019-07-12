package com.consoledeployserver.controller;

import com.consoledeployserver.service.DeployService;
import com.consoledeployserver.model.ProcessCode;
import com.consoledeployserver.model.Return;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class DeployController {

    @Autowired
    private DeployService deployService;

    /**
     * 上传文件并部署
     * @return
     */
    @PostMapping("/deploy")
    public Return deploy(@RequestParam(value = "file") MultipartFile mulfile,
                         @RequestParam(required = false) String cmd){
        log.info("调用部署....");
        // 没有输入指令
        if ("undefined".equals(cmd) || StringUtils.isBlank(cmd)){
            cmd = null;
        }
        if (mulfile == null)
            return Return.FAIL(ProcessCode.FILE_UPLOAD_ERROR);
        return deployService.deploy(mulfile, cmd);
    }

    /**
     * 获取通过当前部署工具部署过的项目状态
     * @return
     */
    @GetMapping("/deployFile")
    public Return deployFile(){
        return deployService.deployFile();
    }

    /**
     * 停止指定项目
     * @return
     */
    @GetMapping("/stop")
    public Return stop(@RequestParam String fileName){
        return deployService.stop(fileName);
    }

    /**
     * 重启指定项目
     * @return
     */
    @GetMapping("/restart")
    public Return restart(@RequestParam String fileName){
        return deployService.restart(fileName);
    }
}
