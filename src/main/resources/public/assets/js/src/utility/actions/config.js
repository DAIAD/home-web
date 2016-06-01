
var actions = {
  
  utility: require('./config-utility'),
  
  reports: require('./config-reports'),

  // Convenience thunk actions

  configure: () => (dispatch, getState) => {
    // Fixme Configure all sub-parts
    //var p1 = dispatch(actions.utility.configure());
    //var p2 = dispatch(actions.reports.configure());
    //return Promise.all([p1, p2]);
    console.info('Configure client...');
    return 42;
  },
};

module.exports = actions;
