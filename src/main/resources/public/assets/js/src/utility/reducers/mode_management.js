var types = require('../constants/ActionTypes');
var Helpers = require('../helpers/helpers');


var initialState = {
	usersFetchingInProgress: false,
	filterStatus: {},
	nameFilter: '',
	userToDecativate: {},
	modal: {
		show: false
	},
	changedModes: [],
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
			filterStatus : filterStatus
		});

	case types.MODEMNG_FILTER_REMOVE:
		filterStatus = Object.assign({}, state.filterStatus);
		delete filterStatus[action.filterId];
		return Object.assign({}, state, {
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
		
	case types.MODEMNG_REQUEST_USERS:
		return Object.assign({}, state, {
			usersFetchingInProgress : true
		});
		
	case types.MODEMNG_RECEIVED_USERS:
		var users = {
			fields: [{
				name: 'id',
				title: 'Table.User.id',
				hidden: true
			}, {
				name: 'active',
				title: 'Table.User.active',
				hidden: true
			},{
				name: 'name',
				title: 'Table.User.name',
				link: '/user/{id}'
			}, {
				name: 'group',
				title: 'Table.User.group',
				link: '/group/{id}'
			}, {
				name: 'b1',
				title: 'Table.User.viewInfoOnB1',
				type:'property'
			}, {
				name: 'mobile',
				title: 'Table.User.viewInfoOnMobile',
				type:'property'
			}, {
				name: 'social',
				title: 'Table.User.allowSocial',
				type:'property'
			}, {
				name: 'deactivate',
				title: 'Table.User.deactivateUser',
				type:'action',
				icon: 'user-times',
				handler: null
	
			}],
			rows: [],
			pager: {
				index: 1,
				size: 10
			}
		};
		
		users.rows = action.users;
		var modes = computeModesState(users);
		return Object.assign({}, state, {
			usersFetchingInProgress : false,
			users: users,
			modes: modes
		});
		
		
	case types.MODEMNG_SET_NAME_FILTER:
		return  Object.assign({}, state, {
			nameFilter : action.nameFilter
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