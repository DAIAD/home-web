var React = require('react');
var bs = require('react-bootstrap');
var classNames = require('classnames');

var intl = require('react-intl');
var { injectIntl, FormattedMessage, FormatterRelative } = require('react-intl');

var { Responsive, WidthProvider } = require('react-grid-layout');
var ResponsiveGridLayout = WidthProvider(Responsive);
var PureRenderMixin = require('react-addons-pure-render-mixin');

var MainSection = require('../layout/MainSection');
var SessionsChart = require('../SessionsChart');

const { IMAGES } = require('../../constants/HomeConstants');


var timeUtil = require('../../utils/time');

function ErrorDisplay (props) {
  return props.errors ? 
    <div style={{ zIndex: 100}}>
      <img src={`${IMAGES}/alert.svg`} /><span className="infobox-error">{`${props.errors}`}</span>
    </div>
    :
     (<div/>);
}


/* Be Polite, greet user */
function SayHello (props) {
  return (
    <div style={{margin: '40px 30px 20px 30px'}}>
      <h3><FormattedMessage id="dashboard.hello" values={{name:props.firstname}} /></h3>
    </div>
  );
}

function InfoBox (props) {
  const { mode, infobox, updateInfobox, removeInfobox, chartFormatter, intl } = props;
  const { id, error, period, type, display, linkToHistory, periods, displays } = infobox;
  
  const _t = intl.formatMessage;
  return (
    <div className='infobox'>
      <div className='infobox-header'>
        <div className='header-left'>
          <h4>{infobox.title}</h4>
        </div>

        <div className='header-right'>
          <div style={{marginRight:10}}>
            {
              displays.map(t => t.id!==display?(
                <a key={t.id} onClick={() => updateInfobox(id, {display:t.id})} style={{marginLeft:5}}>{t.title}</a>
                ):<span key={t}/>)
            }
          </div>
          
          <div>
            {
              periods.map(p => (
                <a key={p.id} onClick={() => updateInfobox(id, {period:p.id})} style={{marginLeft:5}}>{(p.id===period)?(<u>{_t({id: p.title})}</u>):(_t({id: p.title}))}</a>
                ))
            }
          </div>
          {
            //TODO: disable delete infobox until add is created
               <a className='infobox-x' style={{float: 'right', marginLeft: 5, marginRight:5}} onClick={()=>removeInfobox(infobox.id)}><i className="fa fa-times"></i></a>
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
               if (display==='stat') {
                 return (
                   <StatBox {...props} /> 
                 );
               } 
               else if (display==='chart') {
                 return (
                   <ChartBox {...props} /> 
                   );
               }
               else if (display==='tip') {
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
  const { id, title, type, improved, data, highlight, metric, measurements, period, device, deviceDetails, index, time, better, comparePercentage, mu } = props.infobox;
  let improvedDiv = <div/>;
  if (improved === true) {
    improvedDiv = (<img src={`${IMAGES}/success.svg`}/>);
  }
  else if (improved === false) {
    improvedDiv = (<img src={`${IMAGES}/warning.svg`}/>);
  }
  const duration = data?(Array.isArray(data)?null:data.duration):null;
  const arrowClass = better?"fa-arrow-down green":"fa-arrow-up red";
  const bow = (better==null || comparePercentage == null) ? false : true;
  return (
    <div>
      <div style={{float: 'left', width: '50%'}}>
        <h2>{highlight}<span style={{fontSize:'0.5em', marginLeft:5}}>{mu}</span></h2>
      </div>
      <div style={{float: 'left', width: '50%'}}>
        <div>
          {
            (() => bow ? 
             <span><i className={`fa ${arrowClass}`}/>{better ? `${comparePercentage}% better than last ${period} so far!` : `${comparePercentage}% worse than last ${period} so far`}</span>
             :
               <span>No data</span>
               )()
          }
        </div>
      </div>
    </div>
  );
}

function TipBox (props) {
  const { title, type, highlight } = props.infobox;
  return (
    <div >
      <p>{highlight}</p>
    </div>
  );
}

function ChartBox (props) {
  const { intl, history, infobox } = props;
  const { title, type, subtype, improved, data, metric, measurements, period, device, deviceDetails, chartData, chartFormatter, highlight, time, index, mu } = infobox;
  return (
    <div>
      <div >
        {
          (() => chartData.length>0 ? 
            <SessionsChart
              height={200}
              width='100%'  
              title=""
              subtitle=""
              mu={mu}
              yMargin={10}
              fontSize={12}
              type="line"
              formatter={chartFormatter(intl)}
              data={chartData}
            />
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
        breakpoints={{lg:1370, md: 900, sm: 600, xs: 480, xxs: 200}}
        cols={{lg:8, md: 6, sm: 4, xs: 2, xxs: 1}}
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
        <SayHello firstname={firstname} />
        
        <InfoPanel {...this.props} />
        
      </MainSection>
    );
  }
});

//Dashboard = injectIntl(Dashboard);
module.exports = Dashboard;
