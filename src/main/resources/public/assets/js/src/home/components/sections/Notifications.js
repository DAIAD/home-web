var React = require('react');

var { Link } = require('react-router');
var bs = require('react-bootstrap');

const { STATIC_RECOMMENDATIONS } = require('../../constants/HomeConstants'); 

var Topbar = require('../Topbar');
var { SidebarLeft } = require('../Sidebars');
var MainSection = require('../MainSection');

var { NotificationList } = require('../Header');


function NotificationMessage (props) {
  const { notification } = props;
  console.log('notification message', notification);

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

function NotificationModal (props) {
  const { notification, shown, closeNotification, showNext, showePrevious, disabledNext, disabledPrevious } = props;
  return notification ? (
    <bs.Modal animation={false} show={shown} onHide={closeNotification} bsSize="large">
        <bs.Modal.Header closeButton>
          <bs.Modal.Title>
            {notification.title} 
            </bs.Modal.Title>
        </bs.Modal.Header>
        <bs.Modal.Body>

          <NotificationMessage notification={notification} />

        </bs.Modal.Body>
        <bs.Modal.Footer>
          { (() => disabledPrevious ? <span/> : <a className='pull-left' onClick={this.onPrevious}>Previous</a> )() }
          { (() => disabledNext ? <span/> : <a className='pull-right' onClick={this.onNext}>Next</a> )() }
        </bs.Modal.Footer>
      </bs.Modal> 
  )
    :
      (<div/>);
}


var Notifications = React.createClass({
  render: function() {
    return (
      <MainSection id="section.notifications">
        <div className="primary">

          <NotificationList notifications={STATIC_RECOMMENDATIONS} />   
          { /* hack for notification window to close after it has been clicked */ }
          <input 
            type="hidden" 
            ref= {
              function(i) { if (i!==null){ i.click(); } } 
            }
          />
          <br/>
          <NotificationModal notification={null} />

          <input 
            type="hidden" 
            ref= {i => { if (i!==null){ i.click(); }} } 
          />
        </div>
        </MainSection>
    );
  }
});

module.exports = Notifications;
