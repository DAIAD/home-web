var React = require('react');
var ReactDOM = require('react-dom');
var Select = require('react-select');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var $ = require('jquery');

var FormattedMessage = require('react-intl').FormattedMessage;

var Bootstrap = require('react-bootstrap');
var Dropzone = require('react-dropzone');
var moment = require('moment');

var Breadcrumb = require('../../Breadcrumb');
var Checkbox = require('../..//Checkbox');

var { submitQuery } = require('../../../actions/QueryActions');
var { createUser, createAmphiro } = require('../../../actions/DebugActions');


var UPLOAD_NONE = 0;
var UPLOAD_METER = 1;
var UPLOAD_METER_DATA = 2;
var UPLOAD_FORECAST = 3;

var onChangeSRID = function(val) {
  this.setState({
    srid: val.value
  });
}; 

var onChangeTimezone = function(val) {
  this.setState({
    timezone: val.value
  });
}; 

var onChangeMode = function(val) {
  this.setState({
    mode: val.value
  });
};

var clearErrors = function() {
  this.setState({
    errors: null
  });
};

var DataManagement = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
  getInitialState() {
    var profile = this.props.profile;

    var timezone = profile.timezone || 'UTC';
    
    var srid = null;
    switch(profile.country) {
      case 'Greece': case 'Hellas':
        srid = 2100;
        break;
      case 'Spain':
        srid = 3042;
        break;
       default:
         srid = 4326;
         break;
    }
    
    return {
      isLoading: false,
      srid: srid,
      timezone: timezone,
      errors: null,
      isFirstRowProfile: false,
      mode: UPLOAD_NONE
    };
  },

  onFirstRowIsHeaderClick: function(checked) {
    console.log(checked);
    this.setState({ isFirstRowProfile : checked });
  },

  onDropMeter: function (files) {
    var data = new FormData();
    data.append('srid', this.state.srid);
    data.append('firstRowHeader', this.state.isFirstRowProfile);
    
    this.onDrop('/action/upload', files, data, 'METER');
  },
  
  onDropMeterData: function (files) {
    var data = new FormData();
    data.append('timezone', this.state.timezone);
    
    this.onDrop('/action/upload', files, data, 'METER_DATA');
  },
  
  onDropForecastData: function (files) {
    var data = new FormData();
    data.append('timezone', this.state.timezone);
    
    this.onDrop('/action/upload', files, data, 'METER_DATA_FORECAST');
  },
  
  onDrop: function (url, files, data, type) {
    if(this.state.isLoading) {
      return;
    }
    var self = this;

    data.append('type', type);
    for(var f=0; f<files.length; f++) {
      data.append('files', files[f]);
    }
    
    var updateCsrfToken = function(crsf) {
      $('meta[name=_csrf]').attr('content', crsf);
      $('input[name=_csrf]').val(crsf);
    };

    this.setState({ isLoading : type, errors: null });
    
    var request = {
      url: url,
      type: 'POST',
      data: data,
      enctype: 'multipart/form-data',
      processData: false,
      contentType: false,
      cache: false,
      beforeSend : function(xhr) {
        xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr('content'));
      }
    };

    $.ajax(request).done(function(data, textStatus, request) {
      updateCsrfToken(request.getResponseHeader('X-CSRF-TOKEN'));
      self.setState({ isLoading : null, errors: data.errors });
    }).fail(function(jqXHR, textStatus, errorThrown) {
      updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
      self.setState({ isLoading : null });
    });
  },

  render: function() {
    var _t = this.context.intl.formatMessage;

    var dropZoneImage = (<i className="fa fa-cloud-upload fa-4x"></i>);
    if(this.state.isLoading) {
      dropZoneImage = (<i className="fa fa-cog fa-spin fa-4x"></i>);
    }

    var modal = null;
    if((this.state.errors) && (this.state.errors.length > 0)) {
      var errors = [];
      for(var i=0; i<this.state.errors.length; i++) {
        errors.push(<p key={i}>{this.state.errors[i].description}</p>);
      }

      modal = (
        <Bootstrap.Modal show={true} onHide={clearErrors.bind(this)}>
          <Bootstrap.Modal.Header closeButton>
            <Bootstrap.Modal.Title>Error</Bootstrap.Modal.Title>
          </Bootstrap.Modal.Header>
          <Bootstrap.Modal.Body>
            {errors} 
          </Bootstrap.Modal.Body>
          <Bootstrap.Modal.Footer>
            <Bootstrap.Button onClick={clearErrors.bind(this)}>Close</Bootstrap.Button>
          </Bootstrap.Modal.Footer>
        </Bootstrap.Modal>
      );
    }
    
    var panel = null;
    
    var modeSelection = (
      <Bootstrap.ListGroupItem>
        <div className='row'>
          <div className='col-md-4'>
            <Select name='mode'
                    value={this.state.mode}
                    options={[
                        { value: UPLOAD_NONE, label: 'Select ...' },
                        { value: UPLOAD_METER, label: 'Assign meters to users' },
                        { value: UPLOAD_METER_DATA, label: 'Upload water meter data' },
                        { value: UPLOAD_FORECAST, label: 'Upload forecasting data' },
                    ]}
                    onChange={onChangeMode.bind(this)}
                    clearable={false} 
            />
            <span className='help-block'>Select task type</span>  
          </div>
        </div>
      </Bootstrap.ListGroupItem>
    );
    
    switch(this.state.mode) {
      case UPLOAD_METER:
        panel = (
          <Bootstrap.ListGroup fill>
            {modeSelection}
            <Bootstrap.ListGroupItem style={{background : '#f5f5f5'}}>
              <i className='fa fa-cloud-upload fa-fw'></i>
              <span style={{ paddingLeft: 4 }}>Assign meters to users</span>
            </Bootstrap.ListGroupItem>
            <Bootstrap.ListGroupItem>
              <div className='row'>
                <div className='form-group col-md-2'>
                  <label className='control-label' htmlFor='srid'>Coordinates SRID</label>
                </div>
                <div className='col-md-6'>
                  <Select name='srid'
                          value={this.state.srid}
                          options={[
                              { value: 4326, label: 'WGS 84 - EPSG:4326' },
                              { value: 3042, label: 'ETRS89 / UTM zone 30N (N-E) - EPSG:3042' },
                              { value: 25830, label: 'ETRS89 / UTM zone 30N - EPSG:25830' },
                              { value: 2100, label: 'GGRS87 - EPSG:2100)' },
                          ]}
                          onChange={onChangeSRID.bind(this)}
                        clearable={false} 
                  />
                  <span className='help-block'>Select Coordinates Reference System for meter location</span>  
                </div>
              </div>
              <div className='row'>
                <div className='form-group col-md-2'>
                  <label className='control-label' htmlFor='srid'>First row is header</label>
                </div>
                <div className='col-md-6' style={{ paddingLeft: 19 }}>
                  <div style={{ paddingLeft: 1 }}>
                    <Checkbox checked={this.state.isFirstRowProfile}
                              onChange={this.onFirstRowIsHeaderClick} />
                    </div>
                </div>
              </div>
              <div className='row'>
                <div className='form-group col-md-12'>
                  <Dropzone onDrop={this.onDropMeter.bind(this)} disableClick={true} multiple={true} 
                            style={{ textAlign: 'center', fontSize: '3em', color: '#656565', border: '1px dotted #656565' }}>
                    {dropZoneImage}
                  </Dropzone>
                </div>
              </div>
            </Bootstrap.ListGroupItem>
            <Bootstrap.ListGroupItem>
              <span color='#565656'>Drop an excel file with user and meter data. The first sheet must contain
              four columns with the username, meter serial number, longitude and latitude values respectively. Longitude 
              and latitude values are optional.</span>
            </Bootstrap.ListGroupItem>
          </Bootstrap.ListGroup>
        );
        break;
      case UPLOAD_METER_DATA:
        panel = (
          <Bootstrap.ListGroup fill>
            {modeSelection}
            <Bootstrap.ListGroupItem style={{background : '#f5f5f5'}}>
              <i className='fa fa-cloud-upload fa-fw'></i>
              <span style={{ paddingLeft: 4 }}>Upload water meter data</span>
            </Bootstrap.ListGroupItem>
            <Bootstrap.ListGroupItem>
              <div className='row'>
                <div className='form-group col-md-2'>
                  <label className='control-label' htmlFor='timezone'>Time zone </label>
                </div>
                <div className='col-md-6'>
                  <Select name='timezone'
                          value={ this.state.timezone}
                          options={[
                              { value: 'UTC', label: 'Coordinated Universal Time (UTC)' },
                              { value: 'Europe/Madrid', label: 'Madrid' },
                              { value: 'Europe/Athens', label: 'Athens' }
                          ]}
                          onChange={onChangeTimezone.bind(this)}
                        clearable={false} 
                  />
                  <span className='help-block'>Select time zone of the uploaded data</span>  
                </div>
              </div>
              <div className='row'>
                <div className='form-group col-md-12'>
                  <Dropzone onDrop={this.onDropMeterData.bind(this)} disableClick={true} multiple={true} 
                            style={{ textAlign: 'center', fontSize: '3em', color: '#656565', border: '1px dotted #656565'}}>
                    {dropZoneImage}
                  </Dropzone>
                </div>
              </div>
            </Bootstrap.ListGroupItem>
            <Bootstrap.ListGroupItem>
              <p>Drop a text file with smart water meter measurement data. Each row should have
              six values delimited  with semicolon character (<b>;</b>).
              </p>
              <p>Fields:</p>
              <ol>
                <li>Channel</li>
                <li>Utility</li>
                <li>Meter Serial Number</li>
                <li>Date & time formatted as <b>DD/MM/YYYY HH:mm:ss</b></li>
                <li>Meter reading</li>
                <li>Difference since last reading</li>
              </ol>
              <p></p>
              <p>Example:</p>
              <p style={{color:'#565656'}}>
              ALICANTE VHF<b>;</b>AMAEM<b>;</b>C12FA154674<b>;</b>11/04/2016 11:12:04<b>;</b>896377<b>;</b>1<b>;</b>
              </p>
            </Bootstrap.ListGroupItem>
          </Bootstrap.ListGroup>
        );
        break;
      case UPLOAD_FORECAST:
        panel = (
            <Bootstrap.ListGroup fill>
              {modeSelection}
              <Bootstrap.ListGroupItem style={{background : '#f5f5f5'}}>
                <i className='fa fa-cloud-upload fa-fw'></i>
                <span style={{ paddingLeft: 4 }}>Upload forecasting data</span>
              </Bootstrap.ListGroupItem>
              <Bootstrap.ListGroupItem>
                <div className='row'>
                  <div className='form-group col-md-2'>
                    <label className='control-label' htmlFor='timezone'>Time zone </label>
                  </div>
                  <div className='col-md-6'>
                    <Select name='timezone'
                            value={ this.state.timezone}
                            options={[
                                { value: 'UTC', label: 'Coordinated Universal Time (UTC)' },
                                { value: 'Europe/Madrid', label: 'Madrid' },
                                { value: 'Europe/Athens', label: 'Athens' }
                            ]}
                            onChange={onChangeTimezone.bind(this)}
                          clearable={false} 
                    />
                    <span className='help-block'>Select time zone of the uploaded data</span>  
                  </div>
                </div>
                <div className='row'>
                  <div className='form-group col-md-12'>
                    <Dropzone onDrop={this.onDropForecastData.bind(this)} disableClick={true} multiple={true} 
                              style={{ textAlign: 'center', fontSize: '3em', color: '#656565', border: '1px dotted #656565'}}>
                      {dropZoneImage}
                    </Dropzone>
                  </div>
                </div>
              </Bootstrap.ListGroupItem>
              <Bootstrap.ListGroupItem>
                <p>Drop text file with forecasting analysis results. Each line contains three values. The first and second values
                are delimited with an underscore (<b>_</b>). The second and third values are delimited with a space.
                </p>
                <p>Fields:</p>
                <ol>
                  <li>Meter Serial Number</li>
                  <li>Date & time formatted as <b>YYYY-MM-DD-HH</b></li>
                  <li>Difference since last reading</li>
                </ol>
                <p></p>
                <p>Example:</p>
                <p style={{color:'#565656'}}>
                  C11FA586148<b>_</b>2014-06-30-01 1.9244444
                </p>
              </Bootstrap.ListGroupItem>
            </Bootstrap.ListGroup>
          );
        break;
      default:
        panel = (
          <Bootstrap.ListGroup fill>
            {modeSelection}
          </Bootstrap.ListGroup>
        );
        break;
    }
    
    var header = (
      <span>
        <i className='fa fa-cog fa-fw'></i>
        <span style={{ paddingLeft: 4 }}>Task</span>
      </span>
    );
    
    return (
      <div className='container-fluid' style={{ paddingTop: 10 }}>
        {modal}
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-12'>
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-12'>
            <Bootstrap.Panel header={header}>
              {panel}
            </Bootstrap.Panel>
          </div>
        </div>
      </div>
    );
  }
});

DataManagement.icon = 'database';
DataManagement.title = 'Section.Support.Data';

function mapStateToProps(state) {
  return {
      query: state.query,
      profile: state.session.profile,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return { };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(DataManagement);
