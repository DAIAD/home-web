var React = require('react');
var bs = require('react-bootstrap');
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;

var SessionsChart = require('../SessionsChart');

var Budget = require('../Budget');

/* Be Polite, greet user */
var SayHello = React.createClass({
	render: function() {
		return (
			<div >
				<h4><FormattedMessage id="dashboard.hello" values={{name:this.props.firstname}} /></h4>
			</div>
		);
	}
});

var SessionStats = React.createClass({
	render: function() {
		if (!this.props.lastShower){ 
			return (<div/>);
		}
		return (
			<div>
				<h3>Last shower</h3>
				<h4>{new Date(this.props.lastShower.timestamp).toString()}</h4>
				<ul>
					<li>You consumed a total of <b>{this.props.lastShower.volume} liters</b>!</li>
					<li>You used a total of <b>{this.props.lastShower.energy} kWh</b> for water heating!</li>
				</ul>
			</div>
		);
	}
});


var Dashboard = React.createClass({

	render: function() {
		return (
			<section className="section-dashboard">
				<h3><FormattedMessage id="section.dashboard"/></h3>
				<SayHello firstname={this.props.firstname}/>
				
				<SessionStats 
						lastShower={this.props.lastShower}
					/>
				<Budget />
				{
					(() => {
						if (!this.props.lastShower){
							return (<div/>);
						}
						else if (this.props.lastShower.history){
							return (<h4>Oops, can't graph due to limited data..</h4>);	
						}
						else {
							return (
								<SessionsChart
									height='200px'
									width={50}	
									title="Showers"
									subtitle="today"
									mu=""
									formatter={dayFormatter}
									type="line"
									data={this.props.chartData}
								/>
								);
						}
					})()
				}

			</section>
		);
	}
});

function addZero(i) {
    if (i < 10) {
        i = "0" + i;
    }
    return i;
}

const dayFormatter = function(timestamp){
	var date = new Date(timestamp);
	return (addZero(date.getHours()) + ':' +
					addZero(date.getMinutes()) + ':' +
					addZero(date.getSeconds())
				);
};
module.exports = Dashboard;
