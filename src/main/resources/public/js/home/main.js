var daiad = {};

daiad.EnumChartType = {
	TotalDailyConsumption : 1,
	AverageDailyConsumptionPerShower : 2,
	TotalShowerPerDay : 3
};

var model = {
	budget : 10000,
	chart : daiad.EnumChartType.TotalDailyConsumption,
	interval : [],
	timer : null,
	format : {
		date : null
	},
	applicationKey : 'c11f91c3-579b-48e8-afbd-988bf517ebdc',
	devices : [ {
		name : 'Amphiro 1',
		id : 'ddc847df-c0c5-4cff-ba12-11ede01356c5',
	} ],
	meterId : 'C12FA152770',
	data : {
		shower : null,
		showers : null,
		meters : null
	},
	dimensions : {
		dailyConsumption : null,
		dailyMeasurements : null
	},
	groups : {
		dailyConsumptionGroup : null,
		dailyMeterGroup : null
	},
	charts : {
		daily : null,
		dailyChildVolume : null,
		dailyChildEnergy : null,
		dailyChildCount : null,
		deviceAndMeter : null,
		deviceAndMeter : null,
		deviceAndMeterMeasurements : null,
		deviceAndMeterConsumtpion : null,
		monthlyBudget : null
	},
	refresh : {
		home : true,
		calendar : false,
		analysis : true
	},
	suspendEvents : false,
	cost : {
		volume : 0.005,
		energy : 0.4,
		efficiency : 1.3,
		average : 0.815,
		estimateEnergy : true
	},
	environment : {
		temperature : 20
	},
	reload : {
		timeout : null,
		interval : 60000
	}

};

function getActiveTab() {
	return $('.tab-pane.active').attr('id');
}

function refreshTab(tab) {
	if ((getActiveTab() == tab) && (model.refresh[tab])) {
		switch (tab) {
		case 'home':
			refreshDashboardTab();
			break;
		case 'calendar':
			refreshDashboardTab();
			refreshCalendarTab();
			break;
		case 'analysis':
			refreshDashboardTab();
			refreshAnalysisTab();
			break;
		}

		dc.renderAll(tab);
		model.refresh[tab] = false;
	}
}

function estimateKwhForWaterHeating(volume, temperature) {
	if (temperature > model.environment.temperature) {
		return (volume * 4 * (temperature - model.environment.temperature) / 3412);
	}
	return 0;
}

function estimateShowerCost(volume, energyKwh, duration, temperature) {

	if ((duration == 0) || (volume == 0)) {
		return 0;
	}
	return ((energyKwh * model.cost.energy * model.cost.efficiency) + (volume * model.cost.volume));
}

function wattToKwh(duration, energy) {
	if (duration == 0) {
		return 0;
	}
	return (energy * duration / 3600000);
}

function kwhToWatt(duration, energyKwh) {
	if (duration == 0) {
		return 0;
	}
	return (energyKwh * 3600000 / duration);
}

function refreshDashboardTab() {
	// Show/Hide charts
	if (model.data.showers.count == 0) {
		$('#all-graphs').hide();
		$('#all-graphs-empty').show();
	} else {
		$('#all-graphs-empty').hide();
		$('#all-graphs').show();
	}

	// Render main page charts
	renderDashboardCharts();

	// Render most recent shower information
	renderMostRecentShower();
}

function refreshCalendarTab() {
	// Render shower list in calendar
	renderShowerTable();
}

function refreshAnalysisTab() {
	// Draw charts
	renderAnalysisCharts();
}

function suspendUI() {
	$('#ajax-loader-image').show();
}

function resumeUI() {
	$('#ajax-loader-image').hide()
}

function setIntervalDates(date) {
	var dateFormat = d3.time.format('%Y-%m-%d');

	var startDate = date ? date : $('div.input-group.date').datepicker(
			'getDate');

	var endDate = new Date(startDate.getFullYear(), startDate.getMonth() + 1, 0);

	model.interval = [ dateFormat(startDate) + ' 00:00:00',
			dateFormat(endDate) + ' 23:59:59' ];
}

$(function() {
	model.format.date = d3.time.format('%d/%m/%Y %H:%M');

	if (typeof logged !== 'undefined') {
		initialize();
	} else {
		$('#page-main').fadeIn(500);
	}
	$('.action-login').click(function(e) {
		e.preventDefault();
		login();
	});
});

