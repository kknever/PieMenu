package org.kk.lib.piemenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the core class that handles the widget display and user interaction.
 */
public class PieMenuWidget extends View {

	private final float screen_density = getContext().getResources().getDisplayMetrics().density;
	private final PieMenuHelper helper;
	private final List<PieMenuItem> menuEntries = new ArrayList<>();
	private PieMenuItem centerCircle = null;


	// 一级菜单颜色
	private int defaultColor = Color.rgb(34, 96, 120);
	// 一级菜单Alpha
	private int defaultAlpha = 180;

	// 二级菜单颜色
	private int wedge2Color = Color.rgb(50, 50, 50);
	// 二级菜单Alpha
	private int wedge2Alpha = 210;

	// 轮廓线条颜色
	private int outlineColor = Color.rgb(150, 150, 150);
	// 轮廓线条Alpha
	private int outlineAlpha = 255; // transparency of outline

	// 二级菜单选中颜色
	private int outerSelectedColor = Color.rgb(70, 130, 180);
	// 二级菜单选中Alpha
	private int outerSelectedAlpha = 210;

	// 一级菜单禁用块颜色
	private int disabledColor = Color.rgb(34, 96, 120);
	// 一级菜单禁用块Alpha
	private int disabledAlpha = 100;

	// 菜单文本颜色
	private int textColor = Color.rgb(255, 255, 255);
	// 菜单文本Alpha
	private int textAlpha = 255;

	// 标题色值相关
	private int headerTextColor = Color.rgb(255, 255, 255);
	private int headerTextAlpha = 255;
	private int headerBackgroundColor = Color.rgb(0, 0, 0);
	private int headerBackgroundAlpha = 180;

	// 第一层按钮
	private int wedgeQty = 1; // Number of wedges
	private PieMenuWedge[] Wedges = new PieMenuWedge[wedgeQty];
	private PieMenuWedge selected = null; // Keeps track of which wedge is selected
	private PieMenuWedge enabled = null; // Keeps track of which wedge is enabled for outer ring

	// 第二层按钮
	private Rect[] iconRect = new Rect[wedgeQty];
	private int wedgeQty2 = 1; // Number of wedges
	private PieMenuWedge[] Wedges2 = new PieMenuWedge[wedgeQty2];
	private PieMenuWedge selected2 = null; // Keeps track of which wedge is selected
	private Rect[] iconRect2 = new Rect[wedgeQty2];
	private PieMenuInterface wedge2Data = null; // Keeps track off which menuItem data is being used for the outer ring
	private boolean Wedge2Shown = false; // Identifies 2nd wedge is drawn

	// 中心块圆大小
	private int MinSize = scalePX(35); // Radius of inner ring size
	// 一层圆大小
	private int MaxSize = scalePX(95); // Radius of outer ring size
	// 二层间隙大小
	private int r2MinSize = MaxSize + scalePX(5); // Radius of inner second ring size
	// 二层圆大小
	private int r2MaxSize = r2MinSize + scalePX(45); // Radius of outer second ring size
	private int MinIconSize = scalePX(15); // Min Size of Image in Wedge
	private int MaxIconSize = scalePX(35); // Max Size of Image in Wedge
	private int cRadius = MinSize - scalePX(7); // Inner Circle Radius
	private int textSize = scalePX(15); // TextSize
	private int animateTextSize = textSize;

	private int xPosition = scalePX(120); // Center X location of Menu
	private int yPosition = scalePX(120); // Center Y location of Menu

	private int xSource = 0; // Source X of clicked location
	private int ySource = 0; // Center Y of clicked location

	// Touch事件
	private boolean inWedge = false; // Identifies touch event was in first wedge
	private boolean inWedge2 = false; // Identifies touch event was in second wedge
	private boolean inCircle = false; // Identifies touch event was in middle circle
	private boolean dismissOutsideClick = true;

	private boolean HeaderBoxBounded = false; // Identifies if header box is drawn
	private String headerString = null;
	private int headerTextSize = textSize; // TextSize
	private final int headerBuffer = scalePX(8);
	private final Rect textRect = new Rect();
	private final RectF textBoxRect = new RectF();
	private int headerTextLeft;
	private int headerTextBottom;

	private static final int ANIMATE_IN = 1;
	private static final int ANIMATE_OUT = 2;

	private int animateSections = 4;
	private int r2VariableSize;
	private boolean animateOuterIn = false;
	private boolean animateOuterOut = false;
	private int clickIndex;
	private final PopupWindow mWindow;

	Paint paint = new Paint();

