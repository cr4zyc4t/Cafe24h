package cr4zyc4t.cafe24h.model;

import java.io.Serializable;

/**
 * Created by Admin on 2015-04-24.
 */
public class News implements Serializable {
    private String title, icon, time, description, source_icon;
    private int id;

    public News(String title, String icon, String source_icon, String time, String description, int id) {
        this.title = title;
        this.icon = icon;
        this.source_icon = source_icon;
        this.time = time;
        this.description = description;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public String getSource_icon() {
        return source_icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}