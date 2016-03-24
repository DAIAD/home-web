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

function InfoBox (props) {

  const { title, highlight, compareText, linkText, improved, data, metric } = props;
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
  console.log('reducing');
  console.log(title);
  console.log(data);
  console.log(metric);
  console.log(data.map(obj=>obj[metric]).reduce((prev, curr) => prev+curr, 0));
  return (
    <div className='info-box'>
      <h3>{title}</h3>
      <div className={classContainer}>
        <div className={classLeft}>
          <h2>{data.map(obj=>obj[metric]).reduce((prev, curr) => prev+curr, 0)}</h2>
        </div>
        <div className={classRight}>
          {improvedDiv}
          <span> {compareText}</span>
          <br/>
          <a onClick={props.onClick}>{linkText}</a>
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
    //formatter={props.chartFormatter(props.intl)}
    return (
      <SessionsChart
        height={150}
        width='100%'  
        title=""
        subtitle=""
        mu="lt"
        yMargin={10}
        fontSize={12}
        type="line"
        data={props.chartData?props.chartData:[[]]}
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
  const { layouts, infoboxData, updateLayout } = props;
  console.log('rendered info panel');
  return (
    <div>
      <ResponsiveGridLayout 
        className='layout' 
        layouts={{lg:layouts}}
        breakpoints={{lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0}}
        cols={{lg: 12, md: 10, sm: 6, xs: 4, xxs: 2}}
        onLayoutChange={(layout, layouts) => { 
          console.log('layout changed', layout, layouts); 
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
           console.log('each info has', data);
          return (
            <div key={data.id}>
              <InfoBox
                title={data.title}
                data={data.data}
                metric={data.metric}
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
    const { queryDevice, getLastSession, updateInfobox, infoboxData } = this.props;
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
  },
  
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
