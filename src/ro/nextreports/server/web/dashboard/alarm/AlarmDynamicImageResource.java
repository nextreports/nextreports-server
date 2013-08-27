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
package ro.nextreports.server.web.dashboard.alarm;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;

public class AlarmDynamicImageResource extends RenderedDynamicImageResource {

	private int x = 10; // top-bottom padding
	private int d = 3;  // distance between circle and border
	private int radius;
	private int size;

	private Ellipse2D.Double circle;
	private Ellipse2D.Double border;
	private Color color;

	public AlarmDynamicImageResource(int size, Color color) {
		super(size, size);
		this.size = size;
		radius = (size - 2 * x) / 2;
		circle = new Ellipse2D.Double(x, x, 2 * radius, 2 * radius);
		border = new Ellipse2D.Double(x - d, x - d, 2 * radius + 2 * d, 2 * radius + 2 * d);
		this.color = color;
	}

	protected boolean render(Graphics2D g2d) {
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2d.setPaint(Color.WHITE);
		Rectangle rectangle = new Rectangle(0, 0, getWidth(), getHeight());
		g2d.fill(rectangle);

		if (color != null) {
			g2d.setPaint(new GradientPaint(size / 4, size / 4, Color.WHITE,
					size - 2 * x, size - 2 * x, color, false));
		}
		g2d.fill(circle);
		g2d.setPaint(Color.GRAY);
		g2d.draw(circle);
		g2d.setPaint(Color.GRAY);
		g2d.draw(border);

		return true;
	}				

}
