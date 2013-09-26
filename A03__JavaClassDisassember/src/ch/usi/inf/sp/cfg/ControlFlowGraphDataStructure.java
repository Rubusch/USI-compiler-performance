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

/**
 * 
 * @author Lothar Rubusch
 */
public class ControlFlowGraphDataStructure {
// TODO this is a complete mess of data structures, mapping, etc.!
	
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
		// a former forward link connected to this instruction, so we provoke starting a new block
		if( null != ptr){
			if(1 < ptr.size() && null != srctable.get(String.valueOf(idx))){
				ptr = null;
			}
		}

		if( null == ptr){
			// handle int dst to sz dst mapping, for dotty
			dstResolvMap.put(Integer.valueOf(idx), String.valueOf(idx + "> " + mnemonic));

			// start a new block list
			ptr = new ArrayList<String>();
			content.add( ptr );
			if( 0 < idx && !fallthruOpcodes.contains( instructions.get(idx-1).getOpcode() )){
				String src = String.valueOf(idx-1 + "> " + Printer.OPCODES[this.instructions.get(idx-1).getOpcode()]);
				String dst = String.valueOf(idx + "> " + mnemonic);
				fallthruList.add(src + "->" + dst);
			}
		}

		// append current item
		ptr.add( String.valueOf(idx) + "> " + mnemonic );
	}

	public void dottyPrint(){
		System.out.println("\n---");
		if( 0 == content.size() ) return;

		// header
		System.out.println( "digraph G {" );
		System.out.println( "  nodesep=.5" );
		System.out.println( "  rankdir=LR" );
		System.out.println( "  node [shape=record,width=.1,height=.1]" );
		System.out.println( "" ); // mark end of header

		// start node
		System.out.println( "  nodeS [label = \"{ <S> S }\"];" );
		System.out.println( "  nodeE [label = \"{ <E> E }\"];" );

		// body
		for( int idx=0; idx < content.size(); ++idx){
			// block header
			System.out.print("  node" + idx + " [label = \"{ <");
			for( int jdx=0; jdx < content.get(idx).size(); ++jdx){
				System.out.print( content.get(idx).get(jdx) );
				if(jdx < content.get(idx).size() -1 ){
					System.out.print(" | <");
				}
			}
			System.out.print(" }\"];\n");
		}
		System.out.println( "" );

		System.out.println("  nodeS:S -> node0:0");
		// print hashmap as references between groups
		Set set = srctable.entrySet();
		Iterator iter = set.iterator();
		while( iter.hasNext() ){
			Map.Entry me = (Map.Entry) iter.next();
			String []srces = String.valueOf(me.getValue()).split(",");
			String dst = "";
			dst = dstResolvMap.get(Integer.valueOf(String.valueOf(me.getKey())));
			for( String src : srces){
				dottyEdge(src, dst);
			}
		}
		System.out.println("");

		for( String str : fallthruList){
			String src = str.split("->")[0];
			String dst = str.split("->")[1];
			dottyEdge(src, dst);
		}
		System.out.println("");

		// trailer
		System.out.println("  node" + String.valueOf(content.size()-1)
				+ ":" + String.valueOf(content.get(content.size()-1).get(content.get(content.size()-1).size()-1).split(">")[0] )
				+ " -> nodeE:E" );
		System.out.println("}");
	}

	private void dottyEdge(String src, String dst){
		
		String idxDst = "";
		for( int idx = 0; idx < content.size(); ++idx){
			if( content.get(idx).contains(dst)){
				idxDst = String.valueOf( idx);
				break;
			}
		}
		String idxDstIns = dst.split(">")[0];
		String idxSrc = "";
		for( int idx = 0; idx < content.size(); ++idx){
			if( content.get(idx).contains(src)){
				idxSrc = String.valueOf( idx);
				break;
			}
		}
		String idxSrcIns = src.split(">")[0];
		System.out.println( "  node" + idxSrc + ":" + idxSrcIns + " -> node" + idxDst + ":" + idxDstIns );
	}

	private void forward(String src, int dst){
		String srcs;
		if( null != (srcs = srctable.get( String.valueOf(dst) )) ){
			srcs += ",";
		}else{
			srcs = "";
		}
		srcs += src;
		srctable.put( String.valueOf(dst), srcs );
		ptr = null;
	}

	private void backward( String src, int dst){
		// find block index
		int idxBlock = 1;
		for( ; idxBlock < content.size(); ++idxBlock){
			if( dst < Integer.valueOf(content.get(idxBlock).get(0).split("> ")[0] ).intValue()){
				break;
			}
		}

		// started from 1; if run through, the target must be in last (current) block
		--idxBlock;

		// find instruction index, in block
		int idxIns = 0;
		if(dst != Integer.valueOf( content.get(idxBlock).get(idxIns).split("> ")[0]).intValue()){
			// splitting necessary, elem is not at idx 0
			for( idxIns = 1; idxIns < content.get(idxBlock).size(); ++idxIns){
				if( dst == Integer.valueOf( content.get(idxBlock).get(idxIns).split("> ")[0]).intValue()){
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
				secHalfBlock.add( new String( content.get(idxBlock).get(idx) ));
			}

			// clean secHalfElements still in former list
			for( int idx = content.get(idxBlock).size()-1; idx >= idxIns ; --idx){
				content.get(idxBlock).remove(idx);
			}

			// insert new list
			if( secHalfBlock.size() > 0){
				content.add(idxBlock+1, secHalfBlock);
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
		switch( ins.getType() ){
		case AbstractInsnNode.JUMP_INSN:
			final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
			final int dest = instructions.indexOf(targetInstruction);
			if( idx < dest ){
				append( mnemonic, idx);
				forward( new String(idx + "> " + mnemonic), dest );
				return; // already appended
			}else{
				append( mnemonic, idx);
				backward( new String(idx + "> " + mnemonic), dest);
				return;
			}
		case AbstractInsnNode.LABEL:
			// pseudo-instruction (branch or exception target)
			mnemonic = "label";
			break;
		case AbstractInsnNode.FRAME:
			// pseudo-instruction (stack frame map)
			return;
		case AbstractInsnNode.LINE:
			// pseudo-instruction (line number information)
			return;
		}

		// append other type of instructions -1
		append( mnemonic, idx );
	}
}
