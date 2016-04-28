var React = require('react');

function MainSection (props) {
  return (
    <section className="main-section" >
      <div className={props.id}>
        <div className="container">
            {props.children}
          </div>
        </div>
      </section>
  );
}

module.exports = MainSection;
