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

const getAvailableDeviceKeys = function(devices) {
  if (!devices) return [];
  return getAvailableDevices(devices).map((device) => (device.deviceKey));
};

const getDeviceByKey = function(devices, key) {
  if (!devices) return null;
  return devices.find((device) => (device.deviceKey === key));
};

const updateOrAppendToSession = function(sessions, data, id) {
  if (!sessions.length) return null;
  let updated = sessions.slice();
  if (!data || !id) return updated;
  
  const index = getSessionIndexById(sessions, id);
  if (index > -1) {
    updated[index] = data;
  }
  else {
    updated.push(data);
  }
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
  getDeviceByKey
};
