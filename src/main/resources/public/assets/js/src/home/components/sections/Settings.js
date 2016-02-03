var React = require('react');

var Topbar = require('../Topbar.react');
var MainSection = require('../MainSection.react');

var Link = require('react-router').Link;

var Settings = React.createClass({
	render: function() {
		return (
			<section className="section-settings">
					<Topbar> 
					<ul className="list-unstyled">
						<li><Link to="/settings/profile">Profile</Link></li>
						<li><Link to="/settings/devices">Devices</Link></li>
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
