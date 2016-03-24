var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var connect = require('react-redux').connect;
var FormattedMessage = require('react-intl').FormattedMessage;
var { push } = require('react-router-redux');

var Dashboard = require('../components/sections/Dashboard');

var HistoryActions = require('../actions/HistoryActions');
var DashboardActions = require('../actions/DashboardActions');

var timeUtil = require('../utils/time');
var { getDefaultDevice, getLastSession } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  const lastSession = state.section.dashboard.lastSession;
  const defaultDevice = getDefaultDevice(state.user.profile.devices);
  const deviceKey = defaultDevice?defaultDevice.deviceKey:null;
  return {
    defaultDevice: deviceKey,
    firstname: state.user.profile.firstname,
    lastShower: lastSession,
    layouts: state.section.dashboard.layout,
    infoboxData: state.section.dashboard.infobox,
    /*
       infoboxData: [
        {
          i: "1", 
          title: "Water efficiency Score", 
          improved:true,  
          compareText:"Better since last month", 
          highlight:(<h2>B+</h2>), 
          linkText:"Learn how to improve"
        },
        {
          i: "2", 
          title: "Week consumption", 
          improved: false, 
          highlight:(<h2>450lt</h2>), 
          compareText:(<span><b>23%</b> more than last month</span>), 
          linkText: "See more", 
          link:() => HistoryActions.linkToHistory({ 
            device: defaultDevice, 
            filter: 'volume', 
            timeFilter: 'week', 
            time: Object.assign({}, timeUtil.thisWeek(), {granularity:2})
          }) 
        },
        {
          i: "3", 
          title: "Last Shower", 
          classContainer:"padded", 
          classLeft:"row", 
          classRight: "row padded", 
          improved:true, 
          //compareText:lastSession?(<span>You consumed a total of <b>{lastSession.volume + " lt"}</b> in <b>{lastSession.duration + " secs"}</b>.</span>):(<div />), 
          //highlight:(<LastShowerChart {...props} />), 
          //linkText: "" 
        },
        {
          i: "4", 
          title: "Year energy consumption", 
          classLeft:"col-md-7", 
          classRight:"col-md-5", 
          improved:false, 
          highlight:(<h2>850kWh</h2>), 
          linkText:"See more", 
          link:()=> HistoryActions.linkToHistory({ 
            device: defaultDevice, 
            filter: 'energy', 
            timeFilter: 'year', 
            time: Object.assign({}, timeUtil.thisYear(), {granularity: 4})
          }) 
        },
        {
          i: "5", 
          title:"Day consumption", 
          improved: true, 
          highlight: (<h2>38lt</h2>), 
          compareText:(<span><b>5%</b> less than yesterday!</span>), 
          extraText:"See more", 
          link:()=> HistoryActions.linkToHistory({ 
            device: defaultDevice, 
            filter: 'volume', 
            timeFilter: 'day', 
            time: Object.assign({}, timeUtil.today(), {granularity:0})
          }) 
        },
        {
          i: "6", 
          title: "Year forecasting", 
          classLeft:"col-md-8", 
          classRight:"col-md-4", 
          improved: true, 
          compareText:"Based on your water use so far, we estimate you will use less water this year. Good job!", 
          //highlight:(<ForecastingChart {...props} />), 
          linkText:"See more" 
        },
        {
          i: "7", 
          title: "Water breakdown", 
          classLeft:"col-md-12", 
          classRight:"col-md-1", 
          compareText:"", 
          //highlight:(<BreakdownChart {...props} />), 
          linkText:"See more" 
        }
      ]
        */
       };
}

function mapDispatchToProps(dispatch) {
  return merged(bindActionCreators(DashboardActions, dispatch)); 
}

function mergeProps(stateProps, dispatchProps, ownProps) {
  return merged(ownProps,
               dispatchProps,
               merged(stateProps,
                     {
                       lastSessionTime: merged(timeUtil.thisMonth(), {granularity: 0}),
                       chartFormatter: (intl => (x) => { console.log(intl); return intl.formatTime(x, { hour:'numeric', minute:'numeric', second:'numeric'}); }),
                         chartData: [{title:'Consumption', data:(stateProps.lastShower?(getFilteredData(stateProps.lastShower.measurements?stateProps.lastShower.measurements:[], 'volume')):[])}],
                       
                     }));
}

function merged (...objects) {
  return Object.assign({}, ...objects);
}

var DashboardData = connect(mapStateToProps, mapDispatchToProps)(Dashboard);
//DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
