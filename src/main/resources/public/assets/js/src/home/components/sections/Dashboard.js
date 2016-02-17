var React = require('react');
var bs = require('react-bootstrap');
var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;


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

var Dashboard = React.createClass({
	render: function() {
		return (
			<section className="section-dashboard">
				<h3><FormattedMessage id="section.dashboard"/></h3>
				<SayHello firstname={this.props.firstname}/>
			</section>
		);
	}
});

module.exports = Dashboard;
