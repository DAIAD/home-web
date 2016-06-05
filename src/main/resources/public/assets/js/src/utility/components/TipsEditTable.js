var React = require('react');
var ReactDOM = require('react-dom');

var {FormattedMessage, FormattedTime, FormattedDate} = require('react-intl');
var { Link } = require('react-router');

var Bootstrap = require('react-bootstrap');
var Checkbox = require('./Checkbox');

var EditTable = React.createClass({
	
  onPageIndexChange(event, selectedEvent) {
    this.props.setActivePage(selectedEvent.eventKey - 1);
  },
  
  syncStateData: function (){
    var syncdData = this.props.data;
    var syncdRows = syncdData.rows;
    var self = this;
    for (var r = 0, len = syncdRows.length; r < len; r++){
      var row = syncdRows[r];

//      for (var m in self.props.modes[row.id].modes){
//        if(self.props.modes[row.id].modes.hasOwnProperty(m)){
//                row[m] = self.props.modes[row.id].modes[m].value;
//        }
//      }
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
//      for (var m in self.props.modes[row.id].modes){
//              rowDraftFlags[m] = self.props.modes[row.id].modes[m].draft;
//      }
      visibleCellDraftFlags[row.id] = rowDraftFlags;
    }
    return visibleCellDraftFlags;
  },
	
  handleCheckboxChange: function (rowId, propertyName, currentValue){
    var truthTable = {
                    'active': true,
                    'deactivated': false
    };

    var inverseTruthTable = {
                    true: 'active',
                    false: 'deactivated'
    };

    //var currentModesState = Object.assign({}, this.props.modes);
    var currentModesState = [];
    console.log('rowId '+ rowId +' propertyName ' + propertyName + ' currentValue ' + currentValue);
    var obj = self.props;
    function iter(obj) {
      for (var key in obj) {
        if (typeof(obj[key]) == 'object') {
          iter(obj[key]);
        } else {
          alert("Key: " + key + " Values: " + obj[key]);
        }
      }
    }  

    var propValue;
    for(var propName in this.props) {
        propValue = currentModesState[propName];

        console.log(propName +' -> ' + propValue);
    }    

    currentModesState[rowId].modes[propertyName] = {
                    value: inverseTruthTable[!truthTable[true]],
                    draft: !this.props.modes[rowId].modes[propertyName].draft								  
    };
        
    console.log('value ' + currentModesState[rowId].modes[propertyName].value);
    console.log('draft' + currentModesState[rowId].modes[propertyName].draft);
    this.props.setModes(currentModesState);
  },
	
  toggleCheckBoxes: function (propertyName, toggleState){	
    var truthTable = {
      'active': true,
      'deactivated': false
    };

    var inverseTruthTable = {
      true: 'active',
      false: 'deactivated'
    };

    var visibleRowIds = [];
    for (var i = 0; i<this.props.data.rows.length ; i++){
            visibleRowIds.push(this.props.data.rows[i].id);
    }

    var currentModesState = Object.assign({}, this.props.modes);
    console.log('toggleState ' + toggleState);
    console.log('this.props.modes ' + this.props.modes);
    switch(toggleState) {
    case 'selectAll':
        for (let r = 0, len = visibleRowIds.length; r < len; r++){
          let rowId = visibleRowIds[r];
          if(currentModesState[rowId].active === true){
            if (currentModesState[rowId].modes[propertyName].value === 'NOT_APPLICABLE'){
                continue;
            } else {
                currentModesState[rowId].modes[propertyName] = {
                                value: 'active',
                                draft: (truthTable[currentModesState[rowId].modes[propertyName].value] === 
                                        currentModesState[rowId].modes[propertyName].draft) ? true : false
                };
            }
          }
        }
        break;
    case 'unSelectAll':
      for (var r = 0, len = visibleRowIds.length; r < len; r++){
        let rowId = visibleRowIds[r];
        if(currentModesState[rowId].active === true){
          if (currentModesState[rowId].modes[propertyName].value === 'NOT_APPLICABLE'){
            continue;
          } else {
            currentModesState[rowId].modes[propertyName] = {
              value: 'deactivated',
              draft: (truthTable[currentModesState[rowId].modes[propertyName].value] === 
                      currentModesState[rowId].modes[propertyName].draft) ? false : true
            };
          }
        }
      }
      break;                       
    }
    this.props.setModes(currentModesState);
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

  saveModeChanges: function(){
          this.props.saveAction(this.getChangedRows());
  },
  	
  countChangedRows: function(){
    var cnt = 0;
    for (var r in this.props.modes){
      if(this.props.modes.hasOwnProperty(r)){
        let row = this.props.modes[r];
        for (var p in row.modes){
          if(row.modes.hasOwnProperty(p)){
            let mode = row.modes[p];
            if(mode.draft === true){
                    cnt = cnt + 1;
                    break;
            }
          }
        }
      }
    }
    return cnt;
  },
	
  getChangedRows: function(){
    var changedRows = [];
    for (var r in this.props.modes){
      let row = this.props.modes[r];
      for (var p in row.modes){
        if(row.modes.hasOwnProperty(p)){
          let property = row.modes[p];
          if(property.draft === true){
            changedRows.push({
              id: r,
              modes: row.modes
            });
            break;
          }
        }
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
    if (this.countChangedRows() > 0){
      saveButton = (
        <div className='pull-left' style={{ marginTop : 20, marginBottom : 20}}>
          <button id='logout'
            type='submit'
            className='btn btn-primary'
              style={{ height: 33 }}
              //onClick={this.saveModeChanges}
                >
            <FormattedMessage id='Table.Save' />
          </button>
        </div>
      );
    }

    return (
      <div className='clearfix'>
        <Bootstrap.Table hover style={{margin: 0, padding: 0}}>
          <EditTable.Header 
            data = {this.props.data}
            toggleCheckBoxes = {this.toggleCheckBoxes}>
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
		var self = this;

		var header = this.props.data.fields.filter((f) => { return !!!f.hidden; }).map(function(field) {
			switch(field.type ) {
				case 'action':
                                  if(field.name === 'edit'){
                                    console.log('field type: action' + field.name);
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
                          console.log();
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

