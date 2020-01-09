package kr.re.keti.sensorvalue;

import com.fazecast.jSerialComm.*;

public class PortWorker implements Port
{
	public PortWorker(String portName)
	{
		currentPort = SerialPort.getCommPort(portName);
	}
	
	public PortWorker(String portName, int baudRate)
	{
		currentPort = SerialPort.getCommPort(portName);
		
		setBaudRate(baudRate);
	}
	
	public void setBaudRate(int baudRate)
	{
		setParameters(baudRate, defaultDataBits, defaultStopBits, defaultParity);
	}
	
	public void setParameters(int baudRate, int dataBits, int stopBits, int parity)
	{
		currentPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
	}
	
	public boolean isOpen()
	{
		return currentPort.isOpen();
	}
	
	public boolean openPort()
	{
		return currentPort.openPort();
	}
	
	public boolean closePort()
	{
		return currentPort.closePort(); 		
	}
	
	public String getPortName()
	{
		return currentPort.getSystemPortName();
	}
	
	public java.io.InputStream getInputStream()
	{
		return currentPort.getInputStream();
	}
	
	public java.io.OutputStream getOutputStream()
	{
		return currentPort.getOutputStream();
	}
	
	private SerialPort currentPort;
	
	public static final int defaultDataBits = 8;
	public static final int defaultStopBits = 1;
	public static final int defaultParity = SerialPort.NO_PARITY;
}
