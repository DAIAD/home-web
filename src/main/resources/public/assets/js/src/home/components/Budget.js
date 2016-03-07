var React = require('react');
var Chart = require('./Chart');

var Budget = React.createClass({
  render: function() {
    return (
      <Chart 
            style={{
              height:'350px',
              width: '100%'
            }} 
            options={{
              title: {
                  text: 'Budget',
                  subtext: 'february',
                  x: 'center',
                  y: 'center',
                  itemGap: 20,
                  textStyle : {
                      color : '#666',
                      //fontFamily : '',
                      fontSize : 25,
                      fontWeight : 'bold'
                  }
              },
              tooltip : {
                  show: true,
              },
              legend: {
                  orient : 'horizontal',
                  x : 45,
                  y : 45,
                  itemGap:12,
                  data:['Total','Last month','Today']
              },
              toolbox: {
                  show : true,
                  feature : {
                      mark : {show: true},
                      dataView : {show: true, readOnly: false},
                      restore : {show: true},
                      saveAsImage : {show: true}
                  }
              },
              series : [
                  {
                      name:'Total',
                      type:'pie',
                      radius : [125, 150],
                      //itemStyle : dataStyle,
                      data:[
                          {
                            value:120,
                            name:'Total'
                          }
                          
                      ]
                  },
                  {
                      name:'Last month',
                      type:'pie',
                      radius : [100, 125],
                      //itemStyle : dataStyle,
                      data:[
                          {
                              value:29, 
                              name:'Last month'
                          },
                          {
                              value:71,
                              name:'invisible',
                              //itemStyle : placeHolderStyle
                          }
                      ]
                  },
                  {
                      name:'Today',
                      type:'pie',
                      //clockWise:false,
                      radius : [75, 100],
                      //itemStyle : dataStyle,
                      data:[
                          {
                              value:3, 
                              name:'Today'
                          },
                          {
                              value:97,
                              name:'invisible',
                              //itemStyle : placeHolderStyle
                          }
                      ]
                  }
              ]
            }}
          />
    );
  }
});

module.exports = Budget;

