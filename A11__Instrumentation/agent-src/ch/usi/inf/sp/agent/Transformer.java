package ch.usi.inf.sp.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class Transformer implements ClassFileTransformer{

	@Override
	public byte[] transform(ClassLoader loader
			, String className
			, Class<?> classBeingRedefined
			, ProtectionDomain protectionDomain
			, byte[] classfileBuffer)
			throws IllegalClassFormatException {
		System.out.println("About to transform class <" + loader + ", " + className + ">" );

		// transformer instruments
		return instrument( classfileBuffer );
	}



	private byte[] instrument(byte[] bytes) {
		ClassReader cr = new ClassReader( bytes );
		ClassNode cn = new ClassNode();
		cr.accept(cn, ClassReader.SKIP_FRAMES);

		instrument(cn);

		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cn.accept(cw);
		return cw.toByteArray();
	}



	private void instrument(ClassNode cn) {
		for( MethodNode mn : (List<MethodNode>)cn.methods) {
			InsnList patch = new InsnList();
			patch.add( new LdcInsnNode("Method " + mn.name + mn.desc + " called" ));
			patch.add( new MethodInsnNode( Opcodes.INVOKESTATIC, "ch/usi/inf/sp/profiler/Profiler", "log", "(Ljava/lang/String;)V"));
			mn.instructions.insert(patch);
		}
	}
}
