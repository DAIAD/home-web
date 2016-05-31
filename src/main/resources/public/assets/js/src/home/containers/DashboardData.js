var React = require('react');
var bs = require('react-bootstrap');
var { injectIntl } = require('react-intl');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { push } = require('react-router-redux');

var { STATIC_RECOMMENDATIONS, STATBOX_DISPLAYS, DEV_METRICS, METER_METRICS, DEV_PERIODS, METER_PERIODS, DEV_SORT, METER_SORT } = require('../constants/HomeConstants');

var Dashboard = require('../components/sections/Dashboard');

var HistoryActions = require('../actions/HistoryActions');
var DashboardActions = require('../actions/DashboardActions');

var timeUtil = require('../utils/time');

var { getDeviceByKey, getDeviceNameByKey, getDeviceKeysByType, getDeviceTypeByKey, getAvailableDevices, getAvailableDeviceKeys, getAvailableMeters, getDefaultDevice, getLastSession, reduceMetric, reduceSessions, getDataSessions, getDataMeasurements, getShowersCount, getMetricMu, getSessionsIdOffset } = require('../utils/device');

var { getEnergyClass } = require('../utils/general');
var { getChartTimeDataByFilter, getChartDataByFilter, getChartMeterCategories, getChartAmphiroCategories } = require('../utils/chart');


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

  return Object.assign({}, ownProps,
               dispatchProps,
               stateProps,
               {
                 infoboxData: transformInfoboxData(stateProps.infoboxes, stateProps.devices, dispatchProps.link, ownProps.intl),
               });
}


function transformInfoboxData (infoboxes, devices, link, intl) {

  return infoboxes.map(infobox => {
    const { id, title, type, period, index, deviceType, subtype, data, previous, metric, showerId } = infobox;

    const meterPeriods = METER_PERIODS.filter(x => x.id !== 'custom');
    const devPeriods = DEV_PERIODS;

    let device, chartData, reduced, time, linkToHistory, highlight, previousReduced, better, comparePercentage, mu;
    let periods = [], displays = []; 

    const showers = getShowersCount(devices, data);
    
    let chartFormatter = intl => (x) => intl.formatTime(x, { hour:'numeric', minute:'numeric'});
    let chartType = 'line';
    let chartXAxis = 'category';
    let chartCategories = deviceType === 'METER' ? 
      getChartMeterCategories(period, intl) : 
        getChartAmphiroCategories(period, getSessionsIdOffset(data[0] ? data[0].sessions : []));
        
        
    let chartColors = ['#2d3480', '#abaecc', '#7AD3AB', '#CD4D3E'];
    let invertAxis = false;

    if (type==='tip') {
      highlight = STATIC_RECOMMENDATIONS[Math.floor(Math.random()*3)].description;
    }
    else if (type === 'last') {
      device = infobox.device;
      time = infobox.time;
      
      chartCategories = null;

      const last = data.find(d=>d.deviceKey===device);
      const lastShowerMeasurements = getDataMeasurements(devices, last, index);
      
      reduced = lastShowerMeasurements.map(s=>s[metric]).reduce((p, c)=>p+c, 0);
      highlight = reduced;
      mu = getMetricMu(metric);
      //highlight = `${reduced} ${mu}`;

      chartData = [{
        title: getDeviceNameByKey(devices, device), 
        data: getChartDataByFilter(lastShowerMeasurements, infobox.metric, chartCategories)
      }];
      chartXAxis = 'time';
    
      linkToHistory =  () => link({time, showerId, period, deviceType, device:[device], metric, index, data});
    }
    
    else if (type === 'total') {
      device = getDeviceKeysByType(devices, deviceType);
      time = timeUtil.getTimeByPeriod(period);
      
      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;
      displays = STATBOX_DISPLAYS;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      previousReduced = previous ? reduceMetric(devices, previous, metric) : 0; 

      highlight = reduced;
      mu = getMetricMu(metric);
      //highlight = `${reduced} ${mu}`;
      better = reduced < previousReduced;
      comparePercentage = previousReduced === 0 ? null : Math.round((Math.abs(reduced - previousReduced) / previousReduced)*100);

      chartData = data.map(devData => ({ 
        title: getDeviceNameByKey(devices, devData.deviceKey), 
        data: getChartDataByFilter(getDataSessions(devices, devData), infobox.metric, chartCategories)
      }));
     
     linkToHistory =  () => link({id, time, period, deviceType, device, metric, index, data});
    }
    else if (type === 'efficiency') {
      device = getDeviceKeysByType(devices, deviceType);
      reduced = data ? reduceMetric(devices, data, metric) : 0;

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;
      displays = STATBOX_DISPLAYS;

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
        data: getChartDataByFilter(getDataSessions(devices, devData), infobox.metric, chartCategories)
      }));
      
      linkToHistory =  () => link({id, time, period, deviceType, device, metric, index, data});

    }
    else if (type === 'forecast') {
      chartType = 'bar';

      //periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;
      //dummy data
      chartCategories=[2014, 2015, 2016];
      chartData=[{title:'Consumption', data:[100, 200, 150]}];
      mu = getMetricMu(metric);
    }
    else if (type === 'breakdown') {
      chartType = 'bar';

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      //dummy data
      chartData=[{title:'Consumption', data:[Math.floor(reduced/4), Math.floor(reduced/4), Math.floor(reduced/3), Math.floor(reduced/2-reduced/3)]}];
      chartCategories = ["toilet", "faucet", "shower", "kitchen"];
      chartColors = ['#abaecc', '#8185b2', '#575d99', '#2d3480'];
      mu = getMetricMu(metric);
      invertAxis = true;

      linkToHistory =  () => link({id, time, period, deviceType, device, metric, index, data});
    }
    else if (type === 'comparison') {
      chartType = 'bar';

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      mu = getMetricMu(metric);
      //dummy data based on real user data
      chartData=[{title:'Comparison', data:[reduced-0.2*reduced, reduced+0.5*reduced, reduced/2, reduced]}];
      chartCategories = ["City", "Neighbors", "Similar", "You"];
      chartColors = ['#f5dbd8', '#ebb7b1','#a3d4f4', '#2d3480'];
      mu = getMetricMu(metric);
      invertAxis = true;

      linkToHistory =  () => link({id, time, period, deviceType, device, metric, index, data});
    }
    else if (type === 'budget') {
      chartType = 'pie';

      periods = deviceType === 'AMPHIRO' ? devPeriods : meterPeriods;

      reduced = data ? reduceMetric(devices, data, metric) : 0;
      mu = getMetricMu(metric);
      chartCategories = null; 

      chartColors = ['#2d3480', '#abaecc'];
      //dummy data
      chartData=[{title:'66%', data:[{value: 345, name: 'consumed', color: '#2D3580'}, {value: 250, name: 'remaining', color: '#D0EAFA'}]}];
      mu = getMetricMu(metric);

      linkToHistory =  () => link({id, time, period, deviceType, device, metric, index, data});
    }
    return Object.assign({}, 
                       infobox,
                       {
                         periods,
                         displays,
                         device,
                         highlight,
                         chartData,
                         chartFormatter,
                         chartType,
                         chartCategories,
                         chartColors,
                         chartXAxis,
                         invertAxis,
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
