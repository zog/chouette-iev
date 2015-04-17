package mobi.chouette.model.util;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mobi.chouette.model.AccessLink;
import mobi.chouette.model.AccessPoint;
import mobi.chouette.model.Company;
import mobi.chouette.model.ConnectionLink;
import mobi.chouette.model.GroupOfLine;
import mobi.chouette.model.JourneyPattern;
import mobi.chouette.model.Line;
import mobi.chouette.model.Network;
import mobi.chouette.model.Route;
import mobi.chouette.model.StopArea;
import mobi.chouette.model.StopPoint;
import mobi.chouette.model.Timetable;
import mobi.chouette.model.VehicleJourney;

@NoArgsConstructor
@ToString()
public class Referential implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private Map<String, AccessLink> sharedAccessLinks = new HashMap<String, AccessLink>();

	@Getter
	@Setter
	private Map<String, AccessPoint> sharedAccessPoints = new HashMap<String, AccessPoint>();

	@Getter
	@Setter
	private Map<String, Network> sharedPTNetworks = new HashMap<String, Network>();

	@Getter
	@Setter
	private Map<String, Company> sharedCompanies = new HashMap<String, Company>();

	@Getter
	@Setter
	private Map<String, Route> routes = new HashMap<String, Route>();

	@Getter
	@Setter
	private Map<String, Line> lines = new HashMap<String, Line>();

	@Getter
	@Setter
	private Map<String, JourneyPattern> journeyPatterns = new HashMap<String, JourneyPattern>();

	@Getter
	@Setter
	private Map<String, ConnectionLink> sharedConnectionLinks = new HashMap<String, ConnectionLink>();

	@Getter
	@Setter
	private Map<String, StopArea> sharedStopAreas = new HashMap<String, StopArea>();

	@Getter
	@Setter
	private Map<String, GroupOfLine> sharedGroupOfLines = new HashMap<String, GroupOfLine>();

	@Getter
	@Setter
	private Map<String, StopPoint> stopPoints = new HashMap<String, StopPoint>();

	@Getter
	@Setter
	private Map<String, VehicleJourney> vehicleJourneys = new HashMap<String, VehicleJourney>();

	@Getter
	@Setter
	private Map<String, Line> sharedLines = new HashMap<String, Line>();
	
	@Getter
	@Setter
	private Map<String, Timetable> sharedTimetables = new HashMap<String, Timetable>();

	public void clear() {
		lines.clear();
		routes.clear();
		stopPoints.clear();
		journeyPatterns.clear();
		vehicleJourneys.clear();
		
		accessLinks.clear();
		accessPoints.clear();
		ptNetworks.clear();
		companies.clear();
		connectionLinks.clear();
		stopAreas.clear();
		groupOfLines.clear();
		timetables.clear();
	}

	@Getter
	@Setter
	private Map<String, AccessLink> accessLinks = new HashMap<String, AccessLink>();

	@Getter
	@Setter
	private Map<String, AccessPoint> accessPoints = new HashMap<String, AccessPoint>();

	@Getter
	@Setter
	private Map<String, Network> ptNetworks = new HashMap<String, Network>();

	@Getter
	@Setter
	private Map<String, Company> companies = new HashMap<String, Company>();

	@Getter
	@Setter
	private Map<String, ConnectionLink> connectionLinks = new HashMap<String, ConnectionLink>();

	@Getter
	@Setter
	private Map<String, StopArea> stopAreas = new HashMap<String, StopArea>();

	@Getter
	@Setter
	private Map<String, GroupOfLine> groupOfLines = new HashMap<String, GroupOfLine>();

	@Getter
	@Setter
	private Map<String, Timetable> timetables = new HashMap<String, Timetable>();

}