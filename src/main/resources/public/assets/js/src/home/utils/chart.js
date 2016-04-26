var moment = require('moment');

const getCount = function(metrics) {
  return metrics.count?metrics.count:1;
};

const getTimestampIndex = function(points, timestamp) {
    return points.findIndex((x) => (x[0]===timestamp));
};

//TODO: refactor this monster
const addPreviousValues = function(data) {
  return data.map((val, idx, arr) => [val[0], arr.map((array) => array[1]?array[1]:0).reduce((prev, curr, idx2, array, initial) => idx2<=idx?prev+curr:prev)]);
};

const getFilteredData = function(data, filter, devType='AMPHIRO', timeFilter=null, transfer=false) {
  if (!data || !data.length) return [];
  
  let filteredData = [];
  switch (filter) {
    case "showers":
      data.forEach(function(dato, i)  {
        const count = getCount(dato);
        const index = getTimestampIndex(filteredData, dato.timestamp);
        
        //increment or append
        if (index>-1){
          filteredData[index] = [filteredData[index][0], filteredData[index][1]+count]; 
        }
        else{
          filteredData.push([dato.timestamp, count]);     
        } 
      });
      return filteredData.map(x => [new Date(x[0]),x[1]]);

    case "volume":
    case "energy":
      data.forEach(function(dato) {
        if (!dato[filter]){
          return;
        }

        let timestamp = dato.timestamp;
        if (transfer) {
          timestamp = moment(timestamp).add(1, timeFilter);
        }
        filteredData.push([timestamp, dato[filter]]);
      });
      filteredData = filteredData.map(x => [new Date(x[0]),x[1]]);
      
      if (devType === 'AMPHIRO') {
        //filteredData = addPreviousValues(filteredData); 
      }
      else if (devType === 'METER') {
        //filteredData = filteredData.map((x, i, array) => array[i-1]?[x[0],imer(array[i][1] - array[0][1])]:array[i]);
        //filteredData = filteredData.map((x, i, array) => array[i-1]?[x[0], (array[i][1]-array[i-1][1])]:array[i]);
      }

      return filteredData;

    default:
      data.forEach(function(dato) {
        if (!dato[filter]){
          return;
        }
        let timestamp = dato.timestamp;
        if (transfer) {
          timestamp = moment(timestamp).add(1, timeFilter);
        }
        filteredData.push([timestamp, dato[filter]]);
      });
      
      return filteredData.map(x => [new Date(x[0]),x[1]]);
      

  }
  return;
};

module.exports = {
  getFilteredData
};
