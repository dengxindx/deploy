package com.consoledeployserver.service;

import com.consoledeployserver.util.DeployUtil;
import com.consoledeployserver.util.UnzipUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 维护程序进程的服务
 */
@Slf4j
@Service
@Data
public class ProcessService {

    /**
     * 解析进程
     */
    private Process theProcess;

    /**
     * 程序所在目录
     */
    private String path;

    /**
     * 程序运行的jar包名
     */
    private String jarName;

    /**
     * 程序启动命令
     */
    private String cmd;

    /**
     * 判断进程是否在运行状态
     * @return
     */
    public boolean isAlive(){
        return theProcess.isAlive();
    }

    /**
     * 停止工作进程
     */
    public synchronized boolean stop() {
        if (theProcess == null) {                                                                                       // 查询当前有没有已经启动过的进程
            log.error("当前没有可以停止的进程!");
            return true;
        }

        if (!theProcess.isAlive()) {                                                                                    // 判断进程是否本来就处于关闭状态
            log.warn("重复关闭进程!当前进程已经处于关闭状态!");
            return true;
        }

        theProcess.destroy();                                                                                           // 停止进程

        for (int i = 0; i < 5; i++) {                                                                                   // 停止进程后每秒检查进程是否停止,一共尝试五秒
            if (!theProcess.isAlive()) {                                                                                // 如果已经停止则立即返回消息
                log.info("进程关闭成功.");
                return true;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        log.error("进程在5秒内没有停止!请检查部署程序!");                                                               // 5秒内没有停止程序则返回false

        // 如果没有终止则进行强制终止
        if (theProcess.isAlive()){
            theProcess.destroyForcibly();
        }
        for (int i = 0; i < 3; i++) {
            if (!theProcess.isAlive()) {
                log.info("进程强制关闭成功.");
                return true;
            } else {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        log.error("进程强制停止失败!");

        return false;
    }

    /**
     * 启动工作进程
     */
    public synchronized boolean start(Object object, String cmd) {
        File file = null;
        if (object instanceof String){
            file = new File((String)object);
        } else if (object instanceof File){
            file = (File)object;
        }
        if (file == null)
            return false;

        // 删除重复项目
        ProcessService processService = DeployUtil.deployMap.get(file.getName());
        if (processService != null){
            if (!processService.stop()) {
                return false;
            }
            // 不在同一个父目录下，删除原来的文件
            if (!StringUtils.equals(file.getParent(), processService.getPath())){
                UnzipUtil.deleteDirectory(processService.getPath());
            }
            DeployUtil.deployMap.remove(file.getName());
            log.info("删除重复项目进程:{}", file.getName());
        }

        File output_file = new File(file.getParent() + "/" + file.getName() + "_sps_.log");                             // 指定一个输出文件

        ProcessBuilder processBuilder = new ProcessBuilder();                                                           // 创建进程
        processBuilder.directory(new File(file.getParent()));                                                           // 设置启动项目当前所在的目录为工作目录
        processBuilder.command((cmd).split(" "));                                                                       // 设置在新窗口中运行
        processBuilder.redirectErrorStream(true);                                                                       //将错误流重定向到输出流

        try {
            log.info("启动程序...");
            theProcess = processBuilder.start();                                                                        // 记录当前线程

            // 当前文件所在的父路径
            this.path = file.getParent();
            this.cmd = cmd;
            this.jarName = file.getName();

            // 成功启动后存储当前进程
            DeployUtil.deployMap.put(file.getName(), this);

            new Thread(() -> {
                File writeFile = output_file;
                if (!writeFile.exists()){
                    try {
                        writeFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // 已经存在文件
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(theProcess.getInputStream()))){
                    String line = null;
                    try(RandomAccessFile writer = new RandomAccessFile(writeFile, "rw")){
                        while ((line = bufferedReader.readLine()) != null) {
                            if (writeFile.length() == 0){
                                writer.seek(0);
                            }
                            writer.write(line.getBytes());
                            writer.write("\r\n".getBytes());
                        }
                    }catch (Exception e){
                        log.error("追加日志异常，{}", writeFile.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            return true;
        } catch (Exception e) {
            log.error("启动程序失败!", e);
            return false;
        }
    }


    /**
     * 重启工作进程，
     * 如果当前ProcessService实例没有记录进程，则使用传入的路径和命令启动进程，
     * 并且记录路径和命令
     */
    public synchronized boolean restart(File path, String cmd) {
        return stop() && start(path, cmd);
    }
}