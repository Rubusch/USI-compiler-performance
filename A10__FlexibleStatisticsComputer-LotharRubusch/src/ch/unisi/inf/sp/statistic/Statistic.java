package ch.unisi.inf.sp.statistic;



public interface Statistic {

	/**
	 * Compute a statistic over the given sample.
	 * 
	 * @param values the array of values in the sample
	 * @return the value of the statistic, Double.NaN in case the statistic cannot be computed over the given sample
	 */
	public double compute(double[] values);
	
}
