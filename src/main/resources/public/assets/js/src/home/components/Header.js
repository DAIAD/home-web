var React = require('react');
var classNames = require('classnames');
var bs = require('react-bootstrap');
var { Link } = require('react-router');
var { injectIntl, FormattedMessage } = require('react-intl');

var Constants = require('../constants/HomeConstants');
var LocaleSwitcher = require('./LocaleSwitcher');
var { Logout } = require('./LoginForm');

const { STATIC_RECOMMENDATIONS } = require('../constants/HomeConstants'); 

/* DAIAD Logo */
function MainLogo() {
  return (
    <Link to="/"  className="logo" activeClassName="selected">
      <img src="/assets/images/svg/daiad-logo2.svg" alt="DAIAD Logo"
        title="DAIAD"/>
    </Link>
  );
}


/* Main Menu */
function MenuItem(props) {
  return (
    <li>
      <Link to={props.item.route} className="menu-item" activeClassName="selected">
        <img className="menu-icon" src={`/assets/${props.item.image}`} />
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
          <span>{props.firstname}</span>
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
            <NotificationList notifications={this.props.notifications} />
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
              classNames("fa", "fa-md", "white", (this.state.hover || this.state.popover)?"fa-bell":"fa-bell-o")
            } />
          </a>
        </bs.OverlayTrigger>  
    );
  }
});

function NotificationList(props){
  const maxLength = Constants.NOTIFICATION_TITLE_LENGTH;
  return (
    <div className="notification-list">
    <ul className="list-unstyled">
      {
        props.notifications.map(function(notification) {
          const notificationClass = notification.unread?"unread":"read";
          return (
            <li key={notification.id} className={notificationClass} >
              <Link to={`/notifications/${notification.id}`} >
                {(notification.title.length>maxLength)?(`${notification.title.substring(0, maxLength).trim()}...`):notification.title}
              </Link>
            </li>
          );
          })
    }
  </ul>
  </div>
  );
}

function NotificationArea (props) {
  let unreadNotifications = 0;
  props.notifications.forEach(function(notification) {
    if (notification.unread){
      unreadNotifications += 1;
    } 
  });
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
            unreadNotifications={unreadNotifications}
            notifications={props.notifications}
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
    const { intl, firstname, isAuthenticated, logout, deviceCount, setLocale, locale } = this.props;
    return (
      <header className="site-header">
        {(() => {
          if (isAuthenticated) {
            return (
              <div className="container">
                <div className="header-left">
                  <MainLogo />
                  <MainMenu items={Constants.MAIN_MENU}/>
                </div>
                <div className="header-right">
                  <NotificationArea
                    intl={intl}
                    deviceCount={deviceCount}
                    notifications={STATIC_RECOMMENDATIONS} />
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
              );
          }
          else{
            return (
              <div className="container">
                <div className="pull-left">
                  <MainLogo />
                </div>
                <div className="pull-right">
                  <LocaleSwitcher
                    intl={intl}
                    setLocale={setLocale}
                    locale={locale}
                  /> 

                </div>
              </div>
              );
          }
        })() 
        }
      </header>
    );
  }
});




Header = injectIntl(Header);
Header.NotificationList = NotificationList;
module.exports = Header;

