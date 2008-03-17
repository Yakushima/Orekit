package fr.cs.orekit.models.spacecraft;

import org.apache.commons.math.geometry.Vector3D;
import fr.cs.orekit.forces.perturbations.AtmosphericDrag;

/** Adapted container for the Atmosphere drag force model.
 *
 * @see AtmosphericDrag
 * @author F. Maussion
 */
public interface AtmosphereDragSpacecraft {

    /** Get the visible surface from a specific direction.
     * See {@link AtmosphericDrag} for more explanations.
     * @param direction direction of the flux in the spacecraft frame
     * @return surface (m<sup>2</sup>)
     */
    public double getSurface(Vector3D direction);

    /** Get the drag coefficients vector.
     * See {@link AtmosphericDrag} for more explanations.
     * @param direction direction of the flux in the spacecraft frame
     * @return drag coefficients vector (defined in the spacecraft frame)
     */
    public Vector3D getDragCoef(Vector3D direction);

}
