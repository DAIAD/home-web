var types = require('../constants/ActionTypes');
var Helpers = require('../helpers/helpers');
var Schema = require('../constants/ModeManagementTableSchema');



var initialState = {
	isLoading: false,
	filterStatus: {},
	nameFilter: '',
	userToDecativate: {},
	modal: {
		show: false
	},
	changedModes: [],
	filterOptions: null,
	users: null,
	modes: null,
	activePage: 0
};

var computeModesState = function (data){
	var modesState = {};
	var propertyNames = Helpers.pluck(
					Helpers.pickQualiffied(data.fields, 'type', 'property'),
					'name'
				);
	var self = this;
	var rowIds = Helpers.pluck(data.rows, 'id');
	
	for (var i = 0, len = rowIds.length; i < len; i++){
		var modeEntry = {};
		modeEntry.active = data.rows[i].active;
		modeEntry.modes = {};
		for (var p = 0, len2 = propertyNames.length; p < len2; p++){
			var mode = {
				value: data.rows[i][propertyNames[p]],
				draft: false
			};
			modeEntry.modes[propertyNames[p]] = mode;
		}
		modesState[rowIds[i]] = modeEntry;
	}
	return modesState;
};


var mode_management = function(state, action) {
	let filterStatus; 
	
	switch (action.type) {
	
	case types.MODEMNG_FILTER_ADD:
		filterStatus = Object.assign({}, state.filterStatus);
		filterStatus[action.filter.filter] = {
			name: action.filter.filter,
			value: action.filter.value,
			label: action.filter.label,
			icon: action.filter.icon
		};
		return Object.assign({}, state, {
		  isLoading : true,
			filterStatus : filterStatus
		});

	case types.MODEMNG_FILTER_REMOVE:
		filterStatus = Object.assign({}, state.filterStatus);
		delete filterStatus[action.filterId];
		return Object.assign({}, state, {
		  isLoading : true,
			filterStatus : filterStatus
		});
		
	case types.MODEMNG_SET_MODAL:
		return Object.assign({}, state, {
			modal : action.modal
		});
		
	case types.MODEMNG_SET_CHANGED_MODES:
		return Object.assign({}, state, {
			changedModes : action.changedModes
		});
		
	case types.MODEMNG_SET_MODES:
		return Object.assign({}, state, {
			modes : action.modes
		});
		
	case types.MODEMNG_SET_ACTIVE_PAGE:
		return Object.assign({}, state, {
			activePage : action.activePage
		});
	
	case types.MODEMNG_RECEIVED_FILTER_OPTIONS:
		return Object.assign({}, state, {
			filterOptions : action.filterOptions.filterOptions
		});
		
	case types.MODEMNG_REQUEST_USERS:
		return Object.assign({}, state, {
			isLoading : true
		});
		
	case types.MODEMNG_RECEIVED_USERS:
		var users = {
			fields: Schema.fields,
			rows: [],
			pager: Schema.pager
		};
		users.rows = action.users.profileSet;
		var modes = computeModesState(users);
		return Object.assign({}, state, {
			isLoading : false,
			users: users,
			modes: modes
		});
		
	case types.MODEMNG_SAVE_MODE_CHANGES:
		return Object.assign({}, state, {
			isLoading : true,
		});
		
	case types.MODEMNG_DEACTIVATE_USER:
		return Object.assign({}, state, {
			isLoading : true,
		});
		
	case types.MODEMNG_SET_NAME_FILTER:
		return  Object.assign({}, state, {
			nameFilter : action.nameFilter,
			isLoading : true
		});	
	
	case types.MODEMNG_MARK_USER_DEACTIVATION:
		return Object.assign({}, state, {
			userToDecativate : action.userId
		});

	default:
		return state || initialState;
	}

};

module.exports = mode_management;