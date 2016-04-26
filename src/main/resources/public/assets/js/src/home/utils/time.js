var moment = require('moment');

const today = function() {
  return {
    startDate: moment().startOf('day').valueOf(),
    endDate: moment().endOf('day').valueOf(),
    granularity: 0
  };
};

const thisWeek = function() {
  return {
    startDate: moment().startOf('isoweek').valueOf(),
    endDate: moment().endOf('isoweek').valueOf(),
    granularity: 2
  };
};
const thisMonth = function() {
  return {
    startDate: moment().startOf('month').valueOf(),
    endDate: moment().endOf('month').valueOf(),
    granularity: 3
  };
};

const thisYear = function() {
  return {
    startDate: moment().startOf('year').valueOf(),
    endDate: moment().endOf('year').valueOf(),
    granularity: 4
  };
};

const getPeriod = function(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment().startOf(granularity).valueOf(),
    endDate: Object.assign({}, moment(timestamp)).endOf(granularity).valueOf(),
    granularity: convertGranularityToInt(granularity)
  };
};

const getNextPeriod = function(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(granularity).add(1, granularity).valueOf(),
    endDate: Object.assign(moment(), moment(timestamp)).endOf(granularity).add(1, granularity).valueOf(),
    granularity: convertGranularityToInt(granularity)
  };
};

const getPreviousPeriod = function(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(granularity).subtract(1, granularity).valueOf(),
    endDate: moment(timestamp).endOf(granularity).subtract(1, granularity).valueOf(),
    granularity: convertGranularityToInt(granularity)
  };
};

const defaultFormatter = function(timestamp){
  const date = new Date(timestamp);
  return `${date.getDate()}/${date.getMonth()+1}/${date.getFullYear()}`;
};

const selectTimeFormatter = function(key, intl) {
  switch (key) {
    case "always":
      return (x) => intl.formatDate(x);
    case "year":
      return (x) => intl.formatDate(x, { day: 'numeric', month: 'long', year: 'numeric' });
    case "month":
      return (x) => intl.formatDate(x, { day: 'numeric', month: 'short' });
    case "week":
      return (x) => intl.formatMessage({id: "weekdays." + (new Date(x).getDay()+1).toString()});
    case "day":
      return (x) => intl.formatTime(x, { hour: 'numeric', minute: 'numeric'});
    default:
      return (x) => intl.formatDate(x);
  }
};

const convertGranularityToInt = function (granularity) {
  if (granularity === "year") return 4;
  else if (granularity === "month") return 3;
  else if (granularity === "week") return 2;
  else if (granularity === "day") return 0;
  else return 0;
};

const convertIntToGranularity = function (int) {
  if (int === 4) return "year";
  else if (int === 3) return "month"; //(granularity === "month") return 3;
  else if (int === 2) return "week"; //(granularity === "week") return 2;
  else if (int === 1 || int === 0) return "day"; //(granularity === "day") return 0;
  else return "day";
};

const getTimeByPeriod = function (period) {
  if (period === "year") return thisYear();
  else if (period === "month") return thisMonth();
  else if (period === "week") return thisWeek();
  else if (period === "day") return today();
};

const getLastShowerTime = function () {
  return {
    startDate: moment().subtract(1, 'week').valueOf(),
    endDate: moment().valueOf(),
    granularity: 0
  };
};

module.exports = {
  defaultFormatter,
  selectTimeFormatter,
  today,
  thisWeek,
  thisMonth,
  thisYear,
  getPeriod,
  getNextPeriod,
  getPreviousPeriod,
  getTimeByPeriod,
  convertIntToGranularity,
  getLastShowerTime
};
