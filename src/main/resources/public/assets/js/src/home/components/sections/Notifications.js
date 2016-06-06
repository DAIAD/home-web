var React = require('react');

var { Link } = require('react-router');
var bs = require('react-bootstrap');
var classNames = require('classnames');
var { FormattedMessage, FormattedRelative } = require('react-intl');

const { IMAGES, NOTIFICATION_TITLE_LENGTH } = require('../../constants/HomeConstants'); 

var Topbar = require('../layout/Topbar');
var { SidebarLeft } = require('../layout/Sidebars');
var MainSection = require('../layout/MainSection');



function NotificationMessage (props) {
  const { notification, nextMessageId, previousMessageId, setActiveMessageId } = props;

  return !notification?<div />:(
    <div className="notification">
      <h3 className="notification-header">{notification.title}</h3>
      {
        notification.imageEncoded?(<img className="notification-img" src={"data:image/png;base64, " + notification.imageEncoded} />):null
      }

      <div className="notification-details">
        <p>{notification.description}</p>
        {
          (() => notification.acknowledgedOn ?
           <div>
             <p style={{width: '100%', textAlign: 'right', fontSize: '0.8em'}}>
               <i 
                 style={{marginRight: 5}}
                 className={
                  classNames("fa", "fa-md", "green", "fa-check")
                } />
               <FormattedRelative value={notification.acknowledgedOn} />
             </p>
           </div>
           : <span/>
           )()
        }
      </div>

      <div className='notification-pagination'>      
        {
          (() => previousMessageId != null ? 
        <a className="pull-left" onClick={() => setActiveMessageId(previousMessageId)}>
          <img src={`${IMAGES}/arrow-big-left.svg`} /><span>Previous</span>
        </a>
        : <span/>
        )()
        }
        {
          (() => nextMessageId != null ? 
            <a className="pull-right" onClick={() => setActiveMessageId(nextMessageId)}>
              <span>Next</span><img src={`${IMAGES}/arrow-big-right.svg`} />
            </a>
            : <span/>
            )()
        }
      </div>
    </div>
  );
}

function NotificationList(props){
  const maxLength = NOTIFICATION_TITLE_LENGTH;
  return (
    <div className="notification-list">
    <ul className="list-unstyled">
      {
        props.notifications.map(function(notification) {
          const activeClass = notification.id === props.activeMessageId ? ' active' : ''; 
          const notificationClass = notification.acknowledgedOn ? ' read' : ' unread';
          return (
            <li key={notification.id} className={notificationClass + activeClass} >
              <a onClick={() => props.setActiveMessageId(notification.id)}>
                {
                  //(notification.title.length>maxLength)?(`${notification.title.substring(0, maxLength).trim()}...`):notification.title
                  notification.title
                }
              </a>
            </li>
          );
          })
    }
  </ul>
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
    const { intl, categories, messages:notifications, activeMessageId, previousMessageId, nextMessageId, activeMessage:notification, activeTab, setActiveMessageId, setActiveTab } = this.props;
    
    const _t = intl.formatMessage;
    return (
      <MainSection id="section.notifications">
        <div className='notifications'>
          <div className='notifications-left'>
            
            <Topbar> 
              <bs.Tabs position='top' tabWidth={5} activeKey={activeTab} onSelect={(key) => setActiveTab(key)}>
               {
                categories.map(category =>
                            <bs.Tab key={category.id} eventKey={category.id} title={_t({id: category.title}) + ' (' + category.unread + ') '} />)
               } 
              {
                //  <bs.Tab eventKey="always" title={_t({id: "history.always"})} />
               }
            </bs.Tabs>
          </Topbar>

          <NotificationList {...{notifications, activeMessageId, previousMessageId, nextMessageId, setActiveMessageId}}/>   
          </div>
          <div className='notifications-right'>
            <NotificationMessage {...{notification, setActiveMessageId, previousMessageId, nextMessageId}} />
          </div>
        </div>
          { /* hack for notification window to close after it has been clicked */ }
          <input 
            type="hidden" 
            ref= {
              function(i) { if (i!==null){ i.click(); } } 
            }
          />
          
        </MainSection>
    );
  }
});

module.exports = Notifications;
