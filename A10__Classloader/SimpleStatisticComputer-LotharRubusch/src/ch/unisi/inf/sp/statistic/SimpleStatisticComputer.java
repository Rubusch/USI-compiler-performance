package ch.unisi.inf.sp.statistic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public final class SimpleStatisticComputer {

	public static void main(final String[] args) {
		final String fileName = args[0];
		final String statisticName = args[1];
		final Statistic statistic = createStatistic(statisticName);
		try {
			final double[] sample = loadSample(fileName);
			final double value = statistic.compute(sample);
			System.out.println(value);
		} catch (final IOException ex) {
			System.err.println("Could not load sample from file '"+fileName+"': "+ex);
		} catch (final NumberFormatException ex) {
			System.err.println("File '"+fileName+"' contains a line that is not a double: "+ex.getMessage());
		}
	}
	
	private static Statistic createStatistic(final String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		final String packageName = "ch.unisi.inf.sp.statistic";

		// Load class (with Class.forName())
		Class cl = Class.forName(name);
		// TODO

		// Instantiate an object of the class
// TODO do I need to catch exps and re-throw them here???
		Object obj = cl.newInstance();
		
		Statistic statistic = (Statistic) obj;
		// TODO
		
//		cl.getMethod(Max.compute(), parameterTypes) // TODO check how to come to "statistic" from the loaded class...

		return statistic;
	}

	private static double[] loadSample(final String fileName) throws IOException, NumberFormatException {
		final ArrayList<String> lines = new ArrayList<String>();
		final BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line=br.readLine())!=null) {
			lines.add(line);
		}
		br.close();
		final double[] sample = new double[lines.size()];
		for (int i=0; i<sample.length; i++) {
			sample[i] = Double.parseDouble(lines.get(i));
		}
		return sample;
	}
	
}
