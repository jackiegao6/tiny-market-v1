package com.gzc.domain.rebate.model.entity;

import com.gzc.domain.rebate.model.valobj.BehaviorTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {

    private String userId;

    private BehaviorTypeVO behaviorVO;

    private String outBusinessNo;
}
