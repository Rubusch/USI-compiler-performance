package ch.usi.inf.sp.cfg;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Node {
	private ArrayList<AbstractInsnNode> instructions;
	private Integer Id; // TODO how to identify this node

	public Node( ArrayList<AbstractInsnNode> instructions, final Integer startId){
		this.instructions = instructions;
		this.Id = startId;
	}

	public void dotPrint(){
// TODO check, another label?
		System.out.println( "  Node" + Id + " [label = \"" + Id + "\"]");
	}
	
	public Integer id(){
		return Id;
	}
}
