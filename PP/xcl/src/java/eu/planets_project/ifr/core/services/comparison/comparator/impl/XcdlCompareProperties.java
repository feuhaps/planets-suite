package eu.planets_project.ifr.core.services.comparison.comparator.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.jws.WebService;

import eu.planets_project.ifr.core.services.characterisation.extractor.xcdl.XcdlCreator;
import eu.planets_project.ifr.core.services.characterisation.extractor.xcdl.XcdlParser;
import eu.planets_project.ifr.core.services.comparison.comparator.config.ComparatorConfigCreator;
import eu.planets_project.ifr.core.services.comparison.comparator.config.ComparatorConfigParser;
import eu.planets_project.services.PlanetsServices;
import eu.planets_project.services.compare.CompareProperties;
import eu.planets_project.services.compare.CompareResult;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.Prop;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.ServiceReport;
import eu.planets_project.services.utils.FileUtils;

/**
 * XCDL Comparator service. Compares lists of properties.
 * <p/>
 * Note: this is work in progress.
 * @see XcdlComparePropertiesTests
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */
@WebService(
        name = XcdlCompareProperties.NAME, 
        serviceName = CompareProperties.NAME, 
        targetNamespace = PlanetsServices.NS, 
        endpointInterface = "eu.planets_project.services.compare.CompareProperties")
@Stateless
public final class XcdlCompareProperties implements CompareProperties {
    /***/
    static final String NAME = "XcdlCompareProperties";

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.compare.CompareProperties#compare(java.util.List,
     *      java.util.List)
     */
    public CompareResult compare(final List<ArrayList<Property>> lists,
            final List<Prop<Object>> config) {
        if (lists.size() < 2) {
            throw new IllegalArgumentException(
                    "Need at least two lists to compare");
        }
        List<ArrayList<Property>> first = new ArrayList<ArrayList<Property>>();
        first.add(lists.get(0));
        String xcdl = read(first).get(0);
        List<String> xcdls = read(lists.subList(1, lists.size()));
        String pcr = new ComparatorConfigCreator(config)
                .getComparatorConfigXml();
        String result = ComparatorWrapper.compare(xcdl, xcdls, pcr);
        List<Property> props = propertiesFrom(result);
        return new CompareResult(props, new ServiceReport());
    }

    /**
     * @param result The comparator result
     * @return The properties found in the result XML
     */
    private List<Property> propertiesFrom(final String result) {
        File file = FileUtils.writeByteArrayToTempFile(result.getBytes());
        return new ResultPropertiesReader(file).getProperties();
    }

    /**
     * @param list The list of digital objects
     * @return A list of strings representing the content of the digital objects
     */
    private List<String> read(final List<ArrayList<Property>> list) {
        List<String> result = new ArrayList<String>();
        for (List<Property> xcdlProps : list) {
            String content = new XcdlCreator(xcdlProps).getXcdlXml();
            result.add(content);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.compare.CompareProperties#describe()
     */
    public ServiceDescription describe() {
        return new ServiceDescription.Builder(this.NAME, CompareProperties.class.getCanonicalName())
        		.classname(this.getClass().getCanonicalName())
                .author("Fabian Steeg")
                .description("This services is a wrapper for the Comparator command line tool developed at the UzK." +
                		"It compares Property lists instead of .xcdl files. This way, it is possible" +
                		"to use the Comparator to compare the output of any Characterisation tool that could be" +
                		"converted to (or that is delivered as) a list of properties." + 
                		"To enable this, a xcdl file is created on the fly for each list of" + 
                		"properties and is used as input for the Comparator command line tool." + 
                		"IMPORTANT NOTE: To use .xcdl files directly, please use the XcdlCompare service!")
                .serviceProvider("The Planets Consortium").build();
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.compare.CompareProperties#convertInput(eu.planets_project.services.datatypes.DigitalObject)
     */
    public ArrayList<Property> convertInput(final DigitalObject inputFile) {
        File file = FileUtils.writeByteArrayToTempFile(FileUtils
                .writeInputStreamToBinary(inputFile.getContent().read()));
        List<Property> props = new XcdlParser(file).getProperties();
        if (props.size() == 0) {
            throw new IllegalStateException(
                    "Could not parse any properties from: "
                            + file.getAbsolutePath());
        }
        ArrayList<Property> list = new ArrayList<Property>(props);
        return list;
    }

    /**
     * {@inheritDoc}
     * @see eu.planets_project.services.compare.Compare#convert(eu.planets_project.services.datatypes.DigitalObject)
     */
    public List<Prop<Object>> convertConfig(final DigitalObject configFile) {
        File file = FileUtils.writeByteArrayToTempFile(FileUtils
                .writeInputStreamToBinary(configFile.getContent().read()));
        return new ComparatorConfigParser(file).getProperties();
    }

}
