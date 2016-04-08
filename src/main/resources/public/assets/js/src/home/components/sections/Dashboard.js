var React = require('react');
var bs = require('react-bootstrap');
var classNames = require('classnames');

var intl = require('react-intl');
var { injectIntl, FormattedMessage, FormatterRelative } = require('react-intl');

var { Responsive, WidthProvider } = require('react-grid-layout');
var ResponsiveGridLayout = WidthProvider(Responsive);
var PureRenderMixin = require('react-addons-pure-render-mixin');

var MainSection = require('../MainSection');
var SessionsChart = require('../SessionsChart');


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
  const { mode, infobox, linkToHistory, removeInfobox, chartFormatter } = props;
  console.log('infobox here ', infobox);
  const editable = mode==='edit'?true:false;
  return (
    <div className='info-box'>
      {(()=>editable?(<a className="info-box-x" onClick={()=>removeInfobox(infobox.id)}><i className="fa fa-times"></i></a>):(<i/>))()}
      <h3>{infobox.title}</h3>
         <div>
           {
             (()=>{
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
             })()
           }
         </div>
    </div>
  );
}

function StatBox (props) {
  const { title, type, improved, data, reducedData, metric, measurements, period, device, deviceDetails, time, index } = props.infobox;

  console.log('statBOX', props.infobox);
  console.log('REDUCED', reducedData);
  let improvedDiv = (<div/>);
  if (improved === true) {
    improvedDiv = (<img src="/assets/images/svg/success.svg"/>);
  }
  else if (improved === false) {
    improvedDiv = (<img src="/assets/images/svg/warning.svg"/>);
  }
  const duration = data?(Array.isArray(data)?null:data.duration):null;
  return (
    <div className='row'>
      <div className='col-md-5'>
        <h2>{reducedData}</h2>
      </div>
      <div className='col-md-7'>
        <a onClick={() => props.linkToHistory({time, period, device:device.length?device[0]:device, metric, index})}>See more</a>
      </div>
    </div>
  );
}

function ChartBox (props) {
  const { title, type, improved, data, metric, measurements, period, device, deviceDetails, time, index } = props.infobox;
   console.log('chartbox', props);
  const metricReduced = data?data[metric]:0;
  const duration = data?data.duration:null;
  console.log('chartbox device', device.length?device[0]:[] );
  return (
    <div>
      <div >
        <ShowerChart {...props} />
        <span>You consumed a total of <b>{metricReduced} lt</b> in <b>{duration} sec</b>!</span>
      </div>
      <div>
        <a onClick={() => props.linkToHistory({time, period, device:device.length?device[0]:device, metric, index})}>See more</a>
      </div>
    </div>
  );
}


function ShowerChart (props) {
  const { chartFormatter, intl, history, infobox:{chartData}, metric } = props;
  console.log('shower chart data', chartData);
  if (history){
    return (<h4>Oops, cannot graph due to limited data..</h4>);  
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
        formatter={chartFormatter(intl)}
        data={chartData}
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
  const { mode, layout, infoboxData, updateLayout, linkToHistory, switchMode, removeInfobox, chartFormatter, intl } = props;
  const editable = mode==='edit'?true:false;
  return (
    <div>
      <ResponsiveGridLayout 
        className='layout'
        layouts={{lg:layout}}
        breakpoints={{lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0}}
        cols={{lg: 12, md: 10, sm: 6, xs: 4, xxs: 2}}
        isDraggable={editable}
        isResizable={editable}
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
               <InfoBox {...{mode, chartFormatter, infobox, linkToHistory, removeInfobox, intl}} /> 
           </div>
           );
         })
       }
      </ResponsiveGridLayout>
     </div>
  );
}

