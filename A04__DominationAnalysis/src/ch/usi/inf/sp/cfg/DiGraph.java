package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.tree.AbstractInsnNode;

public class DiGraph {
	private List<NodeWrapper> CFGBlockList;
	private List<Edge> CFGEdgeList;
	private final List<Edge> DAedgeList;

	public final static int START = -1;
	public final static int END = -2;


	private NodeWrapper getNodeById( List<NodeWrapper> list, int id ){
		for( NodeWrapper node : list ){
			if( node.id() == id) return node;
		}
		return null;
	}


	private void initCFGBlockList( ControlFlowGraphExtractor controlFlow){
		// populate CFGBlockList
		this.CFGBlockList = new ArrayList<NodeWrapper>();
		for( int nodeId = 0; nodeId < controlFlow.getBlocklist().size(); ++nodeId){
//			Analyzer.db("AAA initCFGBlockList.add( new '" + String.valueOf(nodeId) + "')"); // TODO rm
			CFGBlockList.add(new NodeWrapper( nodeId ));
		}

		// set up nodes
		CFGBlockList.add(new NodeWrapper( START ));
		CFGBlockList.add(new NodeWrapper( END ));
	}

	private void initCFGEdgeList( ControlFlowGraphExtractor controlFlow){
		// populate CFGEdgelist
		this.CFGEdgeList = new ArrayList<Edge>();
		for( String szEdge : controlFlow.getEdgeslist() ){
			int srcId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[0]).intValue() );
			int dstId = controlFlow.insId2NodeId( Integer.valueOf( szEdge.split(":")[1]).intValue() );

			NodeWrapper srcNode;
			srcNode = getNodeById( CFGBlockList, srcId);

			NodeWrapper dstNode;
			dstNode = getNodeById( CFGBlockList, dstId );

			CFGEdgeList.add(new Edge( srcNode, dstNode));
		}
	}
	

// FIXME user END 
	public DiGraph(ControlFlowGraphExtractor controlFlow){
		initCFGBlockList( controlFlow );
		initCFGEdgeList( controlFlow );

/******************************************************************************/
		// CFG prepared
		NodeWrapper currCFG = getNodeById(CFGBlockList, START);

		List<Integer> rememberedIds = new ArrayList<Integer>();
		Stack<Edge> stack = new Stack<Edge>();

		// init inheritage list with START
		List<List<Integer>> inheritageList = new ArrayList<List<Integer>>();
		List<Integer> inheritageElement = new ArrayList<Integer>();
		inheritageElement.add(START);
		inheritageList.add(inheritageElement);
		currCFG.inheritageInit(inheritageList);

		// longtime memory
		rememberedIds.add(currCFG.id());

		while( true ){
			if( stack.isEmpty()){
				// stack is empty discover next tier
				for( Edge peekEdge : CFGEdgeList){
					if( currCFG.id() == peekEdge.getFromNode().id() ){
						if( -1 == rememberedIds.indexOf(peekEdge.getToNode().id())){
							stack.push(peekEdge);
						}
					}
				}

				if( stack.isEmpty()){
					// we're done when the stack still can't be filled anymore
					break;
				}
				continue;
			}

			// stack was NOT empty, candidate was ok
			Edge followEdge = stack.pop();
			currCFG = followEdge.getToNode();
			rememberedIds.add( currCFG.id() );

			// find all edges ending at the current block (linking down, but not linking upward, to avoid loop issues)
			List<Edge> edges = new ArrayList<Edge>();
			for( Edge edge: CFGEdgeList){
				if( currCFG.id() == edge.getToNode().id()){
					edges.add(edge);
				}
			}

			// set heritage to the children
//*
			final List<NodeWrapper> parents = new ArrayList<NodeWrapper>();
			for( Edge edge : edges ){
				parents.add(edge.getFromNode());
			}
			if( 1 == parents.size()){
				currCFG.inheritageInit( parents.get(0).getInheritage() );
			}else{
				currCFG.inheritageMerge(parents);
			}
/*/
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
//*/
		}

/******************************************************************************/
		// map CFG to DA
		this.DAedgeList = new ArrayList<Edge>();

		Analyzer.db("XXX size of CFGBlockList " + String.valueOf(CFGBlockList.size())); // TODO rm

		for( int blockId = CFGBlockList.size()-1; blockId > 0; --blockId){
			Analyzer.db("YYY blockId '" + String.valueOf(blockId) + "'"); // TODO rm
			
			// find all edges ending at current (and no upward linking, to avoid loop issues)
			NodeWrapper currDA = CFGBlockList.get(blockId);

			Analyzer.db("YYY mapping CFG to DA: currDA.id() " + String.valueOf(currDA.id()) ); // TODO rm

//			DAedgelist.add( new Edge( nodelist.get( current.getIDom().intValue()), current )); // XXX -1
			Integer idxidom = currDA.getIDom();
			
			Analyzer.db("YYY idxidom '" + String.valueOf(idxidom) + "'"); // TODO rm
			final NodeWrapper idom;
			if( START == idxidom){
//				idom = new NodeWrapper(null, START); // TODO rm
				idom = new NodeWrapper(START);
			}else if( END == idxidom){
				idom = new NodeWrapper( END );
			}else{
				idom = CFGBlockList.get(idxidom);
			}
// FIXME
			DAedgeList.add( new Edge(idom, currDA) );
		}
	}




// FIXME: forEver connected to end, this is wrong!
	public void dotPrintDA(){
		Analyzer.echo("# ---");
		if( 0 == this.CFGBlockList.size() ) return;

		// header
		Analyzer.echo( "digraph G {" );
		Analyzer.echo( "  nodesep=.5" );
		Analyzer.echo( "  node [shape=record,width=.1,height=.1]" );

		// nodes
		Analyzer.echo( "  nodeS [label = \"start\"];" );
		Analyzer.echo( "  nodeE [label = \"end\"];" );
		for( NodeWrapper node : CFGBlockList ){
			node.dotPrint();
// TODO another label?
//			Analyzer.echo("  node" + node.id() + "[ label = \""+ node.id() + "\"];");
		}

		// edges
		Analyzer.echo( "  nodeS -> node0" ); // TODO replace by automatic detection
//		for( Edge edge : CFGedgelist ){
		for( Edge edge : DAedgeList ){
			edge.dotPrint();
		}
		
// FIXME: nodeE should be part of the "common" process
//		Analyzer.echo("  node" + String.valueOf(nodelist.size()-1) + " -> nodeE" );

		Analyzer.echo("}");
	}
}
