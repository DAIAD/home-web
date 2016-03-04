var getDefaultDevice = function(devices) {
	var amphiroDevices = getAvailableDevices(devices);
	return amphiroDevices.length?amphiroDevices[0]:null;
};

var getDeviceTypeByKey = function(devices, key) {
  const device = getDeviceByKey(devices, key);
  return device.type;
};
var getDeviceCount = function(devices) {
	return getAvailableDevices(devices).length;
};

var getAvailableDevices = function(devices) {
  return devices.filter((device) => (device.type === 'AMPHIRO'));
};

var getAvailableDeviceKeys = function(devices) {
	return getAvailableDevices(devices).map((device) => (device.deviceKey));
};

var getDeviceByKey = function(devices, key) {
	if (!devices) return null;

	return devices.find((device) => (device.deviceKey === key));
};

var updateOrAppendToSession = function(sessions, data, id) {
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
};

var getSessionByIndex = function(sessions, index) {
	if (typeof(index) !== "number") return null;
	//if (sessions.length && !sessions[0].id) return null;

	return sessions[index];
};

var getSessionById = function(sessions, id) {
	if (!id) return null;
	if (sessions.length && !sessions[0].id) return null;

	return sessions.find(x => (x.id).toString() === id.toString());
};

var getNextSession = function(sessions, id) {
	const sessionIndex = getSessionIndexById(sessions, id);
	if (sessions[sessionIndex+1]){
		return sessions[sessionIndex+1].id;
	}
	else {
		return null;
	}
};

var getPreviousSession = function(sessions, id) {
	const sessionIndex = getSessionIndexById(sessions, id);
	if (sessions[sessionIndex-1]){
		return sessions[sessionIndex-1].id;
	}
	else {
		return null;
	}
};
var getSessionIndexById = function(sessions, id) {
	return sessions.findIndex(x => (x.id).toString() === id.toString());
};

var getLastSession = function(sessions) {
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
