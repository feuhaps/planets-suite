package eu.planets_project.TB.api.interfaces.model.finals;

import java.util.Iterator;

/**
 * @author alindley
 * 
 */
public interface PlanetsInstitutions {

	/**
	 * Fixed constants for InstitutionsID
	 */
	public int PLANETS_INSTITUTION_ARC = 0;

	public int PLANETS_INSTITUTION_BL = 1;

	// to be completed

	/**
	 * Fixed constants for partnerTypes
	 */
	public int PLANETS_TYPE_LIBRARY = 100;

	public int PLANETS_TYPE_ARCHIVE = 101;

	public int PLANETS_TYPE_UNIVERSITY = 102;

	public int PLANETS_TYPE_TECHNOLOGY = 103;

	public int PLANETS_TYPE_OTHER = 104;

	/**
	 * Matchin of Partnertypes
	 */
	public int PLANETS_TYPE_ARC = PLANETS_TYPE_TECHNOLOGY;

	public int PLANETS_TYPE_BL = PLANETS_TYPE_LIBRARY;

	// to be completed

	/**
	 * This method returns the institution's names. e.g.
	 * "PLANETS_INSTITUTION_ARC" (String)
	 * 
	 * @return
	 */
	public Iterator<String> getAllAvailableInstitutions();

	/**
	 * Returns the corresponding name for a given ID. e.g.
	 * "PLANETS_INSTITUTION_ARC"
	 * 
	 * @param iInstitutionID
	 * @return institution's name; null when not found
	 */
	public String getInstitutionsName(int iInstitutionID);

	/**
	 * Returns the institution's ID
	 * 
	 * @param sName
	 *            full Variable name. e.g. PLANETS_INSTITUTION_ARC
	 * @return institutionID; -1 when not found
	 */
	public int getInstitutionsID(String sName);

	/**
	 * 
	 * This returns the given (int) PartnerType (e.g. 100) for a given (int)
	 * InstitutionID (e.g. PLANETS_INSTITUTION_ARC) 100.."PLANETS_TYPE_LIBRARY"
	 * 101.."PLANETS_TYPE_ARCHIVE" 102.."PLANETS_TYPE_UNIVERSITY"
	 * 103.."LANETS_TYPE_TECHNOLOGY" 104.."PLANETS_TYPE_OTHER" -1..when ID not
	 * found
	 * 
	 * @param iInstitutionTypeID
	 * @return
	 */
	public int getInstitutionsParnterType(int iInstitutionID);

	/**
	 * 
	 * This returns the given (String) PartnerType (e.g. PLANETS_TYPE_LIBRARY)
	 * for a given (int) InstitutionID (e.g. PLANETS_INSTITUTION_ARC)
	 * 100.."PLANETS_TYPE_LIBRARY" 101.."PLANETS_TYPE_ARCHIVE"
	 * 102.."PLANETS_TYPE_UNIVERSITY" 103.."LANETS_TYPE_TECHNOLOGY"
	 * 104.."PLANETS_TYPE_OTHER" null..when ID not found
	 * 
	 * @param iInstitutionTypeID
	 * @return
	 */
	public String getInstitutionsPartnerTypeDescription(int iInstitutionID);

	/**
	 * This method returns the institution's names. e.g. "PLANETS_TYPE_LIBRARY"
	 * 
	 * @return
	 */
	public Iterator<String> getAllAvailablePartnerTypes();

	/**
	 * Returns the corresponding type name for a given ID. e.g.
	 * "PLANETS_TYPE_LIBRARY"
	 * 
	 * @param iPartnerTypeID
	 * @return
	 */
	public String getTypeNames(int iPartnerTypeID);

	/**
	 * Returns the PartnerType's ID
	 * 
	 * @param sName
	 *            full Variable name. e.g. PLANETS_TYPE_LIBRARY
	 * @return partnerTypeID;
	 */
	public int getTypeID(String sName);

}
