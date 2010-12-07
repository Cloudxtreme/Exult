package com.exult.android;
import java.util.Vector;
import java.util.LinkedList;
import android.graphics.Point;

public class UsecodeIntrinsics extends GameSingletons {
	private static Tile tempTile = new Tile();
	private static Rectangle tempRect = new Rectangle();
	private static Vector<GameObject> foundVec = new Vector<GameObject>();
	// Stack of last items created with intrins. x24.
	private static LinkedList<GameObject> last_created = new LinkedList<GameObject>();
	private static final GameObject getItem(UsecodeValue v) {
		return ucmachine.get_item(v);
	}
	private static GameObject interceptItem;
	private static Tile interceptTile;
	
	/*
	 * The intrinsics:
	 */
	private final UsecodeValue getRandom(UsecodeValue p0) {
		int range = p0.getIntValue();
		if (range == 0)
			return UsecodeValue.getZero();
		return new UsecodeValue.IntValue(1 + (EUtil.rand() % range));
	}
	private void createScript(UsecodeValue objval, UsecodeValue codeval,
														int delay) {
			GameObject obj = getItem(objval);
							// Pure kludge for SI wells:
			/* ++++++++++FINISH
			if (objval.getArraySize() == 2 && 
					Game::get_game_type() == SERPENT_ISLE &&
					obj != null && obj.getShapeNum() == 470 && 
					obj.getLift() == 0) {
							// We want the TOP of the well.
				UsecodeValue v2 = objval.getElem(1);
				GameObject o2 = getItem(v2);
				if (o2.getShapeNum() == obj.getShapeNum() && o2.getLift() == 2) {
					objval = v2;
					obj = o2;
				}
			}
			*/
			if (obj == null) {
				System.out.println("Can't create script for NULL object");
				return;
			}
			UsecodeScript script = new UsecodeScript(obj, codeval);
			script.start(delay);
			}
	private final UsecodeValue executeUsecodeArray(UsecodeValue p0,
					UsecodeValue p1) {
						// Start on next tick.
		createScript(p0, p1, 1);
		return UsecodeValue.getOne();
	}
	public final UsecodeValue delayedExecuteUsecodeArray(UsecodeValue p0,
					UsecodeValue p1, UsecodeValue p2) {
		// Delay = .20 sec.?
						// Special problem with inf. loop:
		/* +++++STILL NEEDED?
		if (Game::get_game_type() == BLACK_GATE &&
		    event == UsecodeMachine.internal_exec && 
		    p1.getArrayAize() == 3 &&
		    parms1.getElem(2).getIntValue() == 0x6f7)
			return UsecodeValue.getZero();
		*/
		int delay = p2.getIntValue();
		createScript(p0, p1, delay);
		return UsecodeValue.getOne();
	}

	private int getFaceShape(UsecodeValue arg1, Actor npc, int frame) {
		int shape = -1;
		if (arg1 instanceof UsecodeValue.IntValue) {
			shape = Math.abs(arg1.getIntValue());
			if (shape == 356)	// Avatar.
				shape = 0;
		} else if (npc != null)
			shape = npc.getFaceShapeNum();
		if (shape < 0)	// No need to do anything else.
			return shape;
		// Checks for Petra flag.
		/*++++++++++++
		shape = Shapeinfo_lookup::GetFaceReplacement(shape);

		Actor iact;
		if (Game::get_game_type() == SERPENT_ISLE)
				{			// Special case: Nightmare Smith.
							//   (But don't mess up Guardian.)
				if (shape == 296 && this.frame.caller_item &&
				    (iact = this.frame.caller_item.as_actor()) != 0 &&
				    iact.get_npc_num() == 277)
					shape = 277;
				}

			// Another special case: map face shape 0 to
			// the avatar's correct face shape and frame:
			if (shape == 0)
				{
				Actor *ava = gwin.get_main_actor();
				bool sishapes = Shape_manager::get_instance().have_si_shapes();
				Skin_data *skin = Shapeinfo_lookup::GetSkinInfoSafe(
						ava.get_skin_color(), npc ? (npc.get_type_flag(Actor::tf_sex)!=0)
							: (ava.get_type_flag(Actor::tf_sex)!=0), sishapes);
				if (gwin.get_main_actor().get_flag(Obj_flags::tattooed))
					{
					shape = skin.alter_face_shape;
					frame = skin.alter_face_frame;
					}
				else
					{
					shape = skin.face_shape;
					frame = skin.face_frame;
					}
				}
		*/
		return shape;
	}
	private final void showNpcFace(UsecodeValue p0, UsecodeValue p1,
				int slot) {	// 0, 1, or -1 to find free spot.
		ucmachine.show_pending_text();
		GameObject item = getItem(p0);
		Actor npc = item.asActor();
		int frame = p1.getIntValue();
		int shape = getFaceShape(p0, npc, frame);
		if (shape < 0)
			return;
	
		if (true /* +++++ Game::get_game_type() == BLACK_GATE*/ && npc != null) {
			// Only do this if the NPC is the caller item.
			if (npc.getNpcNum() != -1) 
				npc.setFlag (GameObject.met);
		}
		if (conv.getNumFacesOnScreen() == 0)
			eman.removeTextEffects();
		// Only non persistent
		/* ++++++++++++
		if (gumpman.showing_gumps(true)) {
			gumpman.close_all_gumps();
			gwin.set_all_dirty();
			init_conversation();	// jsf-Added 4/20/01 for SI-Lydia.
		}
		*/
		gwin.paintDirty();
		conv.showFace(shape, frame, slot);
		//	user_choice = 0;		// Seems like a good idea.
		// Also seems to create a conversation bug in Test of Love :-(
	}

	private final void removeNpcFace(UsecodeValue p0) {
		ucmachine.show_pending_text();
		GameObject item = p0.getObjectValue();
		Actor npc = item.asActor();
		int shape = getFaceShape(p0, npc, 0);
		if (shape < 0)
			return;
		conv.removeFace(shape);
	}

