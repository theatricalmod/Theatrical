package dev.imabad.theatrical.api;

public interface FixtureProvider {
    float getIntensity();

    float getMaxLightDistance();

    boolean shouldTrace();

    boolean emitsLight();

    boolean isUpsideDown();

    Fixture getFixture();
}
