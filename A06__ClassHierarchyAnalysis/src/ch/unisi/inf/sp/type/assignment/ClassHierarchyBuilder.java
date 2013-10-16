package ch.unisi.inf.sp.type.assignment;

import java.util.List;

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

			// extract modifiers, super class, interfaces, methods

			// modifiers / asm.access
			classType.setModifiers(clazz.access);

			// super class
			classType.setSuperClass(classHierarchy.getOrCreateClass(clazz.superName));

			// interfaces
// TODO checkout interfaces are kind of ClassTypes??
			List<String> listInterfaces = (List<String>) clazz.interfaces;
			for( String inf: listInterfaces ){
				classType.addInterface(classHierarchy.getOrCreateClass(inf));
			}

			// methods
// TODO test, check out getClass().name(), as well
			List<MethodNode> listMethodNodes = (List<MethodNode>) clazz.methods;
			for( MethodNode methodNode : listMethodNodes ){
				Method method = new Method(clazz.name, methodNode.name, methodNode.desc, methodNode.access);
				classType.addMethod(method);
			}

// TODO check if classType needs to be registered somewhere explicitely, or if 
// the pointer classType registeres automatically by using the getOrCreate... 
// method
			classType.setResolved();

		} catch (final TypeInconsistencyException ex) {
			System.err.println(ex);
		}
	}
	
}
