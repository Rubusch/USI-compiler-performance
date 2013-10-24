package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DiGraph {
	private final List<NodeWrapper> nodelist;
	private final List<Edge> CFGedgelist;
	private final List<Edge> DAedgelist;

	public final static int START = -1;
	public final static int END = -2;



// FIXME user END 
	public DiGraph(ControlFlowGraphExtractor controlFlow){
		this.nodelist = new ArrayList<NodeWrapper>();
		for( int nodeId = 0; nodeId < controlFlow.getBlocklist().size(); ++nodeId){
//			nodelist.add(new NodeWrapper( controlFlow.getBlocklist().get(nodeId), nodeId)); // TODO rm
			nodelist.add(new NodeWrapper( nodeId ));
		}

		this.CFGedgelist = new ArrayList<Edge>();
		for( String szEdge : controlFlow.getEdgeslist() ){
			int srcId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[0]).intValue() );
			int dstId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[1]).intValue() );
//			CFGedgelist.add(new Edge( nodelist.get(srcId), nodelist.get( dstId ))); // TODO rm

			NodeWrapper srcNode;
			if( 0 > srcId ){ 
				srcNode = new NodeWrapper(START);
			}else{
				srcNode = nodelist.get(srcId);
			}

			NodeWrapper dstNode;
			if( 0 > dstId ){
				dstNode = new NodeWrapper(END);
			}else{
				dstNode = nodelist.get(dstId);
			}

//			CFGedgelist.add(new Edge( nodelist.get(srcId), nodelist.get( dstId ))); // TODO rm
			CFGedgelist.add(new Edge( srcNode, dstNode));
		}

/******************************************************************************/
		// CFG prepared
		NodeWrapper currCFG = nodelist.get(0); // TODO rn "root"??
		List<Integer> passedIds = new ArrayList<Integer>();
		Stack<Edge> stack = new Stack<Edge>();
//		int blockId = 0;
		boolean isFirstRun = true;
		while( true ){

//		for( int blockId = 0; true; ++blockId){ // TODO check, is it really 2x increment?
			// "traverser"
//			if(0 == blockId){
//				++blockId;

			if(isFirstRun){
				isFirstRun = false;
				// init start link
				List<List<Integer>> inheritage = new ArrayList<List<Integer>>();
				inheritage.add(new ArrayList<Integer>());
				inheritage.get(0).add(new Integer( START ));
				currCFG.inheritageInit(inheritage);

				// keep track of passed nodes
				passedIds.add(currCFG.id());
				continue;

			}
//			else{ // TODO no else necessary, before has "continue"?!
				if( stack.isEmpty()){
					// stack is empty discover next tier
					for( Edge peekEdge : CFGedgelist){
						if( currCFG.id() == peekEdge.getFromNode().id() ){
							if( -1 == passedIds.indexOf(peekEdge.getToNode().id())){
								stack.push(peekEdge);
							}
//							else{ // TODO not necessary?!
//								continue; // TODO not necessary?!
//							}
						}
					}

					if( stack.isEmpty()){
						// we're done when the stack still can't be filled anymore
						break;
					}
					continue;
				}
//				else{ // TODO is this 'else' necessary?

					// stack was NOT empty, candidate was ok
					Edge followEdge = stack.pop();
					currCFG = followEdge.getToNode();
					passedIds.add( currCFG.id() );

//				} // TODO refac
//			} // TODO refac

			// find all edges ending directed to current (but not upward linking, to avoid loop issues)
			List<Edge> edges = new ArrayList<Edge>();
			for( Edge edge: CFGedgelist){
				if( currCFG.id() == edge.getToNode().id()){
					edges.add(edge);
				}
			}

			// set heritage to the childs
			if( 1 == edges.size()){
				// only one parent
				NodeWrapper parent = edges.get(0).getFromNode();
				if( 0 == parent.getInheritage().size()){
					Analyzer.echo( "FATAL - only 1 parent, but inheritage is empty");
				}
				currCFG.inheritageInit( parent.getInheritage() );

			}else if( 1 < edges.size() ){
				// more than 1 parents - merge inheritage
				final List<NodeWrapper> parents = new ArrayList<NodeWrapper>();
				for( Edge edge : edges ){
					parents.add(edge.getFromNode());
				}
				currCFG.inheritageMerge(parents);
			}
		}

/******************************************************************************/
		// map CFG to DA
		this.DAedgelist = new ArrayList<Edge>();
		for( int blockId = nodelist.size()-1; blockId > 0; --blockId){
			// find all edges ending at current (and no upward linking, to avoid loop issues)
			NodeWrapper currDA = nodelist.get(blockId);
//			DAedgelist.add( new Edge( nodelist.get( current.getIDom().intValue()), current )); // XXX -1
			Integer idxidom = currDA.getIDom();
			final NodeWrapper idom;
			if( START == idxidom){
//				idom = new NodeWrapper(null, START); // TODO rm
				idom = new NodeWrapper(START);
			}else{
				idom = nodelist.get(idxidom);
			}
			DAedgelist.add( new Edge(idom, currDA) );
		}
	}

// FIXME: forEver connected to end, this is wrong!
	public void dotPrintDA(){
		Analyzer.echo("# ---");
		if( 0 == this.nodelist.size() ) return;

		// header
		Analyzer.echo( "digraph G {" );
		Analyzer.echo( "  nodesep=.5" );
		Analyzer.echo( "  node [shape=record,width=.1,height=.1]" );

		// nodes
		Analyzer.echo( "  nodeS [label = \"start\"];" );
		Analyzer.echo( "  nodeE [label = \"end\"];" );
		for( NodeWrapper node : nodelist ){
			node.dotPrint();
// TODO another label?
//			Analyzer.echo("  node" + node.id() + "[ label = \""+ node.id() + "\"];");
		}

		// edges
		Analyzer.echo( "  nodeS -> node0" );
//		for( Edge edge : CFGedgelist ){
		for( Edge edge : DAedgelist ){
			edge.dotPrint();
		}
		
// FIXME: nodeE should be part of the "common" process
//		Analyzer.echo("  node" + String.valueOf(nodelist.size()-1) + " -> nodeE" );

		Analyzer.echo("}");
	}
}
