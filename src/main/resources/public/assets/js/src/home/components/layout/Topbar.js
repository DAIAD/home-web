var React = require('react');

function Topbar (props) {
  return (
    <div className="top-bar">
      { props.children }
    </div>
  );
}

module.exports = Topbar;
