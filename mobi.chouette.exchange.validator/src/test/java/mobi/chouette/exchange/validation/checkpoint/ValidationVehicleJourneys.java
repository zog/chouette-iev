package mobi.chouette.exchange.validation.checkpoint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Color;
import mobi.chouette.common.Context;
import mobi.chouette.dao.LineDAO;
import mobi.chouette.exchange.validation.ValidationData;
import mobi.chouette.exchange.validation.parameters.ValidationParameters;
import mobi.chouette.exchange.validation.report.CheckPoint;
import mobi.chouette.exchange.validation.report.Detail;
import mobi.chouette.exchange.validation.report.ValidationReport;
import mobi.chouette.exchange.validator.JobDataTest;
import mobi.chouette.model.JourneyPattern;
import mobi.chouette.model.Line;
import mobi.chouette.model.Route;
import mobi.chouette.model.VehicleJourney;
import mobi.chouette.model.VehicleJourneyAtStop;
import mobi.chouette.model.type.TransportModeNameEnum;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

@Log4j
public class ValidationVehicleJourneys extends AbstractTestValidation {
	private VehicleJourneyCheckPoints checkPoint = new VehicleJourneyCheckPoints();
	private ValidationParameters fullparameters;
	private VehicleJourney bean1;
	private VehicleJourney bean2;
	private List<VehicleJourney> beansFor4 = new ArrayList<>();

	@EJB
	LineDAO lineDao;

	@PersistenceContext(unitName = "referential")
	EntityManager em;

	@Inject
	UserTransaction utx;

	@Deployment
	public static WebArchive createDeployment() {

		WebArchive result;

		File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies()
				.resolve("mobi.chouette:mobi.chouette.exchange.validator").withTransitivity().asFile();

		result = ShrinkWrap.create(WebArchive.class, "test.war").addAsWebInfResource("postgres-ds.xml")
				.addAsLibraries(files).addClass(JobDataTest.class).addClass(AbstractTestValidation.class)
				.addAsResource(EmptyAsset.INSTANCE, "beans.xml");
		return result;

	}

	@BeforeGroups(groups = { "vehicleJourney" })
	public void init() {
		super.init();

		long id = 1;

		fullparameters = null;
		try {
			fullparameters = loadFullParameters();
			fullparameters.setCheckVehicleJourney(1);

			Line line = new Line();
			line.setId(id++);
			line.setObjectId("test1:Line:1");
			line.setName("test");
			Route route = new Route();
			route.setId(id++);
			route.setObjectId("test1:Route:1");
			route.setName("test1");
			route.setLine(line);
			JourneyPattern jp = new JourneyPattern();
			jp.setId(id++);
			jp.setObjectId("test1:JourneyPattern:1");
			jp.setName("test1");
			jp.setRoute(route);
			bean1 = new VehicleJourney();
			bean1.setId(id++);
			bean1.setObjectId("test1:VehicleJourney:1");
			bean1.setPublishedJourneyName("test1");
			bean1.setJourneyPattern(jp);
			bean2 = new VehicleJourney();
			bean2.setId(id++);
			bean2.setObjectId("test2:VehicleJourney:1");
			bean2.setPublishedJourneyName("test2");
			bean2.setJourneyPattern(jp);
			
			beansFor4.add(bean1);
			beansFor4.add(bean2);
		} catch (Exception e) {
			fullparameters = null;
			e.printStackTrace();
		}

	}

	@Test(groups = { "vehicleJourney" }, description = "4-VehicleJourney-1 no test",priority=1)
	public void verifyTest4_1_notest() throws Exception {
		// 4-VehicleJourney-1 : check columns
		log.info(Color.BLUE + "4-VehicleJourney-1 no test" + Color.NORMAL);
		Context context = initValidatorContext();
		Assert.assertNotNull(fullparameters, "no parameters for test");
		context.put(VALIDATION, fullparameters);
		context.put(VALIDATION_REPORT, new ValidationReport());

		fullparameters.setCheckVehicleJourney(0);
		ValidationData data = new ValidationData();
		data.getVehicleJourneys().addAll(beansFor4);
		context.put(VALIDATION_DATA, data);

		checkPoint.validate(context, null);

		ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);
		Assert.assertTrue(report.findCheckPointByName("4-VehicleJourney-1") == null, " report must not have item 4-VehicleJourney-1");

