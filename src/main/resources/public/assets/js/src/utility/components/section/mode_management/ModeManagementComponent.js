var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../../Breadcrumb');
var EditTable = require('../../EditTable');
var Modal = require('../../Modal');
var FilterPanel = require('../../FilterPanel');

var Helpers = require('../../../helpers/array-funcs');

var ModeManagementActions = require('../../../actions/ModeManagementActions');
var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

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
		this.closeModal();
		this.props.deactivateUser({userDeactId: this.props.userToDecativate.id});
	},
	
	showModalSaveChanges: function(changedModes){
		var _t = this.context.intl.formatMessage;
		var title = _t({id:'Modal.SaveUserModeChanges.Title'});
		var body;
		if(changedModes.length === 1){
			body = changedModes.length + _t({id:'Modal.SaveUserModeChanges.Body.singular'});
		} else {
			body = changedModes.length + _t({id:'Modal.SaveUserModeChanges.Body.plural'});
		}
		var actions = [{
				action: this.closeModal,
				name: _t({id:'Buttons.Cancel'})
			}, {
				action: this.saveModeChanges,
				name: _t({id:'Buttons.SaveChanges'}),
				style: 'success'
			}
		];
		this.changedModes = changedModes;
		this.openModal(title, body, actions);
	},
	
	saveModeChanges: function(){		
		var changes = {};
		changes.modeChanges = [];
		this.changedModes.forEach(function(value){
			var entry = {};
			entry.id = value.id;
			entry.changes = [];
			Object.keys(value.modes).forEach(function(k, v){
				if (value.modes[k].draft){
					var change = {};
					change.mode = k;
					change.value = value.modes[k].value;
					entry.changes.push(change);
				}
			});
			changes.modeChanges.push(entry);
			
		});

		this.closeModal();
		this.props.saveModeChanges(changes);
	},
	
	searchName: function(nameFilter){
		this.props.applyNameFilter(nameFilter);
	},
	
	computeModesState: function (data){
		var modesState = {};
		var propertyNames = Helpers.pluck(
						Helpers.pickQualiffiedOnEquality(data.fields, 'type', 'property'),
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
				name: _t({id:'Buttons.Cancel'})
			},  {
				action: self.decativateUser,
				name: _t({id:'Buttons.Deactivate'}),
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
  				 						placeholder={this.props.nameFilter.length > 0 ? this.props.nameFilter : _t({ id:'Table.User.searchUsers'})}
  				 						ref='search'
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
		nameFilter: state.mode_management.nameFilter,
		activePage: state.mode_management.activePage,
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

		applyNameFilter : bindActionCreators(ModeManagementActions.applyNameFilter, dispatch),
		
		setModal: function(modal){
			dispatch(ModeManagementActions.setModal(modal));
		},
		
		markUserForDeactivation: function(userId){
			dispatch(ModeManagementActions.markUserForDeactivation(userId));
		},
				
		saveModeChanges: bindActionCreators(ModeManagementActions.saveModeChanges, dispatch),
		
		deactivateUser: bindActionCreators(ModeManagementActions.deactivateUser, dispatch)
	};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(ModeManagementComponent);