function login() {
	var url = $('.form-login').attr('action');

	var data = {
		username : $('#username').val(),
		password : $('#password').val(),
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	$.ajax(
			{
				type : "POST",
				url : url,
				data : data,
				beforeSend : function(xhr) {
					xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]')
							.attr('content'));
				}
			}).done(
			function(data, textStatus, request) {
				if (data.connected) {
					var crsf = request.getResponseHeader('X-CSRF-TOKEN');

					$('meta[name=_csrf]').attr('content', crsf);
					$('input[name=_csrf]').val(crsf);
		
					$('#firstname').html(data.firstname);
					
					initialize();
				} else {
					$('#password').val('');

					$('#authFailedModal').modal('show')
				}
			}).fail(function(jqXHR, textStatus, errorThrown) {
		switch (jqXHR.status) {
		case 403:
			$('#password').val('');

			$('#authFailedModal').modal('show')
			break;
		}
	}).always(function() {
		resumeUI();
	});
}

function loadMostRecentMeterValue() {
	var query = {
		applicationKey : model.applicationKey,
		deviceId : model.meterId,
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	$
			.ajax(
					{
						type : "POST",
						url : 'swm/current',
						dataType : 'json',
						data : JSON.stringify(query),
						contentType : "application/json",
						beforeSend : function(xhr) {
							xhr.setRequestHeader('X-CSRF-TOKEN', $(
									'meta[name=_csrf]').attr('content'));
						}
					})
			.done(
					function(data) {
						if ((data.success) && (data.value1)) {
							var html = '<a id="smart-meter-value-link" tabindex="0" class="btn btn-lg btn-info" '
									+ 'role="button" data-html="true" data-toggle="popover" data-trigger="focus" '
									+ ' data-placement="bottom" title="Smart Water Meter" '
									+ 'data-content="<p>Reference Id: <b>'
									+ data.deviceId
									+ '</b></p><p>Last updated on: <b>'
									+ model.format.date(new Date(
											data.value1.timestamp))
									+ '</b></p>';
							if (data.value2) {
								html += '<p>Consumption increased by: <b>'
										+ (data.value1.volume - data.value2.volume)
										+ '</b></p>';
							}
							html += '" style="padding: 4px;">'
									+ data.value1.volume + '</a>';

							$('#label-meter-value').html(html);
							$('#smart-meter-value-link').popover();
						} else {
							$('#label-meter-value')
									.html(
											'<span class="glyphicon glyphicon-question-sign" style="margin-top: 9px;"></span>');
						}
					})
			.fail(
					function(jqXHR, textStatus, errorThrown) {
						$('#label-meter-value')
								.html(
										'<span class="glyphicon glyphicon-remove-circle" style="margin-top: 9px;"></span>');
					}).always(function() {
				resumeUI();
			});
}

function initialize() {
	$('.form-login').hide();

	$('#label-meter-image, #label-meter-value').show();
	$('#label-firstname').show();
	$('#logout-section').show();
	$('#action-menu').show();

	$('#budget-editor').val(model.budget);

	$('#page-main[role=page]')
			.fadeOut(
					500,
					function() {
						$('#tabs').tab();

						var today = new Date();
						$('div.input-group.date')
								.each(
										function(index, elem) {
											$(elem).datepicker({
												format : "MM yyyy",
												minViewMode : 1,
												autoclose : true,
												toggleActive : true,
												orientation : 'top auto'
											});

											$(elem)
													.datepicker(
															'setDate',
															new Date(
																	today
																			.getFullYear(),
																	today
																			.getMonth(),
																	1));

											$(elem)
													.datepicker()
													.on(
															'changeMonth',
															function(e) {
																var self = this;
																var date = e.date;

																if (model.suspendEvents) {
																	return;
																}

																model.suspendEvents = true;

																$(
																		'div.input-group.date')
																		.each(
																				function(
																						index,
																						elem) {
																					if (elem != self) {
																						$(
																								elem)
																								.datepicker(
																										'setDate',
																										date);
																					}
																				});

																setIntervalDates(date);

																model.refresh.home = true;
																model.refresh.calendar = true;
																model.refresh.analysis = true;

																// Resume events
																model.suspendEvents = false;

																refreshData();
															});
										});

						setIntervalDates(null);

						$('#budget-editor')
								.keydown(
										function(e) {
											var refreshBudget = true;

											if ($.inArray(e.keyCode, [ 38, 40,
													33, 34 ]) !== -1) {
												var value = Number($(
														'#budget-editor').val());
												switch (e.keyCode) {
												case 38:
													value += 1;
													break;
												case 40:
													value -= 1;
													break;
												case 33:
													value += 100;
													break;
												case 34:
													value -= 100;
													break;
												}
												if (value < 0) {
													$('#budget-editor').val(0);
												} else {
													$('#budget-editor').val(
															value);
												}
											} else if ($.inArray(e.keyCode, [
													46, 8, 9, 27, 13, 110, 37,
													39, 190 ]) !== -1) {
												// Special keys allowed
											} else if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57))
													&& (e.keyCode < 96 || e.keyCode > 105)) {
												e.preventDefault();
												refreshBudget = false;
											}

											if (refreshBudget) {
												clearTimeout(model.timer);
												model.timer = setTimeout(
														updateBudget, 1000);
											}

										});

						jQuery('#logout').click(function(e) {
							jQuery('#logout-form').submit();
						});

						$('#label-firstname').find('a').click(function(e) {
							e.preventDefault();
							$('#tabs a[href="#profile"]').tab('show');

						});

						$('#tabs a').click(function(e) {
							$(this).blur();
						});

						$('input.chart-interval').click(function(e) {
							$(this).blur();
						});

						$('a[data-toggle="tab"]')
								.on(
										'shown.bs.tab',
										function(e) {
											var tab = $(e.target).attr(
													'aria-controls');
											setTimeout(function() {
												refreshTab(tab);
											}, 400);
										});

						$('#page-home[role=page]').show();

						refreshData();

						model.reload.timeout = setInterval(refreshData,
								model.reload.interval);
					});

	$('.chart-selector').click(function(e) {
		e.preventDefault();
		$(this).blur();

		if ($(this).parent().hasClass('active')) {
			return;
		}
		$('.chart-selector').parent().removeClass('active');
		$(this).parent().addClass('active');
		model.chart = $(this).data('chart');

		composeDashBoardCharts();

		model.charts.daily.render();
	});
}