		fullparameters.setCheckVehicleJourney(1);

		context.put(VALIDATION_REPORT, new ValidationReport());

		checkPoint.validate(context, null);
		report = (ValidationReport) context.get(VALIDATION_REPORT);
		Assert.assertTrue(report.findCheckPointByName("4-VehicleJourney-1") != null, " report must have item 4-VehicleJourney-1");
		Assert.assertEquals(report.findCheckPointByName("4-VehicleJourney-1").getDetailCount(), 0,
				" checkpoint must have no detail");

	}

	@Test(groups = { "vehicleJourney" }, description = "4-VehicleJourney-1 unicity",priority=2)
	public void verifyTest4_1_unique() throws Exception {
		// 4-VehicleJourney-1 : check columns
		log.info(Color.BLUE + "4-VehicleJourney-1 unicity" + Color.NORMAL);
		Context context = initValidatorContext();
		Assert.assertNotNull(fullparameters, "no parameters for test");

		context.put(VALIDATION, fullparameters);
		context.put(VALIDATION_REPORT, new ValidationReport());

		fullparameters.setCheckVehicleJourney(1);
		fullparameters.getVehicleJourney().getObjectId().setUnique(1);

		ValidationData data = new ValidationData();
		data.getVehicleJourneys().addAll(beansFor4);
		context.put(VALIDATION_DATA, data);

		checkPoint.validate(context, null);
		fullparameters.getRoute().getObjectId().setUnique(0);
		// unique
		ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);

		List<Detail> details = checkReportForTest4_1(report, "4-VehicleJourney-1", 3);
		for (Detail detail : details) {
			Assert.assertEquals(detail.getReferenceValue(), "ObjectId", "detail must refer column");
			Assert.assertEquals(detail.getValue(), bean2.getObjectId().split(":")[2], "detail must refer value");
		}
	}

	@Test(groups = { "vehicleJourney" }, description = "3-VehicleJourney-1",priority=3)
	public void verifyTest3_1() throws Exception {
		// 3-VehicleJourney-1 : check if time progress correctly on each stop
		log.info(Color.BLUE + "3-VehicleJourney-1" + Color.NORMAL);
		Context context = initValidatorContext();
		context.put(VALIDATION_REPORT, new ValidationReport());

		Assert.assertNotNull(fullparameters, "no parameters for test");

		importLines("Ligne_OK.xml", 1, 1, true);

		utx.begin();
		em.joinTransaction();

		List<Line> beans = lineDao.findAll();
		Assert.assertFalse(beans.isEmpty(), "No data for test");
		Line line1 = beans.get(0);

		Route route1 = line1.getRoutes().get(0);
		route1.setObjectId("NINOXE:Route:checkedRoute");
		JourneyPattern jp1 = route1.getJourneyPatterns().get(0);
		jp1.setObjectId("NINOXE:JourneyPattern:checkedJP");
		VehicleJourney vj1 = jp1.getVehicleJourneys().get(0);
		vj1.setObjectId("NINOXE:VehicleJourney:checkedVJ");
		long maxDiffTime = 0;
		for (VehicleJourneyAtStop vjas : vj1.getVehicleJourneyAtStops()) {
			if (vjas.getArrivalTime().equals(vjas.getDepartureTime())) {
				vjas.getArrivalTime().setTime(vjas.getArrivalTime().getTime() - 60000);
			}
			long diffTime = Math.abs(diffTime(vjas.getArrivalTime(), vjas.getDepartureTime()));
			if (diffTime > maxDiffTime)
				maxDiffTime = diffTime;
		}
		ValidationData data = new ValidationData();
		data.getVehicleJourneys().addAll(jp1.getVehicleJourneys());
		fullparameters.setInterStopDurationMax((int) maxDiffTime - 30);
		context.put(VALIDATION_DATA, data);
		context.put(VALIDATION, fullparameters);

		checkPoint.validate(context, null);

		ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);
		Assert.assertNotEquals(report.getCheckPoints().size(), 0, " report must have items");
		
		CheckPoint checkPointReport = report.findCheckPointByName("3-VehicleJourney-1");
		Assert.assertNotNull(checkPointReport, "report must contain a 3-VehicleJourney-1 checkPoint");

		Assert.assertEquals(checkPointReport.getState(), CheckPoint.RESULT.NOK, " checkPointReport must be nok");
		Assert.assertEquals(checkPointReport.getSeverity(), CheckPoint.SEVERITY.WARNING,
				" checkPointReport must be on level warning");
		Assert.assertEquals(checkPointReport.getDetailCount(), 4, " checkPointReport must have 4 item");

		String detailKey = "3-VehicleJourney-1".replaceAll("-", "_").toLowerCase();
		List<Detail> details = checkPointReport.getDetails();
		for (Detail detail : details) {
			Assert.assertTrue(detail.getKey().startsWith(detailKey),
					"details key should start with test key : expected " + detailKey + ", found : " + detail.getKey());
		}
		// check detail keys
		for (Detail detail : checkPointReport.getDetails()) {
			Assert.assertEquals(detail.getSource().getObjectId(), vj1.getObjectId(),
					"vj 1 must be source of error");
		}

		utx.rollback();

	}

	@Test(groups = { "vehicleJourney" }, description = "3-VehicleJourney-2",priority=4)
	public void verifyTest3_2() throws Exception {
		// 3-VehicleJourney-2 : check speed progression
		log.info(Color.BLUE + "3-VehicleJourney-2" + Color.NORMAL);
		Context context = initValidatorContext();
		context.put(VALIDATION_REPORT, new ValidationReport());

		Assert.assertNotNull(fullparameters, "no parameters for test");

		importLines("Ligne_OK.xml", 1, 1, true);

		utx.begin();
		em.joinTransaction();

		List<Line> beans = lineDao.findAll();
		Assert.assertFalse(beans.isEmpty(), "No data for test");
		Line line1 = beans.get(0);

		// line1 is model;

		Route route1 = line1.getRoutes().get(0);
		route1.setObjectId("NINOXE:Route:checkedRoute");
		JourneyPattern jp1 = route1.getJourneyPatterns().get(0);
		jp1.setObjectId("NINOXE:JourneyPattern:checkedJP");

		VehicleJourney vj1 = jp1.getVehicleJourneys().get(0);
		vj1.setObjectId("NINOXE:VehicleJourney:checkedVJ");

		fullparameters.getModeBus().setSpeedMax( 10);
		fullparameters.getModeBus().setSpeedMin( 20);

		ValidationData data = new ValidationData();
		data.getVehicleJourneys().addAll(jp1.getVehicleJourneys());
		context.put(VALIDATION_DATA, data);
		context.put(VALIDATION, fullparameters);

		checkPoint.validate(context, null);

		ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);
		Assert.assertNotEquals(report.getCheckPoints().size(), 0, " report must have items");
		

		CheckPoint checkPointReport = report.findCheckPointByName("3-VehicleJourney-2");
		Assert.assertNotNull(checkPointReport, "report must contain a 3-VehicleJourney-2 checkPoint");

		Assert.assertEquals(checkPointReport.getState(), CheckPoint.RESULT.NOK, " checkPointReport must be nok");
		Assert.assertEquals(checkPointReport.getSeverity(), CheckPoint.SEVERITY.WARNING,
				" checkPointReport must be on level warning");
		
		Assert.assertEquals(checkPointReport.getDetailCount(), 81, " checkPointReport must have 81 item");
		String detailKey = "3-VehicleJourney-2".replaceAll("-", "_").toLowerCase();
		List<Detail> details = checkPointReport.getDetails();
		for (Detail detail : details) {
			Assert.assertTrue(detail.getKey().startsWith(detailKey),
					"details key should start with test key : expected " + detailKey + ", found : " + detail.getKey());
		}
		utx.rollback();

	}

	@Test(groups = { "vehicleJourney" }, description = "3-VehicleJourney-3",priority=5)
	public void verifyTest3_3() throws Exception {
		// 3-VehicleJourney-3 : check if two journeys progress similarly
		log.info(Color.BLUE + "3-VehicleJourney-3" + Color.NORMAL);
		Context context = initValidatorContext();
		context.put(VALIDATION_REPORT, new ValidationReport());

		Assert.assertNotNull(fullparameters, "no parameters for test");

		importLines("Ligne_OK.xml", 1, 1, true);

		utx.begin();
		em.joinTransaction();

		List<Line> beans = lineDao.findAll();
		Assert.assertFalse(beans.isEmpty(), "No data for test");
		Line line1 = beans.get(0);

		// line1 is model;
		VehicleJourney vj1 = null;
		JourneyPattern jp1 = null;
		for (Route route : line1.getRoutes()) {
			for (JourneyPattern jp : route.getJourneyPatterns()) {
				for (VehicleJourney vj : jp.getVehicleJourneys()) {
					if (vj.getObjectId().equals("NINOXE:VehicleJourney:15627288")) {
						vj1 = vj;
						jp1 = jp;
					}
				}
			}
		}

		Assert.assertNotNull(jp1, "tested jp not found");
		Assert.assertNotNull(vj1, "tested vj not found");

		VehicleJourneyAtStop vjas1 = vj1.getVehicleJourneyAtStops().get(1);
		vjas1.getArrivalTime().setTime(vjas1.getArrivalTime().getTime() - 240000);

		fullparameters.getModeBus().setInterStopDurationVariationMax(220);

		ValidationData data = new ValidationData();
		data.getVehicleJourneys().addAll(jp1.getVehicleJourneys());
		context.put(VALIDATION_DATA, data);
		context.put(VALIDATION, fullparameters);

		checkPoint.validate(context, null);

		ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);
		Assert.assertNotEquals(report.getCheckPoints().size(), 0, " report must have items");
		

		CheckPoint checkPointReport = report.findCheckPointByName("3-VehicleJourney-3");
		Assert.assertNotNull(checkPointReport, "report must contain a 3-VehicleJourney-3 checkPoint");

		Assert.assertEquals(checkPointReport.getState(), CheckPoint.RESULT.NOK, " checkPointReport must be nok");
		Assert.assertEquals(checkPointReport.getSeverity(), CheckPoint.SEVERITY.WARNING,
				" checkPointReport must be on level warning");
		
		Assert.assertEquals(checkPointReport.getDetailCount(), 26, " checkPointReport must have 26 item");
		String detailKey = "3-VehicleJourney-3".replaceAll("-", "_").toLowerCase();
		List<Detail> details = checkPointReport.getDetails();
		for (Detail detail : details) {
			Assert.assertTrue(detail.getKey().startsWith(detailKey),
					"details key should start with test key : expected " + detailKey + ", found : " + detail.getKey());
		}
		utx.rollback();

	}

	@Test(groups = { "vehicleJourney" }, description = "3-VehicleJourney-4",priority=6)
	public void verifyTest3_4() throws Exception {
		// 3-VehicleJourney-4 : check if each journey has minimum one timetable
		log.info(Color.BLUE + "3-VehicleJourney-3" + Color.NORMAL);
		Context context = initValidatorContext();
		context.put(VALIDATION_REPORT, new ValidationReport());

		Assert.assertNotNull(fullparameters, "no parameters for test");

		importLines("Ligne_OK.xml", 1, 1, true);

		utx.begin();
		em.joinTransaction();

		List<Line> beans = lineDao.findAll();
		Assert.assertFalse(beans.isEmpty(), "No data for test");
		Line line1 = beans.get(0);

		// line1 is model;

		Route route1 = line1.getRoutes().get(0);
		route1.setObjectId("NINOXE:Route:checkedRoute");
		JourneyPattern jp1 = route1.getJourneyPatterns().get(0);
		jp1.setObjectId("NINOXE:JourneyPattern:checkedJP");
		VehicleJourney vj1 = jp1.getVehicleJourneys().get(0);
		vj1.setObjectId("NINOXE:VehicleJourney:checkedVJ");

		vj1.getTimetables().clear();
		
		ValidationData data = new ValidationData();
		data.getVehicleJourneys().addAll(jp1.getVehicleJourneys());
		context.put(VALIDATION_DATA, data);
		context.put(VALIDATION, fullparameters);

		checkPoint.validate(context, null);

		ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);
		Assert.assertNotEquals(report.getCheckPoints().size(), 0, " report must have items");
		

		CheckPoint checkPointReport = report.findCheckPointByName("3-VehicleJourney-4");
		Assert.assertNotNull(checkPointReport, "report must contain a 3-VehicleJourney-4 checkPoint");

		Assert.assertEquals(checkPointReport.getState(), CheckPoint.RESULT.NOK, " checkPointReport must be nok");
		Assert.assertEquals(checkPointReport.getSeverity(), CheckPoint.SEVERITY.WARNING,
				" checkPointReport must be on level warning");
		
		Assert.assertEquals(checkPointReport.getDetailCount(), 1, " checkPointReport must have 1 item");
		String detailKey = "3-VehicleJourney-4".replaceAll("-", "_").toLowerCase();
		List<Detail> details = checkPointReport.getDetails();
		for (Detail detail : details) {
			Assert.assertTrue(detail.getKey().startsWith(detailKey),
					"details key should start with test key : expected " + detailKey + ", found : " + detail.getKey());
		}
		utx.rollback();

	}

	@Test(groups = { "vehicleJourney" }, description = "4-VehicleJourney-2",priority=7)
	public void verifyTest4_2() throws Exception {
		// 4-VehicleJourney-2 : check transport mode
		log.info(Color.BLUE +"4-VehicleJourney-2"+ Color.NORMAL);
		Context context = initValidatorContext();
		context.put(VALIDATION,fullparameters);

		Assert.assertNotNull(fullparameters, "no parameters for test");

		importLines("Ligne_OK.xml", 1, 1, true);

		utx.begin();
	    em.joinTransaction();
	    
		List<Line> beans = lineDao.findAll();
		Assert.assertFalse(beans.isEmpty(), "No data for test");
		Line line1 = beans.get(0);


		// line1 is model;
		line1.setObjectId("NINOXE:Line:modelLine");

		Route r1 = line1.getRoutes().get(0);
		JourneyPattern jp1 = r1.getJourneyPatterns().get(0);

		{ // check test not required when check is false
			ValidationData data = new ValidationData();
			context.put(VALIDATION_DATA, data);
			data.getVehicleJourneys().addAll(jp1.getVehicleJourneys());
			context.put(VALIDATION_REPORT, new ValidationReport());
			fullparameters.setCheckAllowedTransportModes(0);
			context.put(VALIDATION,fullparameters);
			checkPoint.validate(context, null);
			ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);

			Assert.assertNotEquals(report.getCheckPoints().size(), 0, " report must have items");

			CheckPoint checkPointReport = report.findCheckPointByName("4-VehicleJourney-2");
			Assert.assertNull(checkPointReport, "report must not contain a 4-VehicleJourney-2 checkPoint");
		}



		{ // check test not required when mode is ok
			ValidationData data = new ValidationData();
			context.put(VALIDATION_DATA, data);
			data.getVehicleJourneys().addAll(jp1.getVehicleJourneys());
			context.put(VALIDATION_REPORT, new ValidationReport());
			fullparameters.setCheckAllowedTransportModes(1);
			fullparameters.getModeBus().setAllowedTransport(1);
			context.put(VALIDATION,fullparameters);
			checkPoint.validate(context, null);
			ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);

			Assert.assertNotEquals(report.getCheckPoints().size(), 0, " report must have items");

			CheckPoint checkPointReport = report.findCheckPointByName("4-VehicleJourney-2");
			Assert.assertNotNull(checkPointReport, "report must contain a 4-VehicleJourney-2 checkPoint");
		}

		jp1.getVehicleJourneys().get(0).setTransportMode(TransportModeNameEnum.Bus);
		{ // check test not required when check is false
			ValidationData data = new ValidationData();
			context.put(VALIDATION_DATA, data);
			data.getVehicleJourneys().addAll(jp1.getVehicleJourneys());
			context.put(VALIDATION_REPORT, new ValidationReport());
			fullparameters.setCheckAllowedTransportModes(1);
			fullparameters.getModeBus().setAllowedTransport(0);
			context.put(VALIDATION,fullparameters);
			checkPoint.validate(context, null);
			ValidationReport report = (ValidationReport) context.get(VALIDATION_REPORT);

			Assert.assertNotEquals(report.getCheckPoints().size(), 0, " report must have items");

			CheckPoint checkPointReport = report.findCheckPointByName("4-VehicleJourney-2");
			Assert.assertNotNull(checkPointReport, "report must contain a 4-VehicleJourney-2 checkPoint");

			Assert.assertEquals(checkPointReport.getState(),
					CheckPoint.RESULT.NOK,
					" checkPointReport must be nok");
			Assert.assertEquals(checkPointReport.getSeverity(),
					CheckPoint.SEVERITY.ERROR,
					" checkPointReport must be on severity error");
			Assert.assertEquals(checkPointReport.getDetailCount(), 1,
					" checkPointReport must have 1 item");
		}
		utx.rollback();

	}

}
