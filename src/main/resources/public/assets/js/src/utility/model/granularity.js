
var moment = require('moment');

// Represent granularity of measurements (data points).
// Note This class is mostly used to provide named instances for reports.

var Granularity = function (quantity, unit)
{
  this.unit = unit; // same as in Moment.js
  this.quantity = quantity;
};

Granularity.prototype.toDuration = function ()
{
  return moment.duration(this.quantity, this.unit);
};

Granularity.prototype.valueOf = function ()
{
  return this.toDuration().asMilliseconds();
};

// A (sorted descending) map of commonly used instances 
Granularity.common = new Map([
  ['millisecond', {args: [1, 'ms']}],
  ['second', {args: [1, 's']}],
  ['minute', {args: [1, 'm']}],
  ['hour', {args: [1, 'h']}],
  ['day', {args: [1, 'd']}],
  ['week', {args: [1, 'w']}],
  ['month', {args: [1, 'M']}],
  ['quarter', {args: [1, 'Q']}],
  ['year', {args: [1, 'y']}],
]);

Granularity.fromName = (name) => {
  var u = Granularity.common.get(name);
  return !u? null : (new Granularity(...u.args));
};

Granularity.names = () => (Array.from(Granularity.common.keys()));

module.exports = Granularity;
