package ch.usi.inf.sp.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public final class Transformer implements ClassFileTransformer{

	@Override
	public byte[] transform(ClassLoader arg0, String arg1, Class<?> arg2,
			ProtectionDomain arg3, byte[] arg4)
			throws IllegalClassFormatException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
