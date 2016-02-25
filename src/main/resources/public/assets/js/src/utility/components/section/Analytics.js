var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Chart = require('../Chart');
var LeafletMap = require('../LeafletMap');
var Table = require('../Table');
var ChartWizard = require('../ChartWizard');
var ChartConfig = require('../ChartConfig');
var Tag = require('../Tag');
var Message = require('../Message');
var JobConfig = require('../JobConfig');

var Analytics = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
			chart: false,
			mode: 'queries'
    	};
	},

	toggleView(view) {
		this.setState({chart : !this.state.chart});
  	},

	toggleMode(mode, e) {
		this.setState({
			mode : mode
		});
  	},
  	
  	toggleExpanded() {
  		this.setState({expanded: !this.state.expanded});
  	},
	
  	render: function() {
  		var chartData = {
		    series: [{
		        legend: 'Alicante (average)',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: [{
		            id: 1,
		            volume: 25,
		            date: new Date(2016, 1, 1)
		        }, {
		            id: 1,
		            volume: 70,
		            date: new Date(2016, 1, 2)
		        }, {
		            id: 1,
		            volume: 75,
		            date: new Date(2016, 1, 3)
		        }, {
		            id: 1,
		            volume: 62,
		            date: new Date(2016, 1, 4)
		        }, {
		            id: 1,
		            volume: 53,
		            date: new Date(2016, 1, 5)
		        }, {
		            id: 1,
		            volume: 27,
		            date: new Date(2016, 1, 6)
		        }, {
		            id: 1,
		            volume: 41,
		            date: new Date(2016, 1, 7)
		        }, {
		            id: 1,
		            volume: 45,
		            date: new Date(2016, 1, 8)
		        }, {
		            id: 1,
		            volume: 13,
		            date: new Date(2016, 1, 9)
		        }]
		    }, {
		        legend: 'User 1',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: [{
		            id: 1,
		            volume: 15,
		            date: new Date(2016, 1, 1)
		        }, {
		            id: 1,
		            volume: 30,
		            date: new Date(2016, 1, 2) 
		        }, {
		            id: 1,
		            volume: 44,
		            date: new Date(2016, 1, 3)
		        }, {
		            id: 1,
		            volume: 32,
		            date: new Date(2016, 1, 4)
		        }, {
		            id: 1,
		            volume: 23,
		            date: new Date(2016, 1, 5)
		        }, {
		            id: 1,
		            volume: 11,
		            date: new Date(2016, 1, 6)
		        }, {
		            id: 1,
		            volume: 18,
		            date: new Date(2016, 1, 7)
		        }, {
		            id: 1,
		            volume: 11,
		            date: new Date(2016, 1, 8)
		        }, {
		            id: 1,
		            volume: 5,
		            date: new Date(2016, 1, 9)
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

		var modeTitle = 'Queries', icon = 'list';
		switch(this.state.mode) {
			case 'job':
				icon = 'cog';
				modeTitle = 'Job Scheduling';
				break;
			case 'chart':
				icon = 'bar-chart';
				modeTitle = 'Chart Configuration';
				break;
			case 'history':
				icon = 'clock-o';
				modeTitle = 'Job Management';
				break;
			default:
				icon = 'list';
				modeTitle ='Chart Selection';
				break;
		}

		const configTitle = (
				<span>
					<i className={'fa fa-' + icon + ' fa-fw'}></i>
					<span style={{ paddingLeft: 4 }}>{modeTitle}</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.toggleMode.bind(this, 'history')}>
							<i className='fa fa-clock-o fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.toggleMode.bind(this, 'chart')}>
							<i className='fa fa-bar-chart fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.toggleMode.bind(this, 'job')}>
							<i className='fa fa-cog fa-fw'></i>
						</Bootstrap.Button>
					</span>
					<span style={{float: 'right',  marginTop: -3 }}>
						<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.toggleMode.bind(this, 'queries')}>
							<i className='fa fa-list fa-fw'></i>
						</Bootstrap.Button>
					</span>
				</span>
			);
		
		const dataTitle1 = (
			<span>
				<i className={'fa fa-' + (this.state.chart ? 'map' : 'bar-chart') + ' fa-fw'}></i>
				<span style={{ paddingLeft: 4 }}>Daily consumption for the last 7 days</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle'>
						<i className='fa fa-remove fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle'>
						<i className='fa fa-copy fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle'>
						<i className='fa fa-pencil fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle' onClick={this.toggleView}>
						<i className={'fa fa-' + (this.state.chart ? 'bar-chart' : 'map') + ' fa-fw'}></i>
					</Bootstrap.Button>
				</span>
			</span>
		);
		
		const dataTitle2 = (
			<span>
				<i className='fa fa-map fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Favourite users hourly consumption</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button bsStyle='default' className='btn-circle'>
						<i className='fa fa-remove fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle'>
						<i className='fa fa-copy fa-fw'></i>
					</Bootstrap.Button>
				</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle='default' className='btn-circle'>
						<i className='fa fa-pencil fa-fw'></i>
					</Bootstrap.Button>
				</span>
			</span>
		);
		var configContent, dataContent1, dataContent2;
		if(this.state.chart) {
			dataContent1 = (
				<LeafletMap style={{ width: '100%', height: 600}} 
							elementClassName='mixin'
							prefix='map'
							options={mapOptions} />
			);
		} else {
			dataContent1 = (
				<Chart 	style={{ width: '100%', height: 600 }} 
						elementClassName='mixin'
						prefix='chart'
						options={chartOptions}
						data={chartData}/>
			);
		}

		dataContent2 = (
			<LeafletMap style={{ width: '100%', height: 600}} 
						elementClassName='mixin'
						prefix='map'
						options={mapOptions} />
		);

  		var jobs = {
			fields: [{
				name: 'id',
				hidden: true
			}, {
				name: 'description',
				title: 'Description'
			}, {
				name: 'owner',
				title: 'Owner'			
			}, {
				name: 'createdOn',
				title: 'Created On',
				type: 'datetime'
			}, {
				name: 'scheduledOn',
				title: 'Next Execution',
				type: 'datetime'
			}, {
				name: 'status',
				title: 'Status'
			}, {
				name: 'progress',
				title: 'Progress',
				type: 'progress'
			}, {
				name: 'edit',
				type:'action',
				icon: 'pencil',
				handler: function() {
					console.log(this);
				}
			}, {
				name: 'cancel',
				type:'action',
				icon: 'remove',
				handler: function() {
					console.log(this);
				}
			}],
			rows: [{
				id: 1,
				description: 'Daily average consumption over time interval 01/01/2016 - 31/01/2016',
				owner: 'George',
				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				scheduledOn: new Date((new Date()).getTime() + Math.random() * 3600000),
				status: 'Running',
				progress: 45
			}],
			pager: {
				index: 0,
				size: 1,
				count:2
			}
		};
  		
  		var footerContent;
  		
		switch(this.state.mode) {
			case 'job':
				configContent = (
					<JobConfig />
				);
				break;
			case 'chart':
				configContent = (
					<ChartConfig />
				);
				break;
			case 'history':
				configContent = (
					<div style={{ padding: 10}}>
						<Table data={jobs}></Table>
					</div>
				);
				
				footerContent = (
					<Bootstrap.ListGroupItem>
						<span style={{ paddingLeft : 7}}> </span>
						<Link to='/scheduler' style={{ paddingLeft : 7, float: 'right'}}>View job management</Link>
					</Bootstrap.ListGroupItem>
				);
				break;
			default:
				var actions = [{
					name: 'edit',
					icon: 'pencil',
					handler: function() {
						console.log(this);
					}
				}, {
					name: 'copy',
					icon: 'copy',
					handler: function() {
						console.log(this);
					}
				}, {
					name: 'remove',
					icon: 'remove',
					handler: function() {
						console.log(this);
					}
				}];

				configContent = (
					<Tag.Collection>
						<Tag.Item key={1} text='Daily consumption for the last 7 days' actions={actions} checked={true} />
						<Tag.Item key={2} text='Alicante Trial group monthly consumption' actions={actions} />
						<Tag.Item key={3} text='Favourite users hourly consumption heatmap' actions={actions} checked={true} />
					</Tag.Collection>
				);

				break;
		}

		var content;
		if(this.state.mode === 'queries') {
			content = (
				<div className='row'>
					<div className='col-lg-6'>
						<Bootstrap.Panel header={dataTitle1}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									{dataContent1}
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
					<div className='col-lg-6'>
						<Bootstrap.Panel header={dataTitle2}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									{dataContent2}
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
			);
		}

		const jobTitle = (
			<span>
				<i className='fa fa-clock-o fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Active Jobs</span>
				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
					<Bootstrap.Button	bsStyle="default" className="btn-circle">
						<Bootstrap.Glyphicon glyph="plus" />
					</Bootstrap.Button>
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
					<div className='col-lg-12'>
						<Bootstrap.Panel expanded={this.state.expanded} onSelect={this.toggleExpanded} header={configTitle}>
							<Bootstrap.ListGroup fill>
								{configContent}
								{footerContent}		
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
				{content}
            </div>
 		);
  	}
});

Analytics.icon = 'bar-chart';
Analytics.title = 'Section.Analytics';

module.exports = Analytics;
