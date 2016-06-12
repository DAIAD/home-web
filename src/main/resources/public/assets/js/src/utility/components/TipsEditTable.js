var React = require('react');
var ReactDOM = require('react-dom');

var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');

var Bootstrap = require('react-bootstrap');
var Checkbox = require('./Checkbox');

var changedRowsTemp = [];
var justSaved = false;
var saveDisabled;
var EditTable = React.createClass({
  onPageIndexChange(event, selectedEvent) {
    this.props.setActivePage(selectedEvent.eventKey - 1);
  }, 
 
  syncStateData: function (){
    var syncdData = this.props.data;
    var syncdRows = syncdData.rows;
    var self = this;
    if(this.props.currentTip){
      for (var r = 0, len = syncdRows.length; r < len; r++){
        var row = syncdRows[r];
  				  if(row.id == this.props.currentTip.id){
          row.title = this.props.currentTip.title;
          row.description = this.props.currentTip.description;
				    }
      }
    }
    syncdData.rows = syncdRows;	
    return syncdData;
  },
  syncInitialRows: function (){
    var syncdData = this.props.initialRows;
    var syncdRows = syncdData.rows;
    var self = this;
    if(this.props.currentTip){
      for (var r = 0, len = syncdRows.length; r < len; r++){
        var row = syncdRows[r];
  				  if(row.id == this.props.currentTip.id){
          row.title = this.props.currentTip.title;
          row.description = this.props.currentTip.description;
          row.active = this.props.currentTip.active;
				    }
      }
    }
    syncdData.rows = syncdRows;	
    return syncdData;
  },  
  getVisibleCellsDraftFlags: function(visibleRows){     
    var visibleCellDraftFlags = {};
    var self = this;
    for (var r = 0, len = visibleRows.length; r < len; r++){
      var row = visibleRows[r];
      var rowDraftFlags = {};
      visibleCellDraftFlags[row.id] = rowDraftFlags;
    }
    return visibleCellDraftFlags;
  },
	
  handleCheckboxChange: function (rowId, propertyName, currentValue){
    var newData = Object.assign({}, this.props.data);         
    for (var i in newData.rows) {
      if (newData.rows[i].index == rowId) {
        newData.rows[i].active = !newData.rows[i].active;      
        break; 
      }
    }
    this.props.setActivationChanged(newData);   
  },
  getDefaultProps: function() {
    return {
      data: {
        fields: [],
        rows: [],
        pager: {
          index: 0,
          size: 7
        }
      }
    };
  },
  saveActiveStatusChanges: function(){
    this.props.saveActiveStatusAction(this.getChangedRows());
    justSaved = true;
  }, 
	
  getChangedRows: function(){       
    var changedRows = [];

    var a1 = this.props.initialRows.rows;
    var a2 = this.props.data.rows;
    for(var i =0; i<a1.length; i++){
      if(a1[i].active !== a2[i].active){
        changedRows.push(a2[i]);
      }
    }    
    return changedRows;
  },

  render: function() {
    var self = this;
    var visibleData = Object.assign({}, 
      this.syncStateData(this.props.data),
      {rows: this.props.data.rows.slice(
        this.props.activePage * this.props.data.pager.size, (this.props.activePage + 1) * this.props.data.pager.size)
      }
    );
    
    var numberOfPages = Math.ceil(this.props.data.rows.length / this.props.data.pager.size); 
    var saveButton;
    
    if(justSaved){
      saveDisabled = true;
    }
    else if(!_.isEqual(this.props.initialRows.rows, this.props.data.rows)){  
      saveDisabled = false;       
    }   
    else{
      saveDisabled = true;
    }     
    justSaved = false;
    saveButton = (
      <div className='pull-left' style={{ marginTop : 20, marginBottom : 20}}>
        <button id='logout'
          type='submit'
          className='btn btn-primary'
            style={{ height: 33 }}
            disabled = {saveDisabled}
            onClick={this.saveActiveStatusChanges} >
          <FormattedMessage id='Table.Save' />
        </button>
      </div>
    );

    return (
      <div className='clearfix'>
        <Bootstrap.Table hover style={{margin: 0, padding: 0}}>
          <EditTable.Header 
            data = {this.props.data} >
          </EditTable.Header>
          <EditTable.Body data = {visibleData}
            draftFlags = {this.getVisibleCellsDraftFlags(visibleData.rows)}
            checkboxHandler = {this.handleCheckboxChange}>
          </EditTable.Body>			
        </Bootstrap.Table>
        {saveButton} 
        <div style={{float:'right'}}>
          <Bootstrap.Pagination prev  next  first last  ellipsis  
            items={numberOfPages}
            maxButtons={7}
            activePage={this.props.activePage + 1}
            onSelect={this.onPageIndexChange}
             />	
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
		var self = this;

		var header = this.props.data.fields.filter((f) => { return !!!f.hidden; }).map(function(field) {
			switch(field.type ) {
				case 'action':
      if(field.name === 'edit'){
      }                                  
					return (
						<th key={field.name} style={{ width: 24 }}>{field.title ? _t({ id: field.title}) : ''}</th>
					);
				case 'boolean':
					return (
						<th key={field.name} style={{ width: 90 }}>{field.title ? _t({ id: field.title}) : ''}</th>
					);
				case 'property':
					return (
						<th key={field.name} style={{ width: 90 }}>
							<IndeterminateCheckbox
						    propertyName={field.name}
						    checked={true}
								disabled={false}
								action={self.props.toggleCheckBoxes}
							/>
							{field.title ? _t({ id: field.title}) : ''}
						</th>
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
      var rows = this.props.data.rows.map(function(row, rowIndex) {
      return (
        <EditTable.Row   key={rowIndex} 
          fields={self.props.data.fields} 
          row={row}
          draftFlags={self.props.draftFlags[row.id]}
          checkboxHandler={self.props.checkboxHandler}>
        </EditTable.Row>
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
              <EditTable.Cell key={columnIndex} 
                  row={self.props.row} 
                  field={field}
                  draftFlag={self.props.draftFlags[field.name]}
                  checkboxHandler={self.props.checkboxHandler}>
              </EditTable.Cell>
            );
          })
        }
      </tr>
    );
  }
});

var Cell = React.createClass({
  
  	 render: function() {
      
  		var rowId = this.props.row.index;
  		var value = this.props.row[this.props.field.name];
  		var disabled = !this.props.row.active;
  		var text;
  		if (disabled)
  			 text = (<span className='disabled'>{value}</span>);
  		else
  			text = (<span>{value}</span>);
  		 		
  		if(this.props.field.hasOwnProperty('type')) {
      switch(this.props.field.type) { 
        case 'action':
          text = (<i className={'fa fa-' + this.props.field.icon + ' fa-fw table-action'} onClick={this.props.field.handler.bind(this)}></i>);
          break;
        case 'datetime':
          if(value) {
            text = (<FormattedTime 	
            value={value} 
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
          text = (<FormattedTime 	
          value={value} 
          hour='numeric' 
          minute='numeric' />);
          break;
        case 'progress':
          if(value !== null) {
            text = (<Bootstrap.ProgressBar now={value} label='%(percent)s%' />);
          } else {
            text = (<span />);
          }
          break;
        case 'boolean':
          text = (<Checkbox checked={value} 
          disabled={false} 
          rowId={rowId}
          propertyName={this.props.field.name}
          draftFlag={this.props.draftFlag}
          onUserClick={this.props.checkboxHandler}/>);                                                               
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

EditTable.Header = Header;

EditTable.Body = Body;

EditTable.Row = Row;

EditTable.Cell = Cell;

module.exports = EditTable;

