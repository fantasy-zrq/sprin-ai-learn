package com.zrq.cn.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author zrq
 * 2026/1/16 19:16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String orderId;            // 订单ID
    private Long userId;               // 用户ID

    private BigDecimal totalAmount;    // 总金额
    private BigDecimal payAmount;      // 实付金额
    private String currency;           // 币种

    private String shippingAddress;    // 收货信息

    private LocalDateTime createTime;  // 下单时间
    private LocalDateTime updateTime;  // 修改时间
}
