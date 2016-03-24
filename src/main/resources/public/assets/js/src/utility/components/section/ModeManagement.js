var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var EditTable = require('../EditTable');
var Modal = require('../Modal');
var FilterPanel = require('../FilterPanel');
//var RandomUsers = require('../../testData/Users');

var $ = require('jquery');

var ModeManagement = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		var filterStatus = {};
		$.each(this.filters, function(i, f){
			filterStatus[f.id] = null;
		});
		
		return {
			userToDecativate: null,
			changedRows: null,
			filterStatus: filterStatus,
			nameFilter: '',
			modal : {
				show: false,
				title: '',
				body: '',
				actions: []
			}
    	};
	},
		
	closeModal(){
		var modal = this.state.modal;
		modal.show = false;
		this.setState({ 
			modal: modal
		});
	},
		
	openModal(title, body, actions){
		var modal = this.state.modal;
		modal.show = true;
		modal.title = title;
		modal.body = body;
		modal.actions = actions;
		this.setState({
			modal: modal
		});
	},
	
	decativateUser(){
		console.log("Call API to deactivate user with id: " + this.state.userToDecativate.id);
		this.closeModal();
	},
	
	removeFilter: function(filter) {
    	var currentFilterStatus = this.state.filterStatus;
    	currentFilterStatus[filter] = null;
    	this.setState({filterStatus: currentFilterStatus});
    },
		
	addFilter: function(event, key){
		var currentFilterStatus = this.state.filterStatus;
		currentFilterStatus[key.filter] = {
			name: key.filter,
			value: key.value,
			label: key.label,
			icon: key.icon
		};
		this.setState({filterStatus: currentFilterStatus});
	},
	
	showModalSaveChanges: function(changedRows){
		var _t = this.context.intl.formatMessage;
		var title = _t({id:'Modal.SaveChanges.Title'});
		var body;
		if(changedRows.length === 1){
			body = changedRows.length + _t({id:'Modal.SaveChanges.Body.singular'});
		} else {
			body = changedRows.length + _t({id:'Modal.SaveChanges.Body.plural'});
		}
		var actions = [{
				action: this.closeModal,
				name: _t({id:'Modal.Buttons.Cancel'})
			}, {
				action: this.saveModeChanges,
				name: _t({id:'Modal.Buttons.SaveChanges'}),
				style: 'success'
			}
		];
		this.setState({changedRows: changedRows});
		this.openModal(title, body, actions);
	},
	
	saveModeChanges: function(){
		console.log('Call API to save changes..................');
		console.log(this.state.changedRows);
		this.closeModal();
	},
	
	searchName: function(name){
		this.setState({nameFilter: name});
	},
	
	
	filters: [{
		id: 'group',
		name: 'Group',
		field: 'group',
		icon: 'group',
		type: 'text',
	}, {
		id: 'b1',
		name: 'b1',
		field: 'b1',
		icon: 'tachometer',
		type: 'boolean',		
	}, {
		id: 'mobile',
		name: 'Mobile',
		field: 'mobile',
		icon: 'mobile',
		type: 'boolean',
	}, {
		id: 'social',
		name: 'Social',
		field: 'social',
		icon: 'share',
		type: 'boolean',
	}],
	
  	render: function() {
  		  		
  		var self = this;
  		var _t = this.context.intl.formatMessage;
  		
  		var showModalUserDeactivate = function(){
  			var title = _t({ id:'Modal.DeactivateUser.Title'});
  			
  			var body = _t({ id:'Modal.DeactivateUser.Body.Part1'}) +
  				this.props.row.name +
				_t({ id:'Modal.DeactivateUser.Body.Part2'}) +
				this.props.row.id +
				_t({ id:'Modal.DeactivateUser.Body.Part3'});
  			
  			var actions = [{
					action: self.closeModal,
					name: _t({id:'Modal.Buttons.Cancel'})
				},  {
					action: self.decativateUser,
					name: _t({id:'Modal.Buttons.Deactivate'}),
					style: 'danger'
				}
			];
  			
  			self.openModal(title, body, actions);
  			
  			self.setState({
  				userToDecativate: this.props.row
  			});	
  			
  		};
  		
  		var getDistinctValuesArrayObjects = function(array, property){
  			var distincts = [];
  			var shownBefore = {};
  			
  			$.each(array, function (i,v){
  				if(!shownBefore[v[property]]){
  					shownBefore[v[property]] = true;
  					distincts.push(v[property]);
  				}
  			});
  			return distincts;
  		};
  		
		var users = {
			fields: [{
				name: 'id',
				title: 'Table.User.id',
				hidden: true
			}, {
				name: 'active',
				title: 'Table.User.active',
				hidden: true
			},{
				name: 'name',
				title: 'Table.User.name',
				link: '/user/{id}'
			}, {
				name: 'group',
				title: 'Table.User.group',
				link: '/group/{id}'
			}, {
				name: 'b1',
				title: 'Table.User.viewInfoOnB1',
				type:'property'
			}, {
				name: 'mobile',
				title: 'Table.User.viewInfoOnMobile',
				type:'property'
			}, {
				name: 'social',
				title: 'Table.User.allowSocial',
				type:'property'
			}, {
				name: 'deactivate',
				title: 'Table.User.deactivateUser',
				type:'action',
				icon: 'user-times',
				handler: showModalUserDeactivate

			}],
			rows: [{
				id: 10,
				name: 'Alvar Sandoval', 
				group: 'Alicante',
				b1: false,
				mobile: false,
				social: false,
				active:true
			}, {
				id: 20,
				name: 'Andres Colmenares', 
				group: 'Alicante',
				b1: true,
				mobile: false,
				social: false,
				active:false
			}, {
				id: 30,
				name: 'Ellis Hoffman', 
				group: 'St Albans',
				b1: false,
				mobile: true,
				social: false,
				active:true
			}, {
				id: 40,
				name: 'Rico De los Santos', 
				group: 'Alicante',
				b1: true,
				mobile: true,
				social: false,
				active:true
			}, {
				id: 50,
				name: 'Tasha Elliott', 
				group: 'St Albans',
				b1: true,
				mobile: true,
				social: true,
				active:true
			}, {
				id: 60,
				name: 'Wade Kennedy',
				group: 'St Albans',
				b1: false,
				mobile: false,
				social: false,
				active:true
			}, {
				id: 70,
				name: 'Shari Douglas', 
				group: 'St Albans',
				b1: false,
				mobile: true,
				social: false,
				active:true
			}, {
				id: 80,
				name: 'Leandra Pollino', 
				group: 'Alicante',
				b1: true,
				mobile: true,
				social: false,
				active:true
			}, {
				id: 90,
				name: 'Martha Taylor', 
				group: 'St Albans',
				b1: true,
				mobile: true,
				social: true,
				active:true
			}, {
				id: 100,
				name: 'Jesus Porter',
				group: 'St Albans',
				b1: false,
				mobile: false,
				social: false,
				active:true
			}, {
				id: 110,
				name: 'Mamie Hodges', 
				group: 'St Albans',
				b1: true,
				mobile: true,
				social: true,
				active:true
			}, {
				id: 120,
				name: 'Zachary Paul',
				group: 'St Albans',
				b1: false,
				mobile: false,
				social: false,
				active:true
			}],
			pager: {
				index: 1,
				size: 20
			}
		};
		
		//users.rows = RandomUsers.randomUsers;
  		
		var distinctGroups = getDistinctValuesArrayObjects(users.rows, 'group');
		var distinctGroupOptions = {};
		$.each(distinctGroups, function (i, group){
			distinctGroupOptions[group] = group;
		});
		var binaryOptions = {
			'On': true,
			'Off': false
		};
		
		$.each(this.filters, function (i, f){
			if (f.id === 'group'){
				f.options = distinctGroupOptions;
			} else {
				f.options = binaryOptions;
			}
		});
  		
  		var applyFilters = function(row){
  			var pass = true;
  			$.each(self.state.filterStatus, function (filterName, filterValue){
  				if(filterValue){
  					if(typeof(row[filterValue.name]) === "boolean"){  	
  						
  						if(row[filterValue.name] !== filterValue.value){
  							pass = false;
  						} 
  					} else {
  						if(row[filterValue.name] !== filterValue.value){
  							pass = false;
  						}
  					}
  				}
  			});
  			return pass;
  		};
  		
  		var filteredUsersRows = users.rows.filter(applyFilters);
  		users.rows = filteredUsersRows;
  		
  		// Filter rows with the search bar contents
  		if(this.state.nameFilter.length > 0){
	  		filteredUsersRows = [];
	  		$.each(users.rows, function(i, row){
	  			if(row.name.toLowerCase().indexOf(self.state.nameFilter.toLowerCase()) >= 0){
	  				filteredUsersRows.push(row);
	  			}
	  				
	  		});
	  		users.rows = filteredUsersRows;
  		}
  		
		const userTitle = (
			<span>
				<i className='fa fa-group fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>{_t({ id:'Table.User.Users'})}</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
				</span>
			</span>
		);
		
  		return (
			<div className='container-fluid' style={{ paddingTop: 10 }}>
				<div className='row'>
					<div className='col-md-12'>
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				<div className='row'>
					<div className='col-md-12'>						
						<Modal 	show = {this.state.modal.show}
								onClose = {this.closeModal}
								actions = {this.state.modal.actions}
								title = {this.state.modal.title}
								text = {this.state.modal.body}
						/>
					</div>
				</div>
				<div className='row'>
					<div className='col-md-12'>
					 	<Bootstrap.Input 	type='text'
					 						placeholder={_t({ id:'Table.User.searchUsers'})}
					 						ref="search"
					 						buttonAfter={<Bootstrap.Button onClick={function(){self.searchName(self.refs.search.getValue());}}><i className='fa fa-search fa-fw'></i></Bootstrap.Button>} 
					 	/>
			 		</div>
			 	</div>
			 	<div className='row'>
					<div className='col-md-12'>
						<FilterPanel 
							filters={this.filters}
							filterStatus={self.state.filterStatus}
							addFilter={self.addFilter}
							removeFilter={self.removeFilter}
						/>
					</div>
				</div>
				<div className='row'>
					<div className='col-md-12'>
						<Bootstrap.Panel header={userTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<EditTable 
											data={users}
											saveAction={this.showModalSaveChanges}
									/>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
			</div>
 		);
  	}
});

ModeManagement.icon = 'sliders';
ModeManagement.title = 'Section.ModeManagement';

module.exports = ModeManagement;
