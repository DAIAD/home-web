var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var Select = require('react-select');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var RadialChart = require('../RadialChart');

var { jobChangeIndex, getJobs, executionChangeIndex, getExecutions, filterExecutionByJobName, 
      filterExecutionByExitCode, clearExecutionFilter, enableJob, disableJob,
      launchJob } = require('../../actions/SchedulerActions');

var handleKeyPress = function(e) {
  if (e.key === 'Enter') {
    this.refresh();
  }
};

var Scheduler = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
  componentWillMount : function() {
    if(this.props.scheduler.data.jobs.items == null) {
      this.props.actions.getJobs();
    }
    if(this.props.scheduler.data.executions.items ==null) {
      this.props.actions.getExecutions();
    }
  },

  onExecutionPageIndexChange: function(index) {
    this.props.actions.executionChangeIndex(index);
  },
  
  onJobPageIndexChange: function(index) {
    this.props.actions.jobChangeIndex(index);
  },
  
  filterExecutionByJobName: function(e) {
    this.props.actions.filterExecutionByJobName(e.value === 'UNDEFINED' ? null : e.value);
  },
  
  filterExecutionByExitCode: function(e) {
    this.props.actions.filterExecutionByExitCode(e.value === 'UNDEFINED' ? null : e.value);
  },
  
  clearExecutionFilter: function(e) {
    this.props.actions.clearExecutionFilter();
  },
 
  refreshExecutions: function(e) {
    this.props.actions.getExecutions();
  },
  
  refreshJobs: function(e) {
    this.props.actions.getJobs();
  },
  
	render: function() {
    var _t = this.context.intl.formatMessage;
    
    var jobs = this.props.scheduler.data.jobs;

    var jobNames = [
      { value: 'UNDEFINED', label: '-' }
    ];
    if(jobs.items) {
      jobNames = jobs.items.reduce(function(newArray, currentValue, currentIndex) {    
        newArray.push({ value: currentValue.name, label: currentValue.name });
      
        return newArray;
      }, [
        { value: 'UNDEFINED', label: '-' }
      ]);
    }

    var jobTableConfig = {
      fields: [{
        name: 'id',
        title: 'Id',
        hidden: true
      }, {
        name: 'category',
        title: 'Category'
      }, {
        name: 'container',
        title: 'Framwork'
      }, {
        name: 'name',
        title: 'Name'
      }, {
        name: 'description',
        title: 'Description'
      }, {
        name: 'lastExecution',
        title: 'Last Execution',
        type: 'datetime'
      }, {
        name: 'nextExecution',
        title: 'Next Execution',
        type: 'datetime'
      }, {
        name: 'enable',
        type:'action',
        icon: function(field, row) {
          return 'clock-o';
        },
        color: function(field, row) {
          if(!row.schedule) {
            return '#000000';
          }
          switch(row.schedule.type) {
            case 'CRON': case 'PERIOD':
              if(row.enabled) {
                return '#000000';
              } else {
                return '#9E9E9E';
              }
              break;
            default: 
              return '#000000';
          }          
        },
        handler: (function(field, row) {
          switch(row.schedule.type) {
            case 'CRON': case 'PERIOD':
              if(row.enabled) {
                this.props.actions.disableJob(row.id);
              } else {
                this.props.actions.enableJob(row.id);
              }
              break;
            default: 
              // No action is required
              break;
          }
        }).bind(this),
        visible: function(field, row) {
          if(!row.schedule) {
            return false;
          }
          switch(row.schedule.type) {
            case 'CRON': case 'PERIOD':
              return true;
            default: 
              return false;
          }
        }
      }, {
        name: 'execute',
        type:'action',
        icon: function(field, row) {
          if(row.running) {
            return 'cogs';
          }
          return 'play';
        },
        color: function(field, row) {
          if(row.running) {
            return '#51A351';
          }
          return '#000000';
        },
        handler: (function(field, row) {
          if(!row.running) {
            this.props.actions.launchJob(row.id);
          }
        }).bind(this)
      }],
      rows: jobs.items || [],
      pager: {
        index: jobs.index || 0,
        size: jobs.size || 10,
        count:jobs.total || 0,
        mode: Table.PAGING_CLIENT_SIDE
      }
    };
    
    var executions = this.props.scheduler.data.executions;
    
    var executionTableConfig = {
      fields: [{
        name: 'jobId',
        title: 'jobId',
        hidden: true
      }, {
        name: 'instanceId',
        title: 'instanceId',
        hidden: true
      }, {
        name: 'executionId',
        title: 'executionId',
        hidden: true
      }, {
        name: 'jobName',
        title: 'Name'
      }, {
        name: 'startedOn',
        title: 'Started On',
        type: 'datetime'
      }, {
        name: 'completedOn',
        title: 'Completed On',
        type: 'datetime'
      }, {
        name: 'statusCode',
        title: 'Status Code',
        align: 'center',
        width: 120,
        className: function(value) {
          switch(value) {
            case 'FAILED':
              return 'log_error';
            case 'COMPLETED':
              return 'log_success';
            case 'ABANDONED':
              return 'log_warn';
            default:
              return 'log_debug';
          }
          return '';
        }
      }, {
        name: 'exitCode',
        title: 'Exit Code',
        align: 'center',
        width: 120,
        className: function(value) {
          switch(value) {
            case 'FAILED':
              return 'log_error';
            case 'COMPLETED':
              return 'log_success';
            default:
              return 'log_debug';
          }
          return '';
        }
      }, {
        name: 'view',
        type:'action',
        icon: 'search',
        visible: function(field, row) {
          return (row.exitCode === 'FAILED');
        },
        handler: function(field, row) {

        }
      }],
      rows: executions.items || [],
      pager: {
        index: executions.index || 0,
        size: executions.size || 10,
        count: executions.total || 0,
        mode: Table.PAGING_SERVER_SIDE
      }
    };

    const jobDataNotFound = (
      <span>No jobs found.</span>
    );
    
    const executionDataNotFound = (
      <span>No executions found.</span>
    );

    const jobTableHeader = (
      <span>
        <i className='fa fa-cogs fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Jobs</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.refreshJobs}>
            <i className='fa fa-refresh fa-fw'></i>
          </Bootstrap.Button>
        </span>
      </span>
    );

    const executionFilterHeader = (
      <span>
        <i className='fa fa-history fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Executions</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );

    var resetButton = ( <div />);

    if((this.props.scheduler.query.execution.jobName) ||
       (this.props.scheduler.query.execution.exitCode)) {
      resetButton = (
        <div style={{float: 'right', marginLeft: 20}}>
          <Bootstrap.Button bsStyle='default' onClick={this.clearExecutionFilter}>Reset</Bootstrap.Button>
        </div>
      );
    }
    
    return (
      <div className='container-fluid' style={{ paddingTop: 10 }}>
        <div className='row'>
          <div className='col-md-12'>
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className='row'>
          <div className='col-md-12'>
            <Bootstrap.Panel header={jobTableHeader}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem> 
                  <Table  data={jobTableConfig} 
                          onPageIndexChange={this.onJobPageIndexChange}
                          template={{empty : jobDataNotFound}}
                  ></Table>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
        <div className='row'>
          <div className='col-md-12'>
            <Bootstrap.Panel header={executionFilterHeader }>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <div className='row'>
                    <div className='col-md-2'>
                      <Select name='jobName'
                              value={this.props.scheduler.query.execution.jobName || 'UNDEFINED'}
                              options={jobNames}
                              onChange={this.filterExecutionByJobName}
                              clearable={false} 
                              searchable={false} className='form-group'/>
                      <span className='help-block'>Filter by job name</span>  
                    </div>
                    <div className='col-md-2'>
                      <Select name='exitCode'
                              value={this.props.scheduler.query.execution.exitCode || 'UNDEFINED'}
                              options={[
                                { value: 'UNDEFINED', label: '-' },
                                { value: 'UNKNOWN', label: 'UNKNOWN' },
                                { value: 'EXECUTING', label: 'EXECUTING' },
                                { value: 'COMPLETED', label: 'COMPLETED' },
                                { value: 'FAILED', label: 'FAILED' },
                                { value: 'STOPPED', label: 'STOPPED' }
                              ]}
                              onChange={this.filterExecutionByExitCode}
                              clearable={false} 
                              searchable={false} className='form-group'/>
                      <span className='help-block'>Filter by exit code</span>  
                    </div>
                    <div className='col-md-4' style={{float: 'right'}}>
                      {resetButton}
                      <div style={{float: 'right'}}>
                        <Bootstrap.Button bsStyle='primary' onClick={this.refreshExecutions}>Refresh</Bootstrap.Button>
                      </div>
                    </div>
                  </div>
                </Bootstrap.ListGroupItem>
                  <Bootstrap.ListGroupItem> 
                  <Table  data={executionTableConfig} 
                          onPageIndexChange={this.onExecutionPageIndexChange}
                          template={{empty : executionDataNotFound}}
                  ></Table>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
      </div>
    );
  }
});

Scheduler.icon = 'clock-o';
Scheduler.title = 'Section.Scheduler';

function mapStateToProps(state) {
  return {
      scheduler: state.scheduler,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { 
                jobChangeIndex, 
                getJobs, 
                executionChangeIndex, 
                getExecutions, 
                filterExecutionByJobName, 
                filterExecutionByExitCode,
                clearExecutionFilter,
                enableJob,
                disableJob,
                launchJob
              }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Scheduler);
