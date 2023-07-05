package org.kk.lib.piemenu;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * This class handles the menu item creation.
 */
public class PieMenuItem implements PieMenuInterface {
	private String menuLabel = null;
	private int menuIcon = 0;
	private List<PieMenuItem> menuChildren = null;
	private PieMenuItemClickListener menuListener = null;
	
	public PieMenuItem(String displayName) {
		this.menuLabel = displayName;
	}
	
	/**
	 * Set menu item icon.
	 * @param displayIcon - (int) Icon resource ID.
	 * <strong>secondChildItem.setDisplayIcon(R.drawable.ic_launcher);</strong>
	 */
	public void setDisplayIcon(int displayIcon) {
		this.menuIcon = displayIcon;
	}
	
	/**
	 * Set the on menu item click event.
	 */
	public void setOnMenuItemPressed(PieMenuItemClickListener listener) {
		menuListener = listener;
	}
	
	/**
	 * Set the menu child items.
	 * @param childItems - Pass the list of child items.
	 */
	public void setMenuChildren(List<PieMenuItem> childItems) {
		this.menuChildren = childItems;
	}
	
	@Override
	public String getLabel() {
		return menuLabel;
	}

	@Override
	public int getIcon() {
		return menuIcon;
	}

	@Override
	public List<PieMenuItem> getChildren() {
		return menuChildren;
	}

	@Override
	public void menuActivated() {
		if (menuListener != null) {
			menuListener.execute();
		}
	}
	
	public interface PieMenuItemClickListener {
		public void execute();
	}

	@NonNull
	@Override
	public String toString() {
		return "PieMenuItem{" +
				", menuLabel='" + menuLabel + '\'' +
				", menuIcon=" + menuIcon +
				", menuChildren=" + menuChildren +
				", menuListener=" + menuListener +
				'}';
	}
}
