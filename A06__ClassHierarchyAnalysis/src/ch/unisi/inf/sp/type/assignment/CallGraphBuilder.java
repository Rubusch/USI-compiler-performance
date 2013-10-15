package ch.unisi.inf.sp.type.assignment;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.unisi.inf.sp.type.framework.CallSite;
import ch.unisi.inf.sp.type.framework.ClassAnalyzer;
import ch.unisi.inf.sp.type.framework.ClassHierarchy;
import ch.unisi.inf.sp.type.framework.ClassType;
import ch.unisi.inf.sp.type.framework.Method;
import ch.unisi.inf.sp.type.framework.TypeInconsistencyException;


/**
 * Build a call graph (as part of the class hierarchy)
 * consisting of CallSite nodes pointing to Method nodes.
 * 
 * @author Lothar Rubusch
 */
public final class CallGraphBuilder implements ClassAnalyzer {

	private final ClassHierarchy hierarchy;
	
	
	public CallGraphBuilder(final ClassHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	public void analyze(final String location, final ClassNode classNode) {
		try {
			final ClassType type = hierarchy.getOrCreateClass(classNode.name);
			@SuppressWarnings("unchecked")
			final List<MethodNode> methodNodes = (List<MethodNode>)classNode.methods;
			CallSite callSite;
			for (final MethodNode methodNode : methodNodes) {
				final Method method = type.getMethod(methodNode.name, methodNode.desc);
				final InsnList instructions = methodNode.instructions;
				for (int i=0; i<instructions.size(); i++) {

					// implementation
					AbstractInsnNode ins = instructions.get(i);
					if( ins.getType() == AbstractInsnNode.METHOD_INSN ){
// TODO register callSite somewhere

						// Method(final String declaringClassName, final String name, final String descriptor, final int modifiers)
						// CallSite(final int opcode, final String declaredTargetClassName, final String targetMethodName, final String targetMethodDescriptor) {
						// by javadoc
						// declaredTargetClassName  = ((MethodInsnNode)ins).owner
						// targetMethodName         = ((MethodInsnNode)ins).name
						// targetMethodDescriptor   = ((MethodInsnNode)ins).desc
// TODO check parameters
// TODO check if the method functions, actually just return the values set by approaching CallSite (if so, use the ones in comments above)
						callSite = new CallSite(ins.getOpcode(), method.getDeclaringClassName(), method.getName(), method.getDescriptor());

						// register the method with the callsite
						method.addCallSite(callSite);
					}
				}
			}
		} catch (final TypeInconsistencyException ex) {
			System.err.println(ex);
		}
	}

}
