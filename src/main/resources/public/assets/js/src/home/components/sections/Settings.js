var React = require('react');
var { FormattedMessage } = require('react-intl');
var { Link } = require('react-router');

var Topbar = require('../Topbar');
var MainSection = require('../MainSection');


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
