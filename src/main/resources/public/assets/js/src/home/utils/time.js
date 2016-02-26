const today = function() {
	var start = new Date();
	start.setHours(0,0,0,0);
	var end = new Date();
	end.setHours(23, 59, 59, 999);
	return {
		startDate: start.getTime(),
		endDate: end.getTime()
	};
};

const thisWeek = function() {
	var start = new Date();
	var currDay = start.getDay();
	start.setDate(start.getDate() - currDay);
	start.setHours(0,0,0,0);
	var end = new Date();
	end.setDate(start.getDate() + 6);
	end.setHours(23,59,59,999);
	return {
		startDate: start.getTime(),
		endDate: end.getTime()
	};
};

const thisMonth = function() {
	var start = new Date();
	start.setDate(1);
	start.setHours(0,0,0,0);
	var end = new Date();
	end.setMonth(start.getMonth() + 1);
	end.setHours(23,59,59,999);
	end.setDate(0);
	return {
		startDate: start.getTime(),
		endDate: end.getTime()
	};
};

const thisYear = function() {
	var start = new Date();
	start.setDate(1);
	start.setMonth(0);
	start.setHours(0,0,0,0);

	var end = new Date(new Date().getFullYear(), 11, 31);
	//end.setHours(23,59,59,999);
	return {
		startDate: start.getTime(),
		endDate: end.getTime()
	};
};

const defaultFormatter = function(timestamp){
	const date = new Date(timestamp);
	return (date.getDate() + '/' +
					(date.getMonth()+1) + '/' +
					date.getFullYear());
};

module.exports = {
	defaultFormatter,
	today,
	thisWeek,
	thisMonth,
	thisYear
};
