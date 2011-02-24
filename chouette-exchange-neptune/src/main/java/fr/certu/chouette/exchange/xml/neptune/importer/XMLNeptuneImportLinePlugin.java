/**
 * Projet CHOUETTE
 *
 * ce projet est sous license libre
 * voir LICENSE.txt pour plus de details
 *
 */
package fr.certu.chouette.exchange.xml.neptune.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.ValidationException;

import chouette.schema.ChouettePTNetworkTypeType;
import fr.certu.chouette.common.ChouetteException;
import fr.certu.chouette.exchange.xml.neptune.exception.ExchangeException;
import fr.certu.chouette.exchange.xml.neptune.report.NeptuneReport;
import fr.certu.chouette.exchange.xml.neptune.report.NeptuneReportItem;
import fr.certu.chouette.filter.DetailLevelEnum;
import fr.certu.chouette.model.neptune.Line;
import fr.certu.chouette.plugin.exchange.FormatDescription;
import fr.certu.chouette.plugin.exchange.IImportPlugin;
import fr.certu.chouette.plugin.exchange.ParameterDescription;
import fr.certu.chouette.plugin.exchange.ParameterValue;
import fr.certu.chouette.plugin.exchange.SimpleParameterValue;
import fr.certu.chouette.plugin.report.Report;
import fr.certu.chouette.plugin.report.ReportHolder;
import fr.certu.chouette.plugin.report.ReportItem;

public class XMLNeptuneImportLinePlugin implements IImportPlugin<Line> 
{

	private static final Logger logger = Logger.getLogger(XMLNeptuneImportLinePlugin.class);

	private FormatDescription description;

	private List<String> allowedExtensions = Arrays.asList(new String[]{"xml","zip"});

	@Getter @Setter private NeptuneConverter converter;

	/**
	 * 
	 */
	public XMLNeptuneImportLinePlugin()
	{
		description = new FormatDescription() ;
		description.setName("XMLNeptuneLine");
		List<ParameterDescription> params = new ArrayList<ParameterDescription>();
		ParameterDescription param1 = new ParameterDescription("xmlFile",ParameterDescription.TYPE.FILEPATH,false,true);
		param1.setAllowedExtensions(Arrays.asList(new String[]{"xml","zip"}));
		params.add(param1);
		ParameterDescription param2 = new ParameterDescription("validateXML",ParameterDescription.TYPE.BOOLEAN,false,"false");
		params.add(param2);
		description.setParameterDescriptions(params);		
	}

	/* (non-Javadoc)
	 * @see fr.certu.chouette.plugin.exchange.IExchangePlugin#getDescription()
	 */
	@Override
	public FormatDescription getDescription() 
	{
		return description;
	}

	/* (non-Javadoc)
	 * @see fr.certu.chouette.plugin.exchange.IImportPlugin#doImport(java.util.List, fr.certu.chouette.plugin.report.ReportHolder)
	 */
	@Override
	public List<Line> doImport(List<ParameterValue> parameters,ReportHolder reportContainer)
	throws ChouetteException 
	{
		String filePath = null;
		boolean validate = false;
		for (ParameterValue value : parameters) 
		{
			if (value instanceof SimpleParameterValue)
			{
				SimpleParameterValue svalue = (SimpleParameterValue) value;
				if (svalue.getName().equals("xmlFile"))
				{
					filePath = svalue.getFilepathValue();
				}
				if (svalue.getName().equals("validateXML"))
				{
					validate = svalue.getBooleanValue().booleanValue();
				}
			}
		}
		if (filePath == null) 
		{
			logger.error("missing argument xmlFile");
			throw new IllegalArgumentException("xmlFile required");
		}

		String extension = FilenameUtils.getExtension(filePath).toLowerCase();
		if (!allowedExtensions.contains(extension))
		{
			logger.error("invalid argument xmlFile "+filePath+", allowed extensions : "+Arrays.toString(allowedExtensions.toArray()));
			throw new IllegalArgumentException("invalid file type");
		}


		Report report = new NeptuneReport(NeptuneReport.KEY.IMPORT);
		report.setStatus(Report.STATE.OK);
		reportContainer.setReport(report);
		List<Line> lines = null ; 

		if (extension.equals("xml"))
		{
			// simple file processing
			logger.info("start import simple file "+filePath);
			Line line = processFileImport(filePath, validate, report);
			if (line != null) 
			{
				lines = new ArrayList<Line>();
				lines.add(line);
			}
		}
		else
		{
			// zip file processing
			logger.info("start import zip file "+filePath);
			lines = processZipImport(filePath, validate, report);
		}

		logger.info("import terminated");
		return lines;
	}

	private List<Line> processZipImport(String filePath, boolean validate,Report report) 
	{
		NeptuneFileReader reader = new NeptuneFileReader();
		ZipFile zip = null; 
		try 
		{
			zip = new ZipFile(filePath);
		}
		catch (IOException e) 
		{
			ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.FILE_ERROR,filePath,e.getLocalizedMessage());
			item.setStatus(Report.STATE.ERROR);
			report.addItem(item);
			report.setStatus(Report.STATE.FATAL);
			logger.error("zip import failed (cannot open zip)"+e.getLocalizedMessage());
			return null;
		}
		List<Line> lines = new ArrayList<Line>();

