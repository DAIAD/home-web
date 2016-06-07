var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');

var Announcements = React.createClass({
  contextTypes: {
      intl: React.PropTypes.object
  },

  render: function() {
  		var _t = this.context.intl.formatMessage;
 
  		var data = {
  				fields: [{
  					name: 'id',
  					title: 'Id',
  					hidden: true
  				}, {
  					name: 'text',
  					title: 'Message'
  				}, {
  					name: 'dispatchedOn',
  					title: 'Dispatched On',
  					type: 'datetime'
  				}, {
  					name: 'status',
  					title: 'Status',
  					className: function(value) {
  						return 'danger';
  					},
  					hidden: true
  				}],
  				rows: [{
  					id: 1,
  					text: 'Save 10% on your monthly bill by installing Amphiro B1',
  					dispatchedOn: new Date((new Date()).getTime() + (3+Math.random()) * 3600000),
  					status: 'Pending',
  					acknowledged: true
  				}, {
  					id: 2,
  					text: 'Welcome to DAIAD!',
  					dispatchedOn: new Date((new Date()).getTime() - (950) * 3600000),
  					status: 'Received',
  					acknowledged: false
  				}],
  				pager: {
  					index: 0,
  					size: 10,
  					count:20
  				}
  			};
     
     var rows = [];
     var user1 = {username:'user1@gmail.com', accountRegisteredOn:new Date((new Date()).getTime() + (2+Math.random()) * 3600000)};
     rows.push(user1);
     
    var model = {
        fields: [{
          name: 'id',
          title: 'id',
          hidden: true
        }, {
          name: 'key',
          title: 'key',
          hidden: true
        }, {
          name: 'username',
          title: 'Username'
        }, {
          name: 'accountRegisteredOn',
          title: 'Registered On',
          type: 'datetime'
        }, {
          name: 'lastLoginSuccess',
          title: 'Last login on',
          type: 'datetime'
        }, {
          name: 'meter',
          type:'action',
          image: '/assets/images/utility/meter.svg',
          handler: function(field, row) {
            //if((row.key) && (row.numberOfMeters > 0)) {
              //self.props.actions.getMeters(this.props.row.key, this.props.row.username);
            //}
          },
          visible: function(field, row) {
            //return ((row.key) && (row.numberOfMeters > 0));
          }
        }],
        rows: rows,
        pager: {
          index: 0,
          size: 10,
          count:rows.length
        }
    };
    
    const filterTitle = (
      <span>
        <i className='fa fa-calendar fa-fw'></i>
          <span style={{ paddingLeft: 4 }}>Users</span>
          <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}></span>
      </span>
    );

    const historyTitle = (
      <span>
       <i className='fa fa-calendar fa-fw'></i>
       <span style={{ paddingLeft: 4 }}>History</span>
       <span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
      </span>
      </span>
     );

    var filter = (
      <div className='col-md-4'>
        <Bootstrap.Input type='text'
          id='filter' name='filter' ref='filter'
          placeholder='Search participants by email ...'
          //onChange={this.setFilter}
          //value={this.props.admin.filter}
        />
      </div>
    );  

    var table = (
      <div>
        <Table data={model}></Table>
      </div>
    );  

    return (
     <div className="container-fluid" style={{ paddingTop: 10 }}>
      <div className="row">
       <div className="col-md-12">
        <Breadcrumb routes={this.props.routes}/>
       </div>
      </div>
      <div className="row">
       <div className="col-md-12">
        <Bootstrap.Panel header={filterTitle}>
         <Bootstrap.ListGroup fill>
          <Bootstrap.ListGroupItem>	
            <div className="row">
              <div className="col-md-12"> 

                {filter}
              </div>
           </div>
            <div className="row">            
              {table}  
            </div>         
          </Bootstrap.ListGroupItem>
         </Bootstrap.ListGroup>
        </Bootstrap.Panel>       
        <Bootstrap.Panel header={historyTitle}>
         <Bootstrap.ListGroup fill>
          <Bootstrap.ListGroupItem>	
           <Table data={data}></Table>
          </Bootstrap.ListGroupItem>
         </Bootstrap.ListGroup>
        </Bootstrap.Panel>
       </div>
      </div>
     </div>
     );
  }
});

Announcements.icon = 'wechat';
Announcements.title = 'Section.ManageAlerts.Announcements';

module.exports = Announcements;
