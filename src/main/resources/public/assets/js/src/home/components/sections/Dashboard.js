var React = require('react');
var bs = require('react-bootstrap');
var classNames = require('classnames');
var { FormattedMessage, FormatterRelative } = require('react-intl');
var { Responsive, WidthProvider } = require('react-grid-layout');
var ResponsiveGridLayout = WidthProvider(Responsive);

var MainSection = require('../MainSection');
var SessionsChart = require('../SessionsChart');

var Budget = require('../Budget');


/* Be Polite, greet user */
function SayHello (props) {
  return (
    <div >
      <h3><FormattedMessage id="dashboard.hello" values={{name:props.firstname}} /></h3>
    </div>
  );
}

function InfoBox (props) {
  
  const { title, highlight, compareText, extraText, improved } = props;
  const classLeft = props.classLeft || 'col-md-5';
  const classRight = props.classRight || 'col-md-7';
  const classContainer = props.classContainer || 'row';
  
  let improvedDiv = (<div/>);
  if (improved === true) {
    improvedDiv = (<img src="/assets/images/svg/success.svg"/>);
  }
  else if (improved === false) {
    improvedDiv = (<img src="/assets/images/svg/warning.svg"/>);
  }
  return (
    <div className='info-box'>
      <h3>{title}</h3>
      <div className={classContainer}>
        <div className={classLeft}>
          {highlight} 
        </div>
        <div className={classRight}>
          {improvedDiv}
          <span> {compareText}</span>
          <br/>
          <a onClick={props.onClick}>{extraText}</a>
        </div>
      </div>
    </div>
  );
}

function LastShowerChart (props) {
  if (!props.lastShower){
    return (<div/>);
  }
  else if (props.lastShower.history){
    return (<h4>Oops, can't graph due to limited data..</h4>);  
  }
  else {
    return (
      <SessionsChart
        height={150}
        width='100%'  
        title=""
        subtitle=""
        mu="lt"
        formatter={props.chartFormatter}
        yMargin={10}
        fontSize={12}
        type="line"
        data={props.chartData}
      />);
  }
}

function ForecastingChart (props) {
  return (
    <SessionsChart
      height={180}
      width={250} 
      title=""
      subtitle=""
      mu="lt"
      type="bar"
      xMargin={50}
      yMargin={10}
      xAxis="category"
      xAxisData={[2014, 2015, 2016]}
      data={[{title:'Consumption', data:[100, 200, 150]}]}
    />);
}

function BreakdownChart (props) {
  return (
    <SessionsChart
      height={250}
      width={400}
      mu="lt"
      type="bar"
      invertAxis={true}
      xMargin={80}
      yMargin={10}
      y2Margin={50}
      xAxis="category"
      xAxisData={["toilet", "faucet", "shower", "kitchen"]}
      data={[{title:'Consumption', data:[23, 25, 10, 20]}]}
    />);
}

var InfoPanel = React.createClass({
  onLayoutChange: function(layout) {
    //console.log('layout changed');
  },
  onBreakpointChange: function(breakpoint) {
  },
  onResizeStop: function(layout, oldItem, newItem, placeholder, e, element){
    if (newItem.i === "2") {
      //this.refs.lastShower.forceUpdate();
    }
  },
  render: function() {
    const layouts = [
      {x:0, y:0, w:6, h:1, i:"0"},
      {x:0, y:1, w:2, h:1, i:"1"}, 
      {x:2, y:1, w:2, h:2, i:"2"}, 
      {x:4, y:1, w:2, h:1, i:"3"},
      {x:0, y:2, w:2, h:1, i:"4"},
      {x:4, y:2, w:2, h:1, i:"7"},
      {x:3, y:3, w:3, h:2, i:"5"},
      {x:0, y:3, w:3, h:2, i:"6"},
    ];
    return (
      <div>
        <ResponsiveGridLayout className='layout' layouts={{lg:layouts}}
          breakpoints={{lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0}}
          onResizeStop={this.onResizeStop}
          cols={{lg: 12, md: 10, sm: 6, xs: 4, xxs: 2}} >
        <div key="1">
        <InfoBox
          title="Water efficiency Score"
          improved={true}
          highlight={(()=>(<h2>B+</h2>))()}
          compareText="Better since last month"
          extraText="Learn how to improve"
        />
        </div>
        <div key="2">
          
        {(() => {
          if (!this.props.lastShower) {
            return null;
          }
          else {
            return (
              <InfoBox 
                title="Last Shower"
                classContainer="padded"
                classLeft="row"
                classRight="row padded"
                improved={true}
                compareText={<span>You consumed a total of <b>{this.props.lastShower.volume + " lt"}</b> in <b>{this.props.lastShower.duration + " secs"}</b>.</span>}
                highlight={<LastShowerChart {...this.props} />}
                extraText=""
              />);
          }
        })()
        }
        </div>
        <div key="3">
        <InfoBox 
          title="Week consumption"
          improved={false}
          highlight={(()=>(<h2>450lt</h2>))()}
          compareText={<span><b>23%</b> more than last month</span>}
          extraText="See more"
        />
        </div>
                    
      <div key="4">
      <InfoBox 
        title="Year energy consumption"
        classLeft="col-md-7"
        classRight="col-md-5"
        improved={false}
        size="small"
        highlight={(()=>(<h2>850 kWh</h2>))()}
        compareText={<span><b>5%</b> more than last year</span>}
        extraText="See more"
        onClick={() => this.props.linkToHistoryTest({
          device: this.props.defaultDevice,
          filter: 'energy',
          timeFilter: 'year'
        })}
        />
      </div>

      <div key="7">
      <InfoBox 
          title="Day consumption"
          improved={true}
          highlight={(()=>(<h2>38lt</h2>))()}
          compareText={<span><b>5%</b> less than yesterday!</span>}
          extraText="See more"
        />
      </div>
        <div key="5">
        <InfoBox 
          title="Year forecasting"
          classLeft="col-md-8"
          classRight="col-md-4"
          improved={true}
          compareText="Based on your water use so far, we estimate you will use less water this year. Good job!"
          highlight={<ForecastingChart {...this.props} />}
          extraText="See more"
        />
      </div>
        
      <div key="6">
      <InfoBox 
        title="Water breakdown"
          classLeft="col-md-12"
          classRight="col-md-1"
          compareText=""
          highlight={<BreakdownChart {...this.props} />}
          extraText="See more"
        />
      </div>
      </ResponsiveGridLayout>
    </div>

    );
  }
});


function Dashboard (props) {
  return (
    <MainSection id="section.dashboard">
      <br/>
      <SayHello firstname={props.firstname} style={{margin:50}}/>
      <InfoPanel {...props} />
    </MainSection>
  );
}

module.exports = Dashboard;
