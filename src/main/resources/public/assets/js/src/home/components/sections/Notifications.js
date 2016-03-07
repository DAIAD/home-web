var React = require('react');

var Link = require('react-router').Link;

var Constants = require('../../constants/HomeConstants');

var Topbar = require('../Topbar');
var Sidebar = require('../Sidebar');
var MainSection = require('../MainSection');
var NotificationList = require('../Header').NotificationList;


var NotificationMessage = React.createClass({
  render: function() {
    if (!this.props.notification){
      return (<div />);
    }
    var { notification } = this.props;
    return (
      <div className="notification">
        <h3 className="notification-header">{notification.title}</h3>
        {
          notification.imageLink?(<img className="notification-img" src={notification.imageLink} />):null
        }
        <p className="notification-details">{notification.description}</p>
      </div>
    );
    }
});
var Notifications = React.createClass({
  
  render: function() {
    var notId = parseInt(this.props.params.id);
    var notificationItem = Constants.data.recommendations.find((item) => (item.id === notId));

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

        <MainSection id="section.notifications">
          <div className="primary" >
            <NotificationList notifications={Constants.data.recommendations} />   

            <input 
              type="hidden" 
              ref= {
                function(i) { if (i!==null){ i.click(); } } 
              } />

          </div>
        </MainSection>
      </section>
    );
  }
});

module.exports = Notifications;
