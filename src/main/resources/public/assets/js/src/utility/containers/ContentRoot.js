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

var expandConsumers= function(e) {
	this.setState({ expandConsumers : !this.state.expandConsumers});
};
 
var expandMessages = function(e) {
	this.setState({ expandMessages : !this.state.expandMessages});
};

var expandSettings = function(e) {
	this.setState({ expandSettings : !this.state.expandSettings});
};

var ContentRoot = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

	getInitialState() {
		return {
			expandMessages: false,
			expandSettings: false,
			expandConsumers: false
	    };
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
								<img alt='DAIAD' src='/assets/images/daiad-logo.svg' style={{ marginTop: 15 }} />
							</a>
						</div>
						<div style={{ float: 'right', marginTop: 8, marginLeft: 10, paddingRight: 15}}>
							<LocaleSwitcher locale={this.props.locale} onLocaleSwitch={this.props.actions.setLocale} />
						</div>
					</nav>
					<div>
						<LoginForm action='login' 	isAuthenticated = { this.props.isAuthenticated } 
													errors = {this.props.session.errors}
													onLogin = {this.props.actions.login}
													isLoading = {this.props.session.isLoading}/>
					</div>
				</div>
			);
		} else {
			content = (
				<div className='wrapper'>
					<nav className='navbar navbar-default navbar-fixed-top'>
						<div className='navbar-header' style={{ paddingLeft: 15 }} >
							<a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
								<img alt='DAIAD' src='/assets/images/utility/daiad-logo.svg' style={{ marginTop: 15 }} />
							</a>
						</div>
						<div style={{ float: 'right', marginTop: 8, marginLeft: 10, paddingRight: 45}}>
							<button id='logout'
				   				type='submit'
				   				className='btn btn-primary'
		   						style={{ height: 33 }}
			   					onClick={this.props.actions.logout}>
				   				<FormattedMessage id='LoginForm.button.signout' />
				   			</button>
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
	                            		<Link to='/analytics'>
		                            		<i className='fa fa-bar-chart fa-fw'></i>{' ' + _t({ id: 'Section.Analytics'})}
	                            		</Link>
	                    			</li>
	                            	<li>
	                            		<Link to='/forecasting'>
	                            			<i className='fa fa-line-chart fa-fw'></i>{' ' + _t({ id: 'Section.Forecasting'})}
	                        			</Link>
	                    			</li>
	            					<li>
	            						<a href='#' onClick={expandConsumers.bind(this)}>
	            							<i className='fa fa-group fa-fw'></i>
	            							{' ' + _t({ id: 'Section.Consumers'}) + ' '}
	            							{ this.state.expandConsumers ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
	            						</a>
	            						<Collapsible open={this.state.expandConsumers}>
	            							<ul className='nav'>
		                    					<li>
		                    						<Link to='/demographics'>
				            							<span  style={{paddingLeft: 18}}>
				            								<i className='fa fa-bookmark fa-fw'></i>{' ' + _t({ id: 'Section.Demographics'})}
			            								</span>
				            						</Link>
				            					</li>
				            					<li>
				            						<Link to='/search'>
				            							<span  style={{paddingLeft: 18}}>
				            								<i className='fa fa-search fa-fw'></i>{' ' + _t({ id: 'Section.Search'})}
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
				                        	</ul>
	            						</Collapsible>
	            					</li>
		            				<li>
		            					<Link to='/scheduler'>
		            						<i className='fa fa-clock-o fa-fw'></i>{' ' + _t({ id: 'Section.Scheduler'})}
		            					</Link>
		            				</li>
	            					<li>
	            						<a href='#' onClick={expandMessages.bind(this)}>
	            							<i className='fa fa-comments-o fa-fw'></i>
	            							{' ' + _t({ id: 'Section.Messages'}) + ' '}
	            							<Bootstrap.Badge className='danger'>4</Bootstrap.Badge>
	            							{ this.state.expandMessages ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
	            						</a>
	            						<Collapsible open={this.state.expandMessages}>
	            							<ul className='nav'>
		                    					<li>
		                    						<Link to='/alerts'>
				            							<span  style={{paddingLeft: 18}}>
				            								<i className='fa fa-bell fa-fw'></i>{' ' + _t({ id: 'Section.Alerts'})}
			            								</span>
				            						</Link>
				            					</li>
				            					<li>
				            						<Link to='/announcements'>
				            							<span  style={{paddingLeft: 18}}>
				            								<i className='fa fa-volume-up fa-fw'></i>{' ' + _t({ id: 'Section.Announcements'})}
			            								</span>
				            						</Link>
				            					</li>
				                        	</ul>
	            						</Collapsible>
	            					</li>
	            					<li>
		            					<a href='#' onClick={expandSettings.bind(this)}>
		            					<i className='fa fa-cogs fa-fw'></i>
		            					{' ' + _t({ id: 'Section.Settings'})}
		            					{ this.state.expandSettings ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)} 
		            				</a>
		            				<Collapsible open={this.state.expandSettings}>
		            					<ul className='nav'>
		            						<li>
		            							<Link to='/settings/user'>
		            								<span  style={{paddingLeft: 18}}>
		            									<i className='fa fa-user fa-fw'></i>{' ' + _t({ id: 'Settings.User'})}
		            								</span>
		            							</Link>
		            						</li>
		            						<li>
		            							<Link to='/settings/system'>
		            								<span  style={{paddingLeft: 18}}>
		            									<i className='fa fa-server fa-fw'></i>{' ' + _t({ id: 'Settings.System'})}
		            								</span>
		            							</Link>
		            						</li>
		            					</ul>
		            				</Collapsible>
		            				</li>
		            				<li>
		            					<Link to='/report'>
		            						<i className='fa fa-database fa-fw'></i>{' ' + _t({ id: 'Section.Reporting'})}
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
						<div style={{marginRight: -25}}>
							<i className='fa fa-arrow-up fa-2x fa-fw' style={{ color : '#337ab7'}}></i>
						</div>
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
	    	errors: state.session.errors,
	    	isLoading: state.session.isLoading
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
