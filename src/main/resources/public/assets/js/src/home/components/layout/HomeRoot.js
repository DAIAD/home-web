// Dependencies
var React = require('react');
var ReactDOM = require('react-dom');
var { IntlProvider, FormattedMessage } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { Link } = require('react-router');

//Constants
var { IMAGES, PNG_IMAGES, MAIN_MENU } = require('../../constants/HomeConstants');

// Components
var Header = require('./Header');
var MainSection = require('./MainSection');
var Footer = require('./Footer');
var LoginPage = require('../sections/Login');

// Actions
var { login, logout } = require('../../actions/UserActions');
var { setLocale } = require('../../actions/LocaleActions');

var { getDeviceCount } = require('../../utils/device');

function MainSidebar (props) {
  const { menuItems } = props;
  return (
    <aside className="main-sidebar">
      {
        menuItems.map(item =>  
                      <div key={item.name} className="menu-group">
                        <Link className="menu-item" to={item.route}><img src={`${IMAGES}/${item.image}`}/><FormattedMessage id={item.title}/></Link>
                        {
                          item.children.map(child =>
                                            <Link key={child.name} className="menu-item menu-subitem" to={child.route}><img src={`${IMAGES}/${child.image}`}/><FormattedMessage id={child.title}/></Link>
                                            )
                        }
                      </div>
        )
      }
    </aside>
  );
} 

function ErrorDisplay (props) {
  return props.errors ? 
    <div className='error-display'>
      <a onClick={() => props.dismissError()} className='error-display-x'>x</a>
      <img src={`${IMAGES}/alert.svg`} /><span className="infobox-error">{`${props.errors}`}</span>
    </div>
    :
     (<div/>);
}

function HomeRoot (props) {
  return (
    <IntlProvider 
      locale={props.locale.locale}
      messages={props.locale.messages} >
      <div className='site-container'>
        {
          (() => {
            if (props.loading){
              return (
                <div>
                  <img className="preloader" src={`${PNG_IMAGES}/preloader-counterclock.png`} />
                  <img className="preloader-inner" src={`${PNG_IMAGES}/preloader-clockwise.png`} />
                </div>
                );
            }
            })()
        }
        <Header
          firstname={props.user.profile.firstname}
          deviceCount={props.deviceCount}
          isAuthenticated={props.user.isAuthenticated}
          notifications={props.messages}
          unreadNotifications={props.unreadNotifications}
          linkToNotification={props.linkToNotification}
          locale={props.locale.locale}
          logout={props.logout} 
          setLocale={props.setLocale}
        />

      <div className = "main-container">
        <ErrorDisplay 
          dismissError={props.dismissError}
          errors={props.errors} 
        />
        {(() => props.user.isAuthenticated ? 
          <MainSidebar menuItems={MAIN_MENU} />
          :
          <MainSidebar menuItems={[]}/>
        )()}

        {(() => props.user.isAuthenticated ? 
         props.children
         :
          <LoginPage 
            isAuthenticated = {props.user.isAuthenticated}
            errors = {props.user.status.errors}
            login = {props.login}
            logout = {props.logout} />
        )()}
      </div>

          <Footer />
        </div>
      </IntlProvider>
    );
}
module.exports = HomeRoot;
