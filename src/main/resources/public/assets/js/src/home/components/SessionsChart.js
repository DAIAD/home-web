var React = require('react');

var Chart = require('./Chart');

var SessionsChart = React.createClass({
  getDefaultProps: function() { 
    return {
      height: '350px',
      width: '100%',
      title: "",
      subtitle: "",
      mu: "",
      type: "line",
      xAxis: "time",
      xAxisData: [],
      invertAxis: false,
      sparkline: false,
      fontSize: 15,
      xMargin: 45,
      yMargin: 50,
      x2Margin: 20,
      y2Margin: 30
    };
  },
  componentWillReceiveProps: function(nextProps) {
  },
  render: function() {
    const colors = ['#2D3580', '#CD4D3E'];
    const areaStyle = this.props.sparkline?null:{
            color:  'rgba(232,232,237, 0.7)',
            type: 'default'
          };
    const seriesArray = this.props.data.map((x, i) => { 
      return {
        name: x.title,
        type: this.props.type,
        stack: this.props.type==='bar'?'name':null,
        showAllSymbols: false,
        symbolSize: this.props.sparkline?0:5,
        //symbolSize: this.props.sparkline?0:0,
        smooth: false,
        itemStyle: {
          normal: {
            color: colors[i],
            borderWidth: 2,
            barBorderColor: colors[i],
            barBorderWidth: 15,
            //barBorderRadius:10,
            lineStyle: {
              width: this.props.sparkline?2:1
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
        data: x.data,
        //markLine : {
        //    data : [
        //        {type : 'average', name: 'Average'}
        //    ]
        //}
    };
    });

    const xAxis = [
      {
        show: this.props.sparkline?false:true,
        type : this.props.xAxis,
        data: this.props.xAxisData,
        splitNumber: 0,
        scale: true,
        min: this.props.xMin,
        max: this.props.xMax,
        axisLabel : {
          formatter: this.props.formatter,
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
        boundaryGap: [0, 0.1]
      },
    ];

    const yAxis = [
      {
        show: this.props.sparkline?false:true,
        type : 'value',
        axisLabel : {
          formatter: `{value}  ${this.props.mu}`,
          textStyle: {
            //fontFamily: "OpenSansCondensed",
            color: '#808285',
            fontSize: this.props.fontSize
          },
          margin: 12
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
            //trigger: 'axis'
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
            //show: this.props.sparkline?false:true,
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