	private final void addAnswer(UsecodeValue p0) {
		conv.addAnswer(p0);
		//	user_choice = 0;
	}

	private final void removeAnswer(UsecodeValue p0) {
		conv.removeAnswer(p0);
	// Commented out 'user_choice = 0' 8/3/00 for Tseramed conversation.
//		user_choice = 0;
	}

	private final void pushAnswers() {
		conv.pushAnswers();
	}

	private final void popAnswers() {
		if (!conv.stackEmpty()) {
			conv.popAnswers();
			conv.setUserChoice(null);	// Added 7/24/2000.
		}
	}

	private final void clearAnswers() {
		conv.clearAnswers();
	}
	private static UsecodeValue selectFromMenu() {
		conv.setUserChoice(null);
		UsecodeValue u = new UsecodeValue.StringValue(
									ucmachine.get_user_choice());
		conv.setUserChoice(null);
		return(u);
	}

	private static UsecodeValue selectFromMenu2() {
		// Return index (1-n) of choice.
		conv.setUserChoice(null);
		UsecodeValue val = new UsecodeValue.IntValue(
								ucmachine.get_user_choice_num() + 1);
		conv.setUserChoice(null);
		return(val);
	}
	private final void setItemShape(UsecodeValue itemVal, UsecodeValue shapeVal) {
		int shape = shapeVal.getIntValue();
		GameObject item = getItem(itemVal);
		if (item == null)
			return;
						// See if light turned on/off.
		boolean light_changed = item.getInfo().isLightSource() !=
				    ShapeID.getInfo(shape).isLightSource();
		ContainerGameObject owner = item.getOwner();
		if (owner != null) {		// Inside something?
			owner.changeMemberShape(item, shape);
			if (light_changed)	// Maybe we should repaint all.
				gwin.paint();	// Repaint finds all lights.
			else {
				/* +++++++++FINISH
				Gump *gump = gumpman.find_gump(item);
				if (gump)
					gump.paint();
				*/
			}
			return;
		}
		gwin.addDirty(item);
		MapChunk chunk = item.getChunk();	// Get chunk it's in.
		chunk.remove(item);		// Remove and add to update cache.
		item.setShape(shape);
		chunk.add(item);
		gwin.addDirty(item);
		if (light_changed)
			gwin.paint();		// Complete repaint refigures lights.
	}
	private final UsecodeValue findNearest(UsecodeValue objVal, 
			UsecodeValue shapeVal, UsecodeValue distVal) {
		// Think it rets. nearest obj. near parm0.
		GameObject obj = getItem(objVal);
		if (obj == null)
			return UsecodeValue.getNullObj();
		foundVec.clear();
		obj = obj.getOutermost();	// Might be inside something.
		int dist = distVal.getIntValue();
		int shnum = shapeVal.getIntValue();
						// Kludge for Test of Courage:
		if (ucmachine.getCurrentFunction() == 0x70a && shnum == 0x9a && dist == 0)
			dist = 16;		// Mage may have wandered.
		obj.getTile(tempTile);
		int cnt = gmap.findNearby(foundVec, tempTile, shnum, dist, 0);
		GameObject closest = null;
		int bestdist = 100000;// Distance-squared in tiles.
		int tx1 = obj.getTileX(), ty1 = obj.getTileY(), tz1 = obj.getLift();
		for (int i = 0; i < cnt; ++i) {
			GameObject each = foundVec.elementAt(i);
			each.getTile(tempTile);
			int dx = tx1 - tempTile.tx, dy = ty1 - tempTile.ty, 
				dz = tz1 - tempTile.tz;
			dist = dx*dx + dy*dy + dz*dz;
			if (dist < bestdist) {
				bestdist = dist;
				closest = each;
			}
		}
		return new UsecodeValue.ObjectValue(closest);
	}
	private final UsecodeValue dieRoll(UsecodeValue p0, UsecodeValue p1) {
		// Rand. # within range.
		int low = p0.getIntValue();
		int high = p1.getIntValue();
		if (low > high){
			int tmp = low;
			low = high;
			high = tmp;
		}
		int val = (EUtil.rand() % (high - low + 1)) + low;
		return new UsecodeValue.IntValue(val);
	}
	private final UsecodeValue getItemShape(UsecodeValue p0) {
		GameObject obj = getItem(p0);
		return obj == null ? UsecodeValue.getZero() :
			new UsecodeValue.IntValue(obj.getShapeReal());
	}
	private final UsecodeValue getItemFrame(UsecodeValue p0) {
		GameObject obj = getItem(p0);
		return obj == null ? UsecodeValue.getZero() :
			// Don't count rotated frames.
			new UsecodeValue.IntValue(obj.getFrameNum()&31);
	}
	// Set frame, but don't change rotated bit.
	private final void setItemFrame(UsecodeValue itemVal, UsecodeValue frameVal) {
		setItemFrame(getItem(itemVal), frameVal.getIntValue(), false, false);
	}
	public final static void setItemFrame
		(
		GameObject item,
		int frame,
		boolean check_empty,		// If 1, don't set empty frame.
		boolean set_rotated			// Set 'rotated' bit to one in 'frame'.
		) {
		if (item == null)
			return;
							// Added 9/16/2001:
		if (!set_rotated)		// Leave bit alone?
			frame = (item.getFrameNum()&32)|(frame&31);
		if (frame == item.getFrameNum())
			return;			// Already set to that.
		Actor act = item.asActor();
			// Actors have frame replacements for empty frames:
		if (act != null)
			act.changeFrame(frame);
		else {			// Check for empty frame.
			ShapeFiles file = item.getShapeFile();
			ShapeFrame shape = file.getShape(item.getShapeNum(), frame);
			if (shape == null || (check_empty && shape.isEmpty()))
				return;
								// (Don't mess up rotated frames.)
			if ((frame&0xf) < item.getNumFrames())
				item.changeFrame(frame);
		}
		gwin.setPainted();		// Make sure paint gets done.
	}
	
