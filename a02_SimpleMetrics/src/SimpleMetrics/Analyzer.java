package SimpleMetrics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Analyzer {
	
	public static void main(String[] args) throws IOException {
		int totalNumberOfClasses = 0;
		
		final String jarFileName = args[0];
		System.out.println( "analyzing '" + jarFileName + "'" );
		final JarFile jar = new JarFile( jarFileName );
		final Enumeration<JarEntry> entries = jar.entries();
		while( entries.hasMoreElements() ){
			final JarEntry entry = entries.nextElement();
			if( !entry.isDirectory() && entry.getName().endsWith( ".class" )){
				System.out.println(entry.getName());
				final InputStream is = jar.getInputStream(entry);
				final ClassReader classReader = new ClassReader(is);
				final ClassNode classNode = new ClassNode();
				classReader.accept( classNode, ClassReader.SKIP_FRAMES );
//				System.out.println("  class '" + classNode.name + "'");
				totalNumberOfClasses += 1;

				@SuppressWarnings("unchecked")
				final List<MethodNode> methods = classNode.methods;
				System.out.println("  number of methods in class '" + String.valueOf(methods.size()) + "'");
//				for( final MethodNode methodNode : methods ){
//					System.out.println( "  method '" + methodNode.name + methodNode.desc + "'" );
//					// ...
//				}
			}
		}
		System.out.println( "  number of classes '" + totalNumberOfClasses + "'");
		jar.close();
	}
}