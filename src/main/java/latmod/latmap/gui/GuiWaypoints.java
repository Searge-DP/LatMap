package latmod.latmap.gui;

import ftb.lib.api.gui.GuiIcons;
import ftb.lib.api.gui.callback.*;
import ftb.lib.client.GlStateManager;
import ftb.lib.gui.GuiLM;
import ftb.lib.gui.widgets.ButtonLM;
import latmod.ftbu.util.LatCoreMC;
import latmod.latmap.wp.*;
import net.minecraft.client.gui.*;

public class GuiWaypoints extends GuiLM implements IColorCallback, GuiYesNoCallback
{
	public final GuiScreen parent;
	public final PanelWaypointList panelWaypoints;
	public final ButtonLM buttonClose, buttonAdd;
	
	public GuiWaypoints(GuiScreen g)
	{
		super(null, null);
		parent = g;
		hideNEI = true;
		xSize = width;
		ySize = height;
		
		panelWaypoints = new PanelWaypointList(this);
		
		buttonClose = new ButtonLM(this, 0, 2, 16, 16)
		{
			public void onButtonPressed(int b)
			{
				gui.playClickSound();
				mc.displayGuiScreen(parent);
			}
		};
		
		buttonAdd = new ButtonLM(this, 0, 2, 16, 16)
		{
			public void onButtonPressed(int b)
			{
				gui.playClickSound();
				
				if(isShiftKeyDown())
				{
					Waypoint w = new Waypoint();
					w.type = WaypointType.BEACON;
					w.setPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
					w.name = w.posX + " " + w.posY + " " + w.posZ;
					w.dim = mc.thePlayer.dimension;
					w.color = LatCoreMC.rand.nextInt();
					Waypoints.add(w);
					gui.refreshWidgets();
				}
				else mc.displayGuiScreen(new GuiEditWaypoint(GuiWaypoints.this, null));
			}
		};
	}
	
	public void initLMGui()
	{
		xSize = width;
		ySize = height;
		guiLeft = guiTop = 0;
		buttonClose.posX = width - 18;
		buttonAdd.posX = buttonClose.posX - 18;
		refreshWidgets();
	}
	
	public void addWidgets()
	{
		panelWaypoints.width = xSize;
		mainPanel.add(buttonClose);
		mainPanel.add(panelWaypoints);
		mainPanel.add(buttonAdd);
	}
	
	public void onLMGuiClosed()
	{
		Waypoints.save();
	}
	
	public void drawBackground()
	{
		if(mouseDWheel != 0)
		{
			//int scroll = (int)((20F / (float)(height - panelWaypoints.height)) * 3F);
		}
		
		panelWaypoints.renderWidget();
		
		drawBlankRect(0, 0, zLevel, width, 20, 0x99333333);
		drawCenteredString(getFontRenderer(), "Waypoints", width / 2, 6, 0xFFFFFFFF);//LANG
		
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		buttonClose.render(GuiIcons.accept);
		buttonAdd.render(GuiIcons.add);
	}
	
	public void onColorSelected(ColorSelected c)
	{
		if(c.set || c.closeGui)
		{
			int id = c.ID.hashCode();
			if(id >= 0 && id < Waypoints.waypoints.size())
				Waypoints.waypoints.get(id).color = c.color;
		}
		if(c.closeGui) mc.displayGuiScreen(this);
		refreshWidgets();
	}
	
	public void confirmClicked(boolean b, int i)
	{
		if(b) Waypoints.remove(i);
		mc.displayGuiScreen(this);
		refreshWidgets();
	}
}