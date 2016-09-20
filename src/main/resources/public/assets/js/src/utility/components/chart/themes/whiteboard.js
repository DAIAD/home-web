module.exports = {

  color: [
    
    '#C23531', '#2F4554', '#61A0A8', '#ECA63F', '#41B024', 
    '#DD4BCF', '#30EC9F', '#ECE030', '#ED2868', '#34B4F1',

    '#2D6E8D', '#DB5563', '#9056B4', '#DD4BCF', '#30EC9F',
    '#C23531', '#2F4554', '#61A0A8', '#ECA63F', '#41B024',
  ],

  grid: {
    x: '12%', 
    y: '9%', 
    x2: '9%', 
    y2: '9%',
    borderColor: '#bbb',
  },
  
  legend: {
    padding: 10,
    itemHeight: 12,
    itemGap: 6,
    itemWidth: 35,
    backgroundColor: '#fff',
    borderColor: '#ccc',
    borderWidth: 0,
    textStyle: {
      fontSize: 11,
      fontFamily: 'monospace', // needed only for vertical alignment
    },
    x: 'center',
    y: 0,
  },
  
  tooltip: {
    trigger: 'item',
    borderRadius: 1,
    padding: 8,
    backgroundColor: 'rgba(0,0,0,0.75)',
    textStyle: {
      fontSize: 10,
      color: '#fff',
    },
  },

  categoryAxis: {
    boundaryGap: false,
    axisLine: {
      show: true,
      lineStyle: {
        color: '#555',
        width: 2,
        type: 'solid'
      }
    },
    axisTick: {
      show: true,
      interval: 'auto',
      inside: false,
      length: 5,
      lineStyle: {
        color: '#333',
        width: 1
      }
    },
    axisLabel: {
      show: true,
      interval: 'auto',
      rotate: 0,
      margin: 8,
      textStyle: {
        color: '#333'
      }
    },
    splitArea: {
      show: false,
      areaStyle: {
        color: ['rgba(255,255,255,0.3)', 'rgba(200,200,200,0.2)']
      },
    },
  },

  valueAxis: {
    boundaryGap: [0, 0],
    axisLine: {
      show: true,
      lineStyle: {
        color: '#555',
        width: 2,
        type: 'solid'
      }
    },
    axisTick: {
      show: true,
      interval: 'auto',
      inside: false,
      length: 5,
      lineStyle: {
        color: '#333',
        width: 1
      }
    },
    axisLabel: {
      show: true,
      interval: 'auto',
      rotate: 0,
      margin: 8,
      textStyle: {
        color: '#333'
      }
    },
    splitArea: {
      show: false,
      areaStyle: {
        color: ['rgba(255,255,255,0.3)', 'rgba(200,200,200,0.2)']
      },
    },
  },

  line: {
    itemStyle: {
      normal: {
        lineStyle: {
          width: 2,
          type: 'solid',
        },
      },
      emphasis: {
      }
    },
    smooth : false,
    symbol: 'emptyCircle',
    symbolSize: 4,
  },

  symbolList: [
    'circle', 'rectangle', 'triangle', 'diamond',
    'emptyCircle', 'emptyRectangle', 'emptyTriangle', 'emptyDiamond'
  ],

};
