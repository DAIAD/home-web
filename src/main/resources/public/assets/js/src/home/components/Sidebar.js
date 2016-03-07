var React = require('react');

var Sidebar = React.createClass({
  render: function() {
    return (
      <aside className="sidebar" style={this.props.width?{width:this.props.width}:{}}>
        { this.props.children }
      </aside>
    );
  }
});

module.exports = Sidebar;
