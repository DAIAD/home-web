
var _ = require('lodash');
var moment = require('moment');

// Represent a time span (an interval)

// Note
// This class is mostly used to provide named instances (e.g 'today') for reports.

var TimeSpan = function (t, quantity, unit)
{
  if (_.isString(t)) {
    if (!t || t == 'now')
      this.t = moment();
    else
      this.t = moment(t, 'YYYY-MM-DD HH:mm:ss.SSS');
  } else if (moment.isDate(t)) {
    this.starts = moment(t.getTime());
  } else if (moment.isMoment(t)) {
    this.t = t;
  }
  
  this.unit = unit; // same as in Moment.js
  this.quantity = quantity;
};

TimeSpan.prototype.toDuration = function () {
  return moment.duration(this.quantity, this.unit);
};

TimeSpan.prototype.toRange = function (milliseconds=false) {
  var t0, t1, r;
  t0 = this.t;
  t1 = t0.clone().add(this.quantity, this.unit);
  r = (this.quantity < 0)? [t1, t0] : [t0, t1];
  return !milliseconds? r : [r[0].valueOf(), r[1].valueOf()];
};
 
TimeSpan.common = new Map([
  ['hour', {
    title: 'This hour',
    getArgs: () => ([moment().startOf('hour'), +1, 'hour']),
  }],
  ['hour-1', {
    title: 'Last 1 hour',
    getArgs: () => ([moment(), -1, 'hour']),
  }],
  ['today', {
    title: 'Today',
    getArgs: () => ([moment().startOf('day'), +1, 'day']),
  }],
  ['yesterday', {
    title: 'Yesterday',
    getArgs: () => ([moment().startOf('day'), -1, 'day']),
  }],
  ['day-1', {
    title: 'Last 24 hours',
    getArgs: () => ([moment(), -1, 'day']),
  }],
  ['day-2', {
    title: 'Last 48 hours',
    getArgs: () => ([moment(), -2, 'day']),
  }],
  ['week', {
    title: 'This week',
    getArgs: () => ([moment().startOf('isoweek'), +1, 'week']),
  }],
  ['week-1', {
    title: 'Last week',
    getArgs: () => ([moment(), -1, 'week']),
  }],
  ['month', {
    title: 'This month',
    getArgs: () => ([moment().startOf('month'), +1, 'month']),
  }],
  ['month-1', {
    title: 'Last month',
    getArgs: () => ([moment(), -1, 'month']),
  }],
  ['quarter', {
    title: 'This quarter',
    getArgs: () => ([moment().startOf('quarter'), +1, 'quarter']),
  }],
  ['quarter-1', {
    title: 'Last quarter',
    getArgs: () => ([moment(), -1, 'quarter']),
  }],
  ['year', {
    title: 'This year',
    getArgs: () => ([moment().startOf('year'), +1, 'year']),
  }],
]);

TimeSpan.fromName = function (name) {
  var u = TimeSpan.common.get(name);
  return !u? null : (new TimeSpan(...u.getArgs()));
};

TimeSpan.commonNames = () => (Array.from(TimeSpan.common.keys()));

module.exports = TimeSpan;
