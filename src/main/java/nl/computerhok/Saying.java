package nl.computerhok;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import jersey.repackaged.com.google.common.base.Objects;
import org.hibernate.validator.constraints.Length;

public class Saying {
    private long id;

    @Length(max = 3)
    private String content;

    public Saying() {
        // Jackson deserialization
    }

    public Saying(long id, String content) {
        this.id = id;
        this.content = content;
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    public void setId(long id) {this.id = id; }

    @JsonProperty
    public String getContent() {
        return content;
    }

    public void SetContent(String content) { this.content = content;}

    @Override
    public String toString() {
        return "Saying{" + "id=" + id + ", content='" + content + '\'' + '}';
    }
}