var React = require('react');
var Link = require('react-router').Link;
var bs = require('react-bootstrap');
var injectIntl = require('react-intl').injectIntl;
var FormattedRelative = require('react-intl').FormattedRelative;

var Chart = require('./Chart');
var Shower = require('./Shower');

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
	if (energy>150) {
		return "G";
	}
	else if (energy>=125) {
		return "F";
	}
	else if (energy>=110) {
		return "E";
	}
	else if (energy>=95) {
		return "D";
	}
	else if (energy>=75) {
		return "C";
	}
	else if (energy>=55) {
		return "B";
	}
	else {
		return "A";
	}
};

var SessionItem = React.createClass({
	handleClick: function() {
		this.props.onOpen(this.refs.link.dataset.id, this.refs.link.dataset.index);
	},
	render: function() {
		return (
			<a onClick={this.handleClick} ref="link" data-id={this.props.data.id} data-index={this.props.index} >
				<li className="session-item">	
					<span className="session-item-header">{this.props.data.volume}<span style={{fontSize: '0.6em'}}> lt</span></span><br/>
					<span className="session-item-details">
						<span>Stelios, </span>
						<span><FormattedRelative value={new Date(this.props.data.timestamp)} />, </span> 
						<span>{getFriendlyDuration(this.props.data.duration)}, </span> 
						<span>{getEnergyClass(this.props.data.energy)}</span></span>
				</li>
			</a>
		);
	}
});

var SessionInfo = React.createClass({
	render: function() {
		var data = this.props.data;
		var array = Array.from(entries(data));

		return (
			<ul>
				{
					this.props.activeSession?
				(<li>
					<b>measurements:</b><span>{data.measurements?data.measurements.length:'null'}</span>
				</li>):<div/>
				}
				{
					array.map(function(dato) {
						const prop = dato[0];
						const value = dato[1];
							
						if (typeof(value)==="object") return;
					return (
						<li key={prop}>
							<b>{prop}:</b> <span>{value}</span>
						</li>
					);
					})
				}
		</ul>
		);
	}
});

function* entries(obj) {
	   for (var key of Object.keys(obj)) {
			      yield [key, obj[key]];
		}
}


var SessionsList = React.createClass({

	onOpen: function (id, index) {

		this.props.setActiveSessionIndex(index);

		if (id){
			this.props.setActiveSession(id);
			this.props.fetchSession(id, this.props.activeDevice, this.props.time);
		}
	},
	onClose: function() {
		this.props.resetActiveSessionIndex();
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
			<div style={{marginTop: '50px'}}>
				<h3>List</h3>
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
						<bs.Modal.Title>Details</bs.Modal.Title>
					</bs.Modal.Header>
					<bs.Modal.Body>
						<Shower 
							setSessionFilter={this.props.setSessionFilter}
							activeSession={this.props.activeSession} 
							listData={this.props.activeSessionData}
							chartData={this.props.activeSessionChartData}
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

SessionsList = injectIntl(SessionsList);
module.exports = SessionsList;
