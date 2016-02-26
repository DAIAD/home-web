var React = require('react');

var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;
var MainSection = require('../MainSection.react');
var Sidebar = require('../Sidebar.react');

var Commons = React.createClass({
	render: function() {
		var _t = this.props.intl.formatMessage;
		return (
			<MainSection id="section.commons">
			</MainSection>
		);
	}
});

Commons = injectIntl(Commons);
module.exports = Commons;
