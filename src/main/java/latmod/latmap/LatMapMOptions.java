package latmod.latmap;

import latmod.ftbu.mod.client.minimap.*;
import latmod.ftbu.util.client.ClientConfig;

public class LatMapMOptions extends MOptions
{
	public static final LatMapMOptions instance = new LatMapMOptions();
	
	public static final ClientConfig clientConfig = new ClientConfig("minimap");
	public static final ClientConfig.Property renderIngame = new ClientConfig.Property("render_ingame", 0, "disabled", "right", "left");
	public static final ClientConfig.Property players = new ClientConfig.Property("render_players", false);
	public static final ClientConfig.Property waypoints = new ClientConfig.Property("render_waypoints", false);
	public static final ClientConfig.Property calcHeight = new ClientConfig.Property("calc_height", true);
	public static final ClientConfig.Property blur = new ClientConfig.Property("blur", false)
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
	public static final ClientConfig.Property zoom = new ClientConfig.Property("zoom", 2, getZoomAValues()).setRawValues();
	private static final String[] getZoomAValues()
	{
		String[] s = new String[zoomA.length];
		for(int i = 0; i < zoomA.length; i++)
			s[i] = zoomA[i] + "x";
		return s;
	}
	
	public static final int[] sizeA = { 64, 96, 128, 160 };
	public static final ClientConfig.Property size = new ClientConfig.Property("size_ingame", 2, getSizeAValues()).setRawValues();
	private static final String[] getSizeAValues()
	{
		String[] s = new String[sizeA.length];
		for(int i = 0; i < sizeA.length; i++)
			s[i] = sizeA[i] + "px";
		return s;
	}
	
	public static final ClientConfig.Property grid = new ClientConfig.Property("render_grid", true);
	public static final ClientConfig.Property claimedChunks = new ClientConfig.Property("render_claimed_chunks", true);
	public static final ClientConfig.Property customColors = new ClientConfig.Property("custom_map_colors", true);
	
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
		ClientConfig.Registry.add(clientConfig);
		
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