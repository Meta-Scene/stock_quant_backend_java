package com.example.stock.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("public.all_stocks_days")
public class StockData {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String tsCode;
  private String tradeDate; // 注意这里改为String类型，因为表结构中是varchar
  private BigDecimal open;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal close;
  private BigDecimal preClose;
  private BigDecimal pctChg;
  private BigDecimal vol;
  private BigDecimal amount;
  private BigDecimal bay;
  private BigDecimal ma120; // 半年线
  private BigDecimal ma250; // 年线
  private String name; // 股票名称
  // 注意：没有添加 change, fmark 字段，因为当前业务逻辑中没有使用
}