function composeDashBoardCharts() {
	switch (model.chart) {
	case daiad.EnumChartType.TotalDailyConsumption:
		model.charts.daily
				.compose(
						[ model.charts.dailyChildVolume,
								model.charts.dailyChildEnergy ]).yAxisLabel(
						"Volume (lt)");
		break;
	case daiad.EnumChartType.AverageDailyConsumptionPerShower:
		model.charts.daily
				.compose(
						[ model.charts.dailyChildVolume,
								model.charts.dailyChildEnergy ]).yAxisLabel(
						"Volume (lt)");
		break;
	case daiad.EnumChartType.TotalShowerPerDay:
		model.charts.daily.compose([ model.charts.dailyChildCount ])
				.yAxisLabel("Total");
		break;
	}
}

function refreshData() {
	loadMostRecentMeterValue();

	var query = {
		applicationKey : model.applicationKey,
		deviceId : model.devices[0].id,
		startDate : model.interval[0],
		endDate : model.interval[1],
		granularity : 3,
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	suspendUI();

	$.ajax(
			{
				type : "POST",
				url : 'showers',
				dataType : 'json',
				data : JSON.stringify(query),
				contentType : "application/json",
				beforeSend : function(xhr) {
					xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]')
							.attr('content'));
				}
			}).done(function(data) {
		model.refresh.home = true;
		model.refresh.calendar = true;

		// Transform shower data
		transformShowerData(data);

		// Load smart meter data
		loadSmartMeterData();
	}).fail(function(jqXHR, textStatus, errorThrown) {

	}).always(function() {
		resumeUI();
	});
}

function loadSmartMeterData() {
	var query = {
		applicationKey : model.applicationKey,
		deviceId : model.meterId,
		startDate : model.interval[0],
		endDate : model.interval[1],
		granularity : 3,
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	suspendUI();
	$.ajax(
			{
				type : "POST",
				url : 'swm/history',
				dataType : 'json',
				data : JSON.stringify(query),
				contentType : "application/json",
				beforeSend : function(xhr) {
					xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]')
							.attr('content'));
				}
			}).done(function(data) {
		model.refresh.analysis = true;

		// Transform data
		transformSmartMeterData(data);

		// Refresh tab if needed
		refreshTab(getActiveTab());

	}).fail(function(jqXHR, textStatus, errorThrown) {
		$('#label-meter-value').html('-');
	}).always(function() {
		resumeUI();
	});
}

function renderMostRecentShower() {
	if (model.data.showers.count == 0) {
		$('#shower-data').hide();
		$('#shower-not-found').show();
	} else {
		$('#shower-not-found').hide();

		var points = model.data.showers.records;

		var shower = null;
		for (var s = points.length - 1; s >= 0; s--) {
			if (points[s].volume > 0) {
				shower = points[s];
				break;
			}
		}

		var advice = computeShowerCostAndRecommend(shower.volume,
				shower.energyKwh, shower.temperature, shower.duration);

		$('#shower-date').html(model.format.date(new Date(shower.timestamp)));
		$('#shower-duration').html(
				Math.floor(shower.duration / 60).toFixed(0) + ' mins '
						+ (shower.duration % 60).toFixed(0) + ' secs');
		$('#shower-volume').html(shower.volume.toFixed(2) + ' lt');

		$('#shower-energy').html(shower.energyKwh.toFixed(2) + ' kWh');
		$('#shower-temperature').html(
				shower.temperature.toFixed(1) + ' &#8451;');
		$('#shower-cost').html(advice.cost.toFixed(2) + ' €');

		$('#shower-data').show();

		if (advice.text) {
			$('#shower-advice').find('span').removeClass(
					'label-success label-warning label-danger').html(
					advice.text);
			switch (advice.level) {
			case -1:
				$('#shower-advice').find('span').addClass('label-danger');
				break;
			case 0:
				$('#shower-advice').find('span').addClass('label-warning');
				break;
			case 1:
				$('#shower-advice').find('span').addClass('label-success');
				break;
			}
			$('#shower-advice').show();
		} else {
			$('#shower-advice').hide();
		}
	}
}

