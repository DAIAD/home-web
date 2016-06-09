var moment = require('moment');

const last24Hours = function(timestamp) {
  return {
    startDate: moment(timestamp).subtract(24, 'hours').valueOf(),
    endDate: timestamp,
    granularity: 0
  };
};

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
    endDate: moment(timestamp).endOf(period).valueOf(),
    granularity: convertPeriodToGranularity(period)
  };
};

const getNextPeriod = function(period, timestamp=moment().valueOf()) {
  let sPeriod = period === 'isoweek' ? 'week' : period;
  return {
    startDate: moment(timestamp).startOf(period).add(1, sPeriod).valueOf(),
    endDate: moment(timestamp).endOf(period).add(1, sPeriod).valueOf(),
    granularity: convertPeriodToGranularity(period)
  };
};

const getPreviousPeriod = function(period, timestamp=moment().valueOf()) {
  let sPeriod = period === 'isoweek' ? 'week' : period;
  return {
    startDate: moment(timestamp).startOf(period).subtract(1, sPeriod).valueOf(),
    endDate: moment(timestamp).endOf(period).subtract(1, sPeriod).valueOf(),
    granularity: convertPeriodToGranularity(period)
  };
};

const getPreviousPeriodSoFar = function(period, timestamp=moment().valueOf()) {
  let sPeriod = period === 'isoweek' ? 'week' : period;
  return {
    startDate: moment(timestamp).startOf(period).subtract(1, sPeriod).valueOf(),
    endDate: moment(timestamp).subtract(1, sPeriod).valueOf(),
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

const getLowerGranularityPeriod = function(period) {
  if (period === 'year') return 'month';
  else if (period === 'month') return 'week';
  else if (period === 'week') return 'day';
  else if (period === 'day') return 'hour';
  else throw new Error('error in get lower granularity period with', period);
};

const getTimeByPeriod = function (period) {
  if (period === "year") return thisYear();
  else if (period === "month") return thisMonth();
  else if (period === "week") return thisWeek();
  else if (period === "day") return today();
};

const getLastPeriod = function(period, timestamp) {
  return moment(timestamp).subtract(period, 1).get(period).toString();
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



const timeToBuckets = function(time, oops) {
  const { startDate, endDate, granularity } = time;
  if (startDate == null || endDate == null || granularity == null) return [];
  // throw new Error('Need time object with startDate, endDate & granularity to make buckets');

  let period, aggrPeriod = null;
  if (oops === true) {
    period = 'second';
    aggrPeriod = 'second';
  }
  else {
    period = getLowerGranularityPeriod(convertGranularityToPeriod(granularity));

    aggrPeriod = period === 'hour' ? 'hour' : 'day';
  }
  const bucketCount = moment(endDate).add(1, 'second').diff(moment(startDate), period);

  let buckets = [];
  //let buckets = Array(bucketCount).fill(null).map(i => 
  let x = moment(startDate);
  for (let i=0; i<bucketCount; i++) {
    const t = x.endOf(period === 'week' ? 'isoweek' : period).startOf(aggrPeriod);
    buckets.push(t.valueOf());
    x = t.endOf(aggrPeriod).add(1, 'second').clone();
  }
  console.log('buckets', buckets);
  return buckets;
};

/*
const normalizeSessionTimestamp = function(session, period) {
  const normalized = Object.assign({}, session, {timestamp: moment(session.timestamp).startOf(period).valueOf()});
  return normalized;
};
const getPeriodFromTimestamp = function(period, timestamp=moment()) {
  return moment(timestamp).get(period);
};
*/
/*
const getBucketLabels = function(buckets, period) {
  if (period === 'month') {
    return buckets.map((b, i, a) => a.length > 12 ? moment(b).get('month') + '/' + moment(b).get('year') : moment(b).get('month'));
  }
  else if (period === 'week') {
    return buckets.map((b, i, a) => moment(b).get('week'));
  }
  else if (period === 'day') {
    return buckets.map((b, i, a) => a > 7 ? moment(b).get('day') + '/' + moment(b).get('month') : moment(b).get('day'));
  }
  else if (period === 'hour') {
    return buckets.map((b, i, a) => a > 24 ? moment(b).get('day') + 'T' + moment(b).get('hours') + ':' + moment(b).get('minutes') : moment(b).get('hours') + ':' + moment(b).get('minutes'));

  }
  else {
    throw new Error ('cannot get bucket labels from unrecognized period', period);
  }
};
*/

const addPeriodToSessions = function(sessions, period) {
  return sessions.map(session => Object.assign({}, session, {timestamp: period === 'month' ? moment(session.timestamp).add(1, period).startOf(getLowerGranularityPeriod(period)).valueOf() : moment(session.timestamp).add(1, period).valueOf() }));
};


module.exports = {
  defaultFormatter,
  selectTimeFormatter,
  last24Hours,
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
  timeToBuckets,
  //normalizeSessionTimestamp,
  getLowerGranularityPeriod,
  //getPeriodFromTimestamp,
  //getBucketLabels,
  addPeriodToSessions,
  //getPeriodByTimestamp,
};
