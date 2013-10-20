package ch.unisi.inf.sp.type.assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.Opcodes;

import ch.unisi.inf.sp.type.framework.CallSite;
import ch.unisi.inf.sp.type.framework.ClassHierarchy;
import ch.unisi.inf.sp.type.framework.ClassType;
import ch.unisi.inf.sp.type.framework.Method;
import ch.unisi.inf.sp.type.framework.Type;


/**
 * Dump out information about the given ClassHierarchy.
 * 
 * @author Lothar Rubusch
 */
public final class Dumper {

	public void dumpDot(final ClassHierarchy hierarchy, final String fileName) throws IOException {
		final PrintWriter pw = new PrintWriter(new FileWriter(fileName));
		pw.println("digraph types {");
		pw.println("  rankdir=\"BT\"");

		pw.println("nodesep=.5");
		pw.println("node [shape=record,width=.1,height=.1]");

		int nINVIRT=0;
		float avgINVIRT=0;
		int nININT=0;
		float avgININT=0;
		int nCallSites = 0;

		for (final Type type : hierarchy.getTypes()) {
			if (type instanceof ClassType) {
				final ClassType classType = (ClassType)type;
				System.out.println("classType: " + classType.getSimpleName());
				
// TODO do treelike output
				int nTmpINVIRT = 0;
				int nTmpININT = 0;
				int nTmpCallSites = 0;

				for( Method method : classType.getMethods()) {

					nTmpCallSites += method.getCallSites().size();

					for(CallSite callSite : method.getCallSites()){
						switch (callSite.getOpcode()){
						case Opcodes.INVOKEVIRTUAL: nTmpINVIRT++; break;
//							System.out.println("INVOKE_VIRTUAL"); break;
						case Opcodes.INVOKEINTERFACE: nTmpININT++; break;
//							System.out.println("INVOKE_INTERFACE"); break;
						}
					}

					System.out.println( "\t\tCallSites:\t" + String.valueOf(nCallSites));
				}
				nCallSites += nTmpCallSites;
				nINVIRT += nTmpINVIRT;
				nININT += nTmpININT;

				
				System.out.println("\tCallSites: " + String.valueOf(nTmpCallSites));
				float avgTmpINVIRT = getAvg(nTmpINVIRT, nTmpCallSites);
				System.out.println("\tavg INVOKEVIRTUAL: " + String.valueOf(avgTmpINVIRT));
				float avgTmpININT = getAvg(nTmpININT, nTmpCallSites);
				System.out.println("\tavg INVOKEINTERFACE: " + String.valueOf(avgTmpININT));

// dot EXAMPLE
				// node0 [align=left,label="block0 | { <0> 0: label\l | <1> 1: linenumber\l | <8> 8: INVOKE\l }"];
				// node0:8 -> node1:9[ label="fallthrou PEI" ]

/*
				if( null != classType.getSuperClass() ){
					System.out.println( "SuperClass" + classType.getSuperClass().getSimpleName() );
					pw.println(classType.getSimpleName() + "->" + classType.getSuperClass().getSimpleName());
				}
//*/
			}
		}
		pw.println("}");
		pw.close();


		System.out.println("total CallSites: " + String.valueOf(nCallSites));
		avgINVIRT = getAvg(nINVIRT, nCallSites);
		System.out.println("total avg INVOKEVIRTUAL: " + String.valueOf(avgINVIRT));
		avgININT = getAvg(nININT, nCallSites);
		System.out.println("total avg INVOKEINTERFACE: " + String.valueOf(avgININT));
	}
	
	private float getAvg( int sum, int divided){
		if( divided == 0) return 0;
		else return (float) sum / (float) divided;
	}

}
