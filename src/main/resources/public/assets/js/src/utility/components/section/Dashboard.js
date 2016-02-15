var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');

var Breadcrumb = require('../Breadcrumb');
var Counter = require('../Counter');
var Message = require('../Message');
var Chart = require('../Chart');
var LeafletMap = require('../LeafletMap');
var Table = require('../Table');

var Dashboard = React.createClass({
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

  		var chartData = {
		    series: [{
		        legend: 'Daily Consumption',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: [{
		            id: 1,
		            name: 'Sales',
		            volume: 25,
		            date: new Date(2016, 1, 1),
		            geometry: { }
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 70,
		            date: new Date(2016, 1, 2),
		            geometry: { } 
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 75,
		            date: new Date(2016, 1, 3),
		            geometry: { }
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 62,
		            date: new Date(2016, 1, 4),
		            geometry: { }
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 53,
		            date: new Date(2016, 1, 5),
		            geometry: { }
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 27,
		            date: new Date(2016, 1, 6),
		            geometry: { }
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 41,
		            date: new Date(2016, 1, 7),
		            geometry: { }
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 45,
		            date: new Date(2016, 1, 8),
		            geometry: { }
		        }, {
		            id: 1,
		            name: 'Sales',
		            volume: 13,
		            date: new Date(2016, 1, 9),
		            geometry: { }
		        }]
		    }]
		};
  		
        var chartOptions = {
            tooltip: {
                show: true
            }
        };
        
		var mapOptions = {
			center:	[38.35, -0.48], 
			zoom: 13
		};
        
		const chartTitle = (
			<span>
				<i className="fa fa-bar-chart fa-fw"></i>
				<span style={{ paddingLeft: 4 }}>Daily Consumption</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle="default" className="btn-circle">
						<Bootstrap.Glyphicon glyph="fullscreen" />
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5  }}>
					<Bootstrap.Button	bsStyle="default" className="btn-circle">
						<Bootstrap.Glyphicon glyph="cog" />
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3 }}>
					<Bootstrap.Button	bsStyle="default" className="btn-circle">
						<Bootstrap.Glyphicon glyph="heart-empty" />
					</Bootstrap.Button>
				</span>
			</span>
		);

		const mapTile = (
			<span>
				<i className="fa fa-map fa-fw"></i>
				<span style={{ paddingLeft: 4 }}>Daily Consumption Heatmap</span>
				<span style={{float: 'right',  marginTop: -5 }}>
					<Bootstrap.SplitButton title='Actions' id='map-actions'>
						<Bootstrap.MenuItem eventKey="1">Action</Bootstrap.MenuItem>
						<Bootstrap.MenuItem eventKey="2">Another action</Bootstrap.MenuItem>
						<Bootstrap.MenuItem eventKey="3">Something else here</Bootstrap.MenuItem>
						<Bootstrap.MenuItem divider />
						<Bootstrap.MenuItem eventKey="4">Separated link</Bootstrap.MenuItem>
					</Bootstrap.SplitButton>
			    </span>
			</span>
		);
  		return (
			<div className="container-fluid" style={{ paddingTop: 10 }}>
				<div className="row">
					<div className="col-md-12">
						<Breadcrumb routes={this.props.routes}/>
					</div>
				</div>
				<div className="row">
					<div className="col-md-2">
						<div style={{ marginBottom: 20 }}>
							<Counter text={'Counter.Users'} value={198} />
						</div>
					</div>
					<div className="col-md-2">
						<div style={{ marginBottom: 20 }}>
							<Counter text={'Counter.Meters'} value={75} color='#1abc9c'/>
						</div>
					</div>
					<div className="col-md-2">
						<div style={{ marginBottom: 20 }}>
							<Counter text={'Counter.Devices'} value={230} color='#27ae60' />
						</div>
					</div>
					<div className="col-md-6">
						<div style={{ marginBottom: 20 }}>
							<Message text={'Administrator account jkouvar@daiad.eu has been locked. More than three failed login attempts detected.'} color='#c0392b' />
						</div>
					</div>
				</div>
				<div className="row">
					<div className="col-lg-6">
						<Bootstrap.Panel header={chartTitle}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<Chart 	style={{ width: '100%', height: 400 }} 
											elementClassName='mixin'
											prefix='chart'
											options={chartOptions}
											data={chartData}/>
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<span style={{ paddingLeft : 7}}> </span>
									<a style={{ paddingLeft : 7, float: 'right'}} href='#'>View analytics</a>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
					<div className="col-lg-6">
						<Bootstrap.Panel header={mapTile}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<LeafletMap style={{ width: '100%', height: 400}} 
												elementClassName='mixin'
												prefix='map'
												options={mapOptions} />
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<span style={{ paddingLeft : 7}}> </span>
									<a style={{ paddingLeft : 7, float: 'right'}} href='#'>View timeline</a>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
				<div className="row">
					<div className="col-lg-6">
						<Bootstrap.Panel collapsible defaultExpanded header={ (<span><i className="fa fa-bell fa-fw"></i><span style={{ paddingLeft: 4 }}>Notifications / Alerts</span></span>) }>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									<i className="fa fa-comments fa-fw"></i>
									<span style={{ paddingLeft : 7}}>New Comment</span>
									<span className="pull-right text-muted small"><em>4 minutes ago</em></span>
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<i className="fa fa-cogs fa-fw"></i>
									<span style={{ paddingLeft : 7}}>New Task</span>
									<span className="pull-right text-muted small"><em>12 minutes ago</em></span>
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<i className="fa fa-envelope fa-fw"></i>
									<span style={{ paddingLeft : 7}}>Message Sent</span>
	                                <span className="pull-right text-muted small"><em>27 minutes ago</em></span>
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<i className="fa fa-exclamation fa-fw" style={{ color: 'red'}}></i>
									<span style={{ paddingLeft : 7}}>Server master-c1-n01 has gone offline</span>
									<span className="pull-right text-muted small"><em>1 hour ago</em></span>
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem>
									<span style={{ paddingLeft : 7}}> </span>
									<a style={{ paddingLeft : 7, float: 'right'}} href='#'>View all alerts</a>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
	                </div>
                </div>
            </div>
 		);
  	}
});

Dashboard.icon = 'dashboard';
Dashboard.title = 'Section.Dashboard';

module.exports = Dashboard;
