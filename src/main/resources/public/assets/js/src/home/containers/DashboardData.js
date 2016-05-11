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

var { getDeviceByKey, getDeviceNameByKey, getDeviceKeysByType, getDeviceTypeByKey, getAvailableDevices, getAvailableDeviceKeys, getAvailableMeters, getDefaultDevice, getLastSession, reduceMetric, reduceSessions, getDataSessions, getDataMeasurements, getShowersCount, getMetricMu } = require('../utils/device');

var { getEnergyClass } = require('../utils/general');
var { getChartDataByFilter } = require('../utils/chart');


function mapStateToProps(state, ownProps) {
  return {
    firstname: state.user.profile.firstname,
    devices: state.user.profile.devices,
    layout: state.section.dashboard.layout,
    //mode: state.section.dashboard.mode,
    tempInfoboxData: state.section.dashboard.tempInfoboxData,
    infoboxes: state.section.dashboard.infobox,
  };
}

function mapDispatchToProps(dispatch) {
  return Object.assign({}, 
                       bindActionCreators(DashboardActions, dispatch),
                       {link: options => dispatch(HistoryActions.linkToHistory(options))}
                      ); 

}

function mergeProps(stateProps, dispatchProps, ownProps) {
  const periods = [
      { id: "day", title: "Day", value: timeUtil.today() }, 
      { id: "week", title: "Week", value: timeUtil.thisWeek() }, 
      { id: "month", title: "Month", value: timeUtil.thisMonth() }, 
      { id: "year", title: "Year", value: timeUtil.thisYear() }
  ];
  
  return Object.assign({}, ownProps,
               dispatchProps,
               stateProps,
               {
                 infoboxData: transformInfoboxData(stateProps.infoboxes, stateProps.devices, dispatchProps.link),
                 periods,
               });
}


function transformInfoboxData (infoboxes, devices, link) {

  return infoboxes.map(infobox => {
    const { id, title, type, period, index, deviceType, subtype, data, previous, metric, showerId } = infobox;

    let device, chartData, reduced, time, linkToHistory, highlight, previousReduced, better, comparePercentage, mu;
    
    const showers = getShowersCount(devices, data);
    
    let chartFormatter = intl => (x) => intl.formatTime(x, { hour:'numeric', minute:'numeric'});

  if (period === 'year') 
    chartFormatter = intl => (x) => intl.formatTime(x, { month:'numeric', year:'numeric'});
  else if (period === 'month') 
    chartFormatter = intl => (x) => intl.formatTime(x, { day:'numeric'});
  else if (period === 'week')
    chartFormatter = intl => (x) => intl.formatTime(x, { day:'numeric'});
  else if (period === 'day')
    chartFormatter = intl => (x) => intl.formatTime(x, { hour:'numeric', minute:'numeric'});

    if (type==='tip') {
      highlight = HomeConstants.STATIC_RECOMMENDATIONS[Math.floor(Math.random()*3)].description;
    }
    else if (type === 'last') {
      device = infobox.device;
      time = infobox.time;
      const last = data.find(d=>d.deviceKey===device);
      const lastShowerMeasurements = getDataMeasurements(devices, last, index);
      
      reduced = lastShowerMeasurements.map(s=>s[metric]).reduce((c, p)=>c+p, 0);
      highlight = reduced;
      mu = getMetricMu(metric);
      //highlight = `${reduced} ${mu}`;

      chartData = [{
        title: getDeviceNameByKey(devices, device), 
        data: getChartDataByFilter(lastShowerMeasurements, infobox.metric)
      }];
    
      linkToHistory =  () => link({time, showerId, period, deviceType, device:[device], metric, index, data});
    }
    
    else if (type === 'total') {
      device = getDeviceKeysByType(devices, deviceType);
      time = timeUtil.getTimeByPeriod(period);

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      previousReduced = previous ? reduceMetric(devices, previous, metric) : 0; 

      highlight = reduced;
      mu = getMetricMu(metric);
      //highlight = `${reduced} ${mu}`;
      better = reduced < previousReduced;
      comparePercentage = previousReduced === 0 ? null : Math.round((Math.abs(reduced - previousReduced) / previousReduced)*100);
      console.log('total better?', id, better, reduced, previousReduced, comparePercentage);

      chartData = data.map(devData => ({ 
        title: getDeviceNameByKey(devices, devData.deviceKey), 
        data: getChartDataByFilter(getDataSessions(devices, devData), infobox.metric, getDeviceTypeByKey(devices, devData.device)) 
      }));
     
     linkToHistory =  () => link({id, time, period, deviceType, device, metric, index, data});
    }
    else if (type === 'efficiency') {
      device = getDeviceKeysByType(devices, deviceType);
      reduced = data ? reduceMetric(devices, data, metric) : 0;
      previousReduced = previous ? reduceMetric(devices, previous, metric) : 0; 

      better = reduced < previousReduced;

      comparePercentage = previousReduced === 0 ? null : Math.round((Math.abs(reduced - previousReduced) / previousReduced)*100);

      if (metric === 'energy') {
        highlight = (showers === 0 || reduced === 0) ? null : getEnergyClass(reduced / showers);
      }
      else {
        throw new Error('only energy efficiency supported');
      }
      
      chartData = data.map(devData => ({ 
        title: getDeviceNameByKey(devices, devData.deviceKey), 
        data: getChartDataByFilter(getDataSessions(devices, devData), infobox.metric, getDeviceTypeByKey(devices, devData.device)) 
      }));
     
      
      linkToHistory =  () => link({id, time, period, deviceType, device, metric, index, data});

    }
    return Object.assign({}, 
                       infobox,
                       {
                         device,
                         highlight,
                         chartData,
                         chartFormatter,
                         linkToHistory,
                         better,
                         comparePercentage,
                         mu,
                       });
     });
}

var DashboardData = connect(mapStateToProps, mapDispatchToProps, mergeProps)(Dashboard);
DashboardData = injectIntl(DashboardData);
module.exports = DashboardData;
