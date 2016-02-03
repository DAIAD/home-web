var React = require('react');


var MainSection = require('../MainSection.react');

var Constant = require('../../constants/HomeConstants');
var UserStore = require('../../stores/UserStore');

/* Be Polite, greet user */
var SayHello = React.createClass({
	render: function() {
		var name = UserStore.getProfile().firstname;
		return (
			<h3>Hello {name}!</h3>
		);
	}
});

var Dashboard = React.createClass({
	render: function() {
		return (
			<section className="section-dashboard primary">
				<SayHello />
			</section>
		);
	}
});

module.exports = Dashboard;
