import java.util.ArrayList;

/* Data structure experiment
 * 
 * implemented is an ArrayList of several ArrayLists, in parallel all placed
 * elements are referred also by a separate arraylist and their index.
 * 
 * @author: Lothar Rubusch
 */
public class Datastructure {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Element a = new Element("a");
		final Element b = new Element("b");
		final Element c = new Element("c", 1);
		final Element d = new Element("d");
		final Element e = new Element("e");
		final Element f = new Element("f");

		// register in list
		final ArrayList<Element> elements = new ArrayList<Element>();
		elements.add(a);
		elements.add(b);
		elements.add(c);
		elements.add(d);
		elements.add(e);
		elements.add(f);

		// organize in Array of Array
		final ArrayList<ArrayList<Element>> listlist = new ArrayList<ArrayList<Element>>();

		// 1) append all elements to list of list, depending on their
		// instruction, in case build up new block
		listlist.add(new ArrayList<Element>());
		listlist.get(0).add(a);
		listlist.get(0).add(b);
		listlist.get(0).add(c);

		listlist.add(new ArrayList<Element>());
		listlist.get(1).add(d);
		listlist.get(1).add(e);
		listlist.get(1).add(f);

		// 2) go through list to find forwared / backward branching
		for( Element elem : elements ){
			int target;
			if( -1 != (target = elem.getTarget() )){
				// split blocklist starting the new list with 'target'
				for( int idx = 1; idx < listlist.size(); ++idx ){
					if( target < elements.indexOf( listlist.get(idx).get(0) )){
						int startidx = elements.indexOf( listlist.get(idx - 1).get(0));
						int diffidx = target - startidx;

						// insert new list, a sublist starting from split element
						listlist.add(idx, new ArrayList<Element>( listlist.get(idx-1).subList( diffidx, listlist.get(idx-1).size() ) ) );

						// remove sublist from original ArrayList
						listlist.get(idx-1).removeAll( listlist.get(idx) );
						break;
					}
				}
			}
		}

		// print
		for( int idx = 0; idx < listlist.size(); ++idx ){
			for( int jdx = 0; jdx < listlist.get(idx).size(); ++jdx ){
				System.out.print( listlist.get(idx).get(jdx).getContent() + " " );
			}
			System.out.println("");
		}

		System.out.println( "READY." );
	}

}
