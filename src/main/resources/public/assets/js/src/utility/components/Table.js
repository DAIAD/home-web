var React = require('react');
var ReactDOM = require('react-dom');

var FormattedMessage = require('react-intl').FormattedMessage;
var FormattedDate = require('react-intl').FormattedDate;
var Bootstrap = require('react-bootstrap');

var Table = React.createClass({
	getInitialState: function() {
		return {
			activePage: 0
    	};
	},
	
	onPageIndexChange(event, selectedEvent) {
		this.setState({
			activePage: (selectedEvent.eventKey - 1)
    	});
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
			<div>
				<Bootstrap.Table hover style={{margin: 0, padding: 0}}>
					<Table.Header data = {this.props.data}></Table.Header>
					<Table.Body data = {this.props.data}></Table.Body>			
				</Bootstrap.Table>
				<div style={{float:'right'}}>
					<Bootstrap.Pagination 	prev
											next
											first
											last
											ellipsis
											items={this.props.data.pager.size}
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

		var header = this.props.data.fields.map(function(field) {
			return (
				<th key={field.name}>{_t({ id: field.title})}</th>
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

		var rows = this.props.data.rows.map(function(row, rowIndex) {
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
					this.props.fields.map(function(field, columnIndex) {
						return (
							<Table.Cell key={columnIndex} value={self.props.row[field.name]}>
							</Table.Cell>
						);
					})
				}
			</tr>
		);
  	}
});

var Cell = React.createClass({
  	render: function() {
  		if(this.props.value instanceof Date) {
  			return (
				<td>
  					<FormattedDate value={this.props.value} day="numeric" month="long" year="numeric" />
				</td>
  			);
  		}

		return (
			<td>{this.props.value.toString()}</td>
		);
  	}
});

Table.Header = Header;

Table.Body = Body;

Table.Row = Row;

Table.Cell = Cell;

module.exports = Table;
