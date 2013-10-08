package ch.usi.inf.sp.cfg;

public class ExceptionState {
	private EState state;
	private final int startAddr;
	private final int endAddr;
	private final int handlerAddr;
	
	public ExceptionState( int startAddr, int endAddr, int handlerAddr){
		this.startAddr = startAddr;
		this.endAddr = endAddr;
		this.handlerAddr = handlerAddr;

		this.state = EState.TRYING;
	}

	public EState getState() {
		return state;
	}

	public void setState(EState state) {
		this.state = state;
	}

	public int getStartAddr() {
		return startAddr;
	}

	public int getEndAddr() {
		return endAddr;
	}

	public int getHandlerAddr() {
		return handlerAddr;
	}
}
