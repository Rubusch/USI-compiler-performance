package ch.usi.inf.sp.cfg;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

public class ControlFlowGraphExtractor {
	private List< List<AbstractInsnNode>> blocklist;
	private static InsnList instructions;
	private MethodNode method;
	private List<Integer> forwardJump;
	private List<String> edgeslist; // TODO rename "jumpTable"?
//	private List<String> exceptionlist;
	private Map<Integer, Integer> exceptiontable;
	private List<Integer> omitFallthruList;
	private List<Boolean> isPEI;

	public List<List<AbstractInsnNode>> getBlocklist() {
		return blocklist;
	}

	public List<String> getEdgeslist() {
		return edgeslist;
	}

//	public ControlFlowGraphExtractor( final InsnList instructions ){
	public ControlFlowGraphExtractor( final MethodNode method){
		blocklist = new ArrayList< List<AbstractInsnNode>>();
		blocklist.add(new ArrayList<AbstractInsnNode>());
		this.method = method;
		this.instructions = this.method.instructions;
		this.forwardJump = new ArrayList<Integer>();
		this.edgeslist = new ArrayList<String>();
		this.omitFallthruList = new ArrayList<Integer>();
		{
			// opcodes for goto and jumps
//			final int []iOpcodes = { 167, 168, 169 }; // 169 = ret
			final int []iOpcodes = { 167, 168 };
			for( int iOpcode : iOpcodes){
				omitFallthruList.add(new Integer(iOpcode));
			}
		}
		
		// exception handling
		this.isPEI = new ArrayList<Boolean>();
		this.exceptiontable = new HashMap<Integer, Integer>();

		initInstructions();
	}

	private void branching( int targetIdx, int idx ){
		String dotConnection = "";
		dotConnection += String.valueOf( idx ) + ":" + String.valueOf(targetIdx);
		this.edgeslist.add(dotConnection);

		if( targetIdx < idx ){
			// backward jump
			int idxLastFirstIns = 0;
			for( int listIdx = 1; listIdx < this.blocklist.size()-1; ++listIdx ){
				AbstractInsnNode firstIns = this.blocklist.get(listIdx).get(0);
				int idxFirstIns = this.blocklist.indexOf( firstIns );
				if( targetIdx < idxFirstIns ){

					// we overran one, so the last one is it: go back
					int start = idxLastFirstIns;
					int diff = targetIdx - start;

					// break sublist, and insert new sublist
					List<AbstractInsnNode> sublist = this.blocklist.get(listIdx-1).subList( diff, this.blocklist.get(listIdx-1).size());
					this.blocklist.add( listIdx, new ArrayList<AbstractInsnNode>( sublist ));

					// now remove sublist from old location
					this.blocklist.get( listIdx-1 ).removeAll( this.blocklist.get( listIdx ) );

// TODO is this necessary?
					// fallthrough edge
					this.edgeslist.add(String.valueOf( targetIdx-1 ) + ":" + String.valueOf(targetIdx));

					break;
				}
				idxLastFirstIns = idxFirstIns;
			}

		}else if( targetIdx > idx){
			// forward jump
			this.forwardJump.add(new Integer(targetIdx));
		} // no else: continue with next element
	}

	private void initInstructions(){
// TODO set up exception table - BETTER: add information of exception table to the already maintained jumplist?

		int tryblocklimit = -1; // use a stack here, for nested try/catch
		List<TryCatchBlockNode> trycatchlist = method.tryCatchBlocks;
		for( TryCatchBlockNode trycatch : trycatchlist){
			int start = method.instructions.indexOf((LabelNode) trycatch.start);
			int end = method.instructions.indexOf((LabelNode) trycatch.end);
			this.exceptiontable.put(new Integer(start), new Integer(end));
		}



		boolean branchNextIteration = false;
		for( int idx = 0; idx < this.instructions.size(); ++idx ){

			// get next INSTRUCTION
			AbstractInsnNode ins = this.instructions.get(idx);

			// create new block
			if(true == branchNextIteration){
				blocklist.add(new ArrayList<AbstractInsnNode>());
				branchNextIteration = false;
			}





// EXCEPTIONS
			// exception handling, default FALSE, a PEI: TRUE
			isPEI.add(new Boolean(false));

			if( null != this.exceptiontable.get(new Integer(idx)) ){
				tryblocklimit = this.exceptiontable.get(new Integer(idx));
			}

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
				isPEI.set(idx, new Boolean( true ));
			}

// BRANCHING
			if( ins.getType() == AbstractInsnNode.JUMP_INSN ){
				String dotConnection = "";
				if( !this.omitFallthruList.contains( ins.getOpcode() ) ){
					dotConnection += String.valueOf( idx ) + ":" + String.valueOf( idx+1 );
					this.edgeslist.add(dotConnection);
				}

				LabelNode target = ((JumpInsnNode) ins).label;
				int targetIdx = instructions.indexOf(target);
				branching( targetIdx, idx );

				// provoke a new basic block
				branchNextIteration = true;

			}else if( ins.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN){
				final List<?> keys = ((LookupSwitchInsnNode)ins).keys;
				final List<?> labels = ((LookupSwitchInsnNode)ins).labels;
				for( int t=0; t<keys.size(); t++ ){
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetIdx = instructions.indexOf(targetInstruction);
					branching( targetIdx, idx );
				}

				final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)ins).dflt;
				final int targetIdx = instructions.indexOf(defaultTargetInstruction);
				branching( targetIdx, idx );
				// create a new basic block
				branchNextIteration = true;
			}

			if( !branchNextIteration && isPEI.get(idx).booleanValue()){
				// we have a PEI, and the block is not branched
				// (which actually is implicit, isn't it?)
				
// TODO
//				instructions.getExceptionTable() ???
			}
