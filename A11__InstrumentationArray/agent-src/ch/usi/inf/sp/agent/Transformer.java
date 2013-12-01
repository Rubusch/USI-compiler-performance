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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
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



	private void instr_NEWARRAY( final AbstractInsnNode ins){
		// INT_INSN : newarray
		if( ins.getOpcode() == Opcodes.NEWARRAY ){
			InsnList patch = new InsnList();

			// DUP
			patch.add( new InsnNode( Opcodes.DUP )); // size

			// LDC
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

			// DUP
			patch.add( new InsnNode( Opcodes.DUP )); // size

			// LDC
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


	private int instr_MULTIANEWARRAY( int idx_instr ){
		final AbstractInsnNode ins = method_instructions.get(idx_instr);

		// MULTIANEWARRAY_INSN : multianewarray
		if( ins.getOpcode() == Opcodes.MULTIANEWARRAY ){

			InsnList patch = new InsnList();
			int shift=0;

			String dimensions = String.valueOf( ((MultiANewArrayInsnNode) ins).dims );

			for( int idx_count=0; idx_count<Integer.valueOf(dimensions); ++idx_count){
				// ISTORE
//				patch.add( new VarInsnNode( Opcodes.ISTORE )); // dimension count
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
				patch.add( new InsnNode( Opcodes.ILOAD )); // 2. duplicate

//TODO how to adjust the signature of logMultiANewarray to the number of dimensions, do I need to set up an array?
				shift += 3;
			}

			// LDC - text
			String type = String.valueOf( ((MultiANewArrayInsnNode) ins).desc );
			patch.add( new LdcInsnNode( "MULTIANEWARRAY, [" + type + ", " ));
			shift += 1;

			// LDC - dimensions
			patch.add( new LdcInsnNode( dimensions ));
			shift += 1;

			// INVOKESTATIC
			patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
				, "ch/usi/inf/sp/profiler/Profiler"
				, "logMultiANewArray"
				, "(ILjava/lang/String;)V" ));
			shift += 1;

			// insert before ins
/* TODO
			if( idx == 0 ){
				instructions.insert(patch);
			}else{
				AbstractInsnNode insBefore = instructions.get(idx-1);
				instructions.insert(insBefore, patch);
			}

			// QUICKFIX
			idx += shift; 
//*/
		}
		return idx_instr;
	}


	private InsnList method_instructions;
	private void instrument(ClassNode cn) {
		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			method_instructions = mn.instructions;

//			for( int idx=0; idx<method_instructions.size(); ++idx){
			for( AbstractInsnNode ins = method_instructions.getFirst(); ins != null; ins = ins.getNext() ){

				instr_NEWARRAY(ins);

				instr_ANEWARRAY(ins);

//				idx = instr_MULTIANEWARRAY(idx);
			} // for instructions
		} // for methods
	}
//*/
}
