// 创建 Vue 应用
const { createApp, ref, reactive, onMounted, nextTick, watch } = Vue;

// 获取当前日期，格式为YYYY-MM-DD
const getCurrentDate = () => {
  const date = new Date();
  const year = date.getFullYear();
  let month = date.getMonth() + 1;
  let day = date.getDate();

  month = month < 10 ? '0' + month : month;
  day = day < 10 ? '0' + day : day;

  return `${year}-${month}-${day}`;
};

const app = createApp({
  setup() {
    // 响应式数据
    const searchForm = reactive({
      ts_code: '',
      trade_date: ''
    });
    const stockResponse = ref(null);
    const currentPage = ref(1);
    const activeTab = ref('0'); // 使用字符串类型而不是数字类型
    const currentDataType = ref('all'); // 'all', 'limitUp', 'limitDown', 'halfYearLine', 'yearLine'
    const chartInstances = reactive({});
    const stockSlopeData = ref({});
    const loading = ref(false);

    // 方法
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
      return dateStr.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3');
    };

    const formatDateColumn = (row, column, cellValue) => {
      return formatDate(cellValue);
    };

    // 获取指定股票数据
    const getStockDataByIndex = (index) => {
      if (!stockResponse.value || !stockResponse.value.grid_data) return [];
      if (index < 0 || index >= stockResponse.value.grid_data.length) return [];

      const stockDataArray = stockResponse.value.grid_data[index];
      return stockDataArray;
    };

    // 获取指定股票代码和名称
    const getStockLabelByIndex = (index) => {
      const stockData = getStockDataByIndex(index);
      if (!stockData || stockData.length === 0) return '';

      const tsCode = stockData[0][0]; // 股票代码索引
      const stockName = stockData[0][12]; // 股票名称索引，现在使用column_names中的索引

      return stockName ? `${tsCode} - ${stockName}` : tsCode;
    };

    // 加载数据的方法
    const loadData = async (url) => {
      try {
        loading.value = true;

        // 构建完整URL
        let fullUrl = url;
        let queryParams = [];

        if (searchForm.ts_code) {
          queryParams.push(`ts_code=${searchForm.ts_code}`);
        }

        if (searchForm.trade_date) {
          queryParams.push(`trade_date=${searchForm.trade_date}`);
        } else {
          // 如果没有指定日期，使用当前日期
          const currentDate = getCurrentDate();
          queryParams.push(`trade_date=${currentDate}`);
        }

        if (currentPage.value > 1) {
          queryParams.push(`page=${currentPage.value}`);
        }

        if (queryParams.length > 0) {
          fullUrl += `?${queryParams.join('&')}`;
        }

        console.log('发送请求URL:', fullUrl);

        const response = await fetch(fullUrl);
        if (!response.ok) {
          throw new Error(`请求失败: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        console.log('接收到数据:', data);

        stockResponse.value = data;
        activeTab.value = '0'; // 重置为第一个标签

        // 渲染图表
        nextTick(() => {
          renderCharts();
        });
      } catch (error) {
        console.error('加载数据失败:', error);
        ElementPlus.ElMessage.error('加载数据失败: ' + error.message);
      } finally {
        loading.value = false;
      }
    };

    // 加载全部数据
    const loadAllData = () => {
      currentDataType.value = 'all';
      currentPage.value = 1;
      loadData('/api/stock/data');
    };

    // 加载涨停数据
    const loadLimitUpData = () => {
      currentDataType.value = 'limitUp';
      currentPage.value = 1;
      loadData('/api/stock/data/limit-up');
    };

    // 加载跌停数据
    const loadLimitDownData = () => {
      currentDataType.value = 'limitDown';
      currentPage.value = 1;
      loadData('/api/stock/data/limit-down');
    };

    // 加载半年线数据
    const loadHalfYearLineData = () => {
      currentDataType.value = 'halfYearLine';
      currentPage.value = 1;
      loadData('/api/stock/data/half-year-line');
    };

    // 加载年线数据
    const loadYearLineData = () => {
      currentDataType.value = 'yearLine';
      currentPage.value = 1;
      loadData('/api/stock/data/year-line');
    };

    // 加载强于大盘数据
    const loadOutperformData = () => {
      currentDataType.value = 'outperform';
      currentPage.value = 1;
      loadData('/api/stock/outperform');
    };

    // 加载弱于大盘数据
    const loadUnderperformData = () => {
      currentDataType.value = 'underperform';
      currentPage.value = 1;
      loadData('/api/stock/underperform');
    };

    // 加载五日调整分析数据
    const loadFiveDayAdjustmentData = () => {
      currentDataType.value = 'fiveDayAdjustment';
      currentPage.value = 1;
      loadData('/api/stock_analysis/1');
    };

    // 处理分页变化
    const handlePageChange = (page) => {
      currentPage.value = page;

      switch (currentDataType.value) {
        case 'all':
          loadData('/api/stock/data');
          break;
        case 'limitUp':
          loadData('/api/stock/data/limit-up');
          break;
        case 'limitDown':
          loadData('/api/stock/data/limit-down');
          break;
        case 'halfYearLine':
          loadData('/api/stock/data/half-year-line');
          break;
        case 'yearLine':
          loadData('/api/stock/data/year-line');
          break;
        case 'outperform':
          loadData('/api/stock/outperform');
          break;
        case 'underperform':
          loadData('/api/stock/underperform');
          break;
        case 'fiveDayAdjustment':
          loadData('/api/stock_analysis/1');
          break;
        default:
          loadData('/api/stock/data');
      }
    };

    // 转换数据为图表可用格式
    const prepareChartData = (stockIndex) => {
      const stockData = getStockDataByIndex(stockIndex);
      if (!stockData || stockData.length === 0) return { dates: [], prices: [], volumes: [], ma: [], fmarkPoints: [], trendLineData: [], sortedData: [] };

      // 按日期排序
      const sortedData = [...stockData].sort((a, b) => new Date(a[1]) - new Date(b[1]));

      // 定义索引
      const dateIndex = 1;             // trade_date
      const closeIndex = 5;            // close
      const volIndex = 8;              // vol
      const ma120Index = 9;            // ma120
      const ma250Index = 10;           // ma250
      const bayIndex = 9;              // bay在一些数据集中是第9个元素

      // 提取数据
      const dates = sortedData.map(item => item[dateIndex]);
      const prices = sortedData.map(item => item[closeIndex]);
      const volumes = sortedData.map(item => item[volIndex]);

      // 提取均线数据（如果存在）
      let ma = [];
      if (currentDataType.value === 'halfYearLine') {
        ma = sortedData.map(item => item[ma120Index]);
      } else if (currentDataType.value === 'yearLine') {
        ma = sortedData.map(item => item[ma250Index]);
      }

      // 查找bay数据的点，用于标记
      let fmarkPoints = [];
      if (sortedData.length > 0) {
        sortedData.forEach((item, index) => {
          if (item[bayIndex] > 0) {
            fmarkPoints.push({
              coord: [index, item[closeIndex]],
              value: item[bayIndex],
              date: item[dateIndex]
            });
          }
        });
      }

      let trendLineData = [];

      // 如果是强于大盘或弱于大盘数据，记录一个日志
      if (currentDataType.value === 'outperform' || currentDataType.value === 'underperform') {
        console.log("处理强弱于大盘数据 - 仅显示斜率值");

        // 尝试找到slope值(斜率)的索引位置
        // 因为我们使用了column_names，所以这里不需要硬编码斜率的索引位置
        const firstRecord = sortedData[0];
        console.log("第一条记录:", firstRecord);
      }

      return { dates, prices, volumes, ma, fmarkPoints, trendLineData, sortedData };
    };

    // 渲染图表
    const renderCharts = () => {
      if (!stockResponse.value || !stockResponse.value.grid_data) return;

      // 销毁之前的图表实例
      Object.values(chartInstances).forEach(chart => {
        chart.dispose();
      });

      // 渲染当前激活标签页的图表
      const stockIndex = activeTab.value;
      const tsCode = getStockLabelByIndex(stockIndex);
      const stockName = getStockDataByIndex(stockIndex)[0][12]; // 获取股票名称
      const chartDom = document.getElementById(`chart-${stockIndex}`);

      if (!chartDom) return;

      const chart = echarts.init(chartDom);
      chartInstances[stockIndex] = chart;

      // 准备数据
      const { dates, prices, volumes, ma, fmarkPoints, trendLineData, sortedData } = prepareChartData(stockIndex);

      // 基础图例数据
      const legendData = ['股价', '成交量'];

      // 根据数据类型添加对应的均线
      if (currentDataType.value === 'halfYearLine') {
        legendData.push('半年线');
      } else if (currentDataType.value === 'yearLine') {
        legendData.push('年线');
      }

      // 设置图表选项
      const option = {
        title: {
          text: `${stockName || tsCode} 股价走势`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          formatter: function (params) {
            // 自定义提示框内容
            let result = '';

            if (params.length) {
              // 添加日期
              const dateIndex = params[0].dataIndex;
              result += dates[dateIndex] + '<br/>';

              // 添加每个系列的数据
              params.forEach(param => {
                let color = param.color;
                let seriesName = param.seriesName;
                let value = param.value;
                let marker = '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:' + color + '"></span>';

                if (seriesName === '股价' || seriesName === '半年线' || seriesName === '年线') {
                  result += marker + seriesName + ': ' + value + '<br/>';
                } else if (seriesName === '成交量') {
                  result += marker + seriesName + ': ' + formatVolume(value) + '<br/>';
                }

                // 添加bay信息（如果有）
                if (dateIndex < sortedData.length && sortedData[dateIndex][9] > 0) {
                  result += '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#ff0000"></span>';
                  result += 'Bay: ' + sortedData[dateIndex][9] + '<br/>';
                }

                // 添加斜率信息（如果是强于大盘或弱于大盘数据）
                if ((currentDataType.value === 'outperform' || currentDataType.value === 'underperform') && sortedData && dateIndex < sortedData.length) {
                  const slopeIndex = 10; // 假设斜率在第10个索引位置
                  const currentSlope = Number(sortedData[dateIndex][slopeIndex] || 0);
                  result += '<span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#555"></span>';
                  result += '斜率: ' + currentSlope.toFixed(2) + '%<br/>';
                }
              });
            }

            return result;
          }
        },
        legend: {
          data: legendData,
          top: 30
        },
        dataZoom: [
          {
            type: 'inside',
            xAxisIndex: [0, 1],
            start: currentDataType.value === 'halfYearLine' ? 50 : (currentDataType.value === 'yearLine' ? 60 : 0),
            end: currentDataType.value === 'halfYearLine' ? 75 : (currentDataType.value === 'yearLine' ? 70 : 100)
          },
          {
            show: true,
            xAxisIndex: [0, 1],
            type: 'slider',
            bottom: '10%',
            start: currentDataType.value === 'halfYearLine' ? 50 : (currentDataType.value === 'yearLine' ? 60 : 0),
            end: currentDataType.value === 'halfYearLine' ? 75 : (currentDataType.value === 'yearLine' ? 70 : 100)
          }
        ],
        grid: [
          { left: '5%', right: '5%', top: '15%', height: '50%' },
          { left: '5%', right: '5%', top: '70%', height: '20%' }
        ],
        xAxis: [
          {
            type: 'category',
            data: dates,
            gridIndex: 0,
            axisLabel: {
              rotate: 45
            }
          },
          {
            type: 'category',
            data: dates,
            gridIndex: 1,
            axisLabel: {
              rotate: 45
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '价格',
            gridIndex: 0,
            min: 'dataMin',
            max: 'dataMax'
          },
          {
            type: 'value',
            name: '成交量',
            gridIndex: 1
          }
        ],
        series: [
          {
            name: '股价',
            type: 'line',
            data: prices,
            smooth: true,
            xAxisIndex: 0,
            yAxisIndex: 0,
            markPoint: {
              data: fmarkPoints.map(point => ({
                coord: [dates.indexOf(point.date), point.value],
                value: point.value,
                itemStyle: {
                  color: 'red'
                }
              }))
            }
          },
          {
            name: '成交量',
            type: 'bar',
            data: volumes,
            xAxisIndex: 1,
            yAxisIndex: 1
          }
        ]
      };

      // 根据数据类型添加均线或Fmark标记
      if (currentDataType.value === 'halfYearLine' && ma.length > 0) {
        option.series.push({
          name: '半年线',
          type: 'line',
          data: ma,
          smooth: true,
          xAxisIndex: 0,
          yAxisIndex: 0,
          lineStyle: {
            width: 2,
            type: 'dashed'
          }
        });
      } else if (currentDataType.value === 'yearLine' && ma.length > 0) {
        option.series.push({
          name: '年线',
          type: 'line',
          data: ma,
          smooth: true,
          xAxisIndex: 0,
          yAxisIndex: 0,
          lineStyle: {
            width: 2,
            type: 'dashed'
          }
        });
      }

      chart.setOption(option);
    };

    // 监听窗口大小变化，重新渲染图表
    window.addEventListener('resize', () => {
      Object.values(chartInstances).forEach(chart => {
        chart.resize();
      });
    });

    // 监听标签切换，重新渲染图表
    watch(activeTab, () => {
      nextTick(() => {
        renderCharts();
      });
    });

    // 格式化成交量
    const formatVolume = (val) => {
      if (!val) return '0';
      if (val >= 100000000) {
        return (val / 100000000).toFixed(2) + '亿';
      } else if (val >= 10000) {
        return (val / 10000).toFixed(2) + '万';
      }
      return val.toFixed(2);
    };

    // 格式化百分比
    const formatPercent = (val) => {
      if (val === undefined || val === null) return '0%';
      return val.toFixed(2) + '%';
    };

    // 获取指定股票在指定日期的斜率
    const getTargetDateSlope = (stockIndex) => {
      const stockData = getStockDataByIndex(stockIndex);
      if (!stockData || stockData.length === 0) return '暂无斜率数据';

      // 获取股票代码和查询日期
      const tsCode = stockData[0][0]; // 第一条记录的ts_code
      const queryDate = stockResponse.value.date; // 查询日期

      // 获取斜率和市场斜率
      if (stockSlopeData.value[tsCode]) {
        const data = stockSlopeData.value[tsCode];
        const slope = data.slope.toFixed(2);
        const marketSlope = data.market_slope.toFixed(2);
        const isOutperform = data.is_outperform;

        return `${queryDate} 斜率: ${slope}%, 大盘斜率: ${marketSlope}%, ${isOutperform ? '强于大盘' : '弱于大盘'}`;
      } else {
        // 如果没有缓存数据，加载斜率数据
        loadSlopeData(tsCode, queryDate);
        return '正在加载斜率数据...';
      }
    };

    // 加载斜率数据
    const loadSlopeData = async (tsCode, tradeDate) => {
      try {
        const params = new URLSearchParams();
        params.append('ts_code', tsCode);
        params.append('trade_date', tradeDate);

        const response = await fetch(`/api/slope?${params.toString()}`);
        if (!response.ok) {
          throw new Error('加载斜率数据失败');
        }

        const data = await response.json();
        // 缓存斜率数据
        stockSlopeData.value[tsCode] = data;
        // 触发UI更新
        nextTick();
      } catch (error) {
        console.error('加载斜率数据失败:', error);
        stockSlopeData.value[tsCode] = {
          slope: 0,
          market_slope: 0,
          is_outperform: false,
          error: error.message
        };
      }
    };

    return {
      searchForm,
      stockResponse,
      currentPage,
      activeTab,
      currentDataType,
      formatDate,
      formatDateColumn,
      loadAllData,
      loadLimitUpData,
      loadLimitDownData,
      loadHalfYearLineData,
      loadYearLineData,
      loadOutperformData,
      loadUnderperformData,
      loadFiveDayAdjustmentData,
      handlePageChange,
      getStockDataByIndex,
      getStockLabelByIndex,
      formatVolume,
      formatPercent,
      getTargetDateSlope
    };
  }
});

// 加载 Element Plus
app.use(ElementPlus);

// 挂载 Vue 应用
app.mount('#app'); 