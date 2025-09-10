package com.gzc.types.common;

public class Constants {

    public final static String SPLIT = ",";
    public final static String COLON = ":";
    public final static String SPACE = " ";
    public final static String UNDERLINE = "_";


    public static class RedisKey {
        public static String STRATEGY_KEY = "big_market_strategy:strategy_key:";
        public static String STRATEGY_AWARD_KEY = "big_market_strategy:award_key:";
        public static String STRATEGY_AWARD_LIST_KEY = "big_market_strategy:awards_list_key:";
        public static String STRATEGY_RATE_TABLE_KEY = "big_market_strategy:rate_table_key:";
        public static String STRATEGY_RATE_RANGE_KEY = "big_market_strategy:rate_range_key:";
        public static String RULE_TREE_VO_KEY = "rule_tree_vo_key_";
        public static String STRATEGY_AWARD_COUNT_KEY = "big_market_strategy:award_count_key:";
        public static String STRATEGY_AWARD_COUNT_CONSUME_Q = "big_market_strategy:award_consume_queue:";
        public static String ACTIVITY_KEY = "big_market_activity:activity_info_key:";
        public static String STRATEGY_RULE_WEIGHT_KEY = "strategy_rule_weight_key:";
        public static String ACTIVITY_SKU_KEY = "big_market_activity:sku_key:";
        public static String ACTIVITY_COUNT_KEY = "big_market_activity:count_key:";
        public static String ACTIVITY_SKU_COUNT_QUERY_KEY = "big_market_activity:activity_sku:count_query_key:";
        public static String ACTIVITY_SKU_STOCK_COUNT_KEY = "big_market_activity:sku_stock_count_key:";
    }

}
