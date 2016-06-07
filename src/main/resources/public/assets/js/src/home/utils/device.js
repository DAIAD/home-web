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

const getDeviceKeyByName = function(devices, name) {
  const device = devices.find(d => d.name === name || d.serial === name);
  if (device) return device.deviceKey;
  else return null;
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



module.exports = {
  getDefaultDevice,
  getDeviceTypeByKey,
  getDeviceCount,
  getAvailableDevices,
  getAvailableDeviceKeys,
  getAvailableMeters,
  getDeviceNameByKey,
  getDeviceByKey,
  getDeviceKeyByName,
  getDeviceKeysByType,
};
