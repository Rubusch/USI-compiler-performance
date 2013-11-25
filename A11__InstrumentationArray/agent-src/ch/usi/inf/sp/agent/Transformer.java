package ch.usi.inf.sp.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

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
		System.out.println("## About to transform class <" + loader + ", " + className + ">" );

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



	private void instrument(ClassNode cn) {
/*
// legacy - TODO rm
		// instrument method calls by INVOKESTATIC
		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			InsnList patch = new InsnList();
			patch.add( new LdcInsnNode( "Method " + mn.name + mn.desc + " called" ));
			patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V" ));
		}
//*/

		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			final InsnList instructions = mn.instructions;
			for( int idx=0; idx<instructions.size(); ++idx){
				final AbstractInsnNode ins = instructions.get(idx);

				// INT_INSN : newarray
				if( ins.getType() == AbstractInsnNode.INT_INSN ){
					if( ins.getOpcode() == Opcodes.NEWARRAY ){
// TODO does Profiler should write actually a CSV file?
// TODO do we need to read out arguments (size, type, etc - if so, where? )?
// TODO  - if we need to read out the arguments, do we need to store predeceding BIPUSH args?
						
// TODO is this necessary, what is this code actually good for? Why do we need a separate "Profiler", when we can print out directly here?
//						InsnList patch = new InsnList();
//						patch.add( new LdcInsnNode( "NewArray "+Printer.TYPES[((IntInsnNode)ins).operand]+" called"));
//						patch.add( new MethodInsnNode( Opcodes.NEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V" ));
						System.out.println("NEWARRAY");
					}

				// TYPE_INSN : anewarray
				}else if( ins.getType() == AbstractInsnNode.TYPE_INSN ){
					if( ins.getOpcode() == Opcodes.ANEWARRAY ){
// TODO dito
//						InsnList patch = new InsnList();
//						patch.add( new LdcInsnNode( "ANewArray " + ((TypeInsnNode)ins).desc + " called"));
//						patch.add( new MethodInsnNode( Opcodes.ANEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V" ));
						System.out.println("ANEWARRAY");
					}

				 // MULTIANEWARRAY_INSN : multianewarray
				}else if( ins.getType() == AbstractInsnNode.MULTIANEWARRAY_INSN){
					if( ins.getOpcode() == Opcodes.MULTIANEWARRAY ){
// TODO is this necessary
						;
// TODO what do we need to handle here?
						System.out.println("MULTIANEWARRAY");
					}
				}

			}
		}
	}
}