	private final UsecodeValue getItemQuality(UsecodeValue p0) {
		GameObject obj = getItem(p0);
		if (obj == null)
			return UsecodeValue.getZero();
		ShapeInfo info = obj.getInfo();
		return new UsecodeValue.IntValue(info.hasQuality() ? obj.getQuality() : 0);
	}
	private final UsecodeValue setItemQuality(UsecodeValue p0, UsecodeValue p1) {
		// Guessing it's 
		//  set_quality(item, value).
		int qual = p1.getIntValue();
		if (qual == EConst.c_any_qual)		// Leave alone (happens in SI)?
			return UsecodeValue.getOne();
		GameObject obj = getItem(p0);
		if (obj != null) {
			ShapeInfo info = obj.getInfo();
			if (info.hasQuality()) {
				obj.setQuality(qual);
				return UsecodeValue.getOne();
			}
		}
		return UsecodeValue.getZero();
	}
	private final UsecodeValue getItemQuantity(UsecodeValue p0) {
		// Get quantity of an item.
		//   Get_quantity(item, mystery).
		GameObject obj = getItem(p0);
		if (obj != null)
			return new UsecodeValue.IntValue(obj.getQuantity());
		else
			return UsecodeValue.getZero();
	}
	public static UsecodeValue setItemQuantity(UsecodeValue p0, UsecodeValue p1) {
		// Set_quantity (item, newcount).  Rets 1 iff item.has_quantity().
		GameObject obj = getItem(p0);
		int newquant = p1.getIntValue();
		if (obj != null && obj.getInfo().hasQuantity()) {
			UsecodeValue one = UsecodeValue.getOne();
						// If not in world, don't delete!
			if (newquant == 0 && obj.getChunk() == null)
				return one;
			int oldquant = obj.getQuantity();
			int delta = newquant - oldquant;
						// Note:  This can delete the obj.
			obj.modifyQuantity(delta);
			return one;
		} else
			return UsecodeValue.getZero();
	}
	private final UsecodeValue getObjectPosition(UsecodeValue p0) {
		// Takes itemref.  ?Think it rets.
		//  hotspot coords: (x, y, z).
		GameObject obj = getItem(p0);
		Tile c = tempTile;
		if (obj != null)		// (Watch for animated objs' wiggles.)
			obj.getOutermost().getOriginalTileCoord(c);
		else
			c.set(0,0,0);
		UsecodeValue vx = new UsecodeValue.IntValue(c.tx), 
					 vy = new UsecodeValue.IntValue(c.ty), 
					 vz = new UsecodeValue.IntValue(c.tz);
		UsecodeValue arr = new UsecodeValue.ArrayValue(vx, vy, vz);
		return(arr);
	}
	private final UsecodeValue getDistance(UsecodeValue p0, UsecodeValue p1) {
		// Distance from parm[0] -> parm[1].  Guessing how it's computed.
		GameObject obj0 = getItem(p0);
		GameObject obj1 = getItem(p1);
		if (obj0 == null || obj1 == null)
			return UsecodeValue.getZero();
		return new UsecodeValue.IntValue( 
			obj0.getOutermost().distance(obj1.getOutermost()));
	}
	private final Tile getPosition(UsecodeValue itemval) {
		Tile tile = new Tile();
		GameObject obj;		// An object?
		int sz = itemval.getArraySize();
		if ((sz == 1 || sz == 0) && (obj = getItem(itemval)) != null)
				obj.getOutermost().getTile(tile);
		else if (sz == 3)
						// An array of coords.?
			tile.set(itemval.getElem(0).getIntValue(),
					itemval.getElem(1).getIntValue(),
					itemval.getElem(2).getIntValue());
		else if (itemval.getArraySize() == 4)
						// Result of click_on_item() with
						//  array = (null, tx, ty, tz)?
			tile.set(itemval.getElem(1).getIntValue(),
					itemval.getElem(2).getIntValue(),
					itemval.getElem(3).getIntValue());
		else				// Else assume caller_item.
			ucmachine.get_caller_item().getTile(tile);
		return tile;
	}
	private final UsecodeValue findDirection(UsecodeValue from, UsecodeValue to) {
		// Direction from parm[0] -> parm[1].
		// Rets. 0-7.  Is 0 east?
		int angle;			// Gets angle 0-7 (north - northwest)
		Tile t1 = getPosition(from);
		Tile t2 = getPosition(to);
						// Treat as cartesian coords.
		angle = EUtil.getDirection(t1.ty - t2.ty, t2.tx - t1.tx);
		return new UsecodeValue.IntValue(angle);
	}

