var types = require('../constants/ActionTypes');

const initialState = {
  infoboxToAdd: {
    deviceType: 'METER',
    type: 'totalDifferenceStat',
    title : 'Total volume Stat',
  },
  profileForm: {
  }
};

var form = function (state, action) {
  if (state === undefined) {
    state = initialState;
  }

  switch (action.type) {
    case types.FORM_SET: {
      let newState = Object.assign({}, state);
      newState[action.form] = Object.assign({}, newState[action.form], action.formData);

      return newState;
    }

    case types.FORM_RESET: {
      let newState = Object.assign({}, state);
      newState[action.form] = Object.assign({}, newState[action.form], initialState[action.form]);

      return newState;
    }

    default:
      return state;
  }
};

module.exports = form;

