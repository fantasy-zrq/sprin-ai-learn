package com.zrq.cn;

import cn.hutool.core.util.HashUtil;
import org.junit.jupiter.api.Test;

/**
 * @author zrq
 * 2026/1/19 20:17
 */
public class Test1 {
    @Test
    public void test1() {
        //-8853846986147022034
        long hfHash = HashUtil.cityHash64("奖学金评定".getBytes());
        System.out.println("hfHash = " + hfHash);
    }
}