	private final UsecodeValue getNpcObject(UsecodeValue p0) {
		// Takes -npc.  Returns object, or array of objects.
		if (p0.isArray()) {		// Do it for each element of array.
			int sz = p0.getArraySize();
			Vector<UsecodeValue> arr = new Vector<UsecodeValue>();
			arr.setSize(sz);
			for (int i = 0; i < sz; i++) {
				UsecodeValue elem = new UsecodeValue.ObjectValue(
													getItem(p0.getElem(i)));
				arr.setElementAt(elem, i);
			}
			return new UsecodeValue.ArrayValue(arr);
		}
		GameObject obj = getItem(p0);
		return new UsecodeValue.ObjectValue(obj);
	}
	private final void addToParty(UsecodeValue p0) {
		// NPC joins party.
		Actor npc = getItem(p0).asActor();
		if (partyman.addToParty(npc))
			return;		// Can't add.
		/* +++++++++++++
		npc.setScheduleType(Schedule.follow_avatar);
		npc.setAlignment(Actor.friendly);
		*/
	}
	private final void removeFromParty(UsecodeValue p0) {
		Actor npc = getItem(p0).asActor();
		if (partyman.removeFromParty(npc))
			npc.setAlignment(Actor.neutral);
	}
	private UsecodeValue getNpcProp(UsecodeValue p0, UsecodeValue p1) {
		// Get NPC prop (item, prop_id).
		GameObject obj = getItem(p0);
		
		if (obj == null)
			return UsecodeValue.getZero();
		Actor npc = obj.asActor();
		if (npc == null) {
			if (p1.getIntValue() == Actor.health)
				return new UsecodeValue.IntValue(obj.getObjHp());
			else 
				return UsecodeValue.getZero();
		}
		String att = p1.getStringValue();
		if (att != null)
			return new UsecodeValue.IntValue(npc.getAttribute(att));
		else
			return new UsecodeValue.IntValue(npc.getProperty(p1.getIntValue()));
	}
	private UsecodeValue setNpcProp(UsecodeValue p0, UsecodeValue p1,
														UsecodeValue p2) {
		// Set NPC prop (item, prop_id, delta_value).
		GameObject obj = getItem(p0);
		Actor npc = obj != null ? obj.asActor() : null;;
		if (npc != null) {			// NOTE: 3rd parm. is a delta!
			String att = p1.getStringValue();
			if (att != null)
				npc.setAttribute(att, npc.getAttribute(att) +
							p2.getIntValue());
			else {
				int prop = p1.getIntValue();
				int delta = p2.getIntValue();
				if (prop == Actor.exp)
					delta /= 2;	// Verified.
				if (prop != Actor.sex_flag)
					delta += npc.getProperty(prop);	// NOT for gender.
				npc.setProperty(prop, delta);
				}
			return UsecodeValue.getOne();// SI needs return.
		} else if (obj != null) {
				// Verified. Needed by serpent statue at end of SI.
			int prop = p1.getIntValue();
			int delta = p2.getIntValue();
			if (prop == Actor.health) {
				obj.setObjHp(obj.getObjHp() + delta);
				return UsecodeValue.getOne();
			}
		}
		return UsecodeValue.getZero();
	}
	/*
	 *	Return an array containing the party, with the Avatar first.
	 */
	private Vector<UsecodeValue> getParty() {
		int cnt = partyman.getCount();
		Vector<UsecodeValue> arr = new Vector<UsecodeValue>();
		arr.setSize(cnt + 1);
						// Add avatar.
		arr.setElementAt(new UsecodeValue.ObjectValue(gwin.getMainActor()), 0);
		int num_added = 1;
		for (int i = 0; i < cnt; i++) {
			GameObject obj = gwin.getNpc(partyman.getMember(i));
			if (obj == null)
				continue;
			UsecodeValue val = new UsecodeValue.ObjectValue(obj);
			arr.setElementAt(val, num_added++);
			}
		// cout << "Party:  "; arr.print(cout); cout << endl;
		return arr;
		}
	private final UsecodeValue getPartyList() {
		// Return array with party members.
		return new UsecodeValue.ArrayValue(getParty());
	}
	private final GameObject createObject(int shapenum, boolean equip) {
		GameObject obj = null;		// Create to be written to Ireg.
		ShapeInfo info = ShapeID.getInfo(shapenum);
		ucmachine.setModifiedMap();
		/* +++++++++FINISH
						// +++Not sure if 1st test is needed.
		if (info.get_monster_info() || info.isNpc()) {
						// (Wait sched. added for FOV.)
			// don't add equipment (Erethian's transform sequence)
			/* +++++FINISH
			Monster_actor *monster = Monster_actor::create(shapenum,
				Tile_coord(-1, -1, -1), Schedule::wait, 
						(int) Actor::neutral, true, equip);
						// FORCE it to be neutral (dec04,01).
			monster.set_alignment((int) Actor::neutral);
			gwin.add_dirty(monster);
			gwin.add_nearby_npc(monster);
			gwin.show();
			last_created.push_back(monster);
			return monster;
		} else */ {
			/* +++++++++++
			if (info.isBodyShape())
				obj = new Dead_body(shapenum, 0, 0, 0, 0, -1);
			else */ {
				obj = IregGameObject.create(ShapeID.getInfo(shapenum), shapenum, 0);
						// Be liberal about taking stuff.
				obj.setFlag(GameObject.okay_to_take);
			}
		}
		obj.setInvalid();		// Not in world yet.
		obj.setFlag(GameObject.okay_to_take);
		last_created.addLast(obj);
		return obj;
	}
	private final UsecodeValue createNewObject(UsecodeValue p0) {
		// create_new_object(shapenum).   Stores it in 'last_created'.
		int shapenum = p0.getIntValue();
		GameObject obj = createObject(shapenum, false);
		return new UsecodeValue.ObjectValue(obj);
	}
	private final UsecodeValue setLastCreated(UsecodeValue p0) {
		// Take itemref off map and set last_created to it.
		GameObject obj = getItem(p0);
		// Don't do it for same object if already there.
		/*
		for (vector<Game_object*>::const_iterator it = last_created.begin();
					it != last_created.end(); ++it)
			if (*it == obj)
				return UsecodeValue(0);
		*/
		
		ucmachine.setModifiedMap();
		if (obj != null) {
			gwin.addDirty(obj);		// Set to repaint area.
			last_created.add(obj);
			obj.removeThis();	// Remove.
			}
		UsecodeValue u = new UsecodeValue.ObjectValue(obj);
		return(u);
	}
	private final UsecodeValue updateLastCreated(UsecodeValue p0) {
		/*++++TESTING
		System.out.println("Calling getClick");
		Point pt = new Point();
		ExultActivity.getClick(pt);
		System.out.println("Got x,y = " + pt.x + "," + pt.y);
		*/
		// Think it takes array from 0x18,
		//   updates last-created object.
		//   ??guessing??
		ucmachine.setModifiedMap();
		if (last_created.isEmpty()) {
			return UsecodeValue.getNullObj();
		}
		GameObject obj = last_created.removeLast();
		obj.setInvalid();		// It's already been removed.
		UsecodeValue arr = p0;
		int sz = arr.getArraySize();
		if (sz >= 2) {
			//arr is loc (x, y, z, map) if sz == 4,
			//(x, y, z) for sz == 3 and (x, y) for sz == 2
			
			int tx = arr.getElem(0).getIntValue(),
				ty = arr.getElem(1).getIntValue(),
				tz = sz >= 3 ? arr.getElem(2).getIntValue() : 0;
			obj.move(tx, ty, tz, sz < 4 ? -1 :
				  arr.getElem(3).getIntValue());
			if (/* ++++++ FINISH GAME_BG */ true) {
				return UsecodeValue.getOne();
			} else {
				return new UsecodeValue.ObjectValue(obj);
			}
						// Taking a guess here:
		} else if (sz == 1) {
			obj.removeThis();
		}
		return UsecodeValue.getOne();
	}
	private final UsecodeValue getNpcName(UsecodeValue p0) {
		// Get NPC name(s).  Works on arrays, too.
		//static const char *unknown = "??name??";
		Actor npc;
		int cnt = p0.getArraySize();
		if (cnt > 0) {			// Do array.
			Vector<UsecodeValue> arr = new Vector<UsecodeValue>();
			arr.setSize(cnt);
			for (int i = 0; i < cnt; i++) {
				GameObject obj = getItem(p0.getElem(i));
				npc = obj != null ? obj.asActor() : null;
				String nm = npc != null ? npc.getNpcName()
							  : (obj != null ? obj.getName() : "??name??");
				arr.setElementAt(new UsecodeValue.StringValue(nm), i);
			}
			return new UsecodeValue.ArrayValue(arr);
		}
		GameObject obj = getItem(p0);
		String nm;
		if (obj != null) {
			npc = obj.asActor();
			nm = npc != null ? npc.getNpcName() : obj.getName();
		} else
			nm = "??name??";
		return new UsecodeValue.StringValue(nm);
	}
	/*
	 *	Count objects of a given shape in a container, or in the whole party.
	 */

