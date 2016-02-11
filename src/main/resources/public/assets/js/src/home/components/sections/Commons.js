var React = require('react');

var injectIntl = require('react-intl').injectIntl;
var FormattedMessage = require('react-intl').FormattedMessage;
var MainSection = require('../MainSection.react');
var Sidebar = require('../Sidebar.react');

var Commons = React.createClass({
	render: function() {
		var _t = this.props.intl.formatMessage;
		return (
			<section className="section-commons">
				<h3><FormattedMessage id="section.commons"/></h3>
			</section>
		);
	}
});

Commons = injectIntl(Commons);
module.exports = Commons;
