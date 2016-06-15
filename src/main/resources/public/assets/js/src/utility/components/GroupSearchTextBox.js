var React = require('react');
var Select = require('react-select');
var api = require('../api/base.js');

var _onChange = function(e) {
  this.setState({
    value: e
  });
  
  if(typeof this.props.onChange === 'function') {
    this.props.onChange(e);
  }
};

var _getOptions = (input) => {
  if(!input) {
    return new Promise( (resolve, reject) => {
      resolve({
        options: []
      });
    });
  }

  return api.json(`/action/group/query/${input}`)
    .then((response) => {
      var result = {
        options: []
      };

      if(response.success) {
        result.options = response.groups.map( group => {
          group.value = group.key;
          if(group.type === 'SEGMENT') {
            group.label = group.cluster + ': '+ group.name;  
          } else {
            group.label = group.name;
          }
          

          return group;
        });
      }
      
      return result;
    });
};

var GroupSearchTextBox = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },

  getDefaultProps: function() {
    return {
      name: 'grpup',
      onChange: null,
      multi: false
    };
  },

  getInitialState() {
    return {
      value: null,
    };
  },
  
  render: function() {
    return (
      <Select.Async name={this.props.name}
        value={this.state.value}
        onChange={_onChange.bind(this)}
        loadOptions={_getOptions.bind(this)}
        clearable={true}
        multi={this.props.multi}
        noResultsText={'No groups found'}
        ignoreCase={false}
      />
    );
  }
});

module.exports = GroupSearchTextBox;
