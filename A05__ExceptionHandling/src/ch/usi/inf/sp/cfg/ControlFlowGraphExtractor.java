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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

/**
 *
 * @author Lothar Rubusch
 *
 */
// FIXME INVOKE calls may throw exceptions - test
// FIXME NEWARRAYs may throw exceptions - test
public class ControlFlowGraphExtractor {
	private List< List<AbstractInsnNode>> blockList;
	private static InsnList instructions;
	private MethodNode method;
	private List<Integer> forwardJump;
	private List<String> edgesList;
	private List<Integer> omitFallthruList;

	private final ExceptionTable exceptionTable	;

	/*
	 * ctor
	 */
	public ControlFlowGraphExtractor( final MethodNode method){
		blockList = new ArrayList< List<AbstractInsnNode>>();
		blockList.add(new ArrayList<AbstractInsnNode>());
		this.method = method;
		ControlFlowGraphExtractor.instructions = this.method.instructions;
		this.forwardJump = new ArrayList<Integer>();
		this.edgesList = new ArrayList<String>();
		this.omitFallthruList = new ArrayList<Integer>();
		{
			// opcodes for goto and jumps
//			final int []iOpcodes = { 167, 168, 169 }; // TODO 169 = ret
			final int []iOpcodes = { 167, 168 };
			for( int iOpcode : iOpcodes){
				omitFallthruList.add(new Integer(iOpcode));
			}
		}

		// exception handling
		exceptionTable = new ExceptionTable();

		initInstructions();
	}


	/*
	 * getter / setter
	 */
	public List<List<AbstractInsnNode>> getBlocklist() {
		return blockList;
	}

	public List<String> getEdgeslist() {
		return edgesList;
	}


	/*
	 * utilities
	 */
	private void edgeslistAdd(int idxSrc, int idxDst){
		edgeslistAdd( idxSrc, idxDst, "");
	}

	private void edgeslistAdd(int idxSrc, int idxDst, String opt){
		String str = String.valueOf( idxSrc );
		str += ":" + String.valueOf( idxDst );
		if( 0 < opt.length() ){ str += ":" + opt; }
		edgesList.add(str);
	}

	public int getBlockIdContainingInsId( int idxIns ){
		int blockId = -1;
		int idxBlock = -1;
		for( idxBlock=0; idxBlock < blockList.size(); ++idxBlock ){

			// get first ins id from block
			AbstractInsnNode firstIns = blockList.get(idxBlock).get(0);
			int idxFirstIns = instructions.indexOf( firstIns );

			// if we overrun, pick last or block0
			if( idxIns < idxFirstIns){ // overrun, pick last
				if( idxBlock > 0 ){
					// case 1: idxIns is in block0
					idxBlock--;
				}

				// case 2: idxIns is within elements
				blockId = idxBlock;
				break;
			}
		}

		// case 3: idxIns is in last block
		if( blockList.size() == idxBlock){
			blockId = idxBlock -1;
		}

		return blockId;
	}

	private void branching( int idxSrc, int idxDst ){
		branching( idxSrc, idxDst, "");
	}

	private void branching( int idxSrc, int idxDst, String opt){
		edgeslistAdd( idxSrc, idxDst, opt);

		if( idxDst < idxSrc && idxDst > 0 ){
			// jump back, idxDst < idxSrc, and idxDst is not END (= -1 )
			int idxBlock = getBlockIdContainingInsId( idxDst );
			List<AbstractInsnNode> block = blockList.get(idxBlock);

			// split block at idxDst
			int start = instructions.indexOf(block.get(0));
			int startSplit = idxDst - start;
			int endSplit = block.size();

			if( 0 == startSplit){
				// don't split if this is a backjump to the 0th element of block
				return;
			}

			// split into sublist, and insert new sublist
			List<AbstractInsnNode> blockBottomHalf = block.subList( startSplit, endSplit);

			// clone sublist and append it
			List<AbstractInsnNode> blockNew = new ArrayList<AbstractInsnNode>();
			for( AbstractInsnNode ins : blockBottomHalf){ blockNew.add(ins); }

			// insert newBlock after block
			if(blockList.size() > idxBlock+1){
				// insert
				blockList.add( idxBlock+1, blockNew );
			}else{
				// append, block was currently last block
				blockList.add( blockNew );
			}

			// remove sublist elements from old location block
			block.removeAll( blockNew );

			// fallthrou link, before cut to after cut
			edgeslistAdd( idxDst-1, idxDst, "label=\"fallthrou\"");

		}else if( idxDst > idxSrc){
			// forward jump
			this.forwardJump.add(new Integer(idxDst));
		} // no else: continue with next element
	}

