function getDefaultDevice (devices) {
  var amphiroDevices = getAvailableDevices(devices);
  var meters = getAvailableMeters(devices);
  if (amphiroDevices.length) {
    return amphiroDevices[0];
  }
  else if (meters.length) {
    return meters[0];
  }
  else {
    return null;
  }
}

function getDeviceTypeByKey (devices, key) {
  const device = getDeviceByKey(devices, key);
  if (!device) return null;
  return device.type;
}
function getDeviceCount (devices) {
  return getAvailableDevices(devices).length;
}

function getAvailableDevices (devices) {
  return devices.filter((device) => (device.type === 'AMPHIRO'));
}

function getAvailableMeters (devices) {
  return devices.filter((device) => (device.type === 'METER'));
}

function getAvailableDeviceKeys (devices) {
  return getAvailableDevices(devices).map((device) => (device.deviceKey));
}

function getDeviceByKey (devices, key) {
  if (!devices) return null;

  return devices.find((device) => (device.deviceKey === key));
}

function updateOrAppendToSession (sessions, data, id) {
  if (!data || !id){
    return sessions;
  }
  var index = getSessionIndexById(sessions, id);
  var updated = sessions.slice();
  if (index > -1) {
    updated[index] = data;
  }
  else {
    updated.push(data);
  }
  return updated;
}

function getSessionByIndex (sessions, index) {
  if (typeof(index) !== "number") return null;
  //if (sessions.length && !sessions[0].id) return null;

  return sessions[index];
}

function getSessionById (sessions, id) {
  if (!id) return null;
  if (sessions.length && !sessions[0].id) return null;

  return sessions.find(x => (x.id).toString() === id.toString());
}

function getNextSession (sessions, id) {
  const sessionIndex = getSessionIndexById(sessions, id);
  if (sessions[sessionIndex+1]){
    return sessions[sessionIndex+1].id;
  }
  else {
    return null;
  }
}

function getPreviousSession (sessions, id) {
  const sessionIndex = getSessionIndexById(sessions, id);
  if (sessions[sessionIndex-1]){
    return sessions[sessionIndex-1].id;
  }
  else {
    return null;
  }
}
function getSessionIndexById (sessions, id) {
  return sessions.findIndex(x => (x.id).toString() === id.toString());
}

function getLastSession (sessions) {
  var lastSession = null;
  sessions.forEach(function(session) {
    if (!lastSession){
      lastSession = session;
    }
    if (session.timestamp > lastSession.timestamp) {
      lastSession = session;
    }
  });
  return lastSession;
}

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
