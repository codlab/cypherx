package greendao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table CONFIG.
 */
public class Config {

    private Long id;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String content;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Config() {
    }

    public Config(Long id) {
        this.id = id;
    }

    public Config(Long id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    /** Not-null value. */
    public String getContent() {
        return content;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setContent(String content) {
        this.content = content;
    }

    // KEEP METHODS - put your custom methods here
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        } else {
            if (object instanceof Config) {
                return getName() != null && getName().equals(((Config) object).getName());
            } else if (object instanceof String) {
                return getName() != null && getName().equals((String) object);
            }
        }
        return false;
    }

    public boolean isContentSet() {
        return getContent() != null && getContent().length() > 0;
    }
    // KEEP METHODS END

}
