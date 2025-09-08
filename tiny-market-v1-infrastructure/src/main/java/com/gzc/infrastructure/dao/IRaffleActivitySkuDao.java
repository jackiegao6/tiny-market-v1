package com.gzc.infrastructure.dao;

import com.gzc.infrastructure.dao.po.RaffleActivitySku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 商品sku dao
 */
@Mapper
public interface IRaffleActivitySkuDao {

    RaffleActivitySku queryActivitySku(Long sku);

    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

    List<RaffleActivitySku> queryActivitySkuListByActivityId(Long activityId);

    List<Long> querySkuList();

}
