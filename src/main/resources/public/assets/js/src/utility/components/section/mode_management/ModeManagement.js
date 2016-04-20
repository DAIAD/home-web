var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var ModeManagementComponnent = require('./ModeManagementComponent');

var ModeManagementActions = require('../../../actions/ModeManagementActions');
var Schema = require('../../../constants/ModeManagementTableSchema');

var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var ModeManagement = React.createClass({
		
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	componentWillMount : function() {
		var self = this;
		this.props.fetchFilterOptions();
		this.props.fetchUsers({nameFilter: ''});		
	},
	
  	render: function() {  		  
  		var self = this;
  		
  		var filters = Schema.filters;
  		
  		if (!this.props.isLoading && this.props.users){
  		  filters.forEach(function(f, i){

    				var optionsList = self.props.filterOptions[f.id];
    				var options = {};
    				optionsList.forEach(function(o, j){
    					var o_key = o;
    					if (o === 'NOT_APPLICABLE'){
    						o_key = 'NOT APPLICABLE';
    					} 
    					options[o_key] = o;
    				});
    				f.options = options;
  		  });
  								
	  		return (
				<ModeManagementComponnent
					filters={filters}
					routes={this.props.routes}
				/>
	 		);
  		} else {
  			return (
  				<div>
            <img className='preloader' src='/assets/images/utility/preloader-counterclock.png' />
            <img className='preloader-inner' src='/assets/images/utility/preloader-clockwise.png' />
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
		isLoading: state.mode_management.isLoading,
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

