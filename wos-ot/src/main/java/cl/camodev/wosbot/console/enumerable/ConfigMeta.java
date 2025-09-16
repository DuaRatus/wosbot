package cl.camodev.wosbot.console.enumerable;

final class ConfigMeta {
    private final String defaultValue;
    private final Class<?> type;
    private final EnumConfigCategory category;
    private final EnumConfigurationKey parent;
    private final String description;
    private final String icon;
    private final String title;
    private final String unit;
    private final String[] validValues;

    private ConfigMeta(Builder b) {
        this.defaultValue = b.defaultValue;
        this.type = b.type;
        this.category = b.category;
        this.parent = b.parent;
        this.description = b.description;
        this.icon = b.icon;
        this.title = b.title;
        this.unit = b.unit;
        this.validValues = b.validValues;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String defaultValue() {
        return defaultValue;
    }

    public Class<?> type() {
        return type;
    }

    public EnumConfigCategory category() {
        return category;
    }

    public EnumConfigurationKey parent() {
        return parent;
    }

    public String description() {
        return description;
    }

    public String icon() {
        return icon;
    }

    public String title() {
        return title;
    }

    public String unit() {
        return unit;
    }

    public String[] validValues() {
        return validValues;
    }

    static final class Builder {
        private String defaultValue;
        private Class<?> type;
        private EnumConfigCategory category;
        private EnumConfigurationKey parent;
        private String description;
        private String icon;
        private String title;
        private String unit;
        private String[] validValues;

        // Setters tipados que fijan type y default a la vez
        public Builder boolDefault(boolean v) {
            this.type = Boolean.class;
            this.defaultValue = Boolean.toString(v);
            return this;
        }

        public Builder intDefault(int v) {
            this.type = Integer.class;
            this.defaultValue = Integer.toString(v);
            return this;
        }

        public Builder stringDefault(String v) {
            this.type = String.class;
            this.defaultValue = v;
            return this;
        }

        public Builder category(EnumConfigCategory c) {
            this.category = c;
            return this;
        }

        public Builder parent(EnumConfigurationKey p) {
            this.parent = p;
            return this;
        }

        public Builder description(String d) {
            this.description = d;
            return this;
        }

        public Builder icon(String i) {
            this.icon = i;
            return this;
        }

        public Builder title(String t) {
            this.title = t;
            return this;
        }

        public Builder unit(String u) {
            this.unit = u;
            return this;
        }

        public Builder validValues(String[] v) {
            this.validValues = v;
            return this;
        }

        public ConfigMeta build() {
            if (type == null) throw new IllegalStateException("type is required");
            if (defaultValue == null) defaultValue = "";
            if (description == null) description = "";
            if (icon == null) icon = "";
            if (title == null) title = "";
            if (unit == null) unit = "";
            return new ConfigMeta(this);
        }
    }
}