var api = require('./base');

var ModeManagementAPI = {
		fetchUsers: function(filters) {
			return api.json('/action/profile/modes/list', filters);
		},
		
		fetchFilterOptions: function(){
			return api.json('/action/profile/modes/filter/options');
		},
		
		saveModeChanges : function(data){
			return api.json('/action/profile/modes/save', data);
		},
		
		deactivateUser : function(user){
			return api.json('/action/profile/deactivate', user);
		},
		
		testCall : function(user){
			return api.json('/action/profile/deactivate', user);
		},
	};

module.exports = ModeManagementAPI;
