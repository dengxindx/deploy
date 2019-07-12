package com.consoledeployserver.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置文件工具
 */
@Slf4j
public class ConfigUtil {


    /**
     * 获取文件夹的路径, 相对于程序目录,
     */
    public static String getPath(String file) {

        // 和jar包在同一个文件夹的情况
        String classPath = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (classPath.contains(".jar!")) {
            Matcher matcher = Pattern.compile("/[^/]*?\\.jar!/").matcher(classPath);
            if (matcher.find()) {
                String group = matcher.group();
                String path = StringUtils.substringBefore(classPath, group);
                if (path.startsWith("file:")) {
                    path = path.substring(5);
                }
                log.info("获取文件路径: " + path + "/" + file);
                return path + "/" + file;
            }
        }


        URL resource;

        // 本地开发环境调试(配置文件在classes文件夹)
        resource = Thread.currentThread().getContextClassLoader().getResource(file);

        if (resource != null) {
            return resource.getPath();
        }

        // 本地开发环境调试(配置文件在target文件夹)
        resource = Thread.currentThread().getContextClassLoader().getResource("");

        if (resource != null) {
            String path = resource.getPath();

            if (path == null) {
                return null;
            }

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if (path.startsWith("\\")) {
                path = path.substring(1);
            }

            if (path.contains("/classes") || path.contains("\\classes")) {
                path = StringUtils.substringBefore(path, "classes");
                path += file;
                return path;
            }

            if (path.contains("/test-classes") || path.contains("\\test-classes")) {
                path = StringUtils.substringBefore(path, "test-classes");
                path += file;
                return path;
            }

            return path += file;
        }

        return file;
    }
}
