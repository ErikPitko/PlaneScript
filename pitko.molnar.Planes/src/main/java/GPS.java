import javax.vecmath.Point2d;

import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.operation.TransformException;

public class GPS {
	public static final double eQuatorialEarthRadius = 6378.1370;

	public static double HaversineInKM(Point2d gps1, Point2d gps2) {

		// a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
		// c = 2 ⋅ atan2( √a, √(1−a) )
		// return R * c

		double dlat = Math.toRadians(gps2.x - gps1.x);
		double dlong = Math.toRadians(gps2.y - gps1.y);

		double a = (Math.pow(Math.sin(dlat / 2.0), 2))
				+ (Math.cos(Math.toRadians(gps1.x)) * Math.cos(Math.toRadians(gps2.x)))
						* (Math.pow(Math.sin(dlong / 2.0), 2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return eQuatorialEarthRadius * c;

	}

	public static double Bearing(Point2d gps1, Point2d gps2) {
		// θ = atan2( sin Δλ ⋅ cos φ2 , cos φ1 ⋅ sin φ2 − sin φ1 ⋅ cos φ2 ⋅ cos
		// Δλ )
		// φ/λ for latitude/longitude

		gps1.x = Math.toRadians(gps1.x);
		gps1.y = Math.toRadians(gps1.y);

		gps2.x = Math.toRadians(gps2.x);
		gps2.y = Math.toRadians(gps2.y);

		double y = Math.sin(gps2.y - gps1.y) * Math.cos(gps1.x);
		double x = Math.cos(gps1.x) * Math.sin(gps2.x)
				- Math.sin(gps1.x) * Math.cos(gps2.x) * Math.cos(gps2.y - gps1.y);
		return Math.toDegrees(Math.atan2(y, x));

	}

	public static Point2d calcRelativeGPS(Point2d gps1, Point2d gps2, double speed, double time) {

		double[] temp;
		Point2d gpsDest = null;

		try {
			GeodeticCalculator geo = new GeodeticCalculator();
			geo.setStartingGeographicPoint(gps1.y, gps1.x);
			geo.setDirection(Bearing(gps1, gps2), ((speed / 3.6) * time));
			temp = geo.getDestinationPosition().getCoordinate();
			gpsDest = new Point2d(temp[1], temp[0]);
		} catch (TransformException e) {
			e.printStackTrace();
		}

		return gpsDest;
	}

}
