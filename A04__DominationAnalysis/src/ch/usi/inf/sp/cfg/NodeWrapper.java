package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Welcome to hell!
 * 
 * @author Lothar Rubusch
 * 
 */
public class NodeWrapper {
	private final Integer Id;
// TODO why is idom a Integer, and not a Node?
	private Integer idom; // immediate dominator
	private List<List<Integer>> inheritage;

	public NodeWrapper( final Integer startId){
		this.Id = startId;
//		this.inheritage = new ArrayList<List<Integer>>();
	}

	public List<List<Integer>> getInheritage(){
		return inheritage;
	}












	private boolean isEqualInheritPaths( List<Integer> inheritPathA, List<Integer> inheritPathB ){
		int sizeA = inheritPathA.size();
		int sizeB = inheritPathB.size();

		if( sizeA != sizeB ) return false;
 
		for(int idx=0; idx<sizeA; ++idx){
			if(inheritPathA.get(idx) != inheritPathB.get(idx)){
// TODO test this comparison
				return false;
			}
		}
		return true;
	}

	private boolean isInheritPathContained(List<Integer> inheritPath){
// TODO test method
		if( 0 == this.inheritage.size() ) return false;
		for( List<Integer> thisInheritPath : this.inheritage){
			if( isEqualInheritPaths(thisInheritPath, inheritPath )){
				return true;
			}
		}
		return false;
	}

	/*
	 * merge inheritage list of another NodeWrapper with THIS NodeWrapper
	 */
	public void inheritageInit( List<List<Integer>> inheritage){
		Analyzer.db("NodeWrapper::inheritageInit() - START, block" + String.valueOf( this.id()) ); // XXX

		// just call init ONCE - for a second time, call the merge
		if( null != this.inheritage ){
			return;
		}

		// init inheritage
		this.inheritage = new ArrayList<List<Integer>>();


		for( List<Integer> inheritPath : inheritage ){
			// update parent inheritancePaths
			inheritPath.add(this.id());

			// check wether a new path of inheritance is contained
			if( !isInheritPathContained(inheritPath) ){
				// append it, if it is not contained
				this.inheritage.add(inheritPath);
			}
		}



/* //////////////////////////////////////////////		

		if( DiGraph.START == this.id() ){
// TODO check inheritage, is NULL here!!!
			Analyzer.db("NodeWrapper::inheritageInit() - ROOT node\n" ); // XXX
			// START / root node
			List<Integer> currentGeneration = new ArrayList<Integer>();
			currentGeneration.add(DiGraph.START );
			this.inheritage.add( currentGeneration );
			return;
		}



		// add list as separate new initialized lists
//		Analyzer.db("\t- inheritage.size() " + String.valueOf( inheritage.size() ) ); // XXX
		for( int idxGeneration=0; idxGeneration < inheritage.size(); ++idxGeneration){
			List<Integer> generation = inheritage.get(idxGeneration);
//			this.inheritage.add( new ArrayList<Integer>() ); // TODO why?!

// FIXME
			Analyzer.db("\t\t generation.size() " + String.valueOf( generation.size() ));
			for( int idxid=0; idxid < generation.size(); ++idxid){

				Analyzer.db("\t\t\t block" + generation.get(idxid)); // XXX

				// new generation element
				List<Integer> newGeneration = new ArrayList<Integer>();

				// get the new entry, and add a new generation element
				newGeneration.add( generation.get(idxid) );

				// add new element
				this.inheritage.add( newGeneration );
			}
		}



		List<Integer> generation = this.inheritage.get(0);
		int parentIdx = generation.size()-1;
//		Integer latestId = generation.get( latestIdx );
//*/



		// set dominator
		this.idom = generation.get( parentIdx );
// FIXME: characteristic to select which parent inheritage to choose in case of several parent inheritages in the same generation

		// update, and append own Id to all of the inheritage lists
		for( List<Integer> parentInheritages : this.inheritage ){

			// append if parent inheritage still does not contain this element
			if( -1 == parentInheritages.indexOf(Id)){
				parentInheritages.add(Id);
			}// else: loop (issue with doubled last entries...)
		}



		Analyzer.db("NodeWrapper::inheritageInit() - END\n" ); // XXX
	}



	public void inheritageMerge( List<NodeWrapper> parents ){
		Analyzer.db("NodeWrapper::inheritageMerge() - START");

		// START node
		if( null == parents && this.id() == DiGraph.START ){
			inheritageInit( null );
			return;
		}
		
		// add all parent inheritages
		for( NodeWrapper parent : parents ){
			inheritageInit( parent.getInheritage() );
		}

		// find dominator (reset dominator)
		identifyDominator( parents );
		Analyzer.db("NodeWrapper::inheritageMerge() - END");
	}



	public Integer getIDom(){
		if( null == idom ){ return 0; }
		return idom;
	}



