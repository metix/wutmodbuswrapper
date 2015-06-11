WutModbusWrapper
-
WutModbusWrapper translates the WUT procotol from WUT-Devices (http://www.wut.de/) to TCP-Modbus protocol.


It basically emulates a WUT-Server and passes requests to a modbus server.

It can be used in a case like:

* you have a modbus device but your software only have a client driver for WUT devices

Features
-
At the moment the WutModbusWrapper supports following commands:
* get inputs
* get outputs
* set outputs

Usage
-
First compile the sources. Then use it like this:
```
java -jar wutmodbus.jar [wut-port] [service] {service-options}
  wut-port           -   port of emulated wut-server
  service            -   type of the wrapped protocol
      modbus [host]  -   use the modbus protocol
      virtual        -   use a virtual interactive service
```

#### WUT to Modbus

For example, if you have a device with a modbus server running on address 192.168.0.10 and a software which has a WUT client, you can start a new WutModbusAdapter-Server like this:

`java -jar wutmodbus.jar 80 modbus 192.168.0.10`

#### Virtual Service
You can also use a virtual protocol instead of modbus, to test the WUT-Server.

`java -jar wutmodbus.jar 80 virtual`

Then you get a commando prompt, with accepts following commands:
```
inputs=[hex]      - set inputs
outputs=[hex]     - set outputs
inputs            - print inputs
outputs           - print outputs
```

License
-
MIT
