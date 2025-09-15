package com.gzc.infrastructure.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.gzc.infrastructure.dao.po.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 抽奖活动账户表-日次数
 */
@Mapper
public interface IRaffleActivityAccountDayDao {

    @DBRouter
    RaffleActivityAccountDay queryActivityAccountDayByUserId(RaffleActivityAccountDay raffleActivityAccountDayReq);

    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay raffleActivityAccountDay);

    void insertActivityAccountDay(RaffleActivityAccountDay raffleActivityAccountDay);

    @DBRouter
    Integer queryRaffleActivityAccountDayPartakeCount(RaffleActivityAccountDay raffleActivityAccountDay);

    void addAccountQuota(RaffleActivityAccountDay raffleActivityAccountDay);

}
