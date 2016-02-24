var React = require('react');
var moment = require('moment');
var Bootstrap = require('react-bootstrap');
var Select = require('react-select');
var DateRangePicker = require('react-bootstrap-daterangepicker');

var Wizard = require('./Wizard');
var Message = require('./Message');

var LabelStep  = React.createClass({
    getInitialState: function() {
        return {
            dimension: 'Population',
            granularity: 'Day',
            population: [{ value: 'Alicante', label: 'Alicante', type: 1 },
                         { value: 'User 1', label: 'User 1', type: 2 }],
            ranges: {
				'Today': [moment(), moment()],
				'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
				'Last 7 Days': [moment().subtract(6, 'days'), moment()],
				'Last 30 Days': [moment().subtract(29, 'days'), moment()],
				'This Month': [moment().startOf('month'), moment().endOf('month')],
				'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
			},
			startDate: moment().subtract(29, 'days'),
			endDate: moment()
        };
    },

  	render: function() {
  		var options = [
           	{ value: 'Time', label: 'Time' },
           	{ value: 'Population', label: 'Population' }
        ];

  		var onChangeDimension = function(val) {
  			this.setState({
  				dimension: (val ? val.value : null)
  			});
        };
        
  		var onChangeGranularity = function(val) {
  			this.setState({
  				granularity: val.value
  			});
        };
        
  		var onChangePopulation = function(val) {
  			this.setState({
  				population: (val ? val.map( (item) => { return item.value; } ) : [])
  			});
        };

        var renderOption = function(option) {
        	switch(option.type){
        		case 1:
            		return <span><i className='fa fa-group fa-fw'></i>{option.label}</span>;
        		case 2:
        			return <span><i className='fa fa-user fa-fw'></i>{option.label}</span>;
        	}
    	};

    	var onChangeInterval = function (event, picker) {
    		this.setState({
    			startDate: picker.startDate,
    			endDate: picker.endDate
    		});
    	};
    	
        var config;
        switch(this.state.dimension) {
        	case 'Time':
        		config = (
    				<div className='form-group col-md-6'>
						<label className='col-md-3 control-label' htmlFor='granularity'>Granularity</label>  
						<div className='col-md-7'>
							<Select name='granularity'
								value={this.state.granularity}
		  		            	options={[
	  		            	          { value: 'Hour', label: 'Hour' },
	  		            	          { value: 'Day', label: 'Day' },
	  		            	          { value: 'Month', label: 'Month' },
	  		                    ]}
		  		            	onChange={onChangeGranularity.bind(this)}
								clearable={false} 
							/>
							<span className='help-block'>Select time granularity for time interval</span>  
						</div>
					</div>
				);
        		break;
        	case 'Population':
        		config = (
        				<div className='form-group col-md-6'>
    						<label className='col-md-3 control-label' htmlFor='population'>Population</label>  
    						<div className='col-md-7'>
    							<Select name='population'
    								multi={true}
    								value={this.state.population}
    		  		            	options={[
    	  		            	          { value: 'Alicante', label: 'Alicante', type: 1 },
    	  		            	          { value: 'St. Albans', label: 'St. Albans', type: 1 },
    	  		            	          { value: 'User 1', label: 'User 1', type: 2 },
    	  		                    ]}
    								optionRenderer={renderOption.bind(this)}
    		  		            	onChange={onChangePopulation.bind(this)}
    								clearable={true} 
    							/>
    							<span className='help-block'>Select users or groups</span>  
    						</div>
    					</div>
    				);
            		break;
        }
        
		var start = this.state.startDate.format('DD/MM/YYYY');
		var end = this.state.endDate.format('DD/MM/YYYY');
		var label = start + ' - ' + end;
		if (start === end) {
			label = start;
		}
		
  		return (
			<div className='clearfix' style={{ paddingTop: 10, marginLeft: -16 }}>
				<div className='row'>
					<div className='form-group col-md-6'>
						<label className='col-md-3 control-label' htmlFor='dimension'>Dimension</label>  
						<div className='col-md-7'>
							<Select name='dimension'
								value={this.state.dimension}
		  		            	options={options}
		  		            	onChange={onChangeDimension.bind(this)}
								clearable={true} 
							/>
							<span className='help-block'>Select the source of the labels in the x Axis</span>  
						</div>
					</div>
					{config}
				</div>
				<div className='row'>
					<div className='form-group col-md-6'>
						<label className='col-md-3 control-label' htmlFor='dimension'>Interval</label>  
						<div className='col-md-7'>
							<DateRangePicker	startDate={this.state.startDate} 
												endDate={this.state.endDate} 
												ranges={this.state.ranges} 
												onEvent={onChangeInterval.bind(this)}>
								<div className='clearfix Select-control' style={{ cursor: 'pointer', padding: '5px 10px', width: '100%'}}>
									<span>{label}</span>
								</div>
		        			</DateRangePicker>
		        			<span className='help-block'>Select data time interval</span>
	        			</div>
        			</div>
    			</div>
			</div>
 		);
  	}
});

var SeriesStep  = React.createClass({
  	render: function() {	
  		return (
			<span>SeriesStep</span>
 		);
  	}
});

var SpatialStep  = React.createClass({
  	render: function() {	
  		return (
			<span>SpatialStep</span>
 		);
  	}
});

var TimeLineStep  = React.createClass({
  	render: function() {	
  		return (
			<span>TimeLineStep</span>
 		);
  	}
});

var ChartWizard = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

  	render: function() {
		var steps = [{
			text: 'Labels',
			component: LabelStep,
			props: {}
		}, {
			text: 'Series',
			component: SeriesStep,
			props: {}
		}, {
			text: 'Spatial',
			component: SpatialStep,
			props: {}
		}, {
			text: 'Timeline',
			component: TimeLineStep,
			props: {}
		}];
		
  		return (
			<Wizard steps={steps}/>
 		);
  	}
});

module.exports = ChartWizard;
