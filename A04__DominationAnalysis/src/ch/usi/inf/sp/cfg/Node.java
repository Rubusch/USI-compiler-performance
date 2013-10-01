package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Node {
	private ArrayList<AbstractInsnNode> blockinstructions;
	private final Integer Id; // TODO how to identify this node
// TODO why is idom a Integer?
	private Integer idom; // immediate dominator
	private List<List<Integer>> inheritage;

	public Node( ArrayList<AbstractInsnNode> blockinstructions, final Integer startId){
		this.blockinstructions = blockinstructions;
		this.Id = startId;
		this.inheritage = new ArrayList<List<Integer>>();
	}

	public List<List<Integer>> getInheritage(){
		return inheritage;
	}

	public void inheritageMerge( List<Node> parents ){
		// add all parent inheritages
		for( Node parent : parents ){
			inheritageInit( parent.getInheritage() );

// TODO in case find matching between the parents! for all sets in a parent compared to all sets of another one -> extract "stem"
		}
		
		findDominator( parents );

		// find dominator (reset dominator)
// TODO extract last Id from the matching stem
		this.idom = new Integer(0); // TODO set correct value
	}

	public void inheritageInit( List<List<Integer>> inheritage){
// TODO handle first node, inheritage is what?
		if( null != inheritage ){
			this.inheritage.addAll( inheritage );

			// set dominator
			try{
				this.idom = this.inheritage.get(0).get(0);
			}catch(IndexOutOfBoundsException exp){
				this.idom = null;
			}
		}

		// append own Id to all of the inheritage lists
		for( List<Integer> list : this.inheritage ){
			list.add(Id);
		}
	}

	public Integer getIDom(){
		if( null == idom ){ return 0; }
		return idom;
	}

	public void findDominator( List<Node> parents){
		// list of parents, each parent lists severan "inheritages", each is a list of Integers
		ArrayList<ArrayList<ArrayList<Integer>>> data = new ArrayList<ArrayList<ArrayList<Integer>>>();

		// init
		for( int idxparent=0; idxparent < parents.size(); ++idxparent){
			// per parent
			Node parent = parents.get(idxparent);
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
//		for( int pos=1; true; ++pos ){ // TODO uncomment - at most, up to largest list
		for( int pos=0; pos<10; ++pos ){ // TODO debug, rm
			for( int idxparent=0; idxparent < data.size(); ++idxparent){
				ArrayList<ArrayList<Integer>> parent = data.get(idxparent);
//				for( int idxinherit=data.get(idxparent).size()-1; idxinherit > 0; --idxinherit){
				for( int idxinherit = parent.size()-1; idxinherit > 0; --idxinherit){
					ArrayList<Integer> inherit = parent.get(idxinherit);
// TODO size checks
					Integer id = -1; // TODO START?
//					try{
//						id = parent.get(idxinherit).get(pos);
					if(pos >= inherit.size()){
						if(1 == parent.size()){
							// this parent has played its last inherit list, last element is dominator
							if(0 < pos){
								this.idom = inherit.get(pos -1);
							}else{
								// list was empty - an ERROR
								System.out.println( "FIXME: a node had just one inherit list, which was empty");
							}
							return;
						}else{
							parent.remove(idxinherit);
							continue;
						}
					}

					// now get an id and check with other parents
					id = inherit.get(pos);

//					}catch( Exception exp){
//						if(1 == data.get(idxparent).size() ){
//						if(1 == parent.size() ){
//							this.idom = data.get(idxparent).get(idxinherit).get(pos-1);
//							this.idom = inherit.get(pos-1);
//							return;
//							return data.get(idxparent).get(idxinherits).get(pos-1);
//						}else{
//							data.get(idxparent).remove(idxinherit);
//						}
//						continue;
//					}

					// check now if id is containent in other parent at this position or not ( = discard whole vector)
					for( int jdxparent = idxparent + 1; jdxparent < data.size(); ++jdxparent){
						ArrayList<ArrayList<Integer>> cmpParent = data.get(jdxparent);

						ArrayList<Integer> cmpParentIds = new ArrayList<Integer>();
//						for( int jdxinherit = 0; jdxinherit < data.get(jdxparent).size(); ++jdxinherit){
						for( int jdxinherit = 0; jdxinherit < cmpParent.size(); ++jdxinherit){
							ArrayList<Integer> cmpInherit = cmpParent.get(jdxinherit);
//							jids.add( data.get(jdxparent).get(jdxinherit).get(pos) );
							cmpParentIds.add( cmpInherit.get(pos) );
						}
// TODO wtf?!
						if( -1 == cmpParentIds.indexOf(id)){
							// not contained in entire of this parents' sets, means we have the idom: pos-1
							if(0 < pos){
								this.idom = inherit.get(pos -1);
							}else{
								// list was empty - an ERROR
								System.out.println( "FIXME: a node had just one inherit list, which was empty");
							}
							return;
						}
							
							
							
							
							
/*
//							if(1 == data.get(idxparent).size()){
							if(1 == parent.size()){
// TODO check pos > 1
								this.idom = data.get(idxparent).get(idxinherit).get(pos -1 );
								return;
//								return data.get(idxparent).get(idxinherits).get(pos -1 );
							}else{
								data.get(idxparent).remove(idxinherit);
								break;
							}
						}
//*/


					}

				}
			}
		}
	}


	public void dotPrint(){
// TODO check, another label?
//*
		System.out.println( "  node" + Id + " [label = \"block" + Id + "\"]");
/*/
		System.out.println( ControlFlowGraphExtractor.dotPrintBlock(Id, blockinstructions));
//*/
	}

	public Integer id(){
		return Id;
	}
}
