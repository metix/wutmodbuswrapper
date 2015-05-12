/**
 * @author Maximilian Etti
 */
package modbus;

import java.nio.ByteBuffer;

/**
 * This class represents a TCP ModbusMessage/Telegramm/Datagramm whatever.
 * The structure of such a Message is like this:
 * see the ModBus specs for more infos.
 * <p>
 * ----------------------------------
 * |     transaction identifier     | 2 Bytes
 * |                                |
 * ----------------------------------
 * |       procotol identifier      | 2 Bytes
 * |                                |
 * ----------------------------------
 * |             length             | 2 Bytes
 * |                                |
 * ----------------------------------
 * |         unit identifier        | 1 Byte
 * ----------------------------------
 * |         function code          | 1 Byte
 * ----------------------------------
 * |              data              | (length - 2) Bytes
 * |               ..               |
 * |               ..               |
 * |               ..               |
 * ----------------------------------
 * <p>
 * All stuff is transmitted in BIG ENDIAN
 */
class ModbusMessage
{
	private short transaction_ident;
	private short protocol_ident = 0;
	private short length;
	private byte unit_ident;
	private byte function_code;
	private ByteBuffer data;

	/**
	 * Extract and parse a ModbusMessage from a ByteBuffer
	 *
	 * @param buff
	 */
	public ModbusMessage(ByteBuffer buff)
	{
		// extract packet
		this.transaction_ident = buff.getShort();
		this.protocol_ident = buff.getShort();
		this.length = buff.getShort();
		this.unit_ident = buff.get();
		this.function_code = buff.get();
		this.data = ByteBuffer.allocate(length - 2);

		// extract data and copy it in the data buffer
		buff.get(this.data.array(), 0, length - 2);
	}

	/**
	 * Create a new ModbusMessage
	 *
	 * @param function_code
	 */
	public ModbusMessage(int function_code)
	{
		this.function_code = (byte) function_code;
		this.length = 2;
		this.unit_ident = ModbusProtocol.MODBUS_UNIT_IDENT;
		this.protocol_ident = ModbusProtocol.MODBUS_PROTOCOL_IDENT;

		// allocate new data buffer
		this.data = ByteBuffer.allocate(ModbusProtocol.MODBUS_MAX_SEGMENT_SIZE);
	}

	public void setTransactionIdentifier(short transaction_ident)
	{
		this.transaction_ident = transaction_ident;
	}

	public int getFunctionCode()
	{
		return function_code & 0xff;
	}

	public short getTransactionIdentifier()
	{
		return transaction_ident;
	}

	public short getProtocolIdentifier()
	{
		return protocol_ident;
	}

	public void addDataByte(byte b)
	{
		data.put(b);
		length++;
	}

	public void addDataShort(short s)
	{
		data.putShort(s);
		length += 2;
	}

	public void addDataInt(int i)
	{
		data.putInt(i);
		length += 4;
	}

	public byte getDataByte()
	{
		return data.get();
	}

	public short getDataShort()
	{
		return data.getShort();
	}

	public int getDataInt()
	{
		return data.getInt();
	}

	public byte[] getDataBytes(int n)
	{
		byte[] bytes = new byte[n];
		data.get(bytes, 0, n);
		return bytes;
	}

	public byte[] getBytes()
	{
		// create packet with the right size
		ByteBuffer buff = ByteBuffer.allocate(ModbusProtocol.MBAP_HEADER_LENGTH + length - 2);

		// append header
		buff.putShort(transaction_ident);
		buff.putShort(protocol_ident);
		buff.putShort(length);
		buff.put(unit_ident);
		buff.put(function_code);

		// append data
		buff.put(data.array(), 0, length - 2);

		return buff.array();
	}

	/**
	 * Print all content of this packet for debug purposes.
	 *
	 * @return Content of packet as string
	 */
	public String toString()
	{
		String s = "{\n";

		s += "\ttrans_ident: 0x" + Integer.toHexString(transaction_ident);
		s += "\n\tproto_ident: 0x" + Integer.toHexString(protocol_ident);
		s += "\n\tlength: 0x" + Integer.toHexString(length);
		s += "\n\tunit_ident: 0x" + Integer.toHexString(unit_ident & 0xff);
		s += "\n\tfunction: 0x" + Integer.toHexString(function_code & 0xff);

		String data_string = "";
		for (int i = 0; i < length - 2; i++)
			data_string += "0x" + Integer.toHexString(data.get(i) & 0xff) + ", ";
		s += "\n\tdata[" + (length - 2) + "]: " + data_string;

		s += "\n}";

		return s;
	}
}
