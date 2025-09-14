package com.gzc.domain.strategy.service.armory.algorithm.impl;

import com.gzc.domain.strategy.model.entity.StrategyAwardEntity;
import com.gzc.domain.strategy.service.armory.algorithm.AbstractAlgorithm;
import com.gzc.domain.strategy.service.armory.algorithm.IAlgorithm;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service("OLogN")
public class OLogNAlgorithm extends AbstractAlgorithm implements IAlgorithm {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void armoryAlgorithm(String key, List<StrategyAwardEntity> strategyAwardEntities, BigDecimal rateRange) {

        int begin = 0;
        int end = 0;

        Map<Map<Integer, Integer>, Integer> table = new HashMap<>() ;
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
            Integer awardId = strategyAwardEntity.getAwardId();
            BigDecimal awardRate = strategyAwardEntity.getAwardRate();

            end += rateRange.multiply(awardRate).intValue();
            HashMap<Integer, Integer> begin2end = new HashMap<>();
            begin2end.put(begin, end);
            table.put(begin2end, awardId);

            begin = end + 1;
        }

        strategyRepository.storeSearchRateTable(key, end, table);
    }

    @Override
    public Integer raffleAlgorithm(String key) {
        int rateRange = strategyRepository.getRateRange(key);
        int index = secureRandom.nextInt(rateRange);
        Map<Map<String, Integer>, Integer> table = strategyRepository.getMap(key);

        if (table.size() <= 64){
            return binarySearch(index, table);
        }else{
            return threadSearch(index, table);
        }
    }

    private Integer binarySearch(int index, Map<Map<String, Integer>, Integer> table) {

        ArrayList<Map.Entry<Map<String, Integer>, Integer>> entries = new ArrayList<>(table.entrySet());
        entries.sort(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().keySet().iterator().next())));

        int begin = 0;
        int end = entries.size() - 1;

        while (begin <= end){
            int mid = begin + (end - begin) / 2;
            Map.Entry<Map<String, Integer>, Integer> entry = entries.get(mid);
            Map<String, Integer> rangeMap = entry.getKey();
            Map.Entry<String, Integer> range = rangeMap.entrySet().iterator().next();
            int left = Integer.parseInt(range.getKey());
            int right = range.getValue();

            if (index < left){
                begin = mid - 1;
            }else if (index > right){
                end = mid + 1;
            }else{
                return entry.getValue();
            }
        }
        return null;
    }

    private Integer threadSearch(int index, Map<Map<String, Integer>, Integer> table) {

        List<CompletableFuture<Map.Entry<Map<String, Integer>, Integer>>> futures = table.entrySet().stream().map(
                entry -> CompletableFuture.supplyAsync(() -> {
                            Map<String, Integer> rangeMap = entry.getKey();
                            Map.Entry<String, Integer> range = rangeMap.entrySet().iterator().next();
                            int left = Integer.parseInt(range.getKey());
                            int right = range.getValue();

                            if (index >= left && index <= right) {
                                return entry;
                            }
                            return null;
                        }
                        , threadPoolExecutor)
        ).toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try{
            allFutures.join();

            for (CompletableFuture<Map.Entry<Map<String, Integer>, Integer>> future : futures) {
                Map.Entry<Map<String, Integer>, Integer> res = future.getNow(null);
                if (res != null){
                    return res.getValue();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
}
