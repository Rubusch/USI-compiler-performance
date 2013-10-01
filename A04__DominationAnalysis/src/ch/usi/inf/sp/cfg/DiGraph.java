package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DiGraph {
	private final ControlFlowGraphExtractor controlFlow;
	private final ArrayList<Node> nodelist;
	private final ArrayList<Edge> CFGedgelist;
	private final ArrayList<Edge> DAedgelist;

	public DiGraph(ControlFlowGraphExtractor controlFlow){
		this.controlFlow = controlFlow;

		this.nodelist = new ArrayList<Node>();
		for( int nodeId = 0; nodeId < controlFlow.getBlocklist().size(); ++nodeId){
			nodelist.add(new Node( controlFlow.getBlocklist().get(nodeId), nodeId)); // XXX 1
		}

		this.CFGedgelist = new ArrayList<Edge>();
		for( String szEdge : controlFlow.getEdgeslist() ){
			int srcId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[0]).intValue() );
			int dstId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[1]).intValue() );
			CFGedgelist.add(new Edge( nodelist.get(srcId), nodelist.get( dstId )));
		}


/******************************************************************************/
		// CFG prepared
		for( int blockId = 1; blockId < nodelist.size(); ++blockId){
			Node current = nodelist.get(blockId);

			// find all edges ending at current (and no upward linking, to avoid loop issues)
			List<Edge> edges = new ArrayList<Edge>();
			for( Edge edge: CFGedgelist){
				if( (blockId == edge.getToNode().id())
						&& (edge.getFromNode().id() < edge.getToNode().id())){
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
System.out.println( "XXX id:" + blockId + " 1 == edges.size() " + edges.size()); // TODO rm
				if( 0 < edges.get(0).getFromNode().getInheritage().size()){
					current.inheritageInit(edges.get(0).getFromNode().getInheritage());
				}else{
					edges.get(0).getFromNode().inheritageInit( null );
					current.inheritageInit( edges.get(0).getFromNode().getInheritage());
				}
			}else{
// TODO is this needed?
System.out.println( "XXX id:" + blockId + " else");
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
			DAedgelist.add( new Edge( nodelist.get( current.getIDom().intValue()), current ));
		}
	}

	private void dominationAnalysis(){
		
	}

	public void dotPrintDA(){
		System.out.println("# ---");
		if( 0 == this.nodelist.size() ) return;

		// header
		System.out.println( "digraph G {" );
		System.out.println( "  nodesep=.5" );
//		System.out.println( "  rankdir=LR" );
		System.out.println( "  node [shape=record,width=.1,height=.1]" );

		// nodes
		System.out.println( "  nodeS [label = \"{ <S> S }\"];" );
		System.out.println( "  nodeE [label = \"{ <E> E }\"];" );
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

	public void findLoops(){
// TODO
//		Stack<Edges> stack;
//		ArrayList<> already;
	}
}
