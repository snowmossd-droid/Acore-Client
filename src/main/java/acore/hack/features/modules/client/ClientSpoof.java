package acore.hack.features.modules.client;

import acore.hack.features.modules.Module;

public class ClientSpoof extends Module {

    public ClientSpoof() {
        super("ClientSpoof", Category.CLIENT);
    }

    public Mode mode = Mode.Vanilla;
    public String custom = "feather";

    public enum Mode {
        Vanilla, Lunar1_20_4, Lunar1_20_1, Custom, Null
    }

    public String getClientName() {
        switch (mode) {
            case Vanilla:
                return "vanilla";
            case Lunar1_20_4:
                return "lunarclient:1.20.4";
            case Lunar1_20_1:
                return "lunarclient:1.20.1";
            case Custom:
                return custom;
            default:
                return null;
        }
    }
}
