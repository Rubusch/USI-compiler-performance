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
		// instrument static method in profile by INVOKESTATIC
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
				if( ins.getOpcode() == Opcodes.NEWARRAY ){
// TODO does Profiler should write actually a CSV file?
// TODO do we need to read out arguments (size, type, etc - if so, where? )?
// TODO  - if we need to read out the arguments, do we need to store predeceding BIPUSH args?
// TODO is this necessary, what is this code actually good for? Why do we need a separate "Profiler", when we can print out directly here?
					InsnList patch = new InsnList();

					String type = String.valueOf(Printer.TYPES[((IntInsnNode) ins).operand]);
// FIXME a dup removes the rest, somehow
//					patch.add( new InsnNode( Opcodes.DUP ));   // size
					patch.add( new LdcInsnNode( "NEWARRAY, " + type + ", "));
					patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC
							, "ch/usi/inf/sp/profiler/Profiler"
							, "log"
//							, "logNewArray"
							, "(Ljava/lang/String;)V" ));
					instructions.insert(ins, patch);
/*
						String type = String.valueOf(Printer.TYPES[((IntInsnNode) ins).operand]);
						System.out.println("NEWARRAY, " + type + ", " + bipushed);
					}else if( ins.getOpcode() == Opcodes.BIPUSH ){
						bipusher.push( String.valueOf( ((IntInsnNode) ins).operand ) );
//*/
				}
/*
				// TYPE_INSN : anewarray
				}else if( ins.getType() == AbstractInsnNode.TYPE_INSN ){
					if( ins.getOpcode() == Opcodes.ANEWARRAY ){
// TODO dito
						InsnList patch = new InsnList();
						patch.add( new LdcInsnNode( "ANewArray " + ((TypeInsnNode)ins).desc + " called"));
						patch.add( new MethodInsnNode( Opcodes.ANEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V" ));

						String bipushed = bipusher.pop();
						String type = String.valueOf(((TypeInsnNode)ins).desc);
						System.out.println("ANEWARRAY, " + type + ", " + bipushed);
					}

				 // MULTIANEWARRAY_INSN : multianewarray
				}else if( ins.getType() == AbstractInsnNode.MULTIANEWARRAY_INSN){
					if( ins.getOpcode() == Opcodes.MULTIANEWARRAY ){
// TODO what do we need to handle here?
						String type = String.valueOf( ((MultiANewArrayInsnNode) ins).desc );
						String dimensions = String.valueOf( ((MultiANewArrayInsnNode) ins).dims );
						String sizes = "";

						for( int idx_sizes=0; idx_sizes < Integer.valueOf(dimensions)-1; ++idx_sizes){ // FIXME -1 - why?
							sizes += ", ";
							sizes += bipusher.pop();
						}
// FIXME entries are missing, why?
						System.out.println("MULTIANEWARRAY, " + type + ", " + dimensions + sizes);
					}
				}
//*/
			}
		}
	}
}
