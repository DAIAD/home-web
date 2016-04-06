var modeManagementAPI = require('../api/mode_management');
var types = require('../constants/ActionTypes');

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

var sendDeactivateUserRequest = function(user){
	return {
		type: types.MODEMNG_DEACTIVATE_USER,
		user: user
	};
};

var sendSaveModesRequest = function(updatedModes){
	return {
		type: types.MODEMNG_SAVE_MODE_CHANGES,
		modes: updatedModes
	};
};

const ModeManagementActions = {

	addFilter: function(filter) {
		return {
			type: types.MODEMNG_FILTER_ADD,
			filter: filter
		};
	},
	
	removeFilter: function(filterId) {
		return {
			type: types.MODEMNG_FILTER_REMOVE,
			filterId: filterId
		};
	},
	
	setNameFilter: function (term) {
		return{
			type: types.MODEMNG_SET_NAME_FILTER,
			nameFilter: term
		};
	},
	
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
	
	fetchUsers: function(){
		return function(dispatch, getState) {
			dispatch(requestedUsers());
			return modeManagementAPI.fetchUsers().then(
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
					return modeManagementAPI.fetchUsers().then(
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
					return modeManagementAPI.fetchUsers().then(
						function (users){
							dispatch(receivedUsers(users));
						});
				}
			);
		};
	}
};

module.exports = ModeManagementActions;
