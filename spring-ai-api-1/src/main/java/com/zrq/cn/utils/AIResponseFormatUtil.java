package com.zrq.cn.utils;

/**
 * @author zrq
 * 2026/1/16 20:06
 */
public class AIResponseFormatUtil {

    public static String getFormat() {
        return """
                Return a JSON object (map) with the following rules:
                
                1. The JSON object keys MUST be the user's ID.
                   - The key type is string.
                   - The key value MUST equal the `userId` field of the corresponding Order.
                
                2. Each value MUST be an Order object with the following fields:
                   {
                     "orderId": string,
                     "userId": number,
                     "totalAmount": number,
                     "payAmount": number,
                     "currency": string,
                     "shippingAddress": string,
                     "createTime": string (ISO-8601 format),
                     "updateTime": string (ISO-8601 format)
                   }
                
                3. The `userId` inside the Order object MUST match the map key.
                
                4. Return ONLY valid JSON. Do not include explanations or extra text.
                """;
    }
}
