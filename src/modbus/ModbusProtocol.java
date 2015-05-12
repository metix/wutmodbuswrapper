/**
 * @author Maximilian Etti
 */
package modbus;

/**
 * This class holds a few constants for the TCP Modbus Protocol.
 */
class ModbusProtocol
{
	/* default port of TCP MODBUS */
	public static final int MODBUS_DEFAULT_PORT = 502;

	/* length of TCP MODBUS header in bytes */
	public static final int MBAP_HEADER_LENGTH = 8;

	/* maximum segment size of a single TCP MODBUS frame */
	public static final int MODBUS_MAX_SEGMENT_SIZE = 300;

	/* using  connection.modbus on tcp, the unit identifier is useless, because
	the addressing works with IPs, so the unit identifier is set to 0xff
	 */
	public static final byte MODBUS_UNIT_IDENT = (byte) 0xff;

	/* the header field 'protocol identifier' must be 0x00 */
	public static final int MODBUS_PROTOCOL_IDENT = 0x00;

	/* MODBUS functions */
	public static final int F_READ_COILS = 0x01;
	public static final int F_READ_DISCRETE_INPUTS = 0x02;
	public static final int F_WRITE_SINGLE_COIL = 0x05;

	/* exceptions */
	public static final int E_ILLEGAL_FUNCTION = 0x01;
	public static final int E_ILLEGAL_DATA_ADDRESS = 0x02;
	public static final int E_ILLEGAL_DATA_VALUE = 0x03;
	public static final int E_SLAVE_DEVICE_FAILURE = 0x04;
	public static final int E_ACKNOWLEDGE = 0x05;
	public static final int E_SLAVE_DEVICE_BUSY = 0x06;
	public static final int E_MEMORY_PARITY_ERROR = 0x08;
	public static final int E_GATEWAY_PATH_UNAVAILABLE = 0x0a;
	public static final int E_GATEWAY_TARGET_DEVICE_FAILED_TO_RESPOND = 0x0b;
}
