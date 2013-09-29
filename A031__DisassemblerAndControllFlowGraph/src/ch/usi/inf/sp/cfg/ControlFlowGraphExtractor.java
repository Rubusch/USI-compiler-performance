package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.HashMap;
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

public class ControlFlowGraphExtractor {
	private ArrayList< ArrayList<AbstractInsnNode>> listlist;
	private InsnList instructions;
	private ArrayList<Integer> forwardJump;
	private ArrayList<String> dotJump;
	private ArrayList<Integer> omitFallthruList;

	public ControlFlowGraphExtractor( final InsnList instructions ){
		listlist = new ArrayList< ArrayList<AbstractInsnNode>>();
		listlist.add(new ArrayList<AbstractInsnNode>());
		this.instructions = instructions;
		this.forwardJump = new ArrayList<Integer>();
		this.dotJump = new ArrayList<String>();
		this.omitFallthruList = new ArrayList<Integer>();
		{
			// opcodes for goto and jumps
//			final int []iOpcodes = { 167, 168, 169 };
			final int []iOpcodes = { 167, 168 };
			for( int iOpcode : iOpcodes){
				omitFallthruList.add(new Integer(iOpcode));
			}
		}

		initInstructions();
	}

	private void initInstructions(){
//	public void appendInstruction( final AbstractInsnNode ins, final int idx ){
		boolean branchNextIteration = false;
		for( int idx = 0; idx < this.instructions.size(); ++idx ){
			AbstractInsnNode ins = this.instructions.get(idx);
			String dotConnection = "";
// TODO test
			if(true == branchNextIteration){
				listlist.add(new ArrayList<AbstractInsnNode>());
				branchNextIteration = false;
			}

			if( ins.getType() == AbstractInsnNode.JUMP_INSN ){
				LabelNode target = ((JumpInsnNode) ins).label;
				int targetIdx = instructions.indexOf(target);
				dotConnection += String.valueOf( idx ) + ":" + String.valueOf(targetIdx);
				this.dotJump.add(dotConnection);
				dotConnection = "";
				if( !this.omitFallthruList.contains( ins.getOpcode() ) ){
					dotConnection += String.valueOf( idx ) + ":" + String.valueOf( idx+1 );
					this.dotJump.add(dotConnection);
				}

				if( targetIdx < idx ){
					// backward jump
					int idxLastFirstIns = 0;
//					for( int listIdx = 1; listIdx < this.listlist.size(); ++listIdx ){
					for( int listIdx = 1; listIdx < this.listlist.size()-1; ++listIdx ){
						// TODO test

						int idxFirstIns = this.listlist.indexOf( this.listlist.get(listIdx).get(0) );
						if( targetIdx < idxFirstIns ){
							int start = idxLastFirstIns;
							int diff = targetIdx - start;
							this.listlist.add( listIdx, new ArrayList<AbstractInsnNode>( this.listlist.get(listIdx-1).subList( diff, this.listlist.get(listIdx-1).size())));
							this.listlist.get( listIdx-1 ).removeAll( this.listlist.get( listIdx ) );
							break;
						}
						idxLastFirstIns = idxFirstIns;
					}
				}else if( targetIdx > idx){
					// forward jump
					this.forwardJump.add(new Integer(targetIdx));
				} // no else: jump to next element

				// create a new basic block
/*
				listlist.add(new ArrayList<AbstractInsnNode>());
/*/
				branchNextIteration = true;
//*/
			}

			// append
			if( -1 < this.forwardJump.indexOf( idx ) && this.listlist.get( this.listlist.size() -1 ).size() > 1 ){
				// there was a forward jump to this address
				this.listlist.add( new ArrayList<AbstractInsnNode>() );
			}

			// append instruction at last position
			listlist.get( listlist.size()-1 ).add( ins );

//			listlist.add(new ArrayList<AbstractInsnNode>()); // XXX
		}
	}

