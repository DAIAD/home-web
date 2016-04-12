var React = require('react');
var ReactDOM = require('react-dom');

var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');

var Bootstrap = require('react-bootstrap');
var Checkbox = require('./Checkbox');

var Table = React.createClass({
	getInitialState: function() {
		return {
			activePage: this.props.data.pager.index
  	};
	},
	
	onPageIndexChange(event, selectedEvent) {
		this.setState({
			activePage: (selectedEvent.eventKey - 1)
  	});
		if(typeof this.props.onPageIndexChange === 'function') {
		  this.props.onPageIndexChange(selectedEvent.eventKey - 1);
		}
	},
	  
	getDefaultProps: function() {
		return {
			data: {
				fields: [],
				rows: [],
				pager: {
					index: 0,
					size: 10,
					count:0
				}
			}
		};
	},

	suspendUI: function() {
		this.setState({ loading : false});
  	},
  	
  	resumeUI: function() {
  		this.setState({ loading : true});
  	},

  	render: function() { 		
  		return (
			<div className='clearfix'>
				<Bootstrap.Table hover style={{margin: 0, padding: 0}}>
					<Table.Header data = {this.props.data}></Table.Header>
					<Table.Body data={this.props.data} activePageIndex={this.state.activePage} ></Table.Body>			
				</Bootstrap.Table>
				<div style={{float:'right'}}>
					<Bootstrap.Pagination 	prev
											next
											first
											last
											ellipsis
											items={Math.ceil(this.props.data.pager.count / this.props.data.pager.size)}
			        						maxButtons={7}
			        						activePage={this.state.activePage + 1}
			        						onSelect={this.onPageIndexChange} />	
				</div>
			</div>
 		);
  	}
});

var Header = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {	
  		var _t = this.context.intl.formatMessage;

		var header = this.props.data.fields.filter((f) => { return !!!f.hidden; }).map(function(field) {
			switch(field.type ) {
				case 'action':
					return (
						<th key={field.name} style={{ width: 24 }}>{field.title ? _t({ id: field.title}) : ''}</th>
					);
				case 'boolean':
					return (
						<th key={field.name} style={{ width: 90 }}>{field.title ? _t({ id: field.title}) : ''}</th>
					);
			}

			return (
				<th key={field.name}>{field.title ? _t({ id: field.title}) : ''}</th>
			);
		});
		
  		return (
			<thead>
				<tr>
					{header}
				</tr>	
			</thead>
 		);
  	}
});

var Body = React.createClass({
  	render: function() { 		
  		var self = this;

  		var filtered = this.props.data.rows.reduce(function(newArray, currentValue, currentIndex) {
  		  var pager = self.props.data.pager;
  		  
  		  if(((self.props.activePageIndex*pager.size) <= currentIndex) && (currentIndex < ((self.props.activePageIndex+1)*pager.size))) {
          newArray.push(currentValue);
  		  }

  		  return newArray;
  		}, []);
  		
  		var rows = filtered.map(function(row, rowIndex) {
  			return (
  				<Table.Row 	key={rowIndex} 
  							fields={self.props.data.fields} 
  							row={row}>
  				</Table.Row>
  			);
  		});
		
  		return (
			<tbody>
				{rows}
			</tbody>
 		);
  	}
});

var Row = React.createClass({
  	render: function() {
  		var self = this;

  		return (
			<tr>
				{
					this.props.fields.filter((f) => { return !!!f.hidden; }).map(function(field, columnIndex) {
						return (
							<Table.Cell key={columnIndex} row={self.props.row} field={field}>
							</Table.Cell>
						);
					})
				}
			</tr>
		);
  	}
});

var formatLink = function(route, row) {
	return Object.keys(row).reduce(function(link, key) {
		return link.replace(new RegExp('\{' + key + '\}'), row[key]);
	}, route);
};

var Cell = React.createClass({
  	render: function() {
  		var value= this.props.row[this.props.field.name];
  		var text = (<span>{value}</span>);
 		
  		if(this.props.field.hasOwnProperty('type')) {
  			switch(this.props.field.type) {
  			case 'action':
  			  var visible = true;
  			  if(typeof this.props.field.visible === 'function') {
  			    visible = this.props.field.visible(this.props.row);
  			  }
  			  if(visible) {
  			    if(this.props.field.icon) {
  			      var color = this.props.field.color || '#000000';
  			      text = (<i className={'fa fa-' + this.props.field.icon + ' fa-fw table-action'} style={{color : color}} onClick={this.props.field.handler.bind(this)}></i>);
  			    } else {
  			      text = (<i className="table-action" onClick={this.props.field.handler.bind(this)}><img src={this.props.field.image} /></i>);
  			    }
  			  }
  				break;
  			case 'datetime':
  				if(value) {
  					text = (<FormattedTime 	value={value} 
  										day='numeric' 
  										month='numeric' 
  										year='numeric'
  										hour='numeric' 
  										minute='numeric' />);
  				} else {
  					text = '';
  				}
  				break;
  			case 'time':
  				text = (<FormattedTime 	value={value} 
										hour='numeric' 
										minute='numeric' />);
  				break;
  			case 'progress':
  				if(value !== null) {
  					text = (<Bootstrap.ProgressBar now={value} label="%(percent)s%" />);
  				} else {
  					text = (<span />);
  				}
  				break;
  			case 'boolean':
  				text = (<Checkbox checked={value} disabled={true} />);
  				break;
  			case 'date':
  				text = (<FormattedDate value={value} day='numeric' month='long' year='numeric' />);
  				break;
			default:
				console.log('Cell type [' + this.props.field.type + '] is not supported.');
				break;
  			}
  		} else {
	  		if(value instanceof Date) {
	  			text = (<FormattedDate value={value} day='numeric' month='long' year='numeric' />);
	  		} else if(typeof value === 'boolean') {
	  			text = (<Checkbox checked={value} disabled={true} />);
	  		}
  		} 

  		if(this.props.field.hasOwnProperty('link')) { 	
  			if(typeof this.props.field.link === 'function') {
  			  var href = this.props.field.link(this.props.row);
  			  if(href) {
            text = (<Link to={formatLink(this.props.field.link(this.props.row), this.props.row)}>{text}</Link>);  			    
  			  }
  			} else {
  				text = (<Link to={formatLink(this.props.field.link, this.props.row)}>{text}</Link>);
  			}
  			
  		}

  		if(typeof this.props.field.className === 'function') {
  			return (
				<td className={this.props.field.className(value)}>{text}</td>
			);	
  		}
  		
  		if(this.props.field.hasOwnProperty('align')) {
  			return (
  					<td style={{ textAlign: this.props.field.align}}>{text}</td>
  				);
  		}

		return (
			<td>{text}</td>
		);
  	}
});

Table.Header = Header;

Table.Body = Body;

Table.Row = Row;

Table.Cell = Cell;

module.exports = Table;
