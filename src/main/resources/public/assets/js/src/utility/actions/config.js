
var actions = {
  
  utility: require('./config-utility'),
  
  reports: require('./config-reports'),

  // Thunk actions

  configure: () => (dispatch, getState) => {
    // This is a convenience action that wraps several config-* actions: 
    // Gather all needed configuration for an (authenticated) user.
    var p1 = dispatch(actions.utility.configure());
    var p2 = dispatch(actions.reports.configure());
    return Promise.all([p1, p2]);
  },
};

module.exports = actions;
