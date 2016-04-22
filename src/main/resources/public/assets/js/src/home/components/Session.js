var React = require('react');

var bs = require('react-bootstrap');
//var link = require('react-router').link;
var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedTime = require('react-intl').FormattedTime;
var FormattedRelative = require('react-intl').FormattedRelative;


var SessionsChart = require('./SessionsChart');

var { SHOWER_METRICS } = require('../constants/HomeConstants'); 
var MainSection = require('./MainSection');
var Sidebar = require('./Sidebar');
var timeUtil = require('../utils/time');


function SessionInfoItem (props) {
  const _t = props.intl.formatMessage;
  return !props.data?<div />:(
    <li className="session-item" >
      {(()=> props.sessionClick?(
        <a onClick={() => props.sessionClick(props.id)} title={_t({id: props.details})}>
        <h4 style={{float: 'left'}}><img style={{width:props.id==='temperature'?12:24, marginRight:20}} src={`/assets/images/svg/${props.icon}.svg`} /><FormattedMessage id={props.title} /></h4>
      <h4 style={{float:'right'}}>{props.data} <span>{props.mu}</span></h4>
    </a>
    ):(
    <span>
        <h4 style={{float: 'left'}}><img style={{width:props.id==='temperature'?12:24, marginRight:20}} src={`/assets/images/svg/${props.icon}.svg`} /><FormattedMessage id={props.title} /></h4>
      <h4 style={{float:'right'}}>{props.data} <span>{props.mu}</span></h4>
    </span>
    ))()
      }
    </li>
  );
}

function SessionInfo (props) {
  const { setSessionFilter, intl, data } = props;
  console.log('session info data is', data);
  return !data?<div />:(
    <div style={{width: '80%', marginLeft:'auto', marginRight:'auto'}}>
      <h4><FormattedMessage id="shower.details"/></h4>
      <hr/>
      <h5 style={{float: 'right'}} ><i style={{marginRight: 10}} className="fa fa-user"/>Stelios</h5>
      <h5 style={{float: 'left'}}><i style={{marginRight: 10}} className="fa fa-calendar"/><FormattedTime value={new Date(data.timestamp)} date={{day:"numeric", month:"long", year:"numeric"}} time={{hours:"numeric", minutes:"numeric"}} /></h5>
      <br/>
      <br/>
      <ul className="sessions-list" >
        {
          SHOWER_METRICS.map(function(metric) {
            return (<SessionInfoItem key={metric.id} intl={intl} icon={metric.icon || metric.id} sessionClick={metric.clickable?setSessionFilter:null} title={metric.title} id={metric.id} data={data[metric.id]} mu={metric.mu} details={metric.details} />);
          })
        }
      </ul>
  </div>
  );
}

function Session (props) {
  const { history, intl, filter, data, setSessionFilter } = props;
  if (!data) return <div/>;
  const { hasChartData, chartData } = data;
  console.log('rendered showerinfo', props);
  console.log('has chart data?', hasChartData, data);
  
  const _t = intl.formatMessage;
  return hasChartData?(
        <div style={{padding: 30}}>
          <SessionsChart 
              height={300}
              width='100%'
              title={_t({id: `history.${filter}`})}
              mu="lt"
              xMargin={60}
              x2Margin={60}
              type="line"
              formatter={(x) => intl.formatTime(x, { hour: 'numeric', minute: 'numeric'})}
              data={[{title:filter, data:chartData}]}
            />
          <div style={{marginTop: 30}}/>

          <SessionInfo
            intl={intl}
            setSessionFilter={setSessionFilter}
            data={data} /> 
        </div>
      ):(
      <div style={{padding:30}}>
        <section>
          <div style={{width:'80%', marginLeft: 'auto', marginRight:'auto'}}>
            <h3><FormattedMessage id="history.limitedData"/> </h3>
            <div style={{marginTop: 50}}/>    
          </div>
          <SessionInfo
            intl={intl}
            data={data} />
        </section>
        </div> 
      );
}

var SessionModal = React.createClass({
  componentWillMount: function() {

  },
  
  componentWillReceiveProps: function(nextProps) {
    const data = nextProps.data || this.props.data;
    const fetchSession = nextProps.fetchSession || this.props.fetchSession;
    console.log('component session modal receiving props', nextProps, data);
    if (data && !data.measurements) {
      fetchSession();
    }
  },
  /* 
  onOpen: function (id, index, device) {
    this.props.setActiveSessionIndex(index);
    //this.props.getActiveSession(device, this.props.time);
    },
    */
  onClose: function() {
    this.props.resetActiveSessionIndex();
    //set session filter to volume for sparkline
    this.props.setSessionFilter('volume');
  },
  onNext: function() {
    const { time } = this.props;
    const { next:[id, device]} = this.props.data;

    this.props.increaseActiveSessionIndex();
        
    if (id!==null && device!==null)
      this.props.getDeviceSession(id, device, this.props.time);  
  },
  onPrevious: function() {
    const { time } = this.props;
    const { prev:[id, device]} = this.props.data;

    this.props.decreaseActiveSessionIndex();

    if (id!==null && device!==null)
      this.props.getDeviceSession(id, device, this.props.time);  
  },
  render: function() {
    const { data, intl, filter, setSessionFilter } = this.props;
    if (!data) return (<div/>);
    const { next, prev } = data;
    const disabledNext = Array.isArray(next)?false:true;
    const disabledPrevious = Array.isArray(prev)?false:true;

    //const history = this.props.data?this.props.data.history:null;

    const _t = intl.formatMessage;
    return (
      <bs.Modal animation={false} show={this.props.showModal} onHide={this.onClose} bsSize="large">
        <bs.Modal.Header closeButton>
          <bs.Modal.Title><FormattedMessage id="section.shower" /></bs.Modal.Title>
        </bs.Modal.Header>
        <bs.Modal.Body>

          <Session {...this.props} />

        </bs.Modal.Body>
        <bs.Modal.Footer>
          <bs.Button disabled={disabledPrevious} onClick={this.onPrevious}>Previous</bs.Button>
          <bs.Button disabled={disabledNext} onClick={this.onNext}>Next</bs.Button>
          <bs.Button onClick={this.onClose}>Close</bs.Button>
        </bs.Modal.Footer>
      </bs.Modal> 
    );
  }
});
    
module.exports = SessionModal;
