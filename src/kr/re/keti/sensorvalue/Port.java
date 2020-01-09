package kr.re.keti.sensorvalue;

public interface Port
{
	public void setParameters(int baudRate, int dataBits, int stopBits, int parity);
	public boolean isOpen();
	public boolean openPort();
	public boolean closePort();
	public String getPortName();
	public java.io.InputStream getInputStream();
	public java.io.OutputStream getOutputStream();
}
