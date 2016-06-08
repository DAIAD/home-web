
var React = require('react');
var Bootstrap = require('react-bootstrap');

var {Panel, PanelGroup, ListGroup, ListGroupItem} = Bootstrap;

var UtilityView = require('./overview-utility');
var GroupPerEfficiencyView = require('./overview-per-efficiency');

var OverviewAsAccordion = React.createClass({
  
  getInitialState: function () {
    return {
      activeKey: 'utility',
    };
  },
        
  render: function () {
    var {now} = this.props;
    var isVisible = (k) => (this.state.activeKey == k);
    
    return (
      <PanelGroup accordion onSelect={this._selectPanel} activeKey={this.state.activeKey}>
        <Panel id="overview-utility" header="Water Consumption - Utility" eventKey="utility">
          <UtilityView now={now} visible={isVisible('utility')} />
        </Panel>
        <Panel id="overview-per-efficiency" header="Water Consumption - Customer Efficiency" eventKey="per-efficiency">
          <GroupPerEfficiencyView now={now} visible={isVisible('per-efficiency')} />
        </Panel>
        {/*
        <Panel header="Water Consumption - Household Members" eventKey="per-members">
          <GroupPerMembersView now={now} />
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
  UtilityView,
  GroupPerEfficiencyView,
  //GroupPerSizeView,
  //GroupPerMembersView,
  //GroupPerIncomeView,
};
