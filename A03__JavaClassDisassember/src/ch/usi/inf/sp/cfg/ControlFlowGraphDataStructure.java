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

/**
 * 
 * @author Lothar Rubusch
 */
public class ControlFlowGraphDataStructure {
// TODO change map key = src -> val = dst, but one src, may point to several dsts, as also one dst may have different srcs
// TODO find better datastructure	
	
	// table of jump point sources, key = destination, value = source
	private HashMap< String, String > srctable;

	// data, list of list
	private ArrayList< ArrayList< String > > content;
	private ArrayList< String > ptr;

	// table of dst int (keys) -> sz (values)
	private HashMap< Integer, String > dstResolvMap;
	
	// list of readymade strings for dotty for fallthrough connections for if_ instructions
	private ArrayList< String > fallthruList;

	// list of opcodes NOT to be considered with fallthrough property (e.g. GOTO)
	private ArrayList<Integer> fallthruOpcodes;

	// the asm instruction list
	private final InsnList instructions;

	public ControlFlowGraphDataStructure( final InsnList instructions ){
		this.instructions = instructions;

		srctable = new HashMap< String, String >();
		content = new ArrayList< ArrayList< String >>();
		dstResolvMap = new HashMap< Integer, String>();
		fallthruList = new ArrayList< String >();
		fallthruOpcodes = new ArrayList<Integer>();
		{
			// opcodes for goto and jumps
			final int []iOpcodes = { 167, 168, 169 };
			for( int iOpcode : iOpcodes){
				fallthruOpcodes.add(new Integer(iOpcode));
			}
		}
	}

	private void append( final String mnemonic, final int idx ){
		// initial
		if( 0 == content.size() ){
			// first entry, the start link
			ptr = new ArrayList<String>();
			ptr.add( "S" );
			content.add( ptr );
			ptr = null;

			// update source hashmap (for forward)
			srctable.put( String.valueOf(idx), "S" );

			// just link start
			String dst = String.valueOf(idx + ":" + mnemonic);

			// no fallthruList for 'S' is covered by srcTable
		}

		// a former forward link connected to this instruction, so we provoke starting a new block
		if( null != ptr){
			if(1 < ptr.size() && null != srctable.get(String.valueOf(idx))){
				ptr = null;
			}
		}


		if( null == ptr){
			// this starts a new block

			// handle int dst to sz dst mapping, for dotty
			dstResolvMap.put(Integer.valueOf(idx), String.valueOf(idx + ":" + mnemonic));

			// start a new block list
			ptr = new ArrayList<String>();
			content.add( ptr );

			if( 0 < idx && !fallthruOpcodes.contains( instructions.get(idx).getOpcode() )){
				String src = String.valueOf(idx-1 + ":" + Printer.OPCODES[this.instructions.get(idx-1).getOpcode()]);
				String dst = String.valueOf(idx + ":" + mnemonic);
				fallthruList.add(src + " -> " + dst);
			}
		}

		// append current item
		ptr.add( String.valueOf(idx) + ":" + mnemonic );
	}

	public void printDotty(){
		System.out.println("\n---");
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
			String []srces = String.valueOf(me.getValue()).split(",");

// TODO improve this by a hashmap
			String dst = "";

			if( null == (dst = dstResolvMap.get(Integer.valueOf(String.valueOf(me.getKey()))))){
				dst = "TODO_" + me.getKey();
				// TODO if dst == null?			
			}

			for( String src : srces){
				System.out.println("  " + src + " -> " + dst);
			}

		}
		for( String str : fallthruList){
//System.out.println("XXX size of fallthrulist " + fallthruList.size());
			System.out.println("  " + str);
		}
		System.out.println("}");
	}

	private void forward(String src, int dst){
		String vals;
		if( null != (vals = srctable.get( String.valueOf(dst) )) ){
			vals += ",";
		}else{
			vals = "";
		}
		vals += src;

//System.out.println( "XXX null issue, vals " + vals ); // TODO rm
		srctable.put( String.valueOf(dst), vals );
		ptr = null;
	}

