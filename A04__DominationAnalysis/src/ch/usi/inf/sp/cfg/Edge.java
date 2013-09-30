package ch.usi.inf.sp.cfg;

public class Edge {
	private Integer fromNode;
	private Integer toNode;
	private String dotLabel;

	public Edge( Integer fromNodeId, Integer toNodeId, String label ){
		fromNode = fromNodeId;
		toNode = toNodeId;
		dotLabel = label;
	}

	public Edge( Integer fromNodeId, Integer toNodeId ){
		fromNode = fromNodeId;
		toNode = toNodeId;
	}

// TODO needed?
	public Integer getFromNode() {
		return fromNode;
	}

// TODO needed?
	public Integer getToNode() {
		return toNode;
	}

	public void dotPrint(){
		String srcNode = "node" + fromNode;
		String dstNode = "node" + toNode;
		System.out.print( srcNode + " -> " + dstNode);
		if( 0 < dotLabel.length() ){
			System.out.println( "[" + dotLabel +"]" );
		}else{
			System.out.println("");
		}
	}
}
