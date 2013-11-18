package ch.unisi.inf.sp.statistic;



public final class Min implements Statistic {

	public double compute(final double[] values) {
		if (values==null || values.length==0) {
			return Double.NaN;
		}
		double min = Double.POSITIVE_INFINITY;
		for (final double value : values) {
			min = Math.min(min, value);
		}
		return min;
	}
	
}
