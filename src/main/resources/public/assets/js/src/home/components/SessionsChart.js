var React = require('react');

var Chart = require('./helpers/Chart');

var SessionsChart = React.createClass({

  getDefaultProps: function() { 
    return {
      height: '350px',
      width: '100%',
      title: "",
      subtitle: "",
      mu: "",
      type: "line",
      xAxis: "category",
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
  render: function() {
    const colors = ['#2D3580', '#CD4D3E', '#564535'];
    const areaStyle = {
            color:  'rgba(232,232,237, 0.7)',
            type: 'default'
    };
    const seriesArray = this.props.data.map((x, i) => { 
      const data = this._checkData(this.props.xAxisData, x.data);
      return {
        name: x.title,
        type: this.props.type,
        showAllSymbol: true,
        symbolSize: 5,
        smooth: false,
        itemStyle: {
          normal: {
            color: colors[i]?colors[i]:colors[0],
            borderWidth: 2,
            barBorderColor: colors[i]?colors[i]:colors[0],
            barBorderWidth: 15,
            //barBorderRadius:10,
            lineStyle: {
              width: 1
            },
            label : {
                show: false, 
                position: 'insideTop',
                //fontFamily: "OpenSansCondensed",
                textStyle: '#666'
            },
            textStyle: {
              //fontFamily: "OpenSansCondensed",
              color: '#666'
            },
            areaStyle: areaStyle 
          },
          emphasis: {
            borderWidth: 1,
          }
        },
        data,
        //markLine : {
        //    data : [
        //        {type : 'average', name: 'Average'}
        //    ]
        //}
    };
    });
    const xAxis = [
      {
        show: true,
        type : this.props.xAxis,
        data: this.props.xAxisData ? this.props.xAxisData : [],
        //splitNumber: 12,
        scale:false,
        //scale: true,
        //min: this.props.xMin,
        //max: this.props.xMax,
        axisLabel : {
          formatter: this.props.xAxis === 'time' ? this.props.formatter : null,
          textStyle: {
            //fontFamily: "OpenSansCondensed",
            color: '#808285',
            fontSize: this.props.fontSize
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
      },
    ];

    const yAxis = [
      {
        show: true,
        type : 'value',
        axisLabel : {
          formatter: `{value}  ${this.props.mu}`,
          textStyle: {
            //fontFamily: "OpenSansCondensed",
            color: '#808285',
            fontSize: this.props.fontSize
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
          lineStyle: {
            color: ['#ccc'],
            width: 1,
            type: 'dotted solid double'
          }
        },
        boundaryGap: [0, 0.1]
      }
    ];
    return (
      <Chart
        style={{
          height: this.props.height,
          width: this.props.width,
        }} 
        options = {{
          title : {
            text: this.props.title,
            padding: this.props.title.length?[-2, 0, 50, 30]:5,
            textStyle: {
              //fontFamily: "OpenSansCondensed",
              color: '#808285'
            },
            //x: "center",
            subtext: this.props.subtitle
          },
          tooltip : {
            formatter: (params) => this.props.xAxis === 'time' ? `${new Date(params.value[0])}: ${params.value[1]}` : `${params.name}: ${params.value}`,
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
            //data:[this.props.title]
          },
          toolbox: {
            show : false,
          },
          backgroundColor: 'rgba(55,230,123,0.0)',
          color: ['#2D3580', '#A45476'],
          calculable : false,
          dataZoom: {
            show: false,
            y: 'bottom',  
            realtime: true,
            start: 0,
            end: 100,
            //backgroundColor: 'rgba(0,0,0,0)',
            //dataBackgroundColor: '#E8F5FD',
            //fillerColor: 'rgba(0,0,0,0.4)',
            handleColor: '#2D3580'
          },
          grid: {
            x: this.props.xMargin,
            y: this.props.yMargin,
            x2: this.props.x2Margin,
            y2: this.props.y2Margin
          },
          xAxis : this.props.invertAxis?yAxis:xAxis,
          yAxis : this.props.invertAxis?xAxis:yAxis,
          series : seriesArray
        }}  
      />
    );
  }
});

module.exports = SessionsChart;
