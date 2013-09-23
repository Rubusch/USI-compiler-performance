package SimpleMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/*
 * Number of classes (incl. interfaces and enums)
 * Number of concrete non-native, non-abstract methods (methods with code)
 * Total number of instructions (you have to avoid ASM internal instructions with opcode -1)
 * Total number of instructions by opcode (ignoring instructions with opcode -1)
 * Total number of method invocation instructions (== call sites)
 * Total number of conditional branch instructions (this excludes GOTO)
 */
public class Analyzer {
	private HashMap<Integer, Integer> totalNumberOfInstructionsByOpcode; // FIXME

	public static void main(String[] args) throws IOException {
		int numberOfClasses = 0;
		int numberOfMethods = 0;
		int totalNumberOfInstructions = 0;
		int totalNumberOfMethodInvocationInstructions = 0; // TODO
		final List< Integer > callOpcodes; // = [168]; // FIXME Integer types in list
		callOpcodes = new ArrayList< Integer >(); // FIXME

		int totalNumberOfConditionalBranchInstructions = 0; // TODO
		// 148, 149, 150, 151, 152,   no branching opcodes?!
		final List< Integer > branchOpcodes; // = [ 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166]; // FIXME
		branchOpcodes = new ArrayList< Integer >(); // FIXME

		final String jarFileName = args[0];
		System.out.println( "analyzing '" + jarFileName + "'" );
		final JarFile jar = new JarFile( jarFileName );
		final Enumeration<JarEntry> entries = jar.entries();
		while( entries.hasMoreElements() ){
			final JarEntry entry = entries.nextElement();
			if( !entry.isDirectory() && entry.getName().endsWith( ".class" )){
//				System.out.println(entry.getName());
				final InputStream is = jar.getInputStream(entry);

				final ClassReader classReader = new ClassReader(is);

				final ClassNode classNode = new ClassNode();

				// count number of classes
				// TODO check enums are contained?
				// TODO check interfaces are contained?
				classReader.accept( classNode, ClassReader.SKIP_FRAMES );
				System.out.println("  class '" + classNode.name + "'");
				numberOfClasses += 1;

				// TODO check, interface is same as class, sometimes (so classNodes include number of interfaces? )
//				List<String> interfaces = classNode.interfaces;
//				for( final String interfaceNode : interfaces ){
//					System.out.println("XXX interface '" + interfaceNode + "'");
//				}

				int opcode = -1;
				@SuppressWarnings("unchecked")
				final List<MethodNode> methods = classNode.methods;
//				System.out.println("  Number of methods in class '" + String.valueOf(methods.size()) + "'");

				for( final MethodNode methodNode : methods ){
//					System.out.println( "  method '" + methodNode.name + methodNode.desc + "'" );
					InsnList instructionList = methodNode.instructions;

					// number of non-native and non-empty methods					
					if( 0 < instructionList.size() ){
						numberOfMethods += 1;

						for( AbstractInsnNode instructionNode : instructionList.toArray() ){
							opcode = instructionNode.getOpcode();
							if( -1 != opcode ){
//								System.out.println("  opcode '" + instructionNode.getOpcode() + "'"); // XXX
								// total number of instructions
								totalNumberOfInstructions += 1;

								// TODO append entry to hashmap or update value if key is already in hashmap

								// TODO if instruction is a call (opcode)
								if( callOpcodes.contains( Integer.valueOf( opcode )) ){
									totalNumberOfMethodInvocationInstructions += 1;
								}

								// TODO if instruction is a branch (opcode), no GOTO!!
								if( branchOpcodes.contains( Integer.valueOf( opcode )) ){
									totalNumberOfConditionalBranchInstructions += 1;
								}
							}
						}
					}

//					System.out.println( "  method instructions '" + String.valueOf( methodNode.instructions.size()) + "'");
				}
			}
		}

		// output
		System.out.println( "RESULT: ");
		System.out.println( "  Number of classes '" + numberOfClasses + "'");
		System.out.println( "  Number of methods '" + numberOfMethods + "'");
		System.out.println( "  Total number of instructions '" + totalNumberOfInstructions + "'");
		printOpcodes();
		System.out.println( "  Total number of method invocation instructions '" + totalNumberOfMethodInvocationInstructions + "'");
		System.out.println( "  Total number of conditional branch instructions '" + totalNumberOfConditionalBranchInstructions + "'");
		jar.close();
	}

	private static void printOpcodes(){
		int key = -1;
		int value = -1;
		
		// TODO print totalNumberOfInstructionsByOpcode
		
		System.out.println("  number of each opcode:");
		// TODO iterate over key list
		// TODO print value to each key
	}
};