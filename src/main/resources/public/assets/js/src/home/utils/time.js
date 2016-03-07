var moment = require('moment');

function today() {
  return {
    startDate: moment().startOf('day').valueOf(),
    endDate: moment().endOf('day').add(3, 'hours').valueOf()
  };
}

function thisWeek() {
  return {
    startDate: moment().startOf('week').valueOf(),
    endDate: moment().endOf('week').valueOf()
  };
}

function thisMonth() {
  return {
    startDate: moment().startOf('month').valueOf(),
    endDate: moment().endOf('month').valueOf()
  };
}

function thisYear() {
  return {
    startDate: moment().startOf('year').valueOf(),
    endDate: moment().endOf('year').valueOf()
  };
}

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

function defaultFormatter (timestamp){
  const date = new Date(timestamp);
  return (date.getDate() + '/' +
          (date.getMonth()+1) + '/' +
          date.getFullYear());
}

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
