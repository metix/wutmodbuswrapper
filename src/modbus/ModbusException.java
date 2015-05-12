/**
 * @author Maximilian Etti
 */
package modbus;

public class ModbusException extends Exception
{
	private String message;
	private int error_code;
	private int exception_code;

	public ModbusException(String msg)
	{
		this.message = msg;
	}

	public ModbusException(int error_code, int exception_code)
	{
		this.error_code = error_code;
		this.exception_code = exception_code;

		message = "Modbus protocl error: 0x" + Integer.toHexString(error_code) +
		":0x" + Integer.toHexString(exception_code) + ": ";

		switch (exception_code) {
			case ModbusProtocol.E_ILLEGAL_FUNCTION:
				message += "illegal function";
				break;
			case ModbusProtocol.E_ILLEGAL_DATA_ADDRESS:
				message += "illegal data address";
				break;
			case ModbusProtocol.E_ILLEGAL_DATA_VALUE:
				message += "illegal data value";
				break;
			case ModbusProtocol.E_SLAVE_DEVICE_FAILURE:
				message += "slave device failure";
				break;
			case ModbusProtocol.E_ACKNOWLEDGE:
				// TODO: well, this is not a real error
				message += "acknowledge";
				break;
			case ModbusProtocol.E_SLAVE_DEVICE_BUSY:
				// TODO: and this also isn't a fatal error
				message += "slave device busy";
				break;
			case ModbusProtocol.E_MEMORY_PARITY_ERROR:
				message += "memory parity error";
				break;
			case ModbusProtocol.E_GATEWAY_PATH_UNAVAILABLE:
				message += "gateway path unavailable";
				break;
			case ModbusProtocol.E_GATEWAY_TARGET_DEVICE_FAILED_TO_RESPOND:
				message += "gateway target device failed to respond";
				break;
			default:
				message += "unknow errorcode";
		}
	}

	public int getErrorCode()
	{
		return error_code;
	}

	public int getExceptionCode()
	{
		return exception_code;
	}

	@Override
	public String getMessage()
	{
		return message;
	}
}