	private void initInstructions(){
		exceptionTable.initTable(method.tryCatchBlocks, ControlFlowGraphExtractor.instructions); // TODO
		exceptionTable.initStates(instructions);
//		exceptionTable.printExceptionTable(); // debugging
//		exceptionTable.printStateTable(); // debugging

		boolean branchNextIteration = false;

		// avoid return fallthrou instructions
		int lastReturn = -1;

// FOR
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
					lastReturn = idx;
					break;
				}
			}


			if( EState.TRYING == exceptionTable.state(idx) ){
				// types of instructions of PEIs are:
				// ins.getType() == AbstractInsnNode.INSN
				// ins.getType() == AbstractInsnNode.TYPE_INSN
				// ins.getType() == AbstractInsnNode.FIELD_INSN
				// ins.getType() == AbstractInsnNode.METHOD_INSN
				// ins.getType() == AbstractInsnNode.MULTIANEWARRAY_INSN
				// ins.getType() == AbstractInsnNode.INT_INSN
				// check current instruction being escaped
				if( isPEI(ins) ){
					// fallthrou
					if( !this.omitFallthruList.contains( ins.getOpcode() )){
						branching( idx, idx+1, "label=\"fallthrou PEI\"");
					}

					// PEI branching
					if( exceptionTable.isHavingFinally(idx)){
						List<Integer> furtherHandlers = exceptionTable.getFurtherHandlers(idx);
						for(int idxHandlers=0; idxHandlers < furtherHandlers.size(); ++idxHandlers){
							Integer handlerAddr = furtherHandlers.get(idxHandlers);
							branching( idx, handlerAddr, "label=\"PEI\",style=dotted" );
						}
					}else{
						branching( idx, exceptionTable.getNextHandler(idx), "label=\"PEI\",style=dotted" );
					}

					// start new block
					branchNextIteration = true;
				}

			}else if( isHandler(idx)){
				if( exceptionTable.isHavingFinally(idx)){
					branching( idx, exceptionTable.getNextHandler(idx), "label=\"catching to finally\",style=dotted" );

					// start new block
					branchNextIteration = true;
				} // else try-catch, normal ending
			}