	private final UsecodeValue countObjects(
		UsecodeValue objval,		// The container, or -357 for party.
		UsecodeValue shapeval,	// Object shape to count (c_any_shapenum=any).
		UsecodeValue qualval,		// Quality (c_any_qual=any).
		UsecodeValue frameval		// Frame (c_any_framenum=any).
		) {	
		// How many?
		// ((npc?-357==party, -356=avatar), 
		//   item, quality, frame (c_any_framenum = any)).
		// Quality/frame -359 means any.
		long oval = objval.getIntValue();
		int shapenum = shapeval.getIntValue();
		int qualnum = qualval.getIntValue();
		int framenum = frameval.getIntValue();
		if (oval != -357)
			{
			GameObject obj = getItem(objval);
			return (obj == null ? UsecodeValue.getZero() :
				new UsecodeValue.IntValue(
						obj.countObjects(shapenum, qualnum, framenum)));
		}
		int total = 0;
						// Look through whole party.
		int cnt = partyman.getCount();
		for (int i = 0; i < cnt; i++) {
			GameObject obj = gwin.getNpc(partyman.getMember(i));
			if (obj != null)
				total += obj.countObjects(shapenum, qualnum, framenum);
		}
		return new UsecodeValue.IntValue(total);
	}
	final private UsecodeValue findObject(UsecodeValue p0, UsecodeValue p1, 
										  UsecodeValue p2, UsecodeValue p3) {
		// Find_object(container(-357=party) OR loc, shapenum, qual?? (-359=any), 
		//						frame??(-359=any)).
		int shnum = p1.getIntValue(),
			qual  = p2.getIntValue(),
			frnum = p3.getIntValue();
		if (p0.getArraySize() == 3) {			// Location (x, y).
			Vector<GameObject> vec = new Vector<GameObject>();
			Tile t = new Tile(p0.getElem(0).getIntValue(),
							  p0.getElem(1).getIntValue(),
							  p0.getElem(2).getIntValue());
			gmap.findNearby(vec, t, shnum, 1, 0, qual, frnum);
		if (vec.isEmpty())
			return UsecodeValue.getNullObj();
		else
			return new UsecodeValue.ObjectValue(vec.firstElement());
	}
	int oval  = p0.getIntValue();
	if (oval == -359) {		// Find on map (?)
		Vector<GameObject> vec = new Vector<GameObject>();
		Rectangle scr = new Rectangle();
		gwin.getWinTileRect(scr);
		Tile t = new Tile(scr.x + scr.w/2, scr.y + scr.h/2, 0);
		gmap.findNearby(vec, t, shnum, scr.h/2, 0, qual, frnum);
		return vec.isEmpty() ? UsecodeValue.getNullObj()
				   : new UsecodeValue.ObjectValue(vec.firstElement());
	}
	/* +++++++++FINISH
	if (oval != -357) {		// Not the whole party? Find inside owner.
		GameObject obj = getItem(p0);
		if (obj == null)
			return UsecodeValue.getNullObj();
		GameObject f = obj.findItem(shnum, qual, frnum);
		return new UsecodeValue.ObjectValue(f);
	}
	*/
					// Look through whole party.
	int cnt = partyman.getCount();
	for (int i = 0; i < cnt; i++) {
		GameObject obj = gwin.getNpc(partyman.getMember(i));
		if (obj != null) {
			/* ++++++++FINISH
			GameObject f = obj.findItem(shnum, qual, frnum);
			if (f != null)
				return new UsecodeValue.ObjectValue(f);
			*/
		}
	}
	return UsecodeValue.getNullObj();
	}
	private final UsecodeValue npcNearby(UsecodeValue p0) {
		// NPC nearby? (item).
		GameObject obj = getItem(p0);
		if (obj == null)
			return UsecodeValue.getZero();;
		int tx = obj.getTileX(), ty = obj.getTileY();
		Actor npc;
		gwin.getWinTileRect(tempRect);
		boolean is_near = tempRect.hasPoint(tx, ty) &&
			// Guessing: true if non-NPC, false if NPC is dead, asleep or paralyzed.
			((npc = obj.asActor()) == null || npc.canAct());
		return is_near ? UsecodeValue.getOne() : UsecodeValue.getZero();
	}
	private final UsecodeValue findNearbyAvatar(UsecodeValue p0) {
		UsecodeValue av = new UsecodeValue.ObjectValue(gwin.getMainActor());
							// Try bigger # for Test of Love tree
		UsecodeValue dist = new UsecodeValue.IntValue(192);
		return findNearby(av, p0, dist, UsecodeValue.getZero());
	}
	public final UsecodeValue isNpc(UsecodeValue p0) {
		GameObject obj = getItem(p0);
		if (obj != null && obj.asActor() != null)
			return UsecodeValue.getOne();
		else
			return UsecodeValue.getZero();
	}
	private final UsecodeValue findNearby(UsecodeValue objVal, UsecodeValue shapeVal,
						UsecodeValue distVal, UsecodeValue maskVal) {
		int mval = maskVal.getIntValue();// Some kind of mask?  Guessing:
										//   4 == party members only.
										//   8 == non-party NPC's only.
										//  16 == something with eggs???
										//  32 == monsters? invisible?
		int shapenum;
		
		if (shapeVal.isArray()) {
			// fixes 'lightning whip sacrifice' in Silver Seed
			shapenum = shapeVal.getElem(0).getIntValue();
			if (shapeVal.getArraySize() > 1)
				System.out.println("Calling find_nearby with an array > 1 !!!!");
		} else
			shapenum = shapeVal.getIntValue();

			
						// It might be (tx, ty, tz).
		int arraysize = objVal.getArraySize();
		foundVec.clear();
		if (arraysize == 4) {		// Passed result of click_on_item.
			tempTile.set(objVal.getElem(1).getIntValue(),
						 objVal.getElem(2).getIntValue(),
						 objVal.getElem(3).getIntValue());
			
			gmap.findNearby(foundVec, tempTile, shapenum,
				distVal.getIntValue(), mval);
		} else if (arraysize == 3 || arraysize == 5) {
			// Coords(x,y,z) [qual, frame]. Qual is 4th if there.
			int qual = arraysize == 5 ? objVal.getElem(3).getIntValue()
								: EConst.c_any_qual;
						// Frame is 5th if there.
			int frnum = arraysize == 5 ? objVal.getElem(4).getIntValue()
								: EConst.c_any_framenum;
			tempTile.set(objVal.getElem(0).getIntValue(),
						 objVal.getElem(1).getIntValue(),
						 objVal.getElem(2).getIntValue());
			gmap.findNearby(foundVec, tempTile, shapenum,
				distVal.getIntValue(), mval, qual, frnum);
		} else {
			GameObject obj = getItem(objVal);
			if (obj == null)
				return UsecodeValue.getZero();	// +++Exult rets UsecodeValue(0,0).
			obj = obj.getOutermost();	// Might be inside something.
			obj.getTile(tempTile);
			gmap.findNearby(foundVec, tempTile, shapenum, distVal.getIntValue(), mval);
		}
		/* +++++++FINISH 
		if (foundVec.size() > 1)		// Sort right-left, near-far to fix
						//   SI/SS cask bug.
			std::sort(vec.begin(), vec.end(), Object_reverse_sorter());
		*/
		UsecodeValue nearby = UsecodeValue.ArrayValue.createObjectsList(foundVec);
		return (nearby);
	}
	private final UsecodeValue giveLastCreated(UsecodeValue p0) {
		// Think it's give_last_created(container).
		GameObject cont = getItem(p0);
		boolean ret = false;
		if (cont != null && !last_created.isEmpty()) {
						// Get object, but don't pop yet.
			GameObject obj = last_created.getLast();
			// Might not have been removed from world yet.
			if (obj.getOwner() == null && obj.getChunk() == null)
						// Don't check vol.  Causes failures.
				ret = cont.add(obj, true);
			if (ret)		// Pop only if added.  Fixes chest/
						//   tooth bug in SI.
				last_created.removeLast();
			}
		return ret ? UsecodeValue.getOne() : UsecodeValue.getZero();
	}
	private final UsecodeValue isDead(UsecodeValue p0) {
		Actor npc = getItem(p0).asActor();
		return (npc != null && npc.isDead()) ? UsecodeValue.getOne()
				: UsecodeValue.getZero();
	}
	private final UsecodeValue getNpcNumber(UsecodeValue p0) {
		// Returns NPC# of item. (-356 = avatar).
		Actor npc = getItem(p0).asActor();
		if (npc == gwin.getMainActor())
			return new UsecodeValue.IntValue (-356);
		int num = npc != null ? npc.getNpcNum() : 0;
		return new UsecodeValue.IntValue(-num);
	}
	private final UsecodeValue getAlignment(UsecodeValue p0) {
		// Get npc's alignment.
		Actor npc = getItem(p0).asActor();
		return (npc == null) ? UsecodeValue.getZero() :
						new UsecodeValue.IntValue(npc.getAlignment());
	}

