package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

public class ControlFlowGraphExtractor {
	private ArrayList< ArrayList<AbstractInsnNode>> listlist;
	private InsnList instructions;
	private ArrayList<Integer> forwardJump;

	public ControlFlowGraphExtractor( final InsnList instructions ){
		listlist = new ArrayList< ArrayList<AbstractInsnNode>>();
		listlist.add(new ArrayList<AbstractInsnNode>());
		this.instructions = instructions;
		this.forwardJump = new ArrayList<Integer>();

		initInstructions();
	}

	private void initInstructions(){
//	public void appendInstruction( final AbstractInsnNode ins, final int idx ){
		for( int idx = 0; idx < this.instructions.size(); ++idx ){
			AbstractInsnNode ins = this.instructions.get(idx);
			if( ins.getType() == AbstractInsnNode.JUMP_INSN ){
				LabelNode target = ((JumpInsnNode) ins).label;
				int targetIdx = instructions.indexOf(target);

				if( targetIdx < idx ){
					// backward jump
					int idxLastFirstIns = 0;
					for( int listIdx = 1; listIdx < this.listlist.size(); ++listIdx ){
						int idxFirstIns = this.listlist.indexOf( this.listlist.get(listIdx).get(0) );
						if( targetIdx < idxFirstIns ){
							int start = idxLastFirstIns;
							int diff = targetIdx - start;
							this.listlist.add( listIdx, new ArrayList<AbstractInsnNode>( this.listlist.get(listIdx-1).subList( diff, this.listlist.get(listIdx-1).size())));
							this.listlist.get( listIdx-1 ).removeAll( this.listlist.get( listIdx ) );
							break;
						}
						idxLastFirstIns = idxFirstIns;
					}
				}else if( targetIdx > idx){
					// forward jump
					this.forwardJump.add(new Integer(targetIdx));
				} // no else: jump to next element
				
				// create a new basic block
				listlist.add(new ArrayList<AbstractInsnNode>());
			}

			// append
			if( -1 < this.forwardJump.indexOf( idx ) ){
				// there was a forward jump to this address
				this.listlist.add( new ArrayList<AbstractInsnNode>() );
			}

			// append instruction at last position
			listlist.get( listlist.size()-1 ).add( ins );
		}
	}

	public void dottyPrint(){
		for( int idx = 0; idx < listlist.size(); ++idx ){
			for( int jdx = 0; jdx < listlist.get(idx).size(); ++jdx){
// TODO improve
				int opcode = listlist.get(idx).get(jdx).getOpcode();
				System.out.print( opcode );
				System.out.print( " ");
			}
			System.out.println("");
		}
	}

}
