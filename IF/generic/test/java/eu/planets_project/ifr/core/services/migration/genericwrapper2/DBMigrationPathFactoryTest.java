package eu.planets_project.ifr.core.services.migration.genericwrapper2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import eu.planets_project.ifr.core.services.migration.genericwrapper2.exceptions.MigrationException;
import eu.planets_project.ifr.core.services.migration.genericwrapper2.utils.DocumentLocator;
import eu.planets_project.services.datatypes.Parameter;

/**
 * Test of the docment based migration path factory.
 * 
 * @author Thomas Skou Hansen &lt;tsh@statsbiblioteket.dk&gt;
 */
public class DBMigrationPathFactoryTest {

    private static final String TEST_CONFIG_FILE = "GenericWrapperConfigFileExample.xml";

    private MigrationPathFactory migrationPathFactory;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
	DocumentLocator documentLocator = new DocumentLocator(TEST_CONFIG_FILE);
	Document testConfiguration = documentLocator.getDocument();
	migrationPathFactory = new DBMigrationPathFactory(testConfiguration);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    // TODO: Do also test a series of broken configuration files.

    /**
     * Test method for
     * {@link eu.planets_project.ifr.core.services.migration.genericwrapper2.DBMigrationPathFactory#getAllMigrationPaths(org.w3c.dom.Document)}
     * . Test on valid configuration file.
     * 
     * This test verifies that the factory produces the correct number of
     * migration paths from the test configuration file and fetches a specific
     * migration path from it to verify that all its information is correct.
     */
    @Test
    public void testGetMigrationPaths() throws Exception {

	// TODO: This test has become pretty bloated and ugly. It needs a
	// makeover.
	MigrationPaths migrationPaths = migrationPathFactory
		.getAllMigrationPaths();
	assertNotNull(migrationPaths);

	// Verify the number of paths.
	assertEquals("The factory returned a wrong number of migration paths.",
		14, migrationPaths.getAllMigrationPaths().size());

	// Verify a specific migration path
	final URI inputFormatURI = new URI("info:test/lowercase");
	final URI outputFormatURI = new URI("info:test/uppercase");

	MigrationPath migrationPath = migrationPaths.getMigrationPath(
		inputFormatURI, outputFormatURI);

	assertNotNull("Failed getting a migration path for migration from "
		+ "'" + inputFormatURI + "' to '" + outputFormatURI + "'",
		migrationPath);

	// Verify the command line information
	List<String> expectedCommandFragments = new ArrayList<String>();
	expectedCommandFragments.add("/bin/sh");
	expectedCommandFragments.add("-c");
	expectedCommandFragments
		.add("cat #param1 #tempSource > #myInterimFile && tr #param2 #myInterimFile > #tempDestination");
	commandLineTest(migrationPath, expectedCommandFragments);

	// Verify the tool input configuration.
	final ToolIOProfile toolInputProfile = migrationPath
		.getToolInputProfile();
	assertNotNull(
		"The tool input profile is missing in the migration path.",
		toolInputProfile);
	assertFalse(
		"Expected input via temporary file and not via standard input.",
		toolInputProfile.usePipedIO());

	assertEquals("tempSource", toolInputProfile.getCommandLineFileLabel());
	assertEquals("desiredInputFileName", toolInputProfile
		.getDesiredTempFileName());

	// Verify the tool output configuration.
	final ToolIOProfile toolOutputProfile = migrationPath
		.getToolOutputProfile();
	assertNotNull(
		"The tool input profile is missing in the migration path.",
		toolOutputProfile);
	assertFalse(
		"Expected output via temporary file and not via standard input.",
		toolOutputProfile.usePipedIO());

	assertEquals("tempDestination", toolOutputProfile
		.getCommandLineFileLabel());
	assertNull(toolOutputProfile.getDesiredTempFileName());

	// Verify the temporary file information
	Map<String, String> tempFileMappings = migrationPath
		.getTempFileDeclarations();
	assertEquals(
		"Unexpected number of temporary files defined for migration path.",
		1, tempFileMappings.size());

	final String expectedLabel = "myInterimFile";
	final String expectedFileName = "myDesiredTempFileName.foo";
	assertEquals("Unexpected label name in temp. file mapping.",
		expectedLabel, tempFileMappings.keySet().iterator().next());
	assertEquals("Unexpected temp. file name associated with label: "
		+ expectedLabel, expectedFileName, tempFileMappings
		.get(expectedLabel));

	// Verify the tool parameters.
	final Collection<Parameter> toolParameters = migrationPath
		.getToolParameters();
	assertEquals(
		"Unexpected number of parameters defined for the migration path.",
		2, toolParameters.size());
	final HashMap<String, Parameter> parameterMap = new HashMap<String, Parameter>();
	for (Parameter parameter : toolParameters) {
	    parameterMap.put(parameter.getName(), parameter);
	}

	final String[] expectedParameterNames = { "param1", "param2" };
	for (String parameterName : expectedParameterNames) {
	    final Parameter currentParameter = parameterMap.get(parameterName);
	    assertNotNull("The parameter '" + parameterName
		    + "' was not defined for the migration path.",
		    currentParameter);

	    assertNotNull("No description specified for parameter: "
		    + parameterName, currentParameter.getDescription());
	    assertFalse("Empty description for parameter: " + parameterName,
		    currentParameter.getDescription().length() == 0);
	}

	// Verify the tool presets.
	ToolPresets toolPresets = migrationPath.getToolPresets();
	assertEquals("The tool presets has a wrong default preset ID", "mode",
		toolPresets.getDefaultPresetID());

	Collection<Preset> presets = toolPresets.getAllToolPresets();
	assertEquals(
		"Un-expected number of presets defined for the tested migration path.",
		2, presets.size());

	final HashMap<String, Preset> presetMap = new HashMap<String, Preset>();
	for (Preset preset : presets) {
	    presetMap.put(preset.getName(), preset);
	}

	final HashMap<String, String[]> expectedPresetSettingNameMap = new HashMap<String, String[]>();
	expectedPresetSettingNameMap.put("mode", new String[] { "complete",
		"AC-DC", "extra" });
	expectedPresetSettingNameMap.put("quality", new String[] { "good",
		"better", "best" });

	for (String presetName : expectedPresetSettingNameMap.keySet()) {
	    final Preset currentPreset = presetMap.get(presetName);
	    assertNotNull("The preset '" + presetName
		    + "' was not defined for the migration path.",
		    currentPreset);

	    assertNotNull("No description specified for preset: " + presetName,
		    currentPreset.getDescription());
	    assertFalse("Empty description for preset: " + presetName,
		    currentPreset.getDescription().length() == 0);

	    assertNotNull("The preset '" + currentPreset.getName()
		    + "' has no default setting.", currentPreset
		    .getDefaultSetting().getDescription());

	    assertEquals("Un-expected number of settings defined for the '"
		    + currentPreset.getName() + "' preset.", 3, currentPreset
		    .getAllSettings().size());

	    // Check the setting names for the current preset.
	    for (String settingName : expectedPresetSettingNameMap
		    .get(presetName)) {
		final PresetSetting currentPresetSetting = currentPreset
			.getSetting(settingName);
		assertNotNull(
			"The '" + settingName + "' is undefined for the '"
				+ presetName + "' preset.",
			currentPresetSetting);

		assertNotNull("The '" + settingName + "' in the '" + presetName
			+ "' preset has no description.", currentPresetSetting
			.getDescription());

		final ArrayList<String> settingParameterNames = new ArrayList<String>();
		for (Parameter settingParameter : currentPresetSetting
			.getParameters()) {
		    settingParameterNames.add(settingParameter.getName());
		}

		for (String parameterName : expectedParameterNames) {
		    assertNotNull("The '" + parameterName
			    + "' is not defined in the '" + settingName
			    + "' setting of the '" + presetName + "' preset.",
			    settingParameterNames.contains(parameterName));
		}
	    }
	}
    }

