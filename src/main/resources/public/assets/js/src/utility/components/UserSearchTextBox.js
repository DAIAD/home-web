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

  return api.json(`/action/user/search/prefix/${input}`)
    .then((response) => {
      var result = {
        options: []
      };

      if(response.success) {
        result.options = response.users.map( user => { 
          return {
            value : user.id , 
            label : (user.firstName + ' ' + user.lastName || user.email)
          };
        });
      }
      
      return result;
    });
};

var UserSearchTextBox = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },

  getDefaultProps: function() {
    return {
      name: 'username',
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
        noResultsText={'No users found'}
      />
    );
  }
});

module.exports = UserSearchTextBox;
