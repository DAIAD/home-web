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
  if (!time || time.granularity == null) return [];

  if (time.granularity === 4) {
    return xData.map(t => intl.formatMessage({id: `months.${moment(t).get('month')}`})); 
  }
  else if (time.granularity === 3) {
    return xData.map(t => `${intl.formatMessage({id: 'periods.week'})} ${moment(t).get('isoweek')}`);
  }
  else if (time.granularity === 2) {
    return xData.map(t => intl.formatMessage({id: `weekdays.${moment(t).get('day')}`}));
  }
  else {
    return xData.map(t => `${moment(t).format('hh:mm')}`);

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
  console.log('getting metadata from ', data, xAxisData);
  return xAxisData.map((v, i) => {
    const index = data.findIndex(x => x.timestamp === v);
    return index > -1 ? [data[index].id, data[index].timestamp] : [];
  });
                         //      data[i] ? [data[i].id, data[i].timestamp] : []);
      //}
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
  //sessionsToBuckets
};
