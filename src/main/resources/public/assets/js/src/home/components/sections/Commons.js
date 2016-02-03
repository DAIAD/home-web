var React = require('react');

var MainSection = require('../MainSection.react');
var Sidebar = require('../Sidebar.react');

var Commons = React.createClass({
	render: function() {
		return (
			<section className="section-commons">
				<MainSection title="Commons">
					<input type="search" placeholder="Search for commons..." />
				</MainSection>
				<Sidebar>
					<h3>This is a test sidebar</h3>
				</Sidebar>
			</section>
		);
	}
});

module.exports = Commons;
