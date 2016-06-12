
var _ = require('lodash');

var reports = {
  
  measurements: {

    // A key to identify a report instance
    computeKey: function (field, level, reportName, key) {
      var kp = [field, level, reportName];
      if (_.isArray(key))
        kp.push(...key);
      else
        kp.push(key);
      return kp.join('/');
    },
    
    // Functions to consolidate data points (aggregate at next level).
    consolidateFuncs: {

      'AVERAGE': (a) => (
        a.length? ((a.length > 1)? (_.sum(a)/a.length) : (a[0])) : null
      ),
  
      'MIN': (a) => (_.min(a)),
  
      'MAX': (a) => (_.max(a)),
    },

  },
  
  system: {
    
    // A key to identify a report instance
    computeKey: function (level, reportName) {
      return [level, reportName].join('/');
    },
  },
};

module.exports = reports;
