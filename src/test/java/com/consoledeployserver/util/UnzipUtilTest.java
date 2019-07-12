package com.consoledeployserver.util;

import com.consoledeployserver.service.DeployService;
import org.junit.Test;

import java.io.File;

public class UnzipUtilTest {

    @Test
    public void findFile() throws Exception {
        File file = UnzipUtil.findFile(DeployService.path, "console-deploy-server-0.0.1-SNAPSHOT.jar");
        System.out.println(file);
    }

}