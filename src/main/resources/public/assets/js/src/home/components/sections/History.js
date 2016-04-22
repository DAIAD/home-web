var React = require('react');
var { FormattedMessage, FormattedDate } = require('react-intl');
var bs = require('react-bootstrap');
var Select = require('react-select');

var { Link } = require('react-router');

var MainSection = require('../MainSection');
var Sidebar = require('../Sidebar');
var Topbar = require('../Topbar');

const { IMAGES } = require('../../constants/HomeConstants');
//sub-containers
var HistoryChartData = require('../../containers/HistoryChartData');
var HistoryListData = require('../../containers/HistoryListData');

//utils
var timeUtil = require('../../utils/time');


function TimeNavigator(props) {
    return (
      <div className="time-navigator">
        <a className="pull-left" onClick={props.handleTimePrevious}>
          <img src={`${IMAGES}/arrow-big-left.svg`} />
        </a>
        <div className="pull-left" style={{marginLeft:230, marginTop:10}}>
          <FormattedDate value={props.time.startDate} day="numeric" month="long" year="numeric" /> - <FormattedDate value={props.time.endDate} day="numeric" month="long" year="numeric" />
        </div>
        <a className="pull-right" onClick={props.handleTimeNext}>
          <img src={`${IMAGES}/arrow-big-right.svg`} />
        </a>
      </div>
    );
}

var History = React.createClass({

  componentWillMount: function() {
    const { activeSessionIndex, time, activeDevice, defaultDevice, setActiveDevice, getDeviceOrMeterSessions, getActiveSession } = this.props;
    const device = activeDevice?activeDevice:[defaultDevice];
    if (!activeDevice) {
      setActiveDevice([defaultDevice]);
    }
    if (device) 
      getDeviceOrMeterSessions(device, time).then(() => { if (activeSessionIndex!==null) { return getActiveSession(device, time); } });
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
    this.props.setTimeAndQuery(this.props.activeDevice, time);
  },
  handleTimePrevious: function() { 
    this.props.setTimeAndQuery(this.props.activeDevice, this.props.previousPeriod);
  },
  handleTimeNext: function() { 
    this.props.setTimeAndQuery(this.props.activeDevice, this.props.nextPeriod);
  },
  handleDeviceChange: function(val) {
    const mapped = val.map(d=>d.value); 
    this.props.setActiveDeviceAndQuery(mapped, this.props.time);
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
    const { intl, devices, activeDevice, device, devType, timeFilter, time } = this.props;
    const activeDeviceName = activeDevice?(activeDevice.name?activeDevice.name:activeDevice.serial):"None";
    const activeDeviceKey = activeDevice?activeDevice.deviceKey:"none";
    const _t = intl.formatMessage;
    return (
      <div>
        <Topbar> 
          <ul className="list-unstyled">
            <li><Link to="/history">Explore</Link></li>
            <li><Link to="/history">Compare</Link></li>
            <li><Link to="/history">Forecast</Link></li>
          </ul>
        </Topbar>
      <MainSection id="section.history">
        <div>
          <Sidebar> 
          {(() => {
            if (devType === 'AMPHIRO') {
              return (
                <bs.Tabs style={{marginTop: 60}} position='left' tabWidth={20} activeKey={this.props.metricFilter} onSelect={this.handleTypeSelect}>
                  {
                    this.props.metrics.map(metric =>
                                           <bs.Tab key={metric.id} eventKey={metric.id} title={metric.title}/>
                                           )
                  }
                  {
                  /*
                  <bs.Tab eventKey="showers" title={_t({id: "history.showers"})}/>
                  <bs.Tab eventKey="duration" title={_t({id: "history.duration"})} />
                  <bs.Tab eventKey="volume" title={_t({id: "history.volume"})}/>
                  <bs.Tab eventKey="temperature" title={_t({id: "history.temperature"})}/>
                  <bs.Tab eventKey="energy" title={_t({id: "history.energy"})}/>
                  */
                  }
                </bs.Tabs>);
            }
            else if (devType === 'METER') {
              return (
                <bs.Tabs style={{marginTop: 60}} position='left' tabWidth={20} activeKey={this.props.metricFilter} onSelect={this.handleTypeSelect}>
                  <bs.Tab eventKey="volume" title={_t({id: "history.volume"})}/>
                </bs.Tabs>);
            }
          })()
          }
          </Sidebar>
          
          <div className="primary">
            <div >
              {
                /*
              <bs.DropdownButton
                title={activeDeviceName}
                id="device-switcher"
                defaultValue={activeDevice}
                onSelect={this.handleDeviceChange}>
                {
                  devices.map(function(device) {
                    return (
                      <bs.MenuItem key={device.deviceKey} eventKey={device.deviceKey} value={device.deviceKey} >{device.name || device.serial}</bs.MenuItem>
                    );
                  })
                } 
              </bs.DropdownButton>
              <Select 
                name="device-switcher"
                value={activeDevice}
                onChange={this.handleDeviceChange}
                multi={true}
                autosize={true}
                style={{minWidth:120}}
                options={devices.map(device => {
                  return { 
                    value: device.deviceKey,
                    label: device.name || device.serial || device.macAddress 
                  };
                }) }
              />
              */
              }
              <div
                style={{
                  float: 'right',
                  width: '15%'
                }}
                >
              {
                devices.map((device, i) => {
                  console.log('device', device, i);
                  return (
                    <div key={i}>
                    <input
                      id={device.deviceKey}
                      style={{marginRight: 7, marginLeft: 5}}
                      type = "checkbox"
                      checked={activeDevice.includes(device.deviceKey)}
                      onChange={(e) => e.target.checked?this.props.addToActiveDevices(device.deviceKey, time):this.props.removeFromActiveDevices(device.deviceKey, time)}
                      />
                      
                    <label >{device.name || device.macAddress || device.serial}</label>
                  </div>
                  ); 
                }) 
              }
            </div>
          
          </div>
          <br/>

            <bs.Tabs  position='top' tabWidth={3} activeKey={timeFilter} onSelect={this.handleTimeSelect}>
              
              <bs.Tab eventKey="day" title={_t({id: "history.day"})}/>
              <bs.Tab eventKey="week" title={_t({id: "history.week"})}/>
              <bs.Tab eventKey="month" title={_t({id: "history.month"})}/>
              <bs.Tab eventKey="year" title={_t({id: "history.year"})}/>
              {
               <bs.Tab eventKey="always" title={_t({id: "history.always"})} />
               }
            </bs.Tabs>
            <br/>
            
            <TimeNavigator 
              handleTimePrevious={this.handleTimePrevious} 
              handleTimeNext={this.handleTimeNext}
              time={time}
            /> 
            
            <HistoryChartData />

            <HistoryListData />

          </div>
        </div>

      </MainSection>
    </div>
    );
  }
});

module.exports = History;
