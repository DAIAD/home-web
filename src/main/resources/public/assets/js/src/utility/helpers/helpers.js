var $ = require('jquery');

var Helpers = {
		
	pluck: function(arr, key){
		    return arr.map(function (e) { return e[key]; });
	},
	
	/* From an array of objects, we pick those, 
	 * having a specific value for a given key
	 * */
	pickQualiffied: function(arr, key, value){
		var qualified =  arr.map(function(e) { 
			if (e.hasOwnProperty(key)){
				if (e[key] === value) {
					return e; 
				}
			}
		});
		return qualified.filter(function (e) { if (e !== undefined) return e;} );
	},
	
	getDistinctValuesArrayObjects: function(array, property){
		var distincts = [];
		var shownBefore = {};
		
		$.each(array, function (i,v){
			if(!shownBefore[v[property]]){
				shownBefore[v[property]] = true;
				distincts.push(v[property]);
			}
		});
		return distincts;
	},
};

module.exports = Helpers;