package net.dongliu.apk.parser.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Android intent filter.
 *
 * @author Dong Liu
 */
public class IntentFilter {

    /**
     * action this filter received
     */
    private List<String> actions = new ArrayList<String>(1);
    /**
     * category this filter matched
     */
    private List<String> categories = new ArrayList<String>(1);

    private List<IntentData> dataList = new ArrayList<IntentData>(1);

    /**
     * the activity / service / receiver hold this intent filter
     */
    private AndroidComponent owner;

    public List<String> getActions() {
        return actions;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<IntentData> getDataList() {
        return dataList;
    }

    public void addAction(String action) {
        this.actions.add(action);
    }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    public void addData(IntentData data) {
        this.dataList.add(data);
    }

    public AndroidComponent getOwner() {
        return owner;
    }

    public void setOwner(AndroidComponent owner) {
        this.owner = owner;
    }

    /**
     * intent data, scheme and mime type
     */
    public static class IntentData {
        private String scheme;
        private String mimeType;
        private String host;
        private String pathPrefix;
        private String type;

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getHost() {
            return host;
        }

        public void setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }

        public String getPathPrefix() {
            return pathPrefix;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "IntentData{" +
                    "scheme='" + scheme + '\'' +
                    ", mimeType='" + mimeType + '\'' +
                    ", host='" + host + '\'' +
                    ", pathPrefix='" + pathPrefix + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "IntentFilter{" +
                "actions=" + actions +
                ", categories=" + categories +
                ", dataList=" + dataList +
                ", owner=" + owner +
                '}';
    }
}
