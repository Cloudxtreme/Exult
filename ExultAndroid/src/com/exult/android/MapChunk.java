package com.exult.android;
import java.util.Vector;
import java.util.Arrays;
import java.util.HashSet;

public final class MapChunk extends GameSingletons {
	private GameMap map;				// Map we're a part of.
	private ChunkTerrain terrain;		// Flat landscape tiles.
	private ObjectList objects;			// -'Flat'  obs. (lift=0,ht=0) stored 1st.
	private GameObject firstNonflat;			// ->first nonflat in 'objects'.
	// Counts of overlapping objects from chunks below, to right.
	private short fromBelow, fromRight, fromBelowRight;
	private byte dungeonLevels[];	// A 'dungeon' level value for each tile (4 bit).
	private boolean roof;			// 1 if a roof present.
	// # light sources in chunk.
	private byte dungeonLights, nonDungeonLights;
	private short cx, cy;
	// Data that used to be in Chunk_cache:
	// Each member of 'blocked' is 16x16 tiles, with each short value using 2
	// bits for each bit level for #objs blocking there.
	private Vector<short[]> blocked;	// Each element represents the chunk for 8 lifts.
	private HashSet<GameObject> doors;
	private short eggs[];				// Eggs which influence this chunk.
	// Mask gives low bits (b0) for a given # of ztiles:
	private static final int tmasks[] = {
		0x0,
        0x1,
        0x5,
       0x15,
       0x55,
      0x155,
      0x555,
     0x1555};
	private static Rectangle footRect = new Rectangle(),	// Temp.
							 tilesRect = new Rectangle();
	// Temp. storage for 'blocked' flags for a single tile.
	private static int tflags[] = new int[256/8];
	private static int tflagsMaxz;
	public MapChunk(GameMap m, int chx, int chy) {
		map = m;
		cx = (short)chx;
		cy = (short)chy;
		terrain = null;
		objects = new ObjectList();
	}
	public final int getCx() {
		return cx;
	}
	public final int getCy() {
		return cy;
	}
	public final GameMap getMap() {
		return map;
	}
	public final ObjectList getObjects() {
		return objects;
	}
	public final ObjectList.FlatObjectIterator getFlatObjectIterator() {
		return objects.getFlatIterator(firstNonflat);
	}
	public final ObjectList.NonflatObjectIterator getNonflatObjectIterator() {
		return objects.getNonflatIterator(firstNonflat);
	}
	public final void setTerrain(ChunkTerrain ter) {
		if (terrain != null) {
			terrain.removeClient();
			// ++++++++REMOVE OBJS.?
		}
		terrain = ter;
		terrain.addClient();	
		// Get RLE objects in chunk.
		ShapeID id = new ShapeID();
		for (int tiley = 0; tiley < EConst.c_tiles_per_chunk; tiley++)
			for (int tilex = 0; tilex < EConst.c_tiles_per_chunk; tilex++) {
				int shapenum = ter.getShapeNum(tilex, tiley);
				if (shapenum >= EConst.c_first_obj_shape) {
					ShapeInfo info = ShapesVgaFile.getInfo(shapenum);
					int framenum = ter.getFrameNum(tilex, tiley);
					GameObject obj = /* +++++++ info.is_animated() ?
						new Animated_object(shapenum,
						    	framenum, tilex, tiley)
						: */ new TerrainGameObject(shapenum,
						    	framenum, tilex, tiley, 0);
					add(obj);
				}
			}
	}
	/*
	 *	Add rendering dependencies for a new object.
	 */
	private void addDependencies(GameObject newobj, GameObject.OrderingInfo newinfo) {
		GameObject obj;		// Figure dependencies.
		ObjectList.NonflatObjectIterator next = objects.getNonflatIterator(firstNonflat);
		while ((obj = next.next()) != null) {
			/* Compare returns -1 if lt, 0 if dont_care, 1 if gt. */
			int newcmp = GameObject.compare(newinfo, obj);
			int cmp = newcmp == -1 ? 1 : newcmp == 1 ? 0 : -1;
			if (cmp == 0) {		// Bigger than this object?
				newobj.addDependency(obj);
				obj.addDependor(newobj);
			} else if (cmp == 1) {	// Smaller than?
				obj.addDependency(newobj);
				newobj.addDependor(obj);
			}
		}
	}
	/*
	 *	Add rendering dependencies for a new object to another chunk.
	 *	NOTE:  This is static.
	 *
	 *	Output:	->chunk that was checked.
	 */

