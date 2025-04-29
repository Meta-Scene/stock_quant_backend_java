package com.example.stock.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FiveDayAdjustmentResponse extends StockResponse {
  // 所有符合条件的股票代码列表
  private List<String> ts_codes;
}