		for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) 
		{
			ZipEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (!FilenameUtils.getExtension(entryName).toLowerCase().equals("xml"))
			{
				ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.FILE_IGNORED,entryName);
				item.setStatus(Report.STATE.WARNING);
				report.addItem(item);
				report.updateStatus(Report.STATE.WARNING);
				logger.info("zip entry "+entryName+" bypassed ; not a XML file");
				continue;
			}
			logger.info("start import zip entry "+entryName);
			InputStream stream = null;
			try 
			{
				stream = zip.getInputStream(entry);
			} 
			catch (IOException e) 
			{
				ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.FILE_ERROR,entryName,e.getLocalizedMessage());
				item.setStatus(Report.STATE.ERROR);
				report.addItem(item);
				report.setStatus(Report.STATE.ERROR);
				logger.error("zip entry "+entryName+" import failed (get entry)"+e.getLocalizedMessage());
				continue;
			}
			ChouettePTNetworkTypeType rootObject = null;
			try
			{
				rootObject = reader.read(stream,entryName);
			}
			catch (Exception e) 
			{
				ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.FILE_ERROR,entryName,e.getLocalizedMessage());
				item.setStatus(Report.STATE.ERROR);
				report.addItem(item);
				report.setStatus(Report.STATE.ERROR);
				logger.error("zip entry "+entryName+" import failed (read XML)"+e.getLocalizedMessage());
				continue;
			}
			try 
			{
				Line line = processImport(rootObject,validate,report,entryName);
				
				if (line != null) 
					lines.add(line);
				else
					logger.error("zip entry "+entryName+" import failed (build model)");

			} 
			catch (ExchangeException e) 
			{
				ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.FILE_ERROR,entryName,e.getLocalizedMessage());
				item.setStatus(Report.STATE.ERROR);
				report.addItem(item);
				report.setStatus(Report.STATE.ERROR);
				logger.error("zip entry "+entryName+" import failed (convert to model)"+e.getLocalizedMessage());
				continue;
			}
			logger.info("zip entry imported");
		}

		if (lines.size() == 0)
		{
			report.setStatus(Report.STATE.FATAL);
			logger.error("zip import failed (no valid entry)");
			return null;
		}
		return lines;
	}

	/**
	 * @param filePath
	 * @param validate
	 * @param report
	 * @return
	 * @throws ExchangeException
	 */
	private Line processFileImport(String filePath, boolean validate, Report report) 
	throws ExchangeException 
			{
		ChouettePTNetworkTypeType rootObject = null;
		NeptuneFileReader reader = new NeptuneFileReader();
		try
		{
			rootObject = reader.read(filePath);
		}
		catch (Exception e) 
		{
			ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.FILE_ERROR,filePath,e.getLocalizedMessage());
			item.setStatus(Report.STATE.ERROR);
			report.addItem(item);
			report.setStatus(Report.STATE.FATAL);
			logger.error("import failed ((read XML)) "+e.getLocalizedMessage());
			return null;
		}
		Line line = processImport(rootObject,validate,report,filePath);
		if (line == null)
		{
			logger.error("import failed (build model)");
			report.setStatus(Report.STATE.FATAL);
		}
		return line;
	}

	/**
	 * @param rootObject
	 * @param validate
	 * @param report
	 * @param entryName
	 * @return
	 * @throws ExchangeException
	 */
	private Line processImport(ChouettePTNetworkTypeType rootObject, boolean validate,Report report,String entryName) throws ExchangeException 
	{

		if (validate)
		{
			try 
			{
				rootObject.validate();
			} 
			catch (ValidationException e) 
			{
				logger.error("import failed for "+entryName+" : Castor validation");
				ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.VALIDATION_ERROR,entryName);
				item.setStatus(Report.STATE.ERROR);
				report.addItem(item);
				Throwable t = e;
				while (t != null)
				{
					ReportItem subItem = new NeptuneReportItem(NeptuneReportItem.KEY.VALIDATION_CAUSE,t.getLocalizedMessage());
					subItem.setStatus(Report.STATE.ERROR);
					item.addItem(subItem);
					t = t.getCause();
				}
				return null;
			}
		}

		ModelAssembler modelAssembler = new ModelAssembler();

		Line line = converter.extractLine(rootObject);
		modelAssembler.setLine(line);
		modelAssembler.setRoutes(converter.extractRoutes(rootObject));
		modelAssembler.setCompanies(converter.extractCompanies(rootObject));
		modelAssembler.setPtNetwork(converter.extractPTNetwork(rootObject));
		modelAssembler.setJourneyPatterns(converter.extractJourneyPatterns(rootObject));
		modelAssembler.setPtLinks(converter.extractPTLinks(rootObject));
		modelAssembler.setVehicleJourneys(converter.extractVehicleJourneys(rootObject));
		modelAssembler.setStopPoints(converter.extractStopPoints(rootObject));
		modelAssembler.setStopAreas(converter.extractStopAreas(rootObject));
		modelAssembler.setAreaCentroids(converter.extractAreaCentroids(rootObject));
		modelAssembler.setConnectionLinks(converter.extractConnectionLinks(rootObject));
		modelAssembler.setTimetables(converter.extractTimetables(rootObject));

		modelAssembler.connect();

		line.expand(DetailLevelEnum.ALL_DEPENDENCIES);

		ReportItem item = new NeptuneReportItem(NeptuneReportItem.KEY.OK_LINE,entryName,line.getName());
		item.setStatus(Report.STATE.OK);
		report.addItem(item);

		rootObject.toString();

		return line;
	}


}
