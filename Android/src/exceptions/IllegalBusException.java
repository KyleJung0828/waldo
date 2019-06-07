package ca.ubc.cpsc210.waldo.exceptions;

/**
 * Represent a problem with a Bus definition
 * @author CPSC 210 Instructor
 *
 */

public class IllegalBusException extends RuntimeException {
	
	public IllegalBusException(String msg) {
		super(msg);
	}

}
