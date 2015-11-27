package latmod.latmap;

import java.util.Calendar;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ftb.lib.api.gui.GuiIcons;
import ftb.lib.client.FTBLibClient;
import latmod.ftbu.api.*;
import latmod.ftbu.api.client.EventPlayerAction;
import latmod.ftbu.mod.client.gui.friends.PlayerSelfAction;
import latmod.ftbu.world.*;
import latmod.latmap.gui.GuiWaypoints;
import latmod.latmap.wp.*;
import latmod.lib.LMStringUtils;
import net.minecraft.entity.player.EntityPlayer;

public class LatMapEventHandler
{
	public static final LatMapEventHandler instance = new LatMapEventHandler();
	
	public static final PlayerSelfAction waypoints = new PlayerSelfAction(GuiIcons.compass)
	{
		public void onClicked(LMPlayerClient p)
		{ FTBLibClient.mc.displayGuiScreen(new GuiWaypoints(FTBLibClient.mc.currentScreen)); }
		
		public String getTitle()
		{ return "Waypoints"; } //LANG
	};
	
	@SubscribeEvent
	public void onPlayerActionEvent(EventPlayerAction e)
	{
		if(e.isSelf) e.actions.add(waypoints);
	}
	
	@SubscribeEvent
	public void worldJoined(EventLMWorldClient.Joined e)
	{
		Waypoints.load();
	}
	
	@SubscribeEvent
	public void worldClosed(EventLMWorldClient.Closed e)
	{
		Waypoints.save();
	}
	
	@SubscribeEvent
	public void playerDied(EventLMPlayerClient.PlayerDied e)
	{
		if(Waypoints.enabled.get() && Waypoints.deathPoint.get() && e.player.playerID == LMWorldClient.inst.clientPlayerID)
		{
			EntityPlayer ep = FTBLibClient.mc.thePlayer;
			Calendar c = Calendar.getInstance();
			
			StringBuilder sb = new StringBuilder();
			sb.append(LMStringUtils.formatInt(c.get(Calendar.MONTH) + 1));
			sb.append('_');
			sb.append(LMStringUtils.formatInt(c.get(Calendar.DAY_OF_MONTH)));
			sb.append('_');
			sb.append(LMStringUtils.formatInt(c.get(Calendar.HOUR_OF_DAY)));
			sb.append('_');
			sb.append(LMStringUtils.formatInt(c.get(Calendar.MINUTE)));
			
			Waypoint w = new Waypoint();
			w.name = sb.toString();
			w.dim = ep.dimension;
			w.setPos(ep.posX, ep.posY, ep.posZ);
			w.type = WaypointType.BEACON;
			w.color = 0xFFFF3311;
			w.deathpoint = true;
			Waypoints.add(w);
		}
	}
}