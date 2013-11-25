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
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class Transformer implements ClassFileTransformer{

	@Override
	public byte[] transform(ClassLoader loader
			, String className
			, Class<?> classBeingRedefined
			, ProtectionDomain protectionDomain
			, byte[] classfileBuffer )
			throws IllegalClassFormatException {
		System.out.println("XXX About to transform class <" + loader + ", " + className + ">" );

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
		// instrument method calls by INVOKESTATIC
		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			InsnList patch = new InsnList();
			patch.add( new LdcInsnNode( "Method " + mn.name + mn.desc + " called" ));
			patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V" ));
		}
//*/

		 // INT_INSN : newarray
		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			final InsnList instructions = mn.instructions;
			for( int idx=0; idx<instructions.size(); ++idx){
				final AbstractInsnNode ins = instructions.get(idx);

// TODO extract as separate method
				if( ins.getType() == AbstractInsnNode.INT_INSN ){
					if( ins.getOpcode() == Opcodes.NEWARRAY ){
						// TODO
						InsnList patch = new InsnList();
						patch.add( new LdcInsnNode( "Newarray called"));
						patch.add( new MethodInsnNode( Opcodes.NEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V" ));
					}
				}
			}
		}
	}

		 // TYPE_INSN : anewarray
// TODO

		 // MULTIANEWARRAY_INSN : multianewarray
// TODO

/*
		// filter array instructions
		// TODO IntInsnNode ?
		// TODO MultiANewArrayInsnNode ?
		for( FieldNode fn : (List<FieldNode>)cn.fields ){ // TODO "fields?"
//		for( IntNode in : (List<IntInsnNode>)cn.fileds ){
			InsnList patch  = new InsnList();
			patch.add( new LdcInsnNode( "NewArray " + fn.name + fn.desc + " called" ));
			patch.add( new FieldInsnNode( Opcodes.NEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V")); // TODO check

//			patch.add( new FieldInsnNode( Opcodes.ANEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V")); // TODO check
//			patch.add( new FieldInsnNode( Opcodes.MULTIANEWARRAY, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V")); // TODO check

			fn.instructions.insert(patch); // FIXME
		}
//		patch.add( new LdcInsnNode( "NewArray " + mn.name + mn.desc + ));
	}
//*/
}
