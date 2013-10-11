package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class ExceptionTable {
	
	private List<ExceptionState> exceptionTable;
	
	public ExceptionTable(){
		exceptionTable = new ArrayList<ExceptionState>();
	}

	public class ExceptionComparator implements Comparator<ExceptionState>{

		@Override
		public int compare(ExceptionState arg0, ExceptionState arg1) {
			if(arg0.getStartAddr() < arg1.getStartAddr()) return -1;
			if(arg0.getStartAddr() > arg1.getStartAddr()) return 1;
			
			// both are equal (twisted)
			if(arg1.getEndAddr() < arg0.getEndAddr()) return -1;
			if(arg1.getEndAddr() == arg0.getEndAddr()) Analyzer.die("comparison between two identical entries in Exception Table"); // XXX
			return 1;
		}
	}

	public void init( final List<TryCatchBlockNode> trycatchlist, final InsnList insns){
//		List<TryCatchBlockNode> trycatchlist = method.tryCatchBlocks;
		for( TryCatchBlockNode trycatch : trycatchlist){
			int start = insns.indexOf((LabelNode) trycatch.start);
			int end = insns.indexOf((LabelNode) trycatch.end);
			int handler = insns.indexOf((LabelNode) trycatch.handler);
			this.exceptionTable.add(new ExceptionState(start, end, handler));
			// debug
			Analyzer.db("EXCEPTION: start =" + String.valueOf(start) + ", end =" + String.valueOf(end) + ", handler =" + String.valueOf(handler));
		}
		
		// sort by "start", if equal, then sort reversely by "end"
		Collections.sort( exceptionTable, new ExceptionComparator() );
	}





	public void printExceptionTable(){
		System.out.println( "--- Exception Table ---" );
		for( ExceptionState es : exceptionTable ){
			System.out.println( "start='" + String.valueOf(es.getStartAddr()) + "', end='" + String.valueOf(es.getEndAddr()) + "', handler=" + String.valueOf(es.getHandlerAddr()) +"'");
		}
		System.out.println( "---" );
	}
}
