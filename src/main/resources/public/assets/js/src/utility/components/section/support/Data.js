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

var { submitQuery } = require('../../../actions/QueryActions');
var { createUser, createAmphiro } = require('../../../actions/DebugActions');


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
        srid = 25830;
        break;
       default:
         srid = 4326;
         break;
    }
    
    return {
      isLoading: null,
      srid: srid,
      timezone: timezone,
      errors: null
    };
  },
   
  onDropMeter: function (files) {
    var data = new FormData();
    data.append('srid', this.state.srid);
    
    this.onDrop('/action/upload', files, data, 'METER');
  },
  
  onDropMeterData: function (files) {
    var data = new FormData();
    data.append('timezone', this.state.timezone);
    
    this.onDrop('/action/upload', files, data, 'METER_DATA');
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

    var image1 = (<i className="fa fa-cloud-upload fa-4x"></i>);
    if(this.state.isLoading=='METER') {
      image1 = (<i className="fa fa-cog fa-spin fa-4x"></i>);
    }

    var image2 = (<i className="fa fa-cloud-upload fa-4x"></i>);
    if(this.state.isLoading=='METER_DATA') {
      image2 = (<i className="fa fa-cog fa-spin fa-4x"></i>);
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
    
    return (
      <div className='container-fluid' style={{ paddingTop: 10 }}>
        {modal}
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-12'>
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-6'>
            <Bootstrap.Panel header='Assign meters to users'>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <div className='row'>
                    <div className='form-group col-md-3'>
                      <label className='control-label' htmlFor='srid'>Coordinates SRID</label>
                    </div>
                    <div className='col-md-9'>
                      <Select name='srid'
                              value={this.state.srid}
                              options={[
                                  { value: 4326, label: 'WGS 84 - EPSG:4326' },
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
                    <div className='form-group col-md-12'>
                      <Dropzone onDrop={this.onDropMeter.bind(this)} disableClick={true} multiple={true} 
                                style={{ textAlign: 'center', fontSize: '3em', color: '#656565', border: '1px dotted #656565' }}>
                        {image1}
                      </Dropzone>
                    </div>
                  </div>
                </Bootstrap.ListGroupItem>
                <Bootstrap.ListGroupItem>
                  <span color='#565656'>Drop an excel file with username, meter Id, longitude and latitude values.</span>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
          <div className='col-md-6'>
            <Bootstrap.Panel header='Upload water meter data'>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <div className='row'>
                    <div className='form-group col-md-3'>
                      <label className='control-label' htmlFor='timezone'>Time zone </label>
                    </div>
                    <div className='col-md-9'>
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
                        {image2}
                      </Dropzone>
                    </div>
                  </div>
                </Bootstrap.ListGroupItem>
                <Bootstrap.ListGroupItem>
                  <span color='#565656'>Drop a CSV file with smart water meter measurements.</span>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
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