	/**
	 * menu widget constructor.
	 * @param context - Application Context.
	 * <strong> Usage </strong>
	 * 
	 * PieMenuWidget pieMenu = new PieMenuWidget(this);
	 * pieMenu.addMenuEntry(menuItem);
	 * pieMenu.show(view);
	 */
	public PieMenuWidget(Context context) {
		super(context);
		helper = new PieMenuHelper();
		mWindow = helper.initPopup(context);

		paint.setAntiAlias(true);
		paint.setStrokeWidth(10);

		// Gets screen specs and defaults to center of screen
		this.xPosition = (getResources().getDisplayMetrics().widthPixels) / 2;
		this.yPosition = (getResources().getDisplayMetrics().heightPixels) / 2;

		determineWedges();
		helper.onOpenAnimation(this, xPosition, yPosition, xSource, ySource);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int state = e.getAction();
		int eventX = (int) e.getX();
		int eventY = (int) e.getY();

		if (state == MotionEvent.ACTION_DOWN) {
			// selected = null;
			// selected2 = null;
			inWedge = false;
			inWedge2 = false;
			inCircle = false;

			// Checks if a pie slice is selected in first Wedge
			for (int i = 0; i < Wedges.length; i++) {
				PieMenuWedge f = Wedges[i];
				double slice = (2 * Math.PI) / wedgeQty;
				double start = (2 * Math.PI) * (0.75) - (slice / 2);
				inWedge = helper.pntInWedge(eventX, eventY, xPosition, yPosition, MinSize, MaxSize, (i * slice) + start, slice);
				if (inWedge) {
					selected = f;
					clickIndex = i;
					break;
				}
			}

			// Checks if a pie slice is selected in second Wedge
			if (Wedge2Shown) {
				for (int i = 0; i < Wedges2.length; i++) {
					PieMenuWedge f = Wedges2[i];
					float slice = 360 / wedgeQty / wedgeQty2; // 每个MenuItem分片角度大小
					float start = 270 - (slice * wedgeQty2) / 2 + (clickIndex * (slice * wedgeQty2));
					float angle = (float) Math.toRadians(360f / (wedgeQty * wedgeQty2) * (i + wedgeQty2 * clickIndex - 1));
					float xCenter = xPosition + (float) Math.sin(angle) * (r2MinSize + r2VariableSize + r2MinSize) / 2 ;
					float yCenter = yPosition + (float) -Math.cos(angle) * (r2MinSize + r2VariableSize + r2MinSize) / 2;

					inWedge2 = helper.pntInWedge(eventX, eventY, xCenter, yCenter, r2MinSize, r2MaxSize, (i * slice) + start, slice);
					Rect r = iconRect2[i];
					// 校验点击区域是否合法
					if (eventX >= r.left && eventX <= r.right && eventY >= r.top && eventY <= r.bottom) {
						inWedge2 = true;
					} else {
						inWedge = false;
					}

					if (inWedge2) {
						selected2 = f;
						break;
					}
				}
			}

			// Checks if center is there and is selected
			if(centerCircle != null) {
				inCircle = helper.pntInCircle(eventX, eventY, xPosition, yPosition, cRadius);
			}
		} else if (state == MotionEvent.ACTION_UP) {
			// execute commands...
			// put in stuff here to "return" the button that was pressed.
			if (inCircle) { // 点击了中心块
				if (Wedge2Shown) { // 如果二级菜单显示了，则收缩
					enabled = null;
					animateOuterIn = true; // sets Wedge2Shown = false;
					invalidate();
				}
				selected = null;
				centerCircle.menuActivated();
				selected2 = null;
				inCircle = false;
				return true;
			} else if (selected != null) { // 点击了第一层菜单
				for (int i = 0; i < Wedges.length; i++) {
					PieMenuWedge f = Wedges[i];
					if (f == selected) {
						// Checks if a inner ring is enabled if so closes the outer ring an
						if (enabled != null) {
							enabled = null;
							animateOuterIn = true; // sets Wedge2Shown = false;
							// If outer ring is not enabled, then executes event
						} else {
							menuEntries.get(i).menuActivated();
							// Figures out how many outer rings
							if (menuEntries.get(i).getChildren() != null) {
								determineOuterWedges(menuEntries.get(i));
								enabled = f;
								animateOuterOut = true; // sets Wedge2Shown = true;
							} else {
								Wedge2Shown = false;
							}
						}
						selected = null;
					} else {
//						if (Wedge2Shown) { // 如果二级菜单显示了，则收缩
//							enabled = null;
//							animateOuterIn = false;
//							Wedge2Shown = false;
//							selected = null;
//							selected2 = null;
//							invalidate();
//							return true;
//						}
					}
				}
			} else if (selected2 != null) { // 点击了第二层菜单
				for (int i = 0; i < Wedges2.length; i++) {
					PieMenuWedge f = Wedges2[i];
					if (f == selected2) {
						animateOuterIn = true; // sets Wedge2Shown = false;
						enabled = null;
						selected = null;
						wedge2Data.getChildren().get(i).menuActivated();
					}
				}
			} else { // 点击了其他位置
				if (dismissOutsideClick) {
					dismiss();
				}
				// selected = null;
				// enabled = null;
			}
			// selected = null;
			selected2 = null;
			inCircle = false;
		}
		invalidate();
		return true;
	}

