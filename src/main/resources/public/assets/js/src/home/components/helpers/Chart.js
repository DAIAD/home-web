var React = require('react');
var ReactDOM = require('react-dom');
var PortalMixin = require('./PortalMixin');
var echarts = require('echarts');

var Chart = React.createClass({

  mixins: [PortalMixin],
  
  render: function() {
    return (
      <div 
        style={{
          width: this.props.width,
          height: this.props.height
        }}
        />
    );
  },
  
  getDefaultProps: function() { 
    return {
      height: '350px',
      width: '100%',
      title: "",
      subtitle: "",
      mu: "",
      xAxis: "category",
      colors: ['#2d3480', '#abaecc', '#7AD3AB', '#CD4D3E'],
      data: [],
      clickable: false,
      dataZoom: false,
      onPointClick: () => null,
      onDataZoom: () => null,
      dataZoomY: 'bottom',
      //xTicks: 12,
      xAxisData: null,
      invertAxis: false,
      fontSize: 15,
      xMargin: 75,
      yMargin: 10,
      x2Margin: 20,
      y2Margin: 50
    };
  },

  componentDidMount: function() {
    this._chart = echarts.init(document.getElementById(this.getId())); 
    //if(this.props.options) {
    const { setActiveSession, devices } = this.props;
    if (this.props.clickable) {
      this._chart.on('CLICK', (p => { 
        this.props.onPointClick(p.seriesIndex, p.dataIndex);
      }));
      
      this._chart.on('DATA_ZOOM', (p => { 
        console.log('DATA ZOOM!', p);
        //this.props.onPointClick(p.seriesIndex, p.dataIndex);
      }));

    }
    this._updateOptions(this.props);
      //}
  },
  
  componentWillReceiveProps : function(nextProps) {
    if(this._chart && nextProps) {
      this._updateOptions(nextProps);
    }
  },

  componentWillUnmount : function() {
    this._chart.dispose();
  },

  _updateOptions: function(options) {
    let newOpts = this._getBaseOptions(options);

    if (options.type === 'bar') 
      newOpts = Object.assign({}, newOpts, this._getBarOptions(options));
    else if (options.type === 'pie') 
      newOpts = Object.assign({}, newOpts, this._getPieOptions(options));

    this._chart.setOption(newOpts, true);

  },

  getChart: function() {
    return this._chart;
  },
  
  onResize: function() {
	  this._chart.resize();
	},
  
  // sanity check function from
  // https://github.com/DAIAD/react-echarts/blob/master/src/js/components/line.js#L304
  _checkData: function (xaxisData, data) {
    // Check if supplied (series) data is according to x-axis type
    if (xaxisData) {
      // Expect array of numbers paired to x-axis data (aAxis.type=category)
      data = data.map(v => ((v == '-' || v == null)? null : Number(v)));
      if (data.length != xaxisData.length || data.some(v => isNaN(v)) || data.every(v => v===null))
        data = null; // invalidate the entire array
    } else {
      // Expect array of [x, y] pairs (xAxis.type=value)
      data = data.filter(p => (Array.isArray(p) && p.length == 2))
        .map(p => ([Number(p[0]), Number(p[1])]));
    }
    return data;
  },
  _getSeriesArray: function(options, series, index) {
      const data = this._checkData(options.xAxisData, series.data);
      //if (!data) return {};
      //const data = series.data;
      if (options.type === 'pie') 
        return {
          name: series.title,
          type: options.type,
          radius: ['70%', '85%'],
          itemStyle: {
            normal: {
              //color: this.props.colors[0],
              //color: 'red',
              //borderWidth: 2,
              lineStyle: {
                width: 1
              },
              label : {
                  show: false, 
                  //formatter: (params) => `${this.props.title}`,
                  position: 'center',
                  textStyle: {
                    color: 'red',
                  },
                  //fontFamily: "OpenSansCondensed",
              },
              labelLine: {
                show: false
              },
            },
            

        },
        data: series.data.map((x, i) => Object.assign({}, x, {itemStyle: { normal: {color: this.props.colors[i]?this.props.colors[i]:this.props.colors[0]}}}))
        };
      else if (options.type === 'bar') 
        return {
          name: series.title,
          type: options.type,
          barGap: 0,
          barCategoryGap: 0,
          //barWidth: 50,
          //barMaxWidth: '10%',
          itemStyle: {
            normal: {
              color: this.props.colors[0],
              barBorderColor: this.props.colors[0],
              barBorderWidth: 0,
              //barBorderRadius:10,
              lineStyle: {
                width: 1
              },
              label : {
                show: true, 
                position: options.invertAxis? 'insideLeft' : 'insideBottom',
                //formatter: '{b}: {c}',
                formatter: '{b}',
                textStyle: {
                  color: '#fff',
                },
                //fontFamily: "OpenSansCondensed",
              },
              
              textStyle: {
                //fontFamily: "OpenSansCondensed",
                color: '#666'
              },
              areaStyle: {
                color:  'rgba(232,232,237, 0.7)',
                type: 'default'
              } 
            },
            
          },
          data: series.data.map((x, i) => Object.assign({}, {value:x}, {itemStyle: {normal: {color: this.props.colors[i]?this.props.colors[i]:this.props.colors[0]}}}) )
        };
      else
        return {
          name: series.title,
          type: options.type,
          showAllSymbol: true,
          symbolSize: 5,
          smooth: false,
          itemStyle: {
            normal: {
              color: options.colors[index]?options.colors[index]:options.colors[0],
              borderWidth: 2,
              lineStyle: {
                width: 1
              },
              label : {
                  show: true, 
                  //position: 'insideTop',
                  formatter: (params) => `${options.title}`,
                  position: 'center',
                  textStyle: {
                    fontColor: 'red',
                  },
                  //fontFamily: "OpenSansCondensed",
              },
              textStyle: {
                //fontFamily: "OpenSansCondensed",
                color: '#666'
              },
              areaStyle: {
                color:  'rgba(232,232,237, 0.7)',
                type: 'default'
              }
            },
            emphasis: {
              borderWidth: 1,
            }
          },
          data,
        };
  }, 
  _getXAxis: function(options) {   
    return {
      show: true,
      type : options.xAxis,
      data: options.xAxisData ? options.xAxisData : [],
      //splitNumber: 12,
      scale:false,
      //scale: true,
      //min: options.xMin,
      //max: options.xMax,
      axisLabel : {
        formatter: options.xAxis === 'time' ? options.formatter : null,
        textStyle: {
          //fontFamily: "OpenSansCondensed",
          color: '#808285',
          fontSize: options.fontSize
        },
        margin: 12
      },
      splitLine: {
        show: false
      },
      axisLine: {
        show: true
      },
      // axistTick: {
        //show: true
      //},
      //boundaryGap: [50,50]
      boundaryGap: true
    };
  },
  
  _getYAxis: function(options) {
    return {
      show: true,
      type : 'value',
      axisLabel : {
        formatter: `{value}  ${options.mu}`,
        textStyle: {
          //fontFamily: "OpenSansCondensed",
          color: '#808285',
          fontSize: options.fontSize
        },
        margin: 20
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        show: options.type === 'bar' ? false : true,
        lineStyle: {
          color: ['#ccc'],
          width: 1,
          type: 'dotted solid double'
        }
      },
      boundaryGap: [0, 0.1]
    };

  },
  _getPieOptions: function(options) {
    return {
      title : {
        text: this.props.title,
        padding: this.props.title.length?[-2, 0, 50, 30]:5,
        x: 'center',
        y: 'center',
        textStyle: {
          fontSize: this.props.fontSize,
          fontWeight: 'bold',
          //fontFamily: "OpenSansCondensed",
          color: '#666'
        },
        //x: "center",
        subtext: this.props.subtitle
      },
      xAxis: {
        show: false
      },
      yAxis: {
        show: false
      },
    };
  },
  _getBarOptions: function(options) {
    return {

    };
  },
  _getBaseOptions: function(options) {
    return {
      title : {
        text: options.title,
        padding: options.title.length?[-2, 0, 50, 30]:5,
        textStyle: {
          //fontFamily: "OpenSansCondensed",
          color: '#808285'
        },
        //x: "center",
        subtext: options.subtitle
      },
      tooltip : {
        formatter: (params) => options.xAxis === 'time' ? `${new Date(params.value[0])}: ${params.value[1]}` : `${params.name}: ${params.value}`,
        trigger: 'item',
        backgroundColor: '#2D3580',
        borderColor: '#2D3580',
        padding: 7,
        textStyle: {
          //fontFamily: "OpenSansCondensed",
          color: '#fff'
        },
      },
      legend: {
        //data:[options.title]
      },
      toolbox: {
        show : false,
      },
      //backgroundColor: '#fff',
      //backgroundColor: 'rgba(55,230,123,0.0)',
      color: ['#2D3580', '#A45476'],
      calculable : false,
      dataZoom: {
        show: this.props.dataZoom,
        y: this.props.dataZoomY,  
        realtime: true,
        start: 0,
        end: 100,
        //backgroundColor: '#fff',
        dataBackgroundColor: '#2D3580',
        fillerColor: 'rgba(0,0,0,0.1)',
        handleColor: '#2D3580'
      },
      grid: {
        x: options.xMargin,
        y: options.yMargin,
        x2: options.x2Margin,
        y2: options.y2Margin
      },
      xAxis : options.invertAxis?[this._getYAxis(options)]:[this._getXAxis(options)],
      yAxis : options.invertAxis?[this._getXAxis(options)]:[this._getYAxis(options)],
      series : options.data.map((x, i) => this._getSeriesArray(options, x, i))
    };
  },

});

module.exports = Chart;
