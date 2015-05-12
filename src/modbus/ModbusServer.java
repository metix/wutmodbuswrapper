/**
 * @author Maximilian Etti
 */
package modbus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * this is only for testing....
 */
public class ModbusServer
{
	/* MODBUS frame max. size is 300 bytes, so 900 are enough for 3 frames in buffer */
	private final static int RECEIVE_BUFFER_SIZE = 900;
	private int port;
	private ServerSocket ssocket;

	public ModbusServer()
	{
		this(ModbusProtocol.MODBUS_DEFAULT_PORT);
	}

	public ModbusServer(int port)
	{
		this.port = port;
	}

	public void start() throws IOException
	{
		ssocket = new ServerSocket(port);
		ssocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);

		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for (; ; ) {
					try {
						Socket client = ssocket.accept();
						DataOutputStream os = new DataOutputStream(client.getOutputStream());
						os.writeShort(0x01);
						os.writeShort(0x00);
						os.writeShort(4);
						os.writeByte(0x01);
						os.writeByte(0x01);
						os.writeByte(0x01);
						os.writeByte(0x06);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}

	public void stop()
	{
		try {
			ssocket.close();
		} catch (IOException ignore) {
		}
	}
}