	@SuppressLint({"DrawAllocation", "UseCompatLoadingForDrawables"})
	@Override
	protected void onDraw(Canvas c) {
		// transparency of images
		int pictureAlpha = 255;
		for (int i = 0; i < Wedges.length; i++) {
			PieMenuWedge f = Wedges[i];
			paint.setColor(outlineColor);
			paint.setAlpha(outlineAlpha);
			paint.setStyle(Paint.Style.STROKE);
			c.drawPath(f, paint); // 一级菜单分割线
			if (f == enabled && Wedge2Shown) { // 一级选中 二级展开
				paint.setColor(wedge2Color);
				paint.setAlpha(wedge2Alpha);
				paint.setStyle(Paint.Style.FILL);
				c.drawPath(f, paint);
			} else if (f != enabled && Wedge2Shown) { // 一级未选中
				paint.setColor(disabledColor);
				paint.setAlpha(disabledAlpha);
				paint.setStyle(Paint.Style.FILL);
				c.drawPath(f, paint);
			} else if (f == enabled && !Wedge2Shown) { // 一级选中 二级未展开
				paint.setColor(wedge2Color);
				paint.setAlpha(wedge2Alpha);
				paint.setStyle(Paint.Style.FILL);
				c.drawPath(f, paint);
			} else if (f == selected) { // 一级选中并已展开
				paint.setColor(wedge2Color);
				paint.setAlpha(wedge2Alpha);
				paint.setStyle(Paint.Style.FILL);
				c.drawPath(f, paint);
			} else { // 未选中 未展开
				paint.setColor(defaultColor);
				paint.setAlpha(defaultAlpha);
				paint.setStyle(Paint.Style.FILL);
				c.drawPath(f, paint);
			}

			Rect rf = iconRect[i];
			if ((menuEntries.get(i).getIcon() != 0) && (menuEntries.get(i).getLabel() != null)) {
				// This will look for a "new line" and split into multiple lines
				String menuItemName = menuEntries.get(i).getLabel();
				String[] stringArray = menuItemName.split("\n");

				paint.setColor(textColor);
				if (f != enabled && Wedge2Shown) {
					paint.setAlpha(disabledAlpha);
				} else {
					paint.setAlpha(textAlpha);
				}
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(textSize);

				Rect rect = new Rect();
				float textHeight = 0;
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rect);
					textHeight = textHeight + (rect.height() + 3);
				}

				Rect rf2 = new Rect();
				rf2.set(rf.left, rf.top - ((int) textHeight / 2), rf.right, rf.bottom - ((int) textHeight / 2));

				float textBottom = rf2.bottom;
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rect);
					float textLeft = rf.centerX() - rect.width() / 2;
					textBottom = textBottom + (rect.height() + 3);
					c.drawText(s, textLeft - rect.left, textBottom - rect.bottom, paint);
				}

				// Puts in the Icon
				Drawable drawable = getResources().getDrawable(menuEntries.get(i).getIcon());
				drawable.setBounds(rf2);
				if (f != enabled && Wedge2Shown) {
					drawable.setAlpha(disabledAlpha);
				} else {
					drawable.setAlpha(pictureAlpha);
				}
				drawable.draw(c);
				// Icon Only
			} else if (menuEntries.get(i).getIcon() != 0) {
				// Puts in the Icon
				Drawable drawable = getResources().getDrawable(menuEntries.get(i).getIcon());
				drawable.setBounds(rf);
				if (f != enabled && Wedge2Shown) {
					drawable.setAlpha(disabledAlpha);
				} else {
					drawable.setAlpha(pictureAlpha);
				}
				drawable.draw(c);
				// Text Only
			} else {
				paint.setColor(textColor);
				if (f != enabled && Wedge2Shown) {
					paint.setAlpha(disabledAlpha);
				} else {
					paint.setAlpha(textAlpha);
				}
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(textSize);

				// This will look for a "new line" and split into multiple lines
				String menuItemName = menuEntries.get(i).getLabel();
				String[] stringArray = menuItemName.split("\n");

				// gets total height
				Rect rect = new Rect();
				float textHeight = 0;
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rect);
					textHeight = textHeight + (rect.height() + 3);
				}
				float textBottom = rf.centerY() - (textHeight / 2);
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rect);
					float textLeft = rf.centerX() - rect.width() / 2;
					textBottom = textBottom + (rect.height() + 3);
					c.drawText(s, textLeft - rect.left, textBottom - rect.bottom, paint);
				}
			}
		}

		// Animate the outer ring in/out
		if (animateOuterIn) {
			animateOuterWedges(ANIMATE_IN);	// 二级收缩
		} else if (animateOuterOut) {
			animateOuterWedges(ANIMATE_OUT); // 二级展开
		}

		if (Wedge2Shown) {
			for (int i = 0; i < Wedges2.length; i++) {
				PieMenuWedge f = Wedges2[i];
				paint.setColor(outlineColor);
				paint.setAlpha(outlineAlpha);
				paint.setStyle(Paint.Style.STROKE);
				c.drawPath(f, paint);
				if (f == selected2) {
					paint.setColor(outerSelectedColor);
					paint.setAlpha(outerSelectedAlpha);
				} else {
					paint.setColor(wedge2Color);
					paint.setAlpha(wedge2Alpha);
				}
				paint.setStyle(Paint.Style.FILL);
				c.drawPath(f, paint);

				Rect rf = iconRect2[i];

				if ((wedge2Data.getChildren().get(i).getIcon() != 0) && (wedge2Data.getChildren().get(i).getLabel() != null)) {
					// This will look for a "new line" and split into multiple lines
					String menuItemName = wedge2Data.getChildren().get(i).getLabel();
					String[] stringArray = menuItemName.split("\n");

					paint.setColor(textColor);
					paint.setAlpha(textAlpha);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(animateTextSize);

					Rect rect = new Rect();
					float textHeight = 0;
					for (String s : stringArray) {
						paint.getTextBounds(s, 0, s.length(), rect);
						textHeight = textHeight + (rect.height() + 3);
					}

					Rect rf2 = new Rect();
					rf2.set(rf.left, rf.top - ((int) textHeight / 2), rf.right, rf.bottom - ((int) textHeight / 2));

					float textBottom = rf2.bottom;
					for (String s : stringArray) {
						paint.getTextBounds(s, 0, s.length(), rect);
						float textLeft = rf.centerX() - rect.width() / 2;
						textBottom = textBottom + (rect.height() + 3);
						c.drawText(s, textLeft - rect.left, textBottom - rect.bottom, paint);
					}

					// Puts in the Icon
					Drawable drawable = getResources().getDrawable(wedge2Data.getChildren().get(i).getIcon());
					drawable.setBounds(rf2);
					drawable.setAlpha(pictureAlpha);
					drawable.draw(c);
					// Icon Only
				} else if (wedge2Data.getChildren().get(i).getIcon() != 0) {
					// Puts in the Icon
					Drawable drawable = getResources().getDrawable(
							wedge2Data.getChildren().get(i).getIcon());
					drawable.setBounds(rf);
					drawable.setAlpha(pictureAlpha);
					drawable.draw(c);
					// Text Only
				} else {
					paint.setColor(textColor);
					paint.setAlpha(textAlpha);
					paint.setStyle(Paint.Style.FILL);
					paint.setTextSize(animateTextSize);

					// This will look for a "new line" and split into multiple lines
					String menuItemName = wedge2Data.getChildren().get(i).getLabel();
					String[] stringArray = menuItemName.split("\n");

					// gets total height
					Rect rect = new Rect();
					float textHeight = 0;
					for (String s : stringArray) {
						paint.getTextBounds(s, 0, s.length(), rect);
						textHeight = textHeight + (rect.height() + 3);
					}
					float textBottom = rf.centerY() - (textHeight / 2);
					for (String s : stringArray) {
						paint.getTextBounds(s, 0, s.length(), rect);
						float textLeft = rf.centerX() - rect.width() / 2;
						textBottom = textBottom + (rect.height() + 3);
						c.drawText(s, textLeft - rect.left, textBottom - rect.bottom, paint);
					}
				}
			}
		}

		// Check if the user has given input for centre circle
		if(centerCircle != null) {
			// Draws the Middle Circle
			paint.setColor(outlineColor);
			paint.setAlpha(outlineAlpha);
			paint.setStyle(Paint.Style.STROKE);
			c.drawCircle(xPosition, yPosition, cRadius, paint);
			if (inCircle) {
				paint.setColor(outerSelectedColor);
				paint.setAlpha(outerSelectedAlpha);
				paint.setStyle(Paint.Style.FILL);
				c.drawCircle(xPosition, yPosition, cRadius, paint);
				helper.onCloseAnimation(this, xPosition, yPosition, xSource, ySource);
			} else {
				paint.setColor(defaultColor);
				paint.setAlpha(defaultAlpha);
				paint.setStyle(Paint.Style.FILL);
				c.drawCircle(xPosition, yPosition, cRadius, paint);
			}
			
			// Draw the circle picture
			if ((centerCircle.getIcon() != 0) && (centerCircle.getLabel() != null)) {

				// This will look for a "new line" and split into multiple lines
				String menuItemName = centerCircle.getLabel();
				String[] stringArray = menuItemName.split("\n");

				paint.setColor(textColor);
				paint.setAlpha(textAlpha);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(textSize);

				Rect rectText = new Rect();
				Rect rectIcon = new Rect();
				Drawable drawable = getResources().getDrawable(centerCircle.getIcon());

				int h = getIconSize(drawable.getIntrinsicHeight(), MinIconSize, MaxIconSize);
				int w = getIconSize(drawable.getIntrinsicWidth(), MinIconSize, MaxIconSize);
				rectIcon.set(xPosition - w / 2, yPosition - h / 2, xPosition + w / 2, yPosition + h / 2);

				float textHeight = 0;
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rectText);
					textHeight = textHeight + (rectText.height() + 3);
				}
				rectIcon.set(rectIcon.left, rectIcon.top - ((int) textHeight / 2), rectIcon.right, rectIcon.bottom - ((int) textHeight / 2));
				float textBottom = rectIcon.bottom;
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rectText);
					float textLeft = xPosition - rectText.width() / 2;
					textBottom = textBottom + (rectText.height() + 3);
					c.drawText(s, textLeft - rectText.left, textBottom - rectText.bottom, paint);
				}

				// Puts in the Icon
				drawable.setBounds(rectIcon);
				drawable.setAlpha(pictureAlpha);
				drawable.draw(c);
				// Icon Only
			} else if (centerCircle.getIcon() != 0) {
				Rect rect = new Rect();
				Drawable drawable = getResources().getDrawable(centerCircle.getIcon());
				int h = getIconSize(drawable.getIntrinsicHeight(), MinIconSize, MaxIconSize);
				int w = getIconSize(drawable.getIntrinsicWidth(), MinIconSize, MaxIconSize);
				rect.set(xPosition - w / 2, yPosition - h / 2, xPosition + w / 2,yPosition + h / 2);
				drawable.setBounds(rect);
				drawable.setAlpha(pictureAlpha);
				drawable.draw(c);
				// Text Only
			} else {
				// Puts in the Text if no Icon
				paint.setColor(textColor);
				paint.setAlpha(textAlpha);
				paint.setStyle(Paint.Style.FILL);
				paint.setTextSize(textSize);
	
				// This will look for a "new line" and split into multiple lines
				String menuItemName = centerCircle.getLabel();
				String[] stringArray = menuItemName.split("\n");
	
				// gets total height
				Rect rect = new Rect();
				float textHeight = 0;
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rect);
					textHeight = textHeight + (rect.height() + 3);
				}
				float textBottom = yPosition - (textHeight / 2);
				for (String s : stringArray) {
					paint.getTextBounds(s, 0, s.length(), rect);
					float textLeft = xPosition - rect.width() / 2;
					textBottom = textBottom + (rect.height() + 3);
					c.drawText(s, textLeft - rect.left, textBottom - rect.bottom, paint);
				}
			}
		}

		// Draws Text in TextBox
		if (headerString != null) {	// 标题
			paint.setTextSize(headerTextSize);
			paint.getTextBounds(headerString, 0, headerString.length(), this.textRect);
			if (!HeaderBoxBounded) {
				determineHeaderBox();
				HeaderBoxBounded = true;
			}

			paint.setColor(outlineColor);
			paint.setAlpha(outlineAlpha);
			paint.setStyle(Paint.Style.STROKE);
			c.drawRoundRect(this.textBoxRect, scalePX(5), scalePX(5), paint);
			paint.setColor(headerBackgroundColor);
			paint.setAlpha(headerBackgroundAlpha);
			paint.setStyle(Paint.Style.FILL);
			c.drawRoundRect(this.textBoxRect, scalePX(5), scalePX(5), paint);

			paint.setColor(headerTextColor);
			paint.setAlpha(headerTextAlpha);
			paint.setStyle(Paint.Style.FILL);
			paint.setTextSize(headerTextSize);
			c.drawText(headerString, headerTextLeft, headerTextBottom, paint);
		}
	}

	private int scalePX(int dp_size) {
		return (int) (dp_size * screen_density + 0.5f);
	}
	
	private int getIconSize(int iconSize, int minSize, int maxSize) {
		if (iconSize > minSize) {
			// iconSize < maxSize
			return Math.min(iconSize, maxSize);
		} else { // iconSize < minSize
			return minSize;
		}
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	private void animateOuterWedges( int animation_direction) {
		boolean animationComplete = false;
		// Wedge 2
		float slice2 = 360 / wedgeQty / wedgeQty2; // 每个MenuItem分片角度大小
		float start_slice2 = 270 - (slice2 * wedgeQty2) / 2 + (clickIndex * (slice2 * wedgeQty2));
		this.Wedges2 = new PieMenuWedge[wedgeQty2];
		this.iconRect2 = new Rect[wedgeQty2];

		Wedge2Shown = true;
		int wedgeSizeChange = (r2MaxSize - r2MinSize) / animateSections;

		if (animation_direction == ANIMATE_OUT) {
			if (r2MinSize + r2VariableSize + wedgeSizeChange < r2MaxSize) {
				r2VariableSize += wedgeSizeChange;
			} else {
				animateOuterOut = false;
				r2VariableSize = r2MaxSize - r2MinSize;
				animationComplete = true;
			}

			// animates text size change
			this.animateTextSize = (textSize / animateSections) * (r2VariableSize / wedgeSizeChange);

			// calculates new wedge sizes
			for (int i = 0; i < Wedges2.length; i++) {
				this.Wedges2[i] = new PieMenuWedge(xPosition, yPosition, r2MinSize, r2MinSize + r2VariableSize, (i * slice2) + start_slice2, slice2);

				float angle = (float) Math.toRadians(360f / (wedgeQty * wedgeQty2) * (i + wedgeQty2 * clickIndex - 1));
				float xCenter = xPosition + (float) Math.sin(angle) * (r2MinSize + r2VariableSize + r2MinSize) / 2 ;
				float yCenter = yPosition + (float) -Math.cos(angle) * (r2MinSize + r2VariableSize + r2MinSize) / 2;

				int h = MaxIconSize;
				int w = MaxIconSize;
				if (wedge2Data.getChildren().get(i).getIcon() != 0) {
					Drawable drawable = getResources().getDrawable(wedge2Data.getChildren().get(i).getIcon());
					h = getIconSize(drawable.getIntrinsicHeight(), MinIconSize, MaxIconSize);
					w = getIconSize(drawable.getIntrinsicWidth(), MinIconSize, MaxIconSize);
				}

				if (r2VariableSize < h) {
					h = r2VariableSize;
				}
				if (r2VariableSize < w) {
					w = r2VariableSize;
				}

				this.iconRect2[i] = new Rect(
						(int) xCenter - w / 2,
						(int) yCenter - h / 2,
						(int) xCenter + w / 2,
						(int) yCenter + h / 2);

				int widthOffset = MaxSize;
				if (widthOffset < this.textRect.width() / 2) {
					widthOffset = this.textRect.width() / 2 + scalePX(3);
				}
				this.textBoxRect.set(
						(xPosition - (widthOffset)),
						(int) (yPosition - (r2MinSize + r2VariableSize) - headerBuffer - this.textRect.height() - scalePX(3)),
						(xPosition + (widthOffset)),
						(yPosition - (r2MinSize + r2VariableSize) - headerBuffer + scalePX(3)));

				this.headerTextBottom = yPosition - (r2MinSize + r2VariableSize) - headerBuffer - this.textRect.bottom;
			}
		} else if (animation_direction == ANIMATE_IN) {
			if (r2MinSize < r2MaxSize - r2VariableSize - wedgeSizeChange) {
				r2VariableSize += wedgeSizeChange;
			} else {
				animateOuterIn = false;
				r2VariableSize = r2MaxSize;
				animationComplete = true;
			}

			// animates text size change
			this.animateTextSize = textSize - ((textSize / animateSections) * (r2VariableSize / wedgeSizeChange));

			for (int i = 0; i < Wedges2.length; i++) {
				this.Wedges2[i] = new PieMenuWedge(xPosition, yPosition, r2MinSize, r2MaxSize - r2VariableSize, (i * slice2) + start_slice2, slice2);

				float angle = (float) Math.toRadians(360f / (wedgeQty * wedgeQty2) * (i + wedgeQty2 * clickIndex - 1));
				float xCenter = xPosition + (float) Math.sin(angle) * (r2MinSize + r2VariableSize + r2MinSize) / 2 ;
				float yCenter = yPosition + (float) -Math.cos(angle) * (r2MinSize + r2VariableSize + r2MinSize) / 2;

				int h = MaxIconSize;
				int w = MaxIconSize;
				if (wedge2Data.getChildren().get(i).getIcon() != 0) {
					Drawable drawable = getResources().getDrawable(wedge2Data.getChildren().get(i).getIcon());
					h = getIconSize(drawable.getIntrinsicHeight(), MinIconSize, MaxIconSize);
					w = getIconSize(drawable.getIntrinsicWidth(), MinIconSize, MaxIconSize);
				}

				if (r2MaxSize - r2MinSize - r2VariableSize < h) {
					h = r2MaxSize - r2MinSize - r2VariableSize;
				}
				if (r2MaxSize - r2MinSize - r2VariableSize < w) {
					w = r2MaxSize - r2MinSize - r2VariableSize;
				}

				this.iconRect2[i] = new Rect((int) xCenter - w / 2,
						(int) yCenter - h / 2, (int) xCenter + w / 2,
						(int) yCenter + h / 2);

				// computes header text box
				int heightOffset = r2MaxSize - r2VariableSize;
				int widthOffset = MaxSize;
				if (MaxSize > r2MaxSize - r2VariableSize) {
					heightOffset = MaxSize;
				}
				if (widthOffset < this.textRect.width() / 2) {
					widthOffset = this.textRect.width() / 2 + scalePX(3);
				}
				this.textBoxRect.set(
						(xPosition - (widthOffset)),
						(int) (yPosition - (heightOffset) - headerBuffer - this.textRect.height() - scalePX(3)),
						(xPosition + (widthOffset)),
						(yPosition - (heightOffset) - headerBuffer + scalePX(3)));
				this.headerTextBottom = yPosition - (heightOffset) - headerBuffer - this.textRect.bottom;
			}
		}

		if (animationComplete) {
			r2VariableSize = 0;
			this.animateTextSize = textSize;
			if (animation_direction == ANIMATE_IN) {
				Wedge2Shown = false;
			}
		}
		invalidate(); // re-draws the picture
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	private void determineWedges() {
		int entriesQty = menuEntries.size();
		if (entriesQty > 0) {
			wedgeQty = entriesQty;

			float degSlice = 360 / wedgeQty; // 每个MenuItem分片角度大小
			float start_degSlice = 270 - (degSlice / 2);
			// calculates where to put the images
			double rSlice = (2 * Math.PI) / wedgeQty;
			double rStart = (2 * Math.PI) * (0.75) - (rSlice / 2);

			this.Wedges = new PieMenuWedge[wedgeQty];
			this.iconRect = new Rect[wedgeQty];

			for (int i = 0; i < Wedges.length; i++) {
				this.Wedges[i] = new PieMenuWedge(xPosition, yPosition, MinSize, MaxSize, (i * degSlice) + start_degSlice, degSlice);
				double angle = ((rSlice * i) + (rSlice * 0.5)) + rStart;
				float xCenter = (float) (Math.cos(angle) * (MaxSize + MinSize) / 2) + xPosition;
				float yCenter = (float) (Math.sin(angle) * (MaxSize + MinSize) / 2) + yPosition;

				int h = MaxIconSize;
				int w = MaxIconSize;
				if (menuEntries.get(i).getIcon() != 0) {
					Drawable drawable = getResources().getDrawable(menuEntries.get(i).getIcon());
					h = getIconSize(drawable.getIntrinsicHeight(), MinIconSize, MaxIconSize);
					w = getIconSize(drawable.getIntrinsicWidth(), MinIconSize, MaxIconSize);
				}

				this.iconRect[i] = new Rect((int) xCenter - w / 2,
						(int) yCenter - h / 2, (int) xCenter + w / 2,
						(int) yCenter + h / 2);
			}
			invalidate(); // re-draws the picture
		}
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	private void determineOuterWedges(PieMenuItem entry) {
		wedgeQty2 = entry.getChildren().size();
		// Wedge 2
		float degSlice2 = 360 / wedgeQty /wedgeQty2;
		float start_degSlice2 = 270 - (degSlice2 * wedgeQty2) / 2 + (clickIndex * (degSlice2 * wedgeQty2));
		// calculates where to put the images
		double rSlice2 = (2 * Math.PI) /wedgeQty/ wedgeQty2;
		double rStart2 = (2 * Math.PI) * (0.75) - (rSlice2 / 2);

		this.Wedges2 = new PieMenuWedge[wedgeQty2];
		this.iconRect2 = new Rect[wedgeQty2];

		for (int i = 0; i < Wedges2.length; i++) {
			this.Wedges2[i] = new PieMenuWedge(xPosition, yPosition, r2MinSize, r2MaxSize, (i * degSlice2) + start_degSlice2, degSlice2);
			double angle = ((rSlice2 * i) + (rSlice2 * 0.5)) + rStart2;
			float xCenter = (float) (Math.cos(angle) * (r2MaxSize + r2MinSize) / 2) + xPosition;
			float yCenter = (float) (Math.sin(angle) * (r2MaxSize + r2MinSize) / 2) + yPosition;

			int h = MaxIconSize;
			int w = MaxIconSize;
			if (entry.getChildren().get(i).getIcon() != 0) {
				Drawable drawable = getResources().getDrawable(entry.getChildren().get(i).getIcon());
				h = getIconSize(drawable.getIntrinsicHeight(), MinIconSize, MaxIconSize);
				w = getIconSize(drawable.getIntrinsicWidth(), MinIconSize, MaxIconSize);
			}
			this.iconRect2[i] = new Rect(
					(int) xCenter - w / 2,
					(int) yCenter - h / 2,
					(int) xCenter + w / 2,
					(int) yCenter + h / 2);
		}
		this.wedge2Data = entry;
		invalidate(); // re-draws the picture
	}

	private void determineHeaderBox() {
		this.headerTextLeft = xPosition - this.textRect.width() / 2;
		this.headerTextBottom = yPosition - (MaxSize) - headerBuffer - this.textRect.bottom;
		int offset = MaxSize;
		if (offset < this.textRect.width() / 2) {
			offset = this.textRect.width() / 2 + scalePX(3);
		}
		this.textBoxRect.set(
				(xPosition - (offset)),
				(int) (yPosition - (MaxSize) - headerBuffer - this.textRect.height() - scalePX(3)),
				(xPosition + (offset)),
				(yPosition - (MaxSize) - headerBuffer + scalePX(3)));
	}

	/******************************************************************************************************************************
	 * ADD ITEM METHODS
	 ******************************************************************************************************************************/
	
	/**
	 * This method allows the user to add an array of menu items.
	 * @param menuItems - List object of PieMenuItem.
	 * @return
	 */
	public void addMenuEntry(List<PieMenuItem> menuItems) {
		menuEntries.addAll(menuItems);
		determineWedges();
	}
	
	/**
	 *  This method allows the user to add a menu item.
	 * @param menuItem - Object of PieMenuItem.
	 * @return
	 */
	public void addMenuEntry(PieMenuItem menuItem) {
		menuEntries.add(menuItem);
		determineWedges();
	}
	
	/******************************************************************************************************************************
	 * SET METHODS
	 ******************************************************************************************************************************/
	
	/**
	 * <strong> Optional </strong>
	 * This method allows the user to add the central menu item.
	 * @param menuItem - Object of PieMenuItem.
	 * @return
	 */
	public void setCenterCircle(PieMenuItem menuItem) {
		centerCircle = menuItem;
	}

	/**
	 * <strong> Optional </strong>
	 * Set the middle ring radius of the menu widget.
	 * @param InnerRadius - Inner border radius.
	 * @param OuterRadius - Outer border radius.
	 */
	public void setInnerRingRadius(int InnerRadius, int OuterRadius) {
		this.MinSize = scalePX(InnerRadius);
		this.MaxSize = scalePX(OuterRadius);
		determineWedges();
	}

	/**
	 * <strong> Optional </strong>
	 * Set the outer ring radius of the menu widget.
	 * @param InnerRadius - Inner border radius.
	 * @param OuterRadius - Outer border radius.
	 */
	public void setOuterRingRadius(int InnerRadius, int OuterRadius) {
		this.r2MinSize = scalePX(InnerRadius);
		this.r2MaxSize = scalePX(OuterRadius);
		determineWedges();
	}

	/**
	 * <strong> Optional </strong>
	 * This method set the radius of the centre circle.
	 * @param centerRadius - Radius of the circle.
	 */
	public void setCenterCircleRadius(int centerRadius) {
		this.cRadius = scalePX(centerRadius);
		determineWedges();
	}

	/**
	 * Set the menu items text size.
	 * @param TextSize - Text size of the menu items.
	 */
	public void setTextSize(int TextSize) {
		this.textSize = scalePX(TextSize);
		this.animateTextSize = this.textSize;
	}

	/**
	 * Set the menu item icon size.
	 * @param minIconSize - Minimum scaled size.
	 * @param maxIconSize - Maximum scaled size.
	 */
	public void setIconSize(int minIconSize, int maxIconSize) {
		this.MinIconSize = scalePX(minIconSize);
		this.MaxIconSize = scalePX(maxIconSize);
		determineWedges();
	}

	public void setCenterLocation(int x, int y) {
		this.xPosition = x;
		this.yPosition = y;
		determineWedges();
		helper.onOpenAnimation(this, xPosition, yPosition, xSource, ySource);
	}

	/**
	 * 附着在view的左上角
	 */
	public void setCenterLocationWithView(View view) {
		setCenterLocationWithView(view, 0, 0);
	}

	/**
	 * 附着在view的左上角
	 */
	public void setCenterLocationWithView(View view, int offsetX, int offsetY) {
		float x = view.getX();
		float y = view.getY();
		this.xPosition = (int) (x + MaxSize / 2 - MinSize + offsetX);
		this.yPosition = (int) (y + MaxSize / 2 + MinSize / 2 + offsetY);
		determineWedges();
		helper.onOpenAnimation(this, xPosition, yPosition, xSource, ySource);
	}

//	public void setSourceLocation(int x, int y) {
//		this.xSource = x;
//		this.ySource = y;
//		helper.onOpenAnimation(this, xPosition, yPosition, xSource, ySource);
//	}
	
	public void setDismissOnOutsideClick(boolean clickDismiss) {
		this.dismissOutsideClick = clickDismiss;
	}

	/**
	 * Set the widget animation speed.
	 * @param millis - Time in milliseconds.
	 */
	public void setAnimationSpeed(long millis) {
		helper.onOpenAnimation(this, xPosition, yPosition, xSource, ySource, millis);
	}

	/**
	 * Set the menu inner ring color.
	 * @param color - Color value .
	 * @param alpha - Alpha blend value.
	 */
	public void setInnerRingColor(int color, int alpha) {
		this.defaultColor = color;
		this.defaultAlpha = alpha;
	}
	
	/**
	 * Set the menu outer ring color.
	 * @param color - Color value .
	 * @param alpha - Alpha blend value.
	 */
	public void setOuterRingColor(int color, int alpha) {
		this.wedge2Color = color;
		this.wedge2Alpha = alpha;
	}

	/**
	 * Set the widget outline color.
	 * @param color - Color value .
	 * @param alpha - Alpha blend value.
	 */
	public void setOutlineColor(int color, int alpha) {
		this.outlineColor = color;
		this.outlineAlpha = alpha;
	}

	/**
	 * Set the widget on select color.
	 * @param color - Color value .
	 * @param alpha - Alpha blend value.
	 */
	public void setOuterSelectedColor(int color, int alpha) {
		this.outerSelectedColor = color;
		this.outerSelectedAlpha = alpha;
	}

	/**
	 * Set the widget on disabled color.
	 * @param color - Color value .
	 * @param alpha - Alpha blend value.
	 */
	public void setDisabledColor(int color, int alpha) {
		this.disabledColor = color;
		this.disabledAlpha = alpha;
	}

	/**
	 * Set the widget text color.
	 * @param color - Text color
	 * @param alpha - Text alpha belnd value
	 */
	public void setTextColor(int color, int alpha) {
		this.textColor = color;
		this.textAlpha = alpha;
	}

	/**
	 * <strong> Optional </strong>
	 * Display a header with text along with the displayed menu.
	 * @param header - Header text to be displayed.
	 * @param TextSize -  Text size.
	 */
	public void setHeader(String header, int TextSize) {
		this.headerString = header;
		this.headerTextSize = scalePX(TextSize);
		HeaderBoxBounded = false;
	}

	/**
	 * <strong> Optional </strong>
	 * Display a header with text along with the displayed menu. This method helps in configuring the display background.
	 */
	public void setHeaderColors(int TextColor, int TextAlpha, int BgColor, int BgAlpha) {
		this.headerTextColor = TextColor;
		this.headerTextAlpha = TextAlpha;
		this.headerBackgroundColor = BgColor;
		this.headerBackgroundAlpha = BgAlpha;
	}
	
//	/**
//	 * Shows the menu widget.
//	 * @param anchor - View to be anchored to.
//	 * @param posX - Position X. Pass 0 if not needed.
//	 * @param posY - Position Y. Pass 0 if not needed.
//	 */
//	public void show(View anchor, int posX, int posY) {
//		mWindow.setContentView(this);
//		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, posX, posY);
//	}
	
	/**
	 * Shows the menu widget.
	 * @param anchor - View to be anchored to.
	 */
	public void show(View anchor) {
		mWindow.setContentView(this);
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, this.xSource, this.ySource);
	}
	
	/**
	 * Dismisses the menu widget.
	 */
	public void dismiss() {
		if (mWindow != null) {
			mWindow.dismiss();
		}
	}
}
