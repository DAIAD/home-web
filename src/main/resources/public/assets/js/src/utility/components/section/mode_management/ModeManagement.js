var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var RandomUsers = require('../../../testData/Users');
var ModeManagementComponnent = require('./ModeManagementComponent');

var Helpers = require('../../../helpers/helpers');

var ModeManagementActions = require('../../../actions/ModeManagementActions');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var $ = require('jquery');

var ModeManagement = React.createClass({
		
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	componentWillMount : function() {
		var self = this;
		console.log('Now fetch filterOptions.....');
		this.props.fetchFilterOptions();
		console.log('Now fetch users.....');
		this.props.fetchUsers({nameFilter: ''});		
		console.log('Component ModeManagement will mount now.....');
	},
	
	filters: [{
		id: 'groupName',
		name: 'Group',
		field: 'groupName',
		icon: 'group',
		type: 'text',
	}, {
		id: 'amphiro',
		name: 'b1',
		field: 'amphiro',
		icon: 'tachometer',
		type: 'boolean',		
	}, {
		id: 'mobile',
		name: 'Mobile',
		field: 'mobile',
		icon: 'mobile',
		type: 'boolean',
	}, {
		id: 'social',
		name: 'Social',
		field: 'social',
		icon: 'share',
		type: 'boolean',
	}],	
	
  	render: function() {  		  
  		console.log('RENDERING ModeManagement....................');
  		
  		var self = this;
  		if (!this.props.usersFetchingInProgress && this.props.users){
  			console.log('-----------CASE RESULTS----------');
  			$.each(this.filters, function (i, f){
  				var optionsList = self.props.filterOptions[f.id];
  				var options = {};
  				$.each(optionsList, function(j, o){
  					var o_key = o;
  					if (o === "NOT_APPLICABLE"){
  						o_key = "NOT APPLICABLE";
  					} 
  					options[o_key] = o;
  				});
  				f.options = options;
  				
  			});
  								
	  		return (
				<ModeManagementComponnent
					filters={this.filters}
					routes={this.props.routes}
				/>
	 		);
  		} else {
  			console.log('-----------CASE SPINNER----------');
  			return (
  				<div>
                    <img className="preloader" src="/assets/images/utility/preloader-counterclock.png" />
                    <img className="preloader-inner" src="/assets/images/utility/preloader-clockwise.png" />
                </div>

  			);
  		}
  	}
});

ModeManagement.icon = 'sliders';
ModeManagement.title = 'Section.ModeManagement';

function mapStateToProps(state) {
	return {
		filterOptions: state.mode_management.filterOptions,
		users: state.mode_management.users,
		modes: state.mode_management.modes,
		usersFetchingInProgress: state.mode_management.usersFetchingInProgress,
	};
}


function mapDispatchToProps(dispatch) {
	return {
		fetchFilterOptions : bindActionCreators(ModeManagementActions.fetchFilterOptions, dispatch),
		fetchUsers : bindActionCreators(ModeManagementActions.fetchUsers, dispatch),
		test : bindActionCreators(ModeManagementActions.test, dispatch)
	};
}


module.exports = connect(mapStateToProps, mapDispatchToProps)(ModeManagement);

