package latmod.latmap;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ftb.lib.client.*;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.util.client.*;
import latmod.ftbu.util.client.model.CubeRenderer;
import latmod.latmap.wp.*;
import latmod.lib.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class LatMapRenderHandler
{
	public static final LatMapRenderHandler instance = new LatMapRenderHandler();
	public static final ResourceLocation texMarker = FTBU.mod.getLocation("textures/map/marker.png");
	private static final FastList<RenderableWaypoint> visibleBeacons = new FastList<RenderableWaypoint>();
	private static final FastList<RenderableWaypoint> visibleMarkers = new FastList<RenderableWaypoint>();
	private static final FastList<String> stringList = new FastList<String>();
	private static double far = 4D;
	private static int beaconListID = -1;
	private static long posHash = -1L;
	private static int listSize = -1;
	
	@SubscribeEvent
	public void renderWorld(RenderWorldLastEvent e)
	{
		if(!LatCoreMCClient.isPlaying() || !Waypoints.enabled.get() || Waypoints.waypoints.isEmpty()) return;
		
		if(posHash != LMFrustrumUtils.playerPosHash || listSize != Waypoints.waypoints.size())
		{
			posHash = LMFrustrumUtils.playerPosHash;
			listSize = Waypoints.waypoints.size();
			
			visibleBeacons.clear();
			visibleMarkers.clear();
			
			double renderDistSq = Waypoints.renderDistance.get() * Waypoints.renderDistance.get();
			
			for(int i = 0; i < listSize; i++)
			{
				Waypoint w = Waypoints.waypoints.get(i);
				if(w.enabled && w.dim == LMFrustrumUtils.currentDim)
				{
					double x = w.posX + 0.5D;
					double y = w.posY + 0.5D;
					double z = w.posZ + 0.5D;
					double distSq = MathHelperLM.distSq(x, y, z, LMFrustrumUtils.playerX, LMFrustrumUtils.playerY, LMFrustrumUtils.playerZ);
					
					if(distSq <= renderDistSq)
					{
						if(w.type.isMarker()) visibleMarkers.add(new RenderableWaypoint(w, x, y, z, distSq, far));
						else visibleBeacons.add(new RenderableWaypoint(w, x, y, z, distSq, far));
					}
				}
			}
		}
		
		boolean hasMarkers = !visibleMarkers.isEmpty();
		boolean hasBeacons = !visibleBeacons.isEmpty();
		
		if(!hasMarkers && !hasBeacons) return;
		
		GlStateManager.pushAttrib();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.enableTexture();
		
		visibleMarkers.sort(RenderableWaypoint.comparator);
		visibleBeacons.sort(RenderableWaypoint.comparator);
		
		Tessellator t = Tessellator.instance;
		
		if(hasMarkers && LMFrustrumUtils.isFirstPerson)
		{
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			FTBLibClient.setTexture(texMarker);
			GlStateManager.enableTexture();
			
			for(int i = 0; i < visibleMarkers.size(); i++)
			{
				RenderableWaypoint w = visibleMarkers.get(i);
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(w.closeRenderX, w.closeRenderY, w.closeRenderZ);
				GlStateManager.rotate(-RenderManager.instance.playerViewY, 0F, 1F, 0F);
				GlStateManager.rotate(RenderManager.instance.playerViewX, 1F, 0F, 0F);
				GlStateManager.scale(w.scale, -w.scale, -w.scale);
				
				t.startDrawingQuads();
				t.setColorRGBA(LMColorUtils.getRed(w.waypoint.color), LMColorUtils.getGreen(w.waypoint.color), LMColorUtils.getBlue(w.waypoint.color), 255);
				t.addVertexWithUV(-0.5D, -0.5D, 0D, 0D, 0D);
				t.addVertexWithUV(0.5D, -0.5D, 0D, 1D, 0D);
				t.addVertexWithUV(0.5D, 0.5D, 0D, 1D, 1D);
				t.addVertexWithUV(-0.5D, 0.5D, 0D, 0D, 1D);
				t.draw();
				
				GlStateManager.popMatrix();
			}
			
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
		}
		
		if(hasBeacons)
		{
			GlStateManager.disableCull();
			GlStateManager.depthMask(false);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			//OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);
			GlStateManager.disableTexture();
			
			if(beaconListID == -1)
			{
				GlStateManager.color(1F, 1F, 1F, 1F);
				beaconListID = GL11.glGenLists(1);
				GL11.glNewList(beaconListID, GL11.GL_COMPILE);
				CubeRenderer cr = new CubeRenderer();
				cr.setSize(-0.4F, 0D, -0.4F, 0.4F, 1024D, 0.4F);
				for(int k = 2; k < 6; k++) cr.renderSide(k);
				cr.setSize(-0.3F, 0D, -0.3F, 0.3F, 1024D, 0.3F);
				for(int k = 2; k < 6; k++) cr.renderSide(k);
				GL11.glEndList();
			}
			
			//float deathPointTick = (float)(System.currentTimeMillis() * 0.0001D);
			
			for(int i = 0; i < visibleBeacons.size(); i++)
			{
				RenderableWaypoint w = visibleBeacons.get(i);
				
				if(LMFrustrumUtils.frustrum.isBoxInFrustum(w.posX, 0D, w.posZ, w.posX + 1D, 256D, w.posZ + 1D))
				{
					GlStateManager.pushMatrix();
					GlStateManager.translate(w.posX - LMFrustrumUtils.renderX, -LMFrustrumUtils.playerY, w.posZ - LMFrustrumUtils.renderZ);
					/*if(w.waypoint.deathpoint)
					{
						float h = (float)( (MathHelperLM.sin(deathPointTick + w.hashCode()) + 1D) / 2D * 0.1D);
						int col = 0xFFFFFF;
						GL11.glColor4f(LMColorUtils.getRedF(col), LMColorUtils.getGreenF(col), LMColorUtils.getBlueF(col), 0.15F);
					}
					else*/
					GlStateManager.color(LMColorUtils.getRedF(w.waypoint.color), LMColorUtils.getGreenF(w.waypoint.color), LMColorUtils.getBlueF(w.waypoint.color), 0.15F);
					GL11.glCallList(beaconListID);
					GlStateManager.popMatrix();
				}
			}
			
			GlStateManager.depthMask(true);
		}
		
		boolean displayTitle = Waypoints.displayTitle.get();
		boolean displayDist = Waypoints.displayDist.get();
		
		if((displayTitle || displayDist) && LMFrustrumUtils.isFirstPerson)
		{
			GlStateManager.color(1F, 1F, 1F, 1F);
			GlStateManager.disableCull();
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			for(int i = 0; i < visibleBeacons.size() + visibleMarkers.size(); i++)
			{
				RenderableWaypoint w = (i >= visibleBeacons.size()) ? visibleMarkers.get(i - visibleBeacons.size()) : visibleBeacons.get(i);
				
				if(displayDist || !w.waypoint.name.isEmpty())
				{
					stringList.clear();
					if(displayTitle && !w.waypoint.name.isEmpty()) stringList.add(w.waypoint.name);
					if(displayDist) stringList.add((int)(w.distance + 0.5D) + "m");
					
					if(stringList.isEmpty()) continue;
					
					GlStateManager.pushMatrix();
					GlStateManager.translate(w.closeRenderX, w.closeRenderY + 0.5D, w.closeRenderZ);
					GlStateManager.rotate(-RenderManager.instance.playerViewY, 0F, 1F, 0F);
					GlStateManager.rotate(RenderManager.instance.playerViewX, 1F, 0F, 0F);
					GL11.glNormal3f(0F, 1F, 0F);
					float f1 = 0.0125F;
					GlStateManager.scale(-f1, -f1, f1);
					
					for(int j = 0; j < stringList.size(); j++)
					{
						double y = -2.5D + 11 * j;
						String s = stringList.get(j);
						int l = FTBLibClient.mc.fontRenderer.getStringWidth(s) / 2;
						GlStateManager.disableTexture();
						t.startDrawingQuads();
						t.setColorRGBA_F(0F, 0F, 0F, 0.4F);
						t.addVertex(-l -1, y, 0D);
						t.addVertex(l + 1, y, 0D);
						t.addVertex(l + 1, y + 10, 0D);
						t.addVertex(-l -1, y + 10, 0D);
						t.draw();
						GlStateManager.enableTexture();
						FTBLibClient.mc.fontRenderer.drawString(s, -l, (int)(y + 1D), w.waypoint.deathpoint  ? 0xFFFF1111 : 0xFFFFFFFF);
					}
					
					GlStateManager.popMatrix();
				}
			}
			
			GlStateManager.depthMask(true);
		}
		
		GlStateManager.popAttrib();
	}
}
