var React = require('react');
var { injectIntl } = require('react-intl');
var { FormattedMessage } = require('react-intl');

var MainSection = require('../MainSection');
var Sidebar = require('../Sidebar');

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
