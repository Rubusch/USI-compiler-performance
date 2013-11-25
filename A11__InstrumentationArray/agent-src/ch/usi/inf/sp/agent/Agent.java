package ch.usi.inf.sp.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
	public static void premain( String agentArgs, Instrumentation inst){
		System.out.println("XXX Agent starting (agentArgs: '" + agentArgs + "')");

//		inst.addTransformer(new Transformer());
	}
}
