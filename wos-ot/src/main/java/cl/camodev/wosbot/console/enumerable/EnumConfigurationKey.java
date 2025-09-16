package cl.camodev.wosbot.console.enumerable;

import java.util.function.Consumer;
import static cl.camodev.wosbot.console.enumerable.EnumConfigCategory.*;

public enum EnumConfigurationKey {

    // Shop configurations
    BOOL_NOMADIC_MERCHANT(meta(m -> m
            .boolDefault(false)
            .category(SHOP)
            .title("Nomadic Merchant")
            .description("Exchange resources with nomadic merchant shop")
    )),
    BOOL_NOMADIC_MERCHANT_VIP_POINTS(meta(m -> m
            .boolDefault(false)
            .category(SHOP)
            .parent(BOOL_NOMADIC_MERCHANT)
            .description("Buy vip points using gems")
    )),
    BOOL_VIP_POINTS(meta(m -> m
            .boolDefault(false)
            .category(SHOP)
            .title("VIP Points")
            .description("Claim daily VIP points")
    )),
    VIP_BUY_MONTHLY(meta(m -> m
            .boolDefault(false)
            .category(SHOP)
            .parent(BOOL_VIP_POINTS)
            .title("Buy Monthly VIP")
            .description("Automatically buy monthly VIP if there are no days left")
    )),
    BANK_BOOL(meta(m -> m
            .boolDefault(false)
            .category(SHOP)
            .title("Bank")
            .description("Claim daily bank rewards")
    )),
    BANK_DELAY_INT(meta(m -> m
            .intDefault(1)
            .category(SHOP)
            .parent(BANK_BOOL)
            .title("Bank Delay")
            .validValues(new String[]{"1", "7", "15", "30"})
            .unit("days")
    )),
    BOOL_MYSTERY_SHOP(meta(m -> m
            .boolDefault(false)
            .category(SHOP)
            .title("Mystery Shop")
            .description("Claim daily mystery shop free rewards")
    )),
    BOOL_MYSTERY_SHOP_50D_GEAR(meta(m -> m
            .boolDefault(false)
            .category(SHOP)
            .parent(BOOL_MYSTERY_SHOP)
            .title("50% Hero widget")
            .description("Buy 50% hero promotion widget (250 coins each)")
    )),

