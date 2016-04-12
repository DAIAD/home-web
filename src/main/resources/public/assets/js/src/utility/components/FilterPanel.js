var React = require('react');
var Bootstrap = require('react-bootstrap');
var DropDown = require('./DropDown');
var DismissableFilterTag = require('./DismissableFilterTag');

var ModeManagementActions = require('../actions/ModeManagementActions');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var FilterPanel = React.createClass({
	
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getDefaultProps: function() {
		return {
			filters: [],
			addFilter: null,
			removeFilter: null,
			filterStatus: {}
	    };
	},
	
	render: function(){
		console.log('RENDERING FilterPanel....................');
		var _t = this.context.intl.formatMessage;
		var self = this;
		
		var dropDownButtons = [];
		this.props.filters.forEach(function(filter){
			var options = [];
			for(var k in filter.options){
				if (filter.options.hasOwnProperty(k)) {
					var optObj = {
						label: k,
						value: filter.options[k],
						key: filter.id + '.' + filter.options[k],
						filter: filter.id
					};
					options.push(optObj);
				}
			}
			dropDownButtons.push(
				(
					<DropDown
						title={filter.name}
						key={filter.name}
						options={options}
						onSelect={self.props.applyAddFilter}
					    icon={filter.icon}
						disabled={(typeof self.props.filterStatus[filter.id] !== 'undefined')}
					/>		
				)
			);
		});
		
		var filterTags = [];
		for(var k in this.props.filterStatus){
			if(this.props.filterStatus[k]){
				var tagText = ( <span><b>{this.props.filterStatus[k].name}: </b>{this.props.filterStatus[k].label}</span> );
				filterTags.push(
					(
						<DismissableFilterTag
						    filter={this.props.filterStatus[k].name}
							key={this.props.filterStatus[k].name}
							text={tagText}
							icon={this.props.filterStatus[k].icon}
							onSelect={self.props.applyRemoveFilter}
							/>
					)
				);
			}
		}
		
		var filterTagsListItem;
		if (filterTags.length > 0){
			filterTagsListItem = (
				<Bootstrap.ListGroupItem className='clearfix'>
					<div className='pull-left'>
						{filterTags}
					</div>
				</Bootstrap.ListGroupItem>
			);
		}
		
		const filterPanelTitle = (
			<span>
				<i className='fa fa-filter fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>{_t({ id:'FilterBar.Filters'})}</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
				</span>
			</span>
		);

		return (
	        <Bootstrap.Panel header={filterPanelTitle}>
				<Bootstrap.ListGroup fill>
					<Bootstrap.ListGroupItem className='clearfix'>
						<div className='pull-left'>
							{dropDownButtons}
						</div>
					</Bootstrap.ListGroupItem>
					{filterTagsListItem}
				</Bootstrap.ListGroup>
			</Bootstrap.Panel>	
		);
	}
});

function mapStateToProps(state) {
	return {
		filterStatus: state.mode_management.filterStatus
	};
}

function mapDispatchToProps(dispatch) {
	return {
		applyAddFilter : bindActionCreators(ModeManagementActions.applyAddFilter, dispatch),
		applyRemoveFilter : bindActionCreators(ModeManagementActions.applyRemoveFilter, dispatch)
	};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(FilterPanel);
