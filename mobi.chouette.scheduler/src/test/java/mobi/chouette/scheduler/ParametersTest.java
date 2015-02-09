package mobi.chouette.scheduler;

import java.io.File;
import java.nio.file.Files;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.JSONUtils;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

@Log4j
public class ParametersTest {

	@Test
	public void test() throws Exception {
		
		BasicConfigurator.configure();
		
		String filename = "/home/dsuru/workspace-chouette/chouette/mobi.chouette.scheduler/src/test/resources/parameters.json";
		File f = new File(filename);
		byte[] bytes = Files.readAllBytes(f.toPath());
		String text = new String(bytes, "UTF-8");
		Parameters payload = (Parameters) JSONUtils.fromJSON(text, Parameters.class);
		log.info("ParametersTest.test() : \n" + payload.toString());

		String result = JSONUtils.toJSON(payload);
		log.info("ParametersTest.test() : \n" + result);
	}

}