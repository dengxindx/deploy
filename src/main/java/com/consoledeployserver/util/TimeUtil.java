package com.consoledeployserver.util;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

@Slf4j
public class TimeUtil {

    // 获取线程安全的共享SimpleDateFormat
    public static ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd_HH_mm_ss"));

    public static ThreadLocal<SimpleDateFormat> threadLocal_yyyymmdd = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd"));

}
