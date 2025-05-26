package com.example.stock.controller;

import com.example.stock.dto.SingleStockResponse;
import com.example.stock.dto.StockResponse;
import com.example.stock.service.StockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api("股票可视化")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StockController {

  // 注入StockService

  private final StockService stockService;

  // 构造函数注入StockService
  public StockController(StockService stockService) {
    this.stockService = stockService;
  }

  // 使用RESTful风格的URL
  @ApiOperation("查询股票数据")
  @GetMapping("/stock_data/{type}")
  public StockResponse getStockData(
      @PathVariable(name = "type") Integer type,
      @RequestParam(name = "ts_code", required = false) String tsCode,
      @RequestParam(name = "trade_date", required = false) String tradeDate,
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer pageNum) {

    // 根据类型参数调用不同的服务方法
    switch (type) {
      case 1: // 全部数据 修改为获取全部指数数据
//        return stockService.getAllData(tsCode, tradeDate, pageNum);
        return stockService.getAllIndexData(tsCode,tradeDate,pageNum);
      case 2: // 涨停数据
        return stockService.getLimitUpData(tsCode, tradeDate, pageNum);
      case 3: // 跌停数据
        return stockService.getLimitDownData(tsCode, tradeDate, pageNum);
      case 4: // 半年线数据
        return stockService.getHalfYearLineData(tsCode, tradeDate, pageNum);
      case 5: // 年线数据
        return stockService.getYearLineData(tsCode, tradeDate, pageNum);
      case 6: // 强于大盘数据
        return stockService.getOutperformData(tsCode, tradeDate, pageNum);
      case 7: // 弱于大盘数据
        return stockService.getUnderperformData(tsCode, tradeDate, pageNum);
      default:
        throw new IllegalArgumentException("不支持的查询类型: " + type);
    }
  }

  /**
   * 获取指定日期和股票的斜率数据
   * 
   * @param ts_code    股票代码
   * @param trade_date 交易日期
   * @return 斜率数据
   */
  @GetMapping("/slope")
  public Map<String, Object> getStockSlope(@RequestParam(required = true) String ts_code,
      @RequestParam(required = true) String trade_date) {
    double slope = stockService.getStockSlope(ts_code, trade_date);
    double marketSlope = stockService.getMarketSlope(trade_date);

    Map<String, Object> response = new HashMap<>();
    response.put("ts_code", ts_code);
    response.put("trade_date", trade_date);
    response.put("slope", slope);
    response.put("market_slope", marketSlope);
    response.put("is_outperform", slope > marketSlope);

    return response;
  }

  /**
   * 分析接口，用于各种分析模型
   * 
   * @param type      分析类型：1-五日调整分析, 2-MACD金叉, 3-KDJ金叉, 4-低位资金净流入, 5-高位资金净流出
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 分析结果
   */
  @ApiOperation("股票分析")
  @GetMapping("/stock_analysis/{type}")
  public Object analyzeStock(
      @PathVariable(name = "type") Integer type,
      @RequestParam(name = "ts_code", required = false) String tsCode,
      @RequestParam(name = "trade_date", required = false) String tradeDate,
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer pageNum) {

    // 根据分析类型调用不同的分析方法
    switch (type) {
      case 1: // 五日调整分析
        return stockService.getFiveDayAdjustment(tsCode, tradeDate, pageNum);
      case 2: // MACD金叉分析
        return stockService.getMacdGoldenCross(tsCode, tradeDate, pageNum);
      case 3: // KDJ金叉分析
        return stockService.getKdjGoldenCross(tsCode, tradeDate, pageNum);
      case 4: // 低位资金净流入分析
        return stockService.getLowPriceInflow(tsCode, tradeDate, pageNum);
      case 5: // 高位资金净流出分析
        return stockService.getHighLevelOutflow(tsCode, tradeDate, pageNum);
      default:
        throw new IllegalArgumentException("不支持的分析类型: " + type);
    }
  }

  @ApiOperation("查询单只股票数据")
  @GetMapping("/stock_single_data")
  public SingleStockResponse getSingleStockData(@RequestParam(name = "ts_code") String tsCode){
      return stockService.getSingleStockData(tsCode);
  }

  @ApiOperation("查询自选股数据")
  @GetMapping("/stock_big_data_analysis/{type}")
  public StockResponse getFavoriteStocksData(
      @PathVariable(name = "type") Integer type,
      @RequestParam(name = "trade_date", required = false) String tradeDate,
      @RequestParam(name = "page", required = false, defaultValue = "1") Integer pageNum) {
      
      // 根据类型参数调用不同的服务方法
      switch (type) {
          case 1: // 暂时保留空的case
              throw new IllegalArgumentException("暂不支持的查询类型: " + type);
          case 2: // 暂时保留空的case
              throw new IllegalArgumentException("暂不支持的查询类型: " + type);
          case 3: // 暂时保留空的case
              throw new IllegalArgumentException("暂不支持的查询类型: " + type);
          case 4: // 自选股数据
              return stockService.getFavoriteStocksData(tradeDate, pageNum);
          default:
              throw new IllegalArgumentException("不支持的查询类型: " + type);
      }
  }

}