function renderShowerTable(data) {
	$('#shower-history').find('tr.shower-row').remove();

	var showers = model.data.showers.records;

	if (model.data.showers.count == 0) {
		$('#shower-table').hide();
		$('#shower-table-empty').show();
	} else {
		$('#shower-table-empty').hide();

		for (var i = showers.length - 1, count = 0; i >= count; i--) {
			var shower = showers[i];
			if (shower.volume > 0) {
				var row = [];
				row.push('<tr class="shower-row">');

				row.push('<td>' + model.format.date(new Date(shower.timestamp))
						+ '</td>');
				row.push('<td>' + Math.floor(shower.duration / 60).toFixed(0)
						+ ' mins ' + (shower.duration % 60).toFixed(0)
						+ ' secs</td>');
				row.push('<td>' + shower.volume.toFixed(2) + ' lt</td>');
				row.push('<td>' + shower.energyKwh.toFixed(2) + ' kWh</td>');
				row.push('<td>' + shower.temperature.toFixed(1)
						+ ' &#8451;</td>');
				row.push('<td>'
						+ computeShowerCostAndRecommend(shower.volume,
								shower.energyKwh, shower.temperature,
								shower.duration).cost.toFixed(2) + ' €</td>');
				row
						.push('<td><img class="shower-chart" data-id="'
								+ shower.id
								+ '" src="images/Combo Chart-25.png" style="cursor: pointer;"/></td>');
				row.push('</tr>');

				$('#shower-table').find('tbody').append(row.join(''));
			}
		}

		$('#shower-table').show();

		$('img.shower-chart').click(
				function(e) {
					suspendUI();

					var query = {
						applicationKey : model.applicationKey,
						deviceId : model.devices[0].id,
						startDate : model.interval[0],
						endDate : model.interval[1],
						granularity : 3,
						showerId : $(this).data('id'),
						_csrf : $('meta[name=_csrf]').attr('content')
					};

					$.ajax(
							{
								type : "POST",
								url : 'shower',
								dataType : 'json',
								data : JSON.stringify(query),
								contentType : "application/json",
								beforeSend : function(xhr) {
									xhr
											.setRequestHeader('X-CSRF-TOKEN',
													$('meta[name=_csrf]').attr(
															'content'));
								}
							}).done(function(data) {
						renderShowerDetailAndChart(data);
					}).fail(function(jqXHR, textStatus, errorThrown) {
						switch (jqXHR.status) {
						case 403:
							window.location.href = 'login';
							break;
						}
					}).always(function() {
						resumeUI();
					});
					$('#chartDetailsModal').modal('show');
				});
	}
}

