var React = require('react');
var Link = require('react-router').Link;
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { FormattedMessage, FormattedRelative } = require('react-intl');

var SessionData = require('../containers/SessionData');

var Chart = require('./Chart');
var SessionsChart = require('./SessionsChart');


function SessionListItem (props) {
  const { id, index, device, devType, devName, volume, energyClass, timestamp, duration, better, temperature, history, measurements } = props.data;
  const arrowClass = better===null?"":better?"fa-arrow-down green":"fa-arrow-up red";
  return (
    <li className="session-item"> 
      <a onClick={() => props.onOpen(id, props.index, device)} >
        <div className="session-item-header col-md-3"><h3>{volume}<span style={{fontSize: '0.6em'}}> lt</span> <i className={`fa ${arrowClass}`}/></h3>
        </div>
        <div className="col-md-7">
          <div className="pull-right">
            {(() => {
              if (id) {
                return <span className="session-item-detail">{id}</span>;
              }
            })()}
            
            {(() => {
              if (devType) {
                return <span className="session-item-detail">{devType}</span>;
              }
            })()}
            
            {(() => {
              if (devName) {
                return <span className="session-item-detail">{devName}</span>;
              }
            })()}
            <span className="session-item-detail">Stelios</span>
            <span className="session-item-detail"><i className="fa fa-calendar"/><FormattedRelative value={new Date(timestamp)} /></span> 
            {(() => { 
              if (duration) {
                return (
                  <span className="session-item-detail"><i className="fa fa-clock-o"/>{duration}</span>);
              }
            })()}
            {(() => { 
              if (energyClass) {
                return (
              <span className="session-item-detail"><i className="fa fa-flash"/>{energyClass}</span>);
              }
            })()}
            {(() => { 
              if (temperature) {
                return (
              <span className="session-item-detail"><i className="fa fa-temperature"/>{temperature}ºC</span>);
              }
            })()}
        </div>
        </div>
        <div className="col-md-2">
          <SparklineChart history={history} data={measurements} intl={props.intl}/>
        </div>
      </a>
    </li>
  );
}

function SparklineChart (props) {
  if (!props.data || !props.data.length || props.data.length<=1 || props.history) {
    return (<h3>-</h3>);
  }
  return (
    <SessionsChart
      height={80}
      width='100%'  
      title=""
      subtitle=""
      mu=""
      formatter={(x) => props.intl.formatDate(x)}
      type="line"
      sparkline={true}
      xMargin={5}
      yMargin={0}
      x2Margin={2}
      y2Margin={0}
      data={[{title: 'Consumption', data:props.data}]}
    />);
}


var SessionsList = React.createClass({

  onOpen: function (id, index, device) {
    this.props.setActiveSessionIndex(index);
    
    if (id!==null && device!==null)
      this.props.getDeviceSession(id, device, this.props.time);  

  },
  /*
  onClose: function() {
    this.props.resetActiveSessionIndex();
    //set session filter to volume for sparkline
    this.props.setSessionFilter('volume');
  },
  onNext: function() {
    this.props.increaseActiveSessionIndex();
    //this.props.getActiveSession(this.props.activeDevice, this.props.time);
    //this.props.getDeviceSession(this.props.nextSessionId, this.props.nextDevice, this.props.time);  
  },
  onPrevious: function() {
    this.props.decreaseActiveSessionIndex();
    //this.props.getActiveSession(this.props.activeDevice, this.props.time);
    //this.props.getDeviceSession(this.props.previousSessionId, this.props.previousDevice, this.props.time);  
    },
    */
  render: function() {
    console.log('session item rendered', this.props.sessions);
    return (
      <div style={{margin:50}}>
        <h3>In detail</h3>
        <h4>{this.props.reducedMetric}</h4>
        <ul className="sessions-list">
          {
            this.props.sessions.map((session, idx) => (
              <SessionListItem
                intl={this.props.intl}
                key={idx}
                index={idx}
                data={session}
                onOpen={this.onOpen}
              />  
              ))
          }
        </ul>
        <SessionData sessions={this.props.sessions} />
        </div>
    );
  }
});

module.exports = SessionsList;
