var React = require('react');
var { FormattedMessage, FormattedDate } = require('react-intl');
var bs = require('react-bootstrap');
var Select = require('react-select');

var { Link } = require('react-router');

var MainSection = require('../MainSection');
var Topbar = require('../Topbar');
var { SidebarLeft, SidebarRight } = require('../Sidebars');

var HistoryList = require('../SessionsList');
var CheckboxGroup = require('react-checkbox-group');

//sub-containers
var HistoryChartData = require('../../containers/HistoryChartData');

//utils
var timeUtil = require('../../utils/time');

const { IMAGES } = require('../../constants/HomeConstants');

function TimeNavigator(props) {
    const { time, handleTimePrevious, handleTimeNext } = props;
    return time.startDate ? 
      (
      <div className="time-navigator">
        <a className="time-navigator-child pull-left" onClick={handleTimePrevious}>
          <img src={`${IMAGES}/arrow-big-left.svg`} />
        </a>
        <div className="time-navigator-child">
          <FormattedDate value={time.startDate} day="numeric" month="long" year="numeric" /> - <FormattedDate value={time.endDate} day="numeric" month="long" year="numeric" />
        </div>
        <a className="time-navigator-child pull-right" onClick={handleTimeNext}>
          <img src={`${IMAGES}/arrow-big-right.svg`} />
        </a>
      </div>
      )
        :
          (<div/>);
}

function ErrorDisplay (props) {
  return props.errors ? 
    <div style={{position: 'absolute', marginLeft: '20vw', marginTop: '25vh', zIndex: 100}}>
      <img src={`${IMAGES}/alert.svg`} /><span>{`${props.errors}`}</span>
    </div>
    :
     (<div/>);
}
var History = React.createClass({

  componentWillMount: function() {
    const { synced, setActiveDeviceType, activeDeviceType, timeFilter } = this.props;
    if (!synced) {
        //set active device and dont query cause we havent set time yet!
        setActiveDeviceType(activeDeviceType, false);
        this.handleTimeSelect(timeFilter);
      
      }
  },
  handleTypeSelect: function(key){
    this.props.setQueryFilter(key); 
  },
  
  handleTimeSelect: function(key){
    let time = {};
    if (key==="always"){
      time = {
        startDate: new Date("2000-02-18").getTime(),
        endDate: new Date("2016-12-31").getTime(),
        granularity: 0
      };
    }
    else if (key==="year"){
      time = timeUtil.thisYear();
    }
    else if (key==="month"){
      time = timeUtil.thisMonth();
    }
    else if (key==="week"){
      time = timeUtil.thisWeek();
    }
    else if (key==="day"){
      time = timeUtil.today();
    }
    else{
      throw new Error('oops, shouldn\'t be here');
    }
    this.props.setTimeFilter(key);
    this.props.setTime(time);
  },
  handleTimePrevious: function() { 
    this.props.setTime(this.props.previousPeriod);
  },
  handleTimeNext: function() { 
    this.props.setTime(this.props.nextPeriod);
  },
  handleDeviceChange: function(val) {
    const mapped = val.map(d=>d.value); 
    this.props.setActiveDevice(mapped);
  },
  handleDeviceTypeSelect: function(val) {
    this.props.setActiveDeviceType(val);
  },
  handleActiveDevicesChanged: function (vals) {
    console.log('checkbox vals changed to', vals);
    if (this.props.activeDeviceType === 'METER') 
      this.props.setActiveDeviceType('AMPHIRO', false);
    this.props.setActiveDevice(vals);
  },
  handleComparisonSelect: function(val) {
   this.props.setComparison(val); 
    //onChange={(e) => e.target.checked?this.props.setComparison(comparison.id):this.props.setComparison(null)}
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
    const { intl, devices, amphiros, activeDevice, activeDeviceType, device, devType, timeFilter, time, metrics } = this.props;
    const _t = intl.formatMessage;
    return (
        <MainSection id="section.history">
           
           <Topbar> 
            <bs.Tabs className="history-time-nav" position='top' tabWidth={3} activeKey={timeFilter} onSelect={this.handleTimeSelect}>
              
              <bs.Tab eventKey="day" title={_t({id: "history.day"})}/>
              <bs.Tab eventKey="week" title={_t({id: "history.week"})}/>
              <bs.Tab eventKey="month" title={_t({id: "history.month"})}/>
              <bs.Tab eventKey="year" title={_t({id: "history.year"})}/>
              {
                   <bs.Tab eventKey="always" title={_t({id: "history.always"})} />
               }
            </bs.Tabs>
          </Topbar>
          
         <div className="section-row-container">
          <SidebarLeft> 
            <bs.Tabs position='left' tabWidth={20} activeKey={this.props.metricFilter} onSelect={this.handleTypeSelect}>
              {
                metrics.map(metric =>
                            <bs.Tab key={metric.id} eventKey={metric.id} title={metric.title}/> 
                           )
              }
            </bs.Tabs>
          </SidebarLeft>
          
          <SidebarRight> 
            {
            <bs.Tabs position='left' tabWidth={20} activeKey={this.props.activeDeviceType} onSelect={this.handleDeviceTypeSelect}>
              {
                [{id:'METER', title: 'Water meter', image: 'swm.svg'}, {id:'AMPHIRO', title:'Shower devices', image: 'amphiro_small.svg'}].map((devType, i) => ( 
                                                                                                                                                               <bs.Tab key={devType.id} eventKey={devType.id} title={devType.title} /> 
                           ))
              }
            </bs.Tabs>
            }
            <CheckboxGroup name="amphiro-devices" className="amphiro-devices" value={activeDeviceType==='AMPHIRO'?activeDevice:[]} onChange={this.handleActiveDevicesChanged}>
              {
                Checkbox => (
                  <div>
                    {
                      amphiros.map((device, i) => 
                      <label key={device.deviceKey}>
                        <Checkbox value={device.deviceKey} /> {device.name || device.macAddress || device.serial}
                      </label>
                      )
                    }
                  </div>
                  )
              }
            </CheckboxGroup>
   
            <br/>
            <h5 style={{marginLeft:20}}>Compare with</h5>
            {
              <bs.Tabs position='left' tabWidth={20} activeKey={this.props.comparison} onSelect={this.handleComparisonSelect}>
                {
                  [{id:'last', title: `Your last ${timeFilter}`}].map((comparison, i) => (
                    <bs.Tab key={comparison.id} eventKey={comparison.id} title={comparison.title} />
                             ))
                }
              </bs.Tabs>
              }
              {
                (() => this.props.comparison ? 
                 <a style={{marginLeft: 20, marginTop:20}} onClick={()=>this.props.setComparison(null)}>Clear</a>
                 :
                   <div/>
                   )()
              }

        </SidebarRight>

        <div className="primary"> 

          <ErrorDisplay errors={this.props.errors} />
          <div className="history-chart-area">
            <h4 style={{textAlign: 'center', margin: '10px 0 0 0'}}>{this.props.reducedMetric}</h4>
            <TimeNavigator 
              handleTimePrevious={this.handleTimePrevious} 
              handleTimeNext={this.handleTimeNext}
              time={time}
            /> 

          <div className="history-chart">
            <HistoryChartData />
          </div>
        
        </div>

        <HistoryList {...this.props} />

        </div>
      
      </div>
    </MainSection>
    );
  }
});

module.exports = History;
