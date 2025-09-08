package com.gzc.infrastructure.dao;

import com.gzc.infrastructure.dao.po.UserCreditOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 用户积分流水单 DAO
 */
@Mapper
//@DBRouterStrategy(splitTable = true)
public interface IUserCreditOrderDao {

    void insert(UserCreditOrder userCreditOrderReq);

}
