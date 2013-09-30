package ch.usi.inf.sp.cfg;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DiGraph {
	private final ControlFlowGraphExtractor controlFlow;
	private final ArrayList<Node> nodelist;
	private final ArrayList<Edge> edgeslist;

	public DiGraph(ControlFlowGraphExtractor controlFlow){
		this.controlFlow = controlFlow;

		this.nodelist = new ArrayList<Node>();
		for( int nodeId = 0; nodeId < controlFlow.getBlocklist().size(); ++nodeId){
			nodelist.add(new Node( controlFlow.getBlocklist().get(nodeId), nodeId)); // XXX 1
		}

		this.edgeslist = new ArrayList<Edge>();
		for( String szEdge : controlFlow.getEdgeslist() ){
			int srcId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[0]).intValue() );
			int dstId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[1]).intValue() );
			edgeslist.add(new Edge( srcId, dstId ));
		}
	}

	public void dotPrintDA(){
		
		System.out.println( "TODO" );
	}
}