function renderShowerDetailAndChart(data) {
	var showerCompositeChart = null;
	var showerTimeSeries = null;

	var shower = data.shower;

	transformShower(shower);

	var advice = computeShowerCostAndRecommend(shower.volume, shower.energyKwh,
			shower.temperature, shower.duration);

	$('#shower-detail-date')
			.html(model.format.date(new Date(shower.timestamp)));
	$('#shower-detail-duration').html(
			Math.floor(shower.duration / 60).toFixed(0) + ' mins '
					+ (shower.duration % 60).toFixed(0) + ' secs');
	$('#shower-detail-volume').html(shower.volume.toFixed(2) + ' lt');
	$('#shower-detail-energy').html(shower.energyKwh.toFixed(2) + ' kWh');
	$('#shower-detail-temperature').html(
			shower.temperature.toFixed(1) + ' &#8451;');
	$('#shower-detail-cost').html(advice.cost.toFixed(2) + ' €');

	$('#shower-detail-data').show();

	if (advice.text) {
		$('#shower-detail-advice').find('span').removeClass(
				'label-success label-warning label-danger').html(advice.text);
		switch (advice.level) {
		case -1:
			$('#shower-detail-advice').find('span').addClass('label-danger');
			break;
		case 0:
			$('#shower-detail-advice').find('span').addClass('label-warning');
			break;
		case 1:
			$('#shower-detail-advice').find('span').addClass('label-success');
			break;
		}
		$('#shower-detail-advice').show();
	} else {
		$('#shower-detail-advice').hide();
	}

	var redraw = false;

	// Transform data
	var points = shower.measurements;

	var offset = points[0].showerTime;
	points.forEach(function(d) {
		d.showerTime -= offset - 1;
	});

	var minShowerTime = points[0].showerTime;
	var maxShowerTime = points[points.length - 1].showerTime;

	var count = points.length
	var lastPoint = points[0];
	for (var d = minShowerTime; d <= maxShowerTime; d++) {
		var exists = false;
		for (var m = 0; m < count; m++) {
			if (points[m].showerTime == d) {
				exists = true;
				lastPoint = points[m];
				break;
			}
		}
		if (!exists) {
			lastPoint = {
				dayOfMonth : lastPoint.dayOfMonth,
				energy : 0.0,
				showerId : lastPoint.showerId,
				showerTime : d,
				temperature : 0.0,
				timestamp : lastPoint.timestamp + 1000,
				volume : 0.0,
			}
			points.push(lastPoint);
		}
	}

	if (showerTimeSeries) {
		showerTimeSeries.remove();
		showerTimeSeries.add(points);

		redraw = true;
	} else {
		showerTimeSeries = crossfilter(points);
	}

	if (!redraw) {
		var showerConsumption = showerTimeSeries.dimension(function(d) {
			return d.showerTime;
		});

		var showerVolumeGroup = showerConsumption.group().reduceSum(
				function(d) {
					return d.volume;
				});

		var showerEnergyGroup = showerConsumption.group().reduceSum(
				function(d) {
					return d.energyKwh;
				});

		showerCompositeChart = dc.compositeChart('#shower-details', 'details');

		var dateFormat = d3.time.format('%d/%m/%Y');
		var numberFormat = d3.format('.2f');

		showerCompositeChart.width(null).height(null).transitionDuration(1000)
				.elasticX(false).elasticY(true).renderHorizontalGridLines(true)
				.brushOn(false).rightYAxisLabel('Energy (kWh)').x(
						d3.scale.linear().domain(
								[ 0, maxShowerTime - minShowerTime + 2 ]))

				.yAxisLabel("Volume (lt)").legend(
						dc.legend().x(80).y(20).itemHeight(13).gap(5))
				.renderHorizontalGridLines(true).title(function(d) {
					var value = d.value;
					if (isNaN(value)) {
						value = 0;
					}
					return value;
				}).compose(
						[
								dc.barChart(showerCompositeChart).width(null)
										.height(null).transitionDuration(1000)
										.centerBar(true).gap(1).colors(
												'#3182bd').dimension(
												showerConsumption).group(
												showerVolumeGroup, 'Volume'),
								dc.lineChart(showerCompositeChart).dimension(
										showerConsumption).group(
										showerEnergyGroup, 'Energy').dashStyle(
										[ 5, 5 ]).colors('#6baed6')
										.useRightYAxis(true) ]).brushOn(false);

	} else {
		redraw = true;
	}

	$('#chartDetailsModal').modal('show');

	showerCompositeChart.resetSvg();

	setTimeout(function() {
		dc.renderAll('details');
	}, 500);
}

function computeShowerCostAndRecommend(volume, energyKwh, temperature, duration) {
	var advice = {
		cost : estimateShowerCost(volume, energyKwh, duration, temperature),
		text : null,
		level : null
	}
	if (advice.cost > model.cost.average * 1.2) {
		advice.level = -1;
		advice.text = ((advice.cost - model.cost.average) * 100 / model.cost.average)
				.toFixed(2)
				+ '% more expensive than the average';
	} else if (advice.cost <= model.cost.average) {
		advice.level = 1;
		advice.text = ((model.cost.average - advice.cost) * 100 / advice.cost)
				.toFixed(2)
				+ '% less expensive than the average';
	} else {
		advice.level = 0;
		advice.text = ((advice.cost - model.cost.average) * 100 / model.cost.average)
				.toFixed(2)
				+ '% more expensive than the average';
	}

	return advice;
}

function computeBudget(group) {
	var budgetText = $('#budget-editor').val();
	var budget = parseInt(budgetText);
	if ((!group) || (isNaN(budget)) || (budget <= 0)) {
		return [ {
			key : 'Consumed',
			value : 0
		}, {
			key : 'Saved',
			value : 100
		} ];
	}

	var totalConsumption = group.value();
	var totalSavings = budget - totalConsumption;
	var consumptionDeficit = 0;
	if (totalSavings < 0) {
		consumptionDeficit -= totalSavings;
		totalSavings = 0;
	}

	var budgetPieData = [
			{
				key : 'Consumed',
				value : (totalConsumption / budget > 1 ? 100 : totalConsumption
						/ budget * 100)
			}, {
				key : 'Saved',
				value : totalSavings / budget * 100
			} ];

	return budgetPieData;
}

function updateBudget() {
	budget = Number($('#budget-editor').val());

	model.charts.monthlyBudget.render();
}

