var React = require('react');

var { FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');

var Bootstrap = require('react-bootstrap');
var Checkbox = require('./Checkbox');
var CellCheckbox = require('./CellCheckbox');

var PAGING_CLIENT_SIDE = 'client';
var PAGING_SERVER_SIDE = 'server';

var Table = React.createClass({
	getInitialState: function() {
		return {
			activePage: (this.props.data.pager ? this.props.data.pager.index : 0)
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
				rows: []
			},
			template: {
			  empty: null
			}, 
      style: {
        row : {
          rowHeight: 100
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
  	  var totalPages = 1, currentPageIndex = 0, pagination = null;
  	  
  	  if(this.props.data.pager) {
  	    totalPages = Math.ceil(this.props.data.pager.count / this.props.data.pager.size);
  	    currentPageIndex = (this.state.activePage + 1) > totalPages ? totalPages : (this.state.activePage + 1);
  	    
  	    pagination = (
	        <Bootstrap.Pagination prev
                                next
                                first
                                last
                                ellipsis
                                items={totalPages}
                                maxButtons={7}
                                activePage={currentPageIndex}
                                onSelect={this.onPageIndexChange} />
        );
  	  }

      if((this.props.data.rows.length === 0) && (this.props.template.empty)) {
        return(
          this.props.template.empty
        );
      }

  		return (
			<div className='clearfix'>
			  <div style={{overflow: 'auto'}}>
  				<Bootstrap.Table hover style={{margin: 0, padding: 0}}>
  					<Table.Header data = {this.props.data}></Table.Header>
  					<Table.Body data={this.props.data} activePageIndex={currentPageIndex - 1} style={this.props.style}></Table.Body>			
  				</Bootstrap.Table>
				</div>
				<div style={{float:'right'}}>
					{pagination}	
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
		  var style =  {};
      if(field.hasOwnProperty('align')) {
        style.textAlign = field.align;
      }
      
			switch(field.type ) {
				case 'action':
				  style.width = field.width || 24;
				  break;
				case 'boolean':
				  style.width = field.width || 90;
				  break;
			  default:
		      if((field.width) && (field.width >0)) {
		        style.width = field.width;
		      }
			    break;
			}

      return (
        <th key={field.name} style={style}>{field.title ? _t({ id: field.title}) : ''}</th>
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
  	  
      var pager = self.props.data.pager;
  		var filtered = self.props.data.rows;
  		
  		if((!pager) || (!pager.mode) || (pager.mode === PAGING_CLIENT_SIDE)) {
  		  filtered = self.props.data.rows.reduce(function(newArray, currentValue, currentIndex) {
  		  
  		    if((!pager) || (((self.props.activePageIndex*pager.size) <= currentIndex) && 
  		        (currentIndex < ((self.props.activePageIndex+1)*pager.size)))) {
  		      newArray.push(currentValue);
  		    }

  		    return newArray;
  		  }, []);
  		}
  		
  		var rows = filtered.map(function(row, rowIndex) {
  			return (
  				<Table.Row 	key={rowIndex} 
  							fields={self.props.data.fields} 
  							row={row}
  				      style={self.props.style.row}>
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
							<Table.Cell key={columnIndex} row={self.props.row} field={field} style={self.props.style}>
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
		  		
		var rowId = null;
		if(this.props.row.hasOwnProperty('id')) {
		  rowId = this.props.row.id;
		}
	
		if(this.props.field.hasOwnProperty('type')) {
			switch(this.props.field.type) {
			case 'action':
			  var visible = true;
			  if(typeof this.props.field.visible === 'function') {
			    visible = this.props.field.visible(this.props.field, this.props.row);
			  }
			  if(visible) {
			    if(this.props.field.icon) {
			      var color = this.props.field.color || '#000000';
			      if(typeof color === 'function') {
			        color = color(this.props.field, this.props.row) || '#000000';
			      }
			      var icon =  this.props.field.icon;
			      if(typeof icon === 'function') {
			        icon = icon(this.props.field, this.props.row);
			      }
			      text = (
		          <i  className={'fa fa-' + icon + ' fa-fw table-action'}
		              style={{color : color}}
		              onClick={this.props.field.handler.bind(this, this.props.field, this.props.row)}>
		          </i>
	          );
			    } else {
			      text = (
		          <i  className="table-action" 
		              onClick={this.props.field.handler.bind(this, this.props.field, this.props.row)}>
		            <img src={this.props.field.image} />
              </i>
            );
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
			case 'alterable-boolean':
			  text = (<CellCheckbox 
			            checked={value}
			            rowId={rowId}
			            propertyName={this.props.field.name}
			            disabled={false} 
			            onUserClick={this.props.field.handler}
			          />);
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

		var style = {
		    maxHeight: this.props.style.rowHeight || 100,
		    overflowY: 'auto'
		};
    if((this.props.field.width) && (this.props.field.width > 0)) {
      style.width = this.props.field.width;
    }
    if(this.props.field.hasOwnProperty('align')) {
      style.textAlign = this.props.field.align;
    }

		if(typeof this.props.field.className === 'function') {
			return (
		    <td><div style={style} className={this.props.field.className(value)}>{text}</div></td>
			);	
		} else {
		  return (
        <td><div style={style}>{text}</div></td>
      );  
		}
	}
});

Table.Header = Header;

Table.Body = Body;

Table.Row = Row;

Table.Cell = Cell;

Table.PAGING_CLIENT_SIDE = PAGING_CLIENT_SIDE;
Table.PAGING_SERVER_SIDE = PAGING_SERVER_SIDE;

module.exports = Table;
