package ch.unisi.inf.sp.statistic;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;


public final class FlexibleStatisticComputer {

	public static void main(final String[] args) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		final String fileName = args[0];
		final String statisticName = args[1];
		final Statistic statistic = createStatistic(statisticName);
		try {
			final double[] sample = loadSample(fileName);
			final double value = statistic.compute(sample);
			System.out.println(value);
			if (statistic.getClass().getClassLoader() == FlexibleStatisticComputer.class.getClassLoader()) {
				// NOTE: If your program goes here,
				// you either made a mistake in your createStatistic() function,
				// or you still may have to edit the "Classpath" in your launch configuration
				// (remove things, and manually add the "bin" folder, but NOT the "plugin-bin" folder)
				// to make sure that the application class loader does NOT find the plugin classes.
				// Reaching here means that the class loaded by createStatistic() cannot be unloaded anymore
				// because it was loaded by the application class loader.
				System.err.println("ERROR");
				System.err.println("  Plugin class was not loaded by a separate class loader!");
				System.err.println("  Plugin class was loaded by:      "+statistic.getClass().getClassLoader());
				System.err.println("  Application class was loaded by: "+FlexibleStatisticComputer.class.getClassLoader());
				throw new Error();
			}
		} catch (final IOException ex) {
			System.err.println("Could not load sample from file '"+fileName+"': "+ex);
		} catch (final NumberFormatException ex) {
			System.err.println("File '"+fileName+"' contains a line that is not a double: "+ex.getMessage());
		}
	}
	
	private static Statistic createStatistic(final String name) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		final URL[] pluginClassPath = new URL[] {new URL("file:plugin-bin/")};

		// Create class loader (using above class path)
		ClassLoader loader = new URLClassLoader(pluginClassPath);

		// Load class
		Class cl = loader.loadClass("ch.unisi.inf.sp.statistic." + name);

		// Instantiate an object of the class
		Object obj = cl.newInstance();

		return ((Statistic) obj);
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
