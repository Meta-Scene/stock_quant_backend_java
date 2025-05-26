package com.example.stock.service;

import com.example.stock.dto.SingleStockResponse;
import org.springframework.stereotype.Service;

import com.example.stock.dto.FiveDayAdjustmentResponse;
import com.example.stock.dto.StockResponse;
import com.example.stock.dto.MacdGoldenCrossResponse;
import com.example.stock.dto.KdjGoldenCrossResponse;
import com.example.stock.dto.LowPriceInflowResponse;
import com.example.stock.dto.HighLevelOutflowResponse;

/**
 * 股票数据服务接口
 */
@Service
public interface StockService {

  /**
   * 获取所有股票数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getAllData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取所有指数数据
   *
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getAllIndexData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取涨停股票数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getLimitUpData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取跌停股票数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getLimitDownData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取半年线股票数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getHalfYearLineData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取年线股票数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getYearLineData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 查询强于大盘数据
   * 
   * @param tsCode
   * @param tradeDate
   * @param pageNum
   * @return
   */
  StockResponse getOutperformData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取弱于大盘的股票数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getUnderperformData(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取指定日期和股票的斜率数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @return 斜率值
   */
  double getStockSlope(String tsCode, String tradeDate);

  /**
   * 获取指定日期的市场(大盘)斜率
   * 
   * @param tradeDate 交易日期
   * @return 市场斜率值
   */
  double getMarketSlope(String tradeDate);

  /**
   * 获取五日调整分析数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 五日调整分析数据响应
   */
  FiveDayAdjustmentResponse getFiveDayAdjustment(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 获取单只股票数据
   * @param tsCode
   * @return
   */
  SingleStockResponse getSingleStockData(String tsCode);
  
  /**
   * 获取MACD金叉分析数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return MACD金叉分析数据响应
   */
  MacdGoldenCrossResponse getMacdGoldenCross(String tsCode, String tradeDate, Integer pageNum);
  
  /**
   * 获取KDJ金叉分析数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return KDJ金叉分析数据响应
   */
  KdjGoldenCrossResponse getKdjGoldenCross(String tsCode, String tradeDate, Integer pageNum);
  
  /**
   * 获取低位资金净流入分析数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 低位资金净流入分析数据响应
   */
  LowPriceInflowResponse getLowPriceInflow(String tsCode, String tradeDate, Integer pageNum);
  
  /**
   * 获取高位资金净流出分析数据
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 高位资金净流出分析数据响应
   */
  HighLevelOutflowResponse getHighLevelOutflow(String tsCode, String tradeDate, Integer pageNum);

  /**
   * 检查对应ts_code是否存在
   * @param tsCode
   * @return
   */
  boolean isStockExist(String tsCode);
  
  /**
   * 获取自选股数据
   * 
   * @param tradeDate 交易日期
   * @param pageNum   页码
   * @return 股票数据响应
   */
  StockResponse getFavoriteStocksData(String tradeDate, Integer pageNum);
}