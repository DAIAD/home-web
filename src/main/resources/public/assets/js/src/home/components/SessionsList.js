var React = require('react');
var Link = require('react-router').Link;
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { FormattedMessage, FormattedRelative } = require('react-intl');
var PureRenderMixin = require('react-addons-pure-render-mixin');

var Chart = require('./Chart');
var Shower = require('./Shower');
var SessionsChart = require('./SessionsChart');


function SessionItem (props) {
  const arrowClass = props.data.better===null?"":props.data.better?"fa-arrow-down green":"fa-arrow-up red";

  return (
    <li className="session-item"> 
      <a onClick={() => props.onOpen(props.index)} >
        <div className="session-item-header col-md-3"><h3>{props.data.volume}<span style={{fontSize: '0.6em'}}> lt</span> <i className={`fa ${arrowClass}`}/></h3>
        </div>
        <div className="col-md-7">
          <div className="pull-right">
            {(() => {
              if (props.data.id) {
                return <span className="session-item-detail">{props.data.id}</span>;
              }
            })()}
            <span className="session-item-detail">Stelios</span>
            <span className="session-item-detail"><i className="fa fa-calendar"/><FormattedRelative value={new Date(props.data.timestamp)} /></span> 
            {(() => { 
              if (props.data.duration) {
                return (
                  <span className="session-item-detail"><i className="fa fa-clock-o"/>{props.data.duration}</span>);
              }
            })()}
            {(() => { 
              if (props.data.energyClass) {
                return (
              <span className="session-item-detail"><i className="fa fa-flash"/>{props.data.energyClass}</span>);
              }
            })()}
            {(() => { 
              if (props.data.temperature) {
                return (
              <span className="session-item-detail"><i className="fa fa-temperature"/>{props.data.temperature}ºC</span>);
              }
            })()}
        </div>
        </div>
        <div className="col-md-2">
          <SparklineChart history={props.data.history} data={props.data.measurements} intl={props.intl}/>
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
  mixins: [PureRenderMixin],

  onOpen: function (index) {
    this.props.setActiveSessionIndex(index);
    this.props.getActiveSession(this.props.activeDevice, this.props.time);
  },
  onClose: function() {
    this.props.resetActiveSessionIndex();
    //set session filter to volume for sparkline
    this.props.setSessionFilter('volume');
  },
  onNext: function() {
    this.props.increaseActiveSessionIndex();
    this.props.getActiveSession(this.props.activeDevice, this.props.time);
  },
  onPrevious: function() {
    this.props.decreaseActiveSessionIndex();
    this.props.getActiveSession(this.props.activeDevice, this.props.time);
  },
  render: function() {
    return (
      <div style={{margin:50}}>
        <h3>In detail</h3>
        <ul className="sessions-list">
          {
            this.props.sessions.map((session, idx) => (
              <SessionItem
                intl={this.props.intl}
                key={idx}
                index={idx}
                data={session}
                onOpen={this.onOpen}
              />  
              ))
          }
        </ul>
        
        <bs.Modal animation={false} show={this.props.showModal} onHide={this.onClose} bsSize="large">
          <bs.Modal.Header closeButton>
            <bs.Modal.Title><FormattedMessage id="section.shower" /></bs.Modal.Title>
          </bs.Modal.Header>
          <bs.Modal.Body>
            <Shower 
              intl={this.props.intl}
              setSessionFilter={this.props.setSessionFilter}
              data={this.props.sessions[this.props.activeSessionIndex]}
              filter={this.props.activeSessionFilter}
              />
          </bs.Modal.Body>
          <bs.Modal.Footer>
            <bs.Button disabled={this.props.disabledPrevious} onClick={this.onPrevious}>Previous</bs.Button>
            <bs.Button disabled={this.props.disabledNext} onClick={this.onNext}>Next</bs.Button>
            <bs.Button onClick={this.onClose}>Close</bs.Button>
          </bs.Modal.Footer>
        </bs.Modal>
      </div>
    );
  }
});

module.exports = SessionsList;
