var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../../Breadcrumb');
var EditTable = require('../../EditTable');
var Modal = require('../../Modal');
var FilterPanel = require('../../FilterPanel');
var RandomUsers = require('../../../testData/Users');

var Helpers = require('../../../helpers/helpers');

var ModeManagementActions = require('../../../actions/ModeManagementActions');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var $ = require('jquery');

var ModeManagementComponent = React.createClass({
	
	changedModes: [],
	users: [],
	
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	closeModal(){
		var modal = Object.assign({}, this.props.modal);
		modal.show = false;
		this.props.setModal(modal);
	},
		
	openModal(title, body, actions){
		var modal = Object.assign({}, this.props.modal);
		modal.show = true;
		modal.title = title;
		modal.body = body;
		modal.actions = actions;
		this.props.setModal(modal);
	},
	
	decativateUser(){
		console.log("Call API to deactivate user with id: " + this.props.userToDecativate.id);
		this.closeModal();
	},
	
	showModalSaveChanges: function(changedModes){
		var _t = this.context.intl.formatMessage;
		var title = _t({id:'Modal.SaveChanges.Title'});
		var body;
		if(changedModes.length === 1){
			body = changedModes.length + _t({id:'Modal.SaveChanges.Body.singular'});
		} else {
			body = changedModes.length + _t({id:'Modal.SaveChanges.Body.plural'});
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
		this.changedModes = changedModes;
		this.openModal(title, body, actions);
	},
	
	saveModeChanges: function(){
		console.log('Call API to save changes..................');
		console.log(this.changedModes);
		this.closeModal();
		this.props.fetchUsers();
	},
	
	searchName: function(nameFilter){
		this.props.setNameFilter(nameFilter);
	},
	
	computeModesState: function (data){
		var modesState = {};
		var propertyNames = Helpers.pluck(
						Helpers.pickQualiffied(data.fields, 'type', 'property'),
						'name'
					);
		var self = this;
		var rowIds = Helpers.pluck(data.rows, 'id');
		
		for (var i = 0, len = rowIds.length; i < len; i++){
			var modeEntry = {};
			modeEntry.active = data.rows[i].active;
			modeEntry.modes = {};
			for (var p = 0, len2 = propertyNames.length; p < len2; p++){
				var mode = {
					value: data.rows[i][propertyNames[p]],
					draft: false
				};
				modeEntry.modes[propertyNames[p]] = mode;
			}
			modesState[rowIds[i]] = modeEntry;
		}
		return modesState;
	},
	
  	render: function() {  		  
  		console.log('RENDERING ModeManagementComponent....................');
  		  		
  		var self = this;
  		var _t = this.context.intl.formatMessage;
  		
  		this.users = Object.assign({}, this.props.users);
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
  			
  			self.props.markUserForDeactivation(this.props.row);	
  			
  		};
  		  		
  		for (let i=0; i < this.users.fields.length; i++){
  			if (this.users.fields[i].name === 'deactivate'){
  				this.users.fields[i].handler = showModalUserDeactivate;
  			}
  		}
  		
		var applyFilters = function(row){
  			var pass = true;
  			$.each(self.props.filterStatus, function (filterName, filterValue){
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
  		
  		var filteredUsersRows = this.users.rows.filter(applyFilters);
  		this.users.rows = filteredUsersRows;

  		// Filter rows with the search bar contents
  		if(this.props.nameFilter.length > 0){
	  		filteredUsersRows = [];
	  		$.each(this.users.rows, function(i, row){
	  			if(row.name.toLowerCase().indexOf(self.props.nameFilter.toLowerCase()) >= 0){
	  				filteredUsersRows.push(row);
	  			}
	  				
	  		});
	  		this.users.rows = filteredUsersRows;
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
						<Modal 	show = {this.props.modal.show}
								onClose = {this.closeModal}
								actions = {this.props.modal.actions || []}
								title = {this.props.modal.title || ''}
								text = {this.props.modal.body || ''}
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
							filters={this.props.filters}
						/>
					</div>
				</div>
				<div className='row'>
					<div className='col-md-12'>
						<Bootstrap.Panel header={userTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<EditTable
										data={this.users}
										saveAction={this.showModalSaveChanges}
										activePage={this.props.activePage}
										modes={this.props.modes}
										setModes={this.props.setModes}
										setActivePage={this.props.setActivePage}
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

function mapStateToProps(state) {
	return {
		users: state.mode_management.users,
		modes: state.mode_management.modes,
		activePage: state.mode_management.activePage,
		filterStatus: state.mode_management.filterStatus,
		nameFilter: state.mode_management.nameFilter,
		userToDecativate: state.mode_management.userToDecativate,
		modal: state.mode_management.modal,
		
	};
}

function mapDispatchToProps(dispatch) {
	return {
		setModes: function (modes){
			dispatch(ModeManagementActions.setModes(modes));
		},
		
		setActivePage: function(activePage){
			dispatch(ModeManagementActions.setActivePage(activePage));
		},
		
		setNameFilter: function (nameFilter){
			dispatch(ModeManagementActions.setNameFilter(nameFilter));
		},
		
		setModal: function(modal){
			dispatch(ModeManagementActions.setModal(modal));
		},
		
		markUserForDeactivation: function(userId){
			dispatch(ModeManagementActions.markUserForDeactivation(userId));
		},
		
		fetchUsers : bindActionCreators(ModeManagementActions.fetchUsers, dispatch)
	};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(ModeManagementComponent);

