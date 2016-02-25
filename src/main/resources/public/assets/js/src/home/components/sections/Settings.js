var React = require('react');

var Topbar = require('../Topbar.react');
var MainSection = require('../MainSection.react');
var FormattedMessage = require('react-intl').FormattedMessage;

var Link = require('react-router').Link;

var Settings = React.createClass({
	render: function() {
		return (
			<div>
					<Topbar> 
					<ul className="list-unstyled">
						<li><Link to="/settings/profile"><FormattedMessage id="section.profile" /></Link></li>
						<li><Link to="/settings/devices"><FormattedMessage id="section.devices" /></Link></li>
					</ul>
				</Topbar>
				<div>
					{
						this.props.children
					}
				</div>
			</div>
		);
	}
});

module.exports = Settings;
