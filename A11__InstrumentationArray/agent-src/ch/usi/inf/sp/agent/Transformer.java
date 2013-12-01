package ch.usi.inf.sp.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
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

public final class Transformer implements ClassFileTransformer{

	@Override
	public byte[] transform(ClassLoader loader
			, String className
			, Class<?> classBeingRedefined
			, ProtectionDomain protectionDomain
			, byte[] classfileBuffer )
			throws IllegalClassFormatException {
//		System.out.println("## About to transform class <" + loader + ", " + className + ">" );

		if( className.startsWith("Java/")
				|| className.startsWith("sun/")
				|| className.startsWith( "ch/usi/inf/sp/agent/")
				|| className.startsWith( "ch/usi/inf/sp/profiler/")) {

			return classfileBuffer;
		}else{
			return instrument( classfileBuffer );
		}
	}


	private byte[] instrument(byte[] bytes) {
		ClassReader cr = new ClassReader( bytes );
		ClassNode cn = new ClassNode();
		cr.accept( cn, ClassReader.SKIP_FRAMES);
		instrument( cn );
		final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_FRAMES );
		cn.accept( cw );
		return cw.toByteArray();
	}


	private InsnList method_instructions;
	private void instrument(ClassNode cn) {
		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			method_instructions = mn.instructions;

			for( AbstractInsnNode ins = method_instructions.getFirst(); ins != null; ins = ins.getNext() ){

				instr_NEWARRAY(ins);

				instr_ANEWARRAY(ins);

				instr_MULTIANEWARRAY(ins);

			} // for instructions
//			DEBUG_bytecode_method(mn); // XXX
		} // for methods
//		DEBUG_die(); // XXX
	}


	private void instr_NEWARRAY( final AbstractInsnNode ins){

		// INT_INSN : newarray
		if( ins.getOpcode() == Opcodes.NEWARRAY ){
			InsnList patch = new InsnList();

			// DUP - 1. arg: int
			patch.add( new InsnNode( Opcodes.DUP )); // size

			// LDC - 2. arg: string
			String type = String.valueOf(Printer.TYPES[((IntInsnNode) ins).operand]);
			patch.add( new LdcInsnNode( "NEWARRAY, [" + type + ", "));

			// INVOKESTATIC
			patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
				, "ch/usi/inf/sp/profiler/Profiler"
				, "logNewArray"
				, "(ILjava/lang/String;)V" ));

			// insert before ins
			AbstractInsnNode insBefore = ins.getPrevious();
			if(null == insBefore ){
				method_instructions.insert(patch);
			}else{
				method_instructions.insert(insBefore, patch);
			}
		}
	}


	private void instr_ANEWARRAY( final AbstractInsnNode ins){
		// TYPE_INSN : anewarray
		if( ins.getOpcode() == Opcodes.ANEWARRAY ){
			InsnList patch = new InsnList();

			// DUP - 1. arg: int
			patch.add( new InsnNode( Opcodes.DUP ));

			// LDC - 2. arg: string
			String type = String.valueOf(((TypeInsnNode)ins).desc);
			patch.add( new LdcInsnNode( "ANEWARRAY, [" + type + ", "));

			// INVOKESTATIC
			patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
					, "ch/usi/inf/sp/profiler/Profiler"
					, "logANewArray"
					, "(ILjava/lang/String;)V" ));

			// insert before ins
			AbstractInsnNode insBefore = ins.getPrevious();
			if(null==insBefore){
				method_instructions.insert(patch);
			}else{
				method_instructions.insert(insBefore, patch);
			}
		}
	}


//	for( int idx_count=0; idx_count<Integer.valueOf(dimensions); ++idx_count){
		// ISTORE
//		patch.add( new VarInsnNode( Opcodes.ISTORE )); // dimension count
//mn.maxlocalvars
		// ILOAD
//TODO pass array of dim counts as array to the function

		/*
		loop dims
			istore

		Ldc count(=dims)
		newarray XXX

		loop dims
			DUP
			LDC0123
			ILOAD
			IASTORE

		//ldc - nice to have

		Invokestatic
		iload (back to stack f multianewarray)
		//*/

		// ILOAD
//		patch.add( new InsnNode( Opcodes.ILOAD )); // 2. duplicate

