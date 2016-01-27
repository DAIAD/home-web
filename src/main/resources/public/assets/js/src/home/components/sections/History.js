var React = require('react');

var Chart = require('../Chart');

var MainSection = require('../MainSection.react');


//var parseDate = d3.time.format("%YM%m").parse;

var History = React.createClass({
	render: function() {
		var sampling = [];
		var average = [];
		for (var i = 0; i < 200; i++) {
			sampling.push([i, Math.random() * i * 4]);
			average.push([i, 200]);
		}
		return (
			<section className="section-history">
				<MainSection title="History" >
					<Chart
						{...{
							width: 700,
							height: 400,
							title: {
								text: 'Hello',
								subtext: 'world'
							},
							series: [
								{
									name:	'consumption',
									type: 'line',
									data: sampling,
									markLine: {
										data: [
											{type: 'min', name:'Min'},
											{type: 'max', name:'Max'}
										]
									}
								}
							],
						}
						}

					/>
				</MainSection>
			</section>
		);
	}
});

module.exports = History;
