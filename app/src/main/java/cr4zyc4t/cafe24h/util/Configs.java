package cr4zyc4t.cafe24h.util;

/**
 * Created by cr4zyc4t on 5/4/15.
 */
public class Configs {
    public static final String GET_CATEGORY_URL = "http://content.amobi.vn/api/cafe24h/listcategory";
    public static final String GET_SUBCATEGORY_URL = "http://content.amobi.vn/api/cafe24h/listsubcategory";
    public static final String GET_CONTENT_BY_CATEGORY_URL = "http://content.amobi.vn/api/cafe24h/getcontentbycategory";
    public static final String GET_CONTENT_BY_SUBCATEGORY_URL = "http://content.amobi.vn/api/cafe24h/getcontentbysubcategory";
    public static final String GET_CONTENT_BY_SOURCE_URL = "http://content.amobi.vn/api/cafe24h/getcontentbysource";

    public static final int SPLASH_TIME = 1000;

    public static final int NEWS_PER_LOAD = 10;

    public static final int LARGE_SCREEN_DP = 650;

    public static final int CATEGORY_TYPE = 0;
    public static final int SUBCATEGORY_TYPE = 1;
    public static final int SOURCE_TYPE = 2;

    public static String getContentURL(int type, int target_id, int limit, int offset) {
        switch (type) {
            case CATEGORY_TYPE:
                return GET_CONTENT_BY_CATEGORY_URL + "?category_id=" + target_id + "&limit=" + limit + "&offset=" + offset;
            case SUBCATEGORY_TYPE:
                return GET_CONTENT_BY_SUBCATEGORY_URL + "?subcategory_id=" + target_id + "&limit=" + limit + "&offset" + offset;
            case SOURCE_TYPE:
                return GET_CONTENT_BY_SOURCE_URL + "?source_id=" + target_id + "&limit=" + limit + "&offset" + offset;
        }
        return null;
    }
}
