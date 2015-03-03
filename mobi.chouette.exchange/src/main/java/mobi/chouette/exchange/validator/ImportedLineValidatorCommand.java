/**
 * Projet CHOUETTE
 *
 * ce projet est sous license libre
 * voir LICENSE.txt pour plus de details
 *
 */

package mobi.chouette.exchange.validator;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Color;
import mobi.chouette.common.Constant;
import mobi.chouette.common.Context;
import mobi.chouette.common.chain.Command;
import mobi.chouette.common.chain.CommandFactory;
import mobi.chouette.exchange.report.LineInfo;
import mobi.chouette.exchange.report.LineInfo.LINE_STATE;
import mobi.chouette.exchange.report.LineStats;
import mobi.chouette.exchange.report.Report;
import mobi.chouette.model.Line;
import mobi.chouette.model.util.Referential;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 *
 */
@Log4j
public class ImportedLineValidatorCommand implements Command, Constant {
	public static final String COMMAND = "ImportedLineValidatorCommand";


	@Override
	public boolean execute(Context context) throws Exception {
		boolean result = ERROR;
		Monitor monitor = MonitorFactory.start(COMMAND);
		ValidationData data = (ValidationData) context.get(VALIDATION_DATA);
		InitialContext initialContext = (InitialContext) context.get(INITIAL_CONTEXT);

		try {
			Command lineValidatorCommand = CommandFactory.create(initialContext,
					LineValidatorCommand.class.getName());

            Referential referential = (Referential) context.get(REFERENTIAL);
            Referential cache = (Referential) context.get(CACHE);
            
            Line line = referential.getLines().values().iterator().next();
			
			ValidationDataCollector collector = new ValidationDataCollector();
			collector.collect(data, line, cache);

			lineValidatorCommand.execute(context);
			
			result = SUCCESS;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info(Color.MAGENTA + monitor.stop() + Color.NORMAL);
		}

		return result;
	}

	public static class DefaultCommandFactory extends CommandFactory {

		@Override
		protected Command create(InitialContext context) throws IOException {
			Command result = new ImportedLineValidatorCommand();
			return result;
		}
	}

	static {
		CommandFactory.factories.put(ImportedLineValidatorCommand.class.getName(), new DefaultCommandFactory());
	}

}
