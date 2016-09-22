
module.exports = {
  IMAGES : "/assets/images/home/svg",
  PNG_IMAGES : "/assets/images/home/png",
  NOTIFICATION_TITLE_LENGTH: 50,
  CACHE_SIZE: 20,
  LOCALES: ["en", "el", "de", "es"],
  COUNTRIES: ["United Kingdom", "Spain", "Greece" ],
  TIMEZONES: [
    "Europe/London",
    "Europe/Madrid",
    "Europe/Athens"
  ],
  MESSAGE_TYPES:[
    {type: "ALERT"}, 
    {type: "RECOMMENDATION_STATIC"}, 
    {type: "RECOMMENDATION_DYNAMIC"}, 
    {type: "ANNOUNCEMENT"}
  ],
  MAIN_MENU: [{
    name: "dashboard",
    title: "section.dashboard",
    image: "dashboard-menu.svg",
    route:"/dashboard",
    children: []
  },
  {
    name: "history",
    title: "section.history",
    image: "stats-menu.svg",
    route:"/history",
  },
  {
    name: "messages",
    title: "section.notifications",
    image: "notifications-menu.svg",
    route: "/notifications",
  },
    
      {
        name: "settings",
        title: "section.settings",
        image: "settings-menu.svg",
        route:"/settings",
      },
  ],
  METER_AGG_METRICS: [
      {id:'devName',  mu:'', title:'history.device', icon: 'amphiro_small.svg', details: 'history.durationDetails', clickable: false}, 
      {id:'count',mu:'', title:'history.count', details:'history.countDetails', icon: 'default-ranking.svg', clickable: true},  
      {id:'difference', mu:'lt',title:'history.volume', details:'history.volumeDetails', icon: 'volume.svg',clickable: true},
  ],
  SHOWER_METRICS: [
      {id:'devName',  mu:'', title:'history.device', icon: 'amphiro_small.svg', details: 'history.durationDetails', clickable: false}, 
      {id:'volume', mu:'lt',title:'history.volume', details:'history.volumeDetails', icon:'volume.svg', clickable: true}, 
      {id:'temperature', mu:'ºC', title:'history.temperature', details: 'history.temperatureDetails',icon: 'temperature.svg', clickable: true}, 
      {id:'energy',mu:'W', title:'history.energy', details: 'history.energyDetails', icon:'energy.svg', clickable: true}, 
      {id:'friendlyDuration', mu:'', title:'history.duration', details: 'history.durationDetails', icon:'duration.svg', clickable: false}, 
  ],
  METER_PERIODS: [
    {id: 'day', title: 'periods.day'},
    {id: 'week', title: 'periods.week'},
    {id: 'month', title: 'periods.month'},
    {id: 'year', title: 'periods.year'},
    {id: 'custom', title: 'periods.custom'},
  ],
  DEV_PERIODS: [
    {id: 'ten', title: 'periods.ten'},
    {id: 'twenty', title: 'periods.twenty'},
    {id: 'fifty', title: 'periods.fifty'},
  ],
  METER_METRICS: [
    {id:'difference', title:'Volume'},
  ],
  DEV_METRICS: [
    {id:'volume', title:'Volume'},
    {id:'energy', title:'Energy'},
    {id:'duration', title:'Duration'},
    {id:'temperature', title:'Temperature'}
  ],
  METER_SORT: [
    {id: 'timestamp', title: 'Time'}, 
    {id:'difference', title: 'Volume'}
  ],
  DEV_SORT: [
    {id: 'id', title: 'ID'}, 
    {id: 'timestamp', title: 'Time'}, 
    {id:'volume', title: 'Volume'}, 
    {id:'devName', title: 'Device'}, 
    {id: 'energy', title: 'Energy'}, 
    {id:'temperature', title:'Temperature'}, 
    {id:'duration', title: 'Duration'}
  ],
  STATBOX_DISPLAYS: [
    {id: 'stat', title: 'Stat'}, 
    {id: 'chart', title: 'Chart'}
  ],
  WIDGET_TYPES: [
    {id: 'totalVolumeStat', title: 'Shower Volume Stat', description: 'A stat widget displaying the total consumption for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'volume', display: 'stat'}},
    {id: 'totalVolumeChart', title: 'Shower Volume Chart', description: 'A chart widget presenting the consumption for your last 10 showers for all installed devices. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'volume', display: 'chart'}},
    {id: 'totalEnergyStat', title: 'Shower Energy Stat', description: 'A stat widget displaying the total energy consumption for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'energy', display: 'stat'}},
    {id: 'totalEnergyChart', title: 'Shower Energy Chart', description: 'A chart widget displaying the total energy progress for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'energy', display: 'chart'}},
    {id: 'totalTemperatureStat', title: 'Shower Temperature Stat', description: 'A widget displaying the average temperature for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'temperature', display: 'stat'}},
    {id: 'totalTemperatureChart', title: 'Shower Temperature Chart', description: 'A widget displaying the average temperature variation for your last 10 showers. You can later change this to show the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'total', metric: 'temperature', display: 'chart'}},
    
    {id: 'totalDifferenceStat', description: 'A widget displaying your household\'s total water consumption for the last month. You can later change it to daily, weekly or yearly consumption.', title: 'Total Volume Stat', devType:'METER', data: {type: 'total', metric: 'difference', display: 'stat'}}, 
    {id: 'totalDifferenceChart', title: 'Total Volume Chart', description: 'A chart widget displaying your household\'s total water consumption progress for the last month. You can later change it to daily, weekly or yearly consumption.', devType:'METER', data: {type: 'total', metric: 'difference', display: 'chart'}}, 
    {id: 'last', title: 'Last shower', description: 'A widget displaying the last shower recorded for all your devices.', devType: 'AMPHIRO', data: {type: 'last', metric: 'volume', display: 'chart'}},
    {id:'efficiencyEnergy', title: 'Energy efficiency', description: 'A widget displaying your shower energy score for the last 10 showers. You can later change this to see the energy efficiency for the last 20 or 50 showers.', devType: 'AMPHIRO', data: {type: 'efficiency', metric: 'energy', display: 'stat'}},
    {id: 'breakdown', title: 'Water breakdown', description: 'A chart widget displaying your computed water use per household appliance.', devType: 'METER', data: {type: 'breakdown', metric: 'difference', display: 'chart'}},
    {id: 'forecast', title: 'Forecast', description: 'A chart widget depicting our estimations for your water use for the next month based on your use so far! You can later change this to see estimations for the next day, week, or year.', devType: 'METER', data: {type: 'forecast', metric: 'difference', display: 'chart'}},
    {id: 'comparison', title: 'Comparison', description: 'A widget showing your consumption in comparison to others, like your neighbors or your city average for the last month. You can later change this to see comparison data for the current day, week, or year.', devType: 'METER', data: {type: 'comparison', metric: 'difference', display: 'chart'}},
    {id: 'budget', title: 'Daily Budget', description: 'A widget showing your consumption based on your daily budget.', devType: 'METER', data: {type: 'budget', metric: 'difference', display: 'chart', period: 'day'}}
  ]
};

