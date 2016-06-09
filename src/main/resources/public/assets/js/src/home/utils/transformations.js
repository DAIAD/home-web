var { STATIC_RECOMMENDATIONS, STATBOX_DISPLAYS, DEV_METRICS, METER_METRICS, DEV_PERIODS, METER_PERIODS, DEV_SORT, METER_SORT } = require('../constants/HomeConstants');

var { getFriendlyDuration, getEnergyClass, getMetricMu } = require('./general');
var { getChartMeterData, getChartAmphiroData, getChartMeterCategories, getChartAmphiroCategories } = require('./chart');
var { getTimeByPeriod } = require('./time');
var { getDeviceTypeByKey, getDeviceNameByKey, getDeviceKeysByType } = require('./device');


const getSessionsCount = function (devices, data) {
  return reduceSessions(devices, data).map(s => 1).reduce((p, c) => p + c, 0);
  //return data.map(d=>getDataSessions(devices, data).length).reduce((c, p)=> c+p, 0);
};

const getShowersCount = function (devices, data) {
  return reduceSessions(devices, data).map(s => s.count?s.count:1).reduce((p, c) => p + c, 0);
  //return data.map(d=>getDataSessions(devices, data).length).reduce((c, p)=> c+p, 0);
};


const reduceMetric = function (devices, data, metric) {
  const showers = getShowersCount(devices, data);                     
  const sessions = getSessionsCount(devices, data);
  let reducedMetric;
  //if (metric === 'showers') return `${showers} showers`;
  if (metric === 'showers') return showers;
  
  reducedMetric = data.map(it => getDataSessions(devices, it)
                                    .map(it=>it[metric]?it[metric]:0)
                                    .reduce(((p, c)=>p+c),0))
                        .reduce(((p, c)=>p+c),0);

    if (metric === 'temperature') {
      reducedMetric = reducedMetric / sessions;
      //reducedMetric = showers>0 ? reducedMetric / showers: 0;
    }
      //(metric === 'temperature' && (data[0]?(data[0].sessions[0]?(data[0].sessions[0].count==null):false):false))?(reducedMetric / showers):reducedMetric;
    

  reducedMetric = !isNaN(reducedMetric)?(Math.round(reducedMetric * 10)/10):0;
  //reducedMetric = `${reducedMetric} ${mu}`;
  return reducedMetric;
};

const getSessionIndexById = function (sessions, id) {
    if (!id || !sessions.length || !sessions[0].hasOwnProperty('id')) return null;
      
      return sessions.findIndex(x => (x.id).toString() === id.toString());
};

const updateOrAppendToSession = function (devices, data) {
  const { id } = data;

  let updated = devices.slice();
  if (!data || !id) return devices;

  const devIdx = devices.findIndex(d=>d.deviceKey===data.deviceKey);
  if (devIdx === -1) return updated;

  const sessions = updated[devIdx].sessions.slice();
  if (!sessions.length) return null;
  
  const index = getSessionIndexById(sessions, id);
  if (index > -1) {
    sessions[index] = data;
  }
  else {
    sessions.push(data);
  }
  updated[devIdx] = Object.assign({}, updated[devIdx], {sessions});
  return updated;
};

