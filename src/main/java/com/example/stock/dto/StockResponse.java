package com.example.stock.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class StockResponse {
  // 列名
  private List<String> column_names;
  // 选择的日期
  private String date;
  // 股票数据，格式为数组的数组
  private List<List<List<Object>>> grid_data;
  // 当前页码
  private int page;
  // 股票总数
  private int stock_count;

  @Data
  public static class StockData {
    // 股票代码
    private String tsCode;
    // 交易日期
    private String tradeDate;
    // 开盘价
    private BigDecimal open;
    // 最高价
    private BigDecimal high;
    // 最低价
    private BigDecimal low;
    // 收盘价
    private BigDecimal close;
    // 涨跌额
    private BigDecimal pctChg;
    // 成交量
    private BigDecimal vol;
    // Bay值
    private BigDecimal bay;
    // 成交额
    private BigDecimal amount;
    // 半年线
    private BigDecimal ma120;
    // 年线
    private BigDecimal ma250;
    // 股票名称
    private String name;

    // 转换为Object数组，用于grid_data
    public Object[] toObjectArray() {
      return new Object[] {
          tsCode,
          tradeDate,
          open,
          high,
          low,
          close,
          pctChg,
          vol,
          bay,
          ma120,
          ma250,
          name
      };
    }
  }
}