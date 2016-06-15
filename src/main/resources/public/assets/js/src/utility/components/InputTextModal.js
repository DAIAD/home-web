var React = require('react');
var Bootstrap = require('react-bootstrap');

var InputTextModal = React.createClass({

  onChange: function(e) {
    this.setState({
      text: this.refs.inputText.getValue()
    });
  },
  
  onClick: function(key) {
    if(typeof this.props.handler === 'function'){
      this.props.handler(key, this.state.text);
    }
  },
  
  getInitialState: function() {
    return {
      text: ''
    };
  },
  
  render: function(){
    var actions = [], self = this;
    
    this.props.actions.forEach(function (action, i){
      var button;
      if(action.style){
        button = (<Bootstrap.Button bsStyle={action.style} key={action.key} onClick={self.onClick.bind(self, action.key)}>{action.text}</Bootstrap.Button>);
      } else {
        button = (<Bootstrap.Button key={action.key} onClick={self.onClick.bind(self, action.key)}>{action.text}</Bootstrap.Button>);
      }
      actions.push((
        button
      ));
    });
          
    return (
      <Bootstrap.Modal animation={false} show={this.props.visible} onHide={this.props.onHide}>
        <Bootstrap.Modal.Header closeButton>
          <Bootstrap.Modal.Title>{this.props.title}</Bootstrap.Modal.Title>
        </Bootstrap.Modal.Header>
        <Bootstrap.Modal.Body>
          <Bootstrap.Input 
             type='text' 
             id='inputText' name='inputText' ref='inputText'
             placeholder={this.props.prompt}
             onChange={this.onChange}
             value={this.state.text} />
          <span className='help-block'>{this.props.help}</span>
        </Bootstrap.Modal.Body>
        <Bootstrap.Modal.Footer>
          {actions}
        </Bootstrap.Modal.Footer>
      </Bootstrap.Modal>
    );
  }
});

module.exports = InputTextModal;
