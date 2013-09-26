package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.HashMap;

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

	private void append( final String szIns, final int idx ){
		// new block or start
		if( null == ptr){
			ptr = new ArrayList<String>();
		}

		// start
		if( 0 == content.size() ){
			content.add( ptr );
			ptr.add( "S" );
			srctable.put( String.valueOf(idx), "S" );
		}

		// append current item
		ptr.add( String.valueOf(idx) + ":" + szIns );
	}

	public void printDotty(){
		ControlFlowGraphExtractor.die("XXX"); // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

		if( 0 == content.size() ) return;

		// header
// TODO
		
		for( int idx=0; idx < content.size(); ++idx){
			// block header
// TODO
			for( int jdx=0; jdx < content.get(idx).size(); ++jdx){
				System.out.println( content.get(idx).get(jdx) );
// TODO test
			}
			// block trailer
// TODO
		}

		// print hashmap as references between groups
// TODO

		// trailer
// TODO
	}

	private void forward(){
		// TODO
	}
	
	private void backward(){
		// TODO
	}

	public void appendInstruction( final AbstractInsnNode ins, final int idx, final InsnList instructions ){
		final int opcode = ins.getOpcode();
		final String mnemonic = (opcode==-1 ? "" : Printer.OPCODES[ins.getOpcode()]);

		// start new list, when either in srctable.get(ins.getOpcode()) is not null, or for 'if' instrs
		// if
		// TODO
//		System.out.println("XXX ins " + ins.getType() + ", mnemonic " + mnemonic);

		// append to basicblocklist or start new basic block, when 
		if( AbstractInsnNode.JUMP_INSN == ins.getType() ){
			// extract dest addr, idx will be source

			final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
			final int dest = instructions.indexOf(targetInstruction);
			if( idx < dest ){
				// forward
				srctable.put( String.valueOf(dest), String.valueOf(idx) );
				ptr = null;
// TODO
			}else{
				// backward
// TODO check in other lists to where it points, break the list there in two lists (insert into content list)
			}
			System.out.println( "TODO" );
		}
// TODO
		append( mnemonic, idx );
	}

	
}
