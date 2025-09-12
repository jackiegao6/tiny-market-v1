package com.gzc.infrastructure.dao;

import com.gzc.infrastructure.dao.po.Award;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 奖品表DAO
 */
@Mapper
public interface IAwardDao {

    List<Award> queryAwardList();

    String queryAwardConfigByAwardId(Integer awardId);

    String queryAwardKeyByAwardId(Integer awardId);

}
