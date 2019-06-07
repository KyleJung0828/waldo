package ca.ubc.cpsc210.waldo.exceptions;

/**
 * Represent a problem with a Bus Stop representation
 * @author CPSC 210 Instructor
 *
 */

public class IllegalBusStopException extends RuntimeException {

	public IllegalBusStopException(String msg) {
		super(msg);
	}
}
