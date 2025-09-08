package com.gzc.domain.activity.service.quota.rule.factory;

import com.gzc.domain.activity.service.quota.rule.IActionChain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultActivityChainFactory {

    private final IActionChain headChain;

    public DefaultActivityChainFactory(Map<String, IActionChain> actionChainMap) {
        headChain = actionChainMap.get(ActionModel.activity_base_action.code);
        headChain.appendNext(actionChainMap.get(ActionModel.activity_sku_stock_action.code));
    }

    public IActionChain openActionChain(){
        return this.headChain;
    }

    @Getter
    @AllArgsConstructor
    public enum ActionModel{
        activity_base_action("activity_base_action", "活动的时间、状态校验"),
        activity_sku_stock_action("activity_sku_stock_action", "活动的sku库存"),
        ;

        private final String code;
        private final String info;
    }
}
