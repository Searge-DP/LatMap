package latmod.latmap;

import latmod.ftbu.api.client.*;
import latmod.ftbu.mod.client.minimap.*;

public class LatMapMOptions extends MOptions
{
	public static final LatMapMOptions instance = new LatMapMOptions();
	
	public static final ClientConfig clientConfig = new ClientConfig("minimap");
	public static final ClientConfigProperty renderIngame = new ClientConfigProperty("render_ingame", 0, "disabled", "right", "left");
	public static final ClientConfigProperty players = new ClientConfigProperty("render_players", false);
	public static final ClientConfigProperty waypoints = new ClientConfigProperty("render_waypoints", false);
	public static final ClientConfigProperty calcHeight = new ClientConfigProperty("calc_height", true);
	public static final ClientConfigProperty blur = new ClientConfigProperty("blur", false)
	{
		public void onClicked()
		{
			super.onClicked();
			
			for(Minimap m : Minimap.minimaps.values)
				for(MArea a : m.areas.values)
					a.isDirty = true;
		}
	};
	
	public static final int[] zoomA = { 3, 5, 7, 9, 11, 13 };
	public static final ClientConfigProperty zoom = new ClientConfigProperty("zoom", 2, getZoomAValues()).setRawValues();
	private static final String[] getZoomAValues()
	{
		String[] s = new String[zoomA.length];
		for(int i = 0; i < zoomA.length; i++)
			s[i] = zoomA[i] + "x";
		return s;
	}
	
	public static final int[] sizeA = { 64, 96, 128, 160 };
	public static final ClientConfigProperty size = new ClientConfigProperty("size_ingame", 2, getSizeAValues()).setRawValues();
	private static final String[] getSizeAValues()
	{
		String[] s = new String[sizeA.length];
		for(int i = 0; i < sizeA.length; i++)
			s[i] = sizeA[i] + "px";
		return s;
	}
	
	public static final ClientConfigProperty grid = new ClientConfigProperty("render_grid", true);
	public static final ClientConfigProperty claimedChunks = new ClientConfigProperty("render_claimed_chunks", true);
	public static final ClientConfigProperty customColors = new ClientConfigProperty("custom_map_colors", true);
	
	public void init()
	{
		clientConfig.add(renderIngame);
		clientConfig.add(players);
		clientConfig.add(waypoints);
		clientConfig.add(calcHeight);
		clientConfig.add(blur);
		clientConfig.add(zoom);
		clientConfig.add(size);
		clientConfig.add(grid);
		clientConfig.add(claimedChunks);
		clientConfig.add(customColors);
		ClientConfigRegistry.add(clientConfig);
		
		Minimap.mapOptions = this;
	}
	
	// End of static //
	
	public boolean hasBlur()
	{ return blur.getB(); }
	
	public boolean hasGrid()
	{ return grid.getB(); }
	
	public boolean calcHeight()
	{ return calcHeight.getB(); }
	
	public boolean customColors()
	{ return customColors.getB(); }
}