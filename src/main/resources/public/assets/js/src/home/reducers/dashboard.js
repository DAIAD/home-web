var types = require('../constants/ActionTypes');
var timeUtil = require ('../utils/time');

var dashboard = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      mode: "normal",
      layout: [
        {i: "1", x:0, y:0, w:2, h:2},
        {i: "2", x:2, y:0, w:2, h:2},
        {i: "3", x:2, y:0, w:2, h:1},
        {i: "4", x:4, y:0, w:2, h:2},
        {i: "5", x:4, y:1, w:2, h:1},
        {i: "6", x:0, y:2, w:2, h:2},
        {i: "7", x:0, y:2, w:2, h:1},
        {i: "8", x:4, y:2, w:2, h:1},
        {i: "9", x:0, y:3, w:2, h:2},
        {i: "10", x:4, y:3, w:2, h:2},
      ],
      infobox: [
          {
            id: "1", 
            title: "Water",
            type: "total",
            display: "chart",
            period: "ten",
            deviceType: "AMPHIRO",
            metric: "volume",
            data: [],
          },
          {
            id: "2", 
            title: "Last Shower", 
            period: "ten",
            type: "last",
            display: "chart",
            deviceType: "AMPHIRO",
            metric: "volume",
            data: [],
          },
          {
            id: "3", 
            title: "Energy",
            type: "total",
            display: "stat",
            period: "twenty",
            deviceType: "AMPHIRO",
            metric: "energy",
            data: [],
          },
          {
            id: "4", 
            title: "SWM", 
            type: "total",
            display: "chart",
            deviceType: "METER",
            period: "year",
            metric: "difference",
            data: [],
        },
        {
          id: "7", 
          title: "Tip of the day",
          type: "tip",
          display: "tip",
          data: [],
        
        },
        {
          id: "8", 
          title: "Efficiency",
          type: "efficiency",
          display: "stat",
          deviceType: "AMPHIRO",
          period: "twenty",
          metric: "energy",
          data: [],
        },
        {
          id: "9", 
          title: "Breakdown",
          type: "breakdown",
          display: "chart",
          deviceType: "METER",
          period: "year",
          metric: "difference",
          data: [],
        },
        {
          id: "10", 
          title: "Forecast",
          type: "forecast",
          display: "chart",
          deviceType: "METER",
          period: "year",
          metric: "difference",
          data: [],
        },

      ]
    };
  }
   
  switch (action.type) {
    
    case types.DASHBOARD_SWITCH_MODE: 
        return Object.assign({}, state, {
          mode: action.mode
        });

    case types.DASHBOARD_ADD_INFOBOX: {
      let newInfobox = state.infobox.slice();
      newInfobox.push(action.data);
      
      return Object.assign({}, state, {
        infobox: newInfobox
      });
    }
 
    case types.DASHBOARD_REMOVE_INFOBOX: {
      let newInfobox = state.infobox.slice();
      let idx = newInfobox.findIndex(obj => obj.id === action.id);
      newInfobox.splice(idx, 1);
      
      return Object.assign({}, state, {
        infobox: newInfobox
      });
    }

    case types.DASHBOARD_UPDATE_INFOBOX: {
      let newInfobox = state.infobox.slice();
      //TODO: had to use let instead of const because of browserify block scope error
      let idx = newInfobox.findIndex(obj => obj.id === action.id);

      newInfobox[idx] = Object.assign({}, newInfobox[idx], action.update);
      //newInfobox[idx].data = action.data;
      
      return Object.assign({}, state, {
        infobox: newInfobox
      });
    }
  
    case types.DASHBOARD_SET_INFOBOX_DATA: {
      let newInfobox = state.infobox.slice();
      //TODO: same as above 
      let idx = newInfobox.findIndex(obj => obj.id === action.id);

      newInfobox[idx] = Object.assign({}, newInfobox[idx], action.update);
      //newInfobox[idx].data = action.data;
      
      return Object.assign({}, state, {
        infobox: newInfobox
      });
    }
    case types.DASHBOARD_UPDATE_LAYOUT: {
      return Object.assign({}, state, {
        layout: action.layout
      });
    }

    case types.DASHBOARD_APPEND_LAYOUT: {
      let newLayout = state.layout.slice();
      newLayout.push(action.layout);
      return Object.assign({}, state, {
        layout: newLayout 
      });
    }

    case types.DASHBOARD_REMOVE_LAYOUT: {
      const idx = state.layout.findIndex(x=>x.i===action.id);
      const newLayout = state.layout.splice(idx, 1);
      return Object.assign({}, state, {
        layout: newLayout 
      });
    }

    default:
      return state;
  }
};

module.exports = dashboard;

