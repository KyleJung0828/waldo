package ca.ubc.cpsc210.waldo.waldowebservice;
import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Test;

import ca.ubc.cpsc210.waldo.waldowebservice.WaldoService;

public class TestClass {
	
	@Test
	public void testInitSession() throws JSONException {
		WaldoService ws = new WaldoService();
		assertTrue("hello".equals(ws.initSession("hello")));
	}
}
