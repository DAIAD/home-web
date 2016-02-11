var React = require('react');

var Topbar = require('../Topbar.react');
var MainSection = require('../MainSection.react');
var FormattedMessage = require('react-intl').FormattedMessage;

var Link = require('react-router').Link;

var Settings = React.createClass({
	render: function() {
		return (
			<section className="section-settings">
					<Topbar> 
					<ul className="list-unstyled">
						<li><Link to="/settings/profile"><FormattedMessage id="section.profile" /></Link></li>
						<li><Link to="/settings/devices"><FormattedMessage id="section.devices" /></Link></li>
					</ul>
				</Topbar>
				<MainSection title="Settings">
					{
						this.props.children
					}
				</MainSection>
			</section>
		);
	}
});

module.exports = Settings;
