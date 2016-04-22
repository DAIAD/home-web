var React = require('react');

function Sidebar (props) {
  return (
    <aside className="sidebar" style={props.width?{width:props.width}:{}}>
      { props.children }
    </aside>
  );
}

module.exports = Sidebar;
