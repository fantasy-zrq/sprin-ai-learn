package com.zrq.cn.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author zrq
 * 2026/1/19 19:52
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum RagStatusEnum {
    //解析、分割、存储
    PARSE("解析"),
    SEGMENTATION("分割"),
    STORAGE("存储"),
    FINISH("完成"),
    NOTFOUND("不存在");

    private String value;
}
