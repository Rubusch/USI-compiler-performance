package ch.usi.inf.sp.cfg;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;


/**
 * prints a controll flow graph (w/o exception handling here) and dominator tree
 * (rather buggy)
 * 
 * @author Lothar Rubusch
 *
 */
public class Analyzer {
	public static void die( String msg ){
		System.out.println( msg );
		System.exit(-1);
	}

	public static void echo( String msg ){
		System.out.println( msg );
	}

	public static void db( String msg){
		echo( "# DEBUG: '" + msg + "'");
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String classFileName = "";
// TODO method arguments shall be evaluated, too, in order to distinguish polymorphism
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
			if(1 < args.length){
				methodNameAndDescriptor = args[1];
				control.flow( cnode, methodNameAndDescriptor );
			}else{
				control.flow( cnode );
			}
		}catch( Exception exp ){
			// no specific method provided, do all methods
			exp.printStackTrace();
		}
		echo( "\n# READY.");
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
		echo("\n# " + method.name + "()");
		echo("#");
		final InsnList instructions = method.instructions;
		ControlFlowGraphExtractor controlFlow = new ControlFlowGraphExtractor( instructions );
/*
		controlFlow.dotPrintCFG();
		echo("# ---");
//*/
		DiGraph dominator = new DiGraph(controlFlow);
		dominator.dotPrintDA();
//*/
		echo("# ---");
	}
}
