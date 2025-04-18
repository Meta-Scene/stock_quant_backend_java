// 创建 Vue 应用
const { createApp, ref, reactive, onMounted, nextTick, watch } = Vue;
const app = createApp({
  setup() {
    // 响应式数据
    const searchForm = reactive({
      ts_code: '',
      trade_date: ''
    });
    const stockResponse = ref(null);
    const currentPage = ref(1);
    const activeTab = ref(0); // 使用索引代替股票代码
    const currentDataType = ref('all'); // 'all', 'limitUp', 'limitDown', 'halfYearLine', 'yearLine'
    const chartInstances = reactive({});

    // 方法
    const formatDate = (dateStr) => {
      if (!dateStr) return '-';
      const date = new Date(dateStr);
      return date.toISOString().split('T')[0];
    };

    const formatDateColumn = (row, column, cellValue) => {
      return formatDate(cellValue);
    };

    // 获取指定股票数据
    const getStockDataByIndex = (index) => {
      if (!stockResponse.value || !stockResponse.value.grid_data ||
        index >= stockResponse.value.grid_data.length) {
        return [];
      }
      return stockResponse.value.grid_data[index];
    };

    // 获取指定股票代码和名称
    const getStockLabelByIndex = (index) => {
      const stockData = getStockDataByIndex(index);
      if (stockData.length === 0) return '';
      const tsCode = stockData[0][0]; // 第一条记录的第一个元素是股票代码
      const name = stockData[0][12]; // 获取股票名称
      return name ? `${name}(${tsCode})` : tsCode;
    };

    // 加载数据的方法
    const loadData = async (url) => {
      try {
        const params = new URLSearchParams();
        if (searchForm.ts_code) params.append('ts_code', searchForm.ts_code);
        if (searchForm.trade_date) params.append('trade_date', searchForm.trade_date);
        params.append('page', currentPage.value);

        const response = await fetch(`${url}?${params.toString()}`);
        if (!response.ok) {
          throw new Error('网络请求失败');
        }

        const data = await response.json();
        stockResponse.value = data;

        // 如果数据为空则显示提示
        if (!data.grid_data || data.grid_data.length === 0) {
          ElementPlus.ElMessage.warning('没有查询到符合条件的数据');
          return;
        }

        // 设置第一个标签为激活
        activeTab.value = 0;

        // 在下一个DOM更新周期渲染图表
        nextTick(() => {
          renderCharts();
        });
      } catch (error) {
        console.error('加载数据失败:', error);
        ElementPlus.ElMessage.error('加载数据失败: ' + error.message);
      }
    };

    const loadAllData = () => {
      currentDataType.value = 'all';
      currentPage.value = 1;
      loadData('/api/stock/data');
    };

    const loadLimitUpData = () => {
      currentDataType.value = 'limitUp';
      currentPage.value = 1;
      loadData('/api/stock/limit-up');
    };

    const loadLimitDownData = () => {
      currentDataType.value = 'limitDown';
      currentPage.value = 1;
      loadData('/api/stock/limit-down');
    };

    const loadHalfYearLineData = () => {
      currentDataType.value = 'halfYearLine';
      currentPage.value = 1;
      loadData('/api/stock/half-year-line');
    };

    const loadYearLineData = () => {
      currentDataType.value = 'yearLine';
      currentPage.value = 1;
      loadData('/api/stock/year-line');
    };

    const handlePageChange = (page) => {
      currentPage.value = page;

      // 根据当前数据类型加载对应的数据
      switch (currentDataType.value) {
        case 'all':
          loadData('/api/stock/data');
          break;
        case 'limitUp':
          loadData('/api/stock/limit-up');
          break;
        case 'limitDown':
          loadData('/api/stock/limit-down');
          break;
        case 'halfYearLine':
          loadData('/api/stock/half-year-line');
          break;
        case 'yearLine':
          loadData('/api/stock/year-line');
          break;
      }
    };

    // 转换数据为图表可用格式
    const prepareChartData = (stockIndex) => {
      const stockData = getStockDataByIndex(stockIndex);
      if (!stockData || stockData.length === 0) return { dates: [], prices: [], volumes: [], ma: [] };

      // 按日期排序
      const sortedData = [...stockData].sort((a, b) => new Date(a[1]) - new Date(b[1]));

      // 提取数据
      const dates = sortedData.map(item => item[1]); // trade_date
      const prices = sortedData.map(item => item[5]); // close
      const volumes = sortedData.map(item => item[8]); // vol

      // 提取均线数据（如果存在）
      let ma = [];
      if (currentDataType.value === 'halfYearLine') {
        ma = sortedData.map(item => item[9]); // ma120 在数组中的索引应为9
      } else if (currentDataType.value === 'yearLine') {
        ma = sortedData.map(item => item[10]); // ma250 在数组中的索引应为10
      }

      return { dates, prices, volumes, ma };
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
      const { dates, prices, volumes, ma } = prepareChartData(stockIndex);

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
            yAxisIndex: 0
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

      // 根据数据类型添加均线
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
      handlePageChange,
      getStockDataByIndex,
      getStockLabelByIndex,
      formatVolume,
      formatPercent
    };
  }
});

// 加载 Element Plus
app.use(ElementPlus);

// 挂载 Vue 应用
app.mount('#app'); 