package ch.unisi.inf.sp.type.assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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

		for (final Type type : hierarchy.getTypes()) {
			if (type instanceof ClassType) {
				final ClassType classType = (ClassType)type;

				System.out.println(classType.getSimpleName());
				for( Method method : classType.getMethods()) {
					System.out.println("\t" + method.getName() + ", " + method.getModifiers());
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
	}

}
