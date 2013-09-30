package ch.usi.inf.sp.cfg;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DiGraph {
	private final ControlFlowGraphExtractor controlFlow;
	private final ArrayList<Node> nodelist;
	private final ArrayList<Edge> edgelist;

	public DiGraph(ControlFlowGraphExtractor controlFlow){
		this.controlFlow = controlFlow;

		this.nodelist = new ArrayList<Node>();
		for( int nodeId = 0; nodeId < controlFlow.getBlocklist().size(); ++nodeId){
			nodelist.add(new Node( controlFlow.getBlocklist().get(nodeId), nodeId)); // XXX 1
		}

		this.edgelist = new ArrayList<Edge>();
		for( String szEdge : controlFlow.getEdgeslist() ){
			int srcId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[0]).intValue() );
			int dstId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[1]).intValue() );
			edgelist.add(new Edge( srcId, dstId ));
		}
	}

	public void dotPrintDA(){
		if( 0 == this.nodelist.size() ) return;

		// header
		System.out.println( "digraph G {" );
		System.out.println( "  nodesep=.5" );
		System.out.println( "  rankdir=LR" );
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
		for( Edge edge : edgelist ){
			edge.dotPrint();
		}
		System.out.println("  node" + String.valueOf(nodelist.size()-1) + " -> nodeE" );

		System.out.println("}");
	}
}
