package SimpleMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/*
 * ASSIGNMENT02
 * 
 * Number of classes (incl. interfaces and enums)
 * Number of concrete non-native, non-abstract methods (methods with code)
 * Total number of instructions (you have to avoid ASM internal instructions with opcode -1)
 * Total number of instructions by opcode (ignoring instructions with opcode -1)
 * Total number of method invocation instructions (== call sites)
 * Total number of conditional branch instructions (this excludes GOTO)
 */
public class Analyzer {
	private static HashMap<String, Integer> totalNumberOfInstructionsByOpcode;

	public static void main(String[] args) throws IOException {
		int numberOfClasses = 0;
		int numberOfMethods = 0;
		int totalNumberOfInstructions = 0;

		int totalNumberOfMethodInvocationInstructions = 0;
		final List< Integer > callOpcodes  = new ArrayList< Integer >();
		{
			final int []iOpcodes = { 182, 183, 184, 185, 186 };
			for( int iOpcode : iOpcodes){ callOpcodes.add( new Integer( iOpcode )); }
		}

		int totalNumberOfConditionalBranchInstructions = 0;

		final List< Integer > branchOpcodes;
		branchOpcodes = new ArrayList< Integer >();
		{
			final int []iOpcodes = { 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166 };
			for( int iOpcode : iOpcodes){
				branchOpcodes.add(new Integer(iOpcode));
			}
		}

		totalNumberOfInstructionsByOpcode = new HashMap< String, Integer >();

		if( 0 >= args.length ){
			die( "Usage: java Analyzer <path to .jar>" );
		}
		final String jarFileName = args[0];

		System.out.println( "analyzing '" + jarFileName + "'" );
		JarFile jar = null;
		try{
			jar = new JarFile( jarFileName );
		}catch(IOException exp){
			die("FAILED - see issues with the provided filename / path");
			exp.printStackTrace();
		}

		final Enumeration<JarEntry> entries = jar.entries();
		while( entries.hasMoreElements() ){
			final JarEntry entry = entries.nextElement();
			if( !entry.isDirectory() && entry.getName().endsWith( ".class" )){
//				System.out.println(entry.getName()); // XXX
				final InputStream is = jar.getInputStream(entry);

				final ClassReader classReader = new ClassReader(is);

				final ClassNode classNode = new ClassNode();

				// count number of classes
// TODO check enums are contained?
// TODO check interfaces are contained?
				classReader.accept( classNode, ClassReader.SKIP_FRAMES );
//				System.out.println("  class '" + classNode.name + "'"); // XXX
				numberOfClasses += 1;

// TODO check, interface is same as class, sometimes (so classNodes include number of interfaces? )
//				List<String> interfaces = classNode.interfaces;
//				for( final String interfaceNode : interfaces ){
//					System.out.println("XXX interface '" + interfaceNode + "'");
//				}

				int opcode = -1;
				@SuppressWarnings("unchecked")
				final List<MethodNode> methods = classNode.methods;
//				System.out.println("  Number of methods in class '" + String.valueOf(methods.size()) + "'"); // XXX

				for( final MethodNode methodNode : methods ){
//					System.out.println( "  method '" + methodNode.name + methodNode.desc + "'" ); // XXX
					InsnList instructionList = methodNode.instructions;

					// number of non-native and non-empty methods					
					if( 0 < instructionList.size() ){
						numberOfMethods += 1;

						for( AbstractInsnNode instructionNode : instructionList.toArray() ){
							opcode = instructionNode.getOpcode();
							if( -1 != opcode ){

								// total number of instructions
								totalNumberOfInstructions += 1;

								// append opcode statistics
								Integer cnt = null;
								if( null != (cnt = totalNumberOfInstructionsByOpcode.get( String.valueOf(opcode) ))){
									// opcode already in hashmap
									++cnt;
								}else{
									// opcode is new, new element in hashmap
									cnt = new Integer(1);
								}
								totalNumberOfInstructionsByOpcode.put( String.valueOf(opcode), cnt);

								// if instruction is a method call (opcode "invoke")
								if( callOpcodes.contains( Integer.valueOf( opcode )) ){
									totalNumberOfMethodInvocationInstructions += 1;
								}

								// if instruction is a branch (opcode), w/o GOTO!!
								if( branchOpcodes.contains( Integer.valueOf( opcode )) ){
									totalNumberOfConditionalBranchInstructions += 1;
								}
							}
						}
					}
				}
			}
		}

		// output
		System.out.println( "\nRESULT: ");

		System.out.println( numberOfClasses + "\t - Number of classes (incl. interfaces and enums)" );
		System.out.println( numberOfMethods + "\t - Number of concrete non-native, non-abstract methods (methods with code)");
		System.out.println( totalNumberOfInstructions + "\t - Total number of instructions (you have to avoid ASM internal instructions with opcode -1)");
		printOpcodes();
		System.out.println( totalNumberOfMethodInvocationInstructions + "\t - Total number of method invocation instructions (== call sites)");
		System.out.println( totalNumberOfConditionalBranchInstructions + "\t - Total number of conditional branch instructions (this excludes GOTO)");

		System.out.println( "\nREADY." );
		jar.close();
	}

	private static void printOpcodes(){
		System.out.println("Total number of instructions by opcode (ignoring instructions with opcode -1):");
		SortedSet<String> keys = new TreeSet<String>(totalNumberOfInstructionsByOpcode.keySet());
		for( String key : keys){
			String szOpcode = org.objectweb.asm.util.Printer.OPCODES[ Integer.valueOf(key).intValue()];
			System.out.println( String.valueOf(totalNumberOfInstructionsByOpcode.get(key)) + "\t\t - " + szOpcode);
		}
	}
	
	private static void die( String msg){
		System.out.println( msg );
		System.exit(-1);
	}
};