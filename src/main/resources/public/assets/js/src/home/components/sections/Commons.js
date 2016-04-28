var React = require('react');
var { injectIntl } = require('react-intl');
var { FormattedMessage } = require('react-intl');

var MainSection = require('../MainSection');
var { SidebarLeft } = require('../Sidebars');

var Commons = React.createClass({
  render: function() {
    const _t = this.props.intl.formatMessage;
    return (
      <MainSection id="section.commons">
      </MainSection>
    );
  }
});

Commons = injectIntl(Commons);
module.exports = Commons;
