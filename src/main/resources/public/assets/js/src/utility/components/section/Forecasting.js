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
var JobConfigForecasting = require('../JobConfigForecasting');
var moment = require('moment');
var Timeline = require('../Timeline');
var FilterTag = require('../chart/dimension/FilterTag');

var createPoints = function() {
	var points = [];
	
	for(var i=0; i<50; i++) {
		points.push([38.35 + 0.02 * Math.random(), -0.521 + 0.05 * Math.random(), Math.random()]);
	}
	
	return points;
};

var createSeries = function(ref, days, baseConsumption, offset) {
	var series = [];
	for(var d=0; d < days; d++) {
		series.push({
			volume: (baseConsumption + Math.random() * offset).toFixed(0),
			date: ref.clone().toDate()
		});
		ref.add(1, 'days');
	}

	return series;
};

var Forecasting = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
			map: false,
			mode: 'queries',
			points: []
    	};
	},

	toggleView(view) {
		this.setState({map : !this.state.map});
  	},

	toggleMode(mode, e) {
		this.setState({
			mode : mode
		});
  	},
  	
  	toggleExpanded() {
  		this.setState({expanded: !this.state.expanded});
  	},

    componentWillMount : function() {
		this.setState({points : createPoints()});
	},

  	render: function() { 	  	
  		var chartData = {
		    series: [{
		        legend: 'Forecasting',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: createSeries(moment(new Date()).subtract(7, 'days'), 14, 2000, 100)
		    }, {
		        legend: 'Actual consumption',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: createSeries(moment(new Date()).subtract(7, 'days'), 7, 1800, 300)
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
			case 'history':
				icon = 'clock-o';
				modeTitle = 'Job Management';
				break;
			default:
				icon = 'list';
				modeTitle ='Results';
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
				<i className={'fa fa-' + (this.state.map ? 'map' : 'bar-chart') + ' fa-fw'}></i>
				<span style={{ paddingLeft: 4 }}>Daily consumption forecasting for the next 7 days</span>
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
						<i className={'fa fa-' + (this.state.map ? 'bar-chart' : 'map') + ' fa-fw'}></i>
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
		
		var configContent, dataContent1;
		
		var self = this;
		
		var onChangeTimeline = function(value) {
			self.setState({points: createPoints()});
		};
		
		if(this.state.map) {
			dataContent1 = (
				<Bootstrap.ListGroup fill>
					<Bootstrap.ListGroupItem>
						<LeafletMap style={{ width: '100%', height: 600}} 
									elementClassName='mixin'
									prefix='map'
									options={mapOptions}
									points={this.state.points}/>
						<Timeline onChange={onChangeTimeline} style={{paddingTop: 10}}></Timeline>
					</Bootstrap.ListGroupItem>
				</Bootstrap.ListGroup>					
			);
		} else {
			var chartFilterTags = [];
    		chartFilterTags.push( 
    			<FilterTag key='time' text='Next 7 days' icon='calendar' />
        	);
        	chartFilterTags.push( 
    			<FilterTag key='population' text='All' icon='group' />
        	);
        	chartFilterTags.push( 
    			<FilterTag key='spatial' text='Alicante' icon='map' />
        	);
				
			dataContent1 = (
				<Bootstrap.ListGroup fill>
					<Bootstrap.ListGroupItem>
						<Chart 	style={{ width: '100%', height: 600 }} 
								elementClassName='mixin'
								prefix='chart'
								options={chartOptions}
								data={chartData}/>
					</Bootstrap.ListGroupItem>
					<Bootstrap.ListGroupItem className='clearfix'>				
						<div className='pull-left'>
							{chartFilterTags}
						</div>
						<span style={{ paddingLeft : 7}}> </span>
						<Link className='pull-right' to='/scheduler' style={{ paddingLeft : 7, paddingTop: 12 }}>Job Scheduler</Link>
					</Bootstrap.ListGroupItem>
				</Bootstrap.ListGroup>
			);
		}

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
				description: 'Forecast average consumption over time interval 01/03/2016 - 31/03/2016',
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
					<JobConfigForecasting type='Forecasting'/>
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
						<Tag.Item key={1} text='Daily consumption forecasting for the next 7 days' actions={actions} checked={true} />
					</Tag.Collection>
				);

				break;
		}

		var content;
		if(this.state.mode === 'queries') {
			content = (
				<div className='row'>
					<div className='col-lg-12'>
						<Bootstrap.Panel header={dataTitle1}>
							{dataContent1}
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

Forecasting.icon = 'line-chart';
Forecasting.title = 'Section.Forecasting';

module.exports = Forecasting;
