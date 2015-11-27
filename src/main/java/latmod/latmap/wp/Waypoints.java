package latmod.latmap.wp;

import java.io.File;
import java.util.ArrayList;

import cpw.mods.fml.relauncher.*;
import ftb.lib.api.config.ClientConfigRegistry;
import latmod.ftbu.world.LMWorldClient;
import latmod.lib.*;
import latmod.lib.config.*;
import latmod.lib.util.FloatBounds;

@SideOnly(Side.CLIENT)
public class Waypoints
{
	public static final ConfigGroup clientConfig = new ConfigGroup("waypoints");
	public static final ConfigEntryBool enabled = new ConfigEntryBool("enabled", true);
	public static final ConfigEntryEnum<WaypointType> waypointType = new ConfigEntryEnum<WaypointType>("waypoint_type", WaypointType.class, WaypointType.BEACON);
	public static final ConfigEntryBool displayTitle = new ConfigEntryBool("display_title", true);
	public static final ConfigEntryBool displayDist = new ConfigEntryBool("display_distance", false);
	public static final ConfigEntryFloat renderDistance = new ConfigEntryFloat("render_distance", new FloatBounds(2500F, 300F, 100000F));
	public static final ConfigEntryBool deathPoint = new ConfigEntryBool("death_point", true);
	public static final FastList<Waypoint> waypoints = new FastList<Waypoint>();
	
	private static File waypointsFile;
	
	public static void init()
	{
		clientConfig.add(enabled);
		clientConfig.add(waypointType);
		clientConfig.add(displayTitle);
		clientConfig.add(displayDist);
		clientConfig.add(renderDistance);
		clientConfig.add(deathPoint);
		ClientConfigRegistry.add(clientConfig);
	}
	
	public static void add(Waypoint w)
	{
		if(w.listID >= 0 && w.listID < waypoints.size())
			waypoints.set(w.listID, w);
		else
		{
			waypoints.add(w);
			for(int i = 0; i < waypoints.size(); i++)
				waypoints.get(i).listID = i;
		}
		save();
	}
	
	public static void remove(int index)
	{
		waypoints.remove(index);
		for(int i = 0; i < waypoints.size(); i++)
			waypoints.get(i).listID = i;
		save();
	}
	
	public static void load()
	{
		try
		{
			waypoints.clear();
			if(LMWorldClient.inst == null) return;
			waypointsFile = LMFileUtils.newFile(new File(LMWorldClient.inst.clientDataFolder, "latmap/waypoints.json"));
			WaypointsFile wf = LMJsonUtils.fromJsonFile(waypointsFile, WaypointsFile.class);
			if(wf != null)
			{
				waypoints.addAll(wf.waypoints);
				for(int i = 0; i < waypoints.size(); i++)
					waypoints.get(i).listID = i;
				save();
			}
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
	
	public static void save()
	{
		try
		{
			if(LMWorldClient.inst == null) return;
			WaypointsFile wf = new WaypointsFile();
			wf.waypoints = new ArrayList<Waypoint>();
			wf.waypoints.addAll(waypoints);
			LMJsonUtils.toJsonFile(waypointsFile, wf);
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}
}