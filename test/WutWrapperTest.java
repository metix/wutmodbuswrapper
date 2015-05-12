import org.junit.Before;
import org.junit.Test;
import services.ConnectionService;

import static org.junit.Assert.*;

public class WutWrapperTest
{
	WutWrapper wut;
	boolean inputs[];
	boolean outputs[];

	@Before
	public void setUp() throws Exception
	{
		inputs = new boolean[16];
		outputs = new boolean[16];

		wut = new WutWrapper(new ConnectionService() {
			@Override
			public boolean[] getInputs() throws Exception
			{
				return inputs;
			}

			@Override
			public boolean[] getOutputs() throws Exception
			{
				return outputs;
			}

			@Override
			public void setOutput(int port, boolean value) throws Exception
			{
				outputs[port] = value;
			}

			@Override
			public void close()
			{

			}
		});

	}

	@Test
	public void testInputPortsRequest() throws Exception
	{
		inputs[0] = true;
		assertEquals("input=0001", wut.doRequest("GET /input?"));
		inputs[1] = true;
		assertEquals("input=0003", wut.doRequest("GET /input?"));
		inputs[15] = true;
		assertEquals("input=8003", wut.doRequest("GET /input?"));
	}

	@Test
	public void testOutputPortsRequest() throws Exception
	{
		outputs[0] = true;
		assertEquals("output=0001", wut.doRequest("GET /output?"));
		outputs[1] = true;
		assertEquals("output=0003", wut.doRequest("GET /output?"));
		outputs[15] = true;
		assertEquals("output=8003", wut.doRequest("GET /output?"));
	}

	@Test
	public void testSetOutputPortRequest() throws Exception
	{
		assertFalse(outputs[0]);
		wut.doRequest("GET /outputaccess0?State=ON");
		assertTrue(outputs[0]);
		wut.doRequest("GET /outputaccess0?State=OFF");
		assertFalse(outputs[0]);

		assertFalse(outputs[15]);
		wut.doRequest("GET /outputaccess15?State=ON");
		assertTrue(outputs[15]);
		wut.doRequest("GET /outputaccess15?State=OFF");
		assertFalse(outputs[15]);
	}
}