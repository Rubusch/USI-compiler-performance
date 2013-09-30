package ch.usi.inf.sp.cfg;

public class Edge {
//	private Integer fromNode;
	private Node fromNode;
//	private Integer toNode;
	private Node toNode;
	private String dotLabel;

//	public Edge( Integer fromNodeId, Integer toNodeId, String label ){
	public Edge( Node fromNode, Node toNode, String dotLabel){
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.dotLabel = dotLabel;
	}

//	public Edge( Integer fromNodeId, Integer toNodeId ){
	public Edge( Node fromNode, Node toNode){
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.dotLabel = "";
	}

// TODO needed?
	public Node getFromNode() {
		return fromNode;
	}

// TODO needed?
	public Node getToNode() {
		return toNode;
	}

	public void dotPrint(){
		String srcNode = "node" + fromNode.id();
		String dstNode = "node" + toNode.id();
		System.out.print( "  " + srcNode + " -> " + dstNode);
		if( 0 < dotLabel.length() ){
			System.out.println( "[" + dotLabel +"]" );
		}else{
			System.out.println("");
		}
	}
}
