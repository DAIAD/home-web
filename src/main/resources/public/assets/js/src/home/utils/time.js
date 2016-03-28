var moment = require('moment');

const today = function() {
  return {
    startDate: moment().startOf('day').valueOf(),
    endDate: moment().endOf('day').valueOf()
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

const getPeriod = function(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment().startOf(granularity).valueOf(),
    endDate: Object.assign({}, moment(timestamp)).endOf(granularity).valueOf()
  };
};
const getNextPeriod = function(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(granularity).add(1, granularity).valueOf(),
    endDate: Object.assign(moment(), moment(timestamp)).endOf(granularity).add(1, granularity).valueOf()
  };
};

const getPreviousPeriod = function(granularity, timestamp=moment().valueOf()) {
  return {
    startDate: moment(timestamp).startOf(granularity).subtract(1, granularity).valueOf(),
    endDate: moment(timestamp).endOf(granularity).subtract(1, granularity).valueOf()
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

module.exports = {
  defaultFormatter,
  selectTimeFormatter,
  today,
  thisWeek,
  thisMonth,
  thisYear,
  getPeriod,
  getNextPeriod,
  getPreviousPeriod
};
