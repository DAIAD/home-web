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


function SessionItem (props) {
  const _t = props.intl.formatMessage;
  return !props.data?<div />:(
    <li className="session-item" >
      <a onClick={() => props.sessionClick(props.id)} title={_t({id: props.details})}>
        <h4 style={{float: 'left'}}><img style={{width:props.id==='temperature'?12:24, marginRight:20}} src={`/assets/images/svg/${props.icon}.svg`} /><FormattedMessage id={props.title} /></h4>
      <h4 style={{float:'right'}}>{props.data} <span>{props.mu}</span></h4>
    </a>
    </li>
  );
}

function SessionInfo (props) {
  const { setSessionFilter, intl, data } = props;
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
            return (<SessionItem key={metric.id} intl={intl} icon={metric.icon || metric.id} sessionClick={setSessionFilter} title={metric.title} id={metric.id} data={data[metric.id]} mu={metric.mu} details={metric.details} />);
          })
        }
      </ul>
  </div>
  );
}

function Shower (props) {
 
  console.log('shower',props);
  const history = props.data?props.data.history:null;

  const hasChartData = !history?(props.data?(props.data.id?true:false):false):false;
  const _t = props.intl.formatMessage;
  return (
    hasChartData?(
      <div style={{padding: 30}}>
        <SessionsChart 
            height={300}
            width='100%'
            title={_t({id: `history.${props.filter}`})}
            mu="lt"
            xMargin={60}
            x2Margin={60}
            type="line"
            formatter={(x) => props.intl.formatTime(x, { hour: 'numeric', minute: 'numeric'})}
            data={[{title: props.filter, data:props.data?props.data.chartData:[]}]}
            />
            <div style={{marginTop: 30}}/>

        <SessionInfo
          intl={props.intl}
          setSessionFilter={props.setSessionFilter}
          data={props.data} /> 
      </div>
    ):(
    <div style={{padding:30}}>
      <section>
        <div style={{width:'80%', marginLeft: 'auto', marginRight:'auto'}}>
          <h3><FormattedMessage id="history.limitedData"/> </h3>
          <div style={{marginTop: 50}}/>    
        </div>
        <SessionInfo
          intl={props.intl}
          data={props.data} />
      </section>
      </div> 
    )
  );
}
    
module.exports = Shower;
