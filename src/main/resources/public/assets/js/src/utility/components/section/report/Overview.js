var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');

var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../../Breadcrumb');
var Table = require('../../Table');
var Chart = require('../../Chart');
var DropDown = require('../../DropDown');
var MessageAlert = require('../../AlertDismissable');

var errorsCodes = require('../../../constants/Errors');
var successCodes = require('../../../constants/Successes');


var { getActivity, setFilter, getSessions, getMeters, resetUserData, exportUserData, showAddUserForm,
      hideAddUserForm, addUserSelectUtility, addUserSelectGenderMale, addUserSelectGenderFemale, addUserFillForm,
      addUserValidationsErrorsOccurred, addUserShowMessageAlert, addUserHideErrorAlert, addUser, addUserGetUtilities } = require('../../../actions/AdminActions');

var Overview = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  componentWillMount : function() {
    if(this.props.admin.activity === null) {
      this.props.actions.getActivity();
    }
   this.props.actions.addUserGetUtilities();
  },

  setFilter: function(e) {
    this.props.actions.setFilter(this.refs.filter.getValue());
  },

  clearFilter: function(e) {
    this.props.actions.setFilter('');
  },

  resetUserData: function(e) {
    this.props.actions.resetUserData();
  },

  refreshParticipants: function(e) {
    this.props.actions.getActivity();
  },

  onPageIndexChange: function(index) {

  },

  validateNewUserForm: function(firstName, lastName, email, gender, address, postalCode){
    var errors = [];

    if (!firstName){
      errors.push({code: errorsCodes['ValidationError.User.NO_FIRST_NAME']});
    } else if (firstName.length > 40){
      errors.push({code: errorsCodes['ValidationError.User.TOO_LONG_FIRST_NAME']});
    }

    if (!lastName){
      errors.push({code: errorsCodes['ValidationError.User.NO_LAST_NAME']});
    } else if (lastName.length > 70){
      errors.push({code: errorsCodes['ValidationError.User.TOO_LONG_LAST_NAME']});
    }

    if (!email){
      errors.push({code: errorsCodes['ValidationError.User.NO_EMAIL']});
    } else if (email.length > 100){
      errors.push({code: errorsCodes['ValidationError.User.TOO_LONG_EMAIL']});
    } else {
      var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        if (!re.test(email)){
          errors.push({code: errorsCodes['ValidationError.User.INVALID_EMAIL']});
        }
    }

    if (!gender){
      errors.push({code: errorsCodes['ValidationError.User.NO_GENDER']});
    }

    if (address && address.length > 90){
      errors.push({code: errorsCodes['ValidationError.User.TOO_LONG_ADDRESS']});
    }

    if (this.props.admin.addUser.selectedUtility === null){
      errors.push({code: errorsCodes['ValidationError.User.NO_UTILITY']});
    }

    if (postalCode && postalCode.length > 10){
      errors.push({code: errorsCodes['ValidationError.User.TOO_LONG_POSTAL_CODE']});
    }

    return errors;
  },

  processAddNewUserForm: function(){
    var gender = null;
    if (this.refs.genderMale.getChecked() === true){
      gender = 'MALE';
    } else if (this.refs.genderFemale.getChecked() === true){
      gender = 'FEMALE';
    }
    var inputFieldsFormValues = {
        firstName : this.refs.firstName.getValue(),
        lastName : this.refs.lastName.getValue(),
        email : this.refs.email.getValue(),
        gender : gender,
        address : this.refs.address.getValue(),
        postalCode : this.refs.postalCode.getValue()
    };

    var errors = this.validateNewUserForm(
          this.refs.firstName.getValue(),
          this.refs.lastName.getValue(),
          this.refs.email.getValue(),
          gender,
          this.refs.address.getValue(),
          this.refs.postalCode.getValue()
        );

    if (errors.length === 0){
      this.props.actions.addUserFillForm(inputFieldsFormValues);
      var userInfo = {
          firstName : this.refs.firstName.getValue(),
          lastName : this.refs.lastName.getValue(),
          email : this.refs.email.getValue(),
          gender : gender,
          address : this.refs.address.getValue() === '' ? null : this.refs.address.getValue(),
          utilityId : this.props.admin.addUser.selectedUtility.value,
          postalCode : this.refs.postalCode.getValue() === '' ? null : this.refs.postalCode.getValue()
      };

      this.props.actions.addUser(userInfo);
    } else {
      this.props.actions.addUserValidationsErrorsOccurred(errors);
    }
  },

  render: function() {
    var self = this;
    var _t = this.context.intl.formatMessage;


    const groupTitle = (
      <span>
        <i className='fa fa-group fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Participants</span>
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.refreshParticipants}>
            <i className='fa fa-refresh fa-fw'></i>
          </Bootstrap.Button>
        </span>
      </span>
    );

    var closeSessionChartButton = null;
    if(this.props.admin.user.name) {
      closeSessionChartButton = (
        <span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
          <Bootstrap.Button bsStyle='default' className='btn-circle' onClick={this.resetUserData}>
            <i className='fa fa-rotate-left fa-fw'></i>
          </Bootstrap.Button>
        </span>
      );
    }

    var titleText = 'Activity';
    if(this.props.admin.user.meters) {
      titleText = this.props.admin.user.name + ' - Last 30 days';
    } else if(this.props.admin.user.devices) {
      titleText = this.props.admin.user.name + ' - Last 100 sessions';
    }
    const chartTitle = (
      <span>
        <i className='fa fa-bar-chart fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>{ titleText }</span>
        {closeSessionChartButton}
      </span>
    );

    var rows = [];
    var total = 0, registered = 0, login = 0, paired = 0, uploaded = 0, assigned = 0;

    if(this.props.admin.activity!==null) {
      var records = this.props.admin.activity;
      for(var i=0, count=records.length; i<count; i++) {
        total++;
        if(records[i].accountRegisteredOn){
          registered++;
        }
        if(records[i].lastLoginSuccess){
          login++;
        }
        if(records[i].leastAmphiroRegistration) {
          paired++;
        }
        if(records[i].leastMeterRegistration) {
          assigned++;
        }
        if(records[i].lastDataUploadSuccess) {
          uploaded++;
        }
        if((!this.props.admin.filter) || (records[i].username.indexOf(this.props.admin.filter) !== -1)) {
          rows.push({
            id: records[i].id,
            key: records[i].key,
            username: records[i].username || '',
            accountRegisteredOn: (records[i].accountRegisteredOn ? new Date(records[i].accountRegisteredOn) : null),
            numberOfAmphiroDevices: records[i].numberOfAmphiroDevices,
            numberOfMeters: records[i].numberOfMeters,
            leastAmphiroRegistration: (records[i].leastAmphiroRegistration ? new Date(records[i].leastAmphiroRegistration) : null),
            leastMeterRegistration: (records[i].leastMeterRegistration ? new Date(records[i].leastMeterRegistration) : null),
            lastLoginSuccess: (records[i].lastLoginSuccess ? new Date(records[i].lastLoginSuccess) : null)
          });
        }
      }
    }
    var model = {
        table: null,
        chart: {
          options: null,
          data: null,
          type: 'bar'
        }
    };

    var series = [], data = [];
    if(!this.props.admin.isLoading) {
      if(this.props.admin.user.devices) {
        model.chart.type = 'bar';
        model.chart.options = {
          tooltip: {
              show: true
          },
          dataZoom: {
            show: true
          }
        };


        var devices = this.props.admin.user.devices;

        var maxSerieSize = 0, d;
        for(d=0; d<devices.length; d++) {
          if(devices[d].sessions.length > maxSerieSize) {
            maxSerieSize = devices[d].sessions.length;
          }
        }

        for(d=0; d<devices.length; d++) {
          var device = devices[d];
          data = [];

          var index = 1;
          for(var s=0; s < maxSerieSize; s++) {
            data.push({
              volume: (s < device.sessions.length ? device.sessions[s].volume : 0.0),
              id:  index
            });
            index++;
          }

          series.push({
            legend: device.name || device.deviceKey,
            xAxis: 'id',
            yAxis: 'volume',
            data: data,
            yAxisName: 'Volume (lt)'
          });
        }

        model.chart.data = {
          series: series
        };
      } else if(this.props.admin.user.meters) {
        model.chart.type = 'bar';
        model.chart.options = {
            tooltip: {
                show: true
            },
            dataZoom: {
              show: true,
              format: 'day'
            }
        };

        var meters = this.props.admin.user.meters;

        for(var m=0; m<meters.length; m++) {
          var meter = meters[m];
          data = [];

          for(var v=0; v < meter.values.length; v++) {
            data.push({
              volume: meter.values[v].difference,
              date: new Date(meter.values[v].timestamp)
            });
          }

          series.push({
            legend: meter.serial || meter.deviceKey,
            xAxis: 'date',
            yAxis: 'volume',
            data: data,
            yAxisName: 'Volume (lt)'
          });
        }

        model.chart.data = {
          series: series
        };

      } else {
        model.chart.type = 'bar';

        var dataPoints = [];

        model.chart.options = {
          tooltip: {
              show: true
          },
          dataZoom: {
            show: false
          },
          itemStyle: {
            normal: {
              label : {
                show: true,
                position: 'top',
                formatter: function (params) {
                  return params.value;
                },
                textStyle: {
                  color: '#565656',
                  fontWeight: 'bold'
                }
              }
            }
          }
        };

        dataPoints.push({
          value: total,
          label: 'Total Participants'
        });
        dataPoints.push({
          value: registered,
          label: 'Registered'
        });
        dataPoints.push({
          value: login,
          label: 'Login'
        });
        dataPoints.push({
          value: paired,
          label: 'Paired'
        });
        dataPoints.push({
          value: assigned,
          label: 'Meters'
        });
        dataPoints.push({
          value: uploaded,
          label: 'Uploaded'
        });

        model.chart.data = {
            series: [{
                legend: 'Trial Activity',
                xAxis: 'label',
                yAxis: 'value',
                data: dataPoints,
                yAxisName: 'Count'
            }]
        };
      }
    }

    model.table = {
        fields: [{
          name: 'id',
          title: 'id',
          hidden: true
        }, {
          name: 'key',
          title: 'key',
          hidden: true
        }, {
          name: 'username',
          title: 'Username',
          link: function(row) {
            if(row.key) {
              return '/user/{key}';
            }
            return null;
          }
        }, {
          name: 'accountRegisteredOn',
          title: 'Registered On',
          type: 'datetime'
        }, {
          name: 'numberOfAmphiroDevices',
          title: '# of Amphiro'
        }, {
          name: 'leastAmphiroRegistration',
          title: 'Amphiro registered on',
          type: 'datetime'
        }, {
          name: 'lastLoginSuccess',
          title: 'Last login on',
          type: 'datetime'
        }, {
          name: 'meter',
          type:'action',
          image: '/assets/images/utility/meter.svg',
          handler: function(field, row) {
            if((row.key) && (row.numberOfMeters > 0)) {
              self.props.actions.getMeters(this.props.row.key, this.props.row.username);
            }
          },
          visible: function(field, row) {
            return ((row.key) && (row.numberOfMeters > 0));
          }
        }, {
          name: 'session',
          type:'action',
          image: '/assets/images/utility/amphiro.svg',
          handler: function(field, row) {
            if((row.key) && (row.numberOfAmphiroDevices > 0)) {
              self.props.actions.getSessions(this.props.row.key, this.props.row.username);
            }
          },
          visible: function(field, row) {
            return ((row.key) && (row.numberOfAmphiroDevices > 0));
          }
        }, {
          name: 'export',
          type:'action',
          icon: 'cloud-download fa-2x',
          color: '#2D3580',
          handler: function(field, row) {
            if((row.key) && ((row.numberOfAmphiroDevices > 0) || (row.numberOfMeters > 0))) {
              self.props.actions.exportUserData(this.props.row.key, this.props.row.username);
            }
          },
          visible: function(field, row) {
            return ((row.key) && ((row.numberOfAmphiroDevices > 0) || (row.numberOfMeters > 0)));
          }
        }],
        rows: rows,
        pager: {
          index: 0,
          size: 10,
          count:rows.length
        }
    };

    var header = null, filter = null, addUserButton = null, table = null, chart = null;
    if(this.props.data !== null) {
      addUserButton = (
        <div className='col-md-4'>
          <Bootstrap.ListGroup>
            <div className='clearfix'>
              <div style={{ float: 'right'}}>
                <Bootstrap.Button bsStyle='success' onClick={this.props.actions.showAddUserForm}>
                  <i className='fa fa-plus' style={{ paddingRight: 5 }}></i>
                  {_t({id : 'Buttons.AddNewUser'})}
                </Bootstrap.Button>
              </div>
            </div>
          </Bootstrap.ListGroup>
        </div>
      );

      filter = (
        <div className='col-md-4'>
          <Bootstrap.Input type='text'
                           id='filter' name='filter' ref='filter'
                           placeholder='Search participants by email ...'
                           onChange={this.setFilter}
                           value={this.props.admin.filter}
                           buttonAfter={
                            <Bootstrap.Button onClick={this.clearFilter} style={{paddingTop: 7, paddingBottom: 7 }}><i className='fa fa-trash fa-fw'></i></Bootstrap.Button>
                          }
          />
        </div>
      );

      table = (
        <div className='row'>
          <div className="col-md-12">
            <Bootstrap.Panel header={groupTitle}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <Table data={model.table} onPageIndexChange={this.onPageIndexChange}></Table>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>);

      chart = (
        <div className='row'>
          <div className="col-md-12">
            <Bootstrap.Panel header={chartTitle}>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <Chart  style={{ width: '100%', height: 400 }}
                          elementClassName='mixin'
                          prefix='chart'
                          type={model.chart.type}
                          options={model.chart.options}
                          data={model.chart.data}/>
            		</Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
      );
    }
    self = this;


    const newUserPanelTitle = (
        <span>
          <i className='fa fa-user-plus fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>{_t({ id:'AddUserForm.PanelTitle'})}</span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
          </span>
        </span>
      );

    var utilityOptions = [];
    if(this.props.admin.addUser.utilities){
      this.props.admin.addUser.utilities.forEach(function (utility, i){
        utilityOptions.push({label: utility.name, value: utility.id, key: utility.id});
      });
    }

    var hideAddNewUserForm = function (){
      self.errors = [];
      self.props.actions.hideAddUserForm();
    };

    var addNewUserForm = (
      <div>
        <Bootstrap.Panel header={newUserPanelTitle}>
          <div className='row'>
            <div className='col-md-6'>
              <Bootstrap.ListGroup>
                  <div className='clearfix'>
                    <Bootstrap.Row>
                      <Bootstrap.Col xs={6}>
                        <Bootstrap.Input
                          type='text'
                          value={this.props.admin.addUser.selectedFirstName}
                          label={_t({ id:'AddUserForm.FirstName.label'}) + ' (*)'}
                          ref='firstName'
                          placeholder={_t({ id:'AddUserForm.FirstName.placeholder'})} />
                      </Bootstrap.Col>
                      <Bootstrap.Col xs={6}>
                        <Bootstrap.Input
                          type='text'
                          value={this.props.admin.addUser.selectedLastName}
                          label={_t({ id:'AddUserForm.LastName.label'}) + ' (*)'}
                          ref='lastName'
                          placeholder={_t({ id:'AddUserForm.LastName.placeholder'})} />
                      </Bootstrap.Col>
                    </Bootstrap.Row>
                    <Bootstrap.Row>
                      <Bootstrap.Col xs={6}>
                        <Bootstrap.Input
                          type='text'
                          value={this.props.admin.addUser.selectedEmail}
                          label={_t({ id:'AddUserForm.E-mail.label'}) + ' (*)'}
                          ref='email'
                          placeholder={_t({ id:'AddUserForm.E-mail.placeholder'})} />
                      </Bootstrap.Col>
                      <Bootstrap.Col xs={6}>
                        <Bootstrap.Input label={_t({ id:'AddUserForm.Gender.label'}) + ' (*)'} wrapperClassName='white-wrapper'>
                            <Bootstrap.Row>
                              <Bootstrap.Col xs={6}>
                                <Bootstrap.Input
                                  type='radio'
                                  checked={this.props.admin.addUser.selectedGender === 'MALE' ? true : false}
                                  onClick={this.props.actions.addUserSelectGenderMale}
                                  ref='genderMale'
                                  label={_t({ id:'AddUserForm.Gender.values.Male'})}
                                  name='gender' />
                              </Bootstrap.Col>
                              <Bootstrap.Col xs={6}>
                                <Bootstrap.Input
                                  type='radio'
                                  onClick={this.props.actions.addUserSelectGenderFemale}
                                  checked={this.props.admin.addUser.selectedGender === 'FEMALE' ? true : false}
                                  ref='genderFemale'
                                  label={_t({ id:'AddUserForm.Gender.values.Female'})}
                                  name='gender' />
                              </Bootstrap.Col>
                            </Bootstrap.Row>
                        </Bootstrap.Input>
                      </Bootstrap.Col>
                    </Bootstrap.Row>
                    <Bootstrap.Row>
                      <Bootstrap.Col xs={6}>
                        <Bootstrap.Input
                          type='text'
                          value={this.props.admin.addUser.selectedAddress}
                          label={_t({ id:'AddUserForm.Address.label'})}
                          ref='address'
                          placeholder={_t({ id:'AddUserForm.Address.placeholder'})}/>
                      </Bootstrap.Col>
                      <Bootstrap.Col xs={6}>
                        <Bootstrap.Input label={_t({ id:'AddUserForm.Utility.label'}) + ' (*)'} wrapperClassName='white-wrapper'>
                          <DropDown
                            title={this.props.admin.addUser.selectedUtility ? this.props.admin.addUser.selectedUtility.label : _t({ id:'AddUserForm.Utility.label'})}
                            options={utilityOptions}
                            onSelect={this.props.actions.addUserSelectUtility}
                            disabled={false}
                          />
                        </Bootstrap.Input>
                      </Bootstrap.Col>
                    </Bootstrap.Row>
                    <Bootstrap.Row>
                      <Bootstrap.Col xs={6}>
                        <Bootstrap.Input
                          type='text'
                          value={this.props.admin.addUser.selectedPostalCode}
                          label={_t({ id:'AddUserForm.PostalCode.label'})}
                          ref='postalCode'
                          placeholder={_t({ id:'AddUserForm.PostalCode.placeholder'})}/>
                      </Bootstrap.Col>
                    </Bootstrap.Row>
                  </div>
                  <MessageAlert
                    show={this.props.admin.addUser.showMessageAlert}
                    title={!this.props.admin.addUser.response.success ? _t({id: 'Form.ErrorsDetected'}) : _t({id: 'Form.Success'})}
                    i18nNamespace={this.props.admin.addUser.response.success ? 'Success.' : 'Error.'}
                    bsStyle={this.props.admin.addUser.response.success ? 'success' : 'danger' }
                    format='list'
                    messages={!this.props.admin.addUser.response.success ? this.props.admin.addUser.response.errors : [{code: successCodes['UserSuccess.USER_ADDED_WHITELIST']}]}
                    dismissFunc={this.props.actions.addUserHideErrorAlert}
                  />
              </Bootstrap.ListGroup>
              <Bootstrap.Row>

              <div style={{ float: 'right'}}>
                <Bootstrap.Col xs={6}>
                    <Bootstrap.Button onClick={hideAddNewUserForm}>
                    {_t({ id:'Buttons.Cancel'})}
                    </Bootstrap.Button>
                  </Bootstrap.Col>
                  <Bootstrap.Col xs={6}>
                    <Bootstrap.Button bsStyle='success' onClick={this.processAddNewUserForm}>
                    {_t({ id:'Buttons.AddUser'})}
                    </Bootstrap.Button>
                  </Bootstrap.Col>
                </div>
              </Bootstrap.Row>
              <div>
            </div>
            </div>
          </div>

          <em>(*) {_t({ id:'AddUserForm.MandatoryFields'})}</em>

        </Bootstrap.Panel>
      </div>

    );

    header = (
      <div className="row">
        {filter}
        <div className='col-md-4' />
        {addUserButton}
      </div>
    );
    var reportBody = (
      <div>
        {header}
        {table}
        {chart}
      </div>
    );

    var visiblePart = this.props.admin.addUser.show ? addNewUserForm : reportBody;

		return (
  		<div className="container-fluid" style={{ paddingTop: 10 }}>
  		  <div className="row">
  				<div className="col-md-12">
  					<Breadcrumb routes={this.props.routes}/>
  				</div>
  			</div>
  			{visiblePart}
  		</div>);
  	}
});

Overview.icon = 'table';
Overview.title = 'Section.Reports.Overview';

function mapStateToProps(state) {
  return {
      admin: state.admin,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { getActivity, setFilter, getSessions, getMeters, resetUserData,
      exportUserData, showAddUserForm, hideAddUserForm, addUserSelectUtility, addUserSelectGenderMale, addUserSelectGenderFemale,
      addUserFillForm, addUserValidationsErrorsOccurred, addUserShowMessageAlert, addUserHideErrorAlert, addUser, addUserGetUtilities }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Overview);