	private MapChunk addOutsideDependencies
		(
		int cx, int cy,			// Chunk to check.
		GameObject newobj,		// Object to add.
		GameObject.OrderingInfo newinfo		// Info. for new object's ordering.
		)
		{
		MapChunk chunk = gwin.getMap().getChunk(cx, cy);
		chunk.addDependencies(newobj, newinfo);
		return chunk;
		}

	public void add(GameObject newobj) {
		newobj.setChunk(this);
		GameObject.OrderingInfo ord = newobj.getOrderingInfo1();
		if (firstNonflat != null)
			objects.insertBefore(newobj, firstNonflat);
		else
			objects.append(newobj);
		//+++++++FINISH - Lots of code involving sorting objects, etc.
		if (newobj.getLift() > 0 || ord.info.get3dHeight() > 0) {
					// Deal with dependencies.
					// First this chunk.
			addDependencies(newobj, ord);
			if (fromBelow > 0)		// Overlaps from below?
				addOutsideDependencies(cx, EConst.INCR_CHUNK(cy), newobj, ord);
			if (fromRight > 0)		// Overlaps from right?
				addOutsideDependencies(EConst.INCR_CHUNK(cx), cy, newobj, ord);
			if (fromBelowRight > 0)
				addOutsideDependencies(EConst.INCR_CHUNK(cx), 
					EConst.INCR_CHUNK(cy), newobj, ord);
					// See if newobj extends outside.
			/* Let's try boundary. YES.  This helps with statues through roofs!*/
			boolean ext_left = (newobj.getTx() - ord.xs) < 0 && cx > 0;
			boolean ext_above = (newobj.getTy() - ord.ys) < 0 && cy > 0;
			if (ext_left) {
				addOutsideDependencies(EConst.DECR_CHUNK(cx), cy, 
						newobj, ord).fromRight++;
				if (ext_above)
					addOutsideDependencies(EConst.DECR_CHUNK(cx), 
							 EConst.DECR_CHUNK(cy), newobj, ord).fromBelowRight++;
			}
			if (ext_above)
				addOutsideDependencies(cx, EConst.DECR_CHUNK(cy),
					newobj, ord).fromBelow++;
			firstNonflat = newobj;	// Inserted before old first_nonflat.
		}
		addObjectBlocking(newobj);
		/* +++++++++FINISH
		if (ord.info.is_light_source())	// Count light sources.
			{
			if (dungeon_levels && is_dungeon(newobj.get_tx(),
							newobj.get_ty()))
				dungeon_lights++;
			else
				non_dungeon_lights++;
			}
		 */
		if (newobj.getLift() >= 5) {	// Looks like a roof?
			if (ord.info.getShapeClass() == ShapeInfo.building)
				roof = true;
		}
	}
	public void remove(GameObject remove) {
		removeObjectBlocking(remove);
		remove.clearDependencies();	// Remove all dependencies.
		ShapeInfo info = remove.getInfo();
						// See if it extends outside.
		int frame = remove.getFrameNum(), tx = remove.getTx(),
						ty = remove.getTy();
		/* Let's try boundary. YES.  Helps with statues through roofs. */
		boolean ext_left = (tx - info.get3dXtiles(frame)) < 0 && cx > 0;
		boolean ext_above = (ty - info.get3dYtiles(frame)) < 0 && cy > 0;
		if (ext_left) {
			gmap.getChunk(cx - 1, cy).fromBelowRight--;
			if (ext_above)
				gmap.getChunk(cx - 1, cy - 1).fromBelowRight--;
		}
		if (ext_above)
			gmap.getChunk(cx, cy - 1).fromBelow--;
		if (info.isLightSource()) {	// Count light sources.
			if (dungeonLevels != null /* && +++++ is_dungeon(tx, ty) */)
				dungeonLights--;
			else
				nonDungeonLights--;
		}
		if (remove == firstNonflat)	{ // First nonflat?
			firstNonflat = remove.getNext();
			if (firstNonflat == objects.getFirst())
				firstNonflat = null;
			}
		objects.remove(remove);		// Remove from list.
		remove.setInvalid();		// No longer part of world.
	}
	public ImageBuf getRenderedFlats() {
		return terrain != null ? terrain.getRenderedFlats() : null;
	}
	public void setupCache() {
		//++++++++++++FINISH
	}
	/*
	 *	Set (actually, increment count) for a given tile.
	 *	Want:	00 => 01,	01 => 10,
	 *		10 => 11,	11 => 11.
	 *	So:	newb0 = !b0 OR b1,
	 *		newb1 =  b1 OR b0
	 */
	private void setBlockedTile
		(
		short blocked[],		// 16x16 flags,
		int tx, int ty,			// Tile #'s (0-15).
		int lift,			// Starting lift to set.
		int ztiles			// # tiles along z-axis.
		) {
		int ind = ty*EConst.c_tiles_per_chunk + tx;
		int val = blocked[ind]&0xffff;
							// Get mask for the bit0's:
		int mask0 = (tmasks[ztiles]<<(2*lift))&0xffff;
		int mask1 = mask0<<1;	// Mask for the bit1's.
		int val0s = val&mask0;
		int Nval0s = (~val)&mask0;
		int val1s = val&mask1;
		int newval = val1s | (val0s<<1) | Nval0s | (val1s>>1);
							// Replace old values with new.
		blocked[ind] = (short)((val&~(mask0|mask1)) | newval);
	}
	/*
	 *	Clear (actually, decrement count) for a given tile.
	 *	Want:	00 => 00,	01 => 00,
	 *		10 => 01,	11 => 10.
	 *	So:	newb0 =  b1 AND !b0
	 *		newb1 =  b1 AND  b0
	 */
	private void clearBlockedTile
		(
		short blocked[],		// 16x16 flags,
		int tx, int ty,			// Tile #'s (0-15).
		int lift,			// Starting lift to set.
		int ztiles			// # tiles along z-axis.
		)
		{
		int ind = ty*EConst.c_tiles_per_chunk + tx;
		int val = blocked[ind]&0xffff;
						// Get mask for the bit0's:
		int mask0 = (tmasks[ztiles]<<(2*lift))&0xffff;
		int mask1 = mask0<<1;	// Mask for the bit1's.
		int val0s = val&mask0;
		int Nval0s = (~val)&mask0;
		int val1s = val&mask1;
		int newval = (val1s & (val0s<<1)) | ((val1s>>1) & Nval0s);
						// Replace old values with new.
		blocked[ind] = (short)((val&~(mask0|mask1)) | newval);
	}
	/*
	 *	Create new blocked flags for a given z-level, where each level
	 *	covers 8 lifts.
	 */
	private short[] newBlockedLevel
		(
		int zlevel
		) {
		if (zlevel >= blocked.size())
			blocked.setSize(zlevel + 1);
		short[] block = new short[256];
		blocked.setElementAt(block, zlevel);
//		std::cout << "***Creating block for level " << zlevel << ", cache = " 
//			<< (void*)this << std::endl;
		return block;
	}
	private short[] needBlockedLevel(int zlevel) {
		if (blocked == null)
			blocked = new Vector<short[]>();
		if (zlevel < blocked.size()) {
			short[] block = blocked.elementAt(zlevel);
			if (block != null)
				return block;
		}
		return newBlockedLevel(zlevel);
	}
	/*
	 *	Set/unset the blocked flags in a region.
	 */
	private void setBlocked
		(
		int startx, int starty,		// Starting tile #'s.
		int endx, int endy,		// Ending tile #'s.
		int lift, int ztiles		// Lift, height info.
		) {
		int z = lift;
		while (ztiles > 0) {
			int zlevel = z/8, thisz = z%8, zcnt = 8 - thisz;
			if (ztiles < zcnt)
				zcnt = ztiles;
			short block[] = needBlockedLevel(zlevel);
			for (int y = starty; y <= endy; y++)
				for (int x = startx; x <= endx; x++)
					setBlockedTile(block, x, y, thisz, zcnt);
			z += zcnt;
			ztiles -= zcnt;
		}
	}
	private void clearBlocked
		(
		int startx, int starty,		// Starting tile #'s.
		int endx, int endy,		// Ending tile #'s.
		int lift, int ztiles		// Lift, height info.
		) {
		int z = lift;
		while (ztiles > 0) {
			int zlevel = z/8;
			int thisz = z%8, zcnt = 8 - thisz;
			if (zlevel >= blocked.size())
				break;		// All done.
			if (ztiles < zcnt)
				zcnt = ztiles;
			short block[] = blocked.elementAt(zlevel);
			if (block != null) {
				for (int y = starty; y <= endy; y++)
					for (int x = startx; x <= endx; x++)
						clearBlockedTile(block,x, y, thisz, zcnt);
			}
			z += zcnt;
			ztiles -= zcnt;
		}
	}
	// Process 'blocked' for an object that's added.
	private void addObjectBlocking(GameObject obj) {
		ShapeInfo info = obj.getInfo();
		if (info.isDoor()) {		// Special door list.
			if (doors == null)
				doors = new HashSet<GameObject>();
			doors.add(obj);
		}
		int ztiles = info.get3dHeight(); 
		if (ztiles == 0 || !info.isSolid())
			return;			// Skip if not an obstacle.
		obj.getFootprint(footRect);
		int endx = obj.getTx();	// Lower-right corner of obj.
		int endy = obj.getTy();
		int lift = obj.getLift();
		// Simple case.
		if (footRect.w == 1 && footRect.h == 1 && ztiles <= 8-lift%8) {
			setBlockedTile(needBlockedLevel(lift/8), 
					endx, endy, lift%8, ztiles);
			return;
		}				// Go through intersected chunks.
		ChunkIntersectIterator next_chunk = 
							new ChunkIntersectIterator(footRect);
		MapChunk chunk;
		while ((chunk = next_chunk.getNext(tilesRect)) != null)
			chunk.setBlocked(tilesRect.x, tilesRect.y, 
				tilesRect.x + tilesRect.w - 1, tilesRect.y + tilesRect.h - 1, 
				lift, ztiles);
	}
	// Process 'blocked' for an object that's removed.
	private void removeObjectBlocking(GameObject obj) {
		ShapeInfo info = obj.getInfo();
		if (info.isDoor())		// Special door list.
			doors.remove(obj);
		int ztiles = info.get3dHeight(); 
		if (ztiles == 0 || !info.isSolid())
			return;			// Skip if not an obstacle.
		obj.getFootprint(footRect);
		int endx = obj.getTx();	// Lower-right corner of obj.
		int endy = obj.getTy();
		int lift = obj.getLift();
		// Simple case.
		if (footRect.w == 1 && footRect.h == 1 && ztiles <= 8-lift%8) {
			short[] block = blocked.elementAt(lift/8);
			if (block != null)
				clearBlockedTile(block, endx, endy, lift%8, ztiles);
			return;
		}				// Go through intersected chunks.
		ChunkIntersectIterator next_chunk = 
							new ChunkIntersectIterator(footRect);
		MapChunk chunk;
		while ((chunk = next_chunk.getNext(tilesRect)) != null)
			chunk.clearBlocked(tilesRect.x, tilesRect.y, 
				tilesRect.x + tilesRect.w - 1, tilesRect.y + tilesRect.h - 1, 
				lift, ztiles);
	}
	private void setTflags(int tx, int ty, int maxz) {
		int zlevel = maxz/8, bsize = blocked.size();
		if (zlevel >= bsize) {
			Arrays.fill(tflags, bsize, zlevel + 1, 0);
			zlevel = bsize - 1;
			}
		while (zlevel >= 0) {
			short block[] = blocked.elementAt(zlevel);
			tflags[zlevel--] = block != null 
						? block[ty*EConst.c_tiles_per_chunk + tx] : 0;
		}
		tflagsMaxz = maxz;
	}
	//	Test for given z-coord. (lift)
	private static boolean testTflags(int i) {
		return (tflags[(i)/8] & (3<<(2*((i)%8)))) != 0;
	}
	/*
	 *	Get lowest blocked lift above a given level for a given tile.
	 *
	 *	Output:	Lowest lift that's blocked by an object, or -1 if none.
	 */
	private int getLowestBlocked
		(
		int lift,			// Look above this lift.
		int tx, int ty			// Square to test.
		) {
		setTflags(tx, ty, 255);	// FOR NOW, look up to max.
		int i;				// Look upward.
		for (i = lift; i <= tflagsMaxz && !testTflags(i); i++)
			;
		if (i > tflagsMaxz) return -1;
		return i;
		}
	/*
	 *  Finds if there is a 'roof' above lift in tile (tx, ty)
	 *  of the chunk. Point is taken 4 above lift
	 *
	 *  Roof can be any object, not just a literal roof
	 *
	 *  Output: height of the roof.
	 *  A return of 31 means no roof
	 *
	 */
	public int isRoof(int tx, int ty, int lift) {
		/* Might be lying on bed at lift==2. */
		int height = getLowestBlocked (lift+4, tx, ty);
		if (height == -1) return 255;
		return height;
	}
	/*
	 *	Here's an iterator that takes a rectangle of tiles, and sequentially
	 *	returns the interesection of that rectangle with each chunk that it
	 *	touches.
	 */
	public static class ChunkIntersectIterator {
		private Rectangle tiles;		// Original rect, shifted -cx, -cy.
		private int start_tx;			// Saves start of tx in tiles.
						// Chunk #'s covered:
		private int startcx, stopcx, stopcy;
		private int curcx, curcy;		// Next chunk to return.
		ChunkIntersectIterator(Rectangle t) {
			tiles = new Rectangle(); tiles.set(t);
			startcx = t.x/EConst.c_tiles_per_chunk;
			stopcx = EConst.INCR_CHUNK((t.x + t.w - 1)/EConst.c_tiles_per_chunk);
			stopcy = EConst.INCR_CHUNK((t.y + t.h - 1)/EConst.c_tiles_per_chunk);
			curcy = t.y/EConst.c_tiles_per_chunk;
			curcx = startcx;
			tiles.shift(-curcx*EConst.c_tiles_per_chunk,
						-curcy*EConst.c_tiles_per_chunk);
			start_tx = tiles.x;
			if (t.x < 0 || t.y < 0) {		// Empty to begin with.
				curcx = stopcx;
				curcy = stopcy;
				}
		}
		// Intersect is ranged within chunk.
		MapChunk getNext(Rectangle intersect) {
			if (curcx == stopcx) {	// End of row?
				if (curcy == stopcy)
					return null;
				else {
					tiles.y -= EConst.c_tiles_per_chunk;
					tiles.x = start_tx;
					curcy = EConst.INCR_CHUNK(curcy);
					if (curcy == stopcy)
						return null;
					curcx = startcx;
				}
			}
			intersect.set(0, 0, EConst.c_tiles_per_chunk, EConst.c_tiles_per_chunk);
						// Intersect given rect. with chunk.
			intersect.intersect(tiles);
			MapChunk chunk = gmap.getChunk(curcx, curcy);
			curcx = EConst.INCR_CHUNK(curcx);
			tiles.x -= EConst.c_tiles_per_chunk;
			return chunk;
		}
	}

}
