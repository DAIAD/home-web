
var React = require('react');
var Bootstrap = require('react-bootstrap');

var {Panel, PanelGroup, ListGroup, ListGroupItem} = Bootstrap;

var {ReportByDay, ReportByWeek, ReportByMonth, ReportByYear} = require('./common-reports');

var UtilityOverviewPanel = React.createClass({
  
  render: function () {
    var {now, field, source, uom, reports} = this.props;
    
    var reportProps = {
      now, 
      field,
      source,
      uom,
      reportKey: 'utility',
      target: null,
    };

    return (
      <Panel id="overview-utility" header="Water Consumption - Utility">
        <ListGroup fill>
          <ListGroupItem>
            <h4>Last Day</h4>
            <ReportByDay {...reportProps} report={reports.day} />
          </ListGroupItem>
          <ListGroupItem>
            <h4>Last Week</h4>
            <ReportByWeek {...reportProps} report={reports.week} />
          </ListGroupItem>
          <ListGroupItem>
            <h4>Last Month</h4>
            <ReportByMonth {...reportProps} report={reports.month} />
          </ListGroupItem>
        </ListGroup>
      </Panel>
    );
  }
});

var OverviewAccordion = React.createClass({
  
  getInitialState: function () {
    return {
      activeKey: 'utility',
    };
  },
  
  shouldComponentUpdate: function (nextProps, nextState) {
    return (
      (nextProps.now != this.props.now) ||
      (nextProps.field != this.props.field) ||
      (nextProps.source != this.props.source) ||
      (nextState.activeKey != this.state.activeKey)
    );
  },

  render: function () {
    var {now, field, source, uom, reports} = this.props;
    var visible = (k) => (this.state.activeKey == k);
    
    var commonProps = {source, field, uom, now};

    var reportProps = {
      utility: {
        ...commonProps,
        reportKey: 'utility',
        target: null,
        visible: visible('utility')
      },
      perEfficiency: {
        ...commonProps,
        reportKey: 'per-efficiency',
        target: null, // Fixme population.Cluster
        visible: visible('per-efficiency')
      },
    };
    
    return (
      <PanelGroup accordion onSelect={this._selectPanel} activeKey={this.state.activeKey}>
        
        <Panel id="overview-utility" header="Water Consumption - Utility" eventKey="utility">
          <ListGroup fill>
            <ListGroupItem>
              <h4>Last Day</h4>
              <ReportByDay {...reportProps.utility} report={reports.day} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Week</h4>
              <ReportByWeek {...reportProps.utility} report={reports.week} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Month</h4>
              <ReportByMonth {...reportProps.utility} report={reports.month} />
            </ListGroupItem>
            <ListGroupItem>
              <h4>Last Year</h4>
              <ReportByYear {...reportProps.utility} report={reports.year} />
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
  OverviewAccordion,
  UtilityOverviewPanel,
};
