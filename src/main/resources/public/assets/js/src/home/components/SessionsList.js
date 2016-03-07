var React = require('react');
var Link = require('react-router').Link;
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { FormattedMessage, FormattedRelative } = require('react-intl');

var Chart = require('./Chart');
var Shower = require('./Shower');
var SessionsChart = require('./SessionsChart');

//Actions
var DeviceActions = require('../actions/DeviceActions');

var getFriendlyDuration = function(seconds) {
	if (seconds>3600) {
		return Math.floor(seconds/3600) + ":" + Math.floor((seconds % 3600)/60) + ":" + (Math.floor((seconds % 3600)/60)) % 60;
	}
	else if (seconds>60) {
		return Math.floor(seconds/60)  + ":" + Math.floor(seconds/60)%60;
	}
	else {
		return "00:" + seconds;
	}
};

var getEnergyClass = function(energy) {
  var scale = "";

  if (energy >= 3675) {
    scale = "G-";
  }
  else if (energy >= 3500) {
    scale = "G";
  }
  else if (energy >= 3325) {
    scale = "G+";
  }
  else if (energy >= 3150) {
    scale = "F-";
  }
  else if (energy >= 2975) {
    scale = "F";
  }
  else if (energy >= 2800) {
    scale = "F+";
  }
  else if (energy >= 2625) {
    scale = "E-";
  }
  else if (energy >= 2450) {
    scale = "E";
  }
  else if (energy >= 2275) {
    scale = "E+";
  }
  else if (energy >= 2100) {
    scale = "D-";
  }
  else if (energy >= 1925) {
    scale = "D";
  }
  else if (energy >= 1750) {
    scale = "D+";
  }
  else if (energy >= 1575) {
    scale = "C-";
  }
  else if (energy >= 1400) {
    scale = "C";
  }
  else if (energy >= 1225) {
    scale = "C+";
  }
  else if (energy >= 1050) {
    scale = "B-";
  }
  else if (energy >= 875) {
    scale = "B";
  }
  else if (energy >= 700) {
    scale = "B+";
  }
  else if (energy >= 525) {
    scale = "A-";
  }
  else if (energy >= 351) {
    scale = "A";
  }
  else if (energy <= 350) {
    scale = "A+";
  }
  return scale;
};

var SessionItem = React.createClass({
	handleClick: function() {
		this.props.onOpen(this.refs.link.dataset.id, this.refs.link.dataset.index);
	},
  render: function() {
    //mockup values
    var arrowClasses = "fa-arrow-up red";
    if (this.props.index===2 || this.props.index===4 || this.props.index===0){
      arrowClasses = "fa-arrow-down green";
    }
		return (
			<li className="session-item">	
				<a onClick={this.handleClick} ref="link" data-id={this.props.data.id} data-index={this.props.index} >
          <div className="session-item-header col-md-3"><h3>{this.props.data.volume}<span style={{fontSize: '0.6em'}}> lt</span> <i className={"fa " + arrowClasses}/></h3>
            
          </div>
          <div className="col-md-7">
            <div className="pull-right">
              {
                //mockup values
               }
            <span className="session-item-detail">Stelios</span>
						<span className="session-item-detail"><i className="fa fa-calendar"/><FormattedRelative value={new Date(this.props.data.timestamp)} /></span> 
            {(() => { 
              if (this.props.data.duration) {
                return (
                  <span className="session-item-detail"><i className="fa fa-clock-o"/>{getFriendlyDuration(this.props.data.duration)}</span>);
              }
              else {
                return (null);
              }
            })()}
            {(() => { 
              if (this.props.data.energy) {
                return (
              <span className="session-item-detail"><i className="fa fa-flash"/>{getEnergyClass(this.props.data.energy)}</span>);
              }
              else {
                return (null);
              }
            })()}
            {(() => { 
              if (this.props.data.temperature) {
                return (
              <span className="session-item-detail"><i className="fa fa-temperature"/>{this.props.data.temperature}ºC</span>);
              }
              else {
                return (null);
            }
            })()}
          </div>
          </div>
          <div className="col-md-2">
            <SparklineChart history={this.props.data.history} data={this.props.data.measurements} intl={this.props.intl}/>
          </div>
				</a>
			</li>
		);
	}
});

var SparklineChart = React.createClass({
  render: function() {
    if (!this.props.data || !this.props.data.length || this.props.data.length<=1 || this.props.history) {
      return (<h3>-</h3>);
    }
    return (
      <SessionsChart
        height={80}
        width='100%'	
        title=""
        subtitle=""
        mu=""
        formatter={(x) => this.props.intl.formatDate(x)}
        type="line"
        sparkline={true}
        xMargin={5}
        yMargin={0}
        x2Margin={2}
        y2Margin={0}
        data={[{title: 'Consumption', data:this.props.data}]}
      />);
  }
});


var SessionsList = React.createClass({

	onOpen: function (id, index) {

		this.props.setActiveSessionIndex(index);

		if (id){
			this.props.fetchSession(id, this.props.activeDevice, this.props.time);
		}
	},
	onClose: function() {
    this.props.resetActiveSessionIndex();
    //set session filter to volume for sparkline
    this.props.setSessionFilter('volume');
	},
	onNext: function() {
		
		//this.props.setActiveSessionIndex(index+1);
		this.props.getNextSession(this.props.activeDevice, this.props.time);
	},
	onPrevious: function() {
		//this.props.setActiveSessionIndex(index-1);
		this.props.getPreviousSession(this.props.activeDevice, this.props.time);
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
								loading={this.props.loading}
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
              filter={this.props.sessionFilter}
							loading={this.props.loading}  />
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
