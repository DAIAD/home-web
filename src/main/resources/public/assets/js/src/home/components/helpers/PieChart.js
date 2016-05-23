var React = require('react');

var Chart = require('./Chart');

var PieChart = React.createClass({

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
      //const data = this._checkData(this.props.xAxisData, x.data);
      const data = x.data;
      return {
        name: x.title,
        type: 'pie',
        showAllSymbol: true,
        symbolSize: 5,
        smooth: false,
        radius: ['50%', '70%'],
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
                //formatter: (params) => `${this.props.title}`,
                position: 'center',
                textStyle: {
                  color: 'red',
                },
                //fontFamily: "OpenSansCondensed",
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
        show: false,
      },
    ];

    const yAxis = [
      {
        show: false,
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
            position: 'center',
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

          xAxis,
          yAxis,
          series : seriesArray
        }}  
      />
    );
  }
});

module.exports = PieChart;
