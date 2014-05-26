package ro.nextreports.server.web.dashboard.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;

import ro.nextreports.engine.exporter.util.DisplayData;

public class DisplayDinamicImageResource extends RenderedDynamicImageResource {
	
	private static final long serialVersionUID = 1L;
	
	private int width;
	private int height;
	private DisplayData data;						
	
	public DisplayDinamicImageResource(int width, int height, DisplayData data) {
		super(width, height);	
		
		this.width = width;
		this.height = height;
		this.data = data;
	}
	
	@Override
	protected boolean render(Graphics2D graphics, Attributes attributes) {				
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);				

		int canWidth = getWidth();
		int canHeight = getHeight();
		int size = canHeight;
					
		int valSize = canHeight/5;	
		int titleSize = canHeight/10;	
				
		Color background = Color.WHITE;
		if (data.getBackground() != null) {	
			background = Color.decode(data.getBackground());
		}					
			
		String value = data.getValue();
		if (value == null) {
			value = "NA";
		}	
		Color valueColor = Color.BLACK;
		if (data.getValueColor() != null) {
			valueColor = Color.decode(data.getValueColor());
		}	
		String title = data.getTitle();		
		Color titleColor = Color.BLACK;
		if (data.getTitleColor() != null) {
			titleColor = Color.decode(data.getTitleColor());
		}
		String previous = data.getPrevious();
		
		graphics.clearRect(0, 0, canWidth, canHeight);
		graphics.setPaint(background); 	
		graphics.fillRect(0,0,canWidth, canHeight);	
		
		//if (data.isShadow()) {
		//}
				
		// draw value
		graphics.setPaint(valueColor);
		graphics.setFont(new Font(graphics.getFont().getFontName(), Font.BOLD, valSize));
		int xValue = canWidth/2-graphics.getFontMetrics().stringWidth(value)/2;
		graphics.drawString(value,xValue,canHeight/2+valSize/4);
		
		// draw title
		if (title != null) {
			graphics.setPaint(titleColor);
			graphics.setFont(new Font(graphics.getFont().getFontName(), Font.BOLD, titleSize));			
			graphics.drawString(title,xValue,2*titleSize);
		}
		
		// draw previous
		if (previous != null) {
			Color previousColor = Color.GRAY;
			if (data.getPreviousColor() != null) {
				previousColor = Color.decode(data.getPreviousColor());
			}
			boolean up = data.isUp();					
			boolean shouldRise = data.isShouldRise();
						
			drawArrow(graphics, xValue+valSize/4, canHeight-2*titleSize, up, valSize, shouldRise);
			
			graphics.setPaint(previousColor);
			graphics.setFont(new Font(graphics.getFont().getFontName(), graphics.getFont().getStyle(), valSize/2));					
			graphics.drawString(previous,(int)(xValue+valSize/1.5),canHeight-2*titleSize+valSize/16);
		} 			
		
			
		
		return true;
	}		
	
	private void drawArrow(Graphics2D c, int dotX, int dotY, boolean up, int size, boolean shouldRise) {
		double d = size/1.5;
		
		GeneralPath gp = new GeneralPath();
		if (up) {
			gp.moveTo(dotX-d/2, dotY+Math.sqrt(3)*d/6);
			gp.lineTo(dotX, dotY-2*Math.sqrt(3)*d/6);
			gp.lineTo(dotX+d/2, dotY+Math.sqrt(3)*d/6);
			gp.lineTo(dotX-d/2, dotY+Math.sqrt(3)*d/6);						
		} else {
			gp.moveTo(dotX-d/2, dotY-2*Math.sqrt(3)*d/6);
			gp.lineTo(dotX+d/2, dotY-2*Math.sqrt(3)*d/6);
			gp.lineTo(dotX, dotY+Math.sqrt(3)*d/6);
			gp.lineTo(dotX-d/2, dotY-2*Math.sqrt(3)*d/6);							
		}
				
		Color color = Color.RED;	
		if ((shouldRise && up) || (!shouldRise && !up)) {
			color = Color.GREEN;
		}
		c.setPaint(color);
		c.fill(gp);
		c.setPaint(Color.GRAY);
		c.draw(gp);		
	}
	

}