function InfoboxBuildForm (props) {
  const { mode, switchMode, tempInfoboxData, updateInfoboxTemp, types, subtypes, periods, metrics, devices, fetchInfoboxData, addInfobox, resetInfoboxTemp } = props;
  return (
    <bs.Modal animation={false} show={mode==="add"?true:false} onHide={() => switchMode("normal")} bsSize="sm">
      <bs.Modal.Header closeButton>
        <bs.Modal.Title>Add Infobox</bs.Modal.Title>
      </bs.Modal.Header>
      <bs.Modal.Body>
        <div style={{padding: '5px 20px'}}>
          <bs.Input 
            type="text" 
            label="Title" 
            disabled={true}
            value={tempInfoboxData.title} 
            onChange={(e) => 
              updateInfoboxTemp({title: e.target.value}) 
            }/>
          <bs.Input
            type="select"
            label="Device"
            title={tempInfoboxData.device?tempInfoboxData.device:"none"}
            id="device-switcher"
            defaultValue={tempInfoboxData.device}
            onChange={(e) => 
              updateInfoboxTemp({device: e.target.value})
            }>
            {
              devices.map(function(device) {
                return (
                  <option key={device.deviceKey} value={device.deviceKey} >{device.name || device.serial}</option>
                );
              })
            } 
          </bs.Input>
          
          <bs.Input
            type="select"
            label="Type"
            title={tempInfoboxData.type?tempInfoboxData.type:"none"}
            id="type-switcher"
            defaultValue={tempInfoboxData.type}
            onChange={(e) => 
              updateInfoboxTemp({type: e.target.value})
            }
            >
            {
              types.map(function(type) {
                return (
                  <option key={type.id} value={type.id} >{type.title}</option>
                );
              })
            } 
          </bs.Input>
          { 
            (()=> subtypes?(
              <bs.Input
                type="select"
                label="Sub-type"
                title={tempInfoboxData.subtype?tempInfoboxData.subtype:"none"}
                id="subtype-switcher"
                defaultValue={tempInfoboxData.subtype}
                onChange={(e) => 
                  updateInfoboxTemp({subtype: e.target.value})
                }
                >
                {
                  subtypes.map(function(type) {
                    return (
                      <option key={type.id} value={type.id} >{type.title}</option>
                    );
                  })
                } 
              </bs.Input>):
                (<div/>)
            )()
          }

          <bs.Input
            type="select"
            label="Period"
            title={tempInfoboxData.period?tempInfoboxData.period:"none"}
            id="period-switcher"
            defaultValue={tempInfoboxData.period}
            onChange={(e) => 
              updateInfoboxTemp({
                period: e.target.value,
                time: periods.find(x=> x.id === e.target.value)?periods.find(x=> x.id===e.target.value).value:{}
                }
              )
             }>
            {
              periods.map(function(period) {
                return (
                  <option key={period.id} value={period.id} >{period.title}</option>
                );
              })
            } 
          </bs.Input>

          <bs.Input
            type="select"
            label="Metric"
            title={tempInfoboxData.metric?tempInfoboxData.metric:"none"}
            id="metric-switcher"
            defaultValue={tempInfoboxData.metric}
            onChange={(e) => 
              updateInfoboxTemp({metric: e.target.value})
            }
             >
            {
              metrics.map(function(metric) {
                return (
                  <option key={metric.id} value={metric.id}>{metric.title}</option>
                );
              })
            } 
          </bs.Input>
          
        </div>
         
      </bs.Modal.Body>
      <bs.Modal.Footer>
        <bs.Button 
          onClick={()=> { 
            const { type, subtype, device, time } = tempInfoboxData;
            const id = addInfobox(tempInfoboxData); 
            fetchInfoboxData({id, type, subtype, device, time});
            resetInfoboxTemp(); 
            switchMode("edit"); 
          }}>
          OK
        </bs.Button>
        <bs.Button 
          onClick={()=> 
            switchMode("normal")
          }>
          Cancel
        </bs.Button>
      </bs.Modal.Footer>
    </bs.Modal>
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
  mixins: [PureRenderMixin],

  componentWillMount: function() {
    const { fetchAllInfoboxesData, switchMode } = this.props;
    switchMode("normal");
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
    console.log('rendered dashboaerd');
    console.log(this.props);
    const { firstname, mode, switchMode, amphiros, meters } = this.props;
    return (
      <MainSection id="section.dashboard">
        <br/>
        <SayHello firstname={firstname} />
        
        <ButtonToolbar {...{mode, switchMode}} />
        <br/>
        <InfoPanel {...this.props} />
        
        <InfoboxBuildForm {...this.props} /> 
      </MainSection>
    );
  }
});

//Dashboard = injectIntl(Dashboard);
module.exports = Dashboard;
