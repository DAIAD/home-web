var React = require('react');
var ReactDOM = require('react-dom');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../../Breadcrumb');
var Chart = require('../../Chart');
var ClusterChart = require('../../ClusterChart');
var LeafletMap = require('../../LeafletMap');
var Table = require('../../Table');
var ChartWizard = require('../../ChartWizard');
var ChartConfig = require('../../ChartConfig');
var Tag = require('../../Tag');
var Message = require('../../Message');
var JobConfigAnalysis = require('../../JobConfigAnalysis');
var FilterTag = require('../../chart/dimension/FilterTag');
var Timeline = require('../../Timeline');

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

var createTimeLabels = function(ref, hours) {
	var series = [];

	for(var h=0; h < hours; h++) {
		series.push(ref.clone().toDate());
		ref.subtract(1, 'hours');
	}

	return series.reverse();
};

var Analytics = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
			chart: false,
			mode: 'queries',
			points: [],
			interval:[moment(new Date()).subtract(28, 'days'), moment()]
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
    
  	componentWillMount : function() {
		this.setState({points : createPoints()});
	},
	
  	render: function() {
  		var chartData = {
		    series: [{
		        legend: 'Alicante (average)',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: createSeries(moment(new Date()).subtract(28, 'days'), 29, 180, 60)
		    }, {
		        legend: 'User 1',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: createSeries(moment(new Date()).subtract(28, 'days'), 29, 150, 30)
		    }]
		};
  		
        var chartOptions = {
            tooltip: {
                show: true
            }
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
				<span style={{ paddingLeft: 4 }}>Daily consumption for the last 30 days</span>
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
				<span style={{ paddingLeft: 4 }}>Favourite users hourly consumption heatmap for the last 24 hours</span>
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
						  center={[38.35, -0.48]} 
					    zoom={13}
				      mode={LeafletMap.MODE_VECTOR} 
				/>
			);
		} else {
			dataContent1 = (
				<Chart 	style={{ width: '100%', height: 600 }} 
						elementClassName='mixin'
						prefix='chart'
						options={chartOptions}
						data={chartData}
						type='line'/>
			);
		}

		var onChangeTimeline = function(value) {
			this.setState({points: createPoints()});
		};
		
		var mapFilterTags = [];
		mapFilterTags.push( 
			<FilterTag key='time' text='Last 24 hours' icon='calendar' />
    	);
		mapFilterTags.push( 
			<FilterTag key='population' text='Favourites' icon='group' />
    	);
		mapFilterTags.push( 
			<FilterTag key='source' text='Meter' icon='database' />
    	);
			
		var today = new Date();
		dataContent2 = (
			<Bootstrap.ListGroupItem>
				<LeafletMap style={{ width: '100%', height: 600}} 
							elementClassName='mixin'
							prefix='map'
						  center={[38.35, -0.48]} 
              zoom={13}
				      mode={LeafletMap.MODE_HEATMAP}
							data={this.state.points} />
				<Timeline 	onChange={onChangeTimeline.bind(this)} 
							style={{paddingTop: 10}}
							min={1}
							max={24}
							value={24}
							type='time'
						    data={createTimeLabels(moment(new Date(today.getFullYear(), today.getMonth(), today.getDate(), today.getHours(), 0, 0)), 24)}>
				</Timeline>
			</Bootstrap.ListGroupItem>
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
					<JobConfigAnalysis />
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
						<Tag.Item key={1} text='Daily consumption for the last 30 days' actions={actions} checked={true} />
						<Tag.Item key={3} text='Favourite users hourly consumption heatmap for the last 24 hours' actions={actions} checked={true} />
					</Tag.Collection>
				);

				break;
		}

		var content;
		var chartFilterTags = [];
        
		var intervalLabel ='';
        if(this.state.interval) {
        	var start = this.state.interval[0].format('DD/MM/YYYY');
        	var end = this.state.interval[1].format('DD/MM/YYYY');
        	intervalLabel = start + ' - ' + end;
        	if (start === end) {
        		intervalLabel = start;
        	}
        }  

    	chartFilterTags.push( 
			<FilterTag key='time' text={intervalLabel} icon='calendar' />
    	);
    	chartFilterTags.push( 
			<FilterTag key='population' text='Alicante, User 1' icon='group' />
    	);
    	chartFilterTags.push( 
			<FilterTag key='spatial' text='Alicante' icon='map' />
    	);
    	chartFilterTags.push( 
			<FilterTag key='source' text='Meter, Amphiro' icon='database' />
    	);

		
		if(this.state.mode === 'queries') {
			content = (
				<div className='row'>
					<div className='col-lg-6'>
						<Bootstrap.Panel header={dataTitle1}>
							<Bootstrap.ListGroup fill>
								<Bootstrap.ListGroupItem>
									{dataContent1}
								</Bootstrap.ListGroupItem>
								<Bootstrap.ListGroupItem className='clearfix'>				
									<div className='pull-left'>
										{chartFilterTags}
									</div>
									<span style={{ paddingLeft : 7}}> </span>
									<Link className='pull-right' to='/forecasting' style={{ paddingLeft : 7, paddingTop: 12 }}>View forecasting</Link>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
					<div className='col-lg-6'>
						<Bootstrap.Panel header={dataTitle2}>
							<Bootstrap.ListGroup fill>
								{dataContent2}
								<Bootstrap.ListGroupItem className='clearfix'>
									<div className='pull-left'>
										{mapFilterTags}
									</div>
									<span style={{ paddingLeft : 7}}> </span>
									<Link className='pull-right' to='/forecasting' style={{ paddingLeft : 7, paddingTop: 12 }}>View forecasting</Link>
								</Bootstrap.ListGroupItem>
							</Bootstrap.ListGroup>
						</Bootstrap.Panel>
					</div>
				</div>
			);
		}
		const clusterTitle = (
			<span>
				<i className='fa fa-map fa-fw'></i>
				<span style={{ paddingLeft: 4 }}>Clusters of households based on income and consumption</span>
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

		var clusterFilterTags = [];

		clusterFilterTags.push( 
			<FilterTag key='filter1' text='01/01/2015 - 01/12/2015' icon='calendar' />
    	);
		clusterFilterTags.push( 
			<FilterTag key='filter2' text='Alicante' icon='map' />
    	);
		clusterFilterTags.push( 
			<FilterTag key='filter3' text='Income' icon='euro' />
    	);
		clusterFilterTags.push( 
			<FilterTag key='filter4' text='Consumption' icon='tachometer' />
    	);
    	
		var contentCluster = (
			<div className='row'>
				<div className='col-lg-12'>
					<Bootstrap.Panel header={clusterTitle}>
						<Bootstrap.ListGroup fill>
							<Bootstrap.ListGroupItem>
								<ClusterChart 	style={{ width: '100%', height: 600 }} 
												elementClassName='mixin'
												prefix='chart'/>
							</Bootstrap.ListGroupItem>
							<Bootstrap.ListGroupItem className='clearfix'>
								<div className='pull-left'>
									{clusterFilterTags}
								</div>
								<span style={{ paddingLeft : 7}}> </span>
								<Link className='pull-right' to='/forecasting' style={{ paddingLeft : 7, paddingTop: 12 }}>View forecasting</Link>
							</Bootstrap.ListGroupItem>
						</Bootstrap.ListGroup>
					</Bootstrap.Panel>
				</div>
			</div>
		);

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
				{contentCluster}
            </div>
 		);
  	}
});

Analytics.icon = 'bar-chart';
Analytics.title = 'Section.Analytics.Fav';

module.exports = Analytics;
