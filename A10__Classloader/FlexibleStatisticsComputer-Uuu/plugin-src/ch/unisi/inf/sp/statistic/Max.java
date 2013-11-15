package ch.unisi.inf.sp.statistic;


public final class Max implements Statistic {

	public double compute(final double[] values) {
		if (values==null || values.length==0) {
			return Double.NaN;
		}
		double max = Double.NEGATIVE_INFINITY;
		for (final double value : values) {
			max = Math.max(max, value);
		}
		return max;
	}
	
}
