var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { push } = require('react-router-redux');

var Dashboard = require('../components/sections/Dashboard');

var HistoryActions = require('../actions/HistoryActions');
var DashboardActions = require('../actions/DashboardActions');

var timeUtil = require('../utils/time');
var { getDeviceByKey, getDeviceNameByKey, getDeviceTypeByKey, getAvailableDevices, getAvailableMeters, getDefaultDevice, getLastSession } = require('../utils/device');
var { getFilteredData } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  return {
    firstname: state.user.profile.firstname,
    devices: state.user.profile.devices,
    layout: state.section.dashboard.layout,
    mode: state.section.dashboard.mode,
    tempInfoboxData: state.section.dashboard.tempInfoboxData,
    infoboxData: state.section.dashboard.infobox,
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
                        infoboxData: transformInfoboxData(stateProps.infoboxData, stateProps.devices),
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

function transformInfoboxData (infoboxData, devices) {

  return infoboxData.map(infobox => {
                         //Object.assign({}, infobox, {device: "1bc00c64-a51a-474f-90dd-6156162475d9"}))
                         //Object.assign({}, infobox, {deviceDetails: getDeviceByKey(devices, infobox.device)}))
                         //.map(infobox => {

    
    const { type, deviceType, subtype, data, metric } = infobox;
    let dataSource;
    
    if (deviceType === "AMPHIRO") dataSource = "sessions";
    else if (deviceType === "METER") dataSource = "values";

    if (type === 'chart') {
      if (subtype==='last') {
        //return Object.assign({}, infobox, {data:Object.assign({}, data, {chartData: getFilteredData(data.measurements, metric, infobox.deviceDetails.type)})});
        dataSource = "measurements";
      }
      else if (subtype ==='total') {
        //return Object.assign({}, infobox, {data:Object.assign({}, data, {chartData:getFilteredData(infobox.data, infobox.metric, infobox.deviceDetails.type)})});
      }
      else throw new Error('oops, subtype', subtype, ' not supported');
      //}
      return Object.assign({}, 
                                 infobox,
                                 {device:getAvailableDevices(devices).map(device=>device.deviceKey)},
                                   {chartData:infobox.data.map(devData =>
                                                       {
                                                         return {title: getDeviceNameByKey(devices, devData.deviceKey), data:getFilteredData(devData[dataSource], infobox.metric, getDeviceTypeByKey(devices, devData.deviceKey))};
                                                       }) 
                           });
    }
    else if (type === 'stat') {
      if (subtype === 'last') {
        return Object.assign({},
                             infobox,
                             {
                               reducedData: infobox.data.map(it=>it?it.measurements.map(it=>it[metric]?it[metric]:0).reduce(((c,p)=>c+p),0):[]).reduce(((c, p)=>c+p),0).toFixed(1)
                             }
                            );
      }
      else {
        return Object.assign({},
                             infobox,
                             {
                               reducedData: infobox.data.map(it=>it[dataSource].map(it=>it[metric]?it[metric]:0).reduce(((c,p)=>c+p),0)).reduce(((c, p)=>c+p),0).toFixed(1)
                             }
                            );
        
      }
    }
    else throw new Error('oops, type', type, ' not supported');
  });
}

var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