//TODO how to adjust the signature of logMultiANewarray to the number of dimensions, do I need to set up an array?
//	}
	private void instr_MULTIANEWARRAY(final AbstractInsnNode ins){

		// MULTIANEWARRAY_INSN : multianewarray
		if( ins.getOpcode() == Opcodes.MULTIANEWARRAY ){
			InsnList patch = new InsnList();

			String dimensions = String.valueOf( ((MultiANewArrayInsnNode) ins).dims );

/*
			for( int idx_count=0; idx_count<Integer.valueOf(dimensions); ++idx_count){
				// ISTORE <count idx by dimension>
				patch.add(new VarInsnNode( Opcodes.ISTORE, idx_count)); // TODO check
			}
//*/

			// LDC - 1. arg: dimensions / String
			patch.add( new LdcInsnNode( dimensions ));


			// NEWARRAY - 2. arg
			patch.add( new IntInsnNode( Opcodes.NEWARRAY, Opcodes.T_INT));
/*
			// loop dims
			for( int idx_count=0; idx_count<Integer.valueOf(dimensions); ++idx_count){
				// DUP
				patch.add( new InsnNode( Opcodes.DUP )); // size

				// LDC <dimension count : int>
				patch.add( new LdcInsnNode( idx_count )); // string to int // TODO

				// ILOAD
				patch.add( new VarInsnNode( Opcodes.ILOAD, idx_count )); 

				// IASTORE - create array element
				// needs: array, index, value on the operand stack
				patch.add( new InsnNode( Opcodes.IASTORE ));
			}
	
			// LDC - text - 3. arg
			String type = String.valueOf( ((MultiANewArrayInsnNode) ins).desc );
			patch.add( new LdcInsnNode( "MULTIANEWARRAY, [" + type + ", " ));
//*/

			// INVOKESTATIC
			patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
				, "ch/usi/inf/sp/profiler/Profiler"
				, "logMultiANewArray"
//				, "(Ljava/lang/String;[ILjava/lang/String;)V"
//				, "(Ljava/lang/String;)V" // 1 arg
				, "(Ljava/lang/String;Ljava/lang/String;)V" // 2 args
				));

/*
			// ILOAD - 2. duplicate back on operand stack
			for( int idx_count=0; idx_count<Integer.valueOf(dimensions); ++idx_count){
				patch.add( new VarInsnNode( Opcodes.ILOAD, idx_count )); // 2. duplicate
			}
//*/

			// insert before ins
			AbstractInsnNode insBefore = ins.getPrevious();
			if(null==insBefore){
				method_instructions.insert(patch);
			}else{
				method_instructions.insert(insBefore, patch);
			}

