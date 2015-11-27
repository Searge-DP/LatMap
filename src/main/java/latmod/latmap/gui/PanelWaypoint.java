package latmod.latmap.gui;

import ftb.lib.api.gui.*;
import ftb.lib.api.gui.callback.ClientTickCallback;
import ftb.lib.client.*;
import ftb.lib.gui.GuiLM;
import ftb.lib.gui.widgets.*;
import latmod.latmap.wp.*;
import latmod.lib.LMColorUtils;
import net.minecraft.client.gui.*;

public class PanelWaypoint extends PanelLM
{
	public final Waypoint waypoint;
	public final ButtonLM edit, teleport, color, type, delete;
	
	public PanelWaypoint(PanelWaypointList p, Waypoint w)
	{
		super(p.gui, 0, p.height, p.width, 18);
		waypoint = w;
		
		edit = new ButtonLM(p.gui, 1, 1, p.gui.getFontRenderer().getStringWidth(waypoint.name) + 6, 16)
		{
			public void onButtonPressed(int b)
			{
				gui.playClickSound();
				
				if(b == 0)
					gui.mc.displayGuiScreen(new GuiEditWaypoint((GuiWaypoints)gui, waypoint));
				else
				{
					waypoint.enabled = !waypoint.enabled;
					gui.refreshWidgets();
				}
			}
		};
		
		edit.title = FTBLibLang.button_settings();
		
		teleport = new ButtonLM(p.gui, width - 72, 1, 16, 16)
		{
			public void onButtonPressed(int b)
			{
				gui.playClickSound();
				
				FTBLibClient.addClientTickCallback(new ClientTickCallback()
				{
					public void onCallback()
					{ FTBLibClient.execClientCommand("/tpl " + waypoint.posX + " " + waypoint.posY + " " + waypoint.posZ); }
				});
				
				gui.container.player.closeScreen();
			}
		};
		
		teleport.title = "/tpl " + waypoint.posX + ".5 " + waypoint.posY + ".5 " + waypoint.posZ + ".5";
		
		color = new ButtonLM(p.gui, width - 54, 1, 16, 16)
		{
			public void onButtonPressed(int b)
			{
				gui.playClickSound();
				LMGuis.displayColorSelector((GuiWaypoints)gui, waypoint.color, waypoint.listID, true);
			}
		};
		
		color.title = LMColorUtils.getHex(waypoint.color);
		
		type = new ButtonLM(p.gui, width - 36, 1, 16, 16)
		{
			public void onButtonPressed(int b)
			{
				gui.playClickSound();
				waypoint.type = waypoint.type.next();
				gui.refreshWidgets();
			}
		};
		
		type.title = waypoint.type.getIDS();
		
		delete = new ButtonLM(p.gui, width - 18, 1, 16, 16)
		{
			public void onButtonPressed(int b)
			{
				gui.playClickSound();
				
				if(GuiScreen.isShiftKeyDown())
				{
					Waypoints.remove(waypoint.listID);
					gui.refreshWidgets();
				}
				else gui.mc.displayGuiScreen(new GuiYesNo((GuiWaypoints)gui, FTBLibLang.delete_item(waypoint.name), null, waypoint.listID));
			}
		};
		
		delete.title = FTBLibLang.button_remove();
	}
	
	public void addWidgets()
	{
		add(edit);
		add(teleport);
		add(color);
		add(type);
		add(delete);
	}
	
	public void renderWidget()
	{
		int ay = getAY();
		//if(ay >= gui.height || ay < -height) return;
		boolean mouseOver = mouseOver();
		GuiLM.drawBlankRect(0, ay, 0F, width, height, mouseOver ? 0x33FFFFFF : 0x33333333);
		gui.drawString(gui.getFontRenderer(), waypoint.name, 4, ay + 5, waypoint.enabled ? 0xFFFFFFFF : 0xFF777777);
		FTBLibClient.setGLColor(waypoint.color, 255);
		color.render(GuiIcons.color_blank);
		GlStateManager.color(1F, 1F, 1F, 1F);
		type.render(waypoint.type.icon);
		if(mouseOver) teleport.render(GuiIcons.compass);
		if(delete.mouseOver()) delete.render(GuiIcons.remove);
		else
		{
			GlStateManager.color(1F, 1F, 1F, 0.2F);
			delete.render(GuiIcons.remove_gray);
			GlStateManager.color(1F, 1F, 1F, 1F);
		}
		
		if(edit.mouseOver())
			GuiLM.drawBlankRect(edit.posX, ay + 1, 0F, edit.width, edit.height, 0x33FFFFFF);
	}
}