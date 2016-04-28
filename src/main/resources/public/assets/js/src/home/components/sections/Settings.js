var React = require('react');
var { FormattedMessage } = require('react-intl');
var { Link } = require('react-router');

var Topbar = require('../Topbar');
var MainSection = require('../MainSection');


var Settings = React.createClass({
  render: function() {
    return (
      <div>
        {
          this.props.children
        }
      </div>
    );
  }
});

module.exports = Settings;
