package latmod.latmap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import latmod.ftbu.api.*;
import latmod.ftbu.api.client.*;
import latmod.ftbu.mod.FTBUFinals;
import latmod.ftbu.mod.client.gui.friends.PlayerAction;
import latmod.ftbu.mod.client.minimap.*;
import latmod.ftbu.notification.*;
import latmod.ftbu.util.client.*;
import latmod.ftbu.util.gui.GuiIcons;
import latmod.ftbu.world.*;
import latmod.latmap.gui.GuiWaypoints;
import latmod.latmap.wp.*;
import latmod.lib.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.event.entity.EntityEvent;

public class LatMapEventHandler
{
	public static final LatMapEventHandler instance = new LatMapEventHandler();
	
	public static final PlayerAction waypoints = new PlayerAction(GuiIcons.compass)
	{
		public void onClicked(LMPlayerClient p)
		{ LatCoreMCClient.mc.displayGuiScreen(new GuiWaypoints(LatCoreMCClient.mc.currentScreen)); }
		
		public String getTitle()
		{ return Waypoints.clientConfig.getIDS(); }
	};
	
	@SubscribeEvent
	public void onPlayerActionEvent(EventPlayerAction e)
	{
		if(e.isSelf) e.actions.add(waypoints);
	}
	
	@SubscribeEvent
	public void chunkChanged(EntityEvent.EnteringChunk e)
	{
		if(e.entity.worldObj.isRemote && LatCoreMCClient.isPlaying() && LatMapMOptions.renderIngame.getB() && e.entity.getUniqueID().equals(LMWorldClient.inst.clientPlayer.getUUID()))
		{
			int rd = LatMapMOptions.zoomA[LatMapMOptions.zoom.getI()];
			Minimap m = Minimap.get(e.entity.dimension);
			m.requestArea(rd + 2);
		}
	}
	
	@SubscribeEvent
	public void renderChunk(RenderWorldEvent.Pre e)
	{
		if(e.pass == 0 && LatCoreMCClient.isPlaying() && LatMapMOptions.renderIngame.getB())
		{
			int cx = MathHelperLM.chunk(e.renderer.posX);
			int cz = MathHelperLM.chunk(e.renderer.posZ);
			Minimap m = Minimap.get(LatCoreMCClient.getDim());
			MChunk c = m.loadChunk(cx, cz);
			c.reload(LatCoreMCClient.mc.theWorld);
			//m.requestArea(1);
		}
	}
	
	@SubscribeEvent
	public void worldJoined(EventLMWorldClient.Joined e)
	{
		Minimap.load();
		Waypoints.load();
		
		int rd = LatMapMOptions.zoomA[LatMapMOptions.zoom.getI()];
		Minimap m = Minimap.get(LatCoreMCClient.getDim());
		m.requestArea(rd + 2);
	}
	
	@SubscribeEvent
	public void worldClosed(EventLMWorldClient.Closed e)
	{
		Minimap.save();
		Waypoints.save();
	}
	
	@SubscribeEvent
	public void playerDied(EventLMPlayerClient.PlayerDied e)
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
			w.type = WaypointType.BEACON;
			w.color = 0xFFFF3311;
			w.deathpoint = true;
			Waypoints.add(w);
		}
	}
	
	@SubscribeEvent
	public void onKeyPressed(EventFTBUKey e)
	{
		if(FTBUFinals.DEV && e.pressed)
		{
			//LatCoreMC.printChat(null, Keyboard.getKeyName(e.key));
			
			if(e.key == Keyboard.KEY_GRAVE)
			{
			}
			else if(e.key == Keyboard.KEY_MINUS)
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
			else if(e.key == Keyboard.KEY_EQUALS)
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
			else if(e.key == Keyboard.KEY_M)
			{
				LatMapMOptions.renderIngame.onClicked();
			}
			else if(e.key == Keyboard.KEY_N)
			{
				File f = exportImage();
				if(f != null)
				{
					Notification n = new Notification(null, new ChatComponentText("Minimap exported!"), 2000);
					n.setDesc(new ChatComponentText(f.getName()));
					n.setItem(new ItemStack(Items.map));
					n.setClickEvent(new ClickAction(ClickAction.FILE, f.getAbsolutePath()));
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