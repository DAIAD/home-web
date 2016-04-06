var callAPI = require('./base');

var ModeManagementAPI = {
		fetchUsers: function() {
			return callAPI('/assets/js/build/utility/testData/randomUsers.js');
		},
		saveModeChanges : function(){
			return callAPI('/assets/js/build/utility/testData/randomUsers.js');
		},
		deactivateUser : function(user){
			return callAPI('/assets/js/build/utility/testData/randomUsers.js');
		}
	};

module.exports = ModeManagementAPI;
