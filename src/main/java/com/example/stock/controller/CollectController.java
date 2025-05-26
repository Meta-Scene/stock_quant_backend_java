package com.example.stock.controller;

import com.example.stock.service.CollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CollectController {

    private final CollectService collectService;
    
    /**
     * 收藏股票
     * @param tsCode
     * @return 操作结果
     */
    @PostMapping("/{ts_code}")
    public ResponseEntity<String> collectStock(@PathVariable("ts_code") String tsCode) {
        if (tsCode == null || tsCode.isEmpty()) {
            return ResponseEntity.badRequest().body("股票代码不能为空");
        }
        
        String result = collectService.addCollect(tsCode);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取所有收藏的股票列表（按收藏先后顺序）
     * @return 收藏的股票代码列表
     */
    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllCollectedStocks() {
        List<String> stockList = collectService.getAllCollects();
        return ResponseEntity.ok(stockList);
    }
    
    /**
     * 取消收藏股票
     * @param tsCode 股票代码
     * @return 操作结果
     */
    @DeleteMapping("/{ts_code}")
    public ResponseEntity<String> removeCollectedStock(@PathVariable("ts_code") String tsCode) {
        String result = collectService.removeCollect(tsCode);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 判断股票是否已被收藏
     * @param tsCode 股票代码
     * @return 是否已收藏
     */
    @GetMapping("/{ts_code}")
    public ResponseEntity<Boolean> isStockCollected(@PathVariable("ts_code") String tsCode) {
        boolean isCollected = collectService.isCollected(tsCode);
        return ResponseEntity.ok(isCollected);
    }
    
    /**
     * 手动触发同步操作
     * @return 操作结果
     */
    @PostMapping("/sync")
    public ResponseEntity<String> syncCollects() {
        collectService.syncCollects();
        return ResponseEntity.ok("同步完成");
    }
} 