package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Welcome to hell!
 * 
 * @author Lothar Rubusch
 * 
 */
public class NodeWrapper {
	private final Integer Id;
	private Integer idom; // immediate dominator
	private List<List<Integer>> heritage;

	public NodeWrapper( final Integer startId){
		this.Id = startId;
	}


	public List<List<Integer>> getHeritage(){
		return heritage;
	}


	private boolean isEqualInheritPaths( List<Integer> inheritPathA, List<Integer> inheritPathB ){
		int sizeA = inheritPathA.size();
		int sizeB = inheritPathB.size();
		if( sizeA != sizeB ) return false;
		for(int idx=0; idx<sizeA; ++idx){
			if(inheritPathA.get(idx) != inheritPathB.get(idx)){
				return false;
			}
		}
		return true;
	}


	private boolean isInheritPathContained(List<Integer> inheritPath){
		if( 0 == this.heritage.size() ) return false;
		for( List<Integer> thisInheritPath : this.heritage){
			if( isEqualInheritPaths(thisInheritPath, inheritPath )){
				return true;
			}
		}
		return false;
	}


	private List<Integer> getSmallestInheritPath(){
		// init, if this step fails, this.inheritage was corrupt
		List<Integer> smallestPath = this.heritage.get(0);

		// get min in size of all inheritPaths
		for( List<Integer> inheritPath : this.heritage){
			int sizeSmallest = smallestPath.size();
			int sizeCurrent = inheritPath.size();
			if( sizeSmallest > sizeCurrent){
				smallestPath = inheritPath;
			}
		}
		return smallestPath;
	}


	private boolean inheritPathContains( Integer currAncestor, List<Integer> inheritPath){
		for( Integer ancestor : inheritPath){
			if( currAncestor == ancestor) return true;
		}
		return false;
	}


	private List<Integer> cloneInheritPath( List<Integer> inheritPath ){
		List<Integer> clonedInheritPath = new ArrayList<Integer>();
		for(Integer id: inheritPath){
			clonedInheritPath.add(id);
		}
		return clonedInheritPath;
	}


	/*
	 * merge inheritage list of another NodeWrapper with THIS NodeWrapper
	 * 
	 * may be called at each update, so not just once
	 */
	public void updateHeritage( List<List<Integer>> heritage){
// Analyzer.db("NodeWrapper::inheritageInit() - START, block" + String.valueOf( this.id()) ); // XXX

		// init inheritage
		if( null == this.heritage ){
			// first time called this method, do the initialization
			this.heritage = new ArrayList<List<Integer>>();

			if( DiGraph.START == this.id()){
				// init the absolute first node
				
				// inheritage only has one path, with one element: START
				List<Integer> inheritPath = new ArrayList<Integer>();
				inheritPath.add(this.id());
				this.heritage.add(inheritPath);

				// there is NO dominator
				this.idom = null;

				// done
				return;
			}
		}


		for( List<Integer> inheritPath : heritage ){
			List<Integer> inheritPathClone = cloneInheritPath( inheritPath );

			// update parent inheritancePaths
			inheritPathClone.add(this.id());

			// filter out new inheritPaths in foreign inheritage
			if( !isInheritPathContained(inheritPathClone) ){
				// append, if it is not contained
				this.heritage.add(inheritPathClone);
			}
		}


/*		// DEBUG
Analyzer.db("XXX / this.heritage =");
Analyzer.db(String.valueOf(this.heritage));
Analyzer.db("/ XXX");
//*/		// /DEBUG


		// figure out current dominator
		List<Integer> smallestPath = getSmallestInheritPath();
		List<Integer> resultingPath = new ArrayList<Integer>();

		// last valid element is smallestPath.size() -2 (-1 is this.id())
		for( int idxAncestor=0; idxAncestor < smallestPath.size()-1; ++idxAncestor){
			// get element of the smallest list
			int currAncestor = smallestPath.get(idxAncestor);

			int idxInheritPath = -1;
			for( idxInheritPath=0; idxInheritPath < this.heritage.size(); ++idxInheritPath){
				// per inherit path check if it is contained (add) or not (discard)
				List<Integer> currentInheritPath = this.heritage.get(idxInheritPath);
				if( !inheritPathContains( currAncestor, currentInheritPath) ){
					// not contained, discard element
					break;
				}
			}

			if( idxInheritPath == this.heritage.size()){
				// all inheritPaths contained currAncestor, so append it
				resultingPath.add(currAncestor);
			}
		}

		// dominator
		int lastIdx = resultingPath.size() -1;
		this.idom = resultingPath.get(lastIdx);

//Analyzer.db("NodeWrapper::inheritageInit(), idom "+String.valueOf(this.idom)+" - END\n" ); // XXX
	}


	public void inheritageMerge( NodeWrapper parent ){
		// START node
		if( null == parent && this.id() == DiGraph.START ){
			updateHeritage( null );
			return;
		}

		// set up a stack for the parents
		updateHeritage( parent.getHeritage() );
	}


	public Integer getIDom(){
		if( null == idom ){ return 0; }
		return idom;
	}


	public void dotPrint(){
		if( DiGraph.START == Id ){
			Analyzer.echo( "  nodeS [label = \"Start\"]");
		}else if( DiGraph.END == Id){
			Analyzer.echo( "  nodeE [label = \"End\"]");
		}else{
			Analyzer.echo( "  node" + Id + " [label = \"block" + Id + "\"]");
		}
	}


	public Integer id(){
		return Id;
	}
}
