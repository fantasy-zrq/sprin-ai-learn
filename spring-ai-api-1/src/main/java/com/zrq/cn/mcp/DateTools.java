package com.zrq.cn.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

/**
 * @author zrq
 * 2026/1/18 19:42
 */
public class DateTools {

    @Tool(description = "获取当前时间")
    public String getCurrentTime(String placeholder) {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
