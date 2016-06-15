// Define views that can be enabled (as drop-in) on unit reports. 
// A view instance is intended to be owned (as a child) by a report. It is up to the 
// report component how to actually present these views.

var Summary = ({className, numericFormat}) => (null);

Summary.defaultProps = {
  className: 'report-view summary',
  numericFormat: '0.0a',
};

var SimpleChart = ({className}) => (null);

SimpleChart.defaultProps = {
  className: 'report-view simple-chart', 
};

var ComparisonChart = ({className}) => (null);

ComparisonChart.defaultProps = {
  className: 'report-view comparison-chart', 
};

var ForecastingChart = ({className}) => (null);

ForecastingChart.defaultProps = {
  className: 'report-view forecasting-chart', 
};

module.exports = {Summary, SimpleChart, ComparisonChart, ForecastingChart};
