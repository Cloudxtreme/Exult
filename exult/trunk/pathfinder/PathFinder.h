#ifndef	__PathFinder_h_
#define	__PathFinder_h_

#include "../tiles.h"

/*
 *	This class provides A* cost methods.
 */
class Pathfinder_client
	{
public:
					// Figure cost for a single step.
	virtual int get_step_cost(Tile_coord from, Tile_coord& to) = 0;
					// Estimate cost between two points.
	virtual int estimate_cost(Tile_coord& from, Tile_coord& to) = 0;
					// Is tile at the goal?
	virtual int at_goal(Tile_coord& tile, Tile_coord& goal) = 0;
	};

/*
 *	Base class for all PathFinders.
 */
class	PathFinder
	{
protected:
	Tile_coord src;			// Source tile.
	Tile_coord dest;		// Destination.
public:
	// Find a path from sx,sy,sz to dx,dy,dz
	// Return 0 if no path can be traced.
	// Return !0 if path found
	virtual	int NewPath(Tile_coord s, Tile_coord d, 
					Pathfinder_client *client)=0;
	// Retrieve starting point (set by subclasses).
	Tile_coord get_src()
		{ return src; }
	// Retrieve current destination (set by subclasses).
	Tile_coord get_dest()
		{ return dest; }
	// Retrieve the coordinates of the next step on the path
	virtual	int	GetNextStep(Tile_coord& n)=0;

	virtual ~PathFinder();
	};

#endif
