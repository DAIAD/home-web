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
      <ul className='main-menu-side'>
      {
        menuItems.map(item =>  
                      <li key={item.name} className='menu-item'>
                        <Link to={item.route}>{(() => item.image ? (<div style={{float: 'left', minWidth:25}}><img src={`${IMAGES}/${item.image}`}/></div>) : null)()}
                          <FormattedMessage id={item.title}/></Link>
                        {
                          item.children && item.children.length ? (<ul className='menu-group'>
                            {
                              item.children.map((child,idx) =>
                                               <li key={idx} className="menu-subitem">
                                                 <Link key={child.name} to={child.route}>
                                                   {(() => child.image ? (<img src={`${IMAGES}/${child.image}`}/>) : null)()}
                                                   <FormattedMessage id={child.title}/></Link>
                                               </li>)
                                               }
                                               </ul>)
                                            : null
                        }
                    </li>
        )
      }
    </ul>
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
