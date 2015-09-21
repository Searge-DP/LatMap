package latmod.latmap.gui;

import latmod.ftbu.core.gui.PanelLM;
import latmod.latmap.wp.*;

public class PanelWaypoints extends PanelLM
{
	public PanelWaypoints(GuiWaypoints g)
	{
		super(g, 0, 20, 0, 0);
	}
	
	public void addWidgets()
	{
		height = 0;
		for(Waypoint w : Waypoints.waypoints)
		{
			if(w.dim == gui.mc.thePlayer.dimension)
			{
				PanelWaypoint pw = new PanelWaypoint(this, w);
				add(pw);
				height += pw.height;
			}
		}
	}
	
	public void renderWidget()
	{
		for(int i = 0; i < widgets.size(); i++)
			widgets.get(i).renderWidget();
	}
}