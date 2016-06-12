var _ = require('lodash');

var ActionTypes = require('../action-types');

var actions = {
  
  // Plain actions
  
  setField: (field) => ({
    type: ActionTypes.charting.SET_FIELD,
    field,
  }),
  
  setReportUnchecked: (level, reportName) => ({
    type: ActionTypes.charting.SET_REPORT,
    level,
    reportName,
  }),
  
  // Thunk actions
  
  setReport: (level, reportName) =>  (dispatch, getState) => {
    // Because not all combinations of (level, reportName) are valid,
    // we adjust reportName (if needed) to the given level. 
    
    var reportsForLevel = getState()
      .config
        .reports.byType.measurements.levels[level].reports;
    if (!(reportName in reportsForLevel)) {
      reportName = _.first(_.keys(reportsForLevel));
    }

    return dispatch(actions.setReportUnchecked(level, reportName));
  },

};

module.exports = actions;
