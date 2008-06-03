package fr.cs.orekit.maneuvers;

import java.text.ParseException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.GraggBulirschStoerIntegrator;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.util.MathUtils;

import fr.cs.orekit.errors.OrekitException;
import fr.cs.orekit.frames.Frame;
import fr.cs.orekit.iers.IERSDirectoryCrawler;
import fr.cs.orekit.orbits.CircularOrbit;
import fr.cs.orekit.orbits.KeplerianOrbit;
import fr.cs.orekit.orbits.Orbit;
import fr.cs.orekit.propagation.SpacecraftState;
import fr.cs.orekit.propagation.numerical.NumericalModel;
import fr.cs.orekit.propagation.numerical.OrekitSwitchingFunction;
import fr.cs.orekit.propagation.numerical.forces.maneuvers.ConstantThrustManeuver;
import fr.cs.orekit.time.AbsoluteDate;
import fr.cs.orekit.time.ChunkedDate;
import fr.cs.orekit.time.ChunkedTime;
import fr.cs.orekit.time.UTCScale;
import fr.cs.orekit.utils.PVCoordinates;


public class ConstantThrustManeuverTest extends TestCase {

 // Body mu 
    private double mu;

    private CircularOrbit dummyOrbit(AbsoluteDate date)
    {
        return new CircularOrbit(new PVCoordinates(Vector3D.plusI, Vector3D.plusJ),
                             Frame.getJ2000(), date, mu);
    }

    public void testBadFrame() {
        try {
            new ConstantThrustManeuver(new AbsoluteDate(new ChunkedDate(2004, 01, 01),
                                                        new ChunkedTime(23, 30, 00.000),
                                                        UTCScale.getInstance()),
                                       10.0, 400.0, 300.0, Vector3D.plusK, Integer.MAX_VALUE);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            // expected behavior
        } catch (OrekitException e) {
            fail("wrong exception caught");
        }
    }

    public void testPositiveDuration() throws OrekitException {
        AbsoluteDate date = new AbsoluteDate(new ChunkedDate(2004, 01, 01),
                                             new ChunkedTime(23, 30, 00.000),
                                             UTCScale.getInstance());
        ConstantThrustManeuver maneuver =
            new ConstantThrustManeuver(date, 10.0, 400.0, 300.0, Vector3D.plusK,
                                       ConstantThrustManeuver.INERTIAL);
        OrekitSwitchingFunction[] switches = maneuver.getSwitchingFunctions();

        Orbit o1 = dummyOrbit(new AbsoluteDate(date, - 1.0));
        assertTrue(switches[0].g(new SpacecraftState(o1)) > 0);
        Orbit o2 = dummyOrbit(new AbsoluteDate(date,   1.0));
        assertTrue(switches[0].g(new SpacecraftState(o2)) < 0);
        Orbit o3 = dummyOrbit(new AbsoluteDate(date,   9.0));
        assertTrue(switches[1].g(new SpacecraftState(o3)) > 0);
        Orbit o4 = dummyOrbit(new AbsoluteDate(date,  11.0));
        assertTrue(switches[1].g(new SpacecraftState(o4)) < 0);
    }
    
    public void testNegativeDuration() throws OrekitException {
        AbsoluteDate date = new AbsoluteDate(new ChunkedDate(2004, 01, 01),
                                             new ChunkedTime(23, 30, 00.000),
                                             UTCScale.getInstance());
        ConstantThrustManeuver maneuver =
            new ConstantThrustManeuver(date, -10.0, 400.0, 300.0, Vector3D.plusK,
                                       ConstantThrustManeuver.INERTIAL);
        OrekitSwitchingFunction[] switches = maneuver.getSwitchingFunctions();

        Orbit o1 = dummyOrbit(new AbsoluteDate(date, -11.0));
        assertTrue(switches[0].g(new SpacecraftState(o1)) > 0);
        Orbit o2 = dummyOrbit(new AbsoluteDate(date,  -9.0));
        assertTrue(switches[0].g(new SpacecraftState(o2)) < 0);
        Orbit o3 = dummyOrbit(new AbsoluteDate(date,  -1.0));
        assertTrue(switches[1].g(new SpacecraftState(o3)) > 0);
        Orbit o4 = dummyOrbit(new AbsoluteDate(date,   1.0));
        assertTrue(switches[1].g(new SpacecraftState(o4)) < 0);
    }

    public void testRoughBehaviour() throws DerivativeException, IntegratorException, OrekitException, ParseException {
        final double isp = 318;
        final double mass = 2500;
        final double a = 24396159;
        final double e = 0.72831215;
        final double i = Math.toRadians(7);
        final double omega = Math.toRadians(180);
        final double OMEGA = Math.toRadians(261);
        final double lv = 0;

        final double duration = 3653.99;
        final double f = 420;
        final double delta = Math.toRadians(-7.4978);
        final double alpha = Math.toRadians(351);

        final Vector3D dir = new Vector3D (Math.cos(alpha) * Math.cos(delta),
                                           Math.cos(alpha) * Math.sin(delta),
                                           Math.sin(delta));

        final AbsoluteDate initDate = new AbsoluteDate(new ChunkedDate(2004, 01, 01),
                                                       new ChunkedTime(23, 30, 00.000),
                                                       UTCScale.getInstance());
        final AbsoluteDate fireDate = new AbsoluteDate(new ChunkedDate(2004, 01, 02),
                                                       new ChunkedTime(04, 15, 34.080),
                                                       UTCScale.getInstance());

        final Orbit transPar = new KeplerianOrbit(a, e, i, omega, OMEGA,
                                                  lv, KeplerianOrbit.TRUE_ANOMALY, 
                                                  Frame.getJ2000(), initDate, mu);

        final SpacecraftState transOrb = new SpacecraftState(transPar, mass);

        final NumericalModel propagator =
            new NumericalModel(mu, new GraggBulirschStoerIntegrator(1e-50, 1000, 0, 1e-08));
        propagator.addForceModel(new ConstantThrustManeuver(fireDate, duration, f, isp, dir,
                                                            ConstantThrustManeuver.INERTIAL));

        final SpacecraftState finalorb =
            propagator.propagate(transOrb, new AbsoluteDate(fireDate, 3800));

        assertEquals(2007.88245442614, finalorb.getMass(), 1e-10);
        assertEquals(2.6792, Math.toDegrees(MathUtils.normalizeAngle(finalorb.getI(), Math.PI)), 1e-4);
        assertEquals(28969, finalorb.getA()/1000, 1);

    }

    public void setUp() {
        System.setProperty(IERSDirectoryCrawler.IERS_ROOT_DIRECTORY, "regular-data");

        // Body mu
        mu = 3.9860047e14;
        
    }

    public static Test suite() {
        return new TestSuite(ConstantThrustManeuverTest.class);
    }

}
