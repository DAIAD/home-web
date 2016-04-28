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
  if (!devices ||!Array.isArray(devices)) throw new Error (`devices ${devices} must be of type array`);
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

const getDataShowersCount = function (devices, data) {
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
            getDataSessions(devices, device).map((session, idx) => 
               Object.assign({}, session, 
                             {index:idx},
                             {device:device.deviceKey}
                            )
                          )
                        )
                        .reduce((c, p) => c.concat(p), []);
};

const reduceMetric = function(devices, data, metric) {
  const showers = getDataShowersCount(devices, data);                     
  if (metric === 'showers') return showers;

  let reducedMetric = data.map(it => getDataSessions(devices, it)
                                    .map(it=>it[metric]?it[metric]:[])
                                    .reduce(((c, p)=>c+p),0))
                            .reduce(((c, p)=>c+p),0);

                            reducedMetric = (metric === 'temperature')?(reducedMetric / showers):reducedMetric;
  

  reducedMetric = !isNaN(parseInt(reducedMetric))?parseInt(reducedMetric).toFixed(1):0;
  return reducedMetric;
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
  getDataShowersCount,
};
