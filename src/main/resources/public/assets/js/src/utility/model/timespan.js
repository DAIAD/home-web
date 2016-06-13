
var _ = require('lodash');
var moment = require('moment');

// Represent a time span (an interval)

// Note
// This class is mostly used to provide named instances (e.g 'today') for reports.

var TimeSpan = function (t, duration, unit)
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
  this.duration = duration;
};

TimeSpan.prototype.toDuration = function () {
  return moment.duration(this.duration, this.unit);
};

TimeSpan.prototype.toRange = function (asMillis=false) {
  var t0, t1, r;
  t0 = this.t;
  t1 = t0.clone().add(this.duration, this.unit);
  r = (this.duration < 0)? [t1, t0] : [t0, t1];
  return !asMillis? r : [r[0].valueOf(), r[1].valueOf()];
};
 
TimeSpan.common = new Map([
  ['hour', {
    title: 'This hour',
    startsAt: 'hour',
    duration: +1,
    unit: 'hour',
  }],
  ['1-hour', {
    title: 'Last 1 hour',
    startsAt: null,
    duration: -1,
    unit: 'hour',
  }],
  ['today', {
    title: 'Today',
    startsAt: 'day',
    duration: +1,
    unit: 'day',
  }],
  ['yesterday', {
    title: 'Yesterday',
    startsAt: 'day',
    duration: -1,
    unit: 'day',
  }],
  ['24-hours', {
    title: 'Last 24 hours',
    startsAt: null,
    duration: -24,
    unit: 'hour',
  }],
  ['48-hours', {
    title: 'Last 48 hours',
    startsAt: null,
    duration: -48,
    unit: 'hour',
  }],
  ['7-day', {
    title: 'Last 7 days',
    startsAt: 'day',
    duration: -7,
    unit: 'day',
  }],
  ['week', {
    title: 'This week',
    startsAt: 'isoweek',
    duration: +1,
    unit: 'week',
  }],
  ['last-week', {
    title: 'Last week',
    startsAt: 'isoweek',
    duration: -1,
    unit: 'week',
  }],
  ['month', {
    title: 'This month',
    startsAt: 'month',
    duration: +1,
    unit: 'month',
  }],
  ['last-month', {
    title: 'Last month',
    startsAt: 'month',
    duration: -1,
    unit: 'month',
  }],
  ['quarter', {
    title: 'This quarter',
    startsAt: 'quarter',
    duration: +1,
    unit: 'quarter',
  }],
  ['last-quarter', {
    title: 'Last quarter',
    startsAt: 'quarter',
    duration: -1,
    unit: 'quarter',
  }],
  ['year', {
    title: 'This year',
    startsAt: 'year',
    duration: +1,
    unit: 'year',
  }],
  ['last-year', {
    title: 'Last year',
    startsAt: 'year',
    duration: -1,
    unit: 'year',
  }],
]);

TimeSpan.fromName = function (name, offset, now) {
  var u = TimeSpan.common.get(name);
  if (!u)
    return null;
  
  var t = moment(now);
  
  // A UTC offset (if given) affects all startOf() calculations!
  if (_.isNumber(offset))
    t.utcOffset(offset);
  
  // Align to a start of a bucket
  if (u.startsAt)
    t.startOf(u.startsAt);
  
  return new TimeSpan(t, u.duration, u.unit);
};

TimeSpan.commonNames = () => (Array.from(TimeSpan.common.keys()));

module.exports = TimeSpan;
