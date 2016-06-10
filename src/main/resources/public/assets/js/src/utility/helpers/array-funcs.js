var funcs = {
    
  pluck: function(arr, key){
    return arr.map(function (e) { return e[key]; });
  },
  
  // From an array of objects, pick those having a specific value for a given key
  pickQualiffiedOnEquality: function(arr, key, value){
    var qualified =  arr.map(function(e) { 
      if (e.hasOwnProperty(key)){
        if (e[key] === value) {
          return e; 
        }
      }
    });
    return qualified.filter(function (e) { if (e !== undefined) return e;} );
  },
  
  // From an array of objects, pick those whose value for a given key contains a given substring
  pickQualiffiedOnSusbstring: function(arr, key, substr, caseSensitive){
    var qualified;
    if (caseSensitive){
      qualified =  arr.map(function(e) { 
        if (e.hasOwnProperty(key)){
          if (e[key].indexOf(substr) !== -1) {
            return e; 
          }
        }
      });
    } else {
      qualified =  arr.map(function(e) { 
        if (e.hasOwnProperty(key)){
          if (e[key].toLowerCase().indexOf(substr.toLowerCase()) !== -1) {
            return e; 
          }
        }
      });
    }
    return qualified.filter(function (e) { if (e !== undefined) return e;} );
  },
  
  // ?
  getDistinctValuesArrayObjects: function(array, property){
    var distincts = [];
    var shownBefore = {};
    
    array.forEach(function (v, i){
      if(!shownBefore[v[property]]){
        shownBefore[v[property]] = true;
        distincts.push(v[property]);
      }
    });
    return distincts;
  },

  // Pad *in-place* the given array a from right to length n
  padRight: function (a, n, padding) {
    a.push.apply(a, Array(n - a.length).fill(padding));
    return a;
  },
  
  // Pad *in-place* the given array a to length n
  padLeft: function (a, n, padding) {
    a.unshift.apply(a, Array(n - a.length).fill(padding));
    return a;
  },
  
  // A mapper that computes diffs of successive items
  diffNumber: function (_1, i, a) {
    return (i > 0)? (Number(a[i]) - Number(a[i - 1])): null; 
  },

  // A mapper that returns pairs of successive items
  pairWithPrevious: function (value, i, a) {
    var prevValue = (i > 0)? a[i - 1] : null;
    return [prevValue, value];
  },
  
  // A mapper that returns pairs of successive items
  pairWithNext: function (value, i, a) {
    var nextValue = (i + 1 < a.length)? a[i + 1] : null;
    return [value, nextValue];
  },

};

module.exports = funcs;