/*
			System.out.println("*************************************************");
			final InsnList instructions = method_instructions;
			for (int i=0; i<instructions.size(); i++) {
				final AbstractInsnNode instruction = instructions.get(i);
				disassembleInstruction(instruction, i, instructions);
			}
			System.out.println("*************************************************");
//*/
		}
	}


	private void DEBUG_bytecode_method(final MethodNode method){
//	private void DEBUG_bytecode_method(){
		System.out.println("*************************************************");
/*
		MethodNode method = new MethodNode();
		method.instructions = method_instructions;
*/
		disassembleMethod(method);
		System.out.println("*************************************************");
	}

	private void DEBUG_opstack(final AbstractInsnNode ins){
// TODO test
		InsnList patch = new InsnList();

		// DUP
		patch.add( new InsnNode( Opcodes.DUP )); // size

		// LDC
//		String type = String.valueOf(Printer.TYPES[((IntInsnNode) ins).operand]);
//		patch.add( new LdcInsnNode( "NEWARRAY, [" + type + ", "));

		// INVOKESTATIC
		patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
			, "ch/usi/inf/sp/profiler/Profiler"
			, "DEBUG_opstack"
			, "(I)V" ));

		// insert before ins
		AbstractInsnNode insBefore = ins.getPrevious();
		if(null == insBefore ){
			method_instructions.insert(patch);
		}else{
			method_instructions.insert(insBefore, patch);
		}
	}

	private static void DEBUG_die(){
		System.exit(0);
	}

	/**************************************************************************/

	/**
	 * 
	 * @param method
	 * 
	 * @author Matthias.Hauswirth@usi.ch
	 */
	public static void disassembleMethod(final MethodNode method) {
		System.out.println("  Method: "+method.name+method.desc);
		// get the list of all instructions in that method
		final InsnList instructions = method.instructions;
		for (int i=0; i<instructions.size(); i++) {
			final AbstractInsnNode instruction = instructions.get(i);
			disassembleInstruction(instruction, i, instructions);
		}
	}

	/**
	 * Hint: Check out org.objectweb.asm.MethodVisitor to determine which instructions (opcodes)
	 * have which instruction types (subclasses of AbstractInsnNode).
	 * 
	 * E.g. the comment in org.objectweb.asm.MethodVisitor.visitIntInsn(int opcode, int operand) 
	 * shows the list of all opcodes that are represented as instructions of type IntInsnNode.
	 * That list e.g. includes the BIPUSH opcode.
	 */
	public static void disassembleInstruction(final AbstractInsnNode instruction, final int num, final InsnList instructions) {
		final int opcode = instruction.getOpcode();
		final String mnemonic = opcode==-1?"":Printer.OPCODES[instruction.getOpcode()];
		System.out.print(num+":\t"+mnemonic+" ");
		// There are different subclasses of AbstractInsnNode.
		// AbstractInsnNode.getType() represents the subclass as an int.
		// Note:
		// to check the subclass of an instruction node, we can either use:
		//   if (instruction.getType()==AbstractInsnNode.LABEL)
		// or we can use:
		//   if (instruction instanceof LabelNode)
		// They give the same result, but the first one can be used in a switch statement.
		switch (instruction.getType()) {
		case AbstractInsnNode.LABEL: 
			// pseudo-instruction (branch or exception target)
			System.out.print("// label");
			break;
		case AbstractInsnNode.FRAME:
			// pseudo-instruction (stack frame map)
			System.out.print("// stack frame map");
			break;
		case AbstractInsnNode.LINE:
			// pseudo-instruction (line number information)
			System.out.print("// line number information");
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
			if (instruction.getOpcode()==Opcodes.NEWARRAY) {
				// NEWARRAY
				System.out.println(Printer.TYPES[((IntInsnNode)instruction).operand]);
			} else {
				// BIPUSH or SIPUSH
				System.out.println(((IntInsnNode)instruction).operand);
			}
			break;
		case AbstractInsnNode.JUMP_INSN:
			// Opcodes: IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
		    // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
		    // IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
		{
			final LabelNode targetInstruction = ((JumpInsnNode)instruction).label;
			final int targetId = instructions.indexOf(targetInstruction);
			System.out.print(targetId);
			break;
		}
		case AbstractInsnNode.LDC_INSN:
			// Opcodes: LDC.
			System.out.print(((LdcInsnNode)instruction).cst);
			break;
		case AbstractInsnNode.IINC_INSN:
			// Opcodes: IINC.
			System.out.print(((IincInsnNode)instruction).var);
			System.out.print(" ");
			System.out.print(((IincInsnNode)instruction).incr);
			break;
		case AbstractInsnNode.TYPE_INSN:
			// Opcodes: NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
			System.out.print(((TypeInsnNode)instruction).desc);
			break;
		case AbstractInsnNode.VAR_INSN:
			// Opcodes: ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE,
		    // LSTORE, FSTORE, DSTORE, ASTORE or RET.
			System.out.print(((VarInsnNode)instruction).var);
			break;
		case AbstractInsnNode.FIELD_INSN:
			// Opcodes: GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
			System.out.print(((FieldInsnNode)instruction).owner);
			System.out.print(".");
			System.out.print(((FieldInsnNode)instruction).name);
			System.out.print(" ");
			System.out.print(((FieldInsnNode)instruction).desc);
			break;
		case AbstractInsnNode.METHOD_INSN:
			// Opcodes: INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC,
		    // INVOKEINTERFACE or INVOKEDYNAMIC.
			System.out.print(((MethodInsnNode)instruction).owner);
			System.out.print(".");
			System.out.print(((MethodInsnNode)instruction).name);
			System.out.print(" ");
			System.out.print(((MethodInsnNode)instruction).desc);
			break;
		case AbstractInsnNode.MULTIANEWARRAY_INSN:
			// Opcodes: MULTIANEWARRAY.
			System.out.print(((MultiANewArrayInsnNode)instruction).desc);
			System.out.print(" ");
			System.out.print(((MultiANewArrayInsnNode)instruction).dims);
			break;
		case AbstractInsnNode.LOOKUPSWITCH_INSN:
			// Opcodes: LOOKUPSWITCH.
		{
			final List keys = ((LookupSwitchInsnNode)instruction).keys;
			final List labels = ((LookupSwitchInsnNode)instruction).labels;
			for (int t=0; t<keys.size(); t++) {
				final int key = (Integer)keys.get(t);
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);
				System.out.print(key+": "+targetId+", ");
			}
			final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)instruction).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			System.out.print("default: "+defaultTargetId);
			break;
		}
		case AbstractInsnNode.TABLESWITCH_INSN:
			// Opcodes: TABLESWITCH.
		{
			final int minKey = ((TableSwitchInsnNode)instruction).min;
			final List labels = ((TableSwitchInsnNode)instruction).labels;
			for (int t=0; t<labels.size(); t++) {
				final int key = minKey+t;
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);
				System.out.print(key+": "+targetId+", ");
			}
			final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)instruction).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			System.out.print("default: "+defaultTargetId);
			break;
		}
		}		
		System.out.println();
	}
}