const transformInfoboxData = function (infobox, devices) {
    const { id, title, type, period, index, deviceType, subtype, data, previous, metric, showerId } = infobox;

    const meterPeriods = METER_PERIODS.filter(x => x.id !== 'custom');
    const devPeriods = DEV_PERIODS;

    let device, chartData, reduced, highlight, previousReduced, better, comparePercentage, mu;
    let periods = [], displays = []; 

    let time = infobox.time ? infobox.time : getTimeByPeriod(period);

    const showers = getShowersCount(devices, data);
    
    let chartType = 'line';
    let chartXAxis = 'category';
    let chartCategories = deviceType === 'METER' ? 
      getChartMeterCategories(time) : 
        getChartAmphiroCategories(period);
        
        
    let chartColors = ['#2d3480', '#abaecc', '#7AD3AB', '#CD4D3E'];
    let invertAxis = false;

    if (type==='tip') {
      highlight = STATIC_RECOMMENDATIONS[Math.floor(Math.random()*3)].description;
    }
    else if (type === 'last') {
      device = infobox.device;
      time = infobox.time;
      
      //chartCategories = null;
      chartCategories = getChartMeterCategories(time);

      const last = data.find(d=>d.deviceKey===device);
      const lastShowerMeasurements = getDataMeasurements(devices, last, index);
      
      reduced = lastShowerMeasurements.map(s=>s[metric]).reduce((p, c)=>p+c, 0);
      highlight = reduced;
      mu = getMetricMu(metric);
      //highlight = `${reduced} ${mu}`;

      chartData = [{
        title: getDeviceNameByKey(devices, device), 
        data: getChartMeterData(lastShowerMeasurements, chartCategories, infobox.metric, 'hour')
      }];
      //chartXAxis = 'time';
    
    }
    
    else if (type === 'total') {
      device = getDeviceKeysByType(devices, deviceType);
      //time = getTimeByPeriod(period);
      
      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;
      displays = STATBOX_DISPLAYS;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      previousReduced = previous ? reduceMetric(devices, previous, metric) : 0; 

      highlight = reduced;
      mu = getMetricMu(metric);
      //highlight = `${reduced} ${mu}`;
      better = reduced < previousReduced;
      comparePercentage = previousReduced === 0 ? null : Math.round((Math.abs(reduced - previousReduced) / previousReduced)*100);

      chartData = data.map(devData => ({ 
        title: getDeviceNameByKey(devices, devData.deviceKey), 
        //data: getChartDataByFilter(getDataSessions(devices, devData), infobox.metric, chartCategories)
        data: deviceType === 'METER' ? getChartMeterData(getDataSessions(devices, devData), chartCategories, infobox.metric, getLowerGranularityPeriod(period)) : getChartAmphiroData(getDataSessions(devices, devData), chartCategories, infobox.metric)
      }));
     
    }
    else if (type === 'efficiency') {
      device = getDeviceKeysByType(devices, deviceType);
      reduced = data ? reduceMetric(devices, data, metric) : 0;

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;
      displays = STATBOX_DISPLAYS;

      previousReduced = previous ? reduceMetric(devices, previous, metric) : 0; 

      better = reduced < previousReduced;

      comparePercentage = previousReduced === 0 ? null : Math.round((Math.abs(reduced - previousReduced) / previousReduced)*100);

      if (metric === 'energy') {
        highlight = (showers === 0 || reduced === 0) ? null : getEnergyClass(reduced / showers);
      }
      else {
        throw new Error('only energy efficiency supported');
      }
      
      chartData = data.map(devData => ({ 
        title: getDeviceNameByKey(devices, devData.deviceKey), 
        //data: getChartDataByFilter(getDataSessions(devices, devData), infobox.metric, chartCategories)
        data: deviceType === 'METER' ? getChartMeterData(getDataSessions(devices, devData), chartCategories, infobox.metric, getLowerGranularityPeriod(period)) : getChartAmphiroData(getDataSessions(devices, devData), chartCategories, infobox.metric)
      }));
      /*
      chartData = data.map(devData => ({ 
        title: getDeviceNameByKey(devices, devData.deviceKey), 
        data: getChartDataByFilter(getDataSessions(devices, devData), infobox.metric, chartCategories)
      }));
      */
    }
    else if (type === 'forecast') {
      chartType = 'bar';

      //periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;
      //dummy data
      chartCategories=[2014, 2015, 2016];
      chartData=[{title:'Consumption', data:[100, 200, 150]}];
      mu = getMetricMu(metric);
    }
    else if (type === 'breakdown') {
      chartType = 'bar';

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      //dummy data
      chartData=[{title:'Consumption', data:[Math.floor(reduced/4), Math.floor(reduced/4), Math.floor(reduced/3), Math.floor(reduced/2-reduced/3)]}];
      chartCategories = ["toilet", "faucet", "shower", "kitchen"];
      chartColors = ['#abaecc', '#8185b2', '#575d99', '#2d3480'];
      mu = getMetricMu(metric);
      invertAxis = true;

    }
    else if (type === 'comparison') {
      chartType = 'bar';

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      mu = getMetricMu(metric);
      //dummy data based on real user data
      chartData=[{title:'Comparison', data:[reduced-0.2*reduced, reduced+0.5*reduced, reduced/2, reduced]}];
      chartCategories = ["City", "Neighbors", "Similar", "You"];
      chartColors = ['#f5dbd8', '#ebb7b1','#a3d4f4', '#2d3480'];
      mu = getMetricMu(metric);
      invertAxis = true;

    }
    else if (type === 'budget') {
      chartType = 'pie';

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      mu = getMetricMu(metric);
      chartCategories = null; 

      chartColors = ['#2d3480', '#abaecc'];
      //dummy data
      chartData=[{title:'66%', data:[{value: 345, name: 'consumed', color: '#2D3580'}, {value: 250, name: 'remaining', color: '#D0EAFA'}]}];
      mu = getMetricMu(metric);

    }
    return Object.assign({}, 
                       infobox,
                       {
                         periods,
                         displays,
                         device,
                         highlight,
                         chartData,
                         //chartFormatter,
                         chartType,
                         chartCategories,
                         chartColors,
                         chartXAxis,
                         invertAxis,
                         better,
                         comparePercentage,
                         mu,
                       });
};

