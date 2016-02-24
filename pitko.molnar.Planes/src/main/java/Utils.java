import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.vecmath.Point2d;

public class Utils {

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy.MMMMM.dd HH:mm");
	private static ResultSet planes = null;
	private static ResultSet planList = null;
	private static ResultSet airport = null;
	private static ResultSet models = null;
	private static StringBuilder buff = null;

	public static StringBuilder readInitInfo(String fName) throws FileNotFoundException, IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(fName));
		StringBuilder sbuild = new StringBuilder();
		String line;

		while ((line = bReader.readLine()) != null) {
			sbuild.append(line);
			sbuild.append(" ");
		}
		bReader.close();
		return sbuild;
	}

	public static String getTime() {
		Date date = new Date();
		return df.format(date);

	}

	public static String calcTime(String dateTime, int minutes) throws ParseException {
		Date d = df.parse(dateTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MINUTE, 10);
		return df.format(cal.getTime());
	}

	public static boolean landing(Point2d gpsPlane, Point2d gpsArrive, double avgSpeed,
			int refreshRate) {
		avgSpeed = (avgSpeed / 3.6);
		double distanceDuringRefresh = avgSpeed * refreshRate;
		if (GPS.HaversineInKM(gpsPlane, gpsArrive) * 1000 <= distanceDuringRefresh) {
			return true;
		}
		return false;
	}

	public static int flyLen(double distance, double avgSpeed) {

		Double result = 60 * (distance / avgSpeed);

		return result.intValue();
	}
	
	public static int planesInRadius(SqlC sql, Point2d radius,
			double radiusS) throws SQLException {
		int numOfPlanes = 0;
		ResultSet planes = sql.select("SELECT * FROM Planes;");
		while (planes.next()) {
			Point2d gpsPlane = new Point2d(planes.getDouble("GpsX"), planes.getDouble("GpsY"));
			// pošle x,y do metódy ktorá vráti vzdialenos medzi dvoma bodmi do
			// premeny dist
			double dist = GPS.HaversineInKM(radius, gpsPlane);
			if (dist <= radiusS) {
				numOfPlanes += 1;
				
				System.out.println("" + planes.getString("Name"));
			}

		}
		return numOfPlanes;

	}

	public static void extraDest(SqlC sql, int planeID) throws SQLException, ParseException {
		Random rand = new Random();
		buff = new StringBuilder();
		buff.append("('");
		buff.append(planeID);
		buff.append("', '");

		planList = sql
				.select("SELECT * FROM 'Planned List' WHERE PlaneID = '" + planeID + "' ORDER BY ID  DESC LIMIT 1;");
		planList.next();
		String leave = planList.getString("Leave");
		String arrival = planList.getString("Arrival");
		String datetime = planList.getString("Date/Time");
		buff.append(arrival);
		buff.append("', '");

		planes = sql.select("SELECT * FROM 'Planes' WHERE ID = '" + planeID + "';");
		planes.next();
		int modelID = planes.getInt("ModelID");

		models = sql.select("SELECT * FROM 'Types' WHERE ID = '" + modelID + "';");
		models.next();
		double speed = models.getDouble("AvgSpeed");

		airport = sql.select("SELECT * FROM 'Airports' WHERE Airport = '" + leave + "';");
		airport.next();
		Point2d gpsStartPoint = new Point2d(airport.getDouble("gpsX"), airport.getDouble("gpsY"));

		airport = sql.select("SELECT * FROM 'Airports' WHERE Airport = '" + arrival + "';");
		airport.next();
		Point2d gpsEndPoint = new Point2d(airport.getDouble("gpsX"), airport.getDouble("gpsY"));

		int airportID = airport.getInt("ID");
		int nextRandInt = airportID;
		while (nextRandInt == airportID) {
			nextRandInt = rand.nextInt(sql.count("Airports") - 1) + 1;
		}
		airport = sql.select("SELECT * FROM 'Airports' WHERE ID = " + nextRandInt + ";");
		airport.next();

		buff.append(airport.getString("Airport"));
		buff.append("', '");

		int time = flyLen(GPS.HaversineInKM(gpsStartPoint, gpsEndPoint), speed);

		Date d = df.parse(datetime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.MINUTE, time);

		String newTime = df.format(cal.getTime());

		buff.append(newTime);
		buff.append("')");

		sql.insert("INSERT INTO 'Planned List' (PlaneID, Leave, Arrival, 'Date/Time') VALUES " + buff.toString() + ";");

		// double speed = models.getDouble("AvgSpeed");
		// gpsRelative = GPS.calcRelativeGPS(gpsStartPoint, gpsEndPoint, speed,
		// timeSet);

		// sql.update("UPDATE 'Planes' set 'GpsX' = " + gpsRelative.x + ",
		// 'GpsY' = " + gpsRelative.y + " WHERE 'ID' = " + planes.getInt("ID") +
		// ";");

	}

	public static void initPlanList(SqlC sql) throws SQLException, ParseException {
		Random rand = new Random();
		int count = sql.count("Planes");
		planList = sql.select("SELECT * FROM 'Planes';");
		planList.next();
		int startID = planList.getInt("ID");

		for (int i = 0; i < count; i++) {
			planList = sql.select("SELECT * FROM 'Planes' WHERE ID = " + (startID + i) + ";");

			planList.next();
			int planeID = planList.getInt("ID");
			buff = new StringBuilder();
			buff.append("(");
			buff.append(planeID);
			buff.append(", '");

			models = sql.select("SELECT * FROM 'Types' WHERE 'ID' = " + planList.getInt("ModelID") + ";");
			models.next();

			int randInt = rand.nextInt(sql.count("Airports") - 1) + 1;

			airport = sql.select("SELECT * FROM 'Airports' WHERE ID = " + randInt + ";");
			airport.next();
			Point2d gpsStart = new Point2d(airport.getDouble("gpsX"), airport.getDouble("gpsY"));
			buff.append(airport.getString("Airport"));
			buff.append("', '");

			int nextRandInt = randInt;
			while (nextRandInt == randInt) {
				nextRandInt = rand.nextInt(sql.count("Airports") - 1) + 1;
			}

			airport = sql.select("SELECT * FROM 'Airports' WHERE ID = " + nextRandInt + ";");
			airport.next();
			buff.append(airport.getString("Airport"));
			buff.append("', '");

			buff.append(Utils.getTime());
			buff.append("')");

			sql.insert("INSERT INTO 'Planned List' (PlaneID, Leave, Arrival, 'Date/Time') VALUES " + buff.toString()
					+ ";");
			// "UPDATE COMPANY set SALARY = 25000.00 where ID=1;"
			sql.update("UPDATE 'Planes' set 'GpsX' = '" + gpsStart.x + "', 'GpsY' = '" + gpsStart.y + "' WHERE ID = "
					+ planeID + ";");

			buff = null;

			for (int n = 0; n < 4; n++) {
				extraDest(sql, planeID);
			}

		}

	}

	// "DatabaseInitContent.conf"
	// "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (1, 'Paul', 32,
	// 'California', 20000.00 );"
	public static void initDatabase(SqlC sql, String fname) throws FileNotFoundException, SQLException, IOException {
		sql.insert(Utils.readInitInfo(fname).toString());
	}

	public static void initPlanes(SqlC sql) throws SQLException {
		for (int i = 0; i < 20; i++) {
			Random rnd = new Random();
			UUID name = UUID.randomUUID();
			int modelID = rnd.nextInt(3) + 1;
			double gpsX = rnd.nextDouble() * 180 - 90;
			double gpsY = rnd.nextDouble() * 360 - 180;
			buff = new StringBuilder();
			buff.append("'" + name + "'");
			buff.append(", ");
			buff.append("'" + modelID + "'");
			buff.append(", ");
			buff.append("'" + gpsX + "'");
			buff.append(", ");
			buff.append("'" + gpsY + "'");

			sql.insert("INSERT INTO 'Planes' (Name, ModelID, GpsX, GpsY) VALUES (" + buff.toString() + ");");
			buff = null;
		}

	}

}
