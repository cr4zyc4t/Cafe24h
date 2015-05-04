package cr4zyc4t.cafe24h.model;

/**
 * Created by cr4zyc4t on 5/4/15.
 */
public class Category {
    public int id, order, styleColor;
    public String title, icon;

    public Category(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getStyleColor() {
        return styleColor;
    }

    public void setStyleColor(int styleColor) {
        this.styleColor = styleColor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