// TODO rn backwardJump
	private void backward( String src, int dst){
//		System.out.println( "XXX BACKWARD - src: " + src + ", dst: " + dst); // TODO rm
// TODO check by comparing hash list if ge, then, go into corresponding list, find target node, and split list (insert second part after, w/ hashtable entry)

		// find block index
		int idxBlock = 1;
		for( ; idxBlock < content.size(); ++idxBlock){
			if( dst < Integer.valueOf(content.get(idxBlock).get(0).split(":")[0] ).intValue()){
				break;
			}
		}

		// started from 1; if run through, the target must be in last (current) block
		--idxBlock;

		// find instruction index, in block
		int idxIns = 0;
		if(dst != Integer.valueOf( content.get(idxBlock).get(idxIns).split(":")[0]).intValue()){
			// splitting necessary, elem is not at idx 0
// TODO test
			for( idxIns = 1; idxIns < content.get(idxBlock).size(); ++idxIns){
//				System.out.println( "XXX BACKWARD - " + content.get(idxBlock).get(idxIns).split(":")[0] );
				if( dst == Integer.valueOf( content.get(idxBlock).get(idxIns).split(":")[0]).intValue()){
					break;
				}
			}

			// checks
			if( idxIns == content.get(idxBlock).size()){
				ControlFlowGraphExtractor.die("something went wrong, refering a lower index that was not parsed already?! ");
			}

			// split block at idxIns (first half)
			ArrayList< String > secHalfBlock = new ArrayList< String >();
			for( int idx=idxIns; idx < content.get(idxBlock).size(); ++idx){
//				secHalfBlock.add(content.get(idxBlock).get(idx));
				secHalfBlock.add( new String( content.get(idxBlock).get(idx) ));
// TODO check
			}

			// clean secHalfElements still in former list
			for( int idx = content.get(idxBlock).size()-1; idx >= idxIns ; --idx){
				content.get(idxBlock).remove(idx);
			}

			// insert new list
			if( secHalfBlock.size() > 0){
//				System.out.println( "AAA secHalfBlock.size() " + secHalfBlock.size() );
				content.add(idxBlock+1, secHalfBlock);

				System.out.println("XXX BACKWARD - check first val of secHalf: " + content.get(idxBlock+1).get(0)); // TODO rm
				dstResolvMap.put(Integer.valueOf(idxIns), content.get(idxBlock+1).get(0));
			}
		} // splitting was necessary

		// updating table
		String vals = srctable.get( String.valueOf(dst) );
		if( null != vals ){
			vals += ",";
		}else{
			vals = "";
		}
		vals += src;

		srctable.put( String.valueOf(dst), vals );

		ptr = null;
	}

	public void appendInstruction( final AbstractInsnNode ins, final int idx ){
		final int opcode = ins.getOpcode();
		String mnemonic = (opcode==-1 ? "" : Printer.OPCODES[this.instructions.get(idx).getOpcode()]);
// FIXME some instructions seem not to be in the OPCODES table

// TODO rm
		// start new list, when either in srctable.get(ins.getOpcode()) is not null, or for 'if' instrs
//		System.out.println("XXX ins " + ins.getType() + ", mnemonic " + mnemonic);

		// append to basicblocklist or start new basic block, when 
		switch( ins.getType() ){
		case AbstractInsnNode.JUMP_INSN:
//		if( AbstractInsnNode.JUMP_INSN == ins.getType() ){ // TODO rm
//			System.out.println( "XXX JUMP_INSN found" ); // TODO rm

			// target jump addr
			final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
			final int dest = instructions.indexOf(targetInstruction);
			if( idx < dest ){
				append( mnemonic, idx);
				forward( new String(idx + ":" + mnemonic), dest );
				return; // already appended
			}else{
				append( mnemonic, idx);
				backward( new String(idx + ":" + mnemonic), dest);
				return;
			}

		//		}else if( AbstractInsnNode.LABEL == ins.getType() ){ // TODO rm
		case AbstractInsnNode.LABEL:
			// pseudo-instruction (branch or exception target)
			mnemonic = "label";
			break;
//*
		case AbstractInsnNode.FRAME:
			// pseudo-instruction (stack frame map)
			return;
//			mnemonic = "stack-frame-map";
//			break;
		case AbstractInsnNode.LINE:
			// pseudo-instruction (line number information)
			return;
//			mnemonic = "line-number-information";
//			break;
//*/
		}
		
		// append other type of instructions and nops (-1)
		append( mnemonic, idx );
	}
}
