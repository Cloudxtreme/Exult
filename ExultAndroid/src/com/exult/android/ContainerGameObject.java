package com.exult.android;

public class ContainerGameObject extends IregGameObject {
	private int volumeUsed;		// Amount of volume occupied.
	private byte resistance;	// Resistance to attack.
	protected ObjectList objects;
	
	public ContainerGameObject(int shapenum, int framenum, int tilex, 
			int tiley, int lft,	int res) {
		super(shapenum, framenum, tilex, tiley, lft);
		resistance = (byte)res;
	}
	public boolean add(GameObject obj, boolean dont_check,
			boolean combine, boolean noset) {
		//+++++FINISH  For now, throw away.
		return true;
	}
	public void changeMemberShape(GameObject obj, int newshape) {
		/* ++++++FINISH
		int oldvol = obj.getVolume();
		obj.setShape(newshape);
						// Update total volume.
		volume_used += obj.getVolume() - oldvol;
		*/
	}
}