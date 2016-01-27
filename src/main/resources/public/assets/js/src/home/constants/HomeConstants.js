var keyMirror = require('keymirror');

var constants = keyMirror({
	USER_LOGIN : null,
	USER_LOGOUT : null,

	LOCALE_CHANGE : null,

	PROFILE_REFRESH : null
});
constants.STATIC = "/assets/artwork/";

constants.data = {
	user: {
			name: 'userakos',
		},
		notifications: [{
				id: "Not1",
				type: "alert",
				title: "You are wasting too much water!",
				description: "...we are watching you"
			},
			{
				id: "Not2",
				type: "tip",
				title: "You can save money",
				description: "...by turning off the tap now and then"},
			{
				id: "Not3",
				type: "insight",
				title: "Did you know that you can help save the planet?",
				description: "...by saving up as much water as possible!"
			},
			{
				id: "Not4",
				type: "insight",
				title: "Save some water?",
				description: "...we are starting to repeat ourselves..."
			},
			{
				id: "Not5",
				type: "alert",
				unread: true,
				title: "This is the last time we ever send you a message",
				description: "Please stop ignoring us"
			},
			{
				id: "Not6",
				type: "alert",
				unread: true,
				title: "A water snake is drinking your water",
				description: "Watch out because this can lead to increased bills and cause you discomfort."
			},
		]
	};
module.exports = constants;

