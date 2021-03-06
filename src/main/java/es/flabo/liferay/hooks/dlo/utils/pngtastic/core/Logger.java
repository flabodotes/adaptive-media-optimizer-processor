package es.flabo.liferay.hooks.dlo.utils.pngtastic.core;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Custom logger because I want to have zero dependancies; perhaps to be replaced with java.util logging.
 *
 * @author rayvanderborght
 */
public class Logger {

	static final String NONE = "NONE";
	static final String DEBUG = "DEBUG";
	static final String INFO = "INFO";
	static final String ERROR = "ERROR";
	private static final List<String> LOG_LEVELS = Arrays.asList(NONE, DEBUG, INFO, ERROR);
	
	private static final Log LOGGER = LogFactoryUtil.getLog(Logger.class);

	private final String logLevel;

	/** */
	Logger(String logLevel) {
		this.logLevel = (logLevel == null || !LOG_LEVELS.contains(logLevel.toUpperCase()))
				? INFO : logLevel.toUpperCase();
	}

	/**
	 * Write debug messages.
	 * Takes a varags list of args so that string concatenation only happens if the logging level applies.
	 */
	public void debug(String message, Object... args) {
		if (DEBUG.equals(this.logLevel)) {
			//System.out.println(String.format(message, args));
			LOGGER.debug(String.format(message, args));
		}
	}

	/**
	 * Write info messages.
	 * Takes a varags list of args so that string concatenation only happens if the logging level applies.
	 */
	public void info(String message, Object... args) {
		if (DEBUG.equals(this.logLevel) || INFO.equals(this.logLevel)) {
			//System.out.println(String.format(message, args));
			LOGGER.info(String.format(message, args));
		}
	}

	/**
	 * Write error messages.
	 * Takes a varags list of args so that string concatenation only happens if the logging level applies.
	 */
	public void error(String message, Object... args) {
		if (!NONE.equals(this.logLevel)) {
			//System.out.println(String.format(message, args));
			LOGGER.error(String.format(message, args));
		}
	}
}
