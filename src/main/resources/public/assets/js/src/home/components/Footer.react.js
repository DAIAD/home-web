var React = require('react');

//var MainMenu = require('./Header.react').MainMenu;

var Footer = React.createClass({
	render: function() {
		return (
			<footer className="site-footer">
				<div className="container">
					<h3>Footer</h3>
				</div>
			</footer>
		);
	}
});

module.exports = Footer;
