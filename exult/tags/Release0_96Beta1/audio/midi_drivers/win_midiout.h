//-*-Mode: C++;-*-
/*
Copyright (C) 2000  Ryan Nunn

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

#ifndef _MIDI_driver_win_midiout_h_
#define _MIDI_driver_win_midiout_h_

#if (__GNUG__ >= 2) && (!defined WIN32)
#  pragma interface
#endif

#ifdef WIN32

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#include <windows.h>
#include <mmsystem.h>

#include "Midi.h"
#include "xmidi.h"
#include "exceptions.h"

class	Windows_MidiOut : virtual public MidiAbstract
{
public:
	// Do we accept events, YES!
	virtual bool accepts_events(void) { return true; }

	virtual void start_track(midi_event *evntlist, int ppqn, bool repeat);
	virtual void start_sfx(midi_event *evntlist, int ppqn);
	virtual void stop_track(void);
	virtual void stop_sfx(void);
	virtual bool is_playing(void);
	virtual const char *copyright(void);

	Windows_MidiOut();
	virtual ~Windows_MidiOut();

private:
	UNREPLICATABLE_CLASS(Windows_MidiOut);

	struct mid_data {
		midi_event	*list;
		int 		ppqn;
		bool		repeat;
	};

	static const unsigned short	centre_value;
	static const unsigned char	fine_value;
	static const unsigned char	coarse_value;
	static const unsigned short	combined_value;

	sint32		dev_num;
	HMIDIOUT	midi_port;
	
	HANDLE	 	*thread_handle;
	DWORD		thread_id;

	// Thread communicatoins
	LONG		is_available;
	LONG		playing;
	LONG		s_playing;
	LONG		thread_com;
	LONG		sfx_com;

	mid_data *thread_data;
	mid_data *sfx_data;

	mid_data data;
	mid_data sdata;

	// Methods
	static DWORD __stdcall thread_start(void *data);
	void init_device();
	DWORD thread_main();
	void thread_play ();
	void reset_channel (int i);

	// Microsecond Clock
	Uint32 start;
	Uint32 sfx_start;

	inline void wmoInitClock ()
	{ start = GetTickCount(); }

	inline double wmoGetTime ()
	{ return (GetTickCount() - start) * 1000.0; }

	inline void wmoInitSFXClock ()
	{ sfx_start = GetTickCount(); }

	inline double wmoGetSFXTime ()
	{ return (GetTickCount() - sfx_start) * 1000.0; }

	inline void wmoDelay (const double mcs_delay)
	{ if (mcs_delay >= 0) Sleep ((int) (mcs_delay / 1000.0)); }

};

#endif

#endif
