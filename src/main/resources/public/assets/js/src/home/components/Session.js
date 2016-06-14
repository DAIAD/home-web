var React = require('react');

var bs = require('react-bootstrap');
//var link = require('react-router').link;
var { FormattedMessage, FormattedTime, FormattedDate, FormattedRelative } = require('react-intl');

var Chart = require('./helpers/Chart');

var { SHOWER_METRICS, METER_AGG_METRICS, IMAGES } = require('../constants/HomeConstants'); 
var { SidebarLeft } = require('./layout/Sidebars');
var timeUtil = require('../utils/time');


function SessionInfoItem (props) {
  const _t = props.intl.formatMessage;
  return !props.data?<div />:(
    <li className="session-item" >
      {(()=> props.sessionClick?(
        <a onClick={() => props.sessionClick(props.id)} title={_t({id: props.details})}>
        <h4 style={{float: 'left'}}><img style={{height:props.id==='temperature'?30:24, marginLeft:props.id==='temperature'?7:0, marginRight:20}} src={`${IMAGES}/${props.icon}`} /><FormattedMessage id={props.title} /></h4>
      <h4 style={{float:'right'}}>{props.data} <span>{props.mu}</span></h4>
    </a>
    ):(
    <span>
        <h4 style={{float: 'left'}}><img style={{height:props.id==='temperature'?30:24, marginLeft:props.id==='temperature'?7:0, marginRight:20}} src={`${IMAGES}/${props.icon}`} /><FormattedMessage id={props.title} /></h4>
      <h4 style={{float:'right'}}>{props.data} <span>{props.mu}</span></h4>
    </span>
    ))()
      }
    </li>
  );
}

function SessionInfo (props) {
  const { setSessionFilter, intl, data, firstname, activeDeviceType } = props;
  const metrics = activeDeviceType === 'METER' ? METER_AGG_METRICS : SHOWER_METRICS;
  return !data?<div />:(
    <div className="shower-info">
      <div className="headline">
        <span className="headline-user"><i className="fa fa-user"/>{firstname}</span>
        {
          //<span className="headline-date"><i className="fa fa-calendar"/>{new Date(data.timestamp).toString()}</span>
          }
          {
            //<span className="headline-date"><i className="fa fa-calendar"/><FormattedDate value={new Date(data.timestamp)} year='numeric' month='long' day='numeric' weekday='long' /> <FormattedTime value={new Date(data.timestamp)}/></span>
          }
          <span className="headline-date"><i className="fa fa-calendar"/>{data.date}</span>
      </div>
      <ul className="sessions-list" >
        {
          metrics.map(function(metric) {
            return (<SessionInfoItem key={metric.id} intl={intl} icon={metric.icon} sessionClick={metric.clickable?setSessionFilter:null} title={metric.title} id={metric.id} data={data[metric.id]} mu={metric.mu} details={metric.details} />);
          })
        }
      </ul>
  </div>
  );
}

function Session (props) {
  const { intl, filter, data, chartData, date, chartFormatter, setSessionFilter, firstname, activeDeviceType } = props;
  if (!data) return <div/>;
  const { hasChartData, history, id } = data;
  const _t = intl.formatMessage;
  //title = _t({id: `history.${filter}`})
  return history===false?(
    <div className="shower-container">
      <div className="shower-chart-area">
        <Chart 
            height={300}
            width='100%'
            type='line'
            title=""
            mu="lt"
            xMargin={60}
            x2Margin={60}
            formatter={chartFormatter}
            xAxis="time"
            data={[{title:`#${id}`, data:chartData}]}
          />
        </div>
        
        <SessionInfo
          firstname={firstname}
          intl={intl}
          setSessionFilter={setSessionFilter}
          activeDeviceType={activeDeviceType}
          data={data} /> 
    </div>
  ) : (
  <div className="shower-container">
    <div className="shower-chart-area">
      <h3><FormattedMessage id="history.limitedData"/> </h3>
    </div>
    
    <SessionInfo
      firstname={firstname}
      activeDeviceType={activeDeviceType}
      intl={intl}
      data={data} />
    </div> 
  );
}

var SessionModal = React.createClass({
  
  onClose: function() {
    this.props.resetActiveSession();
  },
  onNext: function() {
    const { next:[device, id, timestamp]} = this.props.data;
    this.props.setActiveSession(device, id, timestamp);
  },
  onPrevious: function() {
    const { prev:[device, id, timestamp]} = this.props.data;
    this.props.setActiveSession(device, id, timestamp);
  },
  render: function() {
    const { data, intl, filter, setSessionFilter } = this.props;
    if (!data) return (<div/>);
    const { next, prev } = data;
    const disabledNext = Array.isArray(next)?false:true;
    const disabledPrevious = Array.isArray(prev)?false:true;

    const _t = intl.formatMessage;
    return (
      <bs.Modal animation={false} show={this.props.showModal} onHide={this.onClose} bsSize="large">
        <bs.Modal.Header closeButton>
          <bs.Modal.Title>
            {(() => data.id ?
              <span>
                <FormattedMessage id="section.shower" /><span>{` #${data.id}`}</span>
              </span>
              :
              <FormattedMessage id="section.shower-aggregated" />
              )()
            }
            </bs.Modal.Title>
        </bs.Modal.Header>
        <bs.Modal.Body>

          <Session {...this.props} />

        </bs.Modal.Body>
        <bs.Modal.Footer>
          { (() => disabledPrevious ? <span/> : <a className='pull-left' onClick={this.onPrevious}>Previous</a> )() }
          { (() => disabledNext ? <span/> : <a className='pull-right' onClick={this.onNext}>Next</a> )() }
        </bs.Modal.Footer>
      </bs.Modal> 
    );
  }
});
    
module.exports = SessionModal;
