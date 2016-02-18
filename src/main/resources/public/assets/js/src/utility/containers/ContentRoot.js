var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { Link } = require('react-router');
var FormattedMessage = require('react-intl').FormattedMessage;
var Bootstrap = require('react-bootstrap');
var ScrollToTop = require('react-scroll-up');

var LoginForm = require('../components/LoginForm');
var LocaleSwitcher = require('../components/LocaleSwitcher');

var LeafletMap = require('../components/LeafletMap');

var { login, logout } = require('../actions/SessionActions');
var { setLocale } = require('../actions/LocaleActions');

var Collapsible = require('../components/Collapsible');

var expandSettings = function(e) {
	this.setState({ expandSettings : !this.state.expandSettings});
};

var ContentRoot = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

	getInitialState() {
		return {
			expandSettings: false
	    };
	},

	render: function() {
		var content = null;
		var _t = this.context.intl.formatMessage;
	
		if(!this.props.isAuthenticated) {
			content = (
				<div className='wrapper'>
					<nav className='navbar navbar-default navbar-fixed-top'>
						<div className='navbar-header' style={{ paddingLeft: 15}} >
							<a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
								<img alt='DAIAD' src='/assets/images/daiad-transparent.png' />
							</a>
						</div>
						<div style={{ float: 'right', marginTop: 8, marginLeft: 10, paddingRight: 15}}>
							<LocaleSwitcher locale={this.props.locale} onLocaleSwitch={this.props.actions.setLocale} />
						</div>
					</nav>
					<div className='login-wrapper'>
						<LoginForm action='login' 	isAuthenticated = { this.props.isAuthenticated } 
													errors = {this.props.session.errors}
													onLogin = {this.props.actions.login} />
					</div>
				</div>
			);
		} else {
			content = (
				<div className='wrapper' style={{ backgroundColor : '#f8f8f8'}} >
					<nav className='navbar navbar-default navbar-fixed-top'>
						<div className='navbar-header' style={{ paddingLeft: 15}} >
							<a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
								<img alt='DAIAD' src='/assets/images/daiad-transparent.png' />
							</a>
						</div>
						<div style={{ float: 'right', marginTop: 8, marginLeft: 10, paddingRight: 45}}>
							<button id="logout"
				   				type="submit"
				   				className="btn btn-primary"
		   						style={{ width: 80, height: 33 }}
			   					onClick={this.props.actions.logout}>
				   				<FormattedMessage id="LoginForm.button.signout" />
				   			</button>
						</div>
						<div style={{ float: 'right', marginTop: 8, marginLeft: 10, paddingRight: 15}}>
							<LocaleSwitcher locale={this.props.locale} onLocaleSwitch={this.props.actions.setLocale} />
						</div>
						<div className='navbar-default navbar-static-side' role='navigation'>
							<div className='sidebar-collapse'>
		                    	<ul className='nav' id='side-menu'>
	                            	<li>
                            			<Link to='/'>
	                            			<i className="fa fa-dashboard fa-fw"></i>{' ' + _t({ id: 'Section.Dashboard'})}
                            			</Link>
	                            	</li>
	                            	<li>
		                            	<a href='#'>
		                            		<i className="fa fa-bar-chart fa-fw"></i>{' ' + _t({ id: 'Section.Analytics'})}
	                            		</a>
	                    			</li>
	                            	<li>
	                            		<a href='#'>
	                            			<i className="fa fa-line-chart fa-fw"></i>{' ' + _t({ id: 'Section.Forecasting'})}
	                        			</a>
	                    			</li>
	                    			<li>
	                    				<Link to='/demographics'>
	                    					<i className="fa fa-group fa-fw"></i>{' ' + _t({ id: 'Section.Demographics'})}
	                					</Link>
	            					</li>
	            					<li>
	            						<a href='#'>
	            							<i className="fa fa-search fa-fw"></i>{' ' + _t({ id: 'Section.Search'})}
	            						</a>
	            					</li>
	            					<li>
	            						<a href='#'>
	            							<i className="fa fa-bell fa-fw"></i>{' ' + _t({ id: 'Section.Alerts_Notifications'})} <Bootstrap.Badge className='danger'>4</Bootstrap.Badge>
	            						</a>
	            					</li>
	            					<li>
	            						<a href='#' onClick={expandSettings.bind(this)}>
	            							<i className="fa fa-cogs fa-fw"></i>
	            							{' ' + _t({ id: 'Section.Settings'})}
	            							{ this.state.expandSettings ? (<i className="fa fa-caret-up fa-fw"></i>) : (<i className="fa fa-caret-down fa-fw"></i>)} 
	            						</a>
	            						<Collapsible open={this.state.expandSettings}>
	            							<ul className='nav'>
		                    					<li>
		                    						<Link to='/settings/user'>
				            							<span  style={{paddingLeft: 18}}>
				            								<i className="fa fa-user fa-fw"></i>{' ' + _t({ id: 'Settings.User'})}
			            								</span>
				            						</Link>
				            					</li>
				            					<li>
				            						<Link to='/settings/system'>
				            							<span  style={{paddingLeft: 18}}>
				            								<i className="fa fa-server fa-fw"></i>{' ' + _t({ id: 'Settings.System'})}
			            								</span>
				            						</Link>
				            					</li>
				                        	</ul>
	            						</Collapsible>
	            					</li>
	            					<li>
	            						<a href='#'>
	            							<i className="fa fa-database fa-fw"></i>{' ' + _t({ id: 'Section.Reporting'})}
	            						</a>
	            					</li>
	            					<li>
	            						<a href='#'>
	            							<i className="fa fa-download fa-fw"></i>{' ' + _t({ id: 'Section.Export'})}
	            						</a>
	            					</li>
	                        	</ul>
	                    	</div>
	                	</div>
					</nav>
					<div className='page-wrapper'>
						{this.props.children}
					</div>
					<ScrollToTop showUnder={160}>
					<i className="fa fa-arrow-circle-o-up fa-4x fa-fw" style={{ color : '#337ab7'}}></i>
					</ScrollToTop>
				</div>
			);
		}
		
		return content;
  }
});


function mapStateToProps(state) {
	return {
	    isAuthenticated: state.session.isAuthenticated,
	    session: {
	    	errors: state.session.errors
	    },
	    routing: state.routing
	};
}

function mapDispatchToProps(dispatch) {
	return {
		actions : bindActionCreators(Object.assign({}, { login, logout, setLocale}) , dispatch)
	};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(ContentRoot);
