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
//Analyzer.db("initCFGEdgeList() - szEdge " + szEdge ); // XXX
			int iSrcId = Integer.valueOf( szEdge.split(":")[0]).intValue();

//Analyzer.db("initCFGEdgeList() - iSrcId " + String.valueOf(iSrcId) ); // XXX
/*
			int srcId = controlFlow.getBlockIdContainingInsId( iSrcId );
			if( 0 > srcId ){
				srcId = START;
			}
/*/
			int srcId = START;
			if( 0 <= iSrcId ){
				srcId = controlFlow.getBlockIdContainingInsId( iSrcId );
			}
//Analyzer.db("initCFGEdgeList() - srcId block" + String.valueOf(srcId) ); // XXX
//*/
			int iDstId = Integer.valueOf( szEdge.split(":")[1]).intValue();

//Analyzer.db("initCFGEdgeList() - iDstId " + String.valueOf(iDstId) ); // XXX
/*
			int dstId = controlFlow.getBlockIdContainingInsId( iDstId );
			if( 0 > dstId ){
				dstId = END;
			}
/*/
			int dstId = END;
			if( 0 <= iDstId ){
				dstId = controlFlow.getBlockIdContainingInsId( iDstId );
			}
//Analyzer.db("initCFGEdgeList() - dstId block" + String.valueOf(dstId) ); // XXX

			// stupid check
			if( srcId == dstId) continue;

			NodeWrapper srcNode;
			srcNode = getNodeById( CFGBlockList, srcId);

			NodeWrapper dstNode;
			dstNode = getNodeById( CFGBlockList, dstId );
// FIXME
			CFGEdgeList.add(new Edge( srcNode, dstNode));
		}
	}


	private List<Edge> getNextEdges( NodeWrapper currNode ){
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
			List<Edge> nextEdges = getNextEdges( currNode );
			for( Edge nextEdge : nextEdges ){
				if( !isPassedEdge( passedEdges, nextEdge )){
					stack.push(nextEdge);
				}
			}

			if( stack.isEmpty()){
				// we're done when the stack can't be filled anymore
				break;
			}

			// stack was NOT empty, fetch first candidate...
			Edge currEdge = stack.pop();
			passedEdges.add( currEdge );

			// prepare for update dominator information
			NodeWrapper parentNode = currEdge.getFromNode();
			currNode = currEdge.getToNode();

// FIXME: nestedFor() - b2, b3 and b4 fall out somewhere (e.g. stack?)
			// update dominator information
			currNode.inheritageMerge(parentNode);
		}


		// map CFG to DA
		this.DAedgeList = new ArrayList<Edge>();

		for( int blockId = CFGBlockList.size()-1; blockId > 0; --blockId){

			// find all edges ending at current (and no upward linking, to avoid loop issues)
			currNode = CFGBlockList.get(blockId);
//*
			// check for END connections
			if(currNode.id() == END){
				boolean isNoReturnConnected = true;
				for(Edge edge : CFGEdgeList){
					if(edge.getToNode().id() == END){
						// the nodes may connect to END
						isNoReturnConnected = false;
					}
				}
				
				if(isNoReturnConnected){
					// no connection to END - just continue
					continue; // FIXME, something's wrong with END here
				}
			}
//*/

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
