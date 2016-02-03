module.exports = {
	title: {
		text: 'Hello',
		subtext: 'world',
	},
	tooltip: {
		show: true
	},
	toolbox: {
		show: true,
		showTitle: true,
		feature: {
			mark: { 
				show: true
			},
			dataZoom: {
				show: true,
				title: {
					dataZoom: 'Range Zoom',
					dataZoomReset: 'Undo Zoom'
				},
			},           
			magicType: {
				show: true,
				type: ['line', 'bar'],
			},
			dataView: {
          show: true,
          title: 'Data View',
          lang: ['Data View', 'Close','Refresh']
        },			
			saveAsImage: {
				show: true,
				title: 'Save as Image',
				lang:['Click to Save']
			}
		}
	},
	dataZoom: {
		show: true,
		realtime: true,
		height: 40,
		y: 20,
		start: 0,
		end: 100
	},
	xAxis: [
		{
			name: 'x',
			type: 'value'
		}
	],
	yAxis: [
		{
			name: 'y',
			type: 'value',
		}
	],
	grid: {
		borderColor: '#eee',
		x: 80,
		y: 60,
		x2: 120,
		y2: 60
	},
	textStyle: {
			fontFamily: 'Helvetica Neue‘, Arial, Verdana, sans-serif'
	},
	animation: true,
	addDataAnimation: true,
	animationThreshold: 250,
	loadingEffect: 'spin',
	loadingText: 'Loading data...',
	noDataEffect: 'spin',
	noDataText: 'Waiting for data',

};