	private static void setAlignment(UsecodeValue p0, UsecodeValue p1) {
		// Set npc's alignment.
		// 2,3==bad towards Ava. 0==good.
		Actor npc = getItem(p0).asActor();
		int val = p1.getIntValue();
		if (npc != null) {
			int oldalign = npc.getAlignment();
			npc.setAlignment(val);
			/* ++++++++++++FINISH
			if (oldalign != val)	// Changed?  Force search for new opp.
				npc.setTarget(0);
						// For fixing List Field fleeing:
			if (npc.get_attack_mode() == Actor::flee)
				npc.set_attack_mode(Actor::nearest);
			*/
		}
	}
	private final void moveObject(UsecodeValue p0, UsecodeValue p1,
				UsecodeValue p2) {
		// move_object(obj(-357=party), (tx, ty, tz)).
		Tile tile = new Tile(p1.getElem(0).getIntValue(),
				p1.getElem(1).getIntValue(),
				p1.getArraySize() > 2 ? p1.getElem(2).getIntValue() : 0);
		int map = p1.getArraySize() < 4 ? -1 :
				p1.getElem(3).getIntValue();
		Actor ava = gwin.getMainActor();
		ucmachine.setModifiedMap();
		if (p0.getIntValue() == -357) {		// Move whole party.
			gwin.teleportParty(tile, false, map);
			return;
		}
		GameObject obj = getItem(p0);
		if (obj == null)
			return;
		int oldX = obj.getTileX(), oldY = obj.getTileY();
		obj.move(tile.tx, tile.ty, tile.tz, map);
		Actor act = obj.asActor();
		if (act != null) {
			act.setAction(null);
			if (act == ava) {		// Teleported Avatar?
						// Make new loc. visible, test eggs.
				if (map != -1)
					gwin.setMap(map);
				gwin.centerView(tile.tx, tile.ty);
				/* +++++++++++
				Map_chunk::try_all_eggs(ava, tile.tx, 
					tile.ty, tile.tz, oldX, oldY);
				*/
			// Close?  Add to 'nearby' list.
			} else if (ava.distance(act) < 
								gwin.getWidth()/EConst.c_tilesize) {
				/* +++++++++++++
				NpcActor npc = act.asNpc();
				if (npc != null) gwin.add_nearby_npc(npc);
				*/
			}
		}
	}
	private final void removeNpc(UsecodeValue p0) {
		// Remove_npc(npc) - Remove npc from world.
		Actor npc = getItem(p0).asActor();
		if (npc != null) {
			ucmachine.setModifiedMap();
						// Don't want him/her coming back!
			//+++++++ npc.set_schedule_type(Schedule.wait);
			gwin.addDirty(npc);
			npc.removeThis();	// Remove, but don't delete.
		}
	}