	public void dottyPrint(){
		System.out.println("\n# ---");
		if( 0 == this.listlist.size() ) return;

		// header
		System.out.println( "digraph G {" );
		System.out.println( "  nodesep=.5" );
		System.out.println( "  rankdir=LR" );
		System.out.println( "  node [shape=record,width=.1,height=.1]" );
		System.out.println( "" );

		// start node
		System.out.println( "  nodeS [label = \"{ <S> S }\"];" );
		System.out.println( "  nodeE [label = \"{ <E> E }\"];" );
		System.out.println( "" );

//*
		for( int idx=0; idx < this.listlist.size(); ++idx){
			System.out.print("  node" + idx + " [label = \"{ <");
			for( int jdx=0; jdx < this.listlist.get(idx).size(); ++jdx){
//				int opcode = this.listlist.get(idx).get(jdx).getOpcode();
				AbstractInsnNode ins = this.listlist.get(idx).get(jdx);
				int opcode = ins.getOpcode();

//				System.out.print( this.instructions.indexOf( this.listlist.get(idx).get(jdx)) + "> " );
				System.out.print( this.instructions.indexOf( ins ) + "> " );
				switch(ins.getType()){
//				if( -1 < opcode){
				case AbstractInsnNode.LABEL: 
					// pseudo-instruction (branch or exception target)
					System.out.print("label");
					break;
				case AbstractInsnNode.FRAME:
					// pseudo-instruction (stack frame map)
					System.out.print("stackframemap");
					break;
				case AbstractInsnNode.LINE:
					// pseudo-instruction (line number information)
					System.out.print("linenumber");
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
					break;
				case AbstractInsnNode.INT_INSN:
					// Opcodes: NEWARRAY, BIPUSH, SIPUSH.
					if( ins.getOpcode()==Opcodes.NEWARRAY) {
						// NEWARRAY
						System.out.print(Printer.TYPES[((IntInsnNode)ins).operand]);
					} else {
						// BIPUSH or SIPUSH
						System.out.print(((IntInsnNode)ins).operand);
					}
					break;
				case AbstractInsnNode.JUMP_INSN:
					// Opcodes: IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
				    // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
				    // IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
				{
					final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
					final int targetId = instructions.indexOf(targetInstruction);
					System.out.print(targetId);
					break;
				}
				case AbstractInsnNode.LDC_INSN:
					// Opcodes: LDC.
					System.out.print(((LdcInsnNode)ins).cst);
					break;
				case AbstractInsnNode.IINC_INSN:
					// Opcodes: IINC.
					System.out.print(((IincInsnNode)ins).var);
					System.out.println(" ");
					System.out.print(((IincInsnNode)ins).incr);
					break;
				case AbstractInsnNode.TYPE_INSN:
					// Opcodes: NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
					System.out.print(((TypeInsnNode)ins).desc);
					break;
				case AbstractInsnNode.VAR_INSN:
					System.out.print( Printer.OPCODES[opcode] );
					System.out.print(" ");
					System.out.print(((VarInsnNode) ins).var );
					break;
				case AbstractInsnNode.FIELD_INSN:
					// Opcodes: GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
					System.out.print(((FieldInsnNode)ins).owner);
					System.out.print(".");
					System.out.print(((FieldInsnNode)ins).name);
					System.out.print(" ");
					System.out.print(((FieldInsnNode)ins).desc);
					break;
				case AbstractInsnNode.METHOD_INSN:
					// Opcodes: INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC,
				    // INVOKEINTERFACE or INVOKEDYNAMIC.
					System.out.print(((MethodInsnNode)ins).owner);
					System.out.print(".");
					System.out.print(((MethodInsnNode)ins).name);
					System.out.print(" ");
					System.out.print(((MethodInsnNode)ins).desc);
					break;
				case AbstractInsnNode.MULTIANEWARRAY_INSN:
					// Opcodes: MULTIANEWARRAY.
					System.out.print(((MultiANewArrayInsnNode)ins).desc);
					System.out.print(" ");
					System.out.print(((MultiANewArrayInsnNode)ins).dims);
					break;
				case AbstractInsnNode.LOOKUPSWITCH_INSN:
					// Opcodes: LOOKUPSWITCH.
				{
					final List keys = ((LookupSwitchInsnNode)ins).keys;
					final List labels = ((LookupSwitchInsnNode)ins).labels;
					System.out.print("LOOKUPSWITCH ");
// TODO is this actually branching?
					for (int t=0; t<keys.size(); t++) {
						final int key = (Integer)keys.get(t);
						final LabelNode targetInstruction = (LabelNode)labels.get(t);
						final int targetId = instructions.indexOf(targetInstruction);
						System.out.print(key+": "+targetId+", ");
					}
					final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)ins).dflt;
					final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
					System.out.print("default: "+defaultTargetId);
					break;
				}
				case AbstractInsnNode.TABLESWITCH_INSN:
					// Opcodes: TABLESWITCH.
				{
					final int minKey = ((TableSwitchInsnNode)ins).min;
					final List labels = ((TableSwitchInsnNode)ins).labels;
					for (int t=0; t<labels.size(); t++) {
						final int key = minKey+t;
						final LabelNode targetInstruction = (LabelNode)labels.get(t);
						final int targetId = instructions.indexOf(targetInstruction);
						System.out.print(key+": "+targetId+", ");
					}
					final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)ins).dflt;
					final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
					System.out.print("default: "+defaultTargetId);
					break;
				}
				}// end
			
// FIXME: connections
				if(jdx < this.listlist.get(idx).size() -1 ){
					System.out.print(" | <");
				}
			}
			System.out.print(" }\"];\n");
		}
		System.out.println( "" );

		// connections
		System.out.println( "  nodeS:S -> node0:0" );

		for( int idx = 0; idx < this.dotJump.size(); ++idx ){
			String str = this.dotJump.get(idx);

			int idxSrc = Integer.valueOf( str.split(":")[0] ).intValue();

			int idxNodeSrc = 0;
			for( int idxNode = 1; idxNode < this.listlist.size(); ++idxNode ){
				if( this.instructions.indexOf( this.listlist.get( idxNode ).get(0) ) > idxSrc){
					idxNodeSrc = idxNode-1;
					break;
				}
			}

			int idxDst = Integer.valueOf( str.split(":")[1] ).intValue();

			int idxNodeDst = 0;
			for( int idxNode = 1; idxNode < this.listlist.size(); ++idxNode ){
				if( this.instructions.indexOf( this.listlist.get( idxNode ).get(0) ) > idxDst){
					idxNodeDst = idxNode-1;
					break;
				}
			}

			System.out.println( "  node" +  idxNodeSrc +":" + idxSrc + " -> node" + idxNodeDst + ":" + idxDst );
		}
		System.out.println( "" );

		// trailer
// TODO back from RETURN, here just the forelast instruction
		System.out.println("  node" + String.valueOf(listlist.size()-1)
				+ ":" + String.valueOf(instructions.size()-2)
				+ " -> nodeE:E" );

		System.out.println("}");
	}
}
