package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

public class Node {
	private ArrayList<AbstractInsnNode> blockinstructions;
	private final Integer Id; // TODO how to identify this node
	private Integer idom; // immediate dominator
	private List<List<Integer>> inheritage;

	public Node( ArrayList<AbstractInsnNode> blockinstructions, final Integer startId){
		this.blockinstructions = blockinstructions;
		this.Id = startId;
		this.inheritage = new ArrayList<List<Integer>>();
//		this.inheritage.add(new ArrayList<Integer>());
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

	public Integer findDominator( List<Node> parents){
		// list of parents, each parent lists severan "inheritages", each is a list of Integers
		ArrayList<ArrayList<ArrayList<Integer>>> data = new ArrayList<ArrayList<ArrayList<Integer>>>();

		// init
		for( int idxparent=0; idxparent < parents.size(); ++idxparent){
			// per parent
			Node parent = parents.get(idxparent);
			data.add(new ArrayList<ArrayList<Integer>>());

			for( int idxinherits=0; idxinherits < parent.getInheritage().size(); ++idxinherits){

				// per descendence lists
				data.add(new ArrayList<ArrayList<Integer>>());
				for( int idxids=0; idxids < parent.getInheritage().get(idxinherits).size(); ++idxids){

					// per item in particular descendence list
					data.get(idxparent).get(idxinherits).add( parent.getInheritage().get(idxinherits).get(idxids) );
				}
			}
		}


		// operate
		

//		for( int pos=1; true; ++pos ){ // TODO uncomment
		for( int pos=1; pos<10; ++pos ){ // TODO debug, rm
			for( int idxparent=0; idxparent < data.size(); ++idxparent){
				for( int idxinherits=data.get(idxparent).size()-1; idxinherits > 0; --idxinherits){

// TODO size checks
					Integer id = -1;
					try{
						id = data.get(idxparent).get(idxinherits).get(pos);
					}catch( Exception exp){
						if(1 == data.get(idxparent).size() ){
							return data.get(idxparent).get(idxinherits).get(pos-1);
						}else{
							data.get(idxparent).remove(idxinherits);
						}
						continue;
					}

					// check now if id is containent in other parent at this position or not ( = discard whole vector)
					for( int jdxparent = idxparent + 1; jdxparent < data.size(); ++jdxparent){
						ArrayList<Integer> jids = new ArrayList<Integer>();
						for( int jdxinherits = 0; jdxinherits < data.get(jdxparent).size(); ++jdxinherits){
							jids.add( data.get(jdxparent).get(jdxinherits).get(pos) );
						}

						if(-1 == jids.indexOf(id)){
							// not contained = remove whole vector
							if(1 == data.get(idxparent).size()){
// TODO check pos > 1
								return data.get(idxparent).get(idxinherits).get(pos -1 );
							}else{
								data.get(idxparent).remove(idxinherits);
								break;
							}
						}
					}

				}
			}
		}
		return -1;
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
