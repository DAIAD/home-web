
var _ = require('lodash');
var sprintf = require('sprintf');

var ActionTypes = require('../action-types');
var {getGroups} = require('../api/admin');

var actions = {

  // Plain actions 

  requestConfiguration: () => ({
    type: ActionTypes.config.utility.REQUEST_CONFIGURATION,
  }), 

  setConfiguration: (config) => ({
    type: ActionTypes.config.utility.SET_CONFIGURATION,
    config,
  }),

  // Thunk actions

  configure: () => (dispatch, getState) => {
    var state = getState();

    if (!_.isEmpty(state.config.utility)) {
      console.info('Configuration for utility is already present; Skipping');
      return Promise.resolve();
    }

    dispatch(actions.requestConfiguration());
    
    // Fetch all groups inside this utility
    return getGroups().then(
      res => {
        if (res.errors.length) {
          throw new Error(sprintf(
            'Cannot configure utility: %s', _.first(res.errors).description
          ));
        }
        
        var {name, key} = _.first(res.groups.filter(g => g.type == 'UTILITY'));
        var clusters = res.groups.filter(g => g.utilityKey == key && g.type == 'CLUSTER');
        var config = {
          name,
          key,
          clusters: clusters.map(c => ({
            key: c.key,
            name: c.name,
            groups: c.segments.map(g => ({
              clusterKey: c.key,
              key: g.key,
              name: g.name,
              size: g.size,
            })),
          })),
        };

        dispatch(actions.setConfiguration(config));
      }
    );
  },
};

module.exports = actions;
