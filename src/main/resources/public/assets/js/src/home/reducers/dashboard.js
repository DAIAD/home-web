var types = require('../constants/ActionTypes');
var timeUtil = require ('../utils/time');

var dashboard = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      mode: "normal",
      layout: [
        {i: "1", x:0, y:0, w:2, h:2, minW:2, minH:2},
        {i: "2", x:2, y:0, w:2, h:2, minW:2, minH:2},
        {i: "4", x:4, y:0, w:2, h:1, minW:2, minH:1},
        {i: "5", x:4, y:1, w:2, h:1, minW:2, minH:1},
      ],
      tempInfoboxData: {
        title: null,
        type: null,
        subtype: null,
        metric: null,
        device: null,
        period: null,
        time: null
      },
      infobox: [
        /*
        {
          id: "1", 
          title: "Water efficiency Score", 
          data: [],

          },
          */
          {
          id: "1", 
          title: "Year amphiro consumption",
          type: "chart",
          subtype: "total",
          period: "year",
          deviceType: "AMPHIRO",
          metric: "volume",
          data: [],
          },
          {
          id: "2", 
          title: "Last Shower", 
          type: "chart",
          subtype: "last",
          deviceType: "AMPHIRO",
          metric: "volume",
          data: [],
          },
        {
          id: "4", 
          title: "Year amphiro consumption", 
          type: "stat",
          deviceType: "AMPHIRO",
          period: "year",
          metric: "volume",
          data: [],
          },
         {
          id: "5", 
          title: "Last shower consumption", 
          type: "stat",
          subtype: "last",
          deviceType: "AMPHIRO",
          metric: "volume",
          data: [],
        },
        /*
        {
          id: "5", 
          title:"Day consumption",
          type: "stat",
          time: Object.assign({}, timeUtil.today(), {granularity:0}),
          device: "39533fa1-61d5-456f-beae-11845a03b374",
          metric: "volume",
          data: [],
          },
          */
        /*{
          i: "6", 
          title: "Year forecasting", 

        },
        {
          i: "7", 
          title: "Water breakdown", 
          
          }
          */
      ]
    };
  }
   
  switch (action.type) {
    
    case types.DASHBOARD_SWITCH_MODE: 
        return Object.assign({}, state, {
          mode: action.mode
        });

    case types.DASHBOARD_UPDATE_INFOBOX_TEMP: {
      let newTemp = Object.assign({}, state.tempInfoboxData, action.data);
       //protection against storing values other than supported
      const { title, type, subtype, time, period, device, metric  } = newTemp;
      newTemp = { title, type, subtype, period, time, device, metric};
     
      return Object.assign({}, state, {
        tempInfoboxData: newTemp
      });

    }

    case types.DASHBOARD_RESET_INFOBOX_TEMP: 
      return Object.assign({}, state, {
        tempInfoboxData: {
          title: null,
          type: null,
          subtype: null,
          metric: null,
          device: null,
          period: null,
          time: null
        },
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
      console.log('old nfobox', newInfobox);
      let idx = newInfobox.findIndex(obj => obj.id === action.id);
      newInfobox.splice(idx, 1);
      console.log('new infobox', newInfobox);
      
      return Object.assign({}, state, {
        infobox: newInfobox
      });
    }

    case types.DASHBOARD_UPDATE_INFOBOX: {
      let newInfobox = state.infobox.slice();
      let idx = newInfobox.findIndex(obj => obj.id === action.id);
      newInfobox[idx].data = action.data;
      
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