	// return false, if was not mergeable (still), needs to be redone later
	// this means basically a "false" shall provoke the remove from the
	// "passedIds" list
// TODO modularize
// TODO separate in different methods
// TODO remove redundant code
// TODO check algorithm
	
	private void identifyDominator( List<NodeWrapper> parents){
		Analyzer.db("NodeWrapper::identifyDominator() - START");

		for( NodeWrapper nd : parents){
			boolean pending=false; // more ugly quickfixes
			List<List<Integer>> ndInheritageList = nd.getInheritage();
			if( 0 == ndInheritageList.size() ){
//			if( 0 == nd.getInheritage().size()){
// TODO in case one parent is still not parsed (upward link), just postpone this 
// node, and generate the merge later (to be implemented), so far, simply omited, 
// since it works anyway +/-
				pending = true;
			}else{
				Analyzer.db("\t- " + String.valueOf( nd.getInheritage().get(0).get( nd.getInheritage().get(0).size()-1 )) ); // XXX
				Analyzer.db("\t- block" + nd.id() );
				this.idom = nd.getInheritage().get(0).get( nd.getInheritage().get(0).size()-1 );
			}
// TODO improve this by parse order
			if( pending ) return;
		}
		Analyzer.db("NodeWrapper::identifyDominator() - no pendings");


/*
		if( 2 > parents.size() ){
			Analyzer.echo("FATAL - compare at least 2 nodes, passed were " + parents.size());
		}
//*/
		// list of parents, each parent lists severan "inheritages", each is a list of Integers
		ArrayList<ArrayList<ArrayList<Integer>>> data = new ArrayList<ArrayList<ArrayList<Integer>>>();

		// init
		for( int idxparent=0; idxparent < parents.size(); ++idxparent){
			// per parent
			NodeWrapper parent = parents.get(idxparent);
			data.add(new ArrayList<ArrayList<Integer>>());

			for( int idxinherit=0; idxinherit < parent.getInheritage().size(); ++idxinherit){
				List<Integer> inherit = parent.getInheritage().get(idxinherit);

				// per descendence lists
				data.get(idxparent).add(new ArrayList<Integer>());
				for( int idxid=0; idxid < inherit.size(); ++idxid){

					// per item in particular descendence list
					data.get(idxparent).get(idxinherit).add( inherit.get(idxid) );
				}
			}
		}

		// operate
		ArrayList<ArrayList<Integer>> parent = data.get(0);
		for( int pos=1; 0 < parent.size(); ++pos ){ // don't enter here if parent - index == 0 - is empty
			for( int idxinherit = parent.size()-1; 0 <= idxinherit; --idxinherit){
				ArrayList<Integer> inherit = parent.get(idxinherit);
				if(pos >= inherit.size()){
					if(1 == parent.size()){
						// this parent has played its last inherit list, last element is dominator
						if(0 < pos){
							this.idom = inherit.get(pos -1);
						}else{
							// list was empty - an ERROR
							Analyzer.echo( "FIXME: a node had just one inherit list, which was empty");
						}
					return;
					}else{
						parent.remove(idxinherit);
						continue;
					}
				}

				// now get an id and check with other parents
				Integer id = inherit.get(pos);

				// check now if id is containent in other parent at this position or not ( = discard whole vector)
				for( int jdxparent = 1; jdxparent < data.size(); ++jdxparent){
					ArrayList<ArrayList<Integer>> cmpParent = data.get(jdxparent);

					ArrayList<Integer> cmpParentIds = new ArrayList<Integer>();
					for( int jdxinherit = 0; jdxinherit < cmpParent.size(); ++jdxinherit){
						ArrayList<Integer> cmpInherit = cmpParent.get(jdxinherit);
						cmpParentIds.add( cmpInherit.get(pos) );
					}
					if( -1 == cmpParentIds.indexOf(id)){
						// not contained in entire set of this parent
						// this means, remove the list from parent, and take next item
						// if this was the last inherit list of parent, then we have idom
						if(1 == parent.size()){
							// this parent has played its last inherit list, last element is dominator
							if(0 < pos){
								this.idom = inherit.get(pos -1);
							}else{
								// list was empty - an ERROR
								Analyzer.echo( "FIXME: a node had just one inherit list, which was empty");
							}
							return;
						}else{
							parent.remove(idxinherit);
							continue;
						}
					}
				}
			}
		}
		return;
	}


	public void dotPrint(){
// TODO check, another label?
		if( DiGraph.START == Id ){
			Analyzer.echo( "  nodeS [label = \"blockS\"]");
		}else if( DiGraph.END == Id){
			Analyzer.echo( "  nodeE [label = \"blockE\"]");
		}else{
			Analyzer.echo( "  node" + Id + " [label = \"block" + Id + "\"]");
		}
	}

	public Integer id(){
		return Id;
	}
}
