package ch.unisi.inf.sp.type.assignment;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.unisi.inf.sp.type.framework.ClassAnalyzer;
import ch.unisi.inf.sp.type.framework.ClassHierarchy;
import ch.unisi.inf.sp.type.framework.ClassType;
import ch.unisi.inf.sp.type.framework.Method;
import ch.unisi.inf.sp.type.framework.TypeInconsistencyException;


/**
 * Build a class hierarchy (including methods).
 * 
 * @author Lothar Rubusch
 */
public final class ClassHierarchyBuilder implements ClassAnalyzer {

	private final ClassHierarchy classHierarchy;
	
	
	public ClassHierarchyBuilder() {
		this.classHierarchy = new ClassHierarchy();
	}
	
	public ClassHierarchy getClassHierarchy() {
		return classHierarchy;
	}
	
	public void analyze(final String location, final ClassNode clazz) {
		try {
			final ClassType classType = classHierarchy.getOrCreateClass(clazz.name);
			if (classType.isResolved()) {
				System.err.println("WARNING: Class "+classType.getInternalName()+" defined multiple times");
				return;
			}
			classType.setLocation(location);
			
			
			// TODO extract modifiers, super class, interfaces, methods
			
			// modifiers / asm.access
			classType.setModifiers(clazz.access);
//			classType.getModifiers(); // TODO

			// super class
//			classType.setSuperClass(clazz.superName);
			//classType.getSuperClass(); // TODO

			// interfaces
//			classType.addInterface();
			//classType.getInterfaces(); // TODO

			// methods
			for( MethodNode methodNode : clazz.methods){
				Method method = new Method(clazz.name, methodNode.name, methodNode.desc, methodNode.access);
//				classType.addMethod(method);
			}
			//classType.getMethods(); // TODO
			
			
			classType.setResolved();
		} catch (final TypeInconsistencyException ex) {
			System.err.println(ex);
		}
	}
	
}
