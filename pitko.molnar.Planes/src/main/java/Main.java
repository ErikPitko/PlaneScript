import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

import javax.vecmath.Point2d;

public class Main {

	public static void main(String[] args) throws SQLException, InterruptedException, FileNotFoundException, IOException, ParseException {
		Scanner sc = new Scanner(System.in);
		
		System.out.print("Welcome to our flight manager");System.out.println();
		
		SqlC sql = new SqlC();
		sql.delete("DELETE FROM 'GPS History'");
		sql.delete("DELETE FROM 'Planned List'");
		sql.delete("DELETE FROM 'Planes'");
		Utils.initPlanes(sql);
		Utils.initPlanList(sql);
		
		Thread t = new Thread(new GpgCalc(sql));
		t.start();
		// try {
		// sql.initDatabase("DatabaseInitContent.conf");
		// } catch (FileNotFoundException e1) {

		// e1.printStackTrace();
		// } catch (SQLException e1) {

		// e1.printStackTrace();
		// } catch (IOException e1) {

		// e1.printStackTrace();
		// }

		// Menu
		Boolean run = true;
		while (run) {
			System.out.println("Please choose an option:");
			System.out.println("1:Show all airplanes");
			System.out.println("2:Show flight history");
			System.out.println("3:Gps History");
			System.out.println("4:Show List of planed flights");
			System.out.println("5:Enter coordinates and radius"); 
			int opt = sc.nextInt();

			switch (opt) {
			default:
				System.out.println("Wrong input");
				break;
			case 1:
				ResultSet planes = null;
				ResultSet models = null;
				ResultSet flightHis = null;
				ResultSet gpsHis = null;
				ResultSet plannedLis = null;
				try {
					planes = sql.select("SELECT * FROM Planes;");

					while (planes.next()) {
						System.out.println("Name: " + planes.getString("Name"));
						System.out.println();
						System.out.println("Model ID: "
								+ planes.getInt("ModelID"));
						System.out
								.println("GPS X: " + planes.getDouble("gpsX"));
						System.out
								.println("GPS Y: " + planes.getDouble("gpsY"));
						System.out.println();

					}
					System.out.println();
					System.out.println("PLANE MODELS:");

					models = sql.select("SELECT * FROM Types;");
					while (models.next()) {
						System.out.println("Model ID: " + models.getInt("ID"));
						System.out.println();
						System.out.println("Model name: "
								+ models.getString("Model"));
						System.out.println("Weight(t): "
								+ models.getDouble("Weight"));
						System.out.println("Length(m): "
								+ models.getDouble("Length"));
						System.out.println("Width(m): "
								+ models.getDouble("Width"));
						System.out.println("Height(m): "
								+ models.getDouble("Height"));
						System.out.println("Average speed(km/h): "
								+ models.getDouble("AvgSpeed"));
						System.out.println();
					}
				} catch (SQLException e) {

					e.printStackTrace();
				}

				break;
			case 2:
				try {
					flightHis = sql.select("SELECT * FROM 'Flight History';");
					while (flightHis.next()) {
						System.out.println("Plane ID: "
								+ flightHis.getInt("PlaneID"));
						System.out.println("Left from:: "
								+ flightHis.getString("Leave"));
						System.out.println("Arrived to: "
								+ flightHis.getString("Arrival"));
						System.out.println("Date/Time: "
								+ flightHis.getString("Date/Time"));
						System.out.println();

					}
				} catch (SQLException e) {

					e.printStackTrace();
				}

				break;

			case 3:
				try {
					gpsHis = sql.select("SELECT * FROM 'GPS History';");
					while (gpsHis.next()) {
						System.out.println("Plane ID: "
								+ gpsHis.getInt("PlaneID"));
						System.out.println();
						System.out.println("Place: " + gpsHis.getInt("Place"));
						System.out
								.println("GPS X: " + gpsHis.getDouble("gpsX"));
						System.out
								.println("GPS Y: " + gpsHis.getDouble("gpsY"));
						System.out.println("Place: "
								+ gpsHis.getInt("Date/Time"));
						System.out.println();

					}

				} catch (SQLException e) {

					e.printStackTrace();
				}

				break;
			case 4:
				try {
					plannedLis = sql.select("SELECT * FROM 'Planned List';");
					while (plannedLis.next()) {
						System.out.println("Plane ID: "
								+ plannedLis.getInt("PlaneID"));
						System.out.println("Leave From: "
								+ plannedLis.getString("Leave"));
						System.out.println("Arrive to: "
								+ plannedLis.getString("Arrival"));
						System.out.println("Date/Time: "
								+ plannedLis.getString("Date/Time"));

					}
				} catch (SQLException e) {

					e.printStackTrace();
				}

				break;
			case 5:
				
				System.out.println("Set X coordinate(middle of radius):");
				double radiusX= sc.nextDouble();
				System.out.println("Set Y coordinate(middle of radius):");
				double radiusY= sc.nextDouble();
				System.out.println("Set size of radius(km):");
				double radiusS=sc.nextDouble();
				// zavolať metódu z utils planesInRadius + vypísať jej int result(počet lietadiel, mená vypysuje sama)
				int radius = Utils.planesInRadius(sql, new Point2d(radiusX, radiusY), radiusS);
				System.out.println("planes in radius: " + radius);
				
				break;
			case 6:
				run = false;
				System.out.println("Exiting");
				sc.close();
				t.interrupt();
				sql.close();

				break;
			}

		}

	}
	}
