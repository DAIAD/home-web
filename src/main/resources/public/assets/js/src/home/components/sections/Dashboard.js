var React = require('react');
var bs = require('react-bootstrap');
var classNames = require('classnames');
var intl = require('react-intl');
var { injectIntl } = require('react-intl');
var { FormattedMessage, FormatterRelative } = require('react-intl');
var { Responsive, WidthProvider } = require('react-grid-layout');
var ResponsiveGridLayout = WidthProvider(Responsive);
var MainSection = require('../MainSection');
var SessionsChart = require('../SessionsChart');
var PureRenderMixin = require('react-addons-pure-render-mixin');

var Budget = require('../Budget');

var timeUtil = require('../../utils/time');


/* Be Polite, greet user */
function SayHello (props) {
  return (
    <div >
      <h3><FormattedMessage id="dashboard.hello" values={{name:props.firstname}} /></h3>
    </div>
  );
}

function StatBox (props) {

  //const { title, highlight, compareText, linkText, improved, data, metric, period, device, time } = props;
  const { title, type, improved, data, metric, measurements, period, device, deviceDetails, time, index } = props.data;
  console.log('info box', props);
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
  const metricReduced = Array.isArray(data)?data.map(obj=>obj[metric]).reduce((prev, curr) => prev+curr, 0):data[metric];
  const duration = Array.isArray(data)?null:data.duration;
  return (
    <div className='info-box'>
      <h3>{title}</h3>
      <div className={classContainer}>
        <div className={classLeft}>
          { (() => {
            if (type === 'stat') {
              return <h2>{metricReduced}</h2>;
            }
            else if (type === 'last') {
              return (
                <div>
                  <LastShowerChart {...data} />
                  <span>You consumed a total of <b>{metricReduced} lt</b> in <b>{duration} sec</b>!</span>
                </div>
                );
            }
          })()
          }
        </div>
        <div className={classRight}>
          {improvedDiv}
          <br/>
          <a onClick={() => props.onClick({time, period, device, metric, index})}>See more</a>
          
        </div>
        <span className="pull-right">{deviceDetails.name || deviceDetails.serial}</span>
      </div>
    </div>
  );
}

function LastShowerChart (props) {
  console.log('last showrr!!');
  console.log(props);
  if (props.history){
    return (<h4>Oops, can't graph due to limited data..</h4>);  
  }
  else {
    //formatter={props.chartFormatter(props.intl)}
    return (
      <SessionsChart
        height={150}
        width={250}  
        title=""
        subtitle=""
        mu="lt"
        yMargin={10}
        fontSize={12}
        type="line"
        data={[{title: props.metric, data:props.chartData}]}
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


function InfoPanel (props) {
  const { layouts, infoboxData, updateLayout, linkToHistory } = props;
  console.log('info panel', infoboxData);
  return (
    <div>
      <ResponsiveGridLayout 
        className='layout' 
        layouts={{lg:layouts}}
        breakpoints={{lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0}}
        cols={{lg: 12, md: 10, sm: 6, xs: 4, xxs: 2}}
        onLayoutChange={(layout, layouts) => { 
          //updateLayout(layout);  
        }}
        onResizeStop={(layout, oldItem, newItem, placeholder) => { 
          updateLayout(layout);  
        }}
        onDragStop={(layout) => {
          updateLayout(layout); 
        }}
       >
       {
         infoboxData.map(function(data) {
           return (
             <div key={data.id}>
               <StatBox
                 data={data.type!=='last'?data:Object.assign({}, data, {index:0})}
                 onClick={linkToHistory}
               /> 
           </div>
           );
        })
       }
      </ResponsiveGridLayout>
     </div>
  );
}

var Dashboard = React.createClass({
  mixins: [PureRenderMixin],

  componentWillMount: function() {
    const { queryDevice, getLastSession, updateInfobox, updateAllInfoboxes, infoboxData } = this.props;
    updateAllInfoboxes();
    /*
    infoboxData.map(infobox => {
      
      if (infobox.type === "stat") {
        queryDevice(infobox.device, infobox.time)
        .then(sessions =>  updateInfobox(infobox.id, sessions));
      }
      //else if (infobox.type === "last") {
      //  getLastSession(infobox.device, infobox.time)
      //  .then(session => updateInfobox(infobox.id, session));
      //}
      });
      */
  },
  /*
  componentWillReceiveProps: function(nextProps) {
    console.log('history receiving props');   
    //console.log(nextProps);
    //console.log(this.props);
    for (let key in nextProps) {
      let prop = nextProps[key];
      if (typeof prop === 'function') { continue; }
      if (this.props[key] === prop) { continue; }
      console.log('new', key, prop);
      console.log('old', key, this.props[key]);
    }

   
  },
  */
  render: function() {
    console.log('rendered dashboard');
    console.log(this.props);
    return (
      <MainSection id="section.dashboard">
        <br/>
        <SayHello firstname={this.props.firstname} style={{margin:50}}/>
        <InfoPanel {...this.props} />
      </MainSection>
    );
  }
});

//Dashboard = injectIntl(Dashboard);
module.exports = Dashboard;
