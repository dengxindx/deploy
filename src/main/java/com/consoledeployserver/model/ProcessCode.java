package com.consoledeployserver.model;

public enum ProcessCode {

    API_INVOKE_SUCCESS("接口调用成功"),

    API_INVOKE_FAIL("接口调用失败"),

    DEPLOY_FAILED("部署失败"),

    THREAD_RUNING("进程已经启动"),

    THREAD_STOP_FAILED("进程停止失败"),

    THREAD_START_FAILED("进程启动失败"),

    THREAD_IS_NOT_EXIST("进程不存在"),

    FILE_IS_NOT_EXIST("文件不存在"),

    FILE_UPLOAD_ERROR("文件上传有误"),

    FILE_CHANGE_ERROR("文件转换有误"),

    CMD_ERROR("部署命令有误"),

    FILE_DEAL_ERROR("文件处理有误");

    private String message;

    ProcessCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
