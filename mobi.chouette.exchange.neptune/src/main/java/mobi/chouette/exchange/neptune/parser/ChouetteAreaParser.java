package mobi.chouette.exchange.neptune.parser;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Context;
import mobi.chouette.common.XPPUtil;
import mobi.chouette.exchange.importer.Parser;
import mobi.chouette.exchange.importer.ParserFactory;
import mobi.chouette.exchange.importer.ParserUtils;
import mobi.chouette.exchange.neptune.Constant;
import mobi.chouette.exchange.neptune.JsonExtension;
import mobi.chouette.exchange.neptune.model.AreaCentroid;
import mobi.chouette.exchange.neptune.model.NeptuneObjectFactory;
import mobi.chouette.exchange.neptune.validation.AreaCentroidValidator;
import mobi.chouette.exchange.neptune.validation.StopAreaValidator;
import mobi.chouette.exchange.validator.ValidatorFactory;
import mobi.chouette.model.StopArea;
import mobi.chouette.model.StopPoint;
import mobi.chouette.model.type.ChouetteAreaEnum;
import mobi.chouette.model.type.LongLatTypeEnum;
import mobi.chouette.model.type.UserNeedEnum;
import mobi.chouette.model.util.ObjectFactory;
import mobi.chouette.model.util.Referential;

import org.codehaus.jettison.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Log4j
public class ChouetteAreaParser implements Parser, Constant, JsonExtension {
	private static final String CHILD_TAG = "ChouetteArea";

	@Override
	public void parse(Context context) throws Exception {

		XmlPullParser xpp = (XmlPullParser) context.get(PARSER);
		// Referential referential = (Referential) context.get(REFERENTIAL);

		xpp.require(XmlPullParser.START_TAG, null, CHILD_TAG);
		context.put(COLUMN_NUMBER, xpp.getColumnNumber());
		context.put(LINE_NUMBER, xpp.getLineNumber());


		BiMap<String, String> map = HashBiMap.create(); 
		context.put(STOPAREA_AREACENTROID_MAP, map);

		while (xpp.nextTag() == XmlPullParser.START_TAG) {

			if (xpp.getName().equals("StopArea")) {
				parseStopArea(context, map);
			} else if (xpp.getName().equals("AreaCentroid")) {
				parseAreaCentroid(context, map);
			} else {
				XPPUtil.skipSubTree(log, xpp);
			}
		}
	}

