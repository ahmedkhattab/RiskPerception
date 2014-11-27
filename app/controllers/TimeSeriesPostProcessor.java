package controllers;

import javax.swing.JOptionPane;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

/**
 * This class was created to edit the data after they were converteed into a
 * timeSeries. It allows to convert the timeSeries or calculate the moving
 * average.
 * 
 * @author Christian Olenberger
 * 
 */
public class TimeSeriesPostProcessor {

	/**
	 * This method uses a TimeSeries and creates a XYSeries where the date is
	 * the index. The TimeSeries is sorted by date so the XYSeries is sorted by
	 * date too.
	 * 
	 * @param timeSeries
	 *            The TimeSeries you want to convert.
	 * @param yAxisValue
	 *            Decide what to do with the data(e.g.
	 *            EmotionMeasurementManager.Y_AXIS_INSTANCE_COUNTER).
	 * @param yAxisName
	 *            The name for the yAxis
	 * @return the XYSeries which is sorted by time.
	 */
	public static XYSeries convertTimeSeriesToIndex(TimeSeries timeSeries,
			int yAxisValue, String yAxisName) {
		XYSeries xySeries = new XYSeries(yAxisName);
		for (int i = 0; i < timeSeries.getItemCount(); i++) {
			XYDataItem d = new XYDataItem(i + 1, timeSeries.getDataItem(i)
					.getValue());
			xySeries.add(d);
		}
		return xySeries;
	}

	/**
	 * This method uses a moving average to recalculate the data.
	 * 
	 * @param timeSeries
	 *            The data you want to recalculate.
	 * @param movAvgRangSizes
	 *            The range for the moving average.
	 * @return The recalculated data.
	 */
	public static TimeSeries movAvgOnTimeSeries(TimeSeries timeSeries,
			int movAvgRangSizes) {
		int rangeSize = movAvgRangSizes;
		if (movAvgRangSizes > timeSeries.getItemCount()) {
			rangeSize = timeSeries.getItemCount();
			// Used code example:
			// http://java-tutorial.org/joptionpane.html
			JOptionPane.showMessageDialog(null, "Range to big. Set number: "
					+ rangeSize);
		}
		int down = rangeSize / 2;
		int up = down;
		if (down * 2 == rangeSize) {
			up--;
		}
		TimeSeries movAvgTimeSeries = calculateMovAvg(timeSeries, down, up,
				rangeSize);
		return movAvgTimeSeries;
	}

	/**
	 * This method uses a moving average to recalculate the data.
	 * 
	 * @param timeSeries
	 *            The data you want to recalculate.
	 * @param up
	 *            How much items on the left side will be used to calculate.
	 * @param down
	 *            How much items on the right side will be used to calculate.
	 * @param movAvgRangSizes
	 *            The range for the moving average.
	 * @return The recalculated data.
	 */
	private static TimeSeries calculateMovAvg(TimeSeries timeSeries, int down,
			int up, int movAvgRangSizes) {
		TimeSeries movAvgTimeSeries = new TimeSeries(timeSeries.getKey());
		int rangeSize = movAvgRangSizes;
		for (int i = 0; i < timeSeries.getItemCount(); i++) {
			TimeSeriesDataItem currItem = timeSeries.getDataItem(i);
			RegularTimePeriod time = currItem.getPeriod();
			double itemValue = 0;
			if (i < down) {
				// Not enough data on the left side of the item
				itemValue = calculateForNotEnoughOnLeft(timeSeries, i, up);
			} else if (i > timeSeries.getItemCount() - 1 - up) {
				// Enough data on both sides
				itemValue = calculateForBoth(timeSeries, i, down);
			} else {
				// Not enough data on the right side of the item.
				itemValue = calculateForNotEnoughOnRight(timeSeries, i, down,
						rangeSize);
			}
			itemValue = (Math.round(itemValue * 100)) * 1.0 / 100;
			movAvgTimeSeries.add(new TimeSeriesDataItem(time, itemValue));
		}
		return movAvgTimeSeries;
	}

	/**
	 * Calculate the value for the item if there is not enough data on the left
	 * size for the given range.
	 * 
	 * @param timeSeries
	 *            The whole timeSeries.
	 * @param itemIndex
	 *            Index of the item you want to calculate the value for.
	 * @param up
	 *            How much items on the left side will be used to calculate.
	 * @return The average value for the item.
	 */
	private static double calculateForNotEnoughOnLeft(TimeSeries timeSeries,
			int itemIndex, int up) {
		double value = 0;
		for (int c = 0; c <= (itemIndex + up); c++) {
			value = value + timeSeries.getDataItem(c).getValue().doubleValue();
		}
		return value / (itemIndex + up + 1);
	}

	/**
	 * Calculate the value for the item for the given range.
	 * 
	 * @param timeSeries
	 *            The whole timeSeries.
	 * @param itemIndex
	 *            Index of the item you want to calculate the value for.
	 * @param down
	 *            How much items on the right side will be used to calculate.
	 * @return The average value for the item.
	 */
	private static double calculateForBoth(TimeSeries timeSeries,
			int itemIndex, int down) {
		double value = 0;
		for (int c = itemIndex - down; c < timeSeries.getItemCount(); c++) {
			value = value + timeSeries.getDataItem(c).getValue().doubleValue();
		}
		return value / (timeSeries.getItemCount() - itemIndex + down);
	}

	/**
	 * Calculate the value for the item if there is not enough data on the right
	 * size for the given range.
	 * 
	 * @param timeSeries
	 *            The whole timeSeries.
	 * @param itemIndex
	 *            Index of the item you want to calculate the value for.
	 * @param down
	 *            How much items on the left side will be used to calculate.
	 * @param rangeSize
	 *            The size of the range for the moving average process.
	 * @return The average value for the item.
	 */
	private static double calculateForNotEnoughOnRight(TimeSeries timeSeries,
			int itemIndex, int down, int rangeSize) {
		double value = 0;
		for (int c = 0; c < rangeSize; c++) {
			value = value
					+ timeSeries.getDataItem(itemIndex - down + c).getValue()
							.doubleValue();
		}
		return value / rangeSize;
	}

}
