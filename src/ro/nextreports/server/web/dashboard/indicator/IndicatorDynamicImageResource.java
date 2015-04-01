/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.dashboard.indicator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;

import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;

import ro.nextreports.engine.exporter.util.IndicatorData;
import ro.nextreports.engine.util.StringUtil;

public class IndicatorDynamicImageResource extends RenderedDynamicImageResource {
	
	private static final long serialVersionUID = 1L;
	
	private int width;
	private int height;
	private IndicatorData data;						
	
	public IndicatorDynamicImageResource(int width, int height, IndicatorData data) {
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
		if (10*canWidth/11 <= canHeight) {
		    size = 10*canWidth/11;
		}
		  
		int radix = 7*size/11;
		int arcWidth = size/2; 
		int x = canWidth/2;
		int y = 8*size/11; 			
		
		graphics.setPaint(data.getBackground());
		Rectangle rectangle = new Rectangle(0, 0, getWidth(), getHeight());
		graphics.fill(rectangle);			
					
		// draw initial background color
		Arc2D externalArc = new Arc2D.Double(x-size/2, y-size/2, size, size, 0, 180, Arc2D.OPEN);		
		Arc2D innerArc = new Arc2D.Double(x-size/2 + arcWidth/2, y-size/2 + arcWidth/2, size - arcWidth, size - arcWidth, 0, 180, Arc2D.OPEN);	
		Area i1 = new Area(externalArc);
		Area i2 = new Area(innerArc);
		i1.subtract(i2);
		graphics.setPaint(new Color(240, 240, 240));		
		graphics.fill(i1);
				
		// draw background color depending on gauge's value
		double val = data.getValue();
		if (data.getValue() < data.getMin()) {
			val = data.getMin();
		}
		if (data.getValue() > data.getMax()) {
			val = data.getMax();
		}
		double range = Math.abs(data.getMax()-data.getMin());
		double delta = Math.abs(data.getMin() - val);
		double f =  (range - delta) /range;
		double angle =  180 * f ;			
		Arc2D externalArcColor = new Arc2D.Double(x-size/2, y-size/2, size, size, angle, 180 - angle, Arc2D.PIE);
		Arc2D innerArcColor = new Arc2D.Double(x-size/2 + arcWidth/2, y-size/2 + arcWidth/2, size - arcWidth, size - arcWidth, 0, 180, Arc2D.PIE);
		Area a1 = new Area(externalArcColor);
		Area a2 = new Area(innerArcColor);
		a1.subtract(a2);		
		graphics.setPaint(new GradientPaint(x-size/2, y-size/2, Color.WHITE, x+size/2, y-size/2, data.getColor()));		
		graphics.fill(a1);
						
		// clear any glitches under inner arc
		Arc2D ia = new Arc2D.Double(x-size/2 + arcWidth/2, y-size/2 + arcWidth/2, size - arcWidth, size - arcWidth, 0, 360, Arc2D.PIE);		
		Area ia2 = new Area(ia);				
		graphics.setPaint(data.getBackground());		
		graphics.fill(ia2);
		
		// draw arcs & base lines
		graphics.setPaint(Color.GRAY);	
		graphics.setStroke(new BasicStroke(1f));
		graphics.draw(externalArc);				
		graphics.draw(innerArc);		
		graphics.drawLine(x-size/2, y, x-size/2 + arcWidth/2, y);
		graphics.drawLine(x+size/2 - arcWidth/2, y, x+size/2, y);		
		
		if (data.isShowMinMax()) {
			Font font = new Font(graphics.getFont().getFontName(), graphics.getFont().getStyle(), size / 11);
			graphics.setFont(font);
			String smin;
			if (hasNoDecimals(data.getMin())) {
				smin = String.valueOf((int) data.getMin());
			} else {
				smin = String.valueOf(data.getMin());
			}
			if ((data.getUnit() != null) && !data.getUnit().isEmpty()) {
				smin = smin + data.getUnit();
			}
			int swidth = graphics.getFontMetrics().stringWidth(smin);
			graphics.drawString(smin, x-size/2 + arcWidth / 4 - swidth / 2, y + size/11);

			String smax;
			if (hasNoDecimals(data.getMax())) {
				smax = String.valueOf((int) data.getMax());
			} else {
				smax = String.valueOf(data.getMax());
			}

			if ((data.getUnit() != null) && !data.getUnit().isEmpty()) {
				smax = smax + data.getUnit();
			}
			swidth = graphics.getFontMetrics().stringWidth(smax);
			graphics.drawString(smax, x + size/2 - arcWidth/4 - swidth / 2 , y + size/11);
		}
		
		if ((data.getDescription() != null) && !data.getDescription().isEmpty()) {
			Font font = new Font(graphics.getFont().getFontName(), graphics.getFont().getStyle(), size/12); 
			graphics.setFont(font);
			int swidth = graphics.getFontMetrics().stringWidth(data.getDescription());
			graphics.setPaint(Color.GRAY);
			graphics.drawString(data.getDescription(), x - swidth/2 , y + size/12);
		}
		
		Font font = new Font(graphics.getFont().getFontName(), Font.BOLD, size/8); 
		graphics.setFont(font);
		graphics.setPaint(Color.BLACK);
		String svalue;
		if (hasNoDecimals(data.getValue())) {
			svalue = String.valueOf((int)data.getValue());
		} else {
			svalue = String.valueOf(data.getValue());
			if (data.getPattern() != null) {
				svalue = StringUtil.getValueAsString(data.getValue(), data.getPattern());
			}			
		}
		if ((data.getUnit() != null) && !data.getUnit().isEmpty()) {
			svalue = svalue + data.getUnit();
		}
		int swidth = graphics.getFontMetrics().stringWidth(svalue);
		graphics.drawString(svalue, x - swidth/2 , y );
		
		if ((data.getTitle() != null) && !data.getTitle().isEmpty()) {
			font = new Font(graphics.getFont().getFontName(), Font.BOLD, size/11); 
			graphics.setFont(font);
			swidth = graphics.getFontMetrics().stringWidth(data.getTitle());
			graphics.drawString(data.getTitle(), x - swidth/2 , y - radix + arcWidth/2 + size/11 );
		}				
		
		return true;
	}		

	private boolean hasNoDecimals(double d) {
		return ((int)d == d);
	}

}
