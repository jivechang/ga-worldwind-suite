package au.gov.ga.worldwind.common.view.stereo;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

import java.awt.Rectangle;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.view.stereo.StereoView.Eye;
import au.gov.ga.worldwind.common.view.transform.TransformView;

/**
 * Helper with common functionality for all {@link StereoView} implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StereoViewHelper
{
	private Eye eye = Eye.LEFT;
	private boolean stereo = false;

	private StereoViewParameters parameters = new BasicStereoViewParameters();

	private double lastFocalLength = 1;
	private double lastEyeSeparation = 0;

	/**
	 * @see StereoView#setup(boolean, Eye)
	 */
	public void setup(boolean stereo, Eye eye)
	{
		//don't allow null eye
		if (eye == null)
		{
			eye = Eye.LEFT;
		}

		this.stereo = stereo;
		this.eye = eye;
	}

	/**
	 * @see StereoView#getEye()
	 */
	public Eye getEye()
	{
		return eye;
	}

	/**
	 * @see StereoView#isStereo()
	 */
	public boolean isStereo()
	{
		return stereo;
	}

	/**
	 * @see StereoView#getParameters()
	 */
	public StereoViewParameters getParameters()
	{
		return parameters;
	}

	/**
	 * @see StereoView#setParameters(StereoViewParameters)
	 */
	public void setParameters(StereoViewParameters parameters)
	{
		this.parameters = parameters;
	}

	/**
	 * @see StereoView#getCurrentFocalLength()
	 */
	public double getCurrentFocalLength()
	{
		if (parameters.isDynamicStereo())
		{
			return lastFocalLength;
		}
		return parameters.getFocalLength();
	}

	/**
	 * @see StereoView#getCurrentEyeSeparation()
	 */
	public double getCurrentEyeSeparation()
	{
		if (parameters.isDynamicStereo())
		{
			return lastEyeSeparation;
		}
		return parameters.getEyeSeparation();
	}

	/**
	 * @see StereoView#calculateProjectionMatrix(double, double)
	 */
	public Matrix calculateProjectionMatrix(View view, double nearDistance, double farDistance)
	{
		if (stereo)
		{
			return calculateStereoProjectionMatrix(view, nearDistance, farDistance, eye, lastFocalLength,
					lastEyeSeparation);
		}
		else
		{
			return calculateMonoProjectionMatrix(view, nearDistance, farDistance);
		}
	}

	/**
	 * Calculate the focal length and eye separation for the current view. These
	 * parameters are calculated on the fly if dynamic stereo is enabled.
	 * 
	 * @param view
	 */
	protected void calculateStereoParameters(View view)
	{
		if (parameters.isDynamicStereo())
		{
			AsymmetricFrutumParameters afp = calculateStereoParameters(view, parameters.getEyeSeparationMultiplier());
			this.lastFocalLength = afp.focalLength;
			this.lastEyeSeparation = afp.eyeSeparation;
		}
		else
		{
			this.lastFocalLength = parameters.getFocalLength();
			this.lastEyeSeparation = parameters.getEyeSeparation();
		}
	}

	/**
	 * @see TransformView#beforeComputeMatrices()
	 */
	public void beforeComputeMatrices(View view)
	{
		calculateStereoParameters(view);
	}

	/**
	 * If stereo is enabled, translate the given matrix to the left or right
	 * according to which eye is currently being rendered.
	 * 
	 * @param matrix
	 *            Matrix to transform
	 * @return Transformed matrix
	 */
	public Matrix transformModelView(Matrix matrix)
	{
		if (matrix != null && stereo)
		{
			matrix =
					Matrix.fromTranslation((eye == Eye.RIGHT ? 1 : -1) * lastEyeSeparation * 0.5, 0, 0)
							.multiply(matrix);
		}
		return matrix;
	}

	/**
	 * @see TransformView#computeProjection()
	 */
	public Matrix computeProjection(View view)
	{
		return calculateProjectionMatrix(view, view.getNearClipDistance(), view.getFarClipDistance());
	}

	/**
	 * Calculate the projection matrix for the given view, taking into account
	 * the current stereo eye being rendered. Off-axis frustums are used; see
	 * http://paulbourke.net/miscellaneous/stereographics/stereorender/.
	 * 
	 * @param view
	 * @param nearDistance
	 * @param farDistance
	 * @param eye
	 * @param focalLength
	 * @param eyeSeparation
	 * @return Stereo projection matrix
	 */
	public static Matrix calculateStereoProjectionMatrix(View view, double nearDistance, double farDistance, Eye eye,
			double focalLength, double eyeSeparation)
	{
		Rectangle viewport = view.getViewport();
		double viewportWidth = viewport.getWidth() <= 0d ? 1d : viewport.getWidth();
		double viewportHeight = viewport.getHeight() <= 0d ? 1d : viewport.getHeight();
		double aspectratio = viewportWidth / viewportHeight;
		double vfov = view.getFieldOfView().radians / aspectratio;
		double widthdiv2 = nearDistance * Math.tan(vfov / 2.0);
		double multiplier = eye == Eye.RIGHT ? 1d : -1d;
		double distance = multiplier * 0.5 * eyeSeparation * nearDistance / focalLength;

		double top = widthdiv2;
		double bottom = -widthdiv2;
		double left = -aspectratio * widthdiv2 + distance;
		double right = aspectratio * widthdiv2 + distance;

		return fromFrustum(left, right, bottom, top, nearDistance, farDistance);
	}

	/**
	 * Calculate a standard non-stereo projection matrix.
	 * 
	 * @param view
	 * @param nearDistance
	 * @param farDistance
	 * @return Perspective projection matrix
	 */
	public static Matrix calculateMonoProjectionMatrix(View view, double nearDistance, double farDistance)
	{
		Rectangle viewport = view.getViewport();
		double viewportWidth = viewport.getWidth() <= 0d ? 1d : viewport.getWidth();
		double viewportHeight = viewport.getHeight() <= 0d ? 1d : viewport.getHeight();
		return Matrix.fromPerspective(view.getFieldOfView(), viewportWidth, viewportHeight, nearDistance, farDistance);
	}

	/**
	 * Calculate a projection matrix from the given frustum parameters.
	 * 
	 * @param left
	 * @param right
	 * @param bottom
	 * @param top
	 * @param near
	 * @param far
	 * @return Frustum projection matrix
	 */
	public static Matrix fromFrustum(double left, double right, double bottom, double top, double near, double far)
	{
		double A = (right + left) / (right - left);
		double B = (top + bottom) / (top - bottom);
		double C = -(far + near) / (far - near);
		double D = -(2 * far * near) / (far - near);
		double E = (2 * near) / (right - left);
		double F = (2 * near) / (top - bottom);
		return new Matrix(E, 0, A, 0, 0, F, B, 0, 0, 0, C, D, 0, 0, -1, 0);
	}

	/**
	 * Calculate a good eye separation and focal length for the current view.
	 * Used when dynamic stereo is enabled.
	 * 
	 * @param view
	 * @param separationExaggeration
	 *            Eye separation multiplier
	 * @return {@link AsymmetricFrutumParameters} containing a good eye
	 *         separation and focal length to use for the current view.
	 */
	public static AsymmetricFrutumParameters calculateStereoParameters(View view, double separationExaggeration)
	{
		Vec4 eyePoint = view.getCurrentEyePoint();
		double distanceFromOrigin = eyePoint.getLength3();

		Globe globe = view.getGlobe();
		double radius = globe.getRadiusAt(globe.computePositionFromPoint(eyePoint));
		double amount = Util.percentDouble(distanceFromOrigin, radius, radius * 5d);

		Vec4 centerPoint = view.getCenterPoint();
		double distanceToCenter;
		if (centerPoint != null)
		{
			distanceToCenter = eyePoint.distanceTo3(centerPoint);
		}
		else
		{
			distanceToCenter = view.getHorizonDistance();
		}

		//limit the distance to center relative to the eye distance from the ellipsoid surface
		double maxDistanceToCenter = (distanceFromOrigin - radius) * 10;
		distanceToCenter = Math.min(maxDistanceToCenter, distanceToCenter);

		//calculate focal length as distance to center when zoomed in, and distance from origin when zoomed out
		double focalLength = Util.mixDouble(amount, distanceToCenter, distanceFromOrigin);

		//exaggerate separation more when zoomed out
		separationExaggeration = Util.mixDouble(amount, separationExaggeration, separationExaggeration * 4);

		//move focal length closer as view is pitched
		amount = Util.percentDouble(view.getPitch().degrees, 0d, 90d);
		focalLength = Util.mixDouble(amount, focalLength, focalLength / 3d);

		//calculate eye separation linearly relative to focal length
		double eyeSeparation = separationExaggeration * focalLength / 100d;

		return new AsymmetricFrutumParameters(focalLength, eyeSeparation);
	}

	/**
	 * Container class for passing eye separation and focal length parameters.
	 */
	public static class AsymmetricFrutumParameters
	{
		public final double focalLength;
		public final double eyeSeparation;

		public AsymmetricFrutumParameters(double focalLength, double eyeSeparation)
		{
			this.focalLength = focalLength;
			this.eyeSeparation = eyeSeparation;
		}
	}
}