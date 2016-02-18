
var today = function() {
	var start = new Date();
	start.setDate(start.getDate() -1);
	start.setHours(0,0,0,0);
	return {
		startDate: start.getTime(),
		endDate: new Date().getTime()
	};
};

var thisWeek = function() {
	var start = new Date();
	var currDay = start.getDay();
	start.setDate(start.getDate() - currDay);
	start.setHours(0,0,0,0);
	return {
		startDate: start.getTime(),
		endDate: new Date().getTime()
	};
};

var thisMonth = function() {
	var start = new Date();
	start.setDate(1);
	start.setHours(0,0,0,0);
	return {
		startDate: start.getTime(),
		endDate: new Date().getTime()
	};
};

var thisYear = function() {
	var start = new Date();
	start.setDate(1);
	start.setMonth(0);
	start.setHours(0,0,0,0);
	return {
		startDate: start.getTime(),
		endDate: new Date().getTime()
	};
};

module.exports = {
	today,
	thisWeek,
	thisMonth,
	thisYear
};
