package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Node {
	private ArrayList<AbstractInsnNode> blockinstructions;
	private final Integer Id; // TODO how to identify this node
// TODO why is idom a Integer, and not a Node?
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
		}

		// find dominator (reset dominator)
		identifyDominator( parents );
System.out.println( "XXX result idom = " + this.idom); // XXX
	}
//*/

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

	public void identifyDominator( List<Node> parents){


// TODO rm
// debugging dump
System.out.println( "AAA ---");
for( int idxparent=0; idxparent < parents.size(); ++idxparent ){
	System.out.println( "parent " + idxparent);
	final List<List<Integer>> parent = parents.get(idxparent).getInheritage();
	for( int idxinherit=0; idxinherit < parent.size(); ++idxinherit){
		System.out.print("\tinherit" + idxinherit + ": ");
		final List<Integer> inherit = parent.get(idxinherit);
		for( int idxid=0; idxid < inherit.size(); ++idxid){
			System.out.print( inherit.get(idxid) + " ");
		}
		System.out.println( "" );
	}
	System.out.println( "" );
}
System.out.println( "BBB ---");




		
		if( 2 > parents.size() ){
			System.out.println("FATAL - compare at least 2 nodes, passed were " + parents.size());
		}

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
							System.out.println( "FIXME: a node had just one inherit list, which was empty");
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
								System.out.println( "FIXME: a node had just one inherit list, which was empty");
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
