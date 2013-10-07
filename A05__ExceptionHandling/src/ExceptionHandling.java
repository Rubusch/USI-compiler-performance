import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
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


public class ExceptionHandling {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
/*
		public class C {
			public void m(Exception ex) throws Exception {
				throw ex;
			}
		}



		int i=0;
		final AbstractInsnNode ins = instructions.get(i);
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
				isPEI[i] = true;















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



//*/
	}
}
