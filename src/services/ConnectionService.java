package services;

public interface ConnectionService
{
	boolean[] getInputs() throws Exception;
	boolean[] getOutputs() throws Exception;
	void setOutput(int port, boolean value) throws Exception;
	void close();
}
