/*
 * @author: Lothar Rubusch
 */
public class Element {
	private String _content;
	private int _target = -1;


	public Element( String content){
		this._content = content;
	}

	public Element( String content, int target ){
		this._target = target;
		this._content = content;
	}



	public int getTarget() {
		return _target;
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		this._content = content;
	}
}
