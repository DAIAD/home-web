
var {getConfiguration} = require('../api/configuration');

// Request server-side configuration for a certain entity

var configure = function (entityName) {
  
  return getConfiguration(entityName).then(
    res => {
      if (res.errors && res.errors.length) {
        throw 'The request is rejected: ' + res.errors[0].description;    
      }
      return res;
    }
  );
};

module.exports = configure;
