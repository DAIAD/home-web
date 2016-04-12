var modeManagementAPI = require('../api/mode_management');
var types = require('../constants/ActionTypes');

var prepareFiltersPayload = function (nameFilter, filterStatus){
	
	var truthTable = {
		true: "ON",
		false: "OFF",
	};
	
	var results = {};
	results.nameFilter = nameFilter;
	for (var key in filterStatus){
		if (typeof filterStatus[key].value === "boolean"){
			results[key] = truthTable[filterStatus[key].value];
		} else {
			results[key] = filterStatus[key].value;
		}
	}
	return results;
};

var requestedTest = function (){
	return {
		type: types.MODEMNG_TEST_REQUEST
		
	};
};

var receivedTest = function (data){
	return {
		type: types.MODEMNG_TEST_RECEIVE,
		data: data
	};
};


var requestedFilterOptions = function (){
	return {
		type: types.MODEMNG_REQUEST_FILTER_OPTIONS
	};
};

var receivedFilterOptions = function (options){
	return {
		type: types.MODEMNG_RECEIVED_FILTER_OPTIONS,
		filterOptions: options
	};
};

var requestedUsers = function (){
	return {
		type: types.MODEMNG_REQUEST_USERS
	};
};

var receivedUsers = function (users){
	return {
		type: types.MODEMNG_RECEIVED_USERS,
		users: users
	};
};

var setNameFilter = function (term) {
	return{
		type: types.MODEMNG_SET_NAME_FILTER,
		nameFilter: term
	};
};

var addFilter = function(event, filter) {
	return {
		type: types.MODEMNG_FILTER_ADD,
		filter: filter
	};
};

var removeFilter = function(filterId) {
	return {
		type: types.MODEMNG_FILTER_REMOVE,
		filterId: filterId
	};
};

var sendDeactivateUserRequest = function(){
	return {
		type: types.MODEMNG_DEACTIVATE_USER
	};
};

var sendSaveModesRequest = function(updatedModes){
	return {
		type: types.MODEMNG_SAVE_MODE_CHANGES,
	};
};

const ModeManagementActions = {

	markUserForDeactivation: function(userId){
		return {
			type: types.MODEMNG_MARK_USER_DEACTIVATION,
			userId: userId
		};
	},
	
	setModal: function(modal){
		return {
			type: types.MODEMNG_SET_MODAL,
			modal: modal
		};
	},
	
	setChangedModes: function(changedModes){
		return {
			type: types.MODEMNG_SET_CHANGED_MODES,
			changedModes: changedModes
		};
	},
	
	setModes: function(modes){
		return {
			type: types.MODEMNG_SET_MODES,
			modes: modes
		};
	},
	
	setActivePage: function(activePage){
		return {
			type: types.MODEMNG_SET_ACTIVE_PAGE,
			activePage: activePage
		};
	},
	
	fetchFilterOptions: function(){
		return function(dispatch, getState){
			dispatch(requestedFilterOptions());
			return modeManagementAPI.fetchFilterOptions().then(
				function (filterOptions) {
					dispatch(receivedFilterOptions(filterOptions));
				});
		};
	},
	
	fetchUsers: function(){
		return function(dispatch, getState) {
			dispatch(requestedUsers());
			var filters = prepareFiltersPayload(getState().mode_management.nameFilter, getState().mode_management.filterStatus);
			return modeManagementAPI.fetchUsers(filters).then(
				function(users) {
					dispatch(receivedUsers(users));
				});
		};
	},
	
	applyNameFilter: function (term){
		return function(dispatch, getState){
			dispatch(setNameFilter(term));
			var filters = prepareFiltersPayload(getState().mode_management.nameFilter, getState().mode_management.filterStatus);
			return modeManagementAPI.fetchUsers(filters).then(
				function(users) {
					dispatch(receivedUsers(users));
				});
		};
		
	},
	
	applyAddFilter: function (event, filter){
		return function(dispatch, getState){
			dispatch(addFilter(event, filter));
			var filters = prepareFiltersPayload(getState().mode_management.nameFilter, getState().mode_management.filterStatus);
			return modeManagementAPI.fetchUsers(filters).then(
				function(users) {
					dispatch(receivedUsers(users));
				});
		};
		
	},
	
	applyRemoveFilter: function (filterId){
		return function(dispatch, getState){
			dispatch(removeFilter(filterId));
			var filters = prepareFiltersPayload(getState().mode_management.nameFilter, getState().mode_management.filterStatus);
			return modeManagementAPI.fetchUsers(filters).then(
				function(users) {
					dispatch(receivedUsers(users));
				});
		};
		
	},
	
	deactivateUser: function(user){
		return function(dispatch, getState) {
			dispatch(sendDeactivateUserRequest());
			modeManagementAPI.deactivateUser(user).then(
				function(){
					var filters = prepareFiltersPayload(getState().mode_management.nameFilter, getState().mode_management.filterStatus);
					return modeManagementAPI.fetchUsers(filters).then(
						function (users){
							dispatch(receivedUsers(users));
						});
				}
			);
		};
	},
	
	saveModeChanges: function(modes){
		return function(dispatch, getState) {
			dispatch(sendSaveModesRequest());
			modeManagementAPI.saveModeChanges(modes).then(
				function(){
					var filters = prepareFiltersPayload(getState().mode_management.nameFilter, getState().mode_management.filterStatus);
					return modeManagementAPI.fetchUsers(filters).then(
						function (users){
							dispatch(receivedUsers(users));
						});
				}
			);
		};
	},
	
	
	test: function(input){
		return function(dispatch, getState) {
			dispatch(requestedTest());
			return modeManagementAPI.testCall(input).then(
				function(data) {
					dispatch(receivedTest(data));
				});
		};
	},
};

module.exports = ModeManagementActions;
