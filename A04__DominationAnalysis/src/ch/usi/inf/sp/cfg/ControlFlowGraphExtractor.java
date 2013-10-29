package ch.usi.inf.sp.cfg;


import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

/**
 * Control flow graph extractor is still more stable than the dominator analysis
 * 
 * @author Lothar Rubusch
 *
 */
public class ControlFlowGraphExtractor {
	private final List< List<AbstractInsnNode>> blockList;
	private static InsnList instructions;
	private final List<Integer> forwardJump;
	private final List<String> edgesList;
	private final List<Integer> omitFallthruList;

	// ctor
	public ControlFlowGraphExtractor( final InsnList instructions ){
		blockList = new ArrayList< List<AbstractInsnNode>>();
		blockList.add(new ArrayList<AbstractInsnNode>());
		ControlFlowGraphExtractor.instructions = instructions;
		this.forwardJump = new ArrayList<Integer>();
		this.edgesList = new ArrayList<String>();
		this.omitFallthruList = new ArrayList<Integer>();
		{
			// opcodes for goto and jumps
//			final int []iOpcodes = { 167, 168, 169 }; // 169 = ret
			final int []iOpcodes = { 167, 168 };
			for( int iOpcode : iOpcodes){
				omitFallthruList.add(new Integer(iOpcode));
			}
		}

		initInstructions();
	}

	/*
	 * getter/setter
	 */

	public List<List<AbstractInsnNode>> getBlocklist() {
		return blockList;
	}

	public List<String> getEdgeslist() {
		return edgesList;
	}


	/*
	 * util methods
	 */

	private void edgeslistAdd(int srcidx, int dstidx){
		edgeslistAdd( srcidx, dstidx, "");
	}

	private void edgeslistAdd(int srcidx, int dstidx, String opt){
		String str = String.valueOf( srcidx );
		str += ":" + String.valueOf( dstidx );
		if( 0 < opt.length() ){ str += ":" + opt; }
		edgesList.add(str);
	}

	public int getBlockbyInsId( int insId ){
		if( 0 > insId ){ return insId; }

		int nodeId = 1;
		for( ; nodeId < blockList.size(); ++nodeId ){
			if( instructions.indexOf( blockList.get( nodeId ).get(0) ) > insId){
				break;
			}
		}
		--nodeId;
		return nodeId;
	}

	private void branching( int srcidx, int dstidx ){
		branching( srcidx, dstidx, "");
	}

	private void branching( int srcidx, int dstidx, String opt ){
		edgeslistAdd( srcidx, dstidx, opt);

		if( dstidx < srcidx ){
Analyzer.db("XXX backward jump - there seems to be a minor bug"); // XXX
			// backward jump
/*
			// TODO split backward


/*/
			int idxLastFirstIns = 0;
			for( int listIdx = 1; listIdx < this.blockList.size()-1; ++listIdx ){
				// start index 1 -> for breaking a block we start with the second element
				// end index size-1 -> ???

				AbstractInsnNode firstIns = this.blockList.get(listIdx).get(0);
				int idxFirstIns = this.blockList.indexOf( firstIns );
				if( dstidx < idxFirstIns ){
					// we ultrapassed one, so the last one is it: go back
					int start = idxLastFirstIns;
					int diff = dstidx - start;

					// break sublist, and insert new sublist
					List<AbstractInsnNode> sublist = this.blockList.get(listIdx-1).subList( diff, this.blockList.get(listIdx-1).size());
					this.blockList.add( listIdx, new ArrayList<AbstractInsnNode>( sublist ));

					// now remove sublist from old location
					this.blockList.get( listIdx-1 ).removeAll( this.blockList.get( listIdx ) );

// TODO is this necessary?
					// fallthrough edge
					edgeslistAdd( srcidx-1, dstidx, "label=\"flashback\"");
					break;
				}
				idxLastFirstIns = idxFirstIns;
			}
//*/
		}else if( dstidx > srcidx){
			// forward jump
			this.forwardJump.add(new Integer(dstidx));
		} // no else: jump to next element
	}



