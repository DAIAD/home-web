var React = require('react');
var classNames = require('classnames');
var bs = require('react-bootstrap');
var { Link } = require('react-router');
var { injectIntl, FormattedMessage } = require('react-intl');

var LocaleSwitcher = require('../LocaleSwitcher');

var { Logout } = require('../LoginForm');

const { IMAGES, NOTIFICATION_TITLE_LENGTH } = require('../../constants/HomeConstants'); 

/* DAIAD Logo */
function MainLogo() {
  return (
    <Link to="/"  className="logo" activeClassName="selected">
      <img src={`${IMAGES}/daiad-logo-navy.svg`} alt="DAIAD Logo"
        title="DAIAD"/>
    </Link>
  );
}


/* Main Menu */
function MenuItem(props) {
  return (
    <li>
      <Link to={props.item.route} className="menu-item" activeClassName="selected">
        <img className="menu-icon" src={`${IMAGES}/${props.item.image}`} />
        <span className="menu-span"><FormattedMessage id={props.item.title}/></span>
      </Link>
    </li>
  );
}

function MainMenu(props) {   
  return (
    <div className="main-menu">
      <div className="navigation" role="navigation">
        <ul className="list-unstyled">
          {
          props.items.map(function (item) {
            return (
              <MenuItem key={item.name} item={item} />
            );
            })
          }   
        </ul>
      </div>
    </div>
  );
}


/* User options */

function UserInfo(props) {
  const _t = props.intl.formatMessage;
  return (
    <div className="user-menu" >
        <div title={_t({id: "section.profile"})}>
        <Link to="settings/profile">
          <span><b>{props.firstname}</b></span>
        </Link>
      </div>
    </div>
  );
}

/* Notification Area */
/*
var DevicesMenuItem = React.createClass({
  getInitialState: function() {
    return {
      hover:false 
    };
  },
  render: function() {
    const deviceCount = this.props.deviceCount;
    const image = this.state.hover?"images/svg/amphiro_small-green.svg":"images/svg/amphiro_small.svg";
    const _t = this.props.intl.formatMessage;
    return (
      <div title={_t({id: "section.settings"})}>
        <Link to="/settings"
          onMouseEnter={() => {this.setState({hover:true});}}
          onMouseLeave={() => {this.setState({hover:false});}} > 
          <img src={`/assets/${image}`} />
          <span className={classNames("deviceCount", "white")}>{deviceCount>0?deviceCount:""}</span>  
        </Link>
      </div>
    );
  }
});
*/

var NotificationMenuItem = React.createClass({
  getInitialState: function() {
    return {
      hover:false,
      popover:false
    };
  },
  render: function() {
    const hasUnread = this.props.unreadNotifications>0?"hasUnread":"";
    const unreadNotifications = hasUnread?this.props.unreadNotifications:"";
    const _t = this.props.intl.formatMessage;
    return (
      
      <bs.OverlayTrigger 
        id="notifications-trigger"
        trigger="click"
        title={_t({id: this.props.item.title})}
        rootClose
        placement="bottom"
        onEnter={() => this.setState({popover:true}) }
        onExit={() => this.setState({popover:false}) }
        overlay={
          <bs.Popover 
            id="notifications-popover"
            title={_t({id: this.props.item.title})} >
          <div className="scrollable">
            <NotificationList
              notifications={this.props.notifications} 
              linkToNotification={this.props.linkToNotification}
            />
          </div>
          <div className="footer">
            <Link className="notifications-show-all" to="/notifications">{_t({id:"notifications.showAll"})}</Link>
          </div>
        </bs.Popover>
        }
        className="notifications-button" >
          <a
            onMouseEnter={() => {this.setState({hover:true});}}
            onMouseLeave={() => {this.setState({hover:false});}} >
            <span className={classNames(hasUnread, "red")}>{unreadNotifications}</span> 
            <i className={
              classNames("fa", "fa-md", "navy", (this.state.hover || this.state.popover)?"fa-bell":"fa-bell-o")
            } />
          </a>
        </bs.OverlayTrigger>  
    );
  }
});

function NotificationList(props){
  const maxLength = NOTIFICATION_TITLE_LENGTH;
  return (
    <div className="notification-list">
    <ul className="list-unstyled">
      {
        props.notifications.map(function(notification) {
          const notificationClass = notification.acknowledgedOn ? 'read' : 'unread';
          return (
            <li key={notification.category+notification.id} className={notificationClass} >
              <a onClick={() => props.linkToNotification({id: notification.id, category:notification.category})}>
                {
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
function NotificationArea (props) {

  return (
    <div className="notification-area">
      <div className="notifications notification-item">
        <NotificationMenuItem 
          intl={props.intl}
          item={{
                    name: "notifications",
                    title: "section.notifications",
                    image: "images/svg/info.svg",
                    link: "#"
                    }}
            unreadNotifications={props.unreadNotifications}
            notifications={props.notifications}
            linkToNotification={props.linkToNotification}
        />
      </div>
        {
        /*
        <div className="settings notification-item">
        <DevicesMenuItem
          deviceCount={this.props.deviceCount}  
          intl={this.props.intl}  
          />
          </div>
        */
        }
    </div>  
  );
}

var Header = React.createClass({

   
  render: function() {
    const { intl, firstname, isAuthenticated, notifications, linkToNotification, unreadNotifications, logout, deviceCount, setLocale, locale } = this.props;
    //<MainMenu items={Constants.MAIN_MENU}/>
    return (
      <header className="site-header">
        {(() => isAuthenticated ? (
          <div>
            <MainLogo />
            <div className="top-header-right">
              <NotificationArea
                intl={intl}
                deviceCount={deviceCount}
                notifications={notifications} 
                unreadNotifications={unreadNotifications}
                linkToNotification={linkToNotification}
              />
              <UserInfo
                intl={intl}
                firstname={firstname}
                />
              <Logout
                intl={intl}   
                isAuthenticated={isAuthenticated}
                logout={logout}
                className="navbar logout"
                 />
            </div>
          </div>
          ) : (
          <div>
            <MainLogo />
            <div className="top-header-right">
              <LocaleSwitcher
                intl={intl}
                setLocale={setLocale}
                locale={locale}
              /> 
          </div>
        </div>
        ))() 
        }
      </header>
    );
  }
});




Header = injectIntl(Header);
Header.NotificationList = NotificationList;
module.exports = Header;

