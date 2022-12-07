package waterbased.anticheat.checks;

public enum Check {

    PLAYER_NoFall("NoFall", "Taking no fall damage"),
    MOVEMENT_Flight("Flight", "Flying in survival/adventure mode."),
    MOVEMENT_ElytraFlight("ElytraFlight", "Gliding like wearing a elytra without wearing it."),
    MOVEMENT_FastLadder("FastLadder", "Climbing ladders faster then allowed."),
    MOVEMENT_Speed("Speed", "Move faster than allowed"),
    MOVEMENT_VehicleFlight("VehicleFlight", "Illegal movement in a vehicle"),
    WORLD_BlockBreak("BlockBreak", "Breaking blocks illegally");

    private final String name;
    private final String description;

    Check(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
