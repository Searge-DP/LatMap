package latmod.latmap;

import java.util.Calendar;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ftb.lib.api.*;
import ftb.lib.api.gui.GuiIcons;
import ftb.lib.client.FTBLibClient;
import latmod.ftbu.api.*;
import latmod.ftbu.util.client.LatCoreMCClient;
import latmod.ftbu.world.LMWorldClient;
import latmod.latmap.gui.GuiWaypoints;
import latmod.latmap.wp.*;
import latmod.lib.LMStringUtils;
import latmod.lib.config.ConfigEntryBool;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

public class LatMapEventHandler
{
	public static final LatMapEventHandler instance = new LatMapEventHandler();
	public static final ConfigEntryBool button_waypoints = new ConfigEntryBool("waypoints", false);
	
	public static final PlayerAction waypoints = new PlayerAction(GuiIcons.compass)
	{
		public void onClicked(int playerID)
		{ FTBLibClient.mc.displayGuiScreen(new GuiWaypoints(FTBLibClient.mc.currentScreen)); }
		
		public String getTitle()
		{ return I18n.format("client_config.sidebar_buttons.waypoint"); }
	};
	
	@SubscribeEvent
	public void onEvent(EventPlayerActionButtons e)
	{
		if(e.self && LatCoreMCClient.isPlaying())
		{
			if(e.addAll || button_waypoints.get()) e.actions.add(waypoints);
		}
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