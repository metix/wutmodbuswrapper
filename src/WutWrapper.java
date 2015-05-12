import services.ConnectionService;

/**
 *
 */
public class WutWrapper
{
	ConnectionService service;

	public WutWrapper(ConnectionService service)
	{
		this.service = service;
	}

	public String doRequest(String req) throws Exception
	{
		String response = null;

		// parse request
		if (req.startsWith("GET /input?"))
			response = getInputs();
		else if (req.startsWith("GET /output?"))
			response = getOutputs();
		else if (req.startsWith("GET /outputaccess"))
			writeOutput(req);
		else
			throw new NoSuchMethodError(req);

		return response != null ? response : null;
	}

	String getInputs() throws Exception
	{
		boolean[] values = service.getInputs();
		String binaryInputString = "";

		// convert boolean array to a 4 character hex-string
		for (boolean value : values) binaryInputString = (value ? "1" : "0") + binaryInputString;

		int inputInteger = Integer.parseInt(binaryInputString, 2);

		return "input=" + String.format("%04X", inputInteger);
	}

	String getOutputs() throws Exception
	{
		boolean[] values = service.getOutputs();
		String binaryOutputString = "";

		// convert boolean array to a 4 character hex-string
		for (boolean value : values) binaryOutputString = (value ? "1" : "0") + binaryOutputString;

		int outputInteger = Integer.parseInt(binaryOutputString, 2);

		return "output=" + String.format("%04X", outputInteger);
	}

	void writeOutput(String req) throws Exception
	{
		// parse port
		int port = Integer.parseInt(req.substring("GET /outputaccess".length(), req.indexOf("?")));

		// parse value
		boolean value;
		if (req.contains("State=ON"))
			value = true;
		else if (req.contains("State=OFF"))
			value = false;
		else {
			System.out.println("writeOutput: wrong State");
			return;
		}

		// write to the connection-service
		service.setOutput(port, value);
	}

	public void shutdown()
	{
		service.close();
	}
}