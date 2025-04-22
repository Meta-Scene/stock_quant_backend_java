package com.example.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.stock.entity.StockData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StockDataMapper extends BaseMapper<StockData> {

  List<StockData> findByDateRange(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  List<StockData> findLimitUp(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  List<StockData> findLimitDown(
      @Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("pageSize") int pageSize,
      @Param("offset") int offset);

  /**
   * 获取最小日期
   * 
   * @return 最小日期
   */
  String findMinDate();

  /**
   * 获取最大日期
   * 
   * @return 最大日期
   */
  String findMaxDate();

  /**
   * 查找指定日期之后的第N个交易日
   * 
   * @param date 基准日期
   * @param n    向后偏移的交易日数量
   * @return 第N个交易日的日期
   */
  String findNextNthTradeDate(@Param("date") String date, @Param("n") int n);

  /**
   * 查找指定日期之前的第N个交易日
   * 
   * @param date 基准日期
   * @param n    向前偏移的交易日数量
   * @return 第N个交易日的日期
   */
  String findPreviousNthTradeDate(@Param("date") String date, @Param("n") int n);
hahahahha
  Long countStocks(@Param("tsCode") String tsCode,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate,
      @Param("isLimitUp") Boolean isLimitUp,
      @Param("isLimitDown") Boolean isLimitDown);

  List<StockData> findHalfYearLine(@Param("tsCode") String tsCode, @Param("startDate") String startDate,
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  List<StockData> findYearLine(@Param("tsCode") String tsCode, @Param("startDate") String startDate,
      @Param("pageSize") int pageSize, @Param("offset") int offset);

  Long countHalfYearLineStocks(@Param("tsCode") String tsCode, @Param("startDate") String startDate);

  Long countYearLineStocks(@Param("tsCode") String tsCode, @Param("startDate") String startDate);
}