function renderDashboardCharts() {
	var points = model.data.showers.records;
	var minDate = model.data.showers.minDate;
	var maxDate = model.data.showers.maxDate;

	// Create filter
	var consumption = crossfilter(points);

	// Daily consumption
	model.dimensions.dailyConsumption = consumption.dimension(function(d) {
		return d.dd;
	});

	model.groups.dailyConsumptionGroup = model.dimensions.dailyConsumption
			.group().reduce(function(p, v) {
				if (v.volume > 0) {
					++p.count;
					p.volumeSum += v.volume;
					p.energySum += v.energyKwh;

					if (p.count == 0) {
						p.volumeAvg = 0;
						p.energyAvg = 0;
					} else {
						p.volumeAvg = p.volumeSum / p.count;
						p.energyAvg = p.energySum / p.count;
					}
				}
				return p;
			}, function(p, v) {
				if (v.volume > 0) {
					--p.count;
					p.volumeSum -= v.volume;
					p.energySum -= v.energyKwh;

					if (p.count == 0) {
						p.volumeAvg = 0;
						p.energyAvg = 0;
					} else {
						p.volumeAvg = p.volumeSum / p.count;
						p.energyAvg = p.energySum / p.count;
					}
				}
				return p;
			}, function() {
				return {
					count : 0,
					volumeSum : 0,
					volumeAvg : 0,
					energySum : 0,
					energyAvg : 0
				};
			});

	// Average day of week consumption
	var dayOfWeek = consumption.dimension(function(d) {
		var day = d.dd.getDay();
		var name = [ 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat' ];
		return day + '.' + name[day];
	});

	var dayOfWeekGroup = dayOfWeek.group().reduce(function(p, v) {
		if (v.volume > 0) {
			++p.count;
			p.volumeSum += v.volume;
			p.volumeAvg = p.volumeSum / p.count;
		}
		return p;
	}, function(p, v) {
		if (v.volume > 0) {
			--p.count;
			p.volumeSum -= v.volume;
			p.volumeAvg = p.volumeSum / p.count;
		}
		return p;
	}, function() {
		return {
			count : 0,
			volumeSum : 0,
			volumeAvg : 0
		};
	});

	// Day of week average consumption
	var dayOfWeekChart = dc.rowChart('#day-of-week-chart', 'home');

	dayOfWeekChart.width(null).height(null).margins({
		top : 20,
		left : 10,
		right : 10,
		bottom : 20
	}).group(dayOfWeekGroup).dimension(dayOfWeek).ordinalColors(
			[ '#3182bd', '#6baed6', '#9ecae1', '#c6dbef', '#dadaeb' ]).label(
			function(d) {
				return d.key.split('.')[1];
			}).valueAccessor(function(d) {
		if (d.value.count == 0) {
			return 0;
		}
		return d.value.volumeAvg;
	}).renderTitleLabel(true).title(function(d) {
		if (d.value.count == 0) {
			return d.value.volumeAvg.toFixed(2);
		}
		return d.value.volumeAvg.toFixed(2);
	}).elasticX(true).xAxis().ticks(5);

	// Monthly budget
	var consumedResourcesGroup = model.dimensions.dailyConsumption.groupAll()
			.reduceSum(function(d) {
				return d.volume;
			});

	model.charts.monthlyBudget = dc.pieChart('#monthly-budget', 'home');

	model.charts.monthlyBudget.width(null).height(null).radius(120).dimension(
			model.dimensions.dailyConsumption).group(consumedResourcesGroup)
			.data(computeBudget).ordinalColors(
					[ '#3182bd', '#6baed6', '#9ecae1', '#c6dbef', '#dadaeb' ])

			.renderTitle(false).label(function(d) {
				return d.key + '(' + d.value.toFixed(2) + ' %)';
			});

	// Disable data filtering for monthly budget data set
	model.charts.monthlyBudget.filter = function() {
	};

	// Daily consumption
	var dateFormat = d3.time.format('%d/%m/%Y');
	var xAxisDateFormat = d3.time.format('%a %d');
	var numberFormat = d3.format('.2f');

	model.charts.daily = dc.compositeChart('#daily-chart', 'home');

	model.charts.dailyChildCount = dc.barChart(model.charts.daily, 'home')
			.width(null).height(null).transitionDuration(1000).centerBar(true)
			.gap(1).colors('#3182bd').dimension(
					model.dimensions.dailyConsumption).group(
					model.groups.dailyConsumptionGroup, 'Total').valueAccessor(
					function(d) {
						return d.value.count;
					});

	model.charts.dailyChildVolume = dc.barChart(model.charts.daily, 'home')
			.width(null).height(null).transitionDuration(1000).centerBar(true)
			.gap(1).colors('#3182bd').dimension(
					model.dimensions.dailyConsumption).group(
					model.groups.dailyConsumptionGroup, 'Water')
			.valueAccessor(function(d) {
				switch (model.chart) {
				case daiad.EnumChartType.TotalDailyConsumption:
					return d.value.volumeSum;
					break;
				case daiad.EnumChartType.AverageDailyConsumptionPerShower:
					return d.value.volumeAvg;
					break;
				}
				return 0;
			});

	model.charts.dailyChildEnergy = dc.lineChart(model.charts.daily, 'home')
			.dimension(model.dimensions.dailyConsumption).group(
					model.groups.dailyConsumptionGroup, 'Energy').dashStyle(
					[ 5, 5 ]).colors('#6baed6').useRightYAxis(true)
			.valueAccessor(function(d) {
				switch (model.chart) {
				case daiad.EnumChartType.TotalDailyConsumption:
					return d.value.energySum;
					break;
				case daiad.EnumChartType.AverageDailyConsumptionPerShower:
					return d.value.energyAvg;
					break;
				}
				return 0;
			});

	model.charts.daily
			.width(null)
			.height(null)
			.transitionDuration(1000)
			.xUnits(d3.time.days)
			.renderHorizontalGridLines(true)
			.rightYAxisLabel('Energy (kWh)')
			.x(d3.time.scale().domain([ minDate, maxDate ]))
			.yAxisLabel("Volume (lt)")
			.legend(dc.legend().x(80).y(20).itemHeight(13).gap(5))
			.renderHorizontalGridLines(true)
			.title(
					function(d) {
						switch (model.chart) {
						case daiad.EnumChartType.TotalDailyConsumption:
							return dateFormat(d.key) + '\nTotal Volume: '
									+ numberFormat(d.value.volumeSum)
									+ '\nTotal Energy: '
									+ numberFormat(d.value.energySum);

							break;
						case daiad.EnumChartType.AverageDailyConsumptionPerShower:
							return dateFormat(d.key) + '\nAverage Volume: '
									+ numberFormat(d.value.volumeAvg)
									+ '\nAverage Energy: '
									+ numberFormat(d.value.energyAvg);

							break;
						case daiad.EnumChartType.TotalShowerPerDay:
							return dateFormat(d.key) + '\nTotal Showers: '
									+ d.value.count
							break;
						}
					}).brushOn(false).elasticY(true).elasticX(true);

	model.charts.daily.xAxis().tickFormat(function(v) {
		return xAxisDateFormat(v);
	});

	composeDashBoardCharts();
}

function renderAnalysisCharts() {
	var points = model.data.meters;

	var dateFormat = d3.time.format('%d/%m/%Y');
	var xAxisDateFormat = d3.time.format('%a %d');
	var numberFormat = d3.format('.2f');

	var measurements = crossfilter(points);

	model.dimensions.dailyMeasurements = measurements.dimension(function(d) {
		return d.date;
	});

	model.groups.dailyMeterGroup = model.dimensions.dailyMeasurements.group()
			.reduceSum(function(d) {
				return d.volume;
			});

	model.charts.deviceAndMeter = dc.compositeChart('#swm-shower-combined',
			'analysis');

	model.charts.deviceAndMeterMeasurement = dc.lineChart(
			model.charts.deviceAndMeter, 'analysis').width(null).renderArea(
			true).height(null).dimension(model.dimensions.dailyMeasurements)
			.group(model.groups.dailyMeterGroup, model.meterId).valueAccessor(
					function(d) {
						return d.value;
					}).title(function(d) {
				return dateFormat(d.key) + '\n' + numberFormat(d.value);
			});

	model.charts.deviceAndMeterConsumtpion = dc.lineChart(
			model.charts.deviceAndMeter, 'analysis').width(null).renderArea(
			true).height(null).dimension(model.dimensions.dailyConsumption)
			.group(model.groups.dailyConsumptionGroup, model.devices[0].name)
			.valueAccessor(function(d) {
				return d.value.volumeAvg * d.value.count
			}).title(
					function(d) {
						return dateFormat(d.key)
								+ '\n'
								+ numberFormat(d.value.volumeAvg
										* d.value.count);
					});

	minDate = new Date(points[0].date.getFullYear(), points[0].date.getMonth(),
			1);
	maxDate = new Date(points[points.length - 1].date.getFullYear(),
			points[points.length - 1].date.getMonth() + 1, 0);

	model.charts.deviceAndMeter.width(null).height(null).transitionDuration(
			1000).xUnits(d3.time.days).elasticY(true).ordinalColors(
			[ '#3182bd', '#6baed6', '#9ecae1', '#c6dbef', '#dadaeb' ])
			.shareColors(true).renderHorizontalGridLines(true).brushOn(false)
			.x(d3.time.scale().domain([ minDate, maxDate ])).legend(
					dc.legend().x(80).y(20).itemHeight(13).gap(5))
			.renderHorizontalGridLines(true).shareTitle(false).brushOn(false);

	model.charts.deviceAndMeter.xAxis().tickFormat(function(v) {
		return xAxisDateFormat(v);
	});

	model.charts.deviceAndMeter.compose([
			model.charts.deviceAndMeterConsumtpion,
			model.charts.deviceAndMeterMeasurement ]);
}

function getConsumption(done, fail, always) {
	var query = {
		applicationKey : model.applicationKey,
		deviceId : model.devices[0].id,
		startDate : model.interval[0],
		endDate : model.interval[1],
		granularity : 3,
		_csrf : $('meta[name=_csrf]').attr('content')
	};

	suspendUI();

	var options = {
		type : "POST",
		url : 'query',
		dataType : 'json',
		data : JSON.stringify(query),
		contentType : "application/json",
		beforeSend : function(xhr) {
			xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
					'content'));
		}
	};

	prepareOptions(options);

	$.ajax(options).done(function(data) {
		if (typeof done === 'function') {
			done();
		}
	}).fail(function(jqXHR, textStatus, errorThrown) {
		if (typeof fail === 'function') {
			fail();
		}
	}).always(function() {
		if (typeof always === 'function') {
			always();
		}
		resumeUI();
	});
}

