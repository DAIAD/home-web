var React = require('react');

var bs = require('react-bootstrap');
//var link = require('react-router').link;
var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedTime = require('react-intl').FormattedTime;
var FormattedRelative = require('react-intl').FormattedRelative;

var SessionsChart = require('./SessionsChart');

var { SHOWER_METRICS, IMAGES } = require('../constants/HomeConstants'); 
var { SidebarLeft } = require('./layout/Sidebars');
var timeUtil = require('../utils/time');


function SessionInfoItem (props) {
  const _t = props.intl.formatMessage;
  return !props.data?<div />:(
    <li className="session-item" >
      {(()=> props.sessionClick?(
        <a onClick={() => props.sessionClick(props.id)} title={_t({id: props.details})}>
        <h4 style={{float: 'left'}}><img style={{width:props.id==='temperature'?12:24, marginRight:20}} src={`${IMAGES}/${props.icon}.svg`} /><FormattedMessage id={props.title} /></h4>
      <h4 style={{float:'right'}}>{props.data} <span>{props.mu}</span></h4>
    </a>
    ):(
    <span>
        <h4 style={{float: 'left'}}><img style={{width:props.id==='temperature'?12:24, marginRight:20}} src={`${IMAGES}/${props.icon}.svg`} /><FormattedMessage id={props.title} /></h4>
      <h4 style={{float:'right'}}>{props.data} <span>{props.mu}</span></h4>
    </span>
    ))()
      }
    </li>
  );
}

function SessionInfo (props) {
  const { setSessionFilter, intl, data, firstname } = props;
  return !data?<div />:(
    <div className="shower-info">
      <div className="headline">
        <span className="headline-user"><i className="fa fa-user"/>{firstname}</span>
        <span className="headline-date"><i className="fa fa-calendar"/><FormattedTime value={new Date(data.timestamp)} date={{day:"numeric", month:"long", year:"numeric"}} time={{hours:"numeric", minutes:"numeric"}} /></span>
      </div>
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
  const { history, intl, filter, data, chartData, setSessionFilter, firstname } = props;
  if (!data) return <div/>;
  const { hasChartData } = data;
  
  const _t = intl.formatMessage;
  //title = _t({id: `history.${filter}`})
  return hasChartData?(
    <div className="shower-container">
      <div className="shower-chart-area">
        <SessionsChart 
            height={300}
            width='100%'
            title=""
            mu="lt"
            xMargin={60}
            x2Margin={60}
            type="line"
            formatter={(x) => intl.formatTime(x, { hour: 'numeric', minute: 'numeric'})}
            data={[{title:filter, data:chartData}]}
          />
        </div>
        
        <SessionInfo
          firstname={firstname}
          intl={intl}
          setSessionFilter={setSessionFilter}
          data={data} /> 
    </div>
  ) : (
  <div className="shower-container">
    <div className="shower-chart-area">
      <h3><FormattedMessage id="history.limitedData"/> </h3>
    </div>
    
    <SessionInfo
      firstname={firstname}
      intl={intl}
      data={data} />
    </div> 
  );
}

var SessionModal = React.createClass({
  
  onClose: function() {
    this.props.resetActiveSessionIndex();
    //set session filter to volume for sparkline
    //this.props.setSessionFilter('volume');
  },
  onNext: function() {
    const { time } = this.props;
    const { next:[id, device]} = this.props.data;

    this.props.increaseActiveSessionIndex(id, device);
  },
  onPrevious: function() {
    const { time } = this.props;
    const { prev:[id, device]} = this.props.data;

    this.props.decreaseActiveSessionIndex(id, device);
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
