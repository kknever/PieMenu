package org.kk.lib.piemenu;

import android.graphics.Path;
import android.graphics.RectF;

/**
 * This class handles the creation of wedges in the menu.
 */
public class PieMenuWedge extends Path {
	private final int x;
	private final int y;
	private final int InnerSize;
	private final int OuterSize;
	private final float StartArc;
	private final float ArcWidth;

	protected PieMenuWedge(int x, int y, int InnerSize, int OuterSize, float StartArc, float ArcWidth) {
		super();
		if (StartArc >= 360) {
			StartArc = StartArc - 360;
		}
		this.x = x;
		this.y = y;
		this.InnerSize = InnerSize;
		this.OuterSize = OuterSize;
		this.StartArc = StartArc;
		this.ArcWidth = ArcWidth;
		this.buildPath();
	}

	protected void buildPath() {
		final RectF rect = new RectF();
		final RectF rect2 = new RectF();
		// Rectangles values
		rect.set(this.x - this.InnerSize, this.y - this.InnerSize, this.x + this.InnerSize, this.y + this.InnerSize);
		rect2.set(this.x - this.OuterSize, this.y - this.OuterSize, this.x + this.OuterSize, this.y + this.OuterSize);
		this.reset();
		// this.moveTo(100, 100);
		this.arcTo(rect2, StartArc, ArcWidth);
		this.arcTo(rect, StartArc + ArcWidth, -ArcWidth);
		this.close();
	}
}