function prepareAjaxRequest(options) {
	options.beforeSend = function(xhr) {
		xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr(
				'content'));
	};
}

function transformShower(d) {
	d.dt = new Date(d.timestamp);
	d.dd = new Date(d.dt.getFullYear(), d.dt.getMonth(), d.dt.getDate());

	if (model.cost.estimateEnergy) {
		d.energyKwh = estimateKwhForWaterHeating(d.volume, d.temperature);
		d.energy = kwhToWatt(d.duration, d.energyKwh);
	} else {
		d.energyKwh = wattToKwh(d.duration, d.energy);
	}

	d.cost = estimateShowerCost(d.volume, d.energyKwh, d.duration,
			d.temperature);
}

function transformShowerData(data) {
	var records = data.showers || [];
	var count = records.length;

	records.forEach(function(d) {
		transformShower(d);
	});

	var now = new Date();
	var minDate = records.length > 0 ? new Date(records[0].dd.getFullYear(),
			records[0].dd.getMonth(), 1) : new Date(now.getFullYear(), now
			.getMonth(), 1);

	var maxDate = records.length > 0 ? new Date(records[records.length - 1].dd
			.getFullYear(), records[records.length - 1].dd.getMonth() + 1, 0)
			: new Date(now.getFullYear(), now.getMonth() + 1, 0);

	var minDay = minDate.getDate();
	var maxDay = maxDate.getDate();

	var count = records.length
	for (var d = minDay; d <= maxDay; d++) {
		var exists = false;
		for (var m = 0; m < count; m++) {
			if (records[m].dayOfMonth == d) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			var date = new Date(minDate.getFullYear(), minDate.getMonth(), d);
			records.push({
				id : 0,
				timestamp : date.getTime(),
				volume : 0.0,
				energy : 0.0,
				energyKwh : 0.0,
				temperature : 0.0,
				duration : 0,
				dd : date,
				date : model.format.date(date),
				dayOfMonth : d,
				cost : 0
			});
		}
	}

	model.data.showers = {
		count : count,
		records : records,
		minDate : minDate,
		maxDate : maxDate
	};
}

