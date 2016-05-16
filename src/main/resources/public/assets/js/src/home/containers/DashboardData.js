var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { push } = require('react-router-redux');

var HomeConstants = require('../constants/HomeConstants');
var Dashboard = require('../components/sections/Dashboard');

var HistoryActions = require('../actions/HistoryActions');
var DashboardActions = require('../actions/DashboardActions');

var timeUtil = require('../utils/time');

var { getDeviceByKey, getDeviceNameByKey, getDeviceKeysByType, getDeviceTypeByKey, getAvailableDevices, getAvailableDeviceKeys, getAvailableMeters, getDefaultDevice, getLastSession, reduceMetric, reduceSessions, getDataSessions, getDataMeasurements, getDataShowersCount } = require('../utils/device');

var { getEnergyClass } = require('../utils/general');
var { getFilteredData } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  return {
    firstname: state.user.profile.firstname,
    devices: state.user.profile.devices,
    layout: state.section.dashboard.layout,
    mode: state.section.dashboard.mode,
    tempInfoboxData: state.section.dashboard.tempInfoboxData,
    infoboxes: state.section.dashboard.infobox,
  };
}

function mapDispatchToProps(dispatch) {
  return Object.assign({},
                        assign(bindActionCreators(DashboardActions, dispatch)),
                        {linkToHistory: options => dispatch(HistoryActions.linkToHistory(options))}); 

}

function mergeProps(stateProps, dispatchProps, ownProps) {
  //available infobox options
  const periods = [
    { id: "day", title: "Day", value: timeUtil.today() }, 
    { id: "week", title: "Week", value: timeUtil.thisWeek() }, 
    { id: "month", title: "Month", value: timeUtil.thisMonth() }, 
    { id: "year", title: "Year", value: timeUtil.thisYear() }
  ];
  
  let metrics = [
    { id: "volume", title: "Volume" }, 
    { id: "energy", title: "Energy" },
    { id: "temperature", title: "Temperature" },
    { id: "duration", title: "Duration" }
  ];
 
  const types = [
    { id: "stat", title: "Statistic" },
    { id: "chart", title: "Chart" }
  ];

  const chartSubtypes = [
    { id: "total", title: "Total" },
    {  id: "last", title: "Last" }
  ];

  const device = stateProps.tempInfoboxData.device || (stateProps.devices.length?stateProps.devices[0].deviceKey:"none");
  metrics = getDeviceTypeByKey(stateProps.devices, device)==='METER'?metrics.filter(x=>x.id==='volume'):metrics;

  //default values 
  const metric = stateProps.tempInfoboxData.metric || metrics[0].id;
  let time = stateProps.tempInfoboxData.time || periods[0].value;
  const period = stateProps.tempInfoboxData.period || periods[0].id;
  const type = stateProps.tempInfoboxData.type || types[0].id;

  const subtypes = chartSubtypes;
  const subtype = subtypes?(stateProps.tempInfoboxData.subtype || subtypes[0].id):"total";
  
  let defTitle = "";
  if (period === 'day') defTitle += "Today's ";
  else defTitle += "This " + period + "'s ";

  if (subtype === 'last') defTitle += "last shower ";

  if (metric === 'volume') defTitle += "water consumption ";
  else if (metric === 'energy') defTitle += "energy consumption ";
  else if (metric === 'temperature') defTitle += "average temperature ";
  else if (metric === 'duration') defTitle += "duration ";

  //const title = stateProps.tempInfoboxData.title || defTitle;
  const title = defTitle;
  
  time = subtype==='last'?Object.assign(time, {granularity:0}):time;
  //const subtypes = type==='chart'?chartSubtypes:null;

  return assign(ownProps,
               dispatchProps,
               assign(stateProps,
                      {
                        chartFormatter: intl => (x) => intl.formatTime(x, { hour:'numeric', minute:'numeric'}),
                          //amphiros: getAvailableDevices(stateProps.devices).map(dev=>dev.deviceKey),
                          //meters: getAvailableMeters(stateProps.devices).map(dev=>dev.deviceKey),
                        infoboxData: transformInfoboxData(stateProps.infoboxes, stateProps.devices, dispatchProps.linkToHistory),
                           periods,
                           types,
                           subtypes,
                           metrics,
                           tempInfoboxData: { 
                             title, 
                             type, 
                             subtype,
                             period, 
                             time, 
                             metric,
                             device,
                           }
                           
                     }));
}

function assign(...objects) {
  return Object.assign({}, ...objects);
}

function transformInfoboxData (infoboxes, devices, link) {

  return infoboxes.map(infobox => {
    const { id, title, type, time, period, index, deviceType, subtype, data, metric } = infobox;

    let device, chartData, reducedData, linkToHistory, tip;
    
    if (subtype === 'last') {
      device = infobox.device;
      const last = data.find(d=>d.deviceKey===device);
      const lastShowerMeasurements = getDataMeasurements(devices, last, index);
      
      reducedData = lastShowerMeasurements.map(s=>s[metric]).reduce((c, p)=>c+p, 0);
      
      chartData = [{
        title: getDeviceNameByKey(devices, device), 
        data:  getFilteredData(lastShowerMeasurements , infobox.metric)
      }];
    
      linkToHistory =  () => link({time, period, device:[device], metric, index, data});
    }
    else if (type==='tip') {
      tip = HomeConstants.STATIC_RECOMMENDATIONS[Math.floor(Math.random()*3)].description;
    }
    else {
      device = getDeviceKeysByType(devices, deviceType);
      
      reducedData = reduceSessions(devices, data).map(s=>s[metric]).reduce((c, p)=>c+p, 0); 
      
      if (subtype === 'efficiency') { 
        if (metric === 'energy') {
          reducedData = getEnergyClass(reducedData / getDataShowersCount(devices, data)); 
        }
      }

      chartData = data.map(devData => ({ 
        title: getDeviceNameByKey(devices, devData.deviceKey), 
        data:getFilteredData(getDataSessions(devices, devData, subtype, index), infobox.metric, getDeviceTypeByKey(devices, devData.device))
      }));
     
     linkToHistory =  () => link({id, time, period, device, metric, index, data});
    }

    return Object.assign({}, 
                       infobox,
                       {
                         device,
                         reducedData,
                         chartData,
                         linkToHistory,
                         tip
                       });
     });
}

var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
