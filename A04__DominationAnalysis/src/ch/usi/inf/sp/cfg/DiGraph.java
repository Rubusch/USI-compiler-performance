package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DiGraph {
	private final ArrayList<Node> nodelist;
	private final ArrayList<Edge> CFGedgelist;
	private final ArrayList<Edge> DAedgelist;

	public final static int START = -1;
	public final static int END = -2;

	public DiGraph(ControlFlowGraphExtractor controlFlow){
		this.nodelist = new ArrayList<Node>();
		for( int nodeId = 0; nodeId < controlFlow.getBlocklist().size(); ++nodeId){
			nodelist.add(new Node( controlFlow.getBlocklist().get(nodeId), nodeId));
		}

		this.CFGedgelist = new ArrayList<Edge>();
		for( String szEdge : controlFlow.getEdgeslist() ){
			int srcId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[0]).intValue() );
			int dstId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[1]).intValue() );
			CFGedgelist.add(new Edge( nodelist.get(srcId), nodelist.get( dstId )));
		}

/******************************************************************************/
		// CFG prepared
		Node currCFG = nodelist.get(0);
		ArrayList<Integer> passedIds = new ArrayList<Integer>();
		Stack<Edge> stack = new Stack<Edge>();
//		for( int blockId = 0; blockId < nodelist.size(); ++blockId){
		for( int blockId = 0; true; ++blockId){

			// "traverser"
			if(0 == blockId){
				// init start link
				List<List<Integer>> inheritage = new ArrayList<List<Integer>>();
				inheritage.add(new ArrayList<Integer>());
				inheritage.get(0).add(new Integer( START ));
				currCFG.inheritageInit(inheritage);

				// keep track of passed nodes
				passedIds.add(currCFG.id());
System.out.println( "AAA currCFG " + currCFG.id());
				continue;

			}else{
				// get all possible next elements, and push them to stack
				for( Edge peekEdge : CFGedgelist){
					if( currCFG.id() == peekEdge.getFromNode().id() ){
						if( -1 == passedIds.indexOf(peekEdge.getToNode().id())){
							stack.push(peekEdge);
						}else{
							continue;
						}
					}
				}

				// check stack, and fetch next candidate
				if( stack.isEmpty() ){ 
					// we're done, this is a dead end
System.out.println( "XXX stack was empty - we're done");
//					return;
					break;
//					continue; // continue, to let it finish by for index
				}
System.out.println( "TEST stacksize " + stack.size() + "[before]");
				Edge followEdge = stack.pop();
System.out.println( "TEST stacksize " + stack.size() + "[after]");
				currCFG = followEdge.getToNode();

				// check if we've seen that node already
//				if( -1 != passedIds.indexOf( currCFG.id() )){
//					continue;
//				}

				// candidate was ok
				passedIds.add( currCFG.id() );
			}

System.out.println( "BBB currCFG " + currCFG.id());

			// find all edges ending directed to current (but not upward linking, to avoid loop issues)
// TODO fix for upward linking, loop detection - allow up, but when "contains" in one of the lists, stop ( = looping)
			List<Edge> edges = new ArrayList<Edge>();
			for( Edge edge: CFGedgelist){
// TODO
//*
				if( currCFG.id() == edge.getToNode().id()){
					if( -1 != edges.indexOf(edge)){
						break;
					}
/*/
				if( (currCFG.id() == edge.getToNode().id())
						&& (edge.getFromNode().id() < edge.getToNode().id())){
//*/
					edges.add(edge);
				}
			}

			// set heritage to the childs
			if( 1 == edges.size()){
				// only one parent
				Node parent = edges.get(0).getFromNode();
				if( 0 == parent.getInheritage().size()){
					System.out.println( "FATAL - only 1 parent, but inheritage is empty");
				}
				currCFG.inheritageInit( parent.getInheritage() );

			}else if( 1 < edges.size() ){
				// more than 1 parents - merge inheritage
				final List<Node> parents = new ArrayList<Node>();
				for( Edge edge : edges ){
					parents.add(edge.getFromNode());
				}
				currCFG.inheritageMerge(parents);

			}else{
				// no downlink, just uplinks
				System.out.println( "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");


				// may happen if node is inferior (no uplinking allowed)
				for( Edge iedge: CFGedgelist){
					if( currCFG.id() == iedge.getToNode().id()){
						edges.add(iedge);
					}
				}
//				current.inheritageInit(edges.get(0).getFromNode().getInheritage());
			}
		}

/******************************************************************************/
		// map CFG to DA
		this.DAedgelist = new ArrayList<Edge>();
		for( int blockId = nodelist.size()-1; blockId > 0; --blockId){
			// find all edges ending at current (and no upward linking, to avoid loop issues)
			Node currDA = nodelist.get(blockId);
//			DAedgelist.add( new Edge( nodelist.get( current.getIDom().intValue()), current )); // XXX -1
			Integer idxidom = currDA.getIDom();
			final Node idom;
			if( START == idxidom){
				idom = new Node(null, START);
			}else{
				idom = nodelist.get(idxidom);
			}
			DAedgelist.add( new Edge(idom, currDA) );
		}
	}


	public void dotPrintDA(){
		System.out.println("# ---");
		if( 0 == this.nodelist.size() ) return;

		// header
		System.out.println( "digraph G {" );
		System.out.println( "  nodesep=.5" );
		System.out.println( "  node [shape=record,width=.1,height=.1]" );

		// nodes
		System.out.println( "  nodeS [label = \"start\"];" );
		System.out.println( "  nodeE [label = \"end\"];" );
		for( Node node : nodelist ){
			node.dotPrint();
// TODO another label?
//			System.out.println("  node" + node.id() + "[ label = \""+ node.id() + "\"];");
		}

		// edges
		System.out.println( "  nodeS -> node0" );
//		for( Edge edge : CFGedgelist ){
		for( Edge edge : DAedgelist ){
			edge.dotPrint();
		}
		System.out.println("  node" + String.valueOf(nodelist.size()-1) + " -> nodeE" );

		System.out.println("}");
	}


// in order to detect the loops, a separate traverser class should be implemented, walking down the DAedges
//	public void findLoops(){
// TODO not implemented so far
//		Stack<Edges> stack;
//		ArrayList<> alreadyPassed;
//		ArrayList<> currenthistory
// 	}
}
