
var api = require('./base');

var QueryAPI = {
  submitQuery : function(query) {
    return api.json('/action/query', query);
  }
};

module.exports = QueryAPI;
