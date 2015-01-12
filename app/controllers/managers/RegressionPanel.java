package controllers.managers;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.function.PowerFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;

/**
 * This classed is based on the following class:
 * "http://test-chart.googlecode.com/svn/trunk/src/main/java/es/efor/plandifor/demo/RegressionDemo1.java"
 * (02.07.2014, 18:29). The methods were taken a modified at some points.
 * 
 * @author Christian Olenberger
 * 
 */
public class RegressionPanel {

	private XYDataset data;

	/**
	 * This method creates a JTabbedPane with two regression models (linear,
	 * power).
	 * 
	 * @param dataset
	 *            The dataset that should be visualized.
	 * @return The JTabbedPane with the two regression models.
	 */
	private JTabbedPane createContent(XYDataset dataset, String yAxisName) {
		JTabbedPane jtabbedpane = new JTabbedPane();
		jtabbedpane.add("Linear", createChartPanel1(dataset,yAxisName));
		jtabbedpane.add("Power", createChartPanel2(dataset,yAxisName));
		return jtabbedpane;
	}

	/**
	 * This method creates a JPanel containing a chart with the data and linear
	 * regression.
	 * 
	 * @param dataset
	 *            The dataset you want to visualize.
	 * @return A JPanel containing the data and the linear regression line.
	 */
	private ChartPanel createChartPanel1(XYDataset dataset, String yAxisName) {
		NumberAxis numberaxis = new NumberAxis("Index");
		numberaxis.setAutoRangeIncludesZero(false);
		NumberAxis numberaxis1 = new NumberAxis(yAxisName);
		numberaxis1.setAutoRangeIncludesZero(false);
		XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer(
				false, true);
		XYPlot xyplot = new XYPlot(data, numberaxis, numberaxis1,
				xylineandshaperenderer);
		double ad[] = Regression.getOLSRegression(data, 0);
		LineFunction2D linefunction2d = new LineFunction2D(ad[0], ad[1]);
		XYDataset xydataset = DatasetUtilities.sampleFunction2D(linefunction2d,
				1D, dataset.getItemCount(0), dataset.getItemCount(0),
				"Fitted Regression Line");
		xyplot.setDataset(0, xydataset);
		xyplot.setDataset(1, data);
		XYLineAndShapeRenderer xylineandshaperenderer1 = new XYLineAndShapeRenderer(
				true, false);
		xylineandshaperenderer1.setSeriesPaint(0, Color.blue);
		xyplot.setRenderer(0, xylineandshaperenderer1);
		xyplot.setRenderer(1, xylineandshaperenderer);
		JFreeChart jfreechart = new JFreeChart("Linear Regression",
				JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
		ChartPanel chartpanel = new ChartPanel(jfreechart, false);
		return chartpanel;
	}

	/**
	 * This method creates a JPanel containing a chart with the data and power
	 * regression.
	 * 
	 * @param dataset
	 *            The dataset you want to visualize.
	 * @return A JPanel containing the data and the power regression line.
	 */
	private ChartPanel createChartPanel2(XYDataset dataset, String yAxisValue) {
		NumberAxis numberaxis = new NumberAxis("Index");
		numberaxis.setAutoRangeIncludesZero(false);
		NumberAxis numberaxis1 = new NumberAxis(yAxisValue);
		numberaxis1.setAutoRangeIncludesZero(false);
		XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer(
				false, true);
		XYPlot xyplot = new XYPlot(data, numberaxis, numberaxis1,
				xylineandshaperenderer);
		double ad[] = Regression.getPowerRegression(data, 0);
		PowerFunction2D powerfunction2d = new PowerFunction2D(ad[0], ad[1]);
		XYDataset xydataset = DatasetUtilities.sampleFunction2D(
				powerfunction2d, 1D, dataset.getItemCount(0),
				dataset.getItemCount(0), "Fitted Regression Line");
		XYLineAndShapeRenderer xylineandshaperenderer1 = new XYLineAndShapeRenderer(
				true, false);
		xylineandshaperenderer1.setSeriesPaint(0, Color.blue);
		xyplot.setDataset(0, xydataset);
		xyplot.setDataset(1, data);
		xyplot.setRenderer(0, xylineandshaperenderer1);
		xyplot.setRenderer(1, xylineandshaperenderer);
		JFreeChart jfreechart = new JFreeChart("Power Regression",
				JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
		ChartPanel chartpanel = new ChartPanel(jfreechart, false);
		return chartpanel;
	}

	/**
	 * This method creates a JPanel with two regression models (linear, power).
	 * 
	 * @param dataset
	 *            The dataset that should be visualized.
	 * @return The JPanel with the two regression models.
	 */
	public JPanel getRegressionPanel(XYDataset dataset, String yAxisName) {
		JPanel panel = new JPanel(new BorderLayout());
		data = dataset;
		panel.add(createContent(dataset,yAxisName));
		return panel;
	}
}