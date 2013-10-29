package ch.usi.inf.sp.cfg;

/**
 * 
 * @author Lothar Rubusch
 *
 */
public class Edge {
	private NodeWrapper fromNode;
	private NodeWrapper toNode;
	private String dotLabel; // TODO use?!

	public Edge( NodeWrapper fromNode, NodeWrapper toNode){
		if( fromNode.id() == toNode.id() ){
			// this check should not be necessary
			return;
		}
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.dotLabel = "";
	}

	public NodeWrapper getFromNode() {
		return fromNode;
	}

	public NodeWrapper getToNode() {
		return toNode;
	}

	public void dotPrint(){
		final String srcNode;
		if( DiGraph.START == fromNode.id() ){
			srcNode = "nodeS";
		}else{
			srcNode = "node" + fromNode.id();
		}

		String dstNode;
		if( DiGraph.END == toNode.id() ){
			dstNode = "nodeE";
		}else{
			dstNode = "node" + toNode.id();
		}

		System.out.print( "  " + srcNode + " -> " + dstNode);
		if( 0 < dotLabel.length() ){
			System.out.println( "[" + dotLabel +"]" );
		}else{
			System.out.println("");
		}
	}
}
