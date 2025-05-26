package com.example.stock.service;

import java.util.List;

public interface CollectService {
    
    /**
     * 添加收藏股票
     * @param tsCode 股票代码
     * @return 操作结果消息
     */
    String addCollect(String tsCode);
    
    /**
     * 取消收藏股票
     * @param tsCode 股票代码
     * @return 操作结果消息
     */
    String removeCollect(String tsCode);
    
    /**
     * 获取所有收藏的股票代码
     * @return 股票代码列表
     */
    List<String> getAllCollects();
    
    /**
     * 判断股票是否已收藏
     * @param tsCode 股票代码
     * @return 是否已收藏
     */
    boolean isCollected(String tsCode);
    
    /**
     * 同步Redis和数据库中的收藏数据
     */
    void syncCollects();
} 