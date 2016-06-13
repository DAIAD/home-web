require('regenerator-runtime/runtime');

var moment = require('moment');

var funcs = {
  
  // Generate a sequence of timestamps from [start,end) with given step
  generateTimestamps: function* (start, end, step) {
    var t = moment(start);
    end = moment(end);
    while (t < end) {
      yield t.valueOf();
      t.add(1, step);
    }
  },

  // Convert local (wall-clock) time to the GMT (UTC+0) equivalent,
  // i.e find the timestamp of the same wall-clock time in the GMT timezone. 
  toUtcTime: function (t, asMillis=false) {
    
    // Note 
    // See also https://github.com/moment/moment/issues/1922
    // It seems that moment.js supports this with a 2nd param to utcOffset (as m.utcOffset(0, true))
    // But, this is not documented in the official docs, so we reside on this solution. 
    
    t = moment(t);
    // Convert and flag as UTC
    t.add(t.utcOffset(), 'minute').utc();
    return asMillis? t.valueOf() : t;
  },

  // Convert a global wall-clock time to our local equivalent
  fromUtcTime: function (t, asMillis=false) {
    var localOffset = moment().utcOffset();
    // Convert and flag as local
    t = moment(t).subtract(localOffset, 'minute').local();
    return asMillis? t.valueOf() : t;
  },
};

module.exports = funcs;
