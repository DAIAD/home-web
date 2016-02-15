var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');

var Demographics = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
			key: 1
    	};
	},

	selectSection(key) {
		this.setState({key : key});
  	},
	
  	render: function() {
  		var _t = this.context.intl.formatMessage;

  		var data = {
			fields: [{
				name: 'id',
				title: 'Table.Group.id'
			}, {
				name: 'name',
				title: 'Table.Group.name'
			}, {
				name: 'size',
				title: 'Table.Group.size'
			}, {
				name: 'createdOn',
				title: 'Table.Group.createdOn'
			}],
			rows: [{
				id: 1,
				name: 'Alicante DAIAD Trial',
				size: 97,
				createdOn: new Date()
			}, {
				id: 2,
				name: 'St. Albans DAIAD Trial',
				size: 32,
				createdOn: new Date()
			}],
			pager: {
				index: 0,
				size: 10,
				count:45
			}
		};
 
  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				<div className="row">
					<div className="col-md-12">
						<Bootstrap.Tabs bsStyle="pills" 
										position="left" 
										activeKey={this.state.key} 
										onSelect={this.selectSection}>
							<Bootstrap.Tab eventKey={1} title={_t({ id: 'Demographics.Group'})}>
								<div style={{marginLeft: 15}}>
									<Table data={data}></Table>
								</div>
							</Bootstrap.Tab>
						</Bootstrap.Tabs>
					</div>
				</div>
			</div>
 		);
  	}
});

Demographics.icon = 'group';
Demographics.title = 'Section.Demographics';

module.exports = Demographics;
