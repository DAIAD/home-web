var React = require('react');

function MainSection (props) {
  return (
    <section className="main-section" >
      <div className={props.id}>
        {props.children}
        </div>
      </section>
  );
}

module.exports = MainSection;
