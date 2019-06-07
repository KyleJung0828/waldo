package ca.ubc.cpsc210.waldo.exceptions;

/**
 * Represent a problem with Waldo Service
 * @author CPSC 210 Instructor
 *
 */

public class WaldoException extends RuntimeException {
	
	public WaldoException(String msg) {
		super(msg);
	}

}
