var moment = require('moment');

const today = function() {
	return {
		startDate: moment().startOf('day').valueOf(),
		endDate: moment().endOf('day').add(3, 'hours').valueOf()
	};
};

const thisWeek = function() {
	return {
		startDate: moment().startOf('week').valueOf(),
		endDate: moment().endOf('week').valueOf()
	};
};

const thisMonth = function() {
	return {
		startDate: moment().startOf('month').valueOf(),
		endDate: moment().endOf('month').valueOf()
  };
};

const thisYear = function() {
	return {
    startDate: moment().startOf('year').valueOf(),
    endDate: moment().endOf('year').valueOf()
	};
};

function getPeriod(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment().startOf(granularity).valueOf(),
    endDate: Object.assign({}, moment(timestamp)).endOf(granularity).valueOf()
  };
}
function getNextPeriod(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(granularity).add(1, granularity).valueOf(),
    endDate: Object.assign(moment(), moment(timestamp)).endOf(granularity).add(1, granularity).valueOf()
  };
}
function getPreviousPeriod(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(granularity).subtract(1, granularity).valueOf(),
    endDate: moment(timestamp).endOf(granularity).subtract(1, granularity).valueOf()
  };
}

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
  thisYear,
  getPeriod,
  getNextPeriod,
  getPreviousPeriod
};
