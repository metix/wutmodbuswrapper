package services;

import java.io.IOException;
import java.util.BitSet;

import modbus.*;

public class ModbusConnectionService implements ConnectionService
{
	ModbusClient client;
	int input_count = 16;
	int output_count = 16;

	public ModbusConnectionService(String host) throws IOException
	{
		client = new ModbusClient(host);
		client.connect();
	}

	@Override
	public boolean[] getInputs() throws Exception
	{
		BitSet set = client.readInputs(0, input_count);

		boolean values[] = new boolean[input_count];

		for (int i = 0; i < input_count; i++)
			values[i] = set.get(i);

		return values;
	}

	@Override
	public boolean[] getOutputs() throws Exception
	{
		BitSet set = client.readOutputs(0, output_count);

		boolean values[] = new boolean[output_count];

		for (int i = 0; i < output_count; i++)
			values[i] = set.get(i);

		return values;
	}

	@Override
	public void setOutput(int port, boolean value) throws Exception
	{
		client.writeOutput(port, value);
	}

	@Override
	public void close()
	{
		client.disconnect();
	}
}
