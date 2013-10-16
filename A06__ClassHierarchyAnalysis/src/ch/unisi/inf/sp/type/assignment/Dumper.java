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

		int nINVOKEVIRTUAL=0;
		float avgINVOKEVIRTUAL=0;
		int nINVOKEINTERFACE=0;
		float avgINVOKEINTERFACE=0;

		for (final Type type : hierarchy.getTypes()) {
			if (type instanceof ClassType) {
				final ClassType classType = (ClassType)type;

				System.out.println("class name: " + classType.getSimpleName());
				for( Method method : classType.getMethods()) {
					int nINVIRT = 0;
					int nININT = 0;

					for(CallSite callSite : method.getCallSites()){
						switch (callSite.getOpcode()){
						case Opcodes.INVOKEVIRTUAL: nINVIRT++; break;
//							System.out.println("INVOKE_VIRTUAL"); break;
						case Opcodes.INVOKEINTERFACE: nININT++; break;
//							System.out.println("INVOKE_INTERFACE"); break;
						}
					}
					System.out.println("\t" + method.getName());
					System.out.println("\t\tINVOKEVIRTUAL:\t\t" + String.valueOf(nINVIRT));
					nINVOKEVIRTUAL += nINVIRT;
					if(avgINVOKEVIRTUAL==0){
						avgINVOKEVIRTUAL = nINVIRT;
					}else{
						avgINVOKEVIRTUAL = (avgINVOKEVIRTUAL + (float)nINVIRT)/2;
					}
					System.out.println("\t\tINVOKEINTERFACE:\t" + String.valueOf(nININT));
					nINVOKEINTERFACE += nININT;
					if(avgINVOKEINTERFACE == 0){
						avgINVOKEINTERFACE = nININT;
					}else{
						avgINVOKEINTERFACE = (avgINVOKEINTERFACE + (float)nININT)/2;
					}
				}
				
				
				
// EXAMPLE
				// node0 [align=left,label="block0 | { <0> 0: label\l | <1> 1: linenumber\l | <8> 8: INVOKE\l }"];
				// node0:8 -> node1:9[ label="fallthrou PEI" ]

				if( null != classType.getSuperClass() ){
					System.out.println( "SuperClass" + classType.getSuperClass().getSimpleName() );
					pw.println(classType.getSimpleName() + "->" + classType.getSuperClass().getSimpleName());
				}

			}
		}
		pw.println("}");
		pw.close();

		System.out.println("avg INVOKEVIRTUAL: " + avgINVOKEVIRTUAL);
		System.out.println("avg INVOKEINTERFACE: " + avgINVOKEINTERFACE);
	}

}
