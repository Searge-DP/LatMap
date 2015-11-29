package latmod.latmap;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ftb.lib.EventBusHelper;
import latmod.ftbu.util.LMMod;
import latmod.latmap.wp.*;
import latmod.lib.LMJsonUtils;
import net.minecraft.client.Minecraft;

@Mod(modid = LatMap.MOD_ID, name = "LatMap", version = "@VERSION@", dependencies = "required-after:FTBU") 
public class LatMap
{
	protected static final String MOD_ID = "LatMap";
	
	@Mod.Instance(LatMap.MOD_ID)
	public static LatMap inst;
	
	@LMMod.Instance(MOD_ID)
	public static LMMod mod;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		LMMod.init(this);
		Minecraft.getMinecraft();
		
		LMJsonUtils.register(Waypoint.class, new Waypoint.Serializer());
		EventBusHelper.register(LatMapEventHandler.instance);
		EventBusHelper.register(LatMapRenderHandler.instance);
		Waypoints.init();
	}
}