// APPEND
			if( -1 < forwardJump.indexOf( idx ) && blockList.get( blockList.size()-1 ).size() > 1 ){

				// there was a forward jump to this address
				blockList.add( new ArrayList<AbstractInsnNode>() );

				// fallthrou edge
				AbstractInsnNode lastIns = instructions.get(idx-1);

				// ATHROW, re-throw an exception which cannot be handeled
				if( Opcodes.ATHROW == lastIns.getOpcode()){
					branching( idx-1, instructions.size(), "label=\"ATHROW\"" );
				}else{
					// forward pointing block fallthrough
					if( (idx-1) != lastReturn){
						// forward pointing block fallthrough
						branching( idx-1, idx, "label=\"forward fallthrou\"");
					}
				}
			}
			// append instruction at last position
			blockList.get( blockList.size()-1 ).add( ins );
		}
	}


	private boolean isPEI(final AbstractInsnNode ins){
		switch (ins.getOpcode()) {
		case Opcodes.AALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.AASTORE: // NullPointerException, ArrayIndexOutOfBoundsException, ArrayStoreException
		case Opcodes.ANEWARRAY: // NegativeArraySizeException, (linking)
		case Opcodes.ARETURN: // IllegalMonitorStateException (if synchronized)
		case Opcodes.ARRAYLENGTH: // NullPointerException
		case Opcodes.ATHROW: // NullPointerException, IllegalMonitorStateException (if synchronized),
		case Opcodes.BALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.BASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.CALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.CASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.CHECKCAST: // ClassCastException, (linking)
		case Opcodes.DALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.DASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.DRETURN: // IllegalMonitorStateException (if synchronized)
		case Opcodes.FALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.FASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.FRETURN: // IllegalMonitorStateException (if synchronized)
		case Opcodes.GETFIELD: // NullPointerException, (linking)
		case Opcodes.GETSTATIC: // Error*, (linking)
		case Opcodes.IALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.IASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.IDIV: // ArithmeticException
		case Opcodes.INSTANCEOF: // (linking)
		case Opcodes.INVOKEDYNAMIC: // what's this??
		case Opcodes.INVOKEINTERFACE: // NullPointerException, IncompatibleClassChangeError, AbstractMethodError, IllegalAccessError, AbstractMethodError, UnsatisfiedLinkError, (linking)
		case Opcodes.INVOKESPECIAL: // NullPointerException, UnsatisfiedLinkError, (linking)
		case Opcodes.INVOKESTATIC: // UnsatisfiedLinkError, Error*, (linking)
		case Opcodes.INVOKEVIRTUAL: // NullPointerException, AbstractMethodError, UnsatisfiedLinkError, (linking)
		case Opcodes.IREM: // ArithmeticException
		case Opcodes.IRETURN: // IllegalMonitorStateException (if synchronized)
		case Opcodes.LALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.LASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.LDIV: // ArithmeticException
		case Opcodes.LREM: // ArithmeticException
		case Opcodes.LRETURN: // IllegalMonitorStateException (if synchronized)
		case Opcodes.MONITORENTER: // NullPointerException
		case Opcodes.MONITOREXIT: // NullPointerException, IllegalMonitorStateException
		case Opcodes.MULTIANEWARRAY: // NegativeArraySizeException, (linking)
		case Opcodes.NEW: // Error*, (linking)
		case Opcodes.NEWARRAY: // NegativeArraySizeException
		case Opcodes.PUTFIELD: // NullPointerException, (linking)
		case Opcodes.PUTSTATIC: // Error*, (linking)
		case Opcodes.RETURN: // IllegalMonitorStateException (if synchronized)
		case Opcodes.SALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
		case Opcodes.SASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
			return true;
		}
		return false;
	}

	private boolean isHandler(int idx){
		return exceptionTable.isHandlerAddr(idx);
	}

	public void dotPrintCFG(){
		if( 0 == this.blockList.size() ) return;
		Analyzer.echo("# control flow graph analysis");

		// header
		Analyzer.echo("digraph G {");
		Analyzer.echo("  nodesep=.5");
		Analyzer.echo("  node [shape=record,width=.1,height=.1]");

		// start node
		Analyzer.echo("  nodeS [label = \"{ <S> start }\"];");
		Analyzer.echo("  nodeE [label = \"{ <E> end }\"];");

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
		for( int idxBlock=0; idxBlock < this.blockList.size(); ++idxBlock){
			Analyzer.echo(dotPrintBlock( idxBlock, blockList.get(idxBlock)));
		}

		// connections
		Analyzer.echo("  nodeS:S -> node0:0");
		for( int idx = 0; idx < this.edgesList.size(); ++idx ){
			Analyzer.echo(dotEdges( idx ));
		}

		// trailer
		Analyzer.echo("}");
	}

	private String dotEdges( int idx ){
		String[] szbuf = this.edgesList.get(idx).split(":");
		int idxSrc = Integer.valueOf( szbuf[0] ).intValue();
		int idxNodeSrc = getBlockIdContainingInsId( idxSrc );
		String str = "  ";
		if(0 > idxSrc){
			// START
			str += "nodeS:S";
		}else{
			str += "node" +  idxNodeSrc +":" + idxSrc;
		}

		int idxDst = Integer.valueOf( szbuf[1] ).intValue();
		int idxNodeDst = getBlockIdContainingInsId( idxDst );
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
//*/

	public static String dotPrintBlock( int blockId, List<AbstractInsnNode> blockinstructions ){
		String szBlock = "";
		szBlock += "  node" + blockId;
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
				szBlock += Printer.OPCODES[ins.getOpcode()];//			if( -1 < opcode){
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
				final List<?> keys = ((LookupSwitchInsnNode)ins).keys;
				final List<?> labels = ((LookupSwitchInsnNode)ins).labels;
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
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
				szBlock += Printer.OPCODES[ins.getOpcode()];
				szBlock += " ";
			{
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
		szBlock += "\\l }\"];";

		return szBlock;
	}
}
