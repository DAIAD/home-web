
var React = require('react');
var {Panel, PanelGroup} = require('react-bootstrap');

var UtilityView = require('./overview-utility');
var GroupPerEfficiencyView = require('./overview-per-efficiency');

var Overview = React.createClass({
  render: function () {
    return (
      <PanelGroup accordion defaultActiveKey="utility">
        <Panel header="Water Consumption" eventKey="utility">
          <UtilityView />
        </Panel>
        <Panel header="Water Consumption - Customer Efficiency" eventKey="per-efficiency">
          <GroupPerEfficiencyView />
        </Panel>
        <Panel header="Water Consumption - Household Members" eventKey="per-members">
          <GroupPerMembersView />
        </Panel>
      </PanelGroup>    
    );
  },

});

// Export

module.exports = {
  Overview,
  UtilityView,
  GroupPerEfficiencyView,
  //GroupPerSizeView,
  //GroupPerMembersView,
  //GroupPerIncomeView,
};
