package com.consoledeployserver.service;

import com.consoledeployserver.model.ProcessCode;
import com.consoledeployserver.model.Return;
import com.consoledeployserver.util.ConfigUtil;
import com.consoledeployserver.util.DeployUtil;
import com.consoledeployserver.util.UnzipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class DeployService {

    public static String path = ConfigUtil.getPath("deployFile");

    @Autowired
    private ProcessService processService;

    static {
        File f = new File(path);
        if(!f.isDirectory()) {
            f.mkdir();
        }
    }

    /**
     * 单线程上传部署
     * @param mulfile
     * @param cmd
     * @return
     */
    public synchronized Return deploy(MultipartFile mulfile, String cmd) {
        // 如果上传的文件不是zip包或者jar包，则不进行后续操作
        if (!mulfile.getOriginalFilename().endsWith(".zip") && !mulfile.getOriginalFilename().endsWith(".jar"))
            return Return.FAIL(ProcessCode.FILE_UPLOAD_ERROR);

        // 如果上传的是jar包，需要上传者手动输入部署命令
        if (mulfile.getOriginalFilename().endsWith(".jar") && cmd == null){
            return Return.FAIL(ProcessCode.CMD_ERROR);
        }

        String folderPath = getFolderPath(path, mulfile.getOriginalFilename());
        File file = new File(folderPath + "/" + mulfile.getOriginalFilename());

        //处理文件是否已经存在
        if (!dealFile(file)){
            return Return.FAIL(ProcessCode.FILE_DEAL_ERROR);
        }

        // 如果删除过文件之后，需要判断父文件夹是否存在，不存在则创建
        File f = new File(folderPath);
        if (!f.exists()){
            f.mkdir();
        }

        // 如果文件转换异常则返回
        if (!multipart2File(file, mulfile))
            return Return.FAIL(ProcessCode.FILE_CHANGE_ERROR);

        // 上传成功后开始解压部署
        if (!deployFile(file, cmd)){
            Return.FAIL(ProcessCode.DEPLOY_FAILED);
        }

        return Return.SUCCESS(ProcessCode.THREAD_RUNING);
    }

    /**
     * 停止指定项目
     * @return
     */
    public synchronized Return stop(String fileName) {
        ProcessService processService = DeployUtil.deployMap.get(fileName);
        if (processService == null){
            return Return.FAIL(ProcessCode.THREAD_IS_NOT_EXIST);
        }
        if (processService.stop()){
            return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS);
        }
        return Return.FAIL(ProcessCode.THREAD_STOP_FAILED);
    }

    /**
     * 重启指定项目
     * @return
     */
    public Return restart(String fileName) {
        ProcessService processService = DeployUtil.deployMap.get(fileName);

        // 根据文件名找到该文件
        File file = UnzipUtil.findFile(processService.getPath(), fileName);
        if (file == null){
            return Return.FAIL(ProcessCode.FILE_IS_NOT_EXIST);
        }

        String cmd = processService.getCmd();
        if (processService == null){
            // 如果该项目没有进程，则根据文件名到存储地寻找文件jar包并启动项目
            // 在同级目录找到部署命令
            cmd = UnzipUtil.findCmd(file.getParent());

            if (cmd == null){
                log.info("未找到部署命令");
                return Return.FAIL(ProcessCode.CMD_ERROR);
            }

            ProcessService pS = new ProcessService();
            boolean start = pS.start(file, cmd);
            if (start){
                return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS);
            }
            return Return.FAIL(ProcessCode.THREAD_START_FAILED);
        }

        boolean restart = processService.restart(file, cmd);
        if (restart){
            return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS);
        }
        return Return.FAIL(ProcessCode.THREAD_START_FAILED);
    }

    /**
     * 如果已经存在该文件，判断是否启动了项目
     * 如果已经启动了项目则停止项目，删除原文件，只保留日志
     * @param file
     */
    private boolean dealFile(File file) {
        if (!file.exists())
            return true;

        boolean dealSt = true;
        // 查找当前文件夹内是否有jar包，有的话则停止进程
        File parentFile = new File(file.getParent());
        for (File f : parentFile.listFiles()) {
            String file_name = f.getName();
            if (file_name.endsWith(".jar")){
                // 如果有jar包，处理所属进程
                ProcessService processService = DeployUtil.deployMap.get(file_name);
                if (processService != null){
                    boolean stop = processService.stop();
                    if (!stop) {
                        dealSt = false;
                    }
                }
                break;
            }
        }
        // 删除原数据，只保留日志
        UnzipUtil.deleteDirectory(file.getParent());
        return dealSt;
    }

    /**
     * 部署项目，优先使用传入的命令
     * @param file
     */
    private boolean deployFile(File file, String cmd) {
        String name = file.getName();

        // 如果文件是jar包，则直接部署
        if (name.endsWith(".jar")){
            log.info("传入的jar包，直接进行部署。file=" + file.getName() + "，cmd=" + cmd);
            return deployJar(file, cmd);
        }

        // 如果文件是zip包则进行解压操作，再寻找文件夹内的jar包进行部署
        UnzipUtil unzipUtil = new UnzipUtil(file.getPath());
        // 解压缩
        unzipUtil.unzip();

        // 在当前文件夹下寻找jar包和.cmd文件进行部署
        File deployF = null;
        File parentFile = new File(file.getParent());
        for (File f : parentFile.listFiles()) {
            if (deployF == null && f.getName().endsWith(".jar")){
                deployF = f;
            }
            if (cmd == null && f.getName().endsWith(".cmd")){
                try {
                    cmd = FileUtils.readFileToString(f);
                } catch (IOException e) {
                    log.error("获取cmd命令异常：" + file.getName() + "，f=" + f.getName());
                    return false;
                }
            }
            if (deployF != null && cmd != null)
                break;
        }

        if (deployF == null || cmd == null){
            log.info("无法进行部署：deployF={}, cmd={}" , deployF, cmd);
            return false;
        }
        return deployJar(deployF, cmd);
    }

    /**
     * 启动jar文件
     * @param file
     */
    private boolean deployJar(File file, String cmd) {
        return processService.start(file, cmd);
    }

    /**
     * MultipartFile类型转换成File类型
     * @param file
     * @param mulfile
     */
    private Boolean multipart2File(File file, MultipartFile mulfile) {
        try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)){
            byte[] buf = mulfile.getBytes();
            bos.write(buf);
        } catch (IOException e) {
            log.error(mulfile.getOriginalFilename() + "，文件转换失败" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 创建部署文件夹
     * @param path 根路径
     * @param originalFilename 文件名（设定只传.zip和.jar）
     * @return 文件夹路径
     */
    private String getFolderPath(String path, String originalFilename) {
        String name = originalFilename;
        if (originalFilename.endsWith(".zip")){
            name = StringUtils.removeEnd(originalFilename, ".zip");
        } else if (originalFilename.endsWith(".jar")){
            name = StringUtils.removeEnd(originalFilename, ".jar");
        }
        File folder = new File(path + "/" + name + System.currentTimeMillis());
        if(!folder.isDirectory()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    /**
     * 获取通过当前部署工具部署过的正在运行的项目
     * @return
     */
    public Return deployFile() {
        Map hashMap = new HashMap();
        for (Map.Entry<String, ProcessService> entry : DeployUtil.deployMap.entrySet()) {
            ProcessService processService = entry.getValue();
            hashMap.put(entry.getKey(), (processService == null || !processService.isAlive()) ? false : true);
        }
        return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS).put("data", hashMap);
    }
}