    // ============== CITY EVENTS ==============
    BOOL_WAR_ACADEMY_SHARDS(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("War Academy Shards")
            .description("Claim daily War Academy shards using steel")
    )),
    BOOL_CRYSTAL_LAB_FC(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Crystal Lab FC")
            .description("Claim daily Fire Crystal Lab rewards")
    )),
    BOOL_EXPLORATION_CHEST(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Exploration Chest")
            .description("Claim exploration chest")
    )),
    INT_EXPLORATION_CHEST_OFFSET(meta(m -> m
            .intDefault(60)
            .category(CITY_EVENTS)
            .parent(BOOL_EXPLORATION_CHEST)
            .title("Offset Time")
            .description("How often to collect rewards (min)")
    )),
    BOOL_HERO_RECRUITMENT(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Hero Recruitment")
            .description("Open recruitment hero chests")
    )),
    LIFE_ESSENCE_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Life Essence")
            .description("Claim life essence")
    )),
    LIFE_ESSENCE_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(CITY_EVENTS)
            .parent(LIFE_ESSENCE_BOOL)
            .title("Offset Time")
            .description("How often to collect life essence (min)")
    )),
    MAIL_REWARDS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Mail Rewards")
            .description("Claim mail rewards")
    )),
    MAIL_REWARDS_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(CITY_EVENTS)
            .parent(MAIL_REWARDS_BOOL)
            .title("Offset Time")
            .description("How often to collect main (min)")
    )),
    DAILY_MISSION_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Daily Mission")
            .description("Claim daily missions")
    )),
    DAILY_MISSION_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(CITY_EVENTS)
            .parent(DAILY_MISSION_BOOL)
            .title("Offset Time")
            .description("How often to claim daily missions")
    )),
    DAILY_MISSION_AUTO_SCHEDULE_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .parent(DAILY_MISSION_BOOL)
            .title("Auto Schedule")
            .description("Automatically schedule daily mission claim")
    )),
    STOREHOUSE_CHEST_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Storehouse Chest")
            .description("Claim storehouse chests")
    )),
    STOREHOUSE_STAMINA_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Storehouse Stamina")
            .description("Collect storehouse stamina")
    )),
    DAILY_LABYRINTH_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Labyrinth")
            .description("Complete daily labyrinth")
    )),

    // ====== TRAINING ======
    TRAIN_INFANTRY_BOOL(meta(m -> m
            .boolDefault(false)
            .category(TRAINING)
            .title("Train Infantry")
            .description("Train Infantry")
    )),
    TRAIN_MARKSMAN_BOOL(meta(m -> m
            .boolDefault(false)
            .category(TRAINING)
            .title("Train Marksman")
            .description("Train Marksman")
    )),
    TRAIN_LANCER_BOOL(meta(m -> m
            .boolDefault(false)
            .category(TRAINING)
            .title("Train Lancer")
            .description("Train Lancer")
    )),
    TRAIN_PRIORITIZE_PROMOTION_BOOL(meta(m -> m
            .boolDefault(false)
            .category(TRAINING)
            .title("Prioritize Promotion")
            .description("Promote all troops before training new ones")
    )),
    BOOL_TRAINING_RESOURCES(meta(m -> m
            .boolDefault(false)
            .category(TRAINING)
            .title("Use Resources")
            .description("Use Resources to fill")
    )),

    // ====== CITY UPGRADES ======
    CITY_UPGRADE_FURNACE_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_UPGRADES)
            .title("Upgrade Furnace")
            .description("Upgrade Furnace")
    )),
    CITY_ACCEPT_NEW_SURVIVORS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_UPGRADES)
            .title("Accept New Survivors")
            .description("Accept new survivors")
    )),
    CITY_ACCEPT_NEW_SURVIVORS_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(CITY_UPGRADES)
            .parent(CITY_ACCEPT_NEW_SURVIVORS_BOOL)
            .title("Offset Time")
            .description("How often to check for new survivors (min)")
    )),

    // ====== ALLIANCE ======
    ALLIANCE_CHESTS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Alliance Chests")
            .description("Claim alliance Chests")
    )),
    ALLIANCE_CHESTS_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(ALLIANCE)
            .parent(ALLIANCE_CHESTS_BOOL)
            .title("Offset Time")
            .description("How often to collect alliance chests (min)")
    )),
    ALLIANCE_TECH_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Alliance Tech")
            .description("Donate to alliance tech")
    )),
    ALLIANCE_TECH_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(ALLIANCE)
            .parent(ALLIANCE_TECH_BOOL)
            .title("Offset Time")
            .description("How often to donate to alliance tech (min)")
    )),
    ALLIANCE_AUTOJOIN_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Auto Join")
            .description("Enable auto join")
    )),
    ALLIANCE_AUTOJOIN_QUEUES_INT(meta(m -> m
            .intDefault(1)
            .category(ALLIANCE)
            .parent(ALLIANCE_AUTOJOIN_BOOL)
            .title("Auto Join Queues")
            .description("Number of queues used for auto join")
    )),
    ALLIANCE_PET_TREASURE_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Pet Treasure")
            .description("Collect alliance pet treasure")
    )),
    ALLIANCE_HELP_REQUESTS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Help Requests")
            .description("Help your allies by clicking help button")
    )),
    ALLIANCE_HELP_REQUESTS_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(ALLIANCE)
            .parent(ALLIANCE_HELP_REQUESTS_BOOL)
            .title("Offset Time")
            .description("How often to help allies (min)")
    )),
    ALLIANCE_TRIUMPH_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Alliance Triumph")
            .description("Claim Alliance Triumph")
    )),
    ALLIANCE_TRIUMPH_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(ALLIANCE)
            .parent(ALLIANCE_TRIUMPH_BOOL)
            .title("Offset Time")
            .description("How often to collect alliance triumph (min)")
    )),
    ALLIANCE_LIFE_ESSENCE_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Life Essence")
            .description("Claim allies' life essence")
    )),
    ALLIANCE_LIFE_ESSENCE_OFFSET_INT(meta(m -> m
            .intDefault(60)
            .category(ALLIANCE)
            .parent(ALLIANCE_LIFE_ESSENCE_BOOL)
            .title("Offset Time")
            .description("How often to claim allies' life essence (min)")
    )),

    // ====== GATHERING ======
    GATHER_SPEED_BOOL(meta(m -> m
            .boolDefault(false)
            .category(GATHERING)
            .title("Gather Speed Boost (250 gems)")
            .description("Activate gathering speed boost")
    )),
    GATHER_COAL_BOOL(meta(m -> m
            .boolDefault(false)
            .category(GATHERING)
            .title("Gather Coal")
            .description("Enable coal gathering")
    )),
    GATHER_WOOD_BOOL(meta(m -> m
            .boolDefault(false)
            .category(GATHERING)
            .title("Gather Wood")
            .description("Enable wood gathering")
    )),
    GATHER_MEAT_BOOL(meta(m -> m
            .boolDefault(false)
            .category(GATHERING)
            .title("Gather Meat")
            .description("Enable meat gathering")
    )),
    GATHER_IRON_BOOL(meta(m -> m
            .boolDefault(false)
            .category(GATHERING)
            .title("Gather Iron")
            .description("Enable iron gathering")
    )),
    GATHER_COAL_LEVEL_INT(meta(m -> m
            .intDefault(1)
            .category(GATHERING)
            .parent(GATHER_COAL_BOOL)
            .title("Coal Tile Level")
            .description("Level of coal tiles to gather")
    )),
    GATHER_WOOD_LEVEL_INT(meta(m -> m
            .intDefault(1)
            .category(GATHERING)
            .parent(GATHER_WOOD_BOOL)
            .title("Wood Tile Level")
            .description("Level of wood tiles to gather")
    )),
    GATHER_MEAT_LEVEL_INT(meta(m -> m
            .intDefault(1)
            .category(GATHERING)
            .parent(GATHER_MEAT_BOOL)
            .title("Meat Tile Level")
            .description("Level of meat tiles to gather")
    )),
    GATHER_IRON_LEVEL_INT(meta(m -> m
            .intDefault(1)
            .category(GATHERING)
            .parent(GATHER_IRON_BOOL)
            .title("Iron Tile Level")
            .description("Level of iron tiles to gather")
    )),
    GATHER_ACTIVE_MARCH_QUEUE_INT(meta(m -> m
            .intDefault(6)
            .category(GATHERING)
            .title("Active March Queues")
            .description("How many gathering queues to use")
    )),
    GATHER_REMOVE_HEROS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(GATHERING)
            .title("Remove Heroes")
            .description("Remove heroes from gathering marches")
    )),

    // ====== INTEL ======
    INTEL_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .title("Enable Intel")
            .description("Enable completing Intel")
    )),
    INTEL_FIRE_BEAST_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .parent(INTEL_BOOL)
            .title("Fire Beast")
            .description("Hunt fire beasts")
    )),
    INTEL_BEASTS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .parent(INTEL_BOOL)
            .title("Beasts")
            .description("Hunt intel beasts")
    )),
    INTEL_CAMP_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .parent(INTEL_BOOL)
            .title("Survivors")
            .description("Rescue survivors")
    )),
    INTEL_EXPLORATION_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .parent(INTEL_BOOL)
            .title("A Hero's Journey")
            .description("Complete a hero's journey intel")
    )),
    INTEL_BEASTS_EVENT_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .parent(INTEL_BOOL)
            .title("Flames and Fangs")
            .description("Attaack Flames and Fangs beast")
    )),
    INTEL_FC_ERA_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .parent(INTEL_BOOL)
            .title("FC Era")
            .description("The account has FC era intel")
    )),

    INTEL_USE_FLAG_BOOL(meta(m -> m
            .boolDefault(false)
            .category(INTEL)
            .parent(INTEL_BOOL)
            .title("Use preset march")
            .description("Use preset march for hunting beasts on intel")
    )),
    INTEL_BEASTS_FLAG_INT(meta(m -> m
            .intDefault(1)
            .category(INTEL)
            .parent(INTEL_USE_FLAG_BOOL)
            .title("Preset march")
            .validValues(new String[]{"1", "2", "3", "4", "5", "6","7","8"})
            .description("Preset march to use for hunting beasts on intel (1-6)")
    )),

    // ====== PETS ======
    PET_SKILL_STAMINA_BOOL(meta(m -> m
            .boolDefault(false)
            .category(PETS)
            .title("Stamina Skill")
            .description("Claim pet stamina")
    )),
    PET_SKILL_FOOD_BOOL(meta(m -> m
            .boolDefault(false)
            .category(PETS)
            .title("Pet Food")
            .description("Claim pet food")
    )),
    PET_SKILL_TRESURE_BOOL(meta(m -> m
            .boolDefault(false)
            .category(PETS)
            .title("Pet Treasure")
            .description("Claim pet treasure")
    )),
    PET_SKILL_GATHERING_BOOL(meta(m -> m
            .boolDefault(false)
            .category(PETS)
            .title("Pet Gathering")
            .description("Use the pet gathering skill")
    )),
    PET_PERSONAL_TREASURE_BOOL(meta(m -> m
            .boolDefault(false)
            .category(PETS)
            .title("Personal Treasure")
            .description("Collect personal pet treasure")
    )),
    EXPERT_AGNES_INTEL_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EXPERTS)
            .title("Expert Agnes Intel")
            .description("Use Expert Agnes to get more intel")
    )),
    EXPERT_ROMULUS_TAG_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EXPERTS)
            .title("Expert Romulus Tag")
            .description("Use Expert Romulus to get tags")
    )),
    EXPERT_ROMULUS_TROOPS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EXPERTS)
            .title("Expert Romulus Troops")
            .description("Use Expert Romulus to get troops")
    )),
    EXPERT_ROMULUS_TROOPS_TYPE_STRING(meta(m -> m
            .stringDefault("Infantry")
            .category(EXPERTS)
            .parent(EXPERT_ROMULUS_TROOPS_BOOL)
            .title("Troop Type")
            .validValues(new String[]{"Infantry", "Marksman", "Lancer"})
            .description("Type of troops to get (Infantry, Marksman, Lancer)")
    )),
    TUNDRA_TREK_SUPPLIES_BOOL(meta(m -> m
            .boolDefault(false)
            .category(CITY_EVENTS)
            .title("Tundra Trek Supplies")
            .description("Use Tundra Trek to get supplies")
    )),
    TUNDRA_TRUCK_EVENT_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EVENTS)
            .title("Tundra Truck Event")
            .description("Sent trucks on tundra truck event")
    )),
    TUNDRA_TRUCK_USE_GEMS_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EVENTS)
            .parent(TUNDRA_TRUCK_EVENT_BOOL)
            .title("Use Gems")
            .description("Use gems to instantly send trucks")
    )),
    TUNDRA_TRUCK_SSR_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EVENTS)
            .parent(TUNDRA_TRUCK_EVENT_BOOL)
            .title("Only SSR")
            .description("Only send trucks when you have SSR rewards")
    )),
    MERCENARY_EVENT_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EVENTS)
            .title("Mercenary Event")
            .description("Complete mercenary event")
    )),
    HERO_MISSION_EVENT_BOOL(meta(m -> m
            .boolDefault(false)
            .category(EVENTS)
            .title("Hero Mission Event")
            .description("Complete hero mission event")
    )),
    ALLIANCE_HONOR_CHEST_BOOL(meta(m -> m
            .boolDefault(false)
            .category(ALLIANCE)
            .title("Alliance Honor Chest")
            .description("Claim alliance honor chest")
    ))
    ;



    private final ConfigMeta meta;

    EnumConfigurationKey(ConfigMeta meta) { this.meta = meta; }

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
    public EnumConfigurationKey getParent()       { return meta.parent(); }
    public boolean hasParent()                 { return meta.parent() != null; }
    public String[] getValidValues()       { return meta.validValues(); }
    public boolean isRoot()                    { return meta.parent() == null; }


    public <T> T parseValue(String value, Class<T> clazz) {
        if (clazz == Boolean.class) {
            return clazz.cast(Boolean.parseBoolean(value));
        } else if (clazz == Integer.class) {
            return clazz.cast(Integer.parseInt(value));
        } else if (clazz == Long.class) {
            return clazz.cast(Long.parseLong(value));
        } else if (clazz == String.class) {
            return clazz.cast(value);
        }
        throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
    }
}