	private void initInstructions(){
// FOR
		boolean branchNextIteration = false;
		for( int idx = 0; idx < instructions.size(); ++idx ){
			// get next INSTRUCTION
			AbstractInsnNode ins = instructions.get(idx);

			if( 0 == idx) edgeslistAdd( -1, idx);

			// create new block
			if(true == branchNextIteration){
				blockList.add(new ArrayList<AbstractInsnNode>());
				branchNextIteration = false;
			}

// BRANCHING
			if( ins.getType() == AbstractInsnNode.JUMP_INSN ){
				// conditional jumps
				if( !this.omitFallthruList.contains( ins.getOpcode() ) ){
					edgeslistAdd( idx, idx+1, "label=\"TRUE\"");
				}

				LabelNode target = ((JumpInsnNode) ins).label;
				int targetIdx = instructions.indexOf(target);

				// GOTO instructions
				if( Opcodes.GOTO == ins.getOpcode()){
					branching( idx, targetIdx, "label=\"GOTO\"" );
				}else{
					branching( idx, targetIdx, "label=\"FALSE\"" );
				}

				// provoke a new basic block
				branchNextIteration = true;

			}else if( ins.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN){
				// switch / case
				final List<?> keys = ((LookupSwitchInsnNode)ins).keys;
				final List<?> labels = ((LookupSwitchInsnNode)ins).labels;
				for( int t=0; t<keys.size(); t++ ){
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetIdx = instructions.indexOf(targetInstruction);
					branching( idx, targetIdx );
				}
				final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)ins).dflt;
				final int targetIdx = instructions.indexOf(defaultTargetInstruction);
				branching( idx, targetIdx );

				// provoke a new basic block
				branchNextIteration = true;

			}else if( ins.getType() == AbstractInsnNode.TABLESWITCH_INSN){
				// table switch
				final int minKey = ((TableSwitchInsnNode)ins).min;
				final List<?> labels = ((TableSwitchInsnNode)ins).labels;
				for (int t=0; t<labels.size(); t++) {
					final int key = minKey+t;
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetIdx = instructions.indexOf(targetInstruction);
					branching( idx, targetIdx);
				}
				final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)ins).dflt;
				final int targetIdx = instructions.indexOf(defaultTargetInstruction);
				branching( idx, targetIdx );

				// provoke a new basic block
				branchNextIteration = true;

				
			}else if( ins.getType() == AbstractInsnNode.INSN){
				switch (ins.getOpcode()){
				case Opcodes.IRETURN:
				case Opcodes.LRETURN:
				case Opcodes.FRETURN:
				case Opcodes.DRETURN:
				case Opcodes.ARETURN:
				case Opcodes.RETURN:
					if(1 >= instructions.size() - idx + 1){
						// RETURN at the end - before last instruction or last 
						// instruction, don't split, just connect to E
						edgeslistAdd( idx, -1, "label=\"return\"");
					}else{
						// RETURN within running code, branch
						branching( idx, -1, "label=\"return\"" );
					}
					break;
				}
			}

// APPEND
			if( -1 < forwardJump.indexOf( idx ) && blockList.get( blockList.size() -1 ).size() > 1 ){
				// there was a forward jump to this address
				blockList.add( new ArrayList<AbstractInsnNode>() );

				// forward pointing block fallthrough
				branching( idx-1, idx, "label=\"fallthrou\"");
			}
			// append instruction at last position
			blockList.get( blockList.size()-1 ).add( ins );
		}
	}


