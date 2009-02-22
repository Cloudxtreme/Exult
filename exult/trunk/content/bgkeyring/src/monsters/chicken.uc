/*
 *
 *  Copyright (C) 2006  Alun Bestor/The Exult Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 *	Author: Marzo Junior (reorganizing/updating code by Alun Bestor)
 *	Last Modified: 2006-03-19
 */

/* Some highly sophisticated behaviour for chickens */
void Chicken shape#(0x1F2) ()
{
	var rand = UI_get_random(20);
	var bark;

	//Chickens will cluck whenever the party is near
	if (event == PROXIMITY)
	{
		//Chickens will crow for 30 minutes after sunrise
		if (UI_game_hour() == 6 && UI_game_minute() <= 30 && rand < 15)
			bark = "@Cockadoodledoo!@";
		else if	(rand == 1)
			bark = "@Cluck, I say!@";
		else if	(rand < 15)
			bark = "@Cluck@";
		else
			bark = "@Peck@";
		item_say(bark);
	}

	//Double-clicking a chicken will spook it and cause it to run away
	else if (event == DOUBLECLICK)
	{
		item_say("@Squawk!@");
		set_schedule_type(SHY);
		if (rand < 5) randomPartyBark("@Leave the poor thing alone!@");
	}
}
