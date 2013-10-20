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

	public void dumpDot(final ClassHierarchy hierarchy, final String fileName, final String jarFileName) throws IOException {
		final PrintWriter pw = new PrintWriter(new FileWriter(fileName));
		pw.println("digraph types {");
		pw.println("  rankdir=\"BT\"");

		pw.println("nodesep=.5");
		pw.println("node [shape=record,width=.1,height=.1]");

		int nINVIRT=0;
		int nININT=0;

		float avgINVIRT=0;
		float avgININT=0;

		int nCallSites = 0;

		for (final Type type : hierarchy.getTypes()) {
			int nTmpINVIRT = 0;
			int nTmpININT = 0;
			int nTmpCallSites = 0;

			if (type instanceof ClassType) {
				final ClassType classType = (ClassType)type;
				for( Method method : classType.getMethods()) {

					nTmpCallSites += method.getCallSites().size();

					for(CallSite callSite : method.getCallSites()){
						switch (callSite.getOpcode()){
						case Opcodes.INVOKEVIRTUAL: nTmpINVIRT++; break;
						case Opcodes.INVOKEINTERFACE: nTmpININT++; break;
						}
					}
				}
			}
			
			nCallSites += nTmpCallSites;
			nINVIRT += nTmpINVIRT;
			nININT += nTmpININT;
		}
		pw.println("}");
		pw.close();

		avgINVIRT = getAvg(nINVIRT, nCallSites);
		avgININT = getAvg(nININT, nCallSites);

		System.out.println("Benchmark, InvocationType, CallSites, Call by CallSites");
		System.out.println(jarFileName + ", INVOKE_VIRTUAL, " + String.valueOf(nINVIRT) + ", " + String.valueOf(avgINVIRT) );
		System.out.println(jarFileName + ", INVOKE_INTERFACE, " + String.valueOf(nININT) + ", " + String.valueOf(avgININT) );
		System.out.println("---");
		
		nINVIRT = 0;
	}
	
	private float getAvg( int sum, int divided){
		if( divided == 0) return 0;
		else return (float) sum / (float) divided;
	}

}
