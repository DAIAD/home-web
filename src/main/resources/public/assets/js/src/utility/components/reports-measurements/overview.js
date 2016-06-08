
var React = require('react');
var Bootstrap = require('react-bootstrap');

var {Panel, PanelGroup, ListGroup, ListGroupItem} = Bootstrap;

var UtilityReport = require('./overview-utility');
var GroupPerEfficiencyReport = require('./overview-per-efficiency');

var OverviewAsAccordion = React.createClass({
  
  getInitialState: function () {
    return {
      activeKey: 'utility',
    };
  },
        
  render: function () {
    var {now, field, uom, reports} = this.props;
    var isVisible = (k) => (this.state.activeKey == k);
    var reportProps = {now, field, uom, reports};

    return (
      <PanelGroup accordion onSelect={this._selectPanel} activeKey={this.state.activeKey}>
        <Panel id="overview-utility" 
          header="Water Consumption - Utility" 
          eventKey="utility"
         >
          <UtilityReport {...reportProps} visible={isVisible('utility')} />
        </Panel>
        <Panel id="overview-per-efficiency" 
          header="Water Consumption - Customer Efficiency" 
          eventKey="per-efficiency"
         >
          <GroupPerEfficiencyReport {...reportProps} visible={isVisible('per-efficiency')} />
        </Panel>
        {/*
        <Panel header="Water Consumption - Household Members" eventKey="per-members">
          <GroupPerMembersReport {...reportProps} visible={isVisible('per-members')} />
        </Panel>
        */}
      </PanelGroup>    
    );
  },

  _selectPanel: function (key) {
    if (this.state.activeKey != key)
      this.setState({activeKey: key});
    return false;
  },

});

// Export

module.exports = {
  OverviewAsAccordion,
  UtilityReport,
  GroupPerEfficiencyReport,
};
