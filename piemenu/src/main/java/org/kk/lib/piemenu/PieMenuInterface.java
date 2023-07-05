package org.kk.lib.piemenu;

import java.util.List;

public interface PieMenuInterface {
	public String getLabel();
	public int getIcon();
	public List<PieMenuItem> getChildren();
	public void menuActivated();
}
