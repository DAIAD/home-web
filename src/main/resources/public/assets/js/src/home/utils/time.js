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

const getPeriod = function(period, timestamp=moment().valueOf()) {
  return {
    startDate: moment().startOf(period).valueOf(),
    endDate: Object.assign({}, moment(timestamp)).endOf(period).valueOf(),
    granularity: convertPeriodToGranularity(period)
  };
};

const getNextPeriod = function(period, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(period).add(1, period).valueOf(),
    endDate: Object.assign(moment(), moment(timestamp)).endOf(period).add(1, period).valueOf(),
    granularity: convertPeriodToGranularity(period)
  };
};

const getPreviousPeriod = function(period, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(period).subtract(1, period).valueOf(),
    endDate: moment(timestamp).endOf(period).subtract(1, period).valueOf(),
    granularity: convertPeriodToGranularity(period)
  };
};

const getPreviousPeriodSoFar = function(period, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(period).subtract(1, period).valueOf(),
    endDate: moment(timestamp).subtract(1, period).valueOf(),
    granularity: convertPeriodToGranularity(period)
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

const convertPeriodToGranularity = function (period) {
  if (period === "year") return 4;
  else if (period === "month") return 3;
  else if (period === "week") return 2;
  else if (period === "day") return 0;
  else return 0;
};

const convertGranularityToPeriod = function (granularity) {
  if (granularity === 4) return "year";
  else if (granularity === 3) return "month"; //(period === "month") return 3;
  else if (granularity === 2) return "week"; //(period === "week") return 2;
  else if (granularity === 1 || granularity === 0) return "day"; //(period === "day") return 0;
  else return "day";
};

const getTimeByPeriod = function (period) {
  if (period === "year") return thisYear();
  else if (period === "month") return thisMonth();
  else if (period === "week") return thisWeek();
  else if (period === "day") return today();
};

const getLastPeriod = function(period, timestamp) {
  return moment(timestamp).subtract(period, 1).get(period);
};

const getLastShowerTime = function () {
  return {
    startDate: moment().subtract(3, 'month').valueOf(),
    endDate: moment().valueOf(),
    granularity: 0
  };
};

const getGranularityByDiff = function(start, end) {
  const diff = moment.duration(end - start);

  const years = diff.years(); 
  const months = diff.months();
  const days = diff.days();
  const milliseconds = diff.milliseconds();

  if (years > 0 || months > 6) return 4;
  else if (months > 0) return 3;
  else if (days > 0) return 2;
  else return 0;
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
  getPreviousPeriodSoFar,
  getTimeByPeriod,
  convertGranularityToPeriod,
  getGranularityByDiff,
  getLastShowerTime,
  getLastPeriod,
};
