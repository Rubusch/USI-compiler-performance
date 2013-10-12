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
	private List<EState> stateTable;

	public ExceptionTable(){
		exceptionTable = new ArrayList<ExceptionState>();
		stateTable = new ArrayList<EState>();
	}

	public class ExceptionComparator implements Comparator<ExceptionState>{
		@Override
		public int compare(ExceptionState arg0, ExceptionState arg1) {
			if(arg0.getStartAddr() < arg1.getStartAddr()) return -1;
			if(arg0.getStartAddr() > arg1.getStartAddr()) return 1;
			
			// both are equal (twisted)
			if(arg1.getEndAddr() < arg0.getEndAddr()){
				arg0.setState(EState.FINALIZING);
				arg1.setState(EState.CATCHING);
				return -1;
			}
			if(arg1.getEndAddr() == arg0.getEndAddr()) Analyzer.die("comparison between two identical entries in Exception Table"); // XXX
			arg0.setState(EState.CATCHING);
			arg1.setState(EState.FINALIZING);
			return 1;
		}
	}

	public void init( final List<TryCatchBlockNode> exceptions, final InsnList insns){
		for( TryCatchBlockNode trycatch : exceptions){
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

	public void initStates(final InsnList instructions){
		stateTable.add( EState.NONE );
		EState state = EState.NONE;
		for( int idx=0; idx < instructions.size(); ++idx){
			for( int idxSub = 0; idxSub < exceptionTable.size(); ++idxSub){
				if(idx == exceptionTable.get(idxSub).getStartAddr()){
					state = EState.TRYING;
					break;
				}
			}

			for( int idxSub = 0; idxSub < exceptionTable.size(); ++idxSub){
				if(idx == exceptionTable.get(idxSub).getHandlerAddr()){
					state = exceptionTable.get(idxSub).getState();
					break;
				}
			}

			stateTable.add(state);
		}
	}

	public EState state( int idx ){
		if(0 == exceptionTable.size()) return EState.NONE;
		return this.stateTable.get(idx);
	}


	public int getNextHandler( int idx ){
		if( 0 == exceptionTable.size()) return -1;

		int idxHandler=0;
		for( idxHandler=1; idxHandler < exceptionTable.size(); ++idxHandler){
			if( idx < exceptionTable.get(idxHandler).getStartAddr()){
				break;
			}
		}

		// selftest
		if(idxHandler == exceptionTable.size()){
			Analyzer.die("getNextHandler() - index overrun");
// TODO what to do in this case?
//			return -1; // overrun, ERROR
		}

		return exceptionTable.get( idxHandler - 1 ).getHandlerAddr(); 
	}

	public int getOverNextHandler( int idx ){
		if( 0 == exceptionTable.size()) return -1;

		int idxHandler=0;
		for( idxHandler=1; idxHandler < exceptionTable.size(); ++idxHandler){
			if( idx < exceptionTable.get(idxHandler).getStartAddr()){
				break;
			}
		}

		// selftest
		if(idxHandler == exceptionTable.size()){
			Analyzer.db("getOverNextHandler() - index overrun");
			return -1; // overrun, ERROR
		}

		if( exceptionTable.get(idxHandler-1).getStartAddr() != exceptionTable.get(idxHandler-2).getStartAddr()){
			Analyzer.die("getOverNextHandler() - no finally handler");
		}

		return exceptionTable.get(idxHandler - 2).getHandlerAddr(); // if this fails, it is definitely a bug
	}

	public void printStateTable(){
		System.out.println( "--- State Table ---");
		for(int idx=0; idx<stateTable.size(); ++idx){
			System.out.println( String.valueOf(idx) + ":" + printer(stateTable.get(idx)));
		}
		System.out.println("---");
	}

	public void printExceptionTable(){
		System.out.println( "--- Exception Table ---" );
		for( ExceptionState es : exceptionTable ){
			System.out.print( "start='" + String.valueOf(es.getStartAddr()) + "', end='" + String.valueOf(es.getEndAddr()) + "', handler=" + String.valueOf(es.getHandlerAddr()) +"'");
//			System.out.println(", STATE='" + (es.getState()==EState.NONE?"NONE":(es.getState()==EState.TRYING?"TRYING":(es.getState()==EState.CATCHING?"CATCHING":"FINALIZING")) ) + "'");
			System.out.println(", STATE='" + ExceptionTable.printer(es.getState()));
		}
		System.out.println( "---" );
	}

	public static String printer( EState state ){
		switch (state){
			case NONE: return "NONE";
			case TRYING: return "TRYING";
			case CATCHING: return "CATCHING";
			case FINALIZING: return "FINALIZING";
		}
		return "FAIL";
	}
}
