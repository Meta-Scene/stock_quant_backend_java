package com.example.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stock.entity.Collect;
import com.example.stock.mapper.CollectMapper;
import com.example.stock.service.CollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectServiceImpl implements CollectService {

    private static final String COLLECT_KEY = "stock:collect:list";
    
    private final CollectMapper collectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * 服务启动时进行数据同步
     */
    @PostConstruct
    public void init() {
        syncCollects();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addCollect(String tsCode) {
        // 验证股票代码是否存在，使用 CollectMapper 而不是 StockService
        if (!collectMapper.isStockExist(tsCode)) {
            return "股票代码不存在";
        }
        
        // 先查询数据库是否已收藏
        boolean exists = collectMapper.existsByTsCode(tsCode);
        if (exists) {
            // 确保Redis中也存在
            double score = Instant.now().toEpochMilli();
            redisTemplate.opsForZSet().add(COLLECT_KEY, tsCode, score);
            return "该股票已收藏";
        }
        
        // 添加到数据库
        Collect collect = new Collect();
        collect.setTsCode(tsCode);
        collectMapper.insert(collect);
        
        // 添加到Redis
        double score = Instant.now().toEpochMilli();
        redisTemplate.opsForZSet().add(COLLECT_KEY, tsCode, score);
        
        return "股票收藏成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String removeCollect(String tsCode) {
        // 从数据库删除
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getTsCode, tsCode);
        int result = collectMapper.delete(queryWrapper);
        
        // 从Redis删除
        Long removed = redisTemplate.opsForZSet().remove(COLLECT_KEY, tsCode);
        
        if (result > 0 || (removed != null && removed > 0)) {
            return "取消收藏成功";
        } else {
            return "该股票未收藏";
        }
    }

    @Override
    public List<String> getAllCollects() {
        // 从Redis获取
        Set<String> stocks = redisTemplate.opsForZSet().range(COLLECT_KEY, 0, -1);
        
        // 如果Redis中没有数据，从数据库获取并同步到Redis
        if (stocks == null || stocks.isEmpty()) {
            List<String> dbStocks = collectMapper.findAllTsCodes();
            if (dbStocks != null && !dbStocks.isEmpty()) {
                // 同步到Redis
                for (int i = 0; i < dbStocks.size(); i++) {
                    // 使用索引作为分数，保持顺序
                    redisTemplate.opsForZSet().add(COLLECT_KEY, dbStocks.get(i), i);
                }
                return dbStocks;
            }
            return new ArrayList<>();
        }
        
        return new ArrayList<>(stocks);
    }

    @Override
    public boolean isCollected(String tsCode) {
        // 先查询Redis
        Double score = redisTemplate.opsForZSet().score(COLLECT_KEY, tsCode);
        if (score != null) {
            return true;
        }
        
        // Redis中不存在，查询数据库
        boolean exists = collectMapper.existsByTsCode(tsCode);
        
        // 如果数据库中存在但Redis中不存在，同步到Redis
        if (exists) {
            double newScore = Instant.now().toEpochMilli();
            redisTemplate.opsForZSet().add(COLLECT_KEY, tsCode, newScore);
        }
        
        return exists;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCollects() {
        log.info("开始同步收藏数据...");
        
        // 获取数据库中的收藏列表
        List<String> dbStocks = collectMapper.findAllTsCodes();
        
        // 清空Redis中的数据
        redisTemplate.delete(COLLECT_KEY);
        
        // 如果数据库为空，则不做任何操作
        if (dbStocks == null || dbStocks.isEmpty()) {
            log.info("数据库中没有收藏数据，无需同步");
            return;
        }
        
        // 将数据库数据同步到Redis
        for (int i = 0; i < dbStocks.size(); i++) {
            // 使用索引作为分数，保持顺序
            redisTemplate.opsForZSet().add(COLLECT_KEY, dbStocks.get(i), i);
        }
        
        log.info("数据库数据已同步到Redis，共{}条", dbStocks.size());
    }
} 