const calculateIndexes = function (sessions) { 
  return sessions.map((session, idx, array) => Object.assign({}, session, 
                     {
                       next:array[idx+1]?[array[idx+1].device, array[idx+1].id, array[idx+1].timestamp]:null,
                       prev:array[idx-1]?[array[idx-1].device, array[idx-1].id, array[idx-1].timestamp]:null,
                     }));
};

const sortSessions = function (sessions, by='timestamp', order='desc') {
  const sorted = order === 'asc' ? sessions.sort((a, b) => a[by] - b[by]) : sessions.sort((a, b) => b[by] - a[by]);
  return calculateIndexes(sorted);
};

// reduces array of devices with multiple sessions arrays
// to single array of sessions (including device key)
const reduceSessions = function (devices, data) {
  return data.map(device =>  
                  getDataSessions(devices, device)
                  .map((session, idx, array) => {
                    const devType = getDeviceTypeByKey(devices, device.deviceKey);
                    return Object.assign({}, session, 
                             {
                               index:idx, 
                               devType,
                               device: device.deviceKey,
                               devName:getDeviceNameByKey(devices, device.deviceKey),
                               //volume: devType==='METER'?session.difference:session.volume,
                               duration: getFriendlyDuration(session.duration), 
                               temperature: session.temperature ? Math.round(session.temperature * 10)/10 : null,
                               energyClass: getEnergyClass(session.energy), 
                               
                               better: array[idx-1]?(devType==='METER'?(array[idx].difference<=array[idx-1].difference?true:false):(array[idx].volume<=array[idx-1].volume?true:false)):null,
                               hasChartData: session.measurements?true:false 

                             });
                  }
                          )
                        )
                        .reduce((p, c) => p.concat(c), []);
};

const getDataSessions = function (devices, data) {
  if (!data || !data.deviceKey) return [];
  
  const devType = getDeviceTypeByKey(devices, data.deviceKey);
  
  if (devType === 'AMPHIRO') {
      return data.sessions;
  }
  else if (devType === 'METER') {
    return data.values;
  }
  else {
    return [];
  }
};

const getDataMeasurements = function (devices, data, index) {
  const sessions = getDataSessions(devices, data);

  if (!Array.isArray(sessions) || sessions.length < index) return [];

  return sessions[index]?sessions[index].measurements:[];
};

const getLastSession = function (sessions) { 
  if (!sessions.length || !sessions[0].hasOwnProperty('timestamp')) return null;

  return sessions.reduce((prev, curr) => (curr.timestamp>prev.timestamp)?curr:prev);
};

const getSessionById = function (sessions, id) {
  if (!id || !sessions.length || !sessions[0].hasOwnProperty('id')) return null;
  return sessions.find(x => (x.id).toString() === id.toString());
};

const getSessionByIndex = function(sessions, index) {
  if (typeof(index) !== "number" || !sessions.length) return null;
  return sessions[index];
};

const getNextSession = function(sessions, id) {
  if (!id || !sessions.length || !sessions[0].hasOwnProperty('id')) return null;
  
  const sessionIndex = getSessionIndexById(sessions, id);
  if (sessions[sessionIndex+1]){
    return sessions[sessionIndex+1].id;
  }
  else {
    return null;
  }
};

const getPreviousSession = function(sessions, id) {
  if (!id || !sessions.length || !sessions[0].hasOwnProperty('id')) return null;
  
  const sessionIndex = getSessionIndexById(sessions, id);
  if (sessions[sessionIndex-1]){
    return sessions[sessionIndex-1].id;
  }
  else {
    return null;
  }
};

const meterSessionsToCSV = function (sessions) {
  return sessions.map(session => [
    session.devName, 
    session.volume, 
    session.difference, 
    session.timestamp 
  ].join('%2C'))
  .reduce((prev, curr) => [prev, curr].join('%0A'), 
          'Device, Volume%A0total, Volume%A0 difference, Timestamp');
};

const deviceSessionsToCSV = function (sessions) {
  return sessions.map(session => [
    session.devName,
    session.id,
    session.history,
    session.volume, 
    session.energy,
    session.energyClass,
    session.temperature,
    session.duration, 
    session.timestamp 
  ].join('%2C'))
  .reduce((prev, curr) => [prev, curr].join('%0A'), 
          'Device, Id, Historic, Volume, Energy, Energy%A0Class, Temperature, Duration, Timestamp');
};

module.exports = {
  getNextSession,
  getPreviousSession,
  getSessionById,
  getSessionByIndex,
  getLastSession,
  updateOrAppendToSession,
  getDataSessions,
  getDataMeasurements,
  reduceSessions,
  sortSessions,
  reduceMetric,
  transformInfoboxData,
  meterSessionsToCSV,
  deviceSessionsToCSV
};
