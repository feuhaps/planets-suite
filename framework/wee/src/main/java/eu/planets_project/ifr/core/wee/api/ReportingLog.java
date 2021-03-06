/***/
package eu.planets_project.ifr.core.wee.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;

import eu.planets_project.services.datatypes.Parameter;

/**
 * A reporting logger that builds up a HTML report from the log messages and
 * forwards the messages to a backing normal logger.
 * @see ReportingLogTests
 * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
 */
public final class ReportingLog implements Log {

    private Logger backingLog;
    private WorkflowReporter reporter;
    private final File logFile;

    /**
     * Enum for the different possible levels of log messages.
     * @author Fabian Steeg (fsteeg)
     */
    enum Level {
        INFO("#CCFF99"), DEBUG("#F0F0F0"), TRACE("#FFFFFF"), FATAL("#CC3333"), ERROR("#FFCC66"), WARN("#99CCFF");
        String color;

        private Level(final String color) {
            this.color = color;
        }
    }

    /**
     * A message to pass to the reporting log. Will be added to the report.
     * @author Fabian Steeg (fabian.steeg@uni-koeln.de)
     */
    public static final class Message {

        String title;
        Parameter[] values;

        /**
         * @param title The message title
         * @param values The key-value message attributes
         */
        public Message(String title, Parameter... values) {
            this.title = title;
            this.values = values;
        }

        /**
         * {@inheritDoc}
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s: %s", title, Arrays.asList(values));
        }
    }

    /**
     * @param backingLog The backing log
     */
    public ReportingLog(final Logger backingLog) {
        this(backingLog,null);
    }
    
    /**
     * The backing log - allows to hand over the workflow's uuid to identify the output folder
     * @param backingLog
     * @param workflowUUID
     */
    public ReportingLog(final Logger backingLog, String workflowUUID) {
    	this.backingLog = backingLog;
        if(workflowUUID!=null){
        	//check if we've got a workflowUUID to init the reporter
        	this.reporter = new WorkflowReporter(workflowUUID);
        }
        else{
        	//else: use the default init
        	this.reporter = new WorkflowReporter();
        }
        this.logFile = new File(reporter.reportOutputFolder, "wf-log.txt");
        try {
            backingLog.addAppender(new FileAppender(new SimpleLayout(), logFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The report assembled during logging
     */
    public String reportAsString() {
        return reporter.reportAsString();
    }

    /**
     * @return The file the HTML report has been written to
     */
    public File reportAsFile() {
        return reporter.reportAsFile();
    }

    /**
     * @return The file the logging messages have been written to
     */
    public File logAsFile() {
        return logFile;
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug(Object message) {
        reporter.reportIfStructured(message, Level.DEBUG, null);
        backingLog.debug(message);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#debug(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void debug(Object message, Throwable t) {
        reporter.reportIfStructured(message, Level.DEBUG, t);
        backingLog.debug(message, t);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error(Object message) {
        reporter.reportIfStructured(message, Level.ERROR, null);
        backingLog.error(message);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#error(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void error(Object message, Throwable t) {
        reporter.reportIfStructured(message, Level.ERROR, t);
        backingLog.error(message, t);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal(Object message) {
        reporter.reportIfStructured(message, Level.FATAL, null);
        backingLog.fatal(message);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void fatal(Object message, Throwable t) {
        reporter.reportIfStructured(message, Level.FATAL, t);
        backingLog.fatal(message, t);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info(Object message) {
        reporter.reportIfStructured(message, Level.INFO, null);
        backingLog.info(message);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#info(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void info(Object message, Throwable t) {
        reporter.reportIfStructured(message, Level.INFO, t);
        backingLog.info(message, t);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace(Object message) {
        reporter.reportIfStructured(message, Level.TRACE, null);
        backingLog.trace(message);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#trace(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void trace(Object message, Throwable t) {
        reporter.reportIfStructured(message, Level.TRACE, t);
        backingLog.trace(message, t);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn(Object message) {
        reporter.reportIfStructured(message, Level.WARN, null);
        backingLog.warn(message);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#warn(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void warn(Object message, Throwable t) {
        reporter.reportIfStructured(message, Level.WARN, t);
        backingLog.warn(message, t);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return backingLog.isDebugEnabled();
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return backingLog.isEnabledFor(Priority.ERROR);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return backingLog.isEnabledFor(Priority.FATAL);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return backingLog.isInfoEnabled();
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return backingLog.isTraceEnabled();
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return backingLog.isEnabledFor(Priority.WARN);
    }

    /**
     * @return The folder the reporting log writes the report and the log to
     */
    public File getOutputFolder() {
        return new File(reporter.reportOutputFolder);
    }
    
    /**
     * @see ReportingLog#getResultsId()
     */
    @Deprecated
	public String getTime() {
    	return reporter.getTime();
    }
    
    public String getResultsId(){
    	return reporter.getResultsId();
    }
}
