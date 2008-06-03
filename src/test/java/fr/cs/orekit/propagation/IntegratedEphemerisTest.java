package fr.cs.orekit.propagation;

import java.io.FileNotFoundException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.GraggBulirschStoerIntegrator;

import fr.cs.orekit.errors.OrekitException;
import fr.cs.orekit.frames.Frame;
import fr.cs.orekit.orbits.EquinoctialOrbit;
import fr.cs.orekit.propagation.analytical.KeplerianPropagator;
import fr.cs.orekit.propagation.numerical.IntegratedEphemeris;
import fr.cs.orekit.propagation.numerical.NumericalModel;
import fr.cs.orekit.time.AbsoluteDate;
import fr.cs.orekit.utils.PVCoordinates;

public class IntegratedEphemerisTest extends TestCase {

    public void testNormalKeplerIntegration() throws OrekitException, FileNotFoundException {

        // Definition of initial conditions with position and velocity

        Vector3D position = new Vector3D(7.0e6, 1.0e6, 4.0e6);
        Vector3D velocity = new Vector3D(-500.0, 8000.0, 1000.0);
        double mu = 3.9860047e14;

        AbsoluteDate initDate = new AbsoluteDate(AbsoluteDate.J2000_EPOCH, 584.);
        SpacecraftState initialOrbit =
            new SpacecraftState(new EquinoctialOrbit(new PVCoordinates(position, velocity),
                                                     Frame.getJ2000(), initDate, mu));

        // Keplerian propagator definition

        KeplerianPropagator keplerEx = new KeplerianPropagator(initialOrbit);

        // Numerical propagator definition

        FirstOrderIntegrator integrator = new GraggBulirschStoerIntegrator(1, 86400, 0, 10e-13);
        NumericalModel numericEx = new NumericalModel(mu, integrator);

        // Integrated ephemeris

        IntegratedEphemeris ephemeris = new IntegratedEphemeris();

        // Propagation

        AbsoluteDate finalDate = new AbsoluteDate(initDate , 86400);
        numericEx.propagate(initialOrbit , finalDate , ephemeris );
        SpacecraftState keplerIntermediateOrbit;
        SpacecraftState numericIntermediateOrbit;
        AbsoluteDate intermediateDate;

        // tests

        for (int i = 1; i<=86400; i++) {
            intermediateDate = new AbsoluteDate(initDate , i);
            keplerIntermediateOrbit = keplerEx.getSpacecraftState(intermediateDate);
            numericIntermediateOrbit = ephemeris.getSpacecraftState(intermediateDate);

            Vector3D test = keplerIntermediateOrbit.getPVCoordinates().getPosition().subtract(numericIntermediateOrbit.getPVCoordinates().getPosition());
            assertEquals(0, test.getNorm(), 10e-2);
        }

        // test inv
        intermediateDate = new AbsoluteDate(initDate , 41589);
        keplerIntermediateOrbit = keplerEx.getSpacecraftState(intermediateDate);
        initialOrbit = keplerEx.getSpacecraftState(finalDate);
        numericEx.propagate(initialOrbit , initDate , ephemeris );
        numericIntermediateOrbit = ephemeris.getSpacecraftState(intermediateDate);

        Vector3D test = keplerIntermediateOrbit.getPVCoordinates().getPosition().subtract(numericIntermediateOrbit.getPVCoordinates().getPosition());
        assertEquals(0, test.getNorm(), 10e-2);

    }

    public static Test suite() {
        return new TestSuite(IntegratedEphemerisTest.class);
    }
}
