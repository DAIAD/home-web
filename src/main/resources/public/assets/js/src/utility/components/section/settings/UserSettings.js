var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var FormattedMessage = require('react-intl').FormattedMessage;
var Bootstrap = require('react-bootstrap');
var LocaleSwitcher = require('../../../components/LocaleSwitcher');
var { setLocale } = require('../../../actions/LocaleActions');


var Breadcrumb = require('../../Breadcrumb');

var UserSettings = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
  	render: function() {
  		console.log('RENDERING UserSettings ...........................');
  		var _t = this.context.intl.formatMessage;

  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				<div className="row">
					<div className="col-md-12">
						<LocaleSwitcher locale={this.props.locale} onLocaleSwitch={this.props.actions.setLocale} />
					</div>
				</div>
			</div>
 		);
  	}
});

UserSettings.icon = 'user';
UserSettings.title = 'Settings.User';

function mapStateToProps(state) {
	return {
		locale: state.i18n.locale
	};
}

function mapDispatchToProps(dispatch) {
	return {
		actions : bindActionCreators(Object.assign({}, { setLocale }) , dispatch)
	};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(UserSettings);
