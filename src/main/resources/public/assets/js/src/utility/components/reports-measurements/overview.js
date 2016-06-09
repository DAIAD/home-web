
var React = require('react');
var Bootstrap = require('react-bootstrap');

var {Panel, PanelGroup, ListGroup, ListGroupItem} = Bootstrap;

var {ReportByDay, ReportByWeek, ReportByMonth} = require('./unit-view-containers');

var OverviewAsAccordion = React.createClass({
  
  getInitialState: function () {
    return {
      activeKey: 'utility',
    };
  },
  
  shouldComponentUpdate: function (nextProps) {
    return (
      (nextProps.now != this.props.now) ||
      (nextProps.field != this.props.field)
    );
  },

  render: function () {
    var {now, field, uom, reports} = this.props;
    var visible = (k) => (this.state.activeKey == k);
    
    var viewsPerReport = ['summary', 'simple-chart', 'comparison-chart'];
    var commonProps = {now, field, uom, views: viewsPerReport};
    
    return (
      <PanelGroup accordion onSelect={this._selectPanel} activeKey={this.state.activeKey}>
        
        <Panel id="overview-utility" header="Water Consumption - Utility" eventKey="utility"
         >
          <ListGroup fill>
            <ListGroupItem>
              <h4>Last Day</h4>
              <ReportByDay {...commonProps} target={null} reportKey='utility' report={reports.day} visible={visible('utility')} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Week</h4>
              <ReportByWeek {...commonProps} target={null} reportKey='utility' report={reports.week} visible={visible('utility')} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Month</h4>
              <ReportByMonth {...commonProps} target={null} reportKey='utility' report={reports.month} visible={visible('utility')} />
            </ListGroupItem>
          </ListGroup>
        </Panel>
        
        <Panel id="overview-per-efficiency" header="Water Consumption - Customer Efficiency" eventKey="per-efficiency">
          <div>Todo</div> 
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
    return true;
  },

});

// Export

module.exports = {
  OverviewAsAccordion,
  ReportByDay, 
  ReportByWeek, 
  ReportByMonth,
};
