var React = require('react');
var { FormattedMessage, FormattedDate } = require('react-intl');
var bs = require('react-bootstrap');
var { Link } = require('react-router');

var MainSection = require('../MainSection');
var Sidebar = require('../Sidebar');
var Topbar = require('../Topbar');

//sub-containers
var HistoryChartData = require('../../containers/HistoryChartData');
var HistoryListData = require('../../containers/HistoryListData');

//actions
var DeviceActions = require('../../actions/DeviceActions');

//utils
var { getDeviceByKey } = require('../../utils/device');
var timeUtil = require('../../utils/time');

function TimeNavigator(props) {
    return (
      <div className="time-navigator">
        <a className="pull-left" onClick={props.handleTimePrevious}>
          <img src="/assets/images/svg/arrow-big-left.svg" />
        </a>
        <div className="pull-left" style={{marginLeft:230, marginTop:10}}>
          <FormattedDate value={props.time.startDate} day="numeric" month="long" year="numeric" /> - <FormattedDate value={props.time.endDate} day="numeric" month="long" year="numeric" />
        </div>
        <a className="pull-right" onClick={props.handleTimeNext}>
          <img src="/assets/images/svg/arrow-big-right.svg" />
        </a>
      </div>
    );
}

var History = React.createClass({
  
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
      time.granularity = 4;
    }
    else if (key==="month"){
      time = timeUtil.thisMonth();
      time.granularity = 2;
    }
    else if (key==="week"){
      time = timeUtil.thisWeek();
      time.granularity = 0;
    }
    else if (key==="day"){
      time = timeUtil.today();
      time.granularity = 0;
    }
    else{
      throw new Error('oops, shouldn\'t be here');
    }
    this.props.setTimeFilter(key);
    this.props.setTimeAndQuery(time);
    //this.props.queryDeviceOrMeter(this.props.activeDevice, time); 
  },
  handleTimePrevious: function() { 
    this.props.setTimeAndQuery(this.props.previousPeriod);
  },
  handleTimeNext: function() { 
    this.props.setTimeAndQuery(this.props.nextPeriod);
  },
  handleDeviceChange: function(e, value) {
    this.props.setActiveAndQuery(value);
  },
  render: function() {
    const devices = this.props.devices?this.props.devices:[];
    const activeDevice = this.props.activeDevice;
    const device = getDeviceByKey(devices, activeDevice);
    const activeDeviceName = device?(device.name?device.name:device.serial):"None";
    const devType = this.props.devType;

    const _t = this.props.intl.formatMessage;
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
            if (this.props.devType === 'AMPHIRO') {
              return (
                <bs.Tabs style={{marginTop: 60}} position='left' tabWidth={20} activeKey={this.props.metricFilter} onSelect={this.handleTypeSelect}>
                  <bs.Tab eventKey="showers" title={_t({id: "history.showers"})}/>
                  <bs.Tab eventKey="duration" title={_t({id: "history.duration"})} />
                  <bs.Tab eventKey="volume" title={_t({id: "history.volume"})}/>
                  <bs.Tab eventKey="temperature" title={_t({id: "history.temperature"})}/>
                  <bs.Tab eventKey="energy" title={_t({id: "history.energy"})}/>
                </bs.Tabs>);
            }
            else if (this.props.devType === 'METER') {
              return (
                <bs.Tabs style={{marginTop: 60}} position='left' tabWidth={20} activeKey={this.props.metricFilter} onSelect={this.handleTypeSelect}>
                  <bs.Tab eventKey="volume" title={_t({id: "history.volume"})}/>
                </bs.Tabs>);
            }
          })()
          }
          </Sidebar>
          
          <div className="primary">
            <div className="pull-right">
              <span style={{marginRight: 10, marginTop: 5, fontFamily:'OpenSansCondensed', fontSize: '1.1em'}}>{devType}</span>
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
            </div>

            <bs.Tabs  position='top' tabWidth={3} activeKey={this.props.timeFilter} onSelect={this.handleTimeSelect}>
              
              <bs.Tab eventKey="day" title={_t({id: "history.day"})}/>
              <bs.Tab eventKey="week" title={_t({id: "history.week"})}/>
              <bs.Tab eventKey="month" title={_t({id: "history.month"})}/>
              <bs.Tab eventKey="year" title={_t({id: "history.year"})}/>
              {
               <bs.Tab eventKey="always" title={_t({id: "history.always"})} />
               }
            </bs.Tabs>
            
            <TimeNavigator 
              handleTimePrevious={this.handleTimePrevious} 
              handleTimeNext={this.handleTimeNext}
              time={this.props.time}
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
