package com.gzc.infrastructure.dao;

import com.gzc.infrastructure.dao.po.DailyBehaviorRebate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 日常行为返利活动配置
 */
@Mapper
public interface IDailyBehaviorRebateDao {

    /**
     * 查询
     * @param behaviorType
     * @return
     */
    List<DailyBehaviorRebate> queryDailyBehaviorRebateByBehaviorType(String behaviorType);

}
