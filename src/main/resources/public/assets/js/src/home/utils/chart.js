var getCount = function(metrics) {
  return metrics.count?metrics.count:1;
};

var getTimestampIndex = function(points, timestamp) {
    return points.findIndex((x) => (x[0]===timestamp));
};

//TODO: refactor this monster
var addPreviousValues = function(data) {
  return data.map((val, idx, arr) => [val[0], arr.map((array) => array[1]?array[1]:0).reduce((prev, curr, idx2, array, initial) => idx2<=idx?prev+curr:prev)]);
};

var getFilteredData = function(data, filter) {
  var filteredData = [];
  if (!data) return [];

  switch (filter) {
    case "showers":
      data.forEach(function(dato, i)  {
        const count = getCount(dato);
        var index = getTimestampIndex(filteredData, dato.timestamp);
        
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
      data.forEach(function(dato) {
        if (!dato[filter]){
          return;
        }
        filteredData.push([dato.timestamp, dato[filter]]);
      });

      filteredData = filteredData.map(x => [new Date(x[0]),x[1]]);
      filteredData = addPreviousValues(filteredData); 

      return filteredData;

    case "energy":
      data.forEach(function(dato) {
        if (!dato[filter]){
          return;
        }
        filteredData.push([dato.timestamp, dato[filter]]);
      });

      filteredData = filteredData.map(x => [new Date(x[0]),x[1]]);
      filteredData = addPreviousValues(filteredData); 
      
      return filteredData;

    default:
      data.forEach(function(dato) {
        if (!dato[filter]){
          return;
        }
        filteredData.push([dato.timestamp, dato[filter]]);
      });
      
      return filteredData.map(x => [new Date(x[0]),x[1]]);
      

  }
  return;
  //return array with dates instead of timestamps
  //return filteredData;
  //return filteredData.map(x => [new Date(x[0]),x[1]]);
};


/*
var data = getFilteredData(activeSessionDataMeasurements, state.section.history.sessionFilter);

  if (state.section.history.sessionFilter === 'volume' || state.section.history.sessionFilter === 'energy'){
    //TODO: refactor this monster
    data = data.map((val, idx, arr) => [val[0], arr.map((array) => array[1]?array[1]:0).reduce((prev, curr, idx2, array, initial) => idx2<=idx?prev+curr:prev)]);
  }
  
  */

module.exports = {
  getFilteredData
};
