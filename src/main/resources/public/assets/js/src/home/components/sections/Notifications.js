var React = require('react');

var Link = require('react-router').Link;

var Constants = require('../../constants/HomeConstants');

var Topbar = require('../Topbar');
var Sidebar = require('../Sidebar');
var MainSection = require('../MainSection');
var Notification = require('../Notification');

const { STATIC_RECOMMENDATIONS } = require('../../constants/HomeConstants'); 


var Notifications = React.createClass({
  render: function() {
    return (
      <section>
        <Topbar> 
          <ul className="list-unstyled">
            <li><Link to="/notifications">All</Link></li>
            <li><Link to="/notifications/insights">Insights</Link></li>
            <li><Link to="/notifications/alerts">Alerts</Link></li>
            <li><Link to="/notifications/tips">Tips</Link></li>
          </ul>
        </Topbar>
        <Notification {...this.props} />
    
        <input 
          type="hidden" 
          ref= {i => { if (i!==null){ i.click(); }} } 
        />
      </section>
    );
  }
});

module.exports = Notifications;
