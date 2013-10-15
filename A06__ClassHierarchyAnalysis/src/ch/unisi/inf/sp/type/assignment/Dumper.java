package ch.unisi.inf.sp.type.assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ch.unisi.inf.sp.type.framework.ClassHierarchy;
import ch.unisi.inf.sp.type.framework.ClassType;
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

		for (final Type type : hierarchy.getTypes()) {
			pw.println("AAA"); // XXX

			if (type instanceof ClassType) {
				final ClassType classType = (ClassType)type;
				
				System.out.println(classType.getSimpleName() + "->" + classType.getSuperClass().getSimpleName());


				// implement this
				pw.println("CCC"); // XXX
				// TODO
			}
		}
		pw.println("}");
		pw.close();
	}

}
