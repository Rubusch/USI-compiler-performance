package ch.unisi.inf.sp.type.assignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.unisi.inf.sp.type.framework.ArchiveScanner;



/**
 * Main class.
 * 
 * @author Lothar Rubusch
 */
public final class Main {

	public static void main(final String[] args) throws IOException {
/*
		for (final String arg : args) {
			System.out.println(arg);
		}
		System.out.println("---");
//*/
		System.out.println("Benchmark, InvocationType, CallSites, Call by CallSites");
		for (int i=0; i<args.length; i++) {
			// phase 1: build inheritance hierarchy
			final ArchiveScanner scanner = new ArchiveScanner();
			final ClassHierarchyBuilder classHierarchyBuilder = new ClassHierarchyBuilder();
			scanner.addAnalyzer(classHierarchyBuilder);
			scanner.scan(args[i]);
			scanner.removeAnalyzer(classHierarchyBuilder);

			// phase 2: add call sites and edges
			final CallGraphBuilder callGraphBuilder = new CallGraphBuilder(classHierarchyBuilder.getClassHierarchy());
			scanner.addAnalyzer(callGraphBuilder);
			scanner.scan(args[i]);

			// dump info about structure (e.g. inheritance hierarchy, call graph, statistics, ...)
			new Dumper().dumpDot(classHierarchyBuilder.getClassHierarchy(), "graph.dot", args[i]);
		}

		System.out.println("READY.");
	}
}
