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
//			Analyzer.db("XXX controlFlow element '" + szEdge + "'" ); // TODO rm

			int iSrcId = Integer.valueOf( szEdge.split(":")[0]).intValue();
//			Analyzer.db("XXX iSrcId " + String.valueOf(iSrcId) ); // TODO rm
			int srcId = controlFlow.insId2NodeId( iSrcId );
			if( 0 > srcId ){
				srcId = START;
			}
//			Analyzer.db("XXX srcId " + String.valueOf(srcId) ); // TODO rm
					
			int iDstId = Integer.valueOf( szEdge.split(":")[1]).intValue();
//			Analyzer.db("XXX iDstId " + String.valueOf(iDstId) ); // TODO rm
			int dstId = controlFlow.insId2NodeId( iDstId );
			if( 0 > dstId ){
				dstId = END;
			}
//			Analyzer.db("XXX dstId " + String.valueOf(dstId) ); // TODO rm

			// stupid check
			if( srcId == dstId) continue;

			NodeWrapper srcNode;
			srcNode = getNodeById( CFGBlockList, srcId);

			NodeWrapper dstNode;
			dstNode = getNodeById( CFGBlockList, dstId );

//			Analyzer.db("initCFGEdgeList( srcNode " + String.valueOf(srcId) + ", dstNode " + String.valueOf(dstId) + ")"); // TODO rm
			CFGEdgeList.add(new Edge( srcNode, dstNode));
		}
	}
	

// FIXME user END 
	public DiGraph(ControlFlowGraphExtractor controlFlow){
		initCFGBlockList( controlFlow );
		initCFGEdgeList( controlFlow );

/******************************************************************************/
		// CFG prepared
		NodeWrapper currNode = getNodeById(CFGBlockList, START);

		List<Integer> passedIds = new ArrayList<Integer>();
		Stack<Edge> stack = new Stack<Edge>();

		// init inheritage list with START
		List<List<Integer>> inheritageList = new ArrayList<List<Integer>>();
		List<Integer> inheritageElement = new ArrayList<Integer>();
		inheritageElement.add(START);
		inheritageList.add(inheritageElement);
		currNode.inheritageInit(inheritageList);

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
			for( Edge edge : edges ){
				parentNodeList.add(edge.getFromNode());
			}
			if( 1 == parentNodeList.size()){
				// pass heritage from a signle node
				currNode.inheritageInit( parentNodeList.get(0).getInheritage() );
			}else{
				// merge heritage from several parents together
				currNode.inheritageMerge(parentNodeList);
			}
		}

/******************************************************************************/
		// map CFG to DA
		this.DAedgeList = new ArrayList<Edge>();

//		Analyzer.db("YYY size of CFGBlockList " + String.valueOf(CFGBlockList.size())); // TODO rm

		for( int blockId = CFGBlockList.size()-1; blockId > 0; --blockId){
//			Analyzer.db("YYY blockId '" + String.valueOf(blockId) + "'"); // TODO rm

			// find all edges ending at current (and no upward linking, to avoid loop issues)
			NodeWrapper currDA = CFGBlockList.get(blockId);
			if(currDA.id() == END){
				boolean isNoReturnConnetcted = true;
				
				for(Edge edge : CFGEdgeList){
					if(edge.getToNode().id() == END){
						Analyzer.db("THIS IS THE END");
						isNoReturnConnetcted = false;
					}
				}
				
				if(isNoReturnConnetcted){
					continue;
				}
			}

//			Analyzer.db("YYY mapping CFG to DA: currDA.id() " + String.valueOf(currDA.id()) ); // TODO rm

			Integer idxidom = currDA.getIDom();
			
//			Analyzer.db("YYY idxidom '" + String.valueOf(idxidom) + "'"); // TODO rm
			final NodeWrapper idom;
			if( START == idxidom){
//				idom = new NodeWrapper(null, START); // TODO rm
				idom = new NodeWrapper(START);
			}
/*			else if( END == idxidom){
				idom = new NodeWrapper( END );
			}
//*/
			else{
				idom = CFGBlockList.get(idxidom);
			}
// FIXME
			if(idom.id() != currDA.id()){
				DAedgeList.add( new Edge(idom, currDA) ); // idom may be currDA?!!
			}
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
