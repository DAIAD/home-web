var { getFriendlyDuration, getEnergyClass } = require('./general');

const getDefaultDevice = function(devices) {
  const amphiroDevices = getAvailableDevices(devices);
  const meters = getAvailableMeters(devices);
  if (amphiroDevices.length) {
    return amphiroDevices[0];
  }
  else if (meters.length) {
    return meters[0];
  }
  else {
    return null;
  }
};

const getDeviceTypeByKey = function(devices, key) {
  const device = getDeviceByKey(devices, key);
  if (!device) return null;
  return device.type;
};

const getDeviceCount = function(devices) {
  if (!devices.length) return 0;
  return getAvailableDevices(devices).length;
};

const getAvailableDevices = function(devices) {
  if (!devices) return [];
  return devices.filter((device) => (device.type === 'AMPHIRO'));
};

const getAvailableMeters = function(devices) {
  if (!devices) return [];
  return devices.filter((device) => (device.type === 'METER'));
};

const getDeviceKeysByType = function(devices, type) {
  let available = [];
  if (type === "AMPHIRO") available = getAvailableDevices(devices);
  else if (type === "METER") available = getAvailableMeters(devices);
  else throw new Error('device type ', type, 'not supported');

  return available.map(d=>d.deviceKey);
};

const getAvailableDeviceKeys = function(devices) {
  if (!devices) return [];
  return getAvailableDevices(devices).map((device) => (device.deviceKey));
};

const getDeviceByKey = function(devices, key) {
  //TODO: if !key added below error is thrown, why?
  //if (!devices ||!Array.isArray(devices)) throw new Error (`devices ${devices} must be of type array`);
  if (!devices ||!Array.isArray(devices)) return {};
  return devices.find((device) => (device.deviceKey === key));
};

const getDeviceNameByKey = function(devices, key) {
  const device = getDeviceByKey(devices, key);
  if (!device) return null;
  return device.name || device.serial || device.macAddress || device.deviceKey;
};

const updateOrAppendToSession = function(devices, data) {
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

const getSessionByIndex = function(sessions, index) {
  if (typeof(index) !== "number" || !sessions.length) return null;
  return sessions[index];
};

const getSessionById = function(sessions, id) {
  if (!id || !sessions.length || !sessions[0].hasOwnProperty('id')) return null;
  return sessions.find(x => (x.id).toString() === id.toString());
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

const getSessionIndexById = function(sessions, id) {
  if (!id || !sessions.length || !sessions[0].hasOwnProperty('id')) return null;
  
  return sessions.findIndex(x => (x.id).toString() === id.toString());
};
const getLastSession = function(sessions) { 
  if (!sessions.length || !sessions[0].hasOwnProperty('timestamp')) return null;

  return sessions.reduce((prev, curr) => (curr.timestamp>prev.timestamp)?curr:prev);
};

const getReducedDeviceType = function(devices, keys) {
   if (keys && !keys.length) return -1;
   return keys.map(deviceKey => getDeviceTypeByKey(devices, deviceKey)).reduce((prev, curr) => prev===curr?curr:-1);
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

const getSessionsCount = function (devices, data) {
  return reduceSessions(devices, data).map(s => 1).reduce((c, p) => c + p, 0);
  //return data.map(d=>getDataSessions(devices, data).length).reduce((c, p)=> c+p, 0);
};

const getShowersCount = function (devices, data) {
  return reduceSessions(devices, data).map(s => s.count?s.count:1).reduce((c, p) => c + p, 0);
  //return data.map(d=>getDataSessions(devices, data).length).reduce((c, p)=> c+p, 0);
};

const getDataMeasurements = function (devices, data, index) {
  const sessions = getDataSessions(devices, data);

  if (!Array.isArray(sessions) || sessions.length < index) return [];

  return sessions[index]?sessions[index].measurements:[];
};

// reduces array of devices with multiple sessions arrays
// to single array of sessions (including device key)
const reduceSessions = function(devices, data) {
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
                        .reduce((c, p) => c.concat(p), []);
};

const calculateIndexes = function(sessions) { 
  return sessions.map((session, idx, array) => Object.assign({}, session, 
                     {
                       next:array[idx+1]?[array[idx+1].id, array[idx+1].device]:null,
                       prev:array[idx-1]?[array[idx-1].id, array[idx-1].device]:null,
                     }));
};

const sortSessions = function(sessions, by='timestamp', order='desc') {
  const sorted = order === 'asc' ? sessions.sort((a, b) => a[by] - b[by]) : sessions.sort((a, b) => b[by] - a[by]);
  return calculateIndexes(sorted);
};

const getMetricMu = function(metric) {
  if (metric === 'showers') return '';
  else if (metric === 'volume' || metric === 'difference') return 'lt';
  else if (metric === 'energy') return 'W';
  else if (metric === 'duration') return 'sec';
  else if (metric === 'temperature') return '°C';
};

const reduceMetric = function(devices, data, metric) {
  const showers = getShowersCount(devices, data);                     
  const sessions = getSessionsCount(devices, data);
  let reducedMetric;
  //if (metric === 'showers') return `${showers} showers`;
  if (metric === 'showers') return showers;
  
  reducedMetric = data.map(it => getDataSessions(devices, it)
                                    .map(it=>it[metric]?it[metric]:0)
                                    .reduce(((c, p)=>c+p),0))
                        .reduce(((c, p)=>c+p),0);

    if (metric === 'temperature') {
      reducedMetric = reducedMetric / sessions;
      //reducedMetric = showers>0 ? reducedMetric / showers: 0;
    }
      //(metric === 'temperature' && (data[0]?(data[0].sessions[0]?(data[0].sessions[0].count==null):false):false))?(reducedMetric / showers):reducedMetric;
    

  reducedMetric = !isNaN(reducedMetric)?(Math.round(reducedMetric * 10)/10):0;
  //reducedMetric = `${reducedMetric} ${mu}`;
  return reducedMetric;
};

const lastNFilterToLength = function (filter) {
  if (filter === 'ten') return 10;
  else if (filter === 'twenty') return 20;
  else if (filter === 'fifty') return 50;
  else throw new Error('unrecognized filter', filter);
};

const getSessionsIdOffset = function(sessions) {
  if (!sessions) return null;
  return sessions[0] ? sessions[0].id : null;
};

module.exports = {
  getSessionById,
  getSessionByIndex,
  getNextSession,
  getPreviousSession,
  getSessionIndexById,
  getLastSession,
  updateOrAppendToSession,
  getDefaultDevice,
  getDeviceTypeByKey,
  getDeviceCount,
  getAvailableDevices,
  getAvailableDeviceKeys,
  getAvailableMeters,
  getDeviceNameByKey,
  getDeviceByKey,
  getDeviceKeysByType,
  getReducedDeviceType,
  reduceSessions,
  reduceMetric,
  getDataSessions,
  getDataMeasurements,
  getShowersCount,
  lastNFilterToLength,
  getMetricMu,
  sortSessions,
  getSessionsIdOffset
};
