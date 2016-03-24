var types = require('../constants/ActionTypes');
var timeUtil = require('../utils/time');

var dashboard = function (state, action) {
  //initial state
  if (state === undefined) {
    state = {
      lastSession: null,
      layout: [
        {x:0, y:1, w:2, h:1, i:"1"},
        {x:0, y:2, w:2, h:1, i:"2"},
        {x:2, y:1, w:2, h:2, i:"3"},
        {x:4, y:1, w:2, h:1, i:"4"},
        {x:4, y:1, w:2, h:1, i:"5"},
        {x:0, y:2, w:3, h:2, i:"6"},
        {x:3, y:2, w:3, h:2, i:"7"},
      ],
      infobox: [
        {
          id: "1", 
          title: "Water efficiency Score", 
          data: [],

        },
        {
          id: "2", 
          title: "Week consumption",
          type: "stat",
          time: Object.assign({}, timeUtil.thisWeek(), {granularity:2}),
          device: "e5f05388-af4b-484b-8f1a-a62f37f08dee",
          metric: "volume",
          data: [],
        },
        {
          id: "3", 
          title: "Last Shower", 
          type: "last",
          time: Object.assign({}, timeUtil.thisMonth(), {granularity:0}),
          device: "e5f05388-af4b-484b-8f1a-a62f37f08dee",
          metric: "volume",
          data: [],
        },
        {
          id: "4", 
          title: "Year energy consumption", 
          type: "stat",
          time: Object.assign({}, timeUtil.thisYear(), {granularity:4}),
          device: "e5f05388-af4b-484b-8f1a-a62f37f08dee",
          metric: "energy",
          data: [],

        },
        {
          id: "5", 
          title:"Day consumption",
          type: "stat",
          time: Object.assign({}, timeUtil.today(), {granularity:0}),
          device: "e5f05388-af4b-484b-8f1a-a62f37f08dee",
          metric: "volume",
          data: [],

        },
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
      case types.DASHBOARD_SET_LAST_SESSION:
        return Object.assign({}, state, {
          lastSession: action.session
        });
      
      case types.DASHBOARD_UPDATE_LAYOUT:
        return Object.assign({}, state, {
          layout: action.layout
        });
      
      case types.DASHBOARD_UPDATE_INFOBOX:
        let newInfobox = state.infobox.slice();
        const idx = newInfobox.findIndex(obj => obj.id === action.id);
        newInfobox[idx].data = action.data;
        
        return Object.assign({}, state, {
          infobox: newInfobox
        });

      default:
        return state;
  }
};

module.exports = dashboard;

