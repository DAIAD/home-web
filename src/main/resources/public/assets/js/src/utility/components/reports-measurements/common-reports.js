var React = require('react');

var views = require('./views');
var reports = require('./unit-reports');

var ReportByDay = (reportProps) => (
  <reports.ReportByDay {...reportProps}>
    <views.Summary />
    <views.SimpleChart />
    <views.ComparisonChart />
  </reports.ReportByDay>
);

var ReportByWeek = (reportProps) => (
  <reports.ReportByWeek {...reportProps}>
    <views.Summary />
    <views.SimpleChart />
    <views.ComparisonChart />
  </reports.ReportByWeek>
);

var ReportByMonth = (reportProps) => (
  <reports.ReportByMonth {...reportProps}>
    <views.Summary />
    <views.SimpleChart />
    <views.ComparisonChart />
  </reports.ReportByMonth>
);

var ReportByYear = (reportProps) => (
  <reports.ReportByYear {...reportProps}>
    <views.Summary />
    <views.SimpleChart />
    <views.ComparisonChart />
  </reports.ReportByYear>
);

module.exports = {ReportByDay, ReportByWeek, ReportByMonth, ReportByYear};
