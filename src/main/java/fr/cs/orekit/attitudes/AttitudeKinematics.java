package fr.cs.orekit.attitudes;

import java.io.Serializable;

import org.apache.commons.math.geometry.Rotation;
import org.apache.commons.math.geometry.Vector3D;

import fr.cs.orekit.frames.Transform;
import fr.cs.orekit.utils.PVCoordinates;

/** This class is a container for attitude representation.
 *
 * <p> It contains the attitude quaternion (used internally but represented by the
 * {@link Rotation} object) and the instant rotation axis (first time derivative).
 *  at a given time. <p>
 *  <p> This object is guaranted to be immutable. </p>
 * @see AttitudeKinematicsProvider
 * @author F. Maussion
 */
public class AttitudeKinematics implements Serializable {

    /** Simple constructor for identity attitude.  */
    public AttitudeKinematics() {
        this.attitude = new Rotation();
        this.spin = new Vector3D();
    }

    /** Simple constructor.
     * @param attitude the attitude rotation
     * @param spinAxis the spin
     */
    public AttitudeKinematics(Rotation attitude, Vector3D spinAxis) {
        this.attitude = attitude;
        this.spin = spinAxis;
    }

    /** Get the attitude rotation.
     * <p> The {@link Rotation} returned by this method represents the rotation
     * to apply to a vector expressed in the inertial frame to obtain the same vector
     * defined in the spacecraft frame </p>
     * @return the attitude rotation of the spacecraft
     */
    public Rotation getAttitude() {
        return attitude;
    }

    /** Get the attitute rotation derivative.
     * <p> The {@link Vector3D} returned by this method is the rotation axis
     * in the spacecraft frame. Its norm represents the spin. </p>
     * @return the instant rotation of the spacecraft
     */
    public Vector3D getspinAxis() {
        return spin;
    }

    /** Get the attitude kinematics transform.
     * @return the transform, which can be applied on {@link PVCoordinates}
     */
    public Transform getAkTransform() {
        return new Transform(attitude, spin);
    }

    /** Attitude quaternion */
    private final Rotation attitude;

    /** Spin */
    private final Vector3D spin;

    private static final long serialVersionUID = -5881827398498909034L;

}
