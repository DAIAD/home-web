var getDefaultDevice = function(devices) {
	var amphiroDevices = getAvailableDevices(devices);
	return amphiroDevices?amphiroDevices[0]:null;
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

var getSessionById = function(sessions, id) {
	return sessions.find(x => (x.id).toString() === id.toString());
};

var getSessionIndexById = function(sessions, id) {
	return sessions.findIndex(x => (x.id).toString() === id.toString());
};

module.exports = {
	getSessionById,
	getSessionIndexById,
	getDefaultDevice,
	getAvailableDevices,
	getAvailableDeviceKeys,
	getDeviceByKey
};