	private final void itemSay(UsecodeValue p0, UsecodeValue p1)  {
		// Show str. near item (item, str).
		GameObject obj = getItem(p0);
		String str = p1.getStringValue();
		if (obj != null && str != null && str.length() > 0) {
						// Added Nov01,01 to fix 'locate':
			eman.removeTextEffect(obj);
			eman.addText(str, obj);
		}
	}
	private final UsecodeValue setToAttack(UsecodeValue p0, UsecodeValue p1,
			UsecodeValue p2) {
		// set_to_attack(fromnpc, to, weaponshape).
		// fromnpc attacks the target 'to' with weapon weaponshape.
		// 'to' can be a game object or the return of a click_on_item
		// call (including the possibility of being a tile target).
		Actor from = getItem(p0).asActor();
		if (from == null)
			return UsecodeValue.getZero();
		int shnum = p2.getIntValue();
		if (shnum < 0)
			return UsecodeValue.getZero();
		/* ++++++FINISH
		Weapon_info *winf = ShapeID::get_info(shnum).get_weapon_info();
		if (!winf)
			return Usecode_value(0);

		Usecode_value& tval = parms[1];
		Game_object *to = get_item(tval.getElem0());
		int nelems;
		if (to)
			{
			// It is an object.
			from.set_attack_target(to, shnum);
			return Usecode_value(1);
			}
		else if (tval.is_array() && (nelems = tval.getArraySize()) >= 3)
			{
			// Tile return of click_on_item. Allowing size to be < 4 for safety.
			Tile_coord trg = Tile_coord(
					tval.getElem(1).getIntValue(),
					tval.getElem(2).getIntValue(),
					nelems >= 4 ? tval.getElem(3).getIntValue() : 0);
			from.set_attack_target(trg, shnum);
			return Usecode_value(1);
			}
		*/
		return UsecodeValue.getZero();	// Failure.
	}

	
	
	private final UsecodeValue getLift(UsecodeValue p0) {
		GameObject obj = getItem(p0);
		return obj == null ? UsecodeValue.getZero() :
			new UsecodeValue.IntValue(obj.getLift());
	}
	private final void setLift(UsecodeValue p0, UsecodeValue p1) {
		GameObject obj = getItem(p0);
		if (obj != null) {
			int lift = p1.getIntValue();
			if (lift >= 0 && lift < 20) {
				obj.move(obj.getTileX(), obj.getTileY(), lift);
				ucmachine.setModifiedMap();
				// ++++USED TO REPAINT WINDOW.  Still needed?
			}
		}
	}
	private final UsecodeValue getArraySize(UsecodeValue p0) {
		int cnt;
		if (p0.isArray())	// An array?  We might return 0.
			cnt = p0.getArraySize();
		else				// Not an array?  Usecode wants a 1.
			cnt = 1;
		return new UsecodeValue.IntValue(cnt);
	}
	private final UsecodeValue getItemFrameRot(UsecodeValue p0) {
		// Same as get_item_frame, but (guessing!) include rotated bit.
		GameObject obj = getItem(p0);
		return obj == null ? UsecodeValue.getZero() :
			new UsecodeValue.IntValue(obj.getFrameNum());
	}

	private final void setItemFrameRot(UsecodeValue p0, UsecodeValue p1) {
		// Set entire frame, including rotated bit.
		setItemFrame(getItem(p0), p1.getIntValue(), false, true);
	}

