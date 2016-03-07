var React = require('react');
var { Link } = require('react-router');

var Constants = require('../constants/HomeConstants');

var Topbar = require('./Topbar');
var Sidebar = require('./Sidebar');
var MainSection = require('./MainSection');
var { NotificationList } = require('./Header');


var NotificationMessage = React.createClass({
  render: function() {
    if (!this.props.notification){
      return (<div />);
    }
    var { notification } = this.props;
    console.log(notification);
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
var Notification = React.createClass({
  
  render: function() {
    var notId = parseInt(this.props.params.id);
    var notificationItem = Constants.data.recommendations.find((item) => (item.id === notId));

    return (
      <section>

        <MainSection id="section.notifications">
          <Sidebar width = '30%'>
            <NotificationList notifications={Constants.data.recommendations} />   
          </Sidebar>

          <div className="primary" style={{width: '50%', marginRight: '150px'}}>
            <input 
              type="hidden" 
              ref= {
                function(i) { if (i!==null){ i.click(); } } 
              } />
            <NotificationMessage notification={notificationItem} />
            
            <Link className="notifications-show-all" to="/notifications">Back to all notifications</Link>
          </div>
        </MainSection>
      </section>
    );
  }
});

module.exports = Notification;