/*
 * dot printing
 */

	public void dotPrintCFG(){
		if( 0 == this.blockList.size() ) return;
		Analyzer.echo("# control flow graph analysis");

		// header
		Analyzer.echo( "digraph G {" );
		Analyzer.echo( "  nodesep=.5" );
		Analyzer.echo( "  node [shape=record,width=.1,height=.1]" );

		// start node
		Analyzer.echo( "  nodeS [label = \"{ <S> Start }\"];" );
		Analyzer.echo( "  nodeE [label = \"{ <E> End }\"];" );

		// block nodes
		int lastidx = blockList.size() -1;
		if( 1 == blockList.get(lastidx).size()){
			// final block consists of a single element
			List<AbstractInsnNode> finalblock = blockList.get(lastidx);
			if( finalblock.get(0).getType()== AbstractInsnNode.LABEL){
				// this single element was just a label, so remove it
				// in case this was an infinite loop w/o return
				blockList.remove(lastidx);
			}
		}
		for( int idx=0; idx < blockList.size(); ++idx){
			System.out.print( dotPrintBlock( idx, blockList.get(idx)) );
		}

		// connections
		for( int idx = 0; idx < this.edgesList.size(); ++idx ){
			Analyzer.echo(dotEdges( idx ));
		}

		Analyzer.echo("}");
	}

	private String dotEdges( int idx ){
		String[] szbuf = this.edgesList.get(idx).split(":");
		int idxSrc = Integer.valueOf( szbuf[0] ).intValue();
		int idxNodeSrc = getBlockbyInsId( idxSrc );
		String str = "  ";
		if(0 > idxSrc){
			// START
			str += "nodeS:S";
		}else{
			str += "node" +  idxNodeSrc +":" + idxSrc;
		}

		int idxDst = Integer.valueOf( szbuf[1] ).intValue();
		int idxNodeDst = getBlockbyInsId( idxDst );
		if(0 > idxDst){
			// RETURN
			str += " -> nodeE:E";
		}else{
			str += " -> node" + idxNodeDst + ":" + idxDst;
		}

		if( 2 < szbuf.length ){
			str += "[ " + szbuf[2] + " ]";
		}
		return str;
	}


	public static String dotPrintBlock( int blockId, List<AbstractInsnNode> blockinstructions ){
		String szBlock = "";
		szBlock += "  node" + blockId;
//		szBlock += " [label = \"block" + blockId + " | { <";
		szBlock += " [align=left,label=\"block" + blockId + " | { <";
		int startAddr = instructions.indexOf( blockinstructions.get(0) );

		for( int jdx=0; jdx < blockinstructions.size(); ++jdx){
			AbstractInsnNode ins = blockinstructions.get(jdx);
			int opcode = ins.getOpcode();
			String addr = String.valueOf(startAddr + jdx);
			szBlock += addr + "> " + addr + ": ";

			switch(ins.getType()){
			case AbstractInsnNode.LABEL: 
				// pseudo-instruction (branch or exception target)
				szBlock += "label";
				break;
			case AbstractInsnNode.FRAME:
				// pseudo-instruction (stack frame map)
				szBlock += "stackframemap";
				break;
			case AbstractInsnNode.LINE:
				// pseudo-instruction (line number information)
				szBlock += "linenumber";
				break;
			case AbstractInsnNode.INSN:
				// Opcodes: NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2,
			    // ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0,
				// FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD, FALOAD,
				// DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE,
				// DASTORE, AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP,
				// DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP, IADD, LADD, FADD,
				// DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
				// FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL,
				// LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR,
				// I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B,
				// I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN,
				// FRETURN, DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW,
				// MONITORENTER, or MONITOREXIT.
				// zero operands, nothing to print
				szBlock += Printer.OPCODES[ins.getOpcode()];
				break;
			case AbstractInsnNode.INT_INSN:
				// Opcodes: NEWARRAY, BIPUSH, SIPUSH.
				if( ins.getOpcode()==Opcodes.NEWARRAY) {
					// NEWARRAY
					szBlock += "NEWARRAY";
					szBlock += " ";
					szBlock += Printer.TYPES[((IntInsnNode)ins).operand];
				} else {
					// BIPUSH or SIPUSH
					szBlock += "BIPUSH SIPUSH";
					szBlock += " ";
					szBlock += ((IntInsnNode)ins).operand;
				}
				break;
			case AbstractInsnNode.JUMP_INSN:
				// Opcodes: IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
			    // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
			    // IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
			{
				final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
				final int targetId = instructions.indexOf(targetInstruction);
				szBlock += targetId;
				break;
			}
			case AbstractInsnNode.LDC_INSN:
				// Opcodes: LDC.
				szBlock += ((LdcInsnNode)ins).cst;
				break;
			case AbstractInsnNode.IINC_INSN:
				// Opcodes: IINC.
				szBlock += "IINC";
				szBlock += " ";
				szBlock += ((IincInsnNode)ins).var;
				szBlock += " ";
				szBlock += ((IincInsnNode)ins).incr;
				break;
			case AbstractInsnNode.TYPE_INSN:
				// Opcodes: NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
				szBlock +=  Printer.OPCODES[opcode];
				szBlock += " ";
				szBlock += ((TypeInsnNode)ins).desc;
				break;
			case AbstractInsnNode.VAR_INSN:
				szBlock +=  Printer.OPCODES[opcode];
				szBlock += " ";
				szBlock += ((VarInsnNode) ins).var;
				break;
			case AbstractInsnNode.FIELD_INSN:
				// Opcodes: GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
				szBlock += ((FieldInsnNode)ins).owner;
				szBlock += ".";
				szBlock += ((FieldInsnNode)ins).name;
				szBlock += " ";
				szBlock += ((FieldInsnNode)ins).desc;
				break;
			case AbstractInsnNode.METHOD_INSN:
				// Opcodes: INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC,
			    // INVOKEINTERFACE or INVOKEDYNAMIC.
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
				szBlock += ((MethodInsnNode)ins).owner;
				szBlock += ".";
				String tmp = ((MethodInsnNode)ins).name;
				tmp = tmp.replace('<', '(');
				tmp = tmp.replace('>', ')');
				szBlock += tmp;
				szBlock += " ";
				szBlock += ((MethodInsnNode)ins).desc;
				break;
			case AbstractInsnNode.MULTIANEWARRAY_INSN:
				// Opcodes: MULTIANEWARRAY.
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
				szBlock += ((MultiANewArrayInsnNode)ins).desc;
				szBlock += " ";
				szBlock += ((MultiANewArrayInsnNode)ins).dims;
				break;
			case AbstractInsnNode.LOOKUPSWITCH_INSN:
				// Opcodes: LOOKUPSWITCH.
			{
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
				final List<?> keys = ((LookupSwitchInsnNode)ins).keys;
				final List<?> labels = ((LookupSwitchInsnNode)ins).labels;
				for (int t=0; t<keys.size(); t++) {
					final int key = (Integer)keys.get(t);
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetId = instructions.indexOf(targetInstruction);
					szBlock += key+": "+targetId+", ";
				}
				final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)ins).dflt;
				final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
				szBlock += "default: "+defaultTargetId;
				break;
			}
			case AbstractInsnNode.TABLESWITCH_INSN:
				// Opcodes: TABLESWITCH.
			{
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
				final int minKey = ((TableSwitchInsnNode)ins).min;
				final List<?> labels = ((TableSwitchInsnNode)ins).labels;
				for (int t=0; t<labels.size(); t++) {
					final int key = minKey+t;
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetId = instructions.indexOf(targetInstruction);
					szBlock += key+": "+targetId+", ";
				}
				final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)ins).dflt;
				final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
				szBlock += "default: "+defaultTargetId;
				break;
			}
			}// end

			if(jdx < blockinstructions.size() -1 ){
				szBlock += "\\l | <";
			}
		}
		szBlock += "\\l }\"];\n";

		return szBlock;
	}
}
