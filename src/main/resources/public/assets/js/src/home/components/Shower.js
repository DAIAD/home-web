var React = require('react');
var ReactDOM = require('react-dom');

var bs = require('react-bootstrap');
//var link = require('react-router').link;
var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedTime = require('react-intl').FormattedTime;
var FormattedRelative = require('react-intl').FormattedRelative;


var SessionsChart = require('./SessionsChart');

var MainSection = require('./MainSection');
var Sidebar = require('./Sidebar');
var timeUtil = require('../utils/time');


var SessionItem = React.createClass({
  handleClick: function(e) {
    e.preventDefault();
    
    this.props.sessionClick(this.props.id);
    //TODO: scroll to chart
  },
  render: function() {
    if (!this.props.data) return (null);
    const _t = this.props.intl.formatMessage;
    return (
      <li className="session-item" >
        <a onClick={this.handleClick} title={_t({id: this.props.details})}>
          <h4 style={{float: 'left'}}><img style={{width:this.props.id==='temperature'?12:24, marginRight:20}} src={`/assets/images/svg/${this.props.icon}.svg`} /><FormattedMessage id={this.props.title} /></h4>
        <h4 style={{float:'right'}}>{this.props.data} <span>{this.props.mu}</span></h4>
      </a>
      </li>
    );
  }
});

var SessionInfo = React.createClass({

  render: function() {
    const { setSessionFilter, chartDOM, intl, data } = this.props;
    
    const metrics = [
      {id:'count',mu:'', title:'history.count', details:'history.countDetails'},  
      {id:'volume', mu:'lt',title:'history.volume', details:'history.volumeDetails'}, 
      {id:'temperature', mu:'ºC', title:'history.temperature', details: 'history.temperatureDetails'}, 
      {id:'energy',mu:'W', title:'history.energy', details: 'history.energyDetails'}, 
      {id:'duration', icon:'timer-on', mu:'sec', title:'history.duration', details: 'history.durationDetails'}, 
    ];
    if (!data) return <div/>;

    return (
      <div style={{width: '80%', marginLeft:'auto', marginRight:'auto'}}>
        <h4><FormattedMessage id="shower.details"/></h4>
        <hr/>
        <h5 style={{float: 'right'}} ><i style={{marginRight: 10}} className="fa fa-user"/>Stelios</h5>
        <h5 style={{float: 'left'}}><i style={{marginRight: 10}} className="fa fa-calendar"/><FormattedTime value={new Date(data.timestamp)} date={{day:"numeric", month:"long", year:"numeric"}} time={{hours:"numeric", minutes:"numeric"}} /></h5>
        <br/>
        <br/>
        <ul className="sessions-list" >
          {
            metrics.map(function(metric) {
              return (<SessionItem key={metric.id} intl={intl} icon={metric.icon || metric.id} sessionClick={setSessionFilter} title={metric.title} chartDOM={chartDOM} id={metric.id} data={data[metric.id]} mu={metric.mu} details={metric.details} />);
            })
          }
        </ul>
    </div>
    );
  }
});


var Shower = React.createClass({
  
  handleTypeSelect: function(key){
    this.props.setSessionFilter(key); 
  },
  render: function() {
    const history = this.props.data?this.props.data.history:null;
    const _t = this.props.intl.formatMessage;
    return (
      <div>
      {
      (() => {
        if (history === false){
        return (
          <div style={{padding: 30}}>
            <SessionsChart 
                ref="chartDOM"
                height={300}
                width='100%'
                title={_t({id: `history.${this.props.filter}`})}
                mu="lt"
                xMargin={60}
                x2Margin={60}
                type="line"
                formatter={(x) => this.props.intl.formatTime(x, { hour: 'numeric', minute: 'numeric'})}
                data={[{title: this.props.filter, data:this.props.data.measurements}]}
                />
                <div style={{marginTop: 30}}/>    
            <SessionInfo
              intl={this.props.intl}
              setSessionFilter={this.props.setSessionFilter}
              chartDOM={this.refs.chartDOM}
              data={this.props.data} /> 
          </div>
        );
      }
      else {
        return (
          <div style={{padding:30}}>
            <section>
              { (() => 
                 {
                  if (this.props.data.count) {
                    return (null);
                  }
                  else {
                    return (
                      <div style={{width:'80%', marginLeft: 'auto', marginRight:'auto'}}>
                      <h3><FormattedMessage id="history.limitedData"/> </h3>
                      <div style={{marginTop: 50}}/>    
                      </div>
                      );
                  }
                })()
              }

          <SessionInfo
             intl={this.props.intl}
            data={this.props.data} />

          </section>
        </div>
      );
      }
      })()
      }
    </div>
    );
  }
    
});

module.exports = Shower;
