require('regenerator-runtime/runtime');

var moment = require('moment');

var funcs = {
  
  generateTimestamps: function* (start, end, step) {
    var t = moment(start);
    end = moment(end);
    while (t < end) {
      yield t.valueOf();
      t.add(1, step);
    }
  },
};

module.exports = funcs;
