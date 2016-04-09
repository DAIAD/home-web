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

var Debug = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },
  
  getInitialState() {
    return {
      isLoading: false
    };
  },
  
  execute: function(e) {
    var end = moment().valueOf();
    var start = moment().subtract(20, 'days').valueOf();
    
    var query = {
        time: {
          type : 'ABSOLUTE',
          interval : 'DAY',
          start: start,
          end: end
        },
        population: {
          users: [1,2],
          groups: [4,15]
        },
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
        metrics: ['SUM', 'COUNT']
    };
    
    this.props.actions.submitQuery({ query : query });
  },
  
  onDrop: function (files) {
    if(this.state.isLoading) {
      return;
    }
    var self = this;
    
    var data = new FormData();
    
    for(var f=0; f<files.length; f++) {
      data.append('files', files[f]);
    }
    
    var updateCsrfToken = function(crsf) {
      $('meta[name=_csrf]').attr('content', crsf);
      $('input[name=_csrf]').val(crsf);
    };

    this.setState({ isLoading : true });
    
    var request = {
      url: '/action/upload',
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
      self.setState({ isLoading : false });
    }).fail(function(jqXHR, textStatus, errorThrown) {
      updateCsrfToken(jqXHR.getResponseHeader('X-CSRF-TOKEN'));
      self.setState({ isLoading : false });
    });
  },

  render: function() {
    var _t = this.context.intl.formatMessage;

    var image = (<i className="fa fa-cloud-upload fa-4x"></i>);
    if(this.state.isLoading) {
      image = (<i className="fa fa-cog fa-spin fa-4x"></i>);
    }

    return (
      <div className='container-fluid' style={{ paddingTop: 10 }}>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-12'>
            <Breadcrumb routes={this.props.routes}/>
          </div>
        </div>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-12'>
            <Bootstrap.Button bsStyle='primary' onClick={this.execute}>Execute Query</Bootstrap.Button>
          </div>
        </div>
        <div className='row' style={{ marginBottom: 10 }}>
          <div className='col-md-4'>
            <Dropzone onDrop={this.onDrop.bind(this)} disableClick={true} multiple={true} 
                      style={{ textAlign: 'center', fontSize: '3em', color: '#f5f5f5', paddingTop: 10, background: '#f9f9f9' }}>
              {image}
            </Dropzone>
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
    actions : bindActionCreators(Object.assign({}, { submitQuery }) , dispatch)
  };
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(Debug);
