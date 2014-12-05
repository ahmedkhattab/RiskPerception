function getValuesRegressionOptions(){
	
	var options = {
        chart: {
            type: 'scatter',
            zoomType: 'xy'
        },
        title: {
            text: 'Linear Regression'
        },
        xAxis: {
            title: {
                enabled: true,
                text: 'Index'
            },
            startOnTick: true,
            endOnTick: true,
            showLastLabel: true
        },
        yAxis: {
            title: {
                text: 'Emotion Value'
            }
        },
        legend: {
            layout: 'vertical',
            align: 'left',
            verticalAlign: 'top',
            x: 100,
            y: 70,
            floating: true,
            backgroundColor: '#FFFFFF',
            borderWidth: 1
        },
        plotOptions: {
            scatter: {
                marker: {
                    radius: 5,
                    states: {
                        hover: {
                            enabled: true,
                            lineColor: 'rgb(100,100,100)'
                        }
                    }
                },
                states: {
                    hover: {
                        marker: {
                            enabled: false
                        }
                    }
                }
            }
        },
        series: [{
            regression: true ,
            regressionSettings: {
                type: 'linear',
                color:  'rgba(223, 83, 83, .9)'
            },
            name: 'Female',
            color: 'rgba(223, 83, 83, .5)'
    };
    return options;
}
function getTimeSeriesOptions(){
	var options = {
			 title: {
		            text: 'Emotion Values Timeseries'
		        },
		        loading: {
		            hideDuration: 1000,
		            showDuration: 1000,
		            labelStyle: {
		                color: 'white',
		                padding: '55px 0 0 0',
		                background: 'url("assets/stylesheets/ajax-loader.gif") no-repeat scroll 11px 14px transparent',
		                left: '-2%',
		            	color: 'black'
		           
		            }
		        },

		        subtitle: {
		            text: document.ontouchstart === undefined ?
		                    'Click and drag in the plot area to zoom in' :
		                    'Pinch the chart to zoom in'
		        },
		        chart: {
		            zoomType: 'x'
		        },
			plotOptions: {
	            area: {
	                fillColor: {
	                    linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1},
	                    stops: [
	                        [0, Highcharts.getOptions().colors[0]],
	                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
	                    ]
	                },
	                marker: {
	                    radius: 2
	                },
	                lineWidth: 1,
	                states: {
	                    hover: {
	                        lineWidth: 1
	                    }
	                },
	                threshold: null
	            }
	        },
	        xAxis: {
	            type: 'datetime',
	            minRange: 14 * 24 * 3600000 
	        },
	        
	        series: [{ type: 'area',  name: 'Emotion measurement'}]
	    }
	return options;
}
function showTimeSeriesChart(data){
	
	var options = getTimeSeriesOptions();
	if(data != null)
		options.series[0].data = data.data;
	else
		options.series[0].data = [];
	 	$('#container').highcharts(options);
}

function showValuesRegressionChart(data){
	var options = getValuesRegressionOptions();
	if(data != null)
		options.series[0].data = data.data;
	else
		options.series[0].data = [];
	 	$('#container').highcharts(options);
}