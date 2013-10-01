package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

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
//		for( int blockId = 1; blockId < nodelist.size(); ++blockId){ // TODO rm ?
		for( int blockId = 0; blockId < nodelist.size(); ++blockId){
System.out.println( "XXX blockId " + blockId);
			Node current = nodelist.get(blockId);
//* TODO
			if(0 == blockId){
				List<List<Integer>> inheritage = new ArrayList<List<Integer>>();
				inheritage.add(new ArrayList<Integer>());
				// START = -1
				inheritage.get(0).add(new Integer(-1));
				current.inheritageInit(inheritage);
				continue;
			}
//*/
			// find all edges ending directed to current (but not upward linking, to avoid loop issues)
// TODO fix for upward linking - allow up, but when "contains" in one of the lists, stop ( = looping)
			List<Edge> edges = new ArrayList<Edge>();
			for( Edge edge: CFGedgelist){
System.out.println( "XXX edge \t from " + edge.getFromNode().id() + ", to " + edge.getToNode().id());
				if( (blockId == edge.getToNode().id())
						&& (edge.getFromNode().id() < edge.getToNode().id())){
System.out.println("XXX edge (taken) from " + edge.getFromNode().id() + ", to " + edge.getToNode().id());
					edges.add(edge);
				}
			}

			// if there are more than 1 parents - merge inheritage, else init
			if( 1 < edges.size() ){
				// get all parents
				final List<Node> parents = new ArrayList<Node>();
				for( Edge edge : edges ){
					parents.add(edge.getFromNode());
				}
				current.inheritageMerge(parents);
			}else if( 1 == edges.size()){
//System.out.println( "XXX id:" + blockId + " 1 == edges.size() " + edges.size()); // TODO rm
				if( 0 < edges.get(0).getFromNode().getInheritage().size()){
					current.inheritageInit(edges.get(0).getFromNode().getInheritage());
				}else{
					edges.get(0).getFromNode().inheritageInit( null );
					current.inheritageInit( edges.get(0).getFromNode().getInheritage());
				}
			}else{
// TODO is this needed?
//System.out.println( "XXX id:" + blockId + " else");
				current.inheritageInit( null );
				continue;
			}
		}



/******************************************************************************/
		// map CFG to DA
		this.DAedgelist = new ArrayList<Edge>();
		for( int blockId = nodelist.size()-1; blockId > 0; --blockId){
			// find all edges ending at current (and no upward linking, to avoid loop issues)
			Node current = nodelist.get(blockId);
//			DAedgelist.add( new Edge( nodelist.get( current.getIDom().intValue()), current )); // XXX -1
			
			Integer idxidom = current.getIDom();
			final Node idom;
			if( START == idxidom){
				idom = new Node(null, START);
			}else{
				idom = nodelist.get(idxidom);
			}
			DAedgelist.add( new Edge(idom, current) );
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
