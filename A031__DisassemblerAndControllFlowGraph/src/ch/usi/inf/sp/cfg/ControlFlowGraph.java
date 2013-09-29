package ch.usi.inf.sp.cfg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class ControlFlowGraph {
	public static void die( String msg ){
		System.out.println( msg );
		System.exit(-1);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String classFileName = "";
		try{
			classFileName = args[0];
		}catch( Exception exp ){
			die( "check class file name (first argument)");
		}

		String methodNameAndDescriptor = "";
		try{
			methodNameAndDescriptor = args[1];
		}catch( Exception exp ){
			die("check method name and descriptor (second argument)");
		}

		final ClassReader cr = new ClassReader( new FileInputStream( classFileName ));
		final ClassNode cnode = new ClassNode();

		cr.accept(cnode, 0);

		ControlFlowGraph control = new ControlFlowGraph();
		control.flow( cnode, methodNameAndDescriptor );

		System.out.println( "\n# READY.");
	}

	private void flow( ClassNode cnode, String methodNameAndDescriptor ){
		final List<MethodNode> methods = cnode.methods;
		for( int idx=0; idx<methods.size(); ++idx){
			final MethodNode method = methods.get(idx);
			if( methodNameAndDescriptor.equals( method.name ) ){
				flowMethod(method);
			}
		}
	}


	/**
	 * analysis and data structure per method
	 * 
	 * @param method
	 */
	private void flowMethod( final MethodNode method ){
		final InsnList instructions = method.instructions;
		ControlFlowGraphExtractor controlFlow = new ControlFlowGraphExtractor( instructions );
/*
		for( int idx=0; idx<instructions.size(); ++idx){
			final AbstractInsnNode instruction = instructions.get(idx);
			controlFlow.appendInstruction(instruction, idx);
		}
//*/
		controlFlow.dottyPrint();
	}
}