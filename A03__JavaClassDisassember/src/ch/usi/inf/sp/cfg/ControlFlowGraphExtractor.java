package ch.usi.inf.sp.cfg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;


/**
 * This class extracts a control flow graph in .dot format
 * from the byte code of a Java method.
 * 
 * @author Lothar Rubusch
 */
public final class ControlFlowGraphExtractor {
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final String classFileName = args[0];

// TODO what shall we do with the.. ??
		final String methodNameAndDescriptor = args[1];

		final ClassReader cr = new ClassReader( new FileInputStream( classFileName ));
		final ClassNode cnode = new ClassNode();

		cr.accept(cnode, 0);

		ControlFlowGraphExtractor control = new ControlFlowGraphExtractor();
		control.flow( cnode );

		System.out.println( "\nREADY.");
	}

	private void flow( ClassNode cnode ){
		final List<MethodNode> methods = cnode.methods;
		for( int idx=0; idx<methods.size(); ++idx){
			final MethodNode method = methods.get(idx);
			flowMethod(method);
		}
	}

	/**
	 * analysis and data structure per method
	 * 
	 * @param method
	 */
	private void flowMethod( final MethodNode method ){
		ControlFlowGraphDataStructure instrblocks = new ControlFlowGraphDataStructure();

		final InsnList instructions = method.instructions;
		for( int idx=0; idx<instructions.size(); ++idx){
			final AbstractInsnNode instruction = instructions.get(idx);
			instrblocks.appendInstruction(instruction, idx, instructions);
		}
	}
}
