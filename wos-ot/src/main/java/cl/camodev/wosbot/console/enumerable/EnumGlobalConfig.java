package cl.camodev.wosbot.console.enumerable;

import java.util.function.Consumer;

public enum EnumGlobalConfig {
    //@formatter:off
    BOOL_DEBUG(meta(m -> m
            .boolDefault(false)
    )),
    DISCORD_TOKEN_STRING(meta(m -> m
            .stringDefault("")
    )),
    MUMU_PATH_STRING(meta(m -> m
            .stringDefault("")
    )),
    MEMU_PATH_STRING(meta(m -> m
            .stringDefault("")
    )),
    LDPLAYER_PATH_STRING(meta(m -> m
            .stringDefault("")
    )),
    CURRENT_EMULATOR_STRING(meta(m -> m
            .stringDefault("")
    )),

    MAX_RUNNING_EMULATORS_INT(meta(m -> m
            .intDefault(1)
    )),
    MAX_IDLE_TIME_INT(meta(m -> m
            .intDefault(15)
    )),
    GAME_VERSION_STRING(meta(m -> m
            .stringDefault("GLOBAL")
    ));
    //@formatter:on
    private final ConfigMeta meta;

    EnumGlobalConfig(ConfigMeta meta) { this.meta = meta; }

    private static ConfigMeta meta(Consumer<ConfigMeta.Builder> c) {
        ConfigMeta.Builder b = ConfigMeta.builder();
        c.accept(b);
        return b.build();
    }

    public String getDescription()             { return meta.description(); }
    public String getDefaultValue()            { return meta.defaultValue(); }
    public String getTitle()                  { return meta.title(); }
    public String getIcon()                    { return meta.icon(); }
    public String getUnit()                    { return meta.unit(); }
    public Class<?> getType()                  { return meta.type(); }
    public EnumConfigCategory getCategory()    { return meta.category(); }
    public boolean hasParent()                 { return meta.parent() != null; }
    public String[] getValidValues()       { return meta.validValues(); }
    public boolean isRoot()                    { return meta.parent() == null; }


}