function transformSmartMeterData(data) {
	var points = data.values;
	var count = data.values.length;

	points.forEach(function(d) {
		d.date = new Date(d.timestamp);
	});

	for (var i = points.length - 1; i > 0; i--) {
		points[i].volume -= points[i - 1].volume;
	}
	if ((count > 0) && (data.reference)) {
		points[0].volume -= data.reference.volume;
	}

	var now = new Date();

	var minDate = points.length > 0 ? new Date(points[0].date.getFullYear(),
			points[0].date.getMonth(), 1) : new Date(now.getFullYear(), now
			.getMonth(), 1);

	var maxDate = points.length > 0 ? new Date(points[points.length - 1].date
			.getFullYear(), points[points.length - 1].date.getMonth() + 1, 0)
			: new Date(now.getFullYear(), now.getMonth() + 1, 0);

	var minDay = minDate.getDate();
	var maxDay = maxDate.getDate();

	for (var d = minDay; d <= maxDay; d++) {
		var exists = false;
		for (var m = 0; m < count; m++) {
			if (points[m].dayOfMonth == d) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			var date = new Date(minDate.getFullYear(), minDate.getMonth(), d);
			points.push({
				volume : 0.0,
				date : date,
				timestamp : date.getTime(),
				dayOfMonth : d
			});
		}
	}
	points.sort(function compare(a, b) {
		if (a.dayOfMonth < b.dayOfMonth)
			return -1;
		if (a.dayOfMonth > b.dayOfMonth)
			return 1;
		return 0;
	});

	if ((count > 0) && (!data.reference)) {
		points[0].volume = 0;
	}
	model.data.meters = points;
}