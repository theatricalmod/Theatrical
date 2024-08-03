package dev.imabad.theatrical.config;

import java.util.Objects;

public final class UniverseConfig {

    public int subnet;
    public int universe;
    public boolean enabled;

    public UniverseConfig(){}

    public UniverseConfig(int subnet, int universe, boolean enabled) {
        this.subnet = subnet;
        this.universe = universe;
        this.enabled = enabled;
    }
    public int getSubnet() {
        return subnet;
    }

    public int getUniverse() {
        return universe;
    }

    public void setSubnet(int subnet) {
        this.subnet = subnet;
    }

    public void setUniverse(int universe) {
        this.universe = universe;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UniverseConfig) obj;
        return this.subnet == that.subnet &&
                this.universe == that.universe;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subnet, universe);
    }

    @Override
    public String toString() {
        return "UniverseConfig[" +
                "subnet=" + subnet + ", " +
                "universe=" + universe + ']';
    }

}
