package dev.imabad.theatrical.client.blockentities.state;

import dev.imabad.theatrical.api.Fixture;
import net.minecraft.resources.Identifier;

public class FixtureRenderState {

    public Identifier tiltModel;
    public Identifier staticModel;
    public Identifier panModel;
    public float[] tiltRotationPosition;
    public float[] panRotationPosition;
    public float[] beamStartPosition;
    public float beamWidth;
    public boolean hasBeam;


    public void extractFromFixture(Fixture fixture) {
        tiltModel =  fixture.getTiltModel();
        staticModel = fixture.getStaticModel();
        panModel =  fixture.getPanModel();
        tiltRotationPosition = fixture.getTiltRotationPosition();
        panRotationPosition = fixture.getPanRotationPosition();
        beamStartPosition = fixture.getBeamStartPosition();
        beamWidth = fixture.getBeamWidth();
        hasBeam = fixture.hasBeam();
    }
}
