import services.ConnectionService;
import services.ModbusConnectionService;
import services.VirtualConnectionService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WutWrapperServer
{
	static boolean virtual_mode;

	private ServerSocket ssocket;
	private WutWrapper wut;

	class ClientHandler extends Thread
	{
		private InputStream is;
		private OutputStream os;

		public ClientHandler(Socket socket) throws IOException
		{
			is = socket.getInputStream();
			os = socket.getOutputStream();

			// start client thread
			start();
		}

		@Override
		public void run()
		{
			try {
				while (true) {
					StringBuilder sb = new StringBuilder();
					int ch;

					// read characters until 0 terminator
					while ((ch = is.read()) != 0 && ch != -1)
						sb.append((char) ch);

					// -1 when connection is closed
					if (ch == -1)
						break;

					String request = sb.toString();

					// process the request
					String response = wut.doRequest(request);

					// write response
					if (response != null)
						os.write((response + "\u0000").getBytes());
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public WutWrapperServer(int port, ConnectionService service) throws IOException
	{
		ssocket = new ServerSocket(port);

		wut = new WutWrapper(service);

		// for each new connection create a new ClientHandler
		while(true) {
			new ClientHandler(ssocket.accept());
		}
	}

	static void printHelp()
	{
		System.out.println("- - WUT-MODBUS-ADAPTER-SERVER - -\n" +
		"this server converts WUT requests to MODBUS requests\n\n" +
		"usage: ./wmas [wut-port] [service] {service-options}\n" +
		"  wut-port           -   port of emulated wut-server\n" +
		"  service            -   type of the wrapped protocol\n" +
		"      modbus [host]  -   use the modbus protocol\n" +
		"      virtual        -   use a virtual interactive service\n");

	}

	public static void main(String[] args)
	{
		int port;
		String service_string;
		ConnectionService service;

		if (args.length < 2) {
			printHelp();
			return;
		}

		// parse settings
		port = Integer.parseInt(args[0]);
		service_string = args[1];

		switch (service_string) {
			case "virtual":
				virtual_mode = true;
				service = new VirtualConnectionService();
				break;
			case "modbus":
				if (args.length < 3) {
					System.out.println("please specify the address of the modbus server");
					System.out.println("example: java -jar server.jar 80 modbus 192.168.0.34");
					return;
				}
				try {
					service = new ModbusConnectionService(args[2]);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				break;
			default:
				System.out.println("service not found! only available: virtual, modbus");
				return;
		}

		try {
			// create and start server
			new WutWrapperServer(port, service);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			service.close();
		}
	}
}