/*
			if( ins.getType() == AbstractInsnNode.INSN
					|| ins.getType() == AbstractInsnNode.TYPE_INSN
					|| ins.getType() == AbstractInsnNode.FIELD_INSN
					|| ins.getType() == AbstractInsnNode.METHOD_INSN
					|| ins.getType() == AbstractInsnNode.MULTIANEWARRAY_INSN
					|| ins.getType() == AbstractInsnNode.INT_INSN ){
				// groups of PEIs
			}
//*/

// APPEND
			if( -1 < this.forwardJump.indexOf( idx ) && this.blocklist.get( this.blocklist.size() -1 ).size() > 1 ){
				// there was a forward jump to this address
				this.blocklist.add( new ArrayList<AbstractInsnNode>() );
				// fallthrough edge
// TODO are there instructions that cannot fall through here? check!
				this.edgeslist.add(String.valueOf( idx-1 ) + ":" + String.valueOf(idx));
			}

			// append instruction at last position
			blocklist.get( blocklist.size()-1 ).add( ins );
		}
	}

	public void dotPrintCFG(){
		System.out.println("# ---");
		if( 0 == this.blocklist.size() ) return;

		// header
		System.out.println( "digraph G {" );
		System.out.println( "  nodesep=.5" );
		System.out.println( "  node [shape=record,width=.1,height=.1]" );
		System.out.println( "" );

		// start node
		System.out.println( "  nodeS [label = \"{ <S> start }\"];" );
		System.out.println( "  nodeE [label = \"{ <E> end }\"];" );
		System.out.println( "" );

		for( int idx=0; idx < this.blocklist.size(); ++idx){
			System.out.print( dotPrintBlock( idx, blocklist.get(idx)) );
		}

		// connections
		System.out.println( "  nodeS:S -> node0:0" );

		for( int idx = 0; idx < this.edgeslist.size(); ++idx ){
			String str = this.edgeslist.get(idx);
			int idxSrc = Integer.valueOf( str.split(":")[0] ).intValue();
			int idxNodeSrc = insId2NodeId( idxSrc );
			int idxDst = Integer.valueOf( str.split(":")[1] ).intValue();
			int idxNodeDst = insId2NodeId( idxDst );

			System.out.println( "  node" +  idxNodeSrc +":" + idxSrc + " -> node" + idxNodeDst + ":" + idxDst );
		}

		// trailer
// TODO back from RETURN, here just the forelast instruction
		System.out.println("  node" + String.valueOf(blocklist.size()-1)
				+ ":" + String.valueOf(instructions.size()-2)
				+ " -> nodeE:E" );

		System.out.println("}");
	}

	public static String dotPrintBlock( int blockId, List<AbstractInsnNode> blockinstructions ){
		String szBlock = "";
		szBlock += "  node" + blockId;
		szBlock += " [label = \"block" + blockId + " | { <";
		for( int jdx=0; jdx < blockinstructions.size(); ++jdx){
			AbstractInsnNode ins = blockinstructions.get(jdx);
			int opcode = ins.getOpcode();
			szBlock +=  instructions.indexOf( ins ) + "> ";
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
				System.out.println(Printer.OPCODES[ins.getOpcode()]);
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
				szBlock += " | <";
			}
		}
		szBlock += " }\"];\n";

		return szBlock;
	}

	public int insId2NodeId( int insId ){
		int nodeId = 1;
		for( ; nodeId < blocklist.size(); ++nodeId ){
			if( instructions.indexOf( blocklist.get( nodeId ).get(0) ) > insId){
				break;
			}
		}
		--nodeId;
		return nodeId;
	}
}
