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
		console.log('Now fetch users.....');
		this.props.fetchUsers();
		console.log('Component ModeManagement will mount now.....');
	},
	
	filters: [{
		id: 'group',
		name: 'Group',
		field: 'group',
		icon: 'group',
		type: 'text',
	}, {
		id: 'b1',
		name: 'b1',
		field: 'b1',
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
			var distinctGroups = Helpers.getDistinctValuesArrayObjects(this.props.users.rows, 'group');
			var distinctGroupOptions = {};
			$.each(distinctGroups, function (i, group){
				distinctGroupOptions[group] = group;
			});
			var binaryOptions = {
				'On': true,
				'Off': false
			};
			$.each(this.filters, function (i, f){
				if (f.id === 'group'){
					f.options = distinctGroupOptions;
				} else {
					f.options = binaryOptions;
				}
			});
					
	  		return (
				<ModeManagementComponnent
					filters={this.filters}
					routes={this.props.routes}
				/>
	 		);
  		} else {
  			
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
		users: state.mode_management.users,
		modes: state.mode_management.modes,
		usersFetchingInProgress: state.mode_management.usersFetchingInProgress,
	};
}


function mapDispatchToProps(dispatch) {
	return {
		fetchUsers : bindActionCreators(ModeManagementActions.fetchUsers, dispatch)
	};
}


module.exports = connect(mapStateToProps, mapDispatchToProps)(ModeManagement);

