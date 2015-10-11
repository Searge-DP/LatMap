package latmod.latmap;

import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import latmod.ftbu.mod.FTBU;
import latmod.ftbu.mod.client.minimap.MRenderer;
import latmod.ftbu.util.client.*;
import latmod.ftbu.util.client.model.CubeRenderer;
import latmod.ftbu.util.gui.GuiLM;
import latmod.latmap.wp.*;
import latmod.lib.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class LatMapRenderHandler
{
	public static final LatMapRenderHandler instance = new LatMapRenderHandler();
	public static final MRenderer mapRenderer = new MRenderer();
	public static final ResourceLocation texMarker = FTBU.mod.getLocation("textures/map/marker.png");
	private static final FastList<WaypointClient> visibleBeacons = new FastList<WaypointClient>();
	private static final FastList<WaypointClient> visibleMarkers = new FastList<WaypointClient>();
	private static final FastList<String> stringList = new FastList<String>();
	private static double far = 4D;
	private static int beaconListID = -1;
	
	@SubscribeEvent
	public void renderMinimap(TickEvent.RenderTickEvent e)
	{
		if(e.phase == TickEvent.Phase.END && LatCoreMCClient.isPlaying())
		{
			Minecraft mc = LatCoreMCClient.mc;
			
			if(!mc.gameSettings.showDebugInfo && LatMapMOptions.renderIngame.getI() > 0 && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat))
			{
				mapRenderer.size = LatMapMOptions.sizeA[LatMapMOptions.size.getI()];
				mapRenderer.renderX = (LatMapMOptions.renderIngame.getI() == 1) ? LatCoreMCClient.displayW - (mapRenderer.size + 3) : 4;
				mapRenderer.renderY = 4;
				mapRenderer.tiles = LatMapMOptions.zoomA[LatMapMOptions.zoom.getI()];
				mapRenderer.startX = MathHelperLM.chunk(mc.thePlayer.posX) - MathHelperLM.floor(mapRenderer.tiles / 2D);
				mapRenderer.startY = MathHelperLM.chunk(mc.thePlayer.posZ) - MathHelperLM.floor(mapRenderer.tiles / 2D);
				mapRenderer.zLevel = -10F;
				
				mapRenderer.renderClaims = LatMapMOptions.claimedChunks.getB();
				mapRenderer.renderGrid = LatMapMOptions.grid.getB();
				mapRenderer.renderPlayers = LatMapMOptions.players.getB();
				mapRenderer.renderAreaTitle = true;
				mapRenderer.render();
				
				if(!Waypoints.waypoints.isEmpty() && LatMapMOptions.waypoints.getB())
				{
					LatCoreMCClient.setTexture(texMarker);
					
					double tsize = mapRenderer.size / (double)mapRenderer.tiles;
					
					for(int i = 0; i < Waypoints.waypoints.size(); i++)
					{
						Waypoint w = Waypoints.waypoints.get(i);
						if(w.enabled && w.dim == LMFrustrumUtils.currentDim)
						{
							LatCoreMCClient.setColor(w.color, 255);
							
							double x = ((MathHelperLM.chunk(w.posX) - mapRenderer.startX) * 16D + MathHelperLM.wrap(w.posX, 16D)) * tsize / 16D;
							double y = ((MathHelperLM.chunk(w.posZ) - mapRenderer.startY) * 16D + MathHelperLM.wrap(w.posZ, 16D)) * tsize / 16D;
							
							x = MathHelperLM.clamp(x, 0D, mapRenderer.size);
							y = MathHelperLM.clamp(y, 0D, mapRenderer.size);
							
							GuiLM.drawTexturedRectD(mapRenderer.renderX + x - 2D, mapRenderer.renderY + y - 2D, mapRenderer.zLevel, 8, 8, 0D, 0D, 1D, 1D);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void renderWorld(RenderWorldLastEvent e)
	{
		if(!LatCoreMCClient.isPlaying() || !Waypoints.enabled.getB() || Waypoints.waypoints.isEmpty()) return;
		visibleBeacons.clear();
		visibleMarkers.clear();
		
		double renderDistSq = Waypoints.renderDistanceSq[Waypoints.renderDistance.getI()];
		
		for(int i = 0; i < Waypoints.waypoints.size(); i++)
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
					if(w.type.isMarker()) visibleMarkers.add(new WaypointClient(w, x, y, z, distSq));
					else visibleBeacons.add(new WaypointClient(w, x, y, z, distSq));
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
		
		visibleMarkers.sort(WaypointComparator.instance);
		visibleBeacons.sort(WaypointComparator.instance);
		
		Tessellator t = Tessellator.instance;
		
		if(hasMarkers && LMFrustrumUtils.isFirstPerson)
		{
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			LatCoreMCClient.setTexture(texMarker);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			for(int i = 0; i < visibleMarkers.size(); i++)
			{
				WaypointClient w = visibleMarkers.get(i);
				
				GL11.glPushMatrix();
				GL11.glTranslated(w.closeRenderX, w.closeRenderY, w.closeRenderZ);
				GL11.glRotatef(-RenderManager.instance.playerViewY, 0F, 1F, 0F);
				GL11.glRotatef(RenderManager.instance.playerViewX, 1F, 0F, 0F);
				GL11.glScaled(w.scale, -w.scale, -w.scale);
				
				t.startDrawingQuads();
				t.setColorRGBA(w.colR, w.colG, w.colB, 255);
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
			
			for(int i = 0; i < visibleBeacons.size(); i++)
			{
				WaypointClient w = visibleBeacons.get(i);
				
				if(LMFrustrumUtils.frustrum.isBoxInFrustum(w.posX, 0D, w.posZ, w.posX + 1D, 256D, w.posZ + 1D))
				{
					GL11.glPushMatrix();
					GL11.glTranslated(w.posX - LMFrustrumUtils.renderX, -LMFrustrumUtils.playerY, w.posZ - LMFrustrumUtils.renderZ);
					GL11.glColor4f(w.colRF, w.colGF, w.colBF, 0.15F);
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
				WaypointClient w = (i >= visibleBeacons.size()) ? visibleMarkers.get(i - visibleBeacons.size()) : visibleBeacons.get(i);
				
				if(displayDist || !w.name.isEmpty())
				{
					stringList.clear();
					if(displayTitle && !w.name.isEmpty()) stringList.add(w.name);
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
						int l = LatCoreMCClient.mc.fontRenderer.getStringWidth(s) / 2;
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						t.startDrawingQuads();
						t.setColorRGBA_F(0F, 0F, 0F, 0.4F);
						t.addVertex(-l -1, y, 0D);
						t.addVertex(l + 1, y, 0D);
						t.addVertex(l + 1, y + 10, 0D);
						t.addVertex(-l -1, y + 10, 0D);
						t.draw();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
						LatCoreMCClient.mc.fontRenderer.drawString(s, -l, (int)(y + 1D), 0xFFFFFFFF);
					}
					
					GL11.glPopMatrix();
				}
			}
			
			GL11.glDepthMask(true);
		}
		
		GL11.glPopAttrib();
	}
	
	public static class WaypointClient
	{
		public final String name;
		public final double posX, posY, posZ;
		public final double closeRenderX, closeRenderY, closeRenderZ;
		public final int colR, colG, colB;
		public final float colRF, colGF, colBF;
		public final double distance, scale;
		
		public WaypointClient(Waypoint w, double x, double y, double z, double dsq)
		{
			name = w.name;
			colR = LMColorUtils.getRed(w.color);
			colG = LMColorUtils.getGreen(w.color);
			colB = LMColorUtils.getBlue(w.color);
			colRF = colR / 255F;
			colGF = colG / 255F;
			colBF = colB / 255F;
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
	}
	
	private static class WaypointComparator implements Comparator<WaypointClient>
	{
		public static final WaypointComparator instance = new WaypointComparator();
		
		public int compare(WaypointClient o1, WaypointClient o2)
		{ return (o1.distance < o2.distance) ? 1 : -1; }
	}
}
