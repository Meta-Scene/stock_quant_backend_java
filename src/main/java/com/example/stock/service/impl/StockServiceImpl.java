package com.example.stock.service.impl;


import com.example.stock.dto.FiveDayAdjustmentResponse;
import com.example.stock.dto.HighLevelOutflowResponse;
import com.example.stock.dto.KdjGoldenCrossResponse;
import com.example.stock.dto.LowPriceInflowResponse;
import com.example.stock.dto.MacdGoldenCrossResponse;
import com.example.stock.dto.SingleStockResponse;
import com.example.stock.dto.StockResponse;
import com.example.stock.entity.StockData;
import com.example.stock.mapper.StockDataMapper;
import com.example.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

  @Autowired
  private StockDataMapper stockDataMapper;

  @Value("${stock.page.size}")
  private int pageSize;

  @Autowired
  private com.example.stock.service.CollectService collectService;

  /**
   * 获取所有股票数据
   * 
   * @param tsCode
   * @param tradeDate
   * @param pageNum
   * @return
   */
  @Override
  public StockResponse getAllData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;

    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;

    // 严格使用指定日期查询，SQL会自动计算前后20个交易日的范围
    List<StockData> stockList = stockDataMapper.findByDateRange(
        tsCode,
        targetDate,
        null, // 不需要显式传递endDate，SQL会自动计算
        pageSize,
        offset);

    Long totalCount = stockDataMapper.countStocks(tsCode, targetDate, null, null, null);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }

    // 从查询结果中获取实际的日期范围
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);

    return buildResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate);
  }
  
  @Override
  public StockResponse getLimitUpData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;

    // 严格使用指定日期筛选涨停股票
    List<StockData> stockList = stockDataMapper.findLimitUp(
        tsCode,
        targetDate,
        null,
        pageSize,
        offset);

    Long totalCount = stockDataMapper.countStocks(tsCode, targetDate, null, true, null);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }

    // 显示使用日期范围的开始和结束日期（如果有数据）
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);

    return buildLimitResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate);
  }

  @Override
  public StockResponse getAllIndexData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;

    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;

    // 严格使用指定日期查询，SQL会自动计算前后20个交易日的范围
    List<StockData> stockList = stockDataMapper.findIndexByDateRange(
            tsCode,
            targetDate,
            null, // 不需要显式传递endDate，SQL会自动计算
            pageSize,
            offset);

    Long totalCount = stockDataMapper.countStocks(tsCode, targetDate, null, null, null);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }

    // 从查询结果中获取实际的日期范围
    String startDate = stockList.stream()
            .map(StockData::getTradeDate)
            .min(String::compareTo)
            .orElse(targetDate);
    String endDate = stockList.stream()
            .map(StockData::getTradeDate)
            .max(String::compareTo)
            .orElse(targetDate);

    return buildResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate);
  }

  @Override
  public StockResponse getLimitDownData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;

    // 严格使用指定日期筛选跌停股票
    List<StockData> stockList = stockDataMapper.findLimitDown(
        tsCode,
        targetDate,
        null,
        pageSize,
        offset);

    Long totalCount = stockDataMapper.countStocks(tsCode, targetDate, null, null, true);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }

    // 显示使用日期范围的开始和结束日期（如果有数据）
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);

    return buildLimitResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate);
  }

  @Override
  public StockResponse getHalfYearLineData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;

    // 查询半年线数据
    List<StockData> stockList = stockDataMapper.findHalfYearLine(
        tsCode,
        targetDate,
        pageSize,
        offset);

    Long totalCount = stockDataMapper.countHalfYearLineStocks(tsCode, targetDate);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }

    // 从查询结果中获取实际的日期范围
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);

    return buildMaResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate, "ma120");
  }

  @Override
  public StockResponse getYearLineData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;

    // 查询年线数据
    List<StockData> stockList = stockDataMapper.findYearLine(
        tsCode,
        targetDate,
        pageSize,
        offset);

    Long totalCount = stockDataMapper.countYearLineStocks(tsCode, targetDate);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }

    // 从查询结果中获取实际的日期范围
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);

    return buildMaResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate, "ma250");
  }

  /**
   * 获取强于大盘的股票
   * 
   * @param tsCode
   * @param tradeDate
   * @param pageNum
   * @return
   */
  @Override
  public StockResponse getOutperformData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    List<StockData> stockList = stockDataMapper.findOutperformData(tsCode,
        targetDate,
        pageSize,
        offset);

    Long totalCount = stockDataMapper.countOutperformStocks(tsCode, targetDate);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }
    // 从查询结果中获取实际的日期范围
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);

    // 构建返回数据
    return buildMaResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate, "outperform");
  }

  @Override
  public StockResponse getUnderperformData(String tsCode, String tradeDate, Integer pageNum) {
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    List<StockData> stockList = stockDataMapper.findUnderperformData(tsCode,
        targetDate,
        pageSize,
        offset);

    Long totalCount = stockDataMapper.countUnderperformStocks(tsCode, targetDate);

    // 如果没有数据，返回一个空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }
    // 从查询结果中获取实际的日期范围
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);

    // 构建返回数据
    return buildMaResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate, "underperform");
  }

  @Override
  public double getStockSlope(String tsCode, String tradeDate) {
    // 直接从all_stocks_days表中获取预先计算好的斜率
    Double slope = stockDataMapper.findStockSlope(tsCode, tradeDate);
    return slope != null ? slope : 0.0;
  }

  @Override
  public double getMarketSlope(String tradeDate) {
    // 从shangzheng表中获取预先计算的大盘斜率
    Double slope = stockDataMapper.findMarketSlope(tradeDate);
    return slope != null ? slope : 0.0;
  }

  @Override
  public FiveDayAdjustmentResponse getFiveDayAdjustment(String tsCode, String tradeDateStr, Integer pageNum) {
    // 处理ts_code参数为空字符串的情况，将其设置为null
    if (tsCode != null && tsCode.trim().isEmpty()) {
      tsCode = null;
    }

    // 确定查询日期
    String targetDate = StringUtils.isEmpty(tradeDateStr) ? stockDataMapper.findMaxDate() : tradeDateStr;

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    int page = pageNum != null ? pageNum : 1;

    // 获取所有包含买点的股票代码
    List<String> tradingSignalTsCodes = stockDataMapper.findFiveDayAdjustmentTsCodes(tsCode, targetDate);

    // 获取有买点的股票数量
    Long totalCount = stockDataMapper.countFiveDayAdjustmentStocks(tsCode, targetDate);

    // 创建响应对象
    FiveDayAdjustmentResponse response = new FiveDayAdjustmentResponse();
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "state",
        "ma120", "ma250", "name"));
    response.setDate(targetDate);
    response.setPage(page);
    response.setStock_count(totalCount.intValue());
    response.setTs_codes(tradingSignalTsCodes);

    // 没有数据或页码超出范围时，返回空结果集
    if (totalCount == 0 || totalCount <= offset) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 查询包含买卖点的股票数据
    List<StockData> stockList = stockDataMapper.findFiveDayAdjustmentStocks(
        tsCode,
        targetDate,
        pageSize,
        offset);

    // 如果分页查询没有返回数据，返回空结果集
    if (stockList.isEmpty()) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 将股票数据按照股票代码分组，使用专门的转换方法
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockList.stream()
        .map(this::convertToDtoForAnalysis) // 使用专门为五日调整分析设计的转换方法
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 获取特定日期的数据用于排序（股票代码已在SQL中按排序）
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String stockCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(stockCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);

    return response;
  }

  @Override
  public SingleStockResponse getSingleStockData(String tsCode) {
    if (tsCode == null || tsCode.trim().isEmpty()) {
      return null;
    }

    // 查询单只股票的所有时间数据
    List<StockData> stockDataList = stockDataMapper.getSingleStockData(tsCode);

    if (stockDataList.isEmpty()) {
      return null;
    }

    SingleStockResponse response = new SingleStockResponse();

    // 设置列名
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol",  "state",
        "fmark", "ma120", "ma250", "name"));

    // 创建外层List
    List<List<List<Object>>> gridData = new ArrayList<>();

    // 创建中层List (每只股票一个List)
    List<List<Object>> stockDataArray = new ArrayList<>();

    // 将每条股票数据转换为Object数组
    for (StockData stockData : stockDataList) {
      // 根据Fmark值进行处理
      Object fmarkValue;
      if (stockData.getFmark() != null) {
        int fmark = stockData.getFmark();
        if (fmark == 0) {
          // 如果Fmark=0，返回当日high
          fmarkValue = stockData.getHigh();
        } else if (fmark == 1) {
          // 如果Fmark=1，返回当日low
          fmarkValue = stockData.getLow();
        } else if (fmark == 2 || fmark == 3) {
          // 如果Fmark=2或3，返回0
          fmarkValue = BigDecimal.ZERO;
        } else {
          // 其他值保持不变
          fmarkValue = stockData.getFmark();
        }
      } else {
        // Fmark为null时返回0
        fmarkValue = BigDecimal.ZERO;
      }

      // 创建内层List (每行数据)
      List<Object> rowData = Arrays.asList(
          stockData.getTsCode(),
          stockData.getTradeDate(),
          stockData.getOpen(),
          stockData.getHigh(),
          stockData.getLow(),
          stockData.getClose(),
          stockData.getPctChg(),
          stockData.getVol(),
          stockData.getState(),
          fmarkValue, // 处理后的Fmark值
          stockData.getMa120(),
          stockData.getMa250(),
          stockData.getName());

      // 添加到中层List
      stockDataArray.add(rowData);
    }

    // 将中层List添加到外层List
    gridData.add(stockDataArray);

    response.setGrid_data(gridData);

    return response;
  }

  /**
   * 获取MACD金叉分析数据
   * MACD金叉是技术分析中的一种买入信号，表示短期均线上穿长期均线
   * 通过macd_golden_state字段识别具有MACD金叉信号的股票
   * 
   * 实现步骤：
   * 1. 处理传入参数，确定查询日期和分页参数
   * 2. 查询指定日期所有具有MACD金叉信号的股票代码和数量
   * 3. 构建响应对象，设置列名和基本信息
   * 4. 查询符合条件的股票数据，包括前后41个交易日的数据
   * 5. 将数据按照股票代码分组，并转换为前端所需格式
   * 6. 返回完整的响应对象
   * 
   * @param tsCode 股票代码，可选参数，为null时查询所有股票
   * @param tradeDateStr 交易日期，为null时使用最新交易日
   * @param pageNum 页码，用于分页查询
   * @return MACD金叉分析数据，包含符合条件的股票及其前后41个交易日的数据
   */
  @Override
  public MacdGoldenCrossResponse getMacdGoldenCross(String tsCode, String tradeDateStr, Integer pageNum) {
    // 处理ts_code参数为空字符串的情况，将其设置为null
    if (tsCode != null && tsCode.trim().isEmpty()) {
      tsCode = null;
    }

    // 确定查询日期
    String targetDate = StringUtils.isEmpty(tradeDateStr) ? stockDataMapper.findMaxDate() : tradeDateStr;

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    int page = pageNum != null ? pageNum : 1;

    // 获取所有包含MACD金叉信号的股票代码
    List<String> macdGoldenTsCodes = stockDataMapper.findMacdGoldenCrossTsCodes(tsCode, targetDate);

    // 获取有MACD金叉信号的股票数量
    Long totalCount = stockDataMapper.countMacdGoldenCrossStocks(tsCode, targetDate);

    // 创建响应对象
    MacdGoldenCrossResponse response = new MacdGoldenCrossResponse();
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "macd_golden_state",
        "ma120", "ma250", "name"));
    response.setDate(targetDate);
    response.setPage(page);
    response.setStock_count(totalCount.intValue());
    response.setTs_codes(macdGoldenTsCodes);

    // 没有数据或页码超出范围时，返回空结果集
    if (totalCount == 0 || totalCount <= offset) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 查询包含MACD金叉信号的股票数据
    List<StockData> stockList = stockDataMapper.findMacdGoldenCrossStocks(
        tsCode,
        targetDate,
        pageSize,
        offset);

    // 如果分页查询没有返回数据，返回空结果集
    if (stockList.isEmpty()) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 将股票数据按照股票代码分组，使用专门的转换方法
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockList.stream()
        .map(entity -> convertToDtoForAnalysis(entity, "macd_golden_state")) // 传递macd_golden_state标识
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 获取特定日期的数据用于排序（股票代码已在SQL中按排序）
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String stockCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(stockCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);

    return response;
  }
  
  /**
   * 获取KDJ金叉分析数据
   * KDJ金叉是技术分析中另一种重要的买入信号，指K线上穿D线形成的交叉
   * 通过kdj_gloden_state字段识别具有KDJ金叉信号的股票
   * 
   * 实现步骤：
   * 1. 处理传入参数，确定查询日期和分页参数
   * 2. 查询指定日期所有具有KDJ金叉信号的股票代码和数量
   * 3. 构建响应对象，设置列名和基本信息
   * 4. 查询符合条件的股票数据，包括前后41个交易日的数据
   * 5. 将数据按照股票代码分组，并转换为前端所需格式
   * 6. 返回完整的响应对象
   * 
   * @param tsCode 股票代码，可选参数，为null时查询所有股票
   * @param tradeDateStr 交易日期，为null时使用最新交易日
   * @param pageNum 页码，用于分页查询
   * @return KDJ金叉分析数据，包含符合条件的股票及其前后41个交易日的数据
   */
  @Override
  public KdjGoldenCrossResponse getKdjGoldenCross(String tsCode, String tradeDateStr, Integer pageNum) {
    // 处理ts_code参数为空字符串的情况，将其设置为null
    if (tsCode != null && tsCode.trim().isEmpty()) {
      tsCode = null;
    }

    // 确定查询日期
    String targetDate = StringUtils.isEmpty(tradeDateStr) ? stockDataMapper.findMaxDate() : tradeDateStr;

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    int page = pageNum != null ? pageNum : 1;

    // 获取所有包含KDJ金叉信号的股票代码
    List<String> kdjGoldenTsCodes = stockDataMapper.findKdjGoldenCrossTsCodes(tsCode, targetDate);

    // 获取有KDJ金叉信号的股票数量
    Long totalCount = stockDataMapper.countKdjGoldenCrossStocks(tsCode, targetDate);

    // 创建响应对象
    KdjGoldenCrossResponse response = new KdjGoldenCrossResponse();
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "kdj_gloden_state",
        "ma120", "ma250", "name"));
    response.setDate(targetDate);
    response.setPage(page);
    response.setStock_count(totalCount.intValue());
    response.setTs_codes(kdjGoldenTsCodes);

    // 没有数据或页码超出范围时，返回空结果集
    if (totalCount == 0 || totalCount <= offset) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 查询包含KDJ金叉信号的股票数据
    List<StockData> stockList = stockDataMapper.findKdjGoldenCrossStocks(
        tsCode,
        targetDate,
        pageSize,
        offset);

    // 如果分页查询没有返回数据，返回空结果集
    if (stockList.isEmpty()) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 将股票数据按照股票代码分组，使用专门的转换方法
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockList.stream()
        .map(entity -> convertToDtoForAnalysis(entity, "kdj_gloden_state")) // 传递kdj_gloden_state标识
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 获取特定日期的数据用于排序（股票代码已在SQL中按排序）
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String stockCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(stockCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);

    return response;
  }
  
  /**
   * 获取低位资金净流入分析数据
   * 低位资金净流入表示股票在低价位时有资金流入，可能是潜在的买入机会
   * 通过low_price_state字段识别具有低位资金净流入信号的股票
   * 
   * 实现步骤：
   * 1. 处理传入参数，确定查询日期和分页参数
   * 2. 查询指定日期所有具有低位资金净流入信号的股票代码和数量
   * 3. 构建响应对象，设置列名和基本信息
   * 4. 查询符合条件的股票数据，包括前后41个交易日的数据
   * 5. 将数据按照股票代码分组，并转换为前端所需格式
   * 6. 返回完整的响应对象
   * 
   * @param tsCode 股票代码，可选参数，为null时查询所有股票
   * @param tradeDateStr 交易日期，为null时使用最新交易日
   * @param pageNum 页码，用于分页查询
   * @return 低位资金净流入分析数据，包含符合条件的股票及其前后41个交易日的数据
   */
  @Override
  public LowPriceInflowResponse getLowPriceInflow(String tsCode, String tradeDateStr, Integer pageNum) {
    // 处理ts_code参数为空字符串的情况，将其设置为null
    if (tsCode != null && tsCode.trim().isEmpty()) {
      tsCode = null;
    }

    // 确定查询日期
    String targetDate = StringUtils.isEmpty(tradeDateStr) ? stockDataMapper.findMaxDate() : tradeDateStr;

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    int page = pageNum != null ? pageNum : 1;

    // 获取所有包含低位资金净流入信号的股票代码
    List<String> lowPriceTsCodes = stockDataMapper.findLowPriceInflowTsCodes(tsCode, targetDate);

    // 获取有低位资金净流入信号的股票数量
    Long totalCount = stockDataMapper.countLowPriceInflowStocks(tsCode, targetDate);

    // 创建响应对象
    LowPriceInflowResponse response = new LowPriceInflowResponse();
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "low_price_state",
        "ma120", "ma250", "name"));
    response.setDate(targetDate);
    response.setPage(page);
    response.setStock_count(totalCount.intValue());
    response.setTs_codes(lowPriceTsCodes);

    // 没有数据或页码超出范围时，返回空结果集
    if (totalCount == 0 || totalCount <= offset) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 查询包含低位资金净流入信号的股票数据
    List<StockData> stockList = stockDataMapper.findLowPriceInflowStocks(
        tsCode,
        targetDate,
        pageSize,
        offset);

    // 如果分页查询没有返回数据，返回空结果集
    if (stockList.isEmpty()) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 将股票数据按照股票代码分组，使用专门的转换方法
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockList.stream()
        .map(entity -> convertToDtoForAnalysis(entity, "low_price_state")) // 传递low_price_state标识
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 获取特定日期的数据用于排序（股票代码已在SQL中按排序）
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String stockCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(stockCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);

    return response;
  }
  
  @Override
  public HighLevelOutflowResponse getHighLevelOutflow(String tsCode, String tradeDateStr, Integer pageNum) {
    // 处理ts_code参数为空字符串的情况，将其设置为null
    if (tsCode != null && tsCode.trim().isEmpty()) {
      tsCode = null;
    }

    // 确定查询日期
    String targetDate = StringUtils.isEmpty(tradeDateStr) ? stockDataMapper.findMaxDate() : tradeDateStr;

    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    int page = pageNum != null ? pageNum : 1;

    // 获取所有包含高位资金净流出信号的股票代码
    List<String> highLevelTsCodes = stockDataMapper.findHighLevelOutflowTsCodes(tsCode, targetDate);

    // 获取有高位资金净流出信号的股票数量
    Long totalCount = stockDataMapper.countHighLevelOutflowStocks(tsCode, targetDate);

    // 创建响应对象
    HighLevelOutflowResponse response = new HighLevelOutflowResponse();
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "high_level_state",
        "ma120", "ma250", "name"));
    response.setDate(targetDate);
    response.setPage(page);
    response.setStock_count(totalCount.intValue());
    response.setTs_codes(highLevelTsCodes);

    // 没有数据或页码超出范围时，返回空结果集
    if (totalCount == 0 || totalCount <= offset) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 查询包含高位资金净流出信号的股票数据
    List<StockData> stockList = stockDataMapper.findHighLevelOutflowStocks(
        tsCode,
        targetDate,
        pageSize,
        offset);

    // 如果分页查询没有返回数据，返回空结果集
    if (stockList.isEmpty()) {
      response.setGrid_data(new ArrayList<>());
      return response;
    }

    // 将股票数据按照股票代码分组，使用专门的转换方法
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockList.stream()
        .map(entity -> convertToDtoForAnalysis(entity, "high_level_state")) // 传递high_level_state标识
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 获取特定日期的数据用于排序（股票代码已在SQL中按排序）
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String stockCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(stockCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);

    return response;
  }

  @Override
  public boolean isStockExist(String tsCode) {
      return stockDataMapper.isExists(tsCode);
  }

  /**
   * 获取日期范围
   *
   * @param tradeDate 交易日期
   * @return 日期范围
   */
  private SimpleImmutableEntry<String, String> getDateRange(String tradeDate) {
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }

    // 获取目标日期前后20个交易日的日期范围
    String startDate = stockDataMapper.findPreviousNthTradeDate(targetDate, 20);
    if (startDate == null) {
      startDate = stockDataMapper.findMinDate(); // 如果没有足够的交易日，则使用最早日期
    }

    String endDate = stockDataMapper.findNextNthTradeDate(targetDate, 20);
    if (endDate == null) {
      endDate = stockDataMapper.findMaxDate(); // 如果没有足够的交易日，则使用最晚日期
    }

    return new SimpleImmutableEntry<>(startDate, endDate);
  }

  /**
   * 构建响应对象
   */
  private StockResponse buildResponse(List<StockData> stockDataList,
      String startDate, String endDate,
      int totalStocks, Integer pageNum, String tradeDateStr) {
    StockResponse response = new StockResponse();

    // 设置列名
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "state",
        "ma120", "ma250", "name"));

    // 设置查询日期
    response.setDate(tradeDateStr != null ? tradeDateStr : stockDataMapper.findMaxDate());

    // 设置分页信息
    int page = pageNum != null ? pageNum : 1;
    response.setPage(page);

    // 根据tradeDateStr获取基准日期
    String baseDate = tradeDateStr != null && !tradeDateStr.isEmpty() ? tradeDateStr : stockDataMapper.findMaxDate();

    // 获取具有最大涨跌幅的股票编码及其涨跌幅值
    Map<String, Double> maxPctChgByStockOnDate = new HashMap<>();

    for (StockData stock : stockDataList) {
      String tsCode = stock.getTsCode();
      String stockDate = stock.getTradeDate();

      // 只考虑基准日期当天或最近的一天的涨跌幅
      if (!maxPctChgByStockOnDate.containsKey(tsCode) ||
          Math.abs(stockDate.compareTo(baseDate)) < Math
              .abs(getClosestDateForStock(stockDataList, tsCode, baseDate).compareTo(baseDate))) {
        maxPctChgByStockOnDate.put(tsCode, stock.getPctChg().doubleValue());
      }
    }

    // 将股票数据按照股票代码分组
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockDataList.stream()
        .map(this::convertToDto)
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 按当天涨跌幅对股票代码进行排序
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());
    sortedStockCodes.sort((tsCode1, tsCode2) -> Double.compare(maxPctChgByStockOnDate.getOrDefault(tsCode2, 0.0),
        maxPctChgByStockOnDate.getOrDefault(tsCode1, 0.0)));

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String tsCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(tsCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);
    response.setStock_count(totalStocks);

    return response;
  }

  /**
   * 获取某只股票在给定日期最近的交易日
   */
  private String getClosestDateForStock(List<StockData> stockDataList, String tsCode, String targetDate) {
    return stockDataList.stream()
        .filter(stock -> stock.getTsCode().equals(tsCode))
        .map(StockData::getTradeDate)
        .min(Comparator.comparing(date -> Math.abs(date.compareTo(targetDate))))
        .orElse(targetDate);
  }

  /**
   * 构建涨停/跌停响应对象
   */
  private StockResponse buildLimitResponse(List<StockData> stockDataList,
      String startDate, String endDate,
      int totalStocks, Integer pageNum, String tradeDateStr) {
    StockResponse response = new StockResponse();

    // 设置列名
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "state",
        "ma120", "ma250", "name"));

    // 设置查询日期
    response.setDate(tradeDateStr != null ? tradeDateStr : stockDataMapper.findMaxDate());

    // 设置分页信息
    int page = pageNum != null ? pageNum : 1;
    response.setPage(page);

    // 根据tradeDateStr获取基准日期
    String baseDate = tradeDateStr != null && !tradeDateStr.isEmpty() ? tradeDateStr : stockDataMapper.findMaxDate();

    // 将股票数据按照股票代码分组
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockDataList.stream()
        .map(this::convertToDto)
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 获取特定日期的数据用于排序（股票代码已在SQL中按涨跌幅排序）
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String tsCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(tsCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);
    response.setStock_count(totalStocks);

    return response;
  }

  /**
   * 构建MA线响应对象
   */
  private StockResponse buildMaResponse(List<StockData> stockDataList,
      String startDate, String endDate,
      int totalStocks, Integer pageNum, String tradeDateStr, String maType) {
    StockResponse response = new StockResponse();

    // 设置列名
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "state",
        "ma120", "ma250", "name"));

    // 设置查询日期
    response.setDate(tradeDateStr != null ? tradeDateStr : stockDataMapper.findMaxDate());

    // 设置分页信息
    int page = pageNum != null ? pageNum : 1;
    response.setPage(page);

    // 根据tradeDateStr获取基准日期
    String baseDate = tradeDateStr != null && !tradeDateStr.isEmpty() ? tradeDateStr : stockDataMapper.findMaxDate();

    // 将股票数据按照股票代码分组
    Map<String, List<StockResponse.StockData>> groupedDataByStock = stockDataList.stream()
        .map(this::convertToDto)
        .collect(Collectors.groupingBy(StockResponse.StockData::getTsCode));

    // 获取特定日期的数据用于排序
    List<String> sortedStockCodes = new ArrayList<>(groupedDataByStock.keySet());

    // 转换为grid_data格式：List<List<List<Object>>>
    List<List<List<Object>>> gridData = new ArrayList<>();

    for (String tsCode : sortedStockCodes) {
      List<List<Object>> stockDataArray = new ArrayList<>();

      // 对同一只股票的数据按照日期升序排序
      List<StockResponse.StockData> sortedData = groupedDataByStock.get(tsCode).stream()
          .sorted(Comparator.comparing(StockResponse.StockData::getTradeDate))
          .collect(Collectors.toList());

      // 将每个股票的数据转换为Object[]并添加到stockDataArray
      for (StockResponse.StockData data : sortedData) {
        stockDataArray.add(Arrays.asList(data.toObjectArray()));
      }

      gridData.add(stockDataArray);
    }

    // 设置响应数据
    response.setGrid_data(gridData);
    response.setStock_count(totalStocks);

    return response;
  }

  /**
   * 将实体转换为DTO (用于常规查询)
   */
  private StockResponse.StockData convertToDto(StockData entity) {
    StockResponse.StockData dto = new StockResponse.StockData();
    dto.setTsCode(entity.getTsCode());
    dto.setTradeDate(entity.getTradeDate());
    dto.setOpen(entity.getOpen());
    dto.setHigh(entity.getHigh());
    dto.setLow(entity.getLow());
    dto.setClose(entity.getClose());
    dto.setPctChg(entity.getPctChg());
    dto.setVol(entity.getVol());

    // 常规查询中state置为0
    dto.setState(BigDecimal.ZERO);

    dto.setAmount(entity.getAmount());
    dto.setMa120(entity.getMa120());
    dto.setMa250(entity.getMa250());
    dto.setName(entity.getName());
    return dto;
  }

  /**
   * 将实体转换为DTO，用于分析场景
   * 根据分析类型返回不同的状态字段
   */
  private StockResponse.StockData convertToDtoForAnalysis(StockData entity, String stateType) {
    StockResponse.StockData dto = new StockResponse.StockData();
    dto.setTsCode(entity.getTsCode());
    dto.setTradeDate(entity.getTradeDate());
    dto.setOpen(entity.getOpen());
    dto.setHigh(entity.getHigh());
    dto.setLow(entity.getLow());
    dto.setClose(entity.getClose());
    dto.setPctChg(entity.getPctChg());
    dto.setVol(entity.getVol());
    dto.setAmount(entity.getAmount());
    dto.setMa120(entity.getMa120());
    dto.setMa250(entity.getMa250());
    dto.setName(entity.getName());
    
    // 根据分析类型设置state字段的值
    switch (stateType) {
      case "macd_golden_state":
        dto.setState(entity.getMacdGoldenState() != null ? new BigDecimal(entity.getMacdGoldenState()) : BigDecimal.ZERO);
        break;
      case "kdj_gloden_state":
        dto.setState(entity.getKdjGlodenState() != null ? new BigDecimal(entity.getKdjGlodenState()) : BigDecimal.ZERO);
        break;
      case "low_price_state":
        dto.setState(entity.getLowPriceState() != null ? new BigDecimal(entity.getLowPriceState()) : BigDecimal.ZERO);
        break;
      case "high_level_state":
        dto.setState(entity.getHighLevelState() != null ? new BigDecimal(entity.getHighLevelState()) : BigDecimal.ZERO);
        break;
      default:
        dto.setState(entity.getState());
        break;
    }
    
    return dto;
  }

  // 修改原有方法，兼容新的转换逻辑
  private StockResponse.StockData convertToDtoForAnalysis(StockData entity) {
    return convertToDtoForAnalysis(entity, "state");
  }

  /**
   * 构建空响应对象
   */
  private StockResponse buildEmptyResponse(String tradeDate, Integer pageNum, String tradeDateStr) {
    StockResponse response = new StockResponse();
    response.setColumn_names(Arrays.asList(
        "ts_code", "trade_date", "open", "high", "low", "close", "pct_chg", "vol", "state",
        "ma120", "ma250", "name"));
    response.setDate(tradeDateStr != null ? tradeDateStr : tradeDate);
    response.setPage(pageNum != null ? pageNum : 1);
    response.setStock_count(0);
    response.setGrid_data(new ArrayList<>());
    return response;
  }

  @Override
  public StockResponse getFavoriteStocksData(String tradeDate, Integer pageNum) {
    // 获取收藏的股票列表
    List<String> favoriteStocks = collectService.getAllCollects();
    
    if (favoriteStocks == null || favoriteStocks.isEmpty()) {
      return buildEmptyResponse(tradeDate, pageNum, tradeDate);
    }
    
    // 使用传入的日期或获取最新日期
    String targetDate = tradeDate;
    if (targetDate == null || targetDate.isEmpty()) {
      targetDate = stockDataMapper.findMaxDate();
    }
    
    int offset = (pageNum != null ? pageNum - 1 : 0) * pageSize;
    
    // 查询收藏股票的数据
    List<StockData> stockList = stockDataMapper.findByTsCodes(
        favoriteStocks, 
        targetDate,
        null,
        pageSize,
        offset);
    
    Long totalCount = Long.valueOf(favoriteStocks.size());
    
    // 如果没有数据，返回空响应
    if (stockList.isEmpty()) {
      return buildEmptyResponse(targetDate, pageNum, tradeDate);
    }
    
    // 从查询结果中获取实际的日期范围
    String startDate = stockList.stream()
        .map(StockData::getTradeDate)
        .min(String::compareTo)
        .orElse(targetDate);
    String endDate = stockList.stream()
        .map(StockData::getTradeDate)
        .max(String::compareTo)
        .orElse(targetDate);
    
    return buildResponse(stockList, startDate, endDate, totalCount.intValue(), pageNum, tradeDate);
  }
}