package latmod.latmap;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import latmod.core.util.LMJsonUtils;
import latmod.ftbu.util.*;
import latmod.latmap.wp.*;

@Mod(modid = LatMap.MOD_ID, name = "LatMap", version = "@VERSION@", dependencies = "required-after:FTBU", canBeDeactivated = true) 
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
		LMMod.init(this, null, null);
		if(LatCoreMC.isDedicatedServer())
			throw new RuntimeException("LatMap can only run on client side!");
		else LatCoreMC.logger.info(LatCoreMC.logger.getClass().getName());
		
		LMJsonUtils.register(Waypoint.class, new Waypoint.Serializer());
		EventBusHelper.register(LatMapEventHandler.instance);
		EventBusHelper.register(LatMapRenderHandler.instance);
		LatMapMOptions.instance.init();
		Waypoints.init();
	}
}