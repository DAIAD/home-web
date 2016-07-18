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

function Loader (props) {
  return (
    <div>
      <img className="preloader" src={`${PNG_IMAGES}/preloader-counterclock.png`} />
      <img className="preloader-inner" src={`${PNG_IMAGES}/preloader-clockwise.png`} />
    </div>
    );
}

//function HomeRoot (props) {
var HomeRoot = React.createClass({
  componentWillMount: function() {
    const { init, ready } = this.props;

    if (!ready) {
      init();
    }
  },
  render: function() {
    const { ready, locale, loading, user, deviceCount, messages, unreadNotifications, linkToNotification, login, logout, setLocale, errors, dismissError, children } = this.props;
    if (!ready) {
      return <Loader/>;
    }
    return (
      <IntlProvider 
        locale={locale.locale}
        messages={locale.messages} >
        <div className='site-container'>
          {
            loading ? <Loader/> : <div/> 
          }
          <Header
            firstname={user.profile.firstname}
            deviceCount={deviceCount}
            isAuthenticated={user.isAuthenticated}
            notifications={messages}
            unreadNotifications={unreadNotifications}
            linkToNotification={linkToNotification}
            locale={locale.locale}
            logout={logout} 
            setLocale={setLocale}
            errors={errors}
            dismissError={dismissError}
          />

        <div className = "main-container">
          
          {(() => user.isAuthenticated ? 
            <MainSidebar menuItems={MAIN_MENU} />
            :
            <MainSidebar menuItems={[]}/>
          )()}

          {(() => user.isAuthenticated ? 
           children
           :
            <LoginPage 
              isAuthenticated = {user.isAuthenticated}
              errors = {user.status.errors}
              login = {login}
              logout = {logout} />
          )()}
        </div>

            <Footer />
          </div>
        </IntlProvider>
    );
  }
});

module.exports = HomeRoot;
