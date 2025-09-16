package cl.camodev.wosbot.console.enumerable;

public enum EnumConfigCategory {

    // @formatter:off

    CITY_UPGRADES(  "City Upgrades",    "Building",         "City related settings"),
    CITY_EVENTS(    "City Events",      "Star",        "City event related settings"),
    SHOP(           "Shop",             "ShoppingCart",         "In-game shop related settings"),
    GATHERING(      "Gathering",        "Package",    "Resource gathering related settings"),
    INTEL(          "Intel",            "Eye",        "Intel mission related settings"),
    ALLIANCE(       "Alliance",         "Users",     "Alliance related settings"),
    TRAINING(       "Training",         "Target",     "Troop training related settings"),
    PETS(           "Pets ",            "Bone",         "Pet related settings"),
    EVENTS(        "Events",           "Calendar",         "Special event related settings"),
    EXPERTS(        "Experts",          "UserTie",         "Expert related settings"),;



    private String name;
    private String icon;
    private String description;
    private EnumConfigCategory(String name, String icon, String description) {
        this.name = name;
        this.icon = icon;
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public String getIcon() {
        return icon;
    }
    public String getDescription() {
        return description;
    }
    public String getCode() {
        return this.name();
    }
    public Long getId() {
        return (long) (ordinal() + 1);
    }
}

