package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DiGraph {
	private final List<Node> nodelist;
	private final List<Edge> CFGedgelist;
	private final List<Edge> DAedgelist;

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
		for( int blockId = 0; true; ++blockId){

			// "traverser"
			if(0 == blockId){
				++blockId;
				// init start link
				List<List<Integer>> inheritage = new ArrayList<List<Integer>>();
				inheritage.add(new ArrayList<Integer>());
				inheritage.get(0).add(new Integer( START ));
				currCFG.inheritageInit(inheritage);

				// keep track of passed nodes
				passedIds.add(currCFG.id());
				continue;

			}else{

				if( stack.isEmpty()){
					// stack is empty discover next tier
					for( Edge peekEdge : CFGedgelist){
						if( currCFG.id() == peekEdge.getFromNode().id() ){
							if( -1 == passedIds.indexOf(peekEdge.getToNode().id())){
								stack.push(peekEdge);
							}else{
								continue;
							}
						}
					}
					
					if( stack.isEmpty()){
						// we're done when the stack can't be filled anymore
						break;
					}
					continue;
				}else{
					// candidate was ok
					Edge followEdge = stack.pop();
					currCFG = followEdge.getToNode();
					passedIds.add( currCFG.id() );
				}
			}

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
				Node parent = edges.get(0).getFromNode();
				if( 0 == parent.getInheritage().size()){
					Analyzer.echo( "FATAL - only 1 parent, but inheritage is empty");
				}
				currCFG.inheritageInit( parent.getInheritage() );

			}else if( 1 < edges.size() ){
				// more than 1 parents - merge inheritage
				final List<Node> parents = new ArrayList<Node>();
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
		Analyzer.echo("# ---");
		if( 0 == this.nodelist.size() ) return;

		// header
		Analyzer.echo( "digraph G {" );
		Analyzer.echo( "  nodesep=.5" );
		Analyzer.echo( "  node [shape=record,width=.1,height=.1]" );

		// nodes
		Analyzer.echo( "  nodeS [label = \"start\"];" );
		Analyzer.echo( "  nodeE [label = \"end\"];" );
		for( Node node : nodelist ){
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
		Analyzer.echo("  node" + String.valueOf(nodelist.size()-1) + " -> nodeE" );

		Analyzer.echo("}");
	}
}
