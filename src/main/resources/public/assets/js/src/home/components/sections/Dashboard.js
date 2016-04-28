var React = require('react');
var bs = require('react-bootstrap');
var classNames = require('classnames');

var intl = require('react-intl');
var { injectIntl, FormattedMessage, FormatterRelative } = require('react-intl');

var { Responsive, WidthProvider } = require('react-grid-layout');
var ResponsiveGridLayout = WidthProvider(Responsive);
var PureRenderMixin = require('react-addons-pure-render-mixin');

const { IMAGES } = require('../../constants/HomeConstants');
var MainSection = require('../MainSection');
var SessionsChart = require('../SessionsChart');


var timeUtil = require('../../utils/time');

function ErrorDisplay (props) {
  return props.errors ? 
    <div style={{ zIndex: 100}}>
      <img src={`${IMAGES}/alert.svg`} /><span>{`${props.errors}`}</span>
    </div>
    :
     (<div/>);
}


/* Be Polite, greet user */
function SayHello (props) {
  return (
    <div style={{marginLeft:30}}>
      <h3><FormattedMessage id="dashboard.hello" values={{name:props.firstname}} /></h3>
    </div>
  );
}

function InfoBox (props) {
  const { mode, infobox, periods, updateInfobox, removeInfobox, chartFormatter } = props;
  const { id, error, period:defPeriod, type:defType, subtype:defSubtype, linkToHistory } = infobox;
  
  return (
    <div className='infobox'>
      <div className='infobox-header'>
      <span style={{float: 'left', fontSize: 20, marginLeft: 10, marginRight:10}}>{infobox.title}</span>
      
      <div style={{float: 'left'}}>
        {
          (() => (defSubtype !== 'last' && defType !== 'tip') ?
            ['chart', 'stat'].map(type => (
              <a key={type} onClick={() => updateInfobox(id, {type})} style={{marginLeft:5}}>{(type===defType)?(<u>{type}</u>):(type)}</a>
              ))
              :
                <div/>
                )()
        }
      </div>
      {
        //TODO: disable delete infobox until add is created
           <a className='infobox-x' style={{float: 'right', marginLeft: 5, marginRight:5}} onClick={()=>removeInfobox(infobox.id)}><i className="fa fa-times"></i></a>
      }
      <div style={{float: 'right', marginRight: 10, marginTop: 2}}>
        {
          (() => (defType !== 'tip' && defSubtype !== 'last') ?
            periods.map(period => (
              <a key={period.id} onClick={() => updateInfobox(id, {period:period.id})} style={{marginLeft:5}}>{(period.id===defPeriod)?(<u>{period.title}</u>):(period.title)}</a>
              ))
                :
                  <div/>
                  )()
        }
      </div>
    </div>
      
      <div className='infobox-body'>
         {
           (()=>{
             if (error) {
               return (<ErrorDisplay errors={error} />);
               } 
             else {
               if (infobox.type==='stat') {
                 return (
                   <StatBox {...props} /> 
                 );
               } 
               else if (infobox.type==='chart') {
                 return (
                   <ChartBox {...props} /> 
                   );
               }
               else if (infobox.type==='tip') {
                 return (
                   <TipBox {...props} />
                   );
               }
             }
           })()
         }
       </div>
       <div className='infobox-footer'>
          <a onClick={linkToHistory}>See more</a>
       </div>
    </div>
  );
}

function StatBox (props) {
  const { id, title, type, improved, data, reducedData, metric, measurements, period, device, deviceDetails, index, time, better } = props.infobox;
  let improvedDiv = <div/>;
  if (improved === true) {
    improvedDiv = (<img src={`${IMAGES}/success.svg`}/>);
  }
  else if (improved === false) {
    improvedDiv = (<img src={`${IMAGES}/warning.svg`}/>);
  }
  const duration = data?(Array.isArray(data)?null:data.duration):null;
  const arrowClass = better===null?"":better?"fa-arrow-down green":"fa-arrow-up red";
  return (
    <div className='row'>
      <div className='col-md-5'>
        <h2>{reducedData}</h2>
      </div>
      <div className='col-md-6'>
        <div><h5><i className={`fa ${arrowClass}`}/>{better?` better than last ${period}`:` worse than last ${period}`}</h5></div>
      </div>
    </div>
  );
}

function TipBox (props) {
  const { title, type, tip } = props.infobox;
  return (
    <div >
      <div style={{fontSize: 14}}>
        {tip}
      </div>
    </div>
  );
}
function ChartBox (props) {
  const { title, type, subtype, improved, data, metric, measurements, period, device, deviceDetails, chartData, reducedData, time, index } = props.infobox;
  return (
    <div>
      <div >
        {
          (() => chartData.length>0 ? 
            <ShowerChart {...props} />
            :
            <h5>Oops, no data available...</h5>
            )()
        }
        {
          (() => subtype === 'efficiency' ? 
           ( reducedData ?
            <span>Your shower efficiency class this {period} was <b>{reducedData}</b>!</span>
            :
              <span>No shower data for this {period}</span>
              )
           :
             <span>You consumed a total of <b>{reducedData}</b>!</span>
             )()
        }
      </div>
    </div>
  );
}


function ShowerChart (props) {
  const { chartFormatter, intl, history, infobox:{chartData}, metric } = props;
  if (history){
    return (<h4>Oops, cannot graph due to limited data..</h4>);  
  }
  else {
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
        formatter={chartFormatter(intl)}
        data={chartData}
      />);
  }
}
/*
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
*/

function InfoPanel (props) {
  const { mode, layout, infoboxData, updateLayout, switchMode,  updateInfobox, removeInfobox, chartFormatter, intl, periods } = props;

  return (
    <div>
      <ResponsiveGridLayout 
        className='layout'
        layouts={{lg:layout}}
        breakpoints={{xlg: 1400, lg: 1000, md: 700, sm: 600, xs: 480, xxs: 200}}
        cols={{xlg:8, lg: 6, md: 6, sm: 4, xs: 2, xxs: 1}}
        draggableHandle='.infobox-header'
        isDraggable={true}
        isResizable={false}
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
         infoboxData.map(function(infobox) {
           return (
             <div key={infobox.id}>
               <InfoBox {...{mode, periods, chartFormatter, infobox, updateInfobox, removeInfobox, intl}} /> 
           </div>
           );
         })
       }
      </ResponsiveGridLayout>
     </div>
  );
}

function ButtonToolbar (props) {
  const { switchMode, mode } = props;
  return (
    <div className="pull-right">
      <bs.ButtonToolbar>
        <bs.Button onClick={()=> switchMode("add")} active={false}>Add</bs.Button>
        {
          (()=> mode==="edit"?(
            <bs.Button onClick={()=> switchMode("normal")} bsStyle="primary" active={false}>Done</bs.Button>
            ):(
            <bs.Button onClick={()=> switchMode("edit")} active={false}>Edit</bs.Button>
            ))()
        }
      </bs.ButtonToolbar>
    </div>
  );
}

var Dashboard = React.createClass({
  mixins: [ PureRenderMixin ],

  componentWillMount: function() {
    const { fetchAllInfoboxesData, switchMode } = this.props;
    //switchMode("normal");
    fetchAllInfoboxesData();

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
    const { firstname, mode, switchMode, amphiros, meters } = this.props;
    return (
      <MainSection id="section.dashboard">
        <br/>
        <SayHello firstname={firstname} />
        
        <InfoPanel {...this.props} />
        
      </MainSection>
    );
  }
});

//Dashboard = injectIntl(Dashboard);
module.exports = Dashboard;
