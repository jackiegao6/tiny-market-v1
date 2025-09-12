package com.gzc.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description 抽奖奖品列表，应答对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RaffleAwardListResponseDTO implements Serializable {

    // 奖品ID
    private Integer awardId;
    // 奖品标题
    private String awardTitle;
    // 奖品副标题【抽奖1次后解锁】
    private String awardSubtitle;
    // 排序编号
    private Integer sort;

    private Integer awardRuleLockCount;

    // true 已解锁
    private Boolean isAwardUnlock;

    // 还需的解锁次数
    private Integer waitUnlockCount;

}
