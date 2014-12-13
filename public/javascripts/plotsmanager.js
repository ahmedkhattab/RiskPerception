function getDistributionBarOptions() {
	var options = {
	        chart: {
	            type: 'column'
	        },
	        title: {
	            text: 'Emotion Values Distribution'
	        },
	        xAxis: {
	            categories: [
	                '[1,2]',
	                '[2,3]',
	                '[3,4]',
	                '[4,5]',
	                '[5,6]',
	                '[6,7]',
	                '[7,8]',
	                '[8,-]']
	        },
	        tooltip: {
	            formatter: function () {
	                return '<b>' + this.series.name + '</b><br/>' +
	                    this.x + ': ' + this.y;
	            }
	        },
	        yAxis: {
	            min: 0,
	            title: {
	                text: 'Count of instances'
	            }
	        },

	        plotOptions: {
	            column: {
	                pointPadding: 0.2,
	                borderWidth: 0
	            }
	        },
	        series: [{
	            name: 'Ranges'
	        }]
	    };
	return options;
}
function getMeasurablePieOtions() {
	var options = {
		chart : {
			plotBackgroundColor : null,
			plotBorderWidth : 1,// null,
			plotShadow : false
		},
		title : {
			text : 'Measurable Vs. Not Measruable'
		},
		tooltip : {
			pointFormat : '{series.name}: <b>{point.percentage:.1f}%</b>'
		},
		plotOptions : {
			pie : {
				allowPointSelect : true,
				cursor : 'pointer',
				dataLabels : {
					enabled : true,
					format : '<b>{point.name}</b>: {point.percentage:.1f} %',
					style : {
						color : (Highcharts.theme && Highcharts.theme.contrastTextColor)
								|| 'black'
					}
				}
			}
		},
		series : [ {
			type : 'pie',
			name : 'Measurable Vs. Not Measruable'
		} ]
	};
	return options;
}

function getValuesRegressionOptions(type) {

	var options = {
		chart : {
			type : 'scatter',
			zoomType : 'xy'
		},
		title : {
			text : 'Linear Regression'
		},
		loading : {
			hideDuration : 1000,
			showDuration : 1000,
			labelStyle : {
				color : 'white',
				padding : '55px 0 0 0',
				background : 'url("assets/stylesheets/ajax-loader.gif") no-repeat scroll 11px 14px transparent',
				left : '-2%',
				color : 'black'

			}
		},
		xAxis : {
			title : {
				enabled : true,
				text : 'Index'
			},
			startOnTick : true,
			endOnTick : true,
			showLastLabel : true
		},
		yAxis : {
			title : {
				text : 'Emotion Value'
			}
		},
		plotOptions : {
			scatter : {
				marker : {
					radius : 5,
					states : {
						hover : {
							enabled : true,
							lineColor : 'rgb(100,100,100)'
						}
					}
				},
				states : {
					hover : {
						marker : {
							enabled : false
						}
					}
				}
			}
		},
		series : [ {
			regression : true,
			regressionSettings : {
				type : type,
				color : 'rgba(9, 122, 220, 0.9)'
			},
			name : 'Emotion Value',
			color : 'rgba(223, 83, 83, .5)'
		} ]
	};
	return options;
}
function getTimeSeriesOptions() {
	var options = {
		title : {
			text : 'Emotion Values Timeseries'
		},
		loading : {
			hideDuration : 1000,
			showDuration : 1000,
			labelStyle : {
				color : 'white',
				padding : '55px 0 0 0',
				background : 'url("assets/stylesheets/ajax-loader.gif") no-repeat scroll 11px 14px transparent',
				left : '-2%',
				color : 'black'

			}
		},

		subtitle : {
			text : document.ontouchstart === undefined ? 'Click and drag in the plot area to zoom in'
					: 'Pinch the chart to zoom in'
		},
		chart : {
			zoomType : 'x'
		},
		plotOptions : {
			area : {
				fillColor : {
					linearGradient : {
						x1 : 0,
						y1 : 0,
						x2 : 0,
						y2 : 1
					},
					stops : [
							[ 0, Highcharts.getOptions().colors[0] ],
							[1,
								Highcharts.Color(
										Highcharts.getOptions().colors[0])
										.setOpacity(0).get('rgba') ] ]
				},
				marker : {
					radius : 2
				},
				lineWidth : 1,
				states : {
					hover : {
						lineWidth : 1
					}
				},
				threshold : null
			}
		},
		xAxis : {
			type : 'datetime',
			minRange : 14 * 24 * 3600000
		},

		series : [ {
			type : 'area',
			name : 'Emotion measurement'
		} ]
	};
	return options;
}
function showTimeSeriesChart(data) {

	var options = getTimeSeriesOptions();
	if (data != null)
		options.series[0].data = data.data;
	else
		options.series[0].data = [];
	$('#container').highcharts(options);
}
function showMovingAverageChart(data) {

	var options = getTimeSeriesOptions();
	options.title.text = "Moving Average";
	if (data != null)
		options.series[0].data = data.data;
	else
		options.series[0].data = [];
	$('#container').highcharts(options);
}
function showValuesRegressionChart(data, type) {
	var options = getValuesRegressionOptions(type);
	if (data != null)
		options.series[0].data = data.data;
	else
		options.series[0].data = [];
	$('#container').highcharts(options);
}
function showMeasurablePieChart(data) {
	var options = getMeasurablePieOtions();
	if (data != null)
		options.series[0].data = data.data;
	else
		options.series[0].data = [];
	$('#container').highcharts(options);
}
showDistributionBarChart
function showDistributionBarChart(data) {
	var options = getDistributionBarOptions();
	if (data != null)
		options.series[0].data = data.data;
	else
		options.series[0].data = [];
	$('#container').highcharts(options);
}