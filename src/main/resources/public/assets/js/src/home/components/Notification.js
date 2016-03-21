var React = require('react');
var { Link } = require('react-router');

var Constants = require('../constants/HomeConstants');

var Topbar = require('./Topbar');
var Sidebar = require('./Sidebar');
var MainSection = require('./MainSection');
var { NotificationList } = require('./Header');
const { STATIC_RECOMMENDATIONS } = require('../constants/HomeConstants'); 


function NotificationMessage (props) {
  const { notification } = props;

  return !notification?<div />:(
    <div className="notification">
      <h3 className="notification-header">{notification.title}</h3>
      {
        notification.imageLink?(<img className="notification-img" src={notification.imageLink} />):null
      }
      <p className="notification-details">{notification.description}</p>
    </div>
  );
}

function Notification (props) {
  const notId = parseInt(props.params.id);
  const notificationItem = STATIC_RECOMMENDATIONS.find((item) => (item.id === notId));
  return (
    <section>
      <MainSection id="section.notifications">
      
        <Sidebar width = '30%'>
          <NotificationList notifications={STATIC_RECOMMENDATIONS} />   
        </Sidebar>

        <div className="primary" style={{width: '50%', marginRight: '150px'}}>
          <input 
            type="hidden" 
            ref= {
              function(i) { if (i!==null){ i.click(); } } 
            }
          />
          <NotificationMessage notification={notificationItem} />
          
          <Link className="notifications-show-all" to="/notifications">Back to all notifications</Link>
        </div>
      </MainSection>
    </section>
  );
}

module.exports = Notification;
