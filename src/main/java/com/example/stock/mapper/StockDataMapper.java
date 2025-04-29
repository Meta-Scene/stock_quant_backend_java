package com.example.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.stock.entity.StockData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 股票数据Mapper接口
 * 用于定义与股票数据相关的数据库操作方法
 */
@Mapper
public interface StockDataMapper extends BaseMapper<StockData> {

  /**
   * 根据日期范围查询股票数据
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 开始日期
   * @param endDate   结束日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 符合条件的股票数据列表
   */
  List<StockData> findByDateRange(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  /**
   * 查询涨停股票数据
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 开始日期
   * @param endDate   结束日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 符合条件的涨停股票数据列表
   */
  List<StockData> findLimitUp(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  /**
   * 查询跌停股票数据
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 开始日期
   * @param endDate   结束日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 符合条件的跌停股票数据列表
   */
  List<StockData> findLimitDown(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  /**
   * 获取数据库中的最小日期
   * 
   * @return 最小日期字符串，格式为YYYY-MM-DD
   */
  String findMinDate();

  /**
   * 获取数据库中的最大日期（最新交易日）
   * 
   * @return 最大日期字符串，格式为YYYY-MM-DD
   */
  String findMaxDate();

  /**
   * 查找指定日期之后的第N个交易日
   * 
   * @param date 基准日期
   * @param n    向后偏移的交易日数量
   * @return 第N个交易日的日期，格式为YYYY-MM-DD
   */
  String findNextNthTradeDate(@Param("date") String date, @Param("n") int n);

  /**
   * 查找指定日期之前的第N个交易日
   * 
   * @param date 基准日期
   * @param n    向前偏移的交易日数量
   * @return 第N个交易日的日期，格式为YYYY-MM-DD
   */
  String findPreviousNthTradeDate(@Param("date") String date, @Param("n") int n);

  /**
   * 统计符合条件的股票数量
   * 
   * @param tsCode      股票代码，可选过滤条件
   * @param startDate   开始日期
   * @param endDate     结束日期
   * @param isLimitUp   是否为涨停，可选过滤条件
   * @param isLimitDown 是否为跌停，可选过滤条件
   * @return 符合条件的股票数量
   */
  Long countStocks(@Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("isLimitUp") Boolean isLimitUp,
      @Param("isLimitDown") Boolean isLimitDown);

  /**
   * 查询半年线股票数据（价格接近120日均线的股票）
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 符合半年线条件的股票数据列表
   */
  List<StockData> findHalfYearLine(@Param("tsCode") String tsCode, @Param("startDate") String startDate,
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  /**
   * 查询年线股票数据（价格接近250日均线的股票）
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 符合年线条件的股票数据列表
   */
  List<StockData> findYearLine(@Param("tsCode") String tsCode, @Param("startDate") String startDate,
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  /**
   * 查询强于大盘的股票数据（斜率大于大盘斜率的股票）
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 强于大盘的股票数据列表
   */
  List<StockData> findOutperformData(@Param("tsCode") String tsCode, @Param("startDate") String startDate,
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  /**
   * 统计符合半年线条件的股票数量
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @return 符合半年线条件的股票数量
   */
  Long countHalfYearLineStocks(@Param("tsCode") String tsCode, @Param("startDate") String startDate);

  /**
   * 统计符合年线条件的股票数量
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @return 符合年线条件的股票数量
   */
  Long countYearLineStocks(@Param("tsCode") String tsCode, @Param("startDate") String startDate);

  /**
   * 统计强于大盘的股票数量
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @return 强于大盘的股票数量
   */
  Long countOutperformStocks(@Param("tsCode") String tsCode, @Param("startDate") String startDate);

  /**
   * 查询弱于大盘的股票数据（斜率小于大盘斜率的股票）
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 弱于大盘的股票数据列表
   */
  List<StockData> findUnderperformData(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  /**
   * 统计弱于大盘的股票数量
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @return 弱于大盘的股票数量
   */
  Long countUnderperformStocks(@Param("tsCode") String tsCode, @Param("startDate") String startDate);

  /**
   * 获取指定股票在指定日期的斜率
   * 
   * @param tsCode    股票代码
   * @param tradeDate 交易日期
   * @return 股票斜率，表示股价的趋势
   */
  Double findStockSlope(String tsCode, String tradeDate);

  /**
   * 获取指定日期的市场斜率（上证指数）
   * 
   * @param tradeDate 交易日期
   * @return 市场斜率，表示大盘的趋势
   */
  Double findMarketSlope(String tradeDate);

  /**
   * 查询具有买卖点的五日调整股票数据
   * 五日调整是指满足特定技术指标条件的股票，bay字段大于0表示存在买点
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @param pageSize  每页数量
   * @param offset    偏移量（用于分页）
   * @return 有买点的五日调整股票数据列表
   */
  List<StockData> findFiveDayAdjustmentStocks(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  /**
   * 获取具有买点的五日调整股票代码列表
   * 返回在指定日期bay字段大于0的所有股票代码
   *
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @return 有买点的五日调整股票代码列表
   */
  List<String> findFiveDayAdjustmentTsCodes(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate);

  /**
   * 统计具有买点的五日调整股票数量
   * 计算在指定日期bay字段大于0的股票数量
   * 
   * @param tsCode    股票代码，可选过滤条件
   * @param startDate 查询日期
   * @return 有买点的五日调整股票数量
   */
  Long countFiveDayAdjustmentStocks(@Param("tsCode") String tsCode, @Param("startDate") String startDate);


  List<StockData> getSingleStockData(String tsCode);
}