	private void parseStopArea(Context context, BiMap<String, String> map)
			throws Exception {
		XmlPullParser xpp = (XmlPullParser) context.get(PARSER);
		Referential referential = (Referential) context.get(REFERENTIAL);

		xpp.require(XmlPullParser.START_TAG, null, "StopArea");
		int columnNumber =  xpp.getColumnNumber();
		int lineNumber =  xpp.getLineNumber();

		StopAreaValidator validator = (StopAreaValidator) ValidatorFactory.create(StopAreaValidator.class.getName(), context);

		StopArea stopArea = null;
		List<String> contains = new ArrayList<String>();

		String objectId = null;
		while (xpp.nextTag() == XmlPullParser.START_TAG) {
			if (xpp.getName().equals("objectId")) {
				objectId = ParserUtils.getText(xpp.nextText());
				stopArea = ObjectFactory.getStopArea(referential, objectId);
				stopArea.setFilled(true);
				validator.addLocation(context, objectId, lineNumber, columnNumber);
			} else if (xpp.getName().equals("objectVersion")) {
				Integer version = ParserUtils.getInt(xpp.nextText());
				stopArea.setObjectVersion(version);
			} else if (xpp.getName().equals("creationTime")) {
				Date creationTime = ParserUtils.getSQLDateTime(xpp.nextText());
				stopArea.setCreationTime(creationTime);
			} else if (xpp.getName().equals("creatorId")) {
				stopArea.setCreatorId(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("name")) {
				stopArea.setName(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("comment")) {
				stopArea.setComment(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("StopAreaExtension")) {

				while (xpp.nextTag() == XmlPullParser.START_TAG) {

					if (xpp.getName().equals("areaType")) {
						stopArea.setAreaType(ParserUtils.getEnum(
								ChouetteAreaEnum.class, xpp.nextText()));
						if (stopArea.getAreaType() == ChouetteAreaEnum.BoardingPosition
								|| stopArea.getAreaType() == ChouetteAreaEnum.Quay) {
							for (String childId : contains) {
								StopPoint stopPoint = ObjectFactory
										.getStopPoint(referential, childId);
								stopPoint.setContainedInStopArea(stopArea);
							}
						} else if (stopArea.getAreaType() == ChouetteAreaEnum.ITL) {
							for (String childId : contains) {
								StopArea child = ObjectFactory.getStopArea(
										referential, childId);
								stopArea.addRoutingConstraintStopArea(child);
							}
						} else {
							for (String childId : contains) {
								StopArea child = ObjectFactory.getStopArea(
										referential, childId);
								child.setParent(stopArea);
							}
						}
					} else if (xpp.getName().equals("nearestTopicName")) {
						stopArea.setNearestTopicName(ParserUtils.getText(xpp
								.nextText()));
					} else if (xpp.getName().equals("fareCode")) {
						stopArea.setFareCode(ParserUtils.getInt(xpp.nextText()));
					} else if (xpp.getName().equals("registration")) {
						while (xpp.nextTag() == XmlPullParser.START_TAG) {
							if (xpp.getName().equals("registrationNumber")) {
								stopArea.setRegistrationNumber(ParserUtils
										.getText(xpp.nextText()));
							} else {
								XPPUtil.skipSubTree(log, xpp);
							}
						}
					} else if (xpp.getName().equals(
							"mobilityRestrictedSuitability")) {
						stopArea.setMobilityRestrictedSuitable(ParserUtils
								.getBoolean(xpp.nextText()));
					} else if (xpp.getName().equals(
							"accessibilitySuitabilityDetails")) {
						List<UserNeedEnum> userNeeds = new ArrayList<UserNeedEnum>();
						while (xpp.nextTag() == XmlPullParser.START_TAG) {
							if (xpp.getName().equals("MobilityNeed")
									|| xpp.getName()
									.equals("PsychosensoryNeed")
									|| xpp.getName().equals("MedicalNeed")
									|| xpp.getName().equals("EncumbranceNeed")) {
								UserNeedEnum userNeed = ParserUtils.getEnum(
										UserNeedEnum.class, xpp.nextText());
								if (userNeed != null) {
									userNeeds.add(userNeed);
								}

							} else {
								XPPUtil.skipSubTree(log, xpp);
							}
						}
						stopArea.setUserNeeds(userNeeds);
					} else if (xpp.getName().equals("stairsAvailability")) {
						stopArea.setStairsAvailable(ParserUtils.getBoolean(xpp
								.nextText()));
					} else if (xpp.getName().equals("liftAvailability")) {
						stopArea.setLiftAvailable(ParserUtils.getBoolean(xpp
								.nextText()));
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}
			} else if (xpp.getName().equals("contains")) {
				String containsId = ParserUtils.getText(xpp.nextText());
				contains.add(containsId);
				validator.addContains(context, objectId, containsId);

			} else if (xpp.getName().equals("centroidOfArea")) {
				String value = ParserUtils.getText(xpp.nextText());
				validator.addAreaCentroidId(context, objectId, value);
				map.put(objectId, value);

			} else {
				XPPUtil.skipSubTree(log, xpp);
			}
		}
	}

	private void parseAreaCentroid(Context context, BiMap<String, String> map)
			throws Exception {
		XmlPullParser xpp = (XmlPullParser) context.get(PARSER);
		Referential referential = (Referential) context.get(REFERENTIAL);
		NeptuneObjectFactory factory =  (NeptuneObjectFactory) context.get(NEPTUNE_OBJECT_FACTORY);

		xpp.require(XmlPullParser.START_TAG, null, "AreaCentroid");
		int columnNumber =  xpp.getColumnNumber();
		int lineNumber =  xpp.getLineNumber();

		AreaCentroidValidator validator = (AreaCentroidValidator) ValidatorFactory.create(AreaCentroidValidator.class.getName(), context);

		BiMap<String, String> inverse = map.inverse();
		StopArea stopArea = null;
		AreaCentroid areaCentroid = null;
		String objectId = null;

		while (xpp.nextTag() == XmlPullParser.START_TAG) {
			if (xpp.getName().equals("objectId")) {
				objectId = ParserUtils.getText(xpp.nextText());
				areaCentroid = factory.getAreaCentroid(objectId);
				String areaId = inverse.get(objectId);
				validator.addLocation(context, objectId, lineNumber, columnNumber);
				stopArea = ObjectFactory.getStopArea(referential, areaId);
			} else if (xpp.getName().equals("name")) {
				areaCentroid.setName(ParserUtils.getText(xpp.nextText()));
				if (stopArea.getName() == null)
					stopArea.setName(areaCentroid.getName());
			} else if (xpp.getName().equals("comment")) {
				areaCentroid.setComment(ParserUtils.getText(xpp.nextText()));
				if (stopArea.getComment() == null)
					stopArea.setComment(areaCentroid.getComment());
			} else if (xpp.getName().equals("longLatType")) {
				stopArea.setLongLatType(ParserUtils.getEnum(
						LongLatTypeEnum.class, xpp.nextText()));
				validator.addLongLatType(context, objectId, stopArea.getLongLatType());
			} else if (xpp.getName().equals("latitude")) {
				stopArea.setLatitude(ParserUtils.getBigDecimal(xpp.nextText()));
			} else if (xpp.getName().equals("longitude")) {
				stopArea.setLongitude(ParserUtils.getBigDecimal(xpp.nextText()));
			} else if (xpp.getName().equals("containedIn")) {
				String containedIn = ParserUtils.getText(xpp.nextText());
				validator.addContainedIn(context, objectId, containedIn);
			} else if (xpp.getName().equals("address")) {

				while (xpp.nextTag() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("countryCode")) {
						stopArea.setCountryCode(ParserUtils.getText(xpp
								.nextText()));
					} else if (xpp.getName().equals("streetName")) {
						stopArea.setStreetName(ParserUtils.getText(xpp
								.nextText()));
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}
			} else if (xpp.getName().equals("projectedPoint")) {

				while (xpp.nextTag() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("X")) {
						BigDecimal value = ParserUtils.getBigDecimal(xpp
								.nextText());
						stopArea.setX(value);
					} else if (xpp.getName().equals("Y")) {
						BigDecimal value = ParserUtils.getBigDecimal(xpp
								.nextText());
						stopArea.setY(value);
					} else if (xpp.getName().equals("projectionType")) {
						String value = ParserUtils.getText(xpp.nextText());
						stopArea.setProjectionType(value);
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}
			} else {
				XPPUtil.skipSubTree(log, xpp);
			}
		}
	}

	protected void parseComment(String comment, StopArea area) {
		if (comment != null && comment.trim().startsWith("{") && comment.trim().endsWith("}")) {
			try {
				// parse json comment
				JSONObject json = new JSONObject(comment);
				area.setComment(json.optString(COMMENT, null));
				if (json.has(URL_REF))
				{
					try {
						new URL(json.getString(URL_REF));
						area.setUrl(json.getString(URL_REF));
					} catch (Exception e) {
						log.error("cannot parse url " + json.getString(URL_REF), e);
					}
				}
				if (json.has(TIME_ZONE))
				{
					try {
						TimeZone.getTimeZone(json.getString(TIME_ZONE));
						area.setTimeZone(json.getString(TIME_ZONE));
					} catch (Exception e) {
						log.error("cannot parse time_zone " + json.getString(TIME_ZONE), e);
					}
				}
			} catch (Exception e1) {
				log.warn("unparsable json : "+comment);
				area.setComment(comment);
			}
		} else {
			// normal comment
			area.setComment(comment);
		}
	}

	static {
		ParserFactory.register(ChouetteAreaParser.class.getName(),
				new ParserFactory() {
			private ChouetteAreaParser instance = new ChouetteAreaParser();

			@Override
			protected Parser create() {
				return instance;
			}
		});
	}
}