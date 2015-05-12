/**
 * @author Maximilian Etti
 */
package modbus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * ModbusClient implements a TCP Client for the Modbus Protocol.
 */
public class ModbusClient
{
	private Socket socket;
	private OutputStream os;
	private InputStream is;
	private String host;
	private int port;
	private short current_transaction_ident;

	/* Modbus frame max. size is 300 bytes, so 900 are enough for 3 frames in buffer */
	private final static int TCP_RECEIVE_BUFFER_SIZE = 900;

	/**
	 * Create a new Instance of a TCP Modbus Client, which can connect to a TCP Modbus Server .
	 * Uses the default port 502 for the communication.
	 * <p>
	 * start the connection by calling connect()
	 *
	 * @param host server host (ip or hostname)
	 */
	public ModbusClient(String host)
	{
		this(host, ModbusProtocol.MODBUS_DEFAULT_PORT);
	}

	/**
	 * Create a new Instance of a TCP Modbus Client, which can connect to a TCP Modbus Server .
	 * Uses a custom port for communication
	 * <p>
	 * start the connection by calling connect()
	 *
	 * @param host server host (ip or hostname)
	 * @param port port for the connection.modbus communication
	 */
	public ModbusClient(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	/**
	 * Establish a connection to the Modbus master.
	 *
	 * @throws IOException when a problem with TCP occured or when client is already connected
	 */
	public void connect() throws IOException
	{
		if (socket != null && socket.isConnected())
			throw new IOException("already connected. please close connection first.");
		socket = new Socket(host, port);

		// disable nagle's algorithm (send small amount of data directly)
		// this increases the performance
		socket.setTcpNoDelay(false);
		socket.setReceiveBufferSize(TCP_RECEIVE_BUFFER_SIZE);

		// enable keep-alive so that connection crashes can be detected faster
		socket.setKeepAlive(true);

		os = socket.getOutputStream();
		is = socket.getInputStream();
	}

	/**
	 * Closes the connection.
	 * No error will be thrown when connection is already closed.
	 */
	public void disconnect()
	{
		try {
			socket.close();
		} catch (IOException ignore) {
		}
	}

	/**
	 * Read {@code n} output bits beginning at {@code start} from remote device.
	 * <p>
	 * {@code start} must be between 0x0000 and 0xFFFF.
	 * {@code n} must be between 1 and 2000.
	 * <p>
	 * This function wraps the MODBUS function 0x01
	 *
	 * @param start address of the first bit (maybe 'offset' is a better word)
	 * @param n     how many bits should be read
	 * @return bit set, which contains the state of the bits
	 */
	public BitSet readOutputs(int start, int n) throws IOException, ModbusException
	{
		ModbusMessage req = new ModbusMessage(ModbusProtocol.F_READ_COILS);
		req.addDataShort((short) start);
		req.addDataShort((short) n);

		ModbusMessage res = sendRequest(req);

		int byte_count = res.getDataByte();
		return BitSet.valueOf(res.getDataBytes(byte_count));
	}

	/**
	 * Read single output port
	 *
	 * @param n output port
	 * @return true if voltage is high, false if voltage is low
	 * @throws IOException
	 * @throws ModbusException
	 */
	public boolean readOutput(int n) throws IOException, ModbusException
	{
		BitSet set = readOutputs(n, 1);
		return set.get(0);
	}

	/**
	 * Read {@code n} input bits beginning at {@code start} from remote device.
	 * <p>
	 * {@code start} must be between 0x0000 and 0xFFFF.
	 * {@code n} must be between 1 and 2000.
	 * <p>
	 * This function wraps the MODBUS function 0x01
	 *
	 * @param start address of the first bit (maybe 'offset' is a better word)
	 * @param n     how many bits should be read
	 * @return bit set, which contains the state of the bits
	 */
	public BitSet readInputs(int start, int n) throws IOException, ModbusException
	{
		ModbusMessage req = new ModbusMessage(ModbusProtocol.F_READ_DISCRETE_INPUTS);
		req.addDataShort((short) start);
		req.addDataShort((short) n);

		ModbusMessage res = sendRequest(req);

		int byte_count = res.getDataByte();
		return BitSet.valueOf(res.getDataBytes(byte_count));
	}

	/**
	 * Read single input port
	 *
	 * @param n input port
	 * @return true if voltage is high, false if voltage is low
	 * @throws IOException
	 * @throws ModbusException
	 */
	public boolean readInput(int n) throws IOException, ModbusException
	{
		BitSet set = readInputs(n, 1);
		return set.get(0);
	}

	/**
	 * Enable or disable output bit {@code addr}
	 * <p>
	 * This function wraps the MODBUS function 0x05
	 * <p>
	 * {@code addr} address must be between 0x0000 and 0xFFFF
	 *
	 * @param addr  the output address
	 * @param value true -> enable the output, false -> disable the output
	 * @throws IOException
	 */
	public void writeOutput(int addr, boolean value) throws IOException, ModbusException
	{
		ModbusMessage req = new ModbusMessage(ModbusProtocol.F_WRITE_SINGLE_COIL);
		req.addDataShort((short) addr);
		req.addDataShort((short) (value ? 0xff00 : 0x0000));

		sendRequest(req);
	}

	private synchronized ModbusMessage sendRequest(ModbusMessage req) throws IOException, ModbusException
	{
		// increase transaction id and avoid invalid zero
		if (++current_transaction_ident == 0)
			current_transaction_ident = 1;

		// set header fields
		req.setTransactionIdentifier(current_transaction_ident);

		// send request
		os.write(req.getBytes());

		// receive and parse response
		ModbusMessage res = receiveResponse();

		// check if response is valid
		if (req.getTransactionIdentifier() != res.getTransactionIdentifier())
			throw new ModbusException("connection.modbus-header: transaction_ident from request differs from response");

		if (req.getFunctionCode() != res.getFunctionCode()) {
			// check if error is transmitted
			if (req.getFunctionCode() + 0x80 == res.getFunctionCode())
				throw new ModbusException(res.getFunctionCode(), res.getDataByte());

			// well okay, then there is some other problem
			throw new ModbusException("connection.modbus-header: unexpected function code 0x" + Integer.toHexString(res.getFunctionCode()));
		}

		if (res.getProtocolIdentifier() != ModbusProtocol.MODBUS_PROTOCOL_IDENT)
			throw new ModbusException("connection.modbus-header: protocol_ident should be zero");

		return res;
	}

	private ModbusMessage receiveResponse() throws IOException
	{
		// read header
		ByteBuffer buff = ByteBuffer.allocate(ModbusProtocol.MODBUS_MAX_SEGMENT_SIZE);
		int read = 0;
		while (read < ModbusProtocol.MBAP_HEADER_LENGTH)
			read += is.read(buff.array(), read, ModbusProtocol.MBAP_HEADER_LENGTH - read);

		// get the length of the data from the 'length' field in the header
		short data_length = (short) (buff.getShort(4) - 2);

		// read all data bytes
		read = 0;
		while (read < data_length)
			read += is.read(buff.array(), read + ModbusProtocol.MBAP_HEADER_LENGTH, data_length - read);

		return new ModbusMessage(buff);
	}
}
