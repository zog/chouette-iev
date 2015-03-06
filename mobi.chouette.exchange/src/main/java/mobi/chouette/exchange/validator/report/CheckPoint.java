package mobi.chouette.exchange.validator.report;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class CheckPoint {

	private static final int maxDetails = 50;

	public enum SEVERITY {
		WARNING, ERROR, IMPROVMENT
	};

	public enum RESULT {
		UNCHECK, OK, NOK
	};

	@XmlElement(name = "test_id",required=true)
	private String name;

	@XmlElement(name="level",required=true)
	private String phase;

	@XmlElement(name="object_type",required=true)
	private String target;

	@XmlElement(name = "rank",required=true)
	private String rank;

	@XmlElement(name = "severity",required=true)
	private SEVERITY severity;

	@XmlElement(name = "result",required=true)
	private RESULT state;

	@XmlElement(name = "error_count")
	private int detailCount = 0;

	@XmlElement(name = "error")
	private List<Detail> details = new ArrayList<Detail>();

	public CheckPoint(String name, RESULT state, SEVERITY severity)
	{
		this.name = name;
		this.severity = severity;
		this.state = state;

		String[] token = name.split("\\-");
		if (token.length == 4)
		{
			this.phase = token[0];
			this.target = token[2];
			this.rank = token[3];
		}
		else if (token.length == 3)
		{
			this.phase = token[0];
			this.target = token[1];
			this.rank = token[2];
		}
		else 
		{
			throw new IllegalArgumentException("invalid name "+name);
		}
	}

	public void addDetail(Detail item) 
	{
		if (detailCount < maxDetails) 
		{
			details.add(item);
		}
		detailCount++;

		state = RESULT.NOK;


	}
}
