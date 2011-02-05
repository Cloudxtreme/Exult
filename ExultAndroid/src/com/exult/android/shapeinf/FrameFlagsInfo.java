package com.exult.android.shapeinf;
import java.io.PushbackInputStream;
import java.io.InputStream;
import com.exult.android.ShapeInfo;
import com.exult.android.EUtil;
import com.exult.android.DataUtils;
/*
 *	Information about frame names.
 *	This is meant to be stored in a totally ordered vector.
 */
public class FrameFlagsInfo extends BaseInfo.FrameInfo {
	private int	m_flags;	// Bit field with the relevant flags.
	
	public boolean get_flag(int tf)
		{ return (m_flags & (1 << tf)) != 0; }
	public int get_flags()
		{ return m_flags; }
	@Override
	public boolean read(InputStream in, int version, boolean patch, int game,
			ShapeInfo info) {
		PushbackInputStream txtin = (PushbackInputStream)in;
		frame = EUtil.ReadInt(txtin);
		if (frame < 0)
			frame = -1;
		else
			frame &= 0xff;

		if (version >= 6)
			quality = EUtil.ReadInt(txtin);
		else
			quality = -1;
		if (quality < 0)
			quality = -1;
		else
			quality &= 0xff;
		final int size = 32;	// Bit count for m_flags.
		m_flags = DataUtils.readBitFlags(txtin, size); 
		//System.out.println("frameFlagsInfo for frame " + frame + ", quality = " + quality);
		info.setFrameFlagsInfo(addVectorInfo(this, info.getFrameFlagsInfo()));
		return true;
	}
}
