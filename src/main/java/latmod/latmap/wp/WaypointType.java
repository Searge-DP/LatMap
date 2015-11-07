package latmod.latmap.wp;

import ftb.lib.api.gui.GuiIcons;
import ftb.lib.client.TextureCoords;
import latmod.latmap.LatMap;

public enum WaypointType
{
	BEACON("beacon", GuiIcons.beacon),
	MARKER("marker", GuiIcons.marker);
	
	public final String ID;
	public final TextureCoords icon;
	
	WaypointType(String s, TextureCoords t)
	{
		ID = s;
		icon = t;
	}

	public boolean isMarker()
	{ return this == MARKER; }
	
	public boolean isBeacon()
	{ return this == BEACON; }
	
	public WaypointType next()
	{ return values()[(ordinal() + 1) % values().length]; }

	public String getIDS()
	{ return LatMap.mod.translateClient("waypoint.type." + ID); }

	public static WaypointType get(String s)
	{
		for(WaypointType t : values())
			if(t.ID.equals(s)) return t;
		return BEACON;
	}
}