package won.bot.jokebot.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ms on 14.11.2019
 */
public class ChuckNorrisJoke {
    private static final String ID = "id";
    private static final String CATEGORIES = "categories";
    private static final String CREATED_AD = "created_at";
    private static final String ICON_URL = "icon_url";
    private static final String UPDATED_AD = "updated_at";
    private static final String URL = "url";
    private static final String VALUE = "value";
    @JsonProperty(ID)
    private String id;
    @JsonProperty(CATEGORIES)
    private List<String> categories;
    @JsonProperty(CREATED_AD)
    private String created_at;
    @JsonProperty(ICON_URL)
    private String icon_url;
    @JsonProperty(UPDATED_AD)
    private String updated_at;
    @JsonProperty(URL)
    private String url;
    @JsonProperty(VALUE)
    private String value;

    public ChuckNorrisJoke() {
    }

    @Override
    public String toString() {
        return "ChuckNorrisJoke [id= " + id + " categories=" + categories + ", created_at=" + created_at + ", icon_url="
                        + icon_url
                        + ", updated_at="
                        + updated_at + ", url=" + url + ", value=" + value + "]";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
