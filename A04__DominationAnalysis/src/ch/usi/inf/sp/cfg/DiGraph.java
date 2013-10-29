package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * unfortunately full of quickfixes and de-composed / unorganized code
 * 
 * @author Lothar Rubusch
 *
 */
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

		// get size of CFG block list, then idx of the blocks will be used as id's here
		int CFGBlockListSize = controlFlow.getBlocklist().size();

		// check if last block is just a trailing lable
		int lastBlockIdx = CFGBlockListSize - 1;
		List<AbstractInsnNode> block = controlFlow.getBlocklist().get(lastBlockIdx);
		int finalBlockSize = block.size();
		if( 1 == finalBlockSize ){
			// ...only has one element
			if( block.get(0).getType()== AbstractInsnNode.LABEL){
				// ...and it is just a lable, then don't add, we're done
				CFGBlockListSize -= 1;
			}
		}

		for( int nodeId = 0; nodeId < CFGBlockListSize; ++nodeId){
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
			int iSrcId = Integer.valueOf( szEdge.split(":")[0]).intValue();
			int srcId = controlFlow.insId2NodeId( iSrcId );
			if( 0 > srcId ){
				srcId = START;
			}
					
			int iDstId = Integer.valueOf( szEdge.split(":")[1]).intValue();
			int dstId = controlFlow.insId2NodeId( iDstId );
			if( 0 > dstId ){
				dstId = END;
			}

			// stupid check
			if( srcId == dstId) continue;

			NodeWrapper srcNode;
			srcNode = getNodeById( CFGBlockList, srcId);

			NodeWrapper dstNode;
			dstNode = getNodeById( CFGBlockList, dstId );

			CFGEdgeList.add(new Edge( srcNode, dstNode));
		}
	}



	private List<Edge> getNextEdges( NodeWrapper currNode ){
// TODO test function
		List<Edge> nextEdges = new ArrayList<Edge>();

		// find next node from currNode
		for( Edge edge : CFGEdgeList){
			if( currNode == edge.getFromNode() ){
				nextEdges.add(edge);
			}
		}

		// return list of following next ndoes
		return nextEdges;
	}

	private boolean isPassedEdge( List<Edge> passedEdges, Edge currEdge ){
		for(Edge edge : passedEdges){
			if( currEdge.getFromNode() == edge.getFromNode()){
				if( currEdge.getToNode() == edge.getToNode()){
					return true;
				}
			}
		}
		return false;
	}


	public DiGraph(ControlFlowGraphExtractor controlFlow){
		initCFGBlockList( controlFlow );
		initCFGEdgeList( controlFlow );

		// CFG prepared
		NodeWrapper currNode = getNodeById(CFGBlockList, START);
		List<Edge> passedEdges = new ArrayList<Edge>();
		Stack<Edge> stack = new Stack<Edge>();

		// init inheritage list with START
		currNode.inheritageMerge( null );

		while( true ){
			if( stack.isEmpty()){
				// stack is empty, go discover next tier, push on stack
				List<Edge> nextEdges = getNextEdges( currNode );
				for( Edge nextEdge : nextEdges ){
					if( !isPassedEdge( passedEdges, nextEdge )){
						stack.push(nextEdge);
					}
				}

				if( stack.isEmpty()){
// TODO test the break and completeness of the traverser
					// we're done when the stack can't be filled anymore
					break;
				}
				continue;
			}

			// stack was NOT empty, fetch first candidate...
			Edge currEdge = stack.pop();
			passedEdges.add( currEdge );

			// prepare for update dominator information
			NodeWrapper parentNode = currEdge.getFromNode();
			currNode = currEdge.getToNode();

			// update dominator information
			currNode.inheritageMerge(parentNode);
		}

/*		
		// longtime memory
		passedIds.add(currNode.id());

		while( true ){
			if( stack.isEmpty()){
				// stack is empty, go discover next tier
				for( Edge probeEdge : CFGEdgeList){
					if( null != probeEdge.getFromNode()){
						if( currNode.id() == probeEdge.getFromNode().id() ){
							// next node points to current node
							if( -1 == passedIds.indexOf(probeEdge.getToNode().id())){
								// this node is unknown, store for later
								stack.push(probeEdge);
							}
						}
					}
				}

				if( stack.isEmpty()){
					// we're done when the stack can't be filled anymore
					break;
				}
				continue;
			}

			// stack was NOT empty, fetch first candidate...
			Edge nextEdge = stack.pop();
			currNode = nextEdge.getToNode();
			passedIds.add( currNode.id() );

			// set up a list of all edges pointing at the current node (linking down, but not linking upward, to avoid loop issues)
			List<Edge> edges = new ArrayList<Edge>();
			for( Edge edge: CFGEdgeList){
				if( null != edge.getToNode()){
					if( currNode.id() == edge.getToNode().id()){
						edges.add(edge);
					}
				}
			}

			// set heritage to the children
			// set up another list of the sources (nodes) of those edges, pointing to current
			final List<NodeWrapper> parentNodeList = new ArrayList<NodeWrapper>();
			

			final List<NodeWrapper> parentNodeList = new ArrayList<NodeWrapper>();
			for( Edge edge : edges ){
				parentNodeList.add(edge.getFromNode());
			}
			
//			if( 1 == parentNodeList.size()){
//				// pass heritage from a single node
//				currNode.inheritageInit( parentNodeList.get(0).getInheritage() );
//			}else{
				// merge heritage from several parents together
//				Analyzer.db("going to merge...");
//				Analyzer.db("\t- parentNodeList.size() " + parentNodeList.size());
//				currNode.inheritageMerge(parentNodeList);
//			}

			// update inheritage merge
			currNode.inheritageMerge(parentNodeList);
		}
//*/

/******************************************************************************/
		// map CFG to DA
		this.DAedgeList = new ArrayList<Edge>();

		for( int blockId = CFGBlockList.size()-1; blockId > 0; --blockId){

			// find all edges ending at current (and no upward linking, to avoid loop issues)
			currNode = CFGBlockList.get(blockId);

			// check for END connections
			if(currNode.id() == END){
				boolean isNoReturnConnetcted = true;
				for(Edge edge : CFGEdgeList){
					if(edge.getToNode().id() == END){
						// the nodes may connect to END
						isNoReturnConnetcted = false;
					}
				}
				if(isNoReturnConnetcted){
					// no connection to END - just continue
					continue;
				}
			}


			Integer idom = currNode.getIDom();
			final NodeWrapper idomNode;
			if( START == idom){
				idomNode = new NodeWrapper(START);
			}else if( END == idom){
				idomNode = new NodeWrapper( END );
			}else{
				idomNode = CFGBlockList.get(idom);
			}

			if(idomNode.id() != currNode.id() && START != currNode.id()){
				// no links to same node
				// no START as to-link destination
				DAedgeList.add( new Edge(idomNode, currNode) );
			}
		}
	}



	public void dotPrintDA(){
		Analyzer.echo("# dominator analysis ");
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
		}

		// edges
		Analyzer.echo( "  nodeS -> node0" ); // TODO replace by automatic detection
		for( Edge edge : DAedgeList ){
			edge.dotPrint();
		}

		Analyzer.echo("}");
	}
}
