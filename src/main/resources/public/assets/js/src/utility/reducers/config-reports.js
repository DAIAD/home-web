
var ActionTypes = require('../action-types');

var initialState = {
  
  // The  level of detail
  levels: {
    'hour': {bucket: 'hour', duration: [1, 'h']},
    'day': {bucket: 'day', duration: [1, 'd']},
    'week': {bucket: 'isoweek', duration: [1, 'w']},
    'month': {bucket: 'month', duration: [1, 'M']},
    'quarter': {bucket: 'quarter', duration: [1, 'Q']},
    'year': {bucket: 'year', duration: [1, 'Y']},
  },
  
  // Describe types of reports
  byType: { 
    
    // Measurements //
    
    measurements: {
      title: 'Measurements',
      
      // The data sources for our measurements
      sources: {
        'meter': {name: 'Meter', title: 'Meter (SWM)'},
        'device': {name: 'Device', title: 'Device (B1)'},
      },

      // Metrics provided
      metrics: ['SUM', 'COUNT', 'AVERAGE', 'MIN', 'MAX'],
      
      // What physical quantities are being measured
      fields: {
        'volume': {
          name: 'Volume',
          title: 'Water Consumption',
          unit: 'lt',
          sources: ['meter', 'device'],
        },
        'energy': {
          name: 'Energy',
          title: 'Energy Consumption',
          unit: 'kWh',
          sources: ['device'],
        },
      },
      
      // Report on different levels of detail 
      levels: { 
        'hour': {
          name: 'hour',
          title: 'Hour',
          description: 'Report over hour', // time unit of 1 hour
          reports: {
            'avg': {
              title: 'Average of hourly consumption',
              description: 'The average hourly consumption',
              granularity: 'HOUR',
              timespan: 'today',
              metrics: ['AVERAGE'],
              consolidate: 'AVERAGE',
            },
            'sum': {
              title: 'Total hourly consumption',
              description: 'The total hourly consumption',
              granularity: 'HOUR',
              timespan: 'today',
              metrics: ['SUM'],
              consolidate: 'AVERAGE',
            },
          },
        },
        'day': {
          name: 'day',
          title: 'Day',
          description: 'Report over day', // time unit of 1 day
          reports: {
            'avg': {
              title: 'Average of daily consumption',
              description: 'The average daily consumption',
              granularity: 'DAY',
              timespan: 'month',
              metrics: ['AVERAGE'],
              consolidate: 'AVERAGE',
            },
            'sum': {
              title: 'Total daily consumption',
              description: 'The total daily consumption',
              granularity: 'DAY',
              timespan: 'month',
              metrics: ['SUM'],
              consolidate: 'AVERAGE',
            },
          },
        },
        'week': {
          name: 'week',
          title: 'Week',
          description: 'Report over week', // time unit of 1 week
          reports: {
            'avg': {
              title: 'Average of weekly consumption',
              description: 'The average weekly consumption',
              granularity: 'WEEK',
              timespan: 'quarter-1',
              metrics: ['AVERAGE'],
              consolidate: 'AVERAGE',
            },
            'sum': {
              title: 'Total weekly consumption',
              description: 'The total weekly consumption',
              granularity: 'WEEK',
              timespan: 'quarter-1',
              metrics: ['SUM'],
              consolidate: 'AVERAGE',
            },
            'avg-daily-avg': {
              // Note This will always be ("avg"/7), over the same population
              title: 'Average of daily consumption',
              description: 'The weekly average of the average daily consumption',
              granularity: 'DAY',
              timespan: 'quarter-1',
              metrics: ['AVERAGE'],
              consolidate: 'AVERAGE',
            },
            'avg-daily-peak': {
              title: 'Peak of daily consumption',
              description: 'The weekly average of the daily min/max consumption',
              granularity: 'DAY',
              timespan: 'quarter-1',
              metrics: ['MIN', 'MAX'],
              consolidate: 'AVERAGE',
            },
            'top-k': {
              title: 'Top consumers',
              description: 'The weekly top/bottom consumers',
              granularity: 'WEEK',
              ranking: [
                {type: 'TOP', metric: 'SUM', limit: 3},
                {type: 'BOTTOM', metric: 'SUM', limit: 3},
              ],
              timespan: 'quarter-1',
              metrics: null, // n/a
              consolidate: 'AVERAGE', // n/a
            }
          },
        },
        'month': {
          name: 'month',
          title: 'Month',
          description: 'Report over month', // time unit of 1 month
          reports: {
            'avg': {
              title: 'Average of monthly consumption',
              description: 'The average monthly consumption',
              granularity: 'MONTH',
              timespan: 'quarter',
              metrics: ['AVERAGE'],
              consolidate: 'AVERAGE',
            },
            'sum': {
              title: 'Total monthly consumption',
              description: 'The total monthly consumption',
              granularity: 'MONTH',
              timespan: 'quarter',
              metrics: ['SUM'],
              consolidate: 'AVERAGE',
            },
          },
        },
        'year': {
          name: 'year',
          title: 'Year',
          description: 'Report over year', // time unit of 1 year
          reports: {
            'avg': {
              title: 'Average of yearly consumption',
              description: 'The average yearly consumption',
              granularity: 'YEAR',
              timespan: null,
              metrics: ['AVERAGE'],
              consolidate: 'AVERAGE',
            },
            'sum': {
              title: 'Total yearly consumption',
              description: 'The total yearly consumption',
              granularity: 'YEAR',
              timespan: null,
              metrics: ['SUM'],
              consolidate: 'AVERAGE',
            },
          }
        }
      },
    },

    // System Utilization //

    system: {
      title: 'System Utilization',
      levels: {
        'week': {
          name: 'week',
          title: 'Week',
          description: 'Report over week', // time unit of 1 week
          reports: {
            'data-transmission': {
              title: 'Data Transmission',
              description: 'Time (days) between 2 consecutive data transmissions of participants',
              // Todo
              // Avg time (days) between 2 consecutive data transmissions of participants
              // Max time (days) between 2 consecutive data transmissions (Top-k articipants)
            },
          },
        },
      },
    },
  
  },
};

var reduce = function (state, action) {
  
  // Note This part (configuration for reports) does not ever change
  return initialState;

};

module.exports = reduce;
