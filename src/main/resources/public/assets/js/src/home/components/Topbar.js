var React = require('react');

function Topbar (props) {
  return (
    <div className="top-bar">
      <div className="container">
        { props.children }
      </div>
    </div>
  );
}

module.exports = Topbar;
