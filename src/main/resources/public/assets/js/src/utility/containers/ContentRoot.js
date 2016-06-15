const develop = (process.env.NODE_ENV !== 'production');

var React = require('react');
var ReactDOM = require('react-dom');
var {bindActionCreators} = require('redux');
var {connect} = require('react-redux');
var {Link} = require('react-router');
var FormattedMessage = require('react-intl').FormattedMessage;
var Bootstrap = require('react-bootstrap');
var ScrollToTop = require('react-scroll-up');

var LoginForm = require('../components/LoginForm');
var LocaleSwitcher = require('../components/LocaleSwitcher');

var {login, logout} = require('../actions/SessionActions');
var {setLocale} = require('../actions/LocaleActions');
var {configure} = require('../actions/config');

var Collapsible = require('../components/Collapsible');

var disableLink = function(e) {
  e.preventDefault();
};

var ContentRoot = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },

  getInitialState() {
    return {
      expand: {
        consumers: false,
        trials: false,
        support: false,
        analytics: false,
        alerts: false
      },
    };
  },

  _toggleExpand: function (itemKey) {
    this.setState((prevState) => {
      var expanded = prevState.expand;
      return {
        expand: Object.assign({}, expanded, {[itemKey]: !expanded[itemKey]})
      };
    });
  },

  render: function() {
    var content = null;

    var _t = this.context.intl.formatMessage;

    if(!this.props.isAuthenticated) {
      content = (
        <div className='login-wrapper'>
          <nav className='navbar navbar-default navbar-fixed-top'>
            <div className='navbar-header' style={{ paddingLeft: 15}} >
              <a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
                <img alt='DAIAD' src='/assets/images/shared/daiad-logo.svg' style={{ marginTop: 15 }} />
              </a>
            </div>
            <div style={{ float: 'right', marginTop: 8, marginLeft: 10, paddingRight: 15}}>
              <LocaleSwitcher locale={this.props.locale} onLocaleSwitch={this.props.actions.setLocale} />
            </div>
          </nav>
          <div>
            <LoginForm 
              action='login'
              isAuthenticated={this.props.isAuthenticated} 
              errors={this.props.session.errors}
              onLogin={this.props.actions.login}
              isLoading={this.props.session.isLoading}
             />
          </div>
        </div>
      );
    } else {

      var development = null;
      if(develop) {
        development = (
          <li>
            <Link to='/support/development'>
              <span  style={{paddingLeft: 18}}>
                <i className='fa fa-bug fa-fw'></i>{' ' + _t({ id: 'Section.Support.Development'})}
              </span>
            </Link>
          </li>
        );
      }

      content = (
        <div className='wrapper'>
          <nav className='navbar navbar-default navbar-fixed-top'>
            <div className='navbar-header' style={{ paddingLeft: 15 }} >
              <a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
                <img alt='DAIAD' src='/assets/images/shared/daiad-logo.svg' style={{ marginTop: 15 }} />
              </a>
            </div>
            <div style={{float: 'right', marginTop: 12, marginLeft: 10, paddingRight: 45}}>
              <span style={{marginRight: 10}}>
                {this.props.session.username}
              </span>
              <i className='fa fa-sign-out fa-fw' style={{color : '#d9534f', cursor : 'pointer'}} onClick={this.props.actions.logout}></i>
            </div>
            <div className='navbar-default navbar-static-side' role='navigation'>
              <div className='sidebar-collapse'>
                <ul className='nav' id='side-menu'>
                  <li>
                    <Link to='/'>
                      <i className='fa fa-dashboard fa-fw'></i>{' ' + _t({ id: 'Section.Dashboard'})}
                    </Link>
                  </li>
                  
                  <li>
                    <a href='#' onClick={() => this._toggleExpand('analytics')}>
                      <i className='fa fa-bar-chart fa-fw'></i>
                      {' ' + _t({ id: 'Section.Analytics-Group'}) + ' '}
                      { this.state.expand.analytics ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
                    </a>
                    <Collapsible open={this.state.expand.analytics}>
                      <ul className='nav'>
                        <li>
                          <Link to='/analytics/panel'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-area-chart fa-fw'></i>{' ' + _t({ id: 'Section.Analytics.ReportPanel'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/analytics/maps' onClick={disableLink} className='disabled-link'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-map-o fa-fw'></i>{' ' + _t({ id: 'Section.Analytics.Maps'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/analytics/basic-reports'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-file-text fa-fw'></i>{' ' + _t({ id: 'Section.Analytics.BasicReports'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/analytics/fav' onClick={disableLink} className='disabled-link'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-diamond fa-fw'></i>{' ' + _t({ id: 'Section.Analytics.Fav'})}
                            </span>
                          </Link>
                        </li>
                      </ul>
                    </Collapsible>
                  </li>

                  <li>
                    <Link to='/forecasting'>
                      <i className='fa fa-line-chart fa-fw'></i>{' ' + _t({ id: 'Section.Forecasting'})}
                    </Link>
                  </li>
                  <li>
                    <a href='#' onClick={() => this._toggleExpand('consumers')}>
                      <i className='fa fa-home fa-fw'></i>
                      {' ' + _t({ id: 'Section.Consumers'}) + ' '}
                      { this.state.expand.consumers ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
                    </a>
                    <Collapsible open={this.state.expand.consumers}>
                      <ul className='nav'>
                        <li>
                          <Link to='/users'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-user fa-fw'></i>{' ' + _t({ id: 'Section.Users'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/groups'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-group fa-fw'></i>{' ' + _t({ id: 'Section.Demographics'})}
                            </span>
                          </Link>
                        </li>
                      </ul>
                    </Collapsible>
                  </li>
                  <li>
                    <Link to='/scheduler'>
                      <i className='fa fa-clock-o fa-fw'></i>{' ' + _t({ id: 'Section.Scheduler'})}
                    </Link>
                  </li>
                  
                  <li>
                    <a href='#' onClick={() => this._toggleExpand('alerts')}>
                      <i className='fa fa-commenting-o fa-fw'></i>
                      {' ' + _t({ id: 'Section.ManageAlerts.Engagement'}) + ' '}
                      { this.state.alerts ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
                    </a>
                    <Collapsible open={this.state.expand.alerts}>
                      <ul className='nav'>
                        <li>
                          <Link to='/manage-alerts'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-list-ol fa-fw'></i>{' ' + _t({ id: 'Section.ManageAlerts.Messages'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/announcements'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-wechat fa-fw'></i>{' ' + _t({ id: 'Section.ManageAlerts.Announcements'})}
                            </span>
                          </Link>
                        </li>
                      </ul>
                    </Collapsible>
                  </li>  
                  <li>
                    <a href='#' onClick={() => this._toggleExpand('trials')}>
                      <i className='fa fa-flask fa-fw'></i>
                      {' ' + _t({ id: 'Section.Trials.Group'}) + ' '}
                      { this.state.expand.trials ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
                    </a>
                    <Collapsible open={this.state.expand.trials}>
                      <ul className='nav'>
                        <li>
                          <Link to='/trials/overview'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-table fa-fw'></i>{' ' + _t({id: 'Section.Trials.Overview'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/trials/pilot-reports'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-pie-chart fa-fw'></i>{' ' + _t({id: 'Section.Trials.PilotReports'})}
                            </span>
                          </Link>
                        </li>
                      </ul>
                    </Collapsible>
                  </li>
                  
                  <li>
                    <a href='#' onClick={() => this._toggleExpand('support')}>
                      <i className='fa fa-support fa-fw'></i>
                      {' ' + _t({ id: 'Section.Support.Group'}) + ' '}
                      { this.state.expand.support ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
                    </a>
                    <Collapsible open={this.state.expand.support}>
                      <ul className='nav'>
                        <li>
                          <Link to='/support/logging'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-history fa-fw'></i>{' ' + _t({ id: 'Section.Support.Logging'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/mode/management'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-sliders fa-fw'></i>{' ' + _t({ id: 'Section.ModeManagement'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/support/data'>
                            <span  style={{paddingLeft: 18}}>
                            <i className='fa fa-database fa-fw'></i>{' ' + _t({ id: 'Section.Support.Data'})}
                            </span>
                          </Link>
                        </li>
                        {development}
                      </ul>
                    </Collapsible>
                  </li>
                  <li>
                    <Link to='/settings/user'>
                      <i className='fa fa-user fa-fw'></i>{' ' + _t({ id: 'Settings.User'})}
                    </Link>
                  </li>
                </ul>
              </div>
            </div>
          </nav>
          <div className='page-wrapper'>
            {this.props.children}
          </div>
          <ScrollToTop showUnder={160}>
            <div style={{marginRight: -30}}>
              <i className='fa fa-arrow-up fa-2x fa-fw' style={{ color : '#337ab7'}}></i>
            </div>
          </ScrollToTop>
        </div>
      );
    }
    
    return content;
  },

  componentDidMount: function () {
    if (this.props.isAuthenticated) {
      this.props.actions.configure();
    }
  },

  componentDidUpdate: function (prevProps, prevState) {
  
    // Detect a succesfull login, and try to configure the client side. 
    // This usually includes requesting configuration parts from the server side.
    if (!prevProps.isAuthenticated && this.props.isAuthenticated) {
      this.props.actions.configure();
    }
    
    // Todo On a succesfull logout, we should probably deconfigure the client 
    // (if the configuration holds any security-sensitive parts).
  },

});

function mapStateToProps(state) {
  return {
      isAuthenticated: state.session.isAuthenticated,
      session: {
        errors: state.session.errors,
        isLoading: state.session.isLoading,
        username: (state.session.profile ? state.session.profile.username : '')
      },
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(
      Object.assign({}, {login, logout, setLocale, configure}), 
      dispatch
    )
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(ContentRoot);

