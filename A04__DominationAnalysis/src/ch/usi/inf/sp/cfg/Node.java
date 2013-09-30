package ch.usi.inf.sp.cfg;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Node {
	private ArrayList<AbstractInsnNode> blockinstructions;
	private Integer Id; // TODO how to identify this node

	public Node( ArrayList<AbstractInsnNode> blockinstructions, final Integer startId){
		this.blockinstructions = blockinstructions;
		this.Id = startId;
	}

	public void dotPrint(){
// TODO check, another label?
//		System.out.println( "  node" + Id + " [label = \"" + Id + "\"]");
// TODO
		System.out.println( ControlFlowGraphExtractor.dotPrintBlock(Id, blockinstructions));
	}

	public Integer id(){
		return Id;
	}
}
