var React = require('react');
var ReactDOM = require('react-dom');

var _ = require('lodash');

var PortalMixin = {

	propTypes : {
		id : React.PropTypes.number,
		elementClassName : React.PropTypes.string,
		prefix : React.PropTypes.string
	},

	getDefaultProps : function() {
		return {
			prefix : 'portal',
			elementClassName : 'mixin'
		};
	},

	_hasClass : function(className) {
		if (this._element.classList) {
			return this._element.classList.contains(className);
		} else {
			return new RegExp('(^| )' + className + '( |$)', 'gi')
					.test(className);
		}
	},

	_addClass : function(className) {
		if (this._element.classList) {
			this._element.classList.add(className);
		} else {
			this._element.className += ' ' + className;
		}
	},

	_removeClass : function(className) {
		if (this._element.classList) {
			this._element.classList.remove(className);
		} else {
			this._element.className = this._element.className.replace(
					new RegExp('(^|\\b)' + className.split(' ').join('|') + '(\\b|$)', 'gi'), ' ');
		}
	},

	_createContainer : function(id) {
		var parent = ReactDOM.findDOMNode(this) || document.body;
		this._element = parent.appendChild(document.createElement('div'));

		this._element.setAttribute('id', id);

		if (this.props.elementClassName) {
			this._addClass(this.props.elementClassName);
		}
	},

	_destroyContainer : function() {
		if (this._element) {
			this._element.parentNode.removeChild(this._element);
		}
	},

	componentDidMount : function() {
		var id = null;
		if (this.props.prefix) {
			id = _.uniqueId(this.props.prefix);
		} else {
			id = _.uniqueId();
		}

		this._createContainer(id);
	},

	componentWillReceiveProps : function(nextProps) {
		if (this.props.elementClassName != nextProps.elementClassName) {
			if (this.props.elementClassName) {
				this._removeClass(this.props.elementClassName);
			}

			if (nextProps.elementClassName) {
				this._addClass(nextProps.elementClassName);
			}
		}
	},

	shouldComponentUpdate : function(nextProps, nextState) {
		return false;
	},

	componentWillUnmount : function() {
		this._destroyContainer();
	},

	getId : function() {
		return this._element.getAttribute('id');
	},

	getElement : function() {
		return this._element;
	},

	getElementClass : function() {
		return this.elementClassName;
	}
};

module.exports = PortalMixin;