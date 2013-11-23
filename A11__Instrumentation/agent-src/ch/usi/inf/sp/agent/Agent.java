package ch.usi.inf.sp.agent;

import java.lang.instrument.Instrumentation;

public class Agent {

	/**
	 * @param args
	 */
	public static void premain(String agentArgs, Instrumentation inst){
		System.out.println("Agent stating (arguments: '"+agentArgs+"')");

		inst.addTransformer(new Transformer());
	}
}
