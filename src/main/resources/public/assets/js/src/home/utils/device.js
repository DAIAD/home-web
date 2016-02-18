var getDefaultDevice = function(devices) {
	var amphiroDevices = getAvailableDevices(devices);
	return amphiroDevices.length?amphiroDevices[0]:null;
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

var getSessionById = function(sessions, id) {
	if (!id) return null;
	return sessions.find(x => (x.id).toString() === id.toString());
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
	getSessionIndexById,
	getLastSession,
	updateOrAppendToSession,
	getDefaultDevice,
	getAvailableDevices,
	getAvailableDeviceKeys,
	getDeviceByKey
};
