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
		for (final String arg : args) {
			System.out.println(arg);
		}
		System.out.println("---");

		// phase 1: build inheritance hierarchy
//		final List<ClassHierarchyBuilder> hierarchyList = new ArrayList<ClassHierarchyBuilder>();
		for (int i=0; i<args.length; i++) {
			final ArchiveScanner scanner = new ArchiveScanner();
			final ClassHierarchyBuilder classHierarchyBuilder = new ClassHierarchyBuilder();
//			hierarchyList.add(classHierarchyBuilder);
			scanner.addAnalyzer(classHierarchyBuilder);
			scanner.scan(args[i]);
			scanner.removeAnalyzer(classHierarchyBuilder);
//		}
		
		// phase 2: add call sites and edges
//		for (int i=0; i<args.length; i++) {

//			final ClassHierarchyBuilder classHierarchyBuilder = hierarchyList.get(i);
			final CallGraphBuilder callGraphBuilder = new CallGraphBuilder(classHierarchyBuilder.getClassHierarchy());
			scanner.addAnalyzer(callGraphBuilder);
//		for (int i=0; i<args.length; i++) {
			scanner.scan(args[i]);
//		}
		
		// dump info about structure (e.g. inheritance hierarchy, call graph, statistics, ...)
//		for( int i=0; i<args.length; ++i){
//			final ClassHierarchyBuilder classHierarchyBuilder = hierarchyList.get(i);
//		for( final String arg : args){
			new Dumper().dumpDot(classHierarchyBuilder.getClassHierarchy(), "graph.dot", args[i]);
		}

		System.out.println("READY.");
	}
	
	
}
