package com.example.stock.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SingleStockResponse {
    // 列名
    private List<String> column_names;

    // 股票数据，格式为数组的数组
    private List<List<List<Object>>> grid_data;

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
        private BigDecimal Fmark;
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