	private final UsecodeValue onBarge() {
		// Only used once for BG, in usecode for magic-carpet.
		// For SI, used for turtle.
		// on_barge()
		/* +++++++FINISH
		Barge_object barge = Get_barge(gwin->get_main_actor());
		if (barge)
			{			// See if party is on barge.
			Rectangle foot = barge->get_tile_footprint();
			Actor *party[9];
			int cnt = gwin->get_party(party, 1);
			for (int i = 0; i < cnt; i++)
				{
				Actor *act = party[i];
				Tile_coord t = act->get_tile();
				if (!foot.has_point(t.tx, t.ty))
					return Usecode_value(0);
				}
						// Force 'gather()' for turtle.
			if (Game::get_game_type() == SERPENT_ISLE)
				barge->done();
			return Usecode_value(1);
			} 
		*/
		return UsecodeValue.getZero();
	}

	private final UsecodeValue getContainer(UsecodeValue p0) {
		// Takes itemref, returns container.
		GameObject obj = getItem(p0);
		if (obj != null) {
			obj = obj.getOwner();
			return obj == null ? UsecodeValue.getNullObj()
					: new UsecodeValue.ObjectValue(obj);
		} else
				return UsecodeValue.getNullObj();
	}
	public final static void removeItem(GameObject obj) {
		if (obj != null) {
			if (!last_created.isEmpty() && obj == last_created.getLast())
				last_created.removeLast();
			gwin.addDirty(obj);
			obj.removeThis();
		}
	}
	private final void removeItem(UsecodeValue p0) {
		removeItem(getItem(p0));
		ucmachine.setModifiedMap();
	}
	
	
	//	For BlackGate.
	public UsecodeValue execute(int id, int event, int num_parms, UsecodeValue parms[]) {
		switch (id) {
		case 0x00:
			return getRandom(parms[0]);
		case 0x01:
			return executeUsecodeArray(parms[0], parms[1]);
		case 0x02:
			return delayedExecuteUsecodeArray(parms[0], parms[1], parms[2]);
		case 0x03:
			showNpcFace(parms[0], parms[1], -1); break;
		case 0x04:
			removeNpcFace(parms[0]); break;
		case 0x05:
			addAnswer(parms[0]); break;
		case 0x06:
			removeAnswer(parms[0]); break;
		case 0x07:
			pushAnswers(); break;
		case 0x08:
			popAnswers(); break;
		case 0x09:
			clearAnswers(); break;
		case 0x0a:
			return selectFromMenu();
		case 0x0b:
			return selectFromMenu2();
		//++++++++++
		case 0x0d:
			setItemShape(parms[0], parms[1]); break;
		case 0x0e:
			return findNearest(parms[0], parms[1], parms[2]);
		//+++++++++++
		case 0x10:
			return dieRoll(parms[0], parms[1]);
		case 0x11:
			return getItemShape(parms[0]);
		case 0x12:
			return getItemFrame(parms[0]);
		case 0x13:
			setItemFrame(parms[0], parms[1]); break;
		case 0x14:
			return getItemQuality(parms[0]);
		case 0x15:
			return setItemQuality(parms[0], parms[1]);
		case 0x16:
			return getItemQuantity(parms[0]);
		case 0x17:
			return setItemQuantity(parms[0], parms[1]);
		case 0x18:
			return getObjectPosition(parms[0]);
		case 0x19:
			return getDistance(parms[0], parms[1]);
		case 0x1a:
			return findDirection(parms[0], parms[1]);
		case 0x1b:
			return getNpcObject(parms[0]);
		//+++++++++
		case 0x1e:
			addToParty(parms[0]); break;
		case 0x1f:
			removeFromParty(parms[0]); break;
		case 0x20:
			return getNpcProp(parms[0], parms[1]);
		case 0x21:
			return setNpcProp(parms[0], parms[1], parms[2]);
		case 0x22:
			return new UsecodeValue.ObjectValue(gwin.getMainActor());
		case 0x23:
			return getPartyList();
		case 0x24:
			return createNewObject(parms[0]);
		case 0x25:
			return setLastCreated(parms[0]);
		case 0x26:
			return updateLastCreated(parms[0]);
		case 0x27:
			return getNpcName(parms[0]);
		case 0x28:
			return countObjects(parms[0], parms[1], parms[2], parms[3]);
		case 0x29:
			return findObject(parms[0], parms[1], parms[2], parms[3]);
		//++++++++
		case 0x2f:
			return npcNearby(parms[0]);
		case 0x30:
			return findNearbyAvatar(parms[0]);
		case 0x31:
			return isNpc(parms[0]);
		//++++++++++++
		case 0x35:
			return findNearby(parms[0], parms[1], parms[2], parms[3]);
		case 0x36:
			return giveLastCreated(parms[0]);
		case 0x37:
			return isDead(parms[0]);
		//++++++++++++++
		case 0x3a:
			return getNpcNumber(parms[0]);
		//+++++++++
		case 0x3c:
			return getAlignment(parms[0]);
		case 0x3d:
			setAlignment(parms[0], parms[1]); break;
		case 0x3e:
			moveObject(parms[0], parms[1], parms[2]); break;
		case 0x3f:
			removeNpc(parms[0]); break;
		case 0x40:
			if (!conv.isNpcTextPending())
				itemSay(parms[0], parms[1]);
			break;
		case 0x41:
			return setToAttack(parms[0], parms[1], parms[2]);
		case 0x42:
			return getLift(parms[0]);
		case 0x43:
			setLift(parms[0], parms[1]); break;
		//++++++++++++
		case 0x5e:
			return getArraySize(parms[0]);
		//+++++++++++++++
		case 0x6b:
			return getItemFrameRot(parms[0]);
		case 0x6c:
			setItemFrameRot(parms[0], parms[1]); break;
		case 0x6d:
			return onBarge();
		case 0x6e:
			return getContainer(parms[0]);
		case 0x6f:
			removeItem(parms[0]); break;
		//++++++++++++++
		default:
			System.out.println("*** UNHANDLED intrinsic # " + id);
			break;
		}
		return UsecodeValue.getZero();
	}
}