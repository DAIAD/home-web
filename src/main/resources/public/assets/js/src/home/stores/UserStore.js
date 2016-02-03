var events = require('events');
var assign = require('object-assign');

var $ = require('jquery');

var AppDispatcher = require('../dispatcher/AppDispatcher');

var HomeConstants = require('../constants/HomeConstants');

var LOGIN_EVENT = 'LOGIN';
var LOGOUT_EVENT = 'LOGOUT';

var PROFILE_REFRESH = 'PROFILE_REFRESH';
var PROFILE_UPDATE = 'PROFILE_UPDATE';

var _model = {
	profile : null
};

function updateCsrfToken(crsf) {
	$('meta[name=_csrf]').attr('content', crsf);
	$('input[name=_csrf]').val(crsf);
}

function login(username, password) {
	_model = {
		profile : null
	};

	var data = {
		username : username,
		password : password,
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	var request = {
		type : "POST",
		url : '/login?application=home',
		data : data,
		beforeSend : function(xhr) {
			xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
					'content'));
		}
	};
	$.ajax(request).done(function(data, textStatus, request) {
		updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
		
		if (data.success) {
			_model.profile = data.profile;
			
			UserStore.emitLogin({ success: true });
		} else {
			UserStore.emitLogin({ success: false });
		}
	}).fail(function(jqXHR, textStatus, errorThrown) {
		updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));

		switch (jqXHR.status) {
		case 403:
			break;
		}
		
		UserStore.emitLogin({ success: false });
	});
}

function logout() {
	var data = {
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	var request = {
		type : "POST",
		url : '/logout',
		data : data,
		beforeSend : function(xhr) {
			xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
					'content'));
		}
	};
	
	$.ajax(request).done(function(data, textStatus, request) {
		updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
		
		_model.profile = null;

		UserStore.emitLogout();
	}).fail(function(jqXHR, textStatus, errorThrown) {
		updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
		
		switch (jqXHR.status) {
		case 403:
			break;
		}
		
		_model.user = null;
		
		UserStore.emitLogout();
	});
}

function profileRefresh() {
	var data = {
			_csrf : $('meta[name=_csrf]').attr('content')
		};

		var request = {
			type : "GET",
			url : '/action/profile',
			beforeSend : function(xhr) {
				xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
						'content'));
			}
		};
		
		$.ajax(request).done(function(data, textStatus, request) {
			updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
			
			if(data.success) {
				_model.profile = data.profile;
				
			} else {
				_model.profile = null;
				
			}

			UserStore.emitProfileRefresh();
		}).fail(function(jqXHR, textStatus, errorThrown) {
			updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
			
			switch (jqXHR.status) {
			case 403:
				break;
			}
			
			_model.profile = null;
		
			UserStore.emitProfileRefresh();
		});	
}

function profileUpdate(profile) {
	var data = {
			_csrf : $('meta[name=_csrf]').attr('content')
		};

		var request = {
			type : "GET",
			url : '/action/profile_update',
			beforeSend : function(xhr) {
				xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
						'content'));
			}
		};
		//_model.profile = assign({}, profile);	
		_model.profile = profile;
		UserStore.emitProfileUpdate();
		console.log('updating profile...');
		console.log(profile);
		//TODO: API Call not ready
		/*
		$.ajax(request).done(function(data, textStatus, request) {
			updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
			
			if(data.success) {
				_model.profile = data.profile;
				
			} else {
				_model.profile = null;
				
			}

			//UserStore.emitProfileRefresh();
		}).fail(function(jqXHR, textStatus, errorThrown) {
			updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
			
			switch (jqXHR.status) {
			case 403:
				break;
			}
			
			_model.profile = null;
		
			//UserStore.emitProfileRefresh();
			});	
			profileRefresh();
			*/
}
var UserStore = assign({}, events.EventEmitter.prototype, {

	isAuthenticated : function() {
		if (_model.profile) {
			return true;
		}
		return false;
	},
	getProfile: function() {
		//TODO
		//Why do i need to do this?
		//otherwise _model.profile is mutable 
		return assign({}, _model.profile);
	},
	addLoginListener : function(callback) {
		this.on(LOGIN_EVENT, callback);
	},

	removeLoginListener : function(callback) {
		this.removeListener(LOGIN_EVENT, callback);
	},

	addLogoutListener : function(callback) {
		this.on(LOGOUT_EVENT, callback);
	},

	removeLogoutListener : function(callback) {
		this.removeListener(LOGOUT_EVENT, callback);
	},

	addProfileRefreshListener : function(callback) {
		this.on(PROFILE_REFRESH, callback);
	},

	removeProfileRefreshListener : function(callback) {
		this.removeListener(PROFILE_REFRESH, callback);
	},

	addProfileUpdateListener : function(callback) {
		this.on(PROFILE_UPDATE, callback);
	},

	removeProfileUpdateListener : function(callback) {
		this.removeListener(PROFILE_UPDATE, callback);
	},

	emitLogin: function(args) {
		this.emit(LOGIN_EVENT, args);
	},
	
	emitLogout: function() {
		this.emit(LOGOUT_EVENT);
	},
	
	emitProfileRefresh: function() {
		this.emit(PROFILE_REFRESH);
	},

	emitProfileUpdate: function() {
		this.emit(PROFILE_UPDATE);
	},
	
	
});

AppDispatcher.register(function(message) {
	var action = message.action;

	switch (action) {
		case HomeConstants.USER_LOGIN:
			login(message.data.username, message.data.password);
			break;

		case HomeConstants.USER_LOGOUT:
			logout();
			break;
		
		case HomeConstants.PROFILE_REFRESH:
			profileRefresh();
			break;

		case HomeConstants.PROFILE_UPDATE:
			profileUpdate(message.data.profile);
			break;

		default:
			break;
		}
});

module.exports = UserStore;
