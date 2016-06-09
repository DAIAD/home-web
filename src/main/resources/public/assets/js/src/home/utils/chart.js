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
const getChartMeterCategories = function (period) {
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

const getChartMeterCategories = function(time) {
  return timeToBuckets(time);
};

const getChartAmphiroCategories = function (period) {
  if (period === 'ten') {
    return Array.from({length: 10}, (v, i) => 10-i);
  }
  else if (period === 'twenty') {
    return Array.from({length: 20}, (v, i) => 20-i); 
  }
  else if (period === 'fifty') {
    return Array.from({length: 50}, (v, i) => 50-i);
  }
  else return [];
};

/*
const getChartTimeDataByFilter = function (data, filter, period, intl) {
  return getChartCategoriesByPeriod(period, intl).map((v, i) =>
            data.find(session => getPeriodByTimestamp(period, session.timestamp) === i) ? (data[i] == null ? null : data[i][filter]) : null);
            };
            */


//TODO: have to make sure data is ALWAYS fetched in order of ascending ids for amphiro, ascending timestamps for meters

const getChartAmphiroData = function (sessions, xAxisData, metric) {
  /*
  //if not x axis data then x axis time
    if (xAxisData === null) {
      return data.map(session => session[filter] == null ? [] :
                        [new Date(session.timestamp), session[filter]]);
    }
    //else x axis is category
    else {
    */
    return xAxisData.map((v, i) =>
      sessions[i] ? sessions[i][metric] : null);
      // }
};

const getChartTimeData = function (sessions, metric) {
  //if not x axis data then x axis time
  //  if (xAxisData === null) {
      return sessions.map(session => session[metric] == null ? [] :
                        [new Date(session.timestamp), session[metric]]);
                        //}
    //else x axis is category
    //else {
    //return xAxisData.map((v, i) =>
    //  sessions[i] ? sessions[i][metric] : null);
    // }
};



const getChartMeterData = function(sessions, xAxisData, metric, time) {

  const period = getLowerGranularityPeriod(convertGranularityToPeriod(time.granularity));
  console.log('period =', period);
  //const sessionsToBuckets = function(sessions, buckets, metric, period) {
  return xAxisData
  .map(t => {
    const bucketSession = sessions.find(session => {
      
      const tt = (period === 'hour' ? moment(session.timestamp).startOf('hour').valueOf(): session.timestamp);
      
      return tt === t;
    });
    
    return bucketSession && bucketSession[metric] != null ? bucketSession[metric] : null;
    
  });
};

const getChartMetadata = function (data, xAxisData) {
  //if (xAxisData === null) {
  //  return data.map(session => [session.id, session.timestamp]);
  //}
  //else {
    return xAxisData.map((v, i) => 
      data[i] ? [data[i].id, data[i].timestamp] : []);
      //}
};

module.exports = {
  //getChartTimeDataByFilter,
  getChartMeterData,
  getChartAmphiroData,
  getChartTimeData,
  getChartMeterCategories,
  getChartAmphiroCategories,
  getChartMetadata,
  //sessionsToBuckets
};
