package latmod.latmap.wp;

import java.util.Comparator;

import latmod.ftbu.util.client.LMFrustrumUtils;
import latmod.lib.MathHelperLM;

public class RenderableWaypoint
{
	public static final Comparator<RenderableWaypoint> comparator = new Comparator<RenderableWaypoint>()
	{
		public int compare(RenderableWaypoint o1, RenderableWaypoint o2)
		{ return (o1.distance < o2.distance) ? 1 : -1; }
	};
	
	public final Waypoint waypoint;
	public final double posX, posY, posZ;
	public final double closeRenderX, closeRenderY, closeRenderZ;
	public final double distance, scale;
	
	public RenderableWaypoint(Waypoint w, double x, double y, double z, double dsq, double far)
	{
		waypoint = w;
		posX = x;
		posY = y;
		posZ = z;
		
		distance = MathHelperLM.sqrt(dsq);
		
		double crX = posX - LMFrustrumUtils.renderX;
		double crY = posY - LMFrustrumUtils.renderY;
		double crZ = posZ - LMFrustrumUtils.renderZ;
		
		double d1 = MathHelperLM.sqrt3sq(crX, crY, crZ);
		
		if(d1 > far)
		{
			double d = far / d1;
			crX *= d;
			crY *= d;
			crZ *= d;
			d1 = far;
		}
		
		closeRenderX = crX;
		closeRenderY = crY;
		closeRenderZ = crZ;
		
		scale = (d1 * 0.1D + 1D) * 0.4D;
	}
	
	public int hashCode()
	{ return Math.abs(waypoint.hashCode()); }
}