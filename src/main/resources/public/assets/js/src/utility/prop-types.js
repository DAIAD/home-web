
var React = require('react');

var population = require('./model/population');
var TimeSpan = require('./model/timespan');

var PropTypes = React.PropTypes;

// A collection of commonly used (component) prop types

var populationPropType = PropTypes.oneOfType([
  PropTypes.instanceOf(population.Group),
  PropTypes.instanceOf(population.Cluster),
]);

var seriesPropType = PropTypes.shape({
  population: populationPropType,
  metric: PropTypes.string,
  source: PropTypes.string,
  ranking: PropTypes.object,
  data: PropTypes.arrayOf(
    PropTypes.arrayOf(PropTypes.number)
  ),
});

var timespanPropType = PropTypes.oneOfType([
  PropTypes.oneOf(TimeSpan.commonNames()),
  (props, propName, componentName) => ( 
    (PropTypes.arrayOf(PropTypes.number)(props, propName, componentName)) ||
    ((props[propName].length == 2)? null : (
      new Error(propName + ' should be an array of length 2')))
  ),
]);

var reportPropType = PropTypes.shape({
  level: PropTypes.string.isRequired,
  reportName: PropTypes.string.isRequired,
  startsAt: PropTypes.string.isRequired,
  duration: PropTypes.array.isRequired,
}); 

// The shape of the global configuration object (passed via props or context)
var configPropType = PropTypes.shape({
  utility: PropTypes.shape({
    clusters: PropTypes.arrayOf(PropTypes.shape({
      groups: PropTypes.array,
      key: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
    })),
    key: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }),
  reports: PropTypes.shape({
    levels: PropTypes.object,
    byType: PropTypes.object
  }),
  overview: PropTypes.shape({
    reports: PropTypes.object,
    sections: PropTypes.object,
  }),
});

module.exports = {
  timespanPropType,
  populationPropType,
  seriesPropType,
  reportPropType,
  configPropType,
};


