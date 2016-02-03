var React = require('react');

var MainSection = require('../MainSection.react');
var Login = require('./Login');

var LoginPage = React.createClass({
render: function() {
	return (
		<MainSection>
			<div className="primary">
				<Login />
			</div>
		</MainSection>
	);
}
});


module.exports = LoginPage;
