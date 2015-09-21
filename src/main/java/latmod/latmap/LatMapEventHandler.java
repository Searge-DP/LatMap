package latmod.latmap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import latmod.ftbu.core.*;
import latmod.ftbu.core.api.*;
import latmod.ftbu.core.client.*;
import latmod.ftbu.core.gui.GuiIcons;
import latmod.ftbu.core.net.*;
import latmod.ftbu.core.util.*;
import latmod.ftbu.core.world.LMWorldClient;
import latmod.ftbu.mod.FTBUFinals;
import latmod.ftbu.mod.client.gui.friends.*;
import latmod.ftbu.mod.client.minimap.*;
import latmod.latmap.gui.GuiWaypoints;
import latmod.latmap.wp.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class LatMapEventHandler
{
	public static final LatMapEventHandler instance = new LatMapEventHandler();
	
	public static final PlayerAction waypoints = new PlayerAction(GuiIcons.compass)
	{
		public void onClicked(GuiFriends g)
		{ g.mc.displayGuiScreen(new GuiWaypoints(g)); }
		
		public String getTitle()
		{ return Waypoints.clientConfig.getIDS(); }
	};
	
	@SubscribeEvent
	public void onPlayerActionEvent(EventPlayerAction e)
	{
		if(e.isSelf) e.actions.add(waypoints);
	}
	
	@SubscribeEvent
	public void onGsonEvent(EventFTBUGson e)
	{
		e.add(Waypoint.class, new Waypoint.Serializer());
	}
	
	@SubscribeEvent
	public void chunkChanged(EntityEvent.EnteringChunk e)
	{
		if(e.entity.worldObj.isRemote && LatMapMOptions.renderIngame.getB() && e.entity.getUniqueID().equals(LMWorldClient.inst.clientPlayer.getUUID()))
		{
			int rd = LatMapMOptions.zoomA[LatMapMOptions.zoom.getI()];
			Minimap m = Minimap.get(e.entity.dimension);
			m.reloadArea(e.entity.worldObj, e.newChunkX - MathHelperLM.floor(rd / 2D), e.newChunkZ - MathHelperLM.floor(rd / 2D), rd, rd);
		}
	}
	
	@SubscribeEvent
	public void renderChunk(RenderWorldEvent.Pre e)
	{
		if(e.pass == 0 && LatMapMOptions.renderIngame.getB())
		{
			int cx = MathHelperLM.chunk(e.renderer.posX);
			int cz = MathHelperLM.chunk(e.renderer.posZ);
			int dim = LatCoreMCClient.getDim();
			MChunk c = Minimap.get(dim).loadChunk(cx, cz);
			c.reload(LatCoreMCClient.mc.theWorld);
			LMNetHelper.sendToServer(new MessageAreaRequest(cx, cz, dim, 1));
		}
	}
	
	@SubscribeEvent
	public void worldJoined(LMClientWorldJoinedEvent e)
	{
		Minimap.load();
		Waypoints.load();
	}
	
	@SubscribeEvent
	public void worldClosed(LMClientWorldClosedEvent e)
	{
		Minimap.save();
		Waypoints.save();
	}
	
	@SubscribeEvent
	public void playerDied(LivingDeathEvent e)
	{
		LatCoreMC.printChat(null, "Remote: " + e.entity.worldObj.isRemote + ", Entity: " + e.entity);
	}
	
	@SubscribeEvent
	public void playerDied(LMPlayerClientEvent.PlayerDied e)
	{
		if(Waypoints.enabled.getB() && Waypoints.deathPoint.getB() && e.player.equalsPlayer(LMWorldClient.inst.clientPlayer))
		{
			EntityPlayer ep = LatCoreMCClient.mc.thePlayer;
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
			w.type = Waypoint.Type.BEACON;
			w.color = LMColorUtils.getRGBA(LatCoreMC.rand.nextInt(100) + 155, LatCoreMC.rand.nextInt(100), 0, 255);
			Waypoints.add(w);
		}
	}
	
	@SubscribeEvent
	public void keyEvent(InputEvent.KeyInputEvent e)
	{
		if(FTBUFinals.DEV && Keyboard.getEventKeyState())
		{
			//LatCoreMC.printChat(null, Keyboard.getKeyName(Keyboard.getEventKey()));
			
			int key = Keyboard.getEventKey();
			if(key == Keyboard.KEY_GRAVE)
			{
				Minimap.save();
			}
			else if(key == Keyboard.KEY_MINUS)
			{
				if(GuiScreen.isShiftKeyDown())
				{
					int i = LatMapMOptions.size.getI() - 1;
					if(i >= 0 && i < LatMapMOptions.size.values.length)
						LatMapMOptions.size.setValue(i);
				}
				else
				{
					int i = LatMapMOptions.zoom.getI() + 1;
					if(i >= 0 && i < LatMapMOptions.zoom.values.length)
						LatMapMOptions.zoom.setValue(i);
				}
				
				LatCoreMCClient.playClickSound();
			}
			else if(key == Keyboard.KEY_EQUALS)
			{
				if(GuiScreen.isShiftKeyDown())
				{
					int i = LatMapMOptions.size.getI() + 1;
					if(i >= 0 && i < LatMapMOptions.size.values.length)
						LatMapMOptions.size.setValue(i);
				}
				else
				{
					int i = LatMapMOptions.zoom.getI() - 1;
					if(i >= 0 && i < LatMapMOptions.zoom.values.length)
						LatMapMOptions.zoom.setValue(i);
				}
				
				LatCoreMCClient.playClickSound();
			}
			else if(key == Keyboard.KEY_M)
			{
				LatMapMOptions.renderIngame.onClicked();
			}
			else if(key == Keyboard.KEY_N)
			{
				File f = exportImage();
				if(f != null)
				{
					Notification n = new Notification(null, new ChatComponentText("Minimap exported!"), 2000);
					n.setDesc(new ChatComponentText(f.getName()));
					n.setItem(new ItemStack(Items.map));
					n.setClickEvent(new NotificationClick(NotificationClick.FILE, f.getAbsolutePath()));
					ClientNotifications.add(n);
				}
				else
				{
					Notification n = new Notification(null, new ChatComponentText("Minimap failed to export!"), 2000);
					n.setItem(new ItemStack(Items.map));
					ClientNotifications.add(n);
				}
			}
		}
	}
	
	public File exportImage()
	{
		Minimap m = Minimap.get(LatCoreMCClient.getDim());
		if(m.areas.isEmpty()) return null;
		
		try
		{
			int ms = Integer.MAX_VALUE;
			int minX = ms;
			int minY = ms;
			int maxX = -ms;
			int maxY = -ms;
			
			for(MArea a : m.areas) for(MChunk c : a.chunks)
			{
				if(c.posX < minX) minX = c.posX;
				if(c.posY < minY) minY = c.posY;
				if(c.posX > maxX) maxX = c.posX;
				if(c.posY > maxY) maxY = c.posY;
			}
			
			long w = (maxX - minX + 1L) * 16L;
			long h = (maxY - minY + 1L) * 16L;
			
			if(w <= 0L || h <= 0L || w * h > Integer.MAX_VALUE) return null;
			
			PixelBuffer image = new PixelBuffer((int)w, (int)h);
			
			for(MArea a : m.areas) for(MChunk c : a.chunks)
			{
				int x = (c.posX - minX) * 16;
				int y = (c.posY - minY) * 16;
				if(x > 0 && y > 0) image.setRGB(x, y, 16, 16, c.pixels);
			}
			
			File file = new File(LMWorldClient.inst.clientDataFolder, "minimap_" + m.dim + ".png");
			ImageIO.write(image.toImage(BufferedImage.TYPE_INT_RGB), "PNG", file);
			return file;
		}
		catch(Exception e)
		{ e.printStackTrace(); }
		
		return null;
	}
}