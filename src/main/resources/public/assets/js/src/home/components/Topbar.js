var React = require('react');

var Topbar = React.createClass({
  render: function() {
    return (
      <div className="top-bar">
        <div className="container">
          { this.props.children }
        </div>
      </div>
    );
  }
});

module.exports = Topbar;