    /**
     * FIXME! Revisit documentation...
     * 
     * Verify that the unprocessed command line from <code>migrationPath</code>
     * is correct. Note that this only verifies the unprocessed command line and
     * not any keyword and variable substitutions as this is not the
     * responsibility of the migration path factory.
     * 
     * @param migrationPath
     *            Migration path to test the command line for.
     * @param expectedCommandFragments
     *            A list of the expected command and associated parameters to
     *            use for the test.
     * @throws MigrationException
     */
    private void commandLineTest(MigrationPath migrationPath,
	    List<String> expectedCommandLine) throws MigrationException {

	final ArrayList<Parameter> emptyParameterList = new ArrayList<Parameter>();
	final HashMap<String, File> emptyFileDeclarations = new HashMap<String, File>();
	final PRCommandBuilder commandBuilder = new PRCommandBuilder();
	List<String> prCommand = commandBuilder.buildCommand(migrationPath, emptyParameterList, emptyFileDeclarations);

	assertEquals(
		"Unexpected number of command line fragments in the migration"
			+ " path object", expectedCommandLine.size(), prCommand
			.size());

	for (int fragmentIdx = 0; fragmentIdx < expectedCommandLine.size(); fragmentIdx++) {
	    final String expectedFragment = expectedCommandLine
		    .get(fragmentIdx);
	    final String actualFragment = prCommand.get(fragmentIdx);
	    assertEquals(
		    "Unexpected command line fragment in the migration path.",
		    expectedFragment, actualFragment);
	}
    }
}
