package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.util.Printer;

// TODO perhaps better inherit from ArrayList< ArrayList >
public class ControlFlowGraphDataStructure {
	// table of jump point sources, key = destination, value = source
	private HashMap< String, String > srctable;

	// data, list of list
	private ArrayList< ArrayList< String > > content;
	private ArrayList< String > ptr;

	public ControlFlowGraphDataStructure(){
		srctable = new HashMap< String, String >();
		content = new ArrayList< ArrayList< String >>();
// TODO
	}

// TODO first idx, then szIns
	private void append( final String mnemonic, final int idx ){
		// new block or start
		if( null == ptr){
			ptr = new ArrayList<String>();
			content.add( ptr );
		}

		// initial
		if( 0 == content.size() ){
			content.add( ptr );
			ptr.add( "S" );
			srctable.put( String.valueOf(idx), "S" );

			ptr = null;
			ptr = new ArrayList<String>();
			content.add( ptr );
		}

		// append current item
		ptr.add( String.valueOf(idx) + ":" + mnemonic );
	}

	public void printDotty(){
		System.out.println("\n---");
//		ControlFlowGraphExtractor.die("XXX"); // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

		if( 0 == content.size() ) return;

// TODO better use String / Stringbuffer instead of this ;)

		// header
		System.out.println("digraph G {");
		System.out.println("  nodesep=.5");
		System.out.println("  rankdir=LR");
		System.out.println("  node [shape=record,width=.1,height=.1]");
		System.out.println( "" ); // mark end of header

		// body
		for( int idx=0; idx < content.size(); ++idx){
			// block header
			System.out.print("  node" + idx + " [label = \"{<n> ");
//			System.out.print("  node" + idx + " [label = \"{ ");
			for( int jdx=0; jdx < content.get(idx).size(); ++jdx){
				System.out.print( content.get(idx).get(jdx) );
				if(jdx < content.get(idx).size() -1 ){
					System.out.print(" | ");
				}
			}
			System.out.print(" | <p> }\"];\n");
//			System.out.print(" }\"];\n");
		}
		System.out.println( "" ); // mark end of body

		// trailer
		// print hashmap as references between groups
		Set set = srctable.entrySet();
		Iterator iter = set.iterator();
		while( iter.hasNext() ){
			Map.Entry me = (Map.Entry) iter.next();
			System.out.println("  " + me.getValue() + " -> " + me.getKey());
		}
		System.out.println("}");
	}

	private void forward(int src, int dst){
		// handle forward jumps
		srctable.put( String.valueOf(dst), String.valueOf(src) );
		ptr = null;
	}

	private void backward(int src, int dst){
// TODO check by comparing hash list if ge, then, go into corresponding list, find target node, and split list (insert second part after, w/ hashtable entry)
		ptr = null;
	}

	public void appendInstruction( final AbstractInsnNode ins, final int idx, final InsnList instructions ){
		final int opcode = ins.getOpcode();
		final String mnemonic = (opcode==-1 ? "" : Printer.OPCODES[ins.getOpcode()]);

// TODO rm
		// start new list, when either in srctable.get(ins.getOpcode()) is not null, or for 'if' instrs
		System.out.println("XXX ins " + ins.getType() + ", mnemonic " + mnemonic);

		// append to basicblocklist or start new basic block, when 
		if( AbstractInsnNode.JUMP_INSN == ins.getType() ){
			System.out.println( "XXX JUMP_INSN found ---" ); // TODO rm

			// target jump addr
			final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
			final int dest = instructions.indexOf(targetInstruction);
			if( idx < dest ){
				append( mnemonic, idx);
				forward( idx, dest);
			}else{
				append( mnemonic, idx);
				backward( idx, dest);
			}
			return;
		}
		append( mnemonic, idx );
	}
}
