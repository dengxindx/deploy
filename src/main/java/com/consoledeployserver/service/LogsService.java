package com.consoledeployserver.service;

import com.consoledeployserver.model.PageInfo;
import com.consoledeployserver.model.ProcessCode;
import com.consoledeployserver.model.Return;
import com.consoledeployserver.util.ConfigUtil;
import com.consoledeployserver.util.DeployUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LogsService {

    /**
     * 设定每页展示的行数
     */
    private static int pageLine = 1000;

    /**
     * 部署项目名->返回名称，文件
     */
    static Map<String, Map<String, File>> deployMap = new ConcurrentHashMap<>();

    /**
     * 获取部署文件路径
     */
    public static String path = ConfigUtil.getPath("deployFile");

    /**
     * 获取通过当前部署工具部署过项目的日志
     * @return
     */
    public Return deployLogs() {
        return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS).put("data", DeployUtil.deployMap.keySet());
    }

    /**
     * 查看所选项目日志
     * @param name
     * @return
     */
    public Return logs(String name) {
        log.info("查看【{}】日志", name);
        if (!DeployUtil.deployMap.containsKey(name))
            return Return.FAIL(ProcessCode.FILE_IS_NOT_EXIST);

        // 查询包含该项目名的所有日志文件
        List<String> fileNameList = new ArrayList<>();

        List<File> fileList = new ArrayList<>();
        checkFile(fileList, new File(path), name);

        Map<String, File> map = new HashMap<>();
        for (File file : fileList) {
            fileNameList.add(file.getName());
            map.put(file.getName(), file);
        }
        deployMap.put(name, map);
        return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS).put("data", fileNameList);
    }

    /**
     * 在指定目录/文件内查询包含该项目名的所有日志文件
     * @param fileList
     * @param file
     * @param name
     * @return
     */
    private void checkFile(List<File> fileList, File file, String name) {
        if (!file.exists())
            return;


        if (file.isDirectory()){
            for (File f : file.listFiles()) {
                checkFile(fileList, f, name);
            }
        }else if (file.isFile() && file.getName().endsWith(".log") && file.getName().contains(name)){
            fileList.add(file);
        }

        return;
    }

    /**
     * 展示所选日志
     *
     * @param jarName
     * @param name
     * @param pageNum
     * @return
     */
    public Return showLog(String jarName, String name, int pageNum) throws IOException {
        log.info("{}，获取日志第{}页", name, pageNum);
        Map<String, File> map = deployMap.get(jarName);
        File file = map.get(name);

        List<String> list = FileUtils.readLines(file);

        int startLine = (pageNum - 1) * pageLine;
        int endLine = startLine + pageLine;
        if (startLine > list.size()){
            // TODO 查询数据开始段不存在
        }
        if (endLine > list.size()){
            endLine = list.size();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startLine; i < list.size() && i < endLine; i++){
            sb.append(list.get(i) + "\n");
        }

        PageInfo pageInfo = new PageInfo();
        pageInfo.CalTotalPage(list.size(), pageLine);
        pageInfo.setContent(sb.toString());
        pageInfo.setCurrentPage(pageNum);

//        return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS).put("data", FileUtils.readFileToString(file));
        return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS).put("data", pageInfo);
    }

    // 一个星期的时长
    public static int weekTime = 7 * 24 * 60 * 60 * 1000;

    /**
     * 定时任务清理日志文件
     * @return
     */
    public void cleanLog(){
        long l = System.currentTimeMillis();
        File file = new File(path);
        for (File f : file.listFiles()) {
            if (f.isDirectory()){
                String time = f.getName().split("_sp_")[1];
                Long i = Long.parseLong(time);
                if ((l - i) > weekTime){
                    cleanLogByPath(f);
                }
            }
        }
    }

    /**
     * 删除日志文件
     * @param file
     * @return
     */
    public void cleanLogByPath(File file){
        if (file.isDirectory()){
            if (file.listFiles() != null) {
                for (File f : file.listFiles()) {
                    cleanLogByPath(f);
                }
            }
            file.delete();
            log.info("删除文件【{}】", file.getName());
        }else {
            file.delete();
            log.info("删除文件【{}】", file.getName());
        }
    }
}
