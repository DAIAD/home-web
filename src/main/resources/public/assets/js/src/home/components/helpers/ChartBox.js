var React = require('react');
var Chart = require('./Chart');

function ChartBox (props) {
  const { title, type, subtype, improved, data, metric, measurements, period, device, deviceDetails, chartData, chartType, chartCategories, chartFormatter, chartColors, chartXAxis, highlight, time, index, mu, invertAxis } = props;
  return (
    <div>
      <div >
        {
          (() => chartData && chartData.length>0 ? 
           (type === 'budget' ? 
            <div>
              <div 
                style={{float: 'left', width: '50%'}}>
              <Chart
                height={70}
                width='100%'
                type='pie'
                title={chartData[0].title}
                subtitle=""
                fontSize={16}
                mu=''
                colors={chartColors}
                data={chartData}
              /> 
            </div>
            <div style={{width: '50%', float: 'right', textAlign: 'center'}}>
              <b>{chartData[0].data[0].value} lt</b> consumed<br/>
              <b>{chartData[0].data[1].value} lt</b> remaining
            </div>
          </div>:
              ((type === 'breakdown' || type === 'forecast' || type === 'comparison') ?
                <Chart
                  height={200}
                  width='100%'  
                  title=''
                  type='bar'
                  subtitle=""
                  xMargin={0}
                  y2Margin={0}  
                  yMargin={0}
                  x2Margin={0}
                  fontSize={12}
                  mu={mu}
                  invertAxis={invertAxis}
                  xAxis={chartXAxis}
                  xAxisData={chartCategories}
                  colors={chartColors}
                  data={chartData}
                /> :
             <Chart
                height={200}
                width='100%'  
                title=''
                subtitle=""
                type='line'
                yMargin={10}
                y2Margin={40}
                fontSize={12}
                mu={mu}
                formatter={chartFormatter}
                invertAxis={invertAxis}
                xAxis={chartXAxis}
                xAxisData={chartCategories}
                colors={chartColors}
                data={chartData}
              />))

            :
            <span>Oops, no data available...</span>
            )()
        }
        {
          /*
          (() => type === 'efficiency' ? 
            <span>Your shower efficiency class this {period} was <b>{highlight}</b>!</span>
           :
             <span>You consumed a total of <b>{highlight}</b>!</span>
             )()
             */
        }
      </div>
    </div>
  );
}
 
module.exports = ChartBox;
