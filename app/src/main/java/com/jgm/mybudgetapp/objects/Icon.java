package com.jgm.mybudgetapp.objects;

public class Icon {

    private final int id;
    private final int icon;
    private final String iconName;

    public Icon(int id, int icon, String iconName) {
        this.id = id;
        this.icon = icon;
        this.iconName = iconName;
    }

    public int getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }

    public String getIconName() {
        return iconName;
    }

}
