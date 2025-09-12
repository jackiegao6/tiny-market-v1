package com.gzc.api.dto.market;

import lombok.Data;

/**
 * @description 商品购物车请求对象
 */
@Data
public class SkuProductShopCartRequestDTO {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * sku 商品
     */
    private Long sku;

}
