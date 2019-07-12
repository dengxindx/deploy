package com.consoledeployserver.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * zip解压工具类
 */
@Slf4j
public class UnzipUtil {

    /**
     * 压缩文件路径
     */
    private final String src;

    /**
     * 解压木匾文件夹
     */
    private String dest;

    /**
     * 构造(解压到当前目录)
     */
    public UnzipUtil(String src) {
        this.src = src;

        //默认解压到当前文件夹
        File file = new File(src);
        if (file.exists())
            setDest(file.getParent());
    }

    /**
     * 构造(解压缩到指定目录)
     */
    public UnzipUtil(String src, String dest) {
        this.src = src;
        setDest(dest);
    }

    /**
     * 将文件夹路径补全
     */
    private void setDest(String dest) {
        if (dest.endsWith("/") || dest.endsWith("\\"))
            this.dest = dest;
        else
            this.dest = dest + "/";
    }

    /**
     * 解压缩
     */
    public String unzip() {
        try (
                FileInputStream fileInputStream = new FileInputStream(src);
                ZipInputStream zipInputStream = new ZipInputStream(fileInputStream, Charset.forName("GBK"))
        ) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {

                if (zipEntry.isDirectory())                                                                             // 判断文件条目是否目录条目
                    checkDirectory(dest + zipEntry.getName());

                else {
                    String fname = dest + zipEntry.getName();
                    checkFile(fname);

                    try (FileOutputStream out = new FileOutputStream(fname)) {
                        inToOut(zipInputStream, out);
                    } catch (Exception e) {
                        System.out.println("解压文件写入磁盘失败!");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("解压缩zip文件失败!");
            e.printStackTrace();
        }
        return dest;
    }

    /**
     * 查看文件夹是否存在,如果不存在则创建目录
     */
    public static File checkDirectory(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                return file;
            } else {
                throw new RuntimeException(path + "是文件而不是文件夹!");
            }
        } else {
            if (file.mkdirs()) {
                return file;
            } else {
                throw new RuntimeException("创建文件夹失败:" + path + "!");
            }
        }
    }

    /**
     * 查看文件夹是否存在,如果不存在则创建空白文件.
     */
    public static File checkFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isFile()) {
                return file;
            } else {
                throw new RuntimeException(path + "是文件夹而不是文件!");
            }
        } else {
            try {
                checkDirectory(file.getParent());
                if (file.createNewFile()) {
                    System.out.println("创建文件:" + path);
                    return file;
                } else {
                    throw new RuntimeException("创建文件失败:" + path + "!");
                }
            } catch (IOException e) {
                throw new RuntimeException("创建文件失败:" + path + "!");
            }
        }
    }

    public static boolean delete(String path) {
        File file = new File(path);
        //noinspection SimplifiableIfStatement
        if (!file.exists()) {
            return true;
        } else {
            return file.delete();
        }
    }

    /**
     * 删除除日志外文件
     */
    public static void deleteDirectory(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    if (file.listFiles() != null) {
                        for (File f : file.listFiles()) {
                            deleteDirectory(f.getPath());
                        }
                    }
                    file.delete();
                } else {
                    if (!file.getName().endsWith(".log")){
                        Thread.sleep(10);
                        file.delete();
                    }
                }
            }
        }catch (Exception e){
            log.error("删除文件异常，path={}", path);
        }
    }

    /**
     * 只保留原解压缩文件的日志文件
     * @return
     */
    public static boolean newCreateDirectory(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory() && file.listFiles() != null && file.listFiles().length > 1) {
            String format = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            File otherName = new File(path + format);
            try {
                FileUtils.moveDirectory(file, otherName);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 输入流拷贝到输出流中,不关闭输入流和输出流!
     */
    public static void inToOut(InputStream in, OutputStream out) {
        try {
            byte[] bs = new byte[2048];                                                                                 // 缓冲区
            int i;
            while ((i = in.read(bs, 0, 2048)) != -1) {
                out.write(bs, 0, i);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 解压到指定目录
     */
    public static void unzip(String file, String path) {
        new UnzipUtil(file, path).unzip();
    }

    /**
     * 解压到当前目录
     */
    public static void unzip(String file) {
        new UnzipUtil(file).unzip();
    }

    /**
     *  根据路径和文件名查找指定文件
     * @param path
     * @param fileName
     * @return
     */
    public static File findFile(String path, String fileName){
        File srcFile = null;
        File file = new File(path);
        if (file.exists()){
            for (File f : file.listFiles()) {
                if (f.isDirectory()){
                    srcFile = findFile(f.getPath(), fileName);
                }else if (StringUtils.equals(f.getName(), fileName)){
                    return f;
                }
            }
        }
        return srcFile;
    }

    /**
     * 根据路径查找到部署命令
     * @param parent
     * @return
     */
    public static String findCmd(String parent) {
        File file = new File(parent);
        for (File f : file.listFiles()) {
            if (f.getName().endsWith(".cmd")){
                try {
                    return FileUtils.readFileToString(f);
                } catch (IOException e) {
                    log.error("查找部署命令异常：[{}]", parent);
                    break;
                }
            }
        }
        return null;
    }
}
