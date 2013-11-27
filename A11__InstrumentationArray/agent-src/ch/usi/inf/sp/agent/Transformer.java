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


/*
	private void instrument(ClassNode cn) {
		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			final InsnList instructions = mn.instructions;
			for( int idx=0; idx<instructions.size(); ++idx){
				final AbstractInsnNode ins = instructions.get(idx);

				// INT_INSN : newarray
				if( ins.getOpcode() == Opcodes.NEWARRAY ){
					InsnList patch = new InsnList();

					String type = String.valueOf(Printer.TYPES[((IntInsnNode) ins).operand]);  // BEWARE: if this cast casts to a wrong type, nothing will happen!
					patch.add( new LdcInsnNode( "NEWARRAY, [" + type + ", "));

					patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
							, "ch/usi/inf/sp/profiler/Profiler"
							, "log"
							, "(Ljava/lang/String;)V"));

					instructions.insert(ins, patch);

				// TYPE_INSN : anewarray
				}else if( ins.getOpcode() == Opcodes.ANEWARRAY ){
					InsnList patch = new InsnList();

					String type = String.valueOf(((TypeInsnNode)ins).desc); // BEWARE: if this cast casts to a wrong type, nothing will happen!
					patch.add( new LdcInsnNode( "ANEWARRAY, [" + type + ", "));

					patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
							, "ch/usi/inf/sp/profiler/Profiler"
							, "log"
							, "(Ljava/lang/String;)V" ));

					instructions.insert(ins, patch);

				}else if( ins.getOpcode() == Opcodes.MULTIANEWARRAY ){
					InsnList patch = new InsnList();

					String type = String.valueOf( ((MultiANewArrayInsnNode) ins).desc );  // BEWARE: if this cast casts to a wrong type, nothing will happen!
					String dimensions = String.valueOf( ((MultiANewArrayInsnNode) ins).dims );
					patch.add( new LdcInsnNode( "MULTIANEWARRAY, [" + type + ", " + dimensions));

					patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
							, "ch/usi/inf/sp/profiler/Profiler"
							, "log"
							, "(Ljava/lang/String;)V" ));

					instructions.insert(ins, patch);
				}
			} // instructions
		} // methods
	}

/*/
	private void instrument(ClassNode cn) {

		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			final InsnList instructions = mn.instructions;
			for( int idx=0; idx<instructions.size(); ++idx){
				final AbstractInsnNode ins = instructions.get(idx);

				// INT_INSN : newarray
				if( ins.getOpcode() == Opcodes.NEWARRAY ){
					InsnList patch = new InsnList();

///////////////
					// DUP
					patch.add( new InsnNode( Opcodes.DUP )); // size

					// LDC
					String type = String.valueOf(Printer.TYPES[((IntInsnNode) ins).operand]);
					patch.add( new LdcInsnNode( "NEWARRAY, [" + type + ", "));

					// INVOKESTATIC
					patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
							, "ch/usi/inf/sp/profiler/Profiler"
//							, "log"
//							, "(Ljava/lang/String;)V" ));
							, "logNewArray"
							, "(ILjava/lang/String;)V" ));


					// insert STRING - INVOKESTATIC after ins
					AbstractInsnNode insBefore = instructions.get(idx-1);
					instructions.insert(insBefore, patch);

					// QUICKFIX: move 3 positions
					idx += 3; 



				// TYPE_INSN : anewarray
				}else if( ins.getType() == AbstractInsnNode.TYPE_INSN ){
					if( ins.getOpcode() == Opcodes.ANEWARRAY ){

//						InsnList patch = new InsnList();
//						patch.add( new LdcInsnNode( "ANewArray " + ((TypeInsnNode)ins).desc + " called"));
//						patch.add( new MethodInsnNode( Opcodes.ANEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V" ));
//
//						String type = String.valueOf(((TypeInsnNode)ins).desc);
//						System.out.println("ANEWARRAY, " + type + ", " + bipushed);
						;
					}

				 // MULTIANEWARRAY_INSN : multianewarray
				}else if( ins.getType() == AbstractInsnNode.MULTIANEWARRAY_INSN){
					if( ins.getOpcode() == Opcodes.MULTIANEWARRAY ){
//						String type = String.valueOf( ((MultiANewArrayInsnNode) ins).desc );
//						String dimensions = String.valueOf( ((MultiANewArrayInsnNode) ins).dims );
//						String sizes = "";
//
//						for( int idx_sizes=0; idx_sizes < Integer.valueOf(dimensions)-1; ++idx_sizes){ // FIXME -1 - why?
//							sizes += ", ";
//							sizes += bipusher.pop();
//						}
//
//						System.out.println("MULTIANEWARRAY, " + type + ", " + dimensions + sizes);
						;
					}
				}
			}
		}
	}
//*/
}
