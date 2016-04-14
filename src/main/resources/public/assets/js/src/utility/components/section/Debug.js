var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var $ = require('jquery');

var FormattedMessage = require('react-intl').FormattedMessage;

var Bootstrap = require('react-bootstrap');
var Dropzone = require('react-dropzone');
var moment = require('moment');

var Breadcrumb = require('../Breadcrumb');

var { submitQuery } = require('../../actions/QueryActions');
var { createUser, createAmphiro } = require('../../actions/DebugActions');

var Debug = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
  getInitialState() {
    return {
      isLoading: null
    };
  },
  
  createUser: function(e) {
    if(this.refs.password.value) {
      this.props.actions.createUser(this.refs.password.value);
    }
  },
  
  createAmphiro: function(e) {
    this.props.actions.createAmphiro();
  },
  
  executeQuery: function(e) {
    var end = moment().valueOf();
    var start = moment().subtract(30, 'days').valueOf();
    
    var query = {
        time: {
          type : 'SLIDING',
          start: moment().valueOf(),
          duration: -30,
          durationTimeUnit: 'DAY',
          granularity: 'DAY'
        },
        population: [
          { type :'USER', label: 'Bob', users: ['633fcd0d-6f9d-4d2d-97f1-4556b4b3caaf']},
          { type :'UTILITY', label: 'Alicante', utility: '3e0663f2-d6c3-4d9a-8799-a2e8a24d0549'}
        ],
        spatial : {
          type: 'CONTAINS',
          geometry: {
            'type': 'Polygon',
            'coordinates': [
              [
                [
                  -0.525970458984375,
                  38.329537722849636
                ],
                [
                  -0.5233955383300781,
                  38.36386812314455
                ],
                [
                  -0.4821968078613281,
                  38.37651914591569
                ],
                [
                  -0.4440879821777344,
                  38.33963658855894
                ],
                [
                  -0.46966552734375,
                  38.31647443592999
                ],
                [
                  -0.5089759826660156,
                  38.313511301083466
                ],
                [
                  -0.525970458984375,
                  38.329537722849636
                ]
              ]
            ]
          }
        },
        source: 'ALL',
        metrics: ['SUM', 'COUNT', 'MIN', 'MAX', 'AVERAGE']
    };
    
    this.props.actions.submitQuery({ query : query });
  },
  
  onDropMeter: function (files) {
    this.onDrop('/action/upload', 'METER', files);
  },
  
  onDropMeterData: function (files) {
    this.onDrop('/action/upload', 'METER_DATA', files);
  },
  
  onDropAmphiroData: function (files) {
    this.onDrop('/action/debug/amphiro/data/generate', 'AMPHIRO_DATA', files);
  },
  
  onDrop: function (url, type, files) {
    if(this.state.isLoading) {
      return;
    }
    var self = this;
    
    var data = new FormData();
    
    data.append('type', type);
    for(var f=0; f<files.length; f++) {
      data.append('files', files[f]);
    }
    
    var updateCsrfToken = function(crsf) {
      $('meta[name=_csrf]').attr('content', crsf);
      $('input[name=_csrf]').val(crsf);
    };

    this.setState({ isLoading : type });
    
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
      self.setState({ isLoading : null });
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
    
    var image3 = (<i className="fa fa-cloud-upload fa-4x"></i>);
    if(this.state.isLoading=='AMPHIRO_DATA') {
      image3 = (<i className="fa fa-cog fa-spin fa-4x"></i>);
    }
    
    return (
      <div className='container-fluid' style={{ paddingTop: 10 }}>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-12'>
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-4'>
            <input id='password' name='password' type='password' ref='password' autofocus 
                   placeholder='Password ...' className='form-control' style={{ marginBottom : 15 }}/>
            <Bootstrap.Button bsStyle='primary' onClick={this.createUser}>Register all users</Bootstrap.Button>
          </div>
          <div className='col-md-4'>
            <Bootstrap.Button bsStyle='primary' onClick={this.createAmphiro}>Create Amphiro for all users</Bootstrap.Button>
          </div>  
          <div className='col-md-4'>
            <Bootstrap.Button bsStyle='primary' onClick={this.executeQuery}>Execute Query</Bootstrap.Button>
          </div>
        </div>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-4'>
            <Bootstrap.Panel header='Assign meters to users'>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <Dropzone onDrop={this.onDropMeter.bind(this)} disableClick={true} multiple={true} 
                            style={{ textAlign: 'center', fontSize: '3em', color: '#656565', paddingTop: 10 }}>
                    {image1}
                  </Dropzone>
                </Bootstrap.ListGroupItem>
                <Bootstrap.ListGroupItem>
                  <span color='#565656'>Drop an excel file with username, meter Id, longitude and latitude values.</span>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
          <div className='col-md-4'>
            <Bootstrap.Panel header='Upload water meter data'>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <Dropzone onDrop={this.onDropMeterData.bind(this)} disableClick={true} multiple={true} 
                            style={{ textAlign: 'center', fontSize: '3em', color: '#656565', paddingTop: 10}}>
                    {image2}
                  </Dropzone>
                </Bootstrap.ListGroupItem>
                <Bootstrap.ListGroupItem>
                  <span color='#565656'>Drop a CSV file with smart water meter measurements.</span>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
          <div className='col-md-4'>
            <Bootstrap.Panel header='Upload random Amphiro data'>
              <Bootstrap.ListGroup fill>
                <Bootstrap.ListGroupItem>
                  <Dropzone onDrop={this.onDropAmphiroData.bind(this)} disableClick={true} multiple={true} 
                            style={{ textAlign: 'center', fontSize: '3em', color: '#656565', paddingTop: 10}}>
                    {image3}
                  </Dropzone>
                </Bootstrap.ListGroupItem>
                <Bootstrap.ListGroupItem>
                  <span color='#565656'>Drop a text file with Amphiro sessions. Data will be generated for the current month.</span>
                  <br/>
                  <span color='#565656'>Only devices with no sessions will be updated.</span>
                </Bootstrap.ListGroupItem>
              </Bootstrap.ListGroup>
            </Bootstrap.Panel>
          </div>
        </div>
      </div>
    );
  }
});

Debug.icon = 'bug';
Debug.title = 'Debug';

function mapStateToProps(state) {
  return {
      query: state.query,
      routing: state.routing
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions : bindActionCreators(Object.assign({}, { submitQuery, createUser, createAmphiro }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Debug);
