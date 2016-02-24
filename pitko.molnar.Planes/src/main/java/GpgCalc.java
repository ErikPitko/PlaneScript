import java.sql.ResultSet;
import java.sql.SQLException;

import javax.vecmath.Point2d;

public class GpgCalc implements Runnable {
	private SqlC sql = null;
	private ResultSet planes = null;
	private ResultSet models = null;
	private ResultSet plans = null;
	private ResultSet airport = null;
	private int timeSet = 5; // in seconds
	private StringBuilder buff = null;
	private Point2d gpsRelative = null;

	public GpgCalc(SqlC sql) {
		this.sql = sql;
	}

	public void run() {
		System.out.println("Thread started ...");
		try {
			planes = sql.select("SELECT * FROM 'Planes';");
			planes.next();
			int first = planes.getInt("ID");
			int count = sql.count("Planes");

			while (true) {
				for (int i = 0; i < count; i++) {
					planes = sql.select("SELECT * FROM 'Planes' WHERE ID = " + (first + i) + ";");
					planes.next();
					int planeID = planes.getInt("ID");
					int modelID = planes.getInt("ModelID");
					Point2d gpsPlane = new Point2d(planes.getDouble("GpsX"), planes.getDouble("GpsY"));

					plans = sql
							.select("SELECT * FROM 'Planned List' WHERE PlaneID = " + planeID + " ORDER BY ID LIMIT 1");
					plans.next();
					String arrival = plans.getString("Arrival");

					airport = sql.select("SELECT * FROM 'Airports' WHERE Airport = '" + arrival + "';");
					airport.next();
					Point2d gpsEndPoint = new Point2d(airport.getDouble("gpsX"), airport.getDouble("gpsY"));

					models = sql.select("SELECT * FROM 'Types' WHERE ID = " + modelID);
					models.next();

					double speed = models.getDouble("AvgSpeed");

					if (Utils.landing(gpsPlane, gpsEndPoint, speed, timeSet)) {
						plans = sql.select("SELECT * FROM 'Planned List' WHERE PlaneID = " + planeID + " ORDER BY ID LIMIT 2;");
						plans.next();
						String leave = plans.getString("Leave");
						String arrive = plans.getString("Arrival");
						plans.next();
						String datetime = plans.getString("Date/Time");

						// airport = sql.select("SELECT * FROM 'Airports' WHERE
						// Airport = '" + leave + "';");
						// airport.next();
						// Point2d gpsPlaneStartPoint = new
						// Point2d(airport.getDouble("gpsX"),
						// airport.getDouble("gpsY"));

						airport = sql.select("SELECT * FROM 'Airports' WHERE Airport = '" + arrive + "';");
						airport.next();
						Point2d gpsPlaneEndPoint = new Point2d(airport.getDouble("gpsX"), airport.getDouble("gpsY"));

						buff = new StringBuilder();
						buff.append("('");
						buff.append(planeID);
						buff.append("', '");
						buff.append(arrive);
						buff.append("', '");
						buff.append(gpsPlaneEndPoint.x);
						buff.append("', '");
						buff.append(gpsPlaneEndPoint.y);
						buff.append("', '");
						buff.append(datetime);
						buff.append("')");
						sql.insert("INSERT INTO 'GPS History' (PlaneID, Place, gpsX, gpsY, 'Date/Time') VALUES "
								+ buff.toString() + ";");

						buff = new StringBuilder();
						buff.append("('");
						buff.append(planeID);
						buff.append("', '");
						buff.append(leave);
						buff.append("', '");
						buff.append(arrival);
						buff.append("', '");
						buff.append(datetime);
						buff.append("')");
						sql.insert("INSERT INTO 'Flight History' (PlaneID, Leave, Arrival, 'Date/Time') VALUES "
								+ buff.toString() + ";");
						System.out.println("Plane landing: " + gpsPlaneEndPoint.toString());
						sql.update("UPDATE 'Planes' set 'GpsX' = '" + gpsPlaneEndPoint.x + "', 'GpsY' = '"+ gpsPlaneEndPoint.y + "' WHERE ID = " + planeID + ";");
						
						
						plans = sql.select("SELECT * FROM 'Planned List' WHERE PlaneID = " + planeID + " ORDER BY ID LIMIT 1;");
						plans.next();
						int id = plans.getInt("ID");
						sql.delete("DELETE FROM 'Planned List' WHERE ID = " + id + ";");

					} else {
						gpsRelative = GPS.calcRelativeGPS(gpsPlane, gpsEndPoint, speed, timeSet);
//						System.out.println("Plane update: " + gpsRelative.toString());
						sql.update("UPDATE 'Planes' set 'GpsX' = '" + gpsRelative.x + "', 'GpsY' = '" + gpsRelative.y
								+ "' WHERE ID = " + planeID + ";");
						
					}

				}
				try {
					Thread.sleep(timeSet*1000);
				} catch (InterruptedException e) {}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
