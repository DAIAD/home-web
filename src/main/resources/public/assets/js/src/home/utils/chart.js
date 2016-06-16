var moment = require('moment');
var { convertGranularityToPeriod, getLowerGranularityPeriod, timeToBuckets } = require('./time');
//var { getPeriodFromTimestamp, timeToBuckets } = require('./time');

const getCount = function (metrics) {
  return metrics.count?metrics.count:1;
};

const getTimestampIndex = function (points, timestamp) {
    return points.findIndex((x) => (x[0]===timestamp));
};

//TODO: complete this±
/*
const getChartMeterCategories = function (time) {
  if (period === 'year') {
    return Array.from({length: 12}, (v, i) => i);
  }
  else if (period === 'month') {
    return Array.from({length: 4}, (v, i) => i);
  }
  else if (period === 'week') {
    return Array.from({length: 7}, (v, i) => i);
  }
  else if (period === 'day') {
    return Array.from({length: 24}, (v, i) => i);
  }
  else return [];
};
*/
/*
   
const getChartMeterCategories = function (period, intl) {
  if (period === 'year') {
    //    return Array.from({length: 12}, (v, i) => i);
    return Array.from({length: 12}, (v, i) => intl.formatMessage({id:`months.${i}`}));
  }
  else if (period === 'month') {
    //    return Array.from({length: 4}, (v, i) => i);
     return Array.from({length: 4}, (v, i) => `Week ${i+1}`);
  }
  else if (period === 'week') {
    //    return Array.from({length: 7}, (v, i) => i);
    return Array.from({length: 7}, (v, i) => intl.formatMessage({id: `weekdays.${i}`}));
  }
  else if (period === 'day') {
    //    return Array.from({length: 24}, (v, i) => i);
    return Array.from({length: 24}, (v, i) => `${i}:00`);
  }
  else return [];
};
*/

const getChartMeterCategories = function(time) {
  return timeToBuckets(time);
};

const getChartMeterCategoryLabels = function(xData, time, intl) {
  if (!time || time.granularity == null || !intl) return [];

  return xData.map(t => getTimeLabelByGranularityShort(t, time.granularity, intl));

};

const getTimeLabelByGranularity = function (timestamp, granularity, intl) {

  if (granularity === 4) {
    return intl.formatMessage({id: `months.${moment(timestamp).get('month')}`}) + ' ' + moment(timestamp).format('YYYY'); 
  }
  else if (granularity === 3) {
    return intl.formatMessage({id: 'periods.week'}) + ' ' + moment(timestamp).get('isoweek') + ', ' + intl.formatMessage({id: 'months.'+moment(timestamp).get('month')}) + ', ' + moment(timestamp).format('YYYY');
  }
  else if (granularity === 2) {
    return intl.formatMessage({id: `weekdays.${moment(timestamp).get('day')}`}) + ',  ' + moment(timestamp).format(' DD / MM / YYYY');
  }
  else {
    return intl.formatMessage({id: `weekdays.${moment(timestamp).get('day')}`}) + ',  ' + moment(timestamp).format('DD/ MM/ YYYY hh:mm a');
  }
};

const getTimeLabelByGranularityShort = function (timestamp, granularity, intl) {

  if (granularity === 4) {
    return intl.formatMessage({id: `months.${moment(timestamp).get('month')}`}); 
  }
  else if (granularity === 3) {
    return intl.formatMessage({id: 'periods.week'}) + ' ' + moment(timestamp).get('isoweek');
  }
  else if (granularity === 2) {
    return intl.formatMessage({id: `weekdays.${moment(timestamp).get('day')}`});
  }
  else {
    return moment(timestamp).format('hh:mm');
  }
};

const getChartAmphiroCategories = function (period) {
  if (period === 'ten') {
    //return Array.from({length: 10}, (v, i) => 10-i);
    return Array.from({length: 10}, (v, i) => `#${10-i}`);
  }
  else if (period === 'twenty') {
    //return Array.from({length: 20}, (v, i) => 20-i); 
    return Array.from({length: 20}, (v, i) => `#${20-i}`);
  }
  else if (period === 'fifty') {
    //return Array.from({length: 50}, (v, i) => 50-i);
    return Array.from({length: 50}, (v, i) => `#${50-i}`);
  }
  else return [];
};

/*
const getChartTimeDataByFilter = function (data, filter, period, intl) {
  return getChartCategoriesByPeriod(period, intl).map((v, i) =>
            data.find(session => getPeriodByTimestamp(period, session.timestamp) === i) ? (data[i] == null ? null : data[i][filter]) : null);
            };
            */


const getChartTimeData = function (sessions, metric) {
    return sessions.map(session => session[metric] == null ? [] :
                      [new Date(session.timestamp), session[metric]]);
};

//TODO: have to make sure data is ALWAYS fetched in order of ascending ids for amphiro, ascending timestamps for meters

const getChartAmphiroData = function (sessions, xAxisData, metric) {
   return xAxisData.map((v, i, arr) => {
     return (i >= (arr.length - sessions.length)) && sessions[i-(arr.length-sessions.length)] ? sessions[i-(arr.length-sessions.length)][metric] : null;
    });
};

const getChartMeterData = function(sessions, xAxisData, metric, time) {

  const period = getLowerGranularityPeriod(convertGranularityToPeriod(time.granularity));
  //const sessionsToBuckets = function(sessions, buckets, metric, period) {
  return xAxisData.map(t => {
      const bucketSession = sessions.find(session => {
        
        const tt = (period === 'hour' ? moment(session.timestamp).startOf('hour').valueOf() : session.timestamp);
        return tt === t;
      });
      
      return bucketSession && bucketSession[metric] != null ? bucketSession[metric] : null;
      
    });
};

const getChartMetadata = function (sessions, xAxisData, timeBased=true) {
  if (timeBased) {
    return xAxisData.map((v, i) => {
      const index = sessions.findIndex(x => moment(x.timestamp).startOf('hour').valueOf() === v);
      return index > -1 ? [sessions[index].id, sessions[index].timestamp] : [];
    });
  }
  else {
    return xAxisData.map((v, i, arr) => {
      return (i >= (arr.length - sessions.length)) && sessions[i-(arr.length-sessions.length)] ? [sessions[i-(arr.length-sessions.length)].id, sessions[i-(arr.length-sessions.length)].timestamp] : [null, null];
      //return data[i] ? [data[i].id, data[i].timestamp] : [null, null];
    });
  }

};

module.exports = {
  //getChartTimeDataByFilter,
  getChartMeterData,
  getChartAmphiroData,
  getChartTimeData,
  getChartMeterCategories,
  getChartMeterCategoryLabels,
  getChartAmphiroCategories,
  getChartMetadata,
  getTimeLabelByGranularity,
  getTimeLabelByGranularityShort,
  //sessionsToBuckets
};
