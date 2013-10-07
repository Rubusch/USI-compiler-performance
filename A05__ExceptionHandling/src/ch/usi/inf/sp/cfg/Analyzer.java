package ch.usi.inf.sp.cfg;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;


public class Analyzer {
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

		final ClassReader cr = new ClassReader( new FileInputStream( classFileName ));
		final ClassNode cnode = new ClassNode();
		cr.accept(cnode, 0);
		Analyzer control = new Analyzer();


		String methodNameAndDescriptor = "";
		try{
			methodNameAndDescriptor = args[1];
			control.flow( cnode, methodNameAndDescriptor );
		}catch( Exception exp ){
			// no specific method provided, do all methods
			control.flow( cnode );			
		}
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

	private void flow( ClassNode cnode ){
		final List<MethodNode> methods = cnode.methods;
		for( int idx=0; idx<methods.size(); ++idx){
			flowMethod(methods.get(idx));
		}
	}

	/**
	 * analysis and data structure per method
	 * 
	 * @param method
	 */
	private void flowMethod( final MethodNode method ){
		System.out.println("\n# " + method.name);

//		System.out.println( "XXX " + method.exceptions );
//		System.out.println( "XXX " + method.tryCatchBlocks );
//		System.out.println( "XXX " + method.tryCatchBlocks.get(0) );

		System.out.println( "XXX " + method.tryCatchBlocks.get(0) );

		List<TryCatchBlockNode> trycatch = method.tryCatchBlocks;
		System.out.println( "AAA number of try-catch " + String.valueOf(trycatch.size()));
		System.out.println( "BBB element 0 is " + String.valueOf( trycatch.get(0)));
		System.out.println( "CCC start " + String.valueOf( trycatch.get(0).start)); // LabelNode
		System.out.println( "DDD end " + String.valueOf( trycatch.get(0).end)); // LabelNode

		die("STOP");

		final InsnList instructions = method.instructions;
		ControlFlowGraphExtractor controlFlow = new ControlFlowGraphExtractor( instructions );
//*
		controlFlow.dotPrintCFG();
/*/
		DiGraph dominator = new DiGraph(controlFlow);
		dominator.dotPrintDA();
//*/
	}
}
