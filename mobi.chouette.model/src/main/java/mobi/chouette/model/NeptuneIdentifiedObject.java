/**
 * Projet CHOUETTE
 *
 * ce projet est sous license libre
 * voir LICENSE.txt pour plus de details
 *
 */
package mobi.chouette.model;

import java.util.Date;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * Abstract object used for all Identified Chouette Object
 * <p/>
 */
@SuppressWarnings("serial")
@Log4j
@MappedSuperclass
@EqualsAndHashCode(of = { "objectId" })
public abstract class NeptuneIdentifiedObject extends NeptuneObject {

	/**
	 * Neptune object id <br/>
	 * composed of 3 items separated by a colon
	 * <ol>
	 * <li>prefix : an alphanumerical value (underscore accepted)</li>
	 * <li>type : a camelcase name describing object type</li>
	 * <li>technical id: an alphanumerical value (underscore and minus accepted)
	 * </li>
	 * </ol>
	 * This data must be unique in dataset
	 * 
	 * @return The actual value
	 */
	@Getter
	@Column(name = "objectid", nullable = false, unique = true)
	private String objectId;

	public void setObjectId(String value) {
		objectId = dataBaseSizeProtectedValue(value, "objectId", log);
	}

	/**
	 * object version
	 * 
	 * @param objectVersion
	 *            New value
	 * @return The actual value
	 */
	@Getter
	@Setter
	@Column(name = "object_version")
	private Integer objectVersion = 1;

	/**
	 * creation time
	 * 
	 * @param creationTime
	 *            New value
	 * @return The actual value
	 */
	@Getter
	@Setter
	@Column(name = "creation_time")
	private Date creationTime = new Date();

	/**
	 * creator id
	 * 
	 * @param creatorId
	 *            New value
	 * @return The actual value
	 */
	@Getter
	@Setter
	@Column(name = "creator_id")
	private String creatorId;

	@Transient
	private String unsaved_name;

	/**
	 * virtual name for object without name attribute<br>
	 * use to maintain coherence for generic interfaces
	 * 
	 * @return The actual value
	 */
	public String getName() {
		return unsaved_name;
	}

	/**
	 * virtual name for object without name attribute<br>
	 * use to maintain coherence for generic interfaces
	 * 
	 * @param name
	 *            New value
	 */
	public void setName(String name) {
		this.unsaved_name = name;
	}

	/**
	 * to be overrided; facility to check registration number on any object
	 * 
	 * @return null : when object has no registration number
	 */
	public String getRegistrationNumber() {
		return null;
	}

	/**
	 * check if an objectId is conform to Trident
	 * 
	 * @param oid
	 *            objectId to check
	 * @return true if valid, false othewise
	 */
	public static boolean checkObjectId(String oid) {
		if (oid == null)
			return false;

		Pattern p = Pattern.compile("(\\w|_)+:\\w+:([0-9A-Za-z]|_|-)+");
		return p.matcher(oid).matches();
	}

	private String[] objectIdArray() {
		return objectId.split(":");
	}

	/**
	 * return prefix of objectId
	 * 
	 * @return String
	 */
	public String objectIdPrefix() {
		if (objectIdArray().length > 2) {
			return objectIdArray()[0].trim();
		} else
			return "";
	}

	/**
	 * return suffix of objectId
	 * 
	 * @return String
	 */
	public String objectIdSuffix() {
		if (objectIdArray().length > 2)
			return objectIdArray()[2].trim();
		else
			return "";
	}

}