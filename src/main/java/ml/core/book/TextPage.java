package ml.core.book;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

public class TextPage extends Page {

	protected List<String> text = new ArrayList<String>();
	
	public TextPage(WindowBook window, List<String> text) {
		super(window);
		
		int h = 0;
		while ((h+=getFontRenderer().FONT_HEIGHT) < window.getPageHeight() && text.size()>0) {
			this.text.add(text.remove(0));
		}
	}
	
	@Override
	public void drawPage(Minecraft mc, int x, int y, int w, int h, float partialTick) {
		for (String ln : text) {
			mc.fontRenderer.drawString(ln, x, y, 0x000000);
			y+=mc.fontRenderer.FONT_HEIGHT;
		}
	}

}
