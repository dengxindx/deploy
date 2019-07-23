package com.consoledeployserver.service;

import com.consoledeployserver.model.PageInfo;
import com.consoledeployserver.model.ProcessCode;
import com.consoledeployserver.model.Return;
import com.consoledeployserver.util.ConfigUtil;
import com.consoledeployserver.util.DeployUtil;
import com.consoledeployserver.util.TimeUtil;
import com.consoledeployserver.util.UnzipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
     * 本项目日志路径
     */
    public static String local_log_path = ConfigUtil.getPath("__logs");

    /**
     * 一周
     */
    public static long weekDate = 1000 * 60 * 60 * 24 * 7;

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
        // 查询包含该项目名的所有日志文件

        // 同名文件获取最后一个
        Set<String> fileNameSet = new HashSet<>();
        List<File> fileList = new ArrayList<>();
        
        if (StringUtils.equals("deployLogs", name)){
            // 查找本项目日志
            log.info("查询路径老：{}", local_log_path);
            if (!local_log_path.contains("/target")){
                local_log_path = local_log_path.replace("/__logs", "") + "/target/__logs";
            }
            log.info("查询路径新：{}", local_log_path);
            checkFile(fileList, new File(local_log_path), name);
        }else {
            if (!DeployUtil.deployMap.containsKey(name))
                return Return.FAIL(ProcessCode.FILE_IS_NOT_EXIST);
            checkFile(fileList, new File(path), name);
        }

        Map<String, File> map = new HashMap<>();

        SimpleDateFormat sdf = TimeUtil.threadLocal.get();

        for (File file : fileList) {
            // 文件名加上父路径的时间，保证唯一性
            String fileName = file.getName();
            String time = "";
            if (file.getParent().contains("_sp_")){
                time = file.getParent().split("_sp_")[1];
                if (time.matches("\\d+")) time = sdf.format(new Date(Long.parseLong(time))) + "_";
            }
            fileNameSet.add(time + fileName);
            map.put(time + fileName, file);
        }
        deployMap.put(name, map);
        return Return.SUCCESS(ProcessCode.API_INVOKE_SUCCESS).put("data", fileNameSet);
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
     * 删除项目进程指定的输出日志文件一星期前的日志数据
     * （其他的项目日志由运行的项目自己配置删除）
     * @param file
     * @return
     */
    public void cleanLogByPath(File file){
        if (file.isDirectory()){
            // 子文件一级目录下没有.log日志文件，说明该文件夹下没有部署文件，日志已经超过了1个星期，直接删除掉所有
            boolean clean = true;
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".log")) {
                    clean = false;
                    break;
                }
            }
            if (clean){
                UnzipUtil.delete(file);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            for (File f : file.listFiles()) {
                String name = f.getName();
                if (name.endsWith(".log") && name.contains("_sps_")){
                    String time = name.split("_sps_")[1].replace(".log", "");
                    try {
                        if ((System.currentTimeMillis() - sdf.parse(time).getTime()) > weekDate){
                            if (f.delete())
                                log.info("日志{}删除成功", name);
                            else
                                log.info("日志{}删除失败", name);
                        }
                    } catch (ParseException e) {
                        log.error("{}日志时间转换异常", name, e);
                    }
                }
            }
        }
    }

    /**
     * 滚动部署项目的日志
     */
    public static void rollingLog() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String beforeDateStr = sdf.format(DateUtils.addDays(new Date(), -1)); // 前一天

        // 获取到项目jar包的父路径（带时间的目录）
        for (ProcessService processService : DeployUtil.deployMap.values()) {
            String path = processService.getPath();
            File f = new File(path);
            // 一级目录
            for (File ff : f.listFiles()) {
                if (ff.getName().endsWith("_sps_.log")){
                    // 生成历史日志
                    String name = ff.getName();
                    String jarName = name.split("_sps_")[0];
                    File historyLog = new File(ff.getParent() + "/" + jarName + "_sps_" + beforeDateStr + ".log");
                    if (!historyLog.exists()){
                        try {
                            historyLog.createNewFile();
                            log.info("生成历史文件{}", historyLog.getName());
                        } catch (IOException e) {
                            log.error("生成历史文件异常:{}", ff.getName(), e);
                        }

                        try {
                            FileUtils.copyFile(ff, historyLog);
                            log.info("复制到历史文件{}", historyLog.getName());
                        } catch (IOException e) {
                            log.error("复制到历史文件中异常:{}", ff.getName(), e);
                        }

                        // 清除日志内容
                        try(FileWriter fileWriter =new FileWriter(ff)){
                            fileWriter.write("");
                            fileWriter.flush();
                        }catch (Exception e){
                            log.error("清除日志异常，{}", ff.getName());
                        }
                    }
                }
            }
        }
    }
}
