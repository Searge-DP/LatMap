package latmod.latmap;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ftb.lib.client.FTBLibClient;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.util.client.*;
import latmod.ftbu.util.client.model.CubeRenderer;
import latmod.latmap.wp.*;
import latmod.lib.*;
import net.minecraft.client.renderer.*;
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
		if(!LatCoreMCClient.isPlaying() || !Waypoints.enabled.getB() || Waypoints.waypoints.isEmpty()) return;
		
		if(posHash != LMFrustrumUtils.playerPosHash || listSize != Waypoints.waypoints.size())
		{
			posHash = LMFrustrumUtils.playerPosHash;
			listSize = Waypoints.waypoints.size();
			
			visibleBeacons.clear();
			visibleMarkers.clear();
			
			double renderDistSq = Waypoints.renderDistanceSq[Waypoints.renderDistance.getI()];
			
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
		
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		visibleMarkers.sort(RenderableWaypoint.comparator);
		visibleBeacons.sort(RenderableWaypoint.comparator);
		
		Tessellator t = Tessellator.instance;
		
		if(hasMarkers && LMFrustrumUtils.isFirstPerson)
		{
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			FTBLibClient.setTexture(texMarker);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			for(int i = 0; i < visibleMarkers.size(); i++)
			{
				RenderableWaypoint w = visibleMarkers.get(i);
				
				GL11.glPushMatrix();
				GL11.glTranslated(w.closeRenderX, w.closeRenderY, w.closeRenderZ);
				GL11.glRotatef(-RenderManager.instance.playerViewY, 0F, 1F, 0F);
				GL11.glRotatef(RenderManager.instance.playerViewX, 1F, 0F, 0F);
				GL11.glScaled(w.scale, -w.scale, -w.scale);
				
				t.startDrawingQuads();
				t.setColorRGBA(LMColorUtils.getRed(w.waypoint.color), LMColorUtils.getGreen(w.waypoint.color), LMColorUtils.getBlue(w.waypoint.color), 255);
				t.addVertexWithUV(-0.5D, -0.5D, 0D, 0D, 0D);
				t.addVertexWithUV(0.5D, -0.5D, 0D, 1D, 0D);
				t.addVertexWithUV(0.5D, 0.5D, 0D, 1D, 1D);
				t.addVertexWithUV(-0.5D, 0.5D, 0D, 0D, 1D);
				t.draw();
				
				GL11.glPopMatrix();
			}
			
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		
		if(hasBeacons)
		{
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			//OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			
			if(beaconListID == -1)
			{
				GL11.glColor4f(1F, 1F, 1F, 1F);
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
					GL11.glPushMatrix();
					GL11.glTranslated(w.posX - LMFrustrumUtils.renderX, -LMFrustrumUtils.playerY, w.posZ - LMFrustrumUtils.renderZ);
					/*if(w.waypoint.deathpoint)
					{
						float h = (float)( (MathHelperLM.sin(deathPointTick + w.hashCode()) + 1D) / 2D * 0.1D);
						int col = 0xFFFFFF;
						GL11.glColor4f(LMColorUtils.getRedF(col), LMColorUtils.getGreenF(col), LMColorUtils.getBlueF(col), 0.15F);
					}
					else*/
					GL11.glColor4f(LMColorUtils.getRedF(w.waypoint.color), LMColorUtils.getGreenF(w.waypoint.color), LMColorUtils.getBlueF(w.waypoint.color), 0.15F);
					GL11.glCallList(beaconListID);
					GL11.glPopMatrix();
				}
			}
			
			GL11.glDepthMask(true);
		}
		
		boolean displayTitle = Waypoints.displayTitle.getB();
		boolean displayDist = Waypoints.displayDist.getB();
		
		if((displayTitle || displayDist) && LMFrustrumUtils.isFirstPerson)
		{
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			
			for(int i = 0; i < visibleBeacons.size() + visibleMarkers.size(); i++)
			{
				RenderableWaypoint w = (i >= visibleBeacons.size()) ? visibleMarkers.get(i - visibleBeacons.size()) : visibleBeacons.get(i);
				
				if(displayDist || !w.waypoint.name.isEmpty())
				{
					stringList.clear();
					if(displayTitle && !w.waypoint.name.isEmpty()) stringList.add(w.waypoint.name);
					if(displayDist) stringList.add((int)(w.distance + 0.5D) + "m");
					
					if(stringList.isEmpty()) continue;
					
					GL11.glPushMatrix();
					GL11.glTranslated(w.closeRenderX, w.closeRenderY + 0.5D, w.closeRenderZ);
					GL11.glRotatef(-RenderManager.instance.playerViewY, 0F, 1F, 0F);
					GL11.glRotatef(RenderManager.instance.playerViewX, 1F, 0F, 0F);
					GL11.glNormal3f(0F, 1F, 0F);
					float f1 = 0.0125F;
					GL11.glScalef(-f1, -f1, f1);
					
					for(int j = 0; j < stringList.size(); j++)
					{
						double y = -2.5D + 11 * j;
						String s = stringList.get(j);
						int l = FTBLibClient.mc.fontRenderer.getStringWidth(s) / 2;
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						t.startDrawingQuads();
						t.setColorRGBA_F(0F, 0F, 0F, 0.4F);
						t.addVertex(-l -1, y, 0D);
						t.addVertex(l + 1, y, 0D);
						t.addVertex(l + 1, y + 10, 0D);
						t.addVertex(-l -1, y + 10, 0D);
						t.draw();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
						FTBLibClient.mc.fontRenderer.drawString(s, -l, (int)(y + 1D), w.waypoint.deathpoint  ? 0xFFFF1111 : 0xFFFFFFFF);
					}
					
					GL11.glPopMatrix();
				}
			}
			
			GL11.glDepthMask(true);
		}
		
		GL11.glPopAttrib();
	}
}
