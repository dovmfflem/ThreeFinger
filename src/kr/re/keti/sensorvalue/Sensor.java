package kr.re.keti.sensorvalue;

//import kr.re.keti.LifeGesture;
import com.fazecast.jSerialComm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.io.DataInputStream;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

public class Sensor
{
	public Sensor()
	{
		degreeMap = null;

		
		serialPort = null;
		
		sensorInputStream = null;
		
		sensingThread = null;
		continueSensing = false;
		
		sensingBuffer = null;
	}
	
	public Sensor(Port p)
	{
		sensingThread = null;
		continueSensing = false;
		
		sensorInputStream = null;
		
		sensingBuffer = null;
		
		degreeMap = null;

		
		setPortObj(p);
	}
	
	public void finalize()
	{
		closePort();
	}
	
	public void setPortObj(Port p)
	{
		serialPort = p;
	}
	
	public void openPort()
	{
		if(serialPort != null)
		{
			serialPort.openPort();
			
			sensorInputStream = serialPort.getInputStream();
		}
		
		if(sensingBuffer == null)
		{
			sensingBuffer = new StringBuffer();
			
			sensingBuffer.ensureCapacity(maxBufferSize);
		}
		
		sensingThread = new Thread()
		{
			@Override
			public void run()
			{
				final int startIndex = 0;
				
				final int endIndex = shrinkSize;
				
				int readSize = 0;
				
				byte[] bufferArray = new byte[defaultSensingBufferSize];
				
				while(continueSensing)
				{
					try
					{
						readSize = sensorInputStream.read(bufferArray);
						//System.out.println(bufferArray);
					}
					catch (IOException e)
					{
						continue;
//						System.exit(0);
					}
					
					if(readSize <= 0)
					{
						continue;
					}
					
					if(sensingBuffer.length() >= maxBufferSize)
					{
						sensingBuffer.delete(startIndex, endIndex);
					}
					
					sensingBuffer.append(new String(bufferArray, startIndex, readSize));
				}
			}
		};
		
		continueSensing = true;
		
		sensingThread.start();
	}
	
	public void closePort()
	{
		continueSensing = false;
		
		if(sensingBuffer != null)
		{
			final int startIndex = 0;
			final int endIndex = sensingBuffer.length();
			
			sensingBuffer.delete(startIndex, endIndex);
		}
		
		if(serialPort != null)
		{
			try
			{
				sensorInputStream.close();
			}
			catch (IOException e)
			{
				
			}
			
			serialPort.closePort();
		}
	}
	
	public String getPortName()
	{
		if(serialPort == null)
		{
			return null;
		}
		else
		{
			return serialPort.getPortName();
		}
	}
	
	public int getDegree(int r)
	{
		int resultDegree = 120;
		
		if(degreeMap != null)
		{	
			for(int i = 0; i < numOfAngleUnits; i++)
			{
				if(r >= degreeMap[i])
				{
					resultDegree = (AngleInterval * (i));
					break;
				}
				
			}
		}
		return resultDegree;
	}
	
	
	
	
	
	
	public int[] getDegrees(int[] r)
	{
		final int initialValue = 0;
		
		int arraySize = 0; 
		if(r == null)
		{
			return null;
		}
		else
		{
			arraySize = r.length;
		}
		
		int[] resultDegrees = new int[arraySize];
		
		Arrays.fill(resultDegrees, initialValue);
		
		for(int i = 0; i < arraySize; ++i)
		{
			
			resultDegrees[i] = getDegree(r[i]);
			//System.out.printf("%d ", r[i]);
		}
		//System.out.println();
		return resultDegrees.clone();
	}
	
	public int[] getDegrees()
	{
		final int initialValue = 0;
		final int startPosition = 0;
		
		//final int packetSize = 180;
		//final int packetSize = 60;
		//final int packetSize = 42*4;
		
		final String starter = "$";
		final String finisher = "#OK";
		
		if(serialPort == null || !serialPort.isOpen())
		{
			return null;
		}
		
		if(sensingBuffer == null || sensingBuffer.length() < (packetSize + starter.length() + finisher.length()))
		{
			return null;
		}
		
		int[] r = new int[numOfSensingPoints];
		
		Arrays.fill(r, initialValue);
		
		String dataBody = null;
		
		int starterIndex = sensingBuffer.indexOf(starter);
		int finisherIndex = sensingBuffer.indexOf(finisher);
		
		if(finisherIndex - starterIndex - 1 != packetSize)
		{
			if(finisherIndex >= 0)
			{
				sensingBuffer.delete(startPosition, finisherIndex + finisher.length());
			}
			else if(sensingBuffer.length() > 0)
			{
				sensingBuffer.delete(startPosition, sensingBuffer.length());
			}
			System.out.println("return null if(finisherIndex - starterIndex - 1 != packetSize)");
			return null;
		}
		
		dataBody = sensingBuffer.substring(starterIndex + 1, finisherIndex);
		
		sensingBuffer.delete(startPosition, finisherIndex + finisher.length());
				
		char[] buf = dataBody.toCharArray();
		int cnt=3;
		int ans=0;
		int IndexOfSensingPoint=0;
		int number=0;
		for(int i = 0; i < buf.length; i++)
		{
			number = (int)buf[i]-48;
			if(number >= 17)
				number-=7;
			
			ans += (number) * Math.pow(16, cnt);

			cnt--;
			if((i+1) % 4 == 0)
			{
				r[IndexOfSensingPoint++] = ans;
				cnt = 3;
				ans = 0;
			}
		}
		
		return getDegrees(r);
	}

	public double[] getDatas3th() throws IOException
	{
		
		final int packetSize = 256;
		//final int packetSize = 60;		
//		String finisherArr[] = {"S1", "S2", "S3", "S0"};
		String finger[] = {"S0", "S1", "S2", "S3", "S4", "S"};
		String l_starter = "L0";
		String l_finisher = "L0";
		String r_starter = "R0";
		String r_finisher = "R0";
		String starter = "S0";
		String finisher = "S0";
		
		if(serialPort == null || !serialPort.isOpen())
		{
			return null;
		}
		else if(sensingBuffer == null || sensingBuffer.length() < (packetSize + starter.length() + finisher.length()))
		{
			return null;
		}
		
//		System.out.println(sensingBuffer);
		String dataBody = null;
		int starterIndex = sensingBuffer.indexOf(starter); // 癲ル슪�릻�댆洹잙뼀占쎈Ŋ占썬굝�쐻�뜝占� S0
		int finisherIndex = sensingBuffer.indexOf(finisher, starterIndex+1) - 1; // single
		if(starterIndex < 0 && finisherIndex < 0)
		{
			starterIndex = sensingBuffer.indexOf(l_starter); // 癲ル슪�릻�댆洹잙뼀占쎈Ŋ占썬굝�쐻�뜝占� S0
			finisherIndex = sensingBuffer.indexOf(l_finisher, starterIndex+1) - 1; // left
			starter = l_starter;
			finisher = l_finisher;
			finger[0] = "L0";
			finger[1] = "L1";
			finger[2] = "L2";
			finger[3] = "L3";
			finger[4] = "L4";
			finger[5] = "L";
			if(starterIndex < 0 && finisherIndex < 0)
			{
				starterIndex = sensingBuffer.indexOf(r_starter); // 癲ル슪�릻�댆洹잙뼀占쎈Ŋ占썬굝�쐻�뜝占� S0
				finisherIndex = sensingBuffer.indexOf(r_finisher, starterIndex+1) - 1; // right
				starter = r_starter;
				finisher = r_finisher;				
				finger[0] = "R0";
				finger[1] = "R1";
				finger[2] = "R2";
				finger[3] = "R3";
				finger[4] = "R4";
				finger[5] = "R";
			}			
		}
			
//		System.out.println(starterIndex);
//		System.out.println(finisherIndex);
		final int startPosition = 0;
		final int initialValue = 0;

		int numOfGyro = 6;
		int numOfPressure = 30;
		int numOfSensingPoint = 6; // pressure indivisual
		int numOfStrech = 5;
		int allOfSensors = numOfPressure + numOfGyro + numOfStrech;
		int i, j, k, sensorPos=0, sensorNum; // 29 is 3th
		double[] SensorValue = new double[allOfSensors];
		Arrays.fill(SensorValue, initialValue);
		String data;
		
		if(starterIndex <= -1 || finisherIndex <= -1){
			if(sensingBuffer.length() > 0)
			{
				sensingBuffer.delete(startPosition, sensingBuffer.length());				
			}
			return null;
		}
		else if(finisherIndex - starterIndex < 0)
		{
//			System.out.println("Error1 : " + sensingBuffer.toString());
			if (starterIndex > 0)
			{
				sensingBuffer.delete(startPosition, starterIndex-1);											
			}
			starterIndex = sensingBuffer.indexOf(starter);
			finisherIndex = sensingBuffer.indexOf(finisher, starterIndex+1) - 1;
//			System.out.println("Search2 : " + finisherIndex + " " + starterIndex+ " " + (finisherIndex - starterIndex));
			if(finisherIndex - starterIndex - 1 < 0)
			{
				return null;
			}
		}
		
		
		//v1126		System.out.println(); // print v1114
		//v1126		System.out.println("Result : " + finisherIndex + " " + starterIndex+ " " + (finisherIndex - starterIndex)); // print v1114
//		System.out.println(starter + finisher + finger[0] + finger[1] + finger[2] +	finger[3] +	finger[4] +	finger[5]);
//		System.out.println("Length : " + sensingBuffer.length());
		dataBody = sensingBuffer.substring(starterIndex, finisherIndex);
//		System.out.printf("%d\t%d\t", sensingBuffer.length(), dataBody.length());
		sensingBuffer.delete(0, finisherIndex); //(int)(sensingBuffer.length()/3*2));	// 		sensingBuffer.delete(startPosition, finisherIndex);
		if(sensingBuffer.length() > packetSize*1000){ // �뜝�럥�뱡�뜝�럩�쓤�뜝�럥彛� data set�뜝�럩逾� 1000�뤆�룊�삕 �뜝�럩逾졾뜝�럡留믣뜝�럩逾좂춯濡녹삕
			sensingBuffer.setLength(0);
		}

///		System.out.println("Data : " + dataBody); // print v1114
		/*		System.out.println("buffer1 : " + sensingBuffer.toString());
		sensingBuffer.delete(startPosition, finisherIndex-1);
		System.out.println("buffer2 : " + sensingBuffer.toString());*/
		
		
		if(dataBody.indexOf("Read fail!", 0) >= 0)
		{
			SensorValue[0] = -12345.67890;
			return SensorValue;
		}
		else if(dataBody.indexOf(finger[0], 0)<0 || dataBody.indexOf(finger[1], 0)<0 || dataBody.indexOf(finger[2], 0)<0 || dataBody.indexOf(finger[3], 0)<0 || dataBody.indexOf(finger[4], 0)<0 || dataBody.indexOf(finger[5], 0)<0)
		{
			SensorValue[0] = -12345.67890;
			return SensorValue;
		}
		//System.out.println("Data : \n" + dataBody); // print v1114

/*// �뜝�럥由� �뜝�럥占썹춯�엪�삕 �뜝�럥�뿼�뇦猿됲�ｏ옙六� 
		sensorNum = 0;
		for(i=0; i<5; i++)
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
			sensorPos = sensorPos + data.length() + 1; // S0 ~ S4 delete 
			 // <count> flexiblesensor*8 stretchable			
			data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
			sensorPos = sensorPos + data.length() + 1; // count 
			
			for(j=0; j<numOfSensingPoint; j++) // flexiblesensor*6
			{
				data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
				SensorValue[sensorNum] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//				System.out.println(sensorNum + " - " + SensorValue[sensorNum]); // print v1114
				sensorPos = sensorPos + data.length() + 1;
				sensorNum ++;
			}
			
			data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));
			SensorValue[numOfPressure+i] = Double.parseDouble(data); //stretchable
			sensorPos = sensorPos + data.length() + 1;
//			System.out.println(numOfPressure+i + " - " + SensorValue[numOfPressure+i]); // print v1114
//			sensorNum ++;
			
		}

		data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1)); // S�뜝�럩逾� �뜝�럥瑜닷뜝�럥裕� NO.
		sensorPos = sensorPos + data.length() + 1; // L or R delete 
		for(j=0; j<numOfGyro-1; j++)		//IMU sensor datas
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
			SensorValue[numOfPressure+numOfStrech+j] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//			System.out.println((numOfPressure+numOfStrech+j) + " - " + data); // print v1114
			sensorPos = sensorPos + data.length() + 1;
		}
		data = dataBody.substring(sensorPos, dataBody.length());
		SensorValue[numOfPressure+numOfStrech+j] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//		System.out.println((numOfPressure+numOfStrech+j) + " - " + SensorValue[numOfPressure+numOfStrech+j]); // print v1114
		sensorPos = sensorPos + data.length() + 1;
//		System.out.println("Result : " + SensorValue); // print v1114
*/	

		String[] split_dataBody = dataBody.split("\n");	
		k = 0;
		sensorNum = 0;
		i = 0;
		while(i<5)
//		for(i=0; i<5; i++)
		{
//			System.out.println(split_dataBody[k]);
			if(split_dataBody[k].indexOf("\t",sensorPos+1) < 0) //占쎌굙占쎌뇚筌ｌ꼶�봺
			{
				k ++;
				sensorPos = 0;
				continue;				
			}
			data = split_dataBody[k].substring(sensorPos, split_dataBody[k].indexOf("\t",sensorPos+1));
			sensorPos = sensorPos + data.length() + 1; // S0 ~ S4 delete 
			if(!data.equals(finger[i]))
			{
				k ++;
				sensorPos = 0;
				continue;
			}
			 // <count> flexiblesensor*8 stretchable		
			data = split_dataBody[k].substring(sensorPos, split_dataBody[k].indexOf("\t",sensorPos+1));
			sensorPos = sensorPos + data.length() + 1; // count 
			
			for(j=0; j<numOfSensingPoint; j++) // flexiblesensor*6
			{
				data = split_dataBody[k].substring(sensorPos, split_dataBody[k].indexOf("\t",sensorPos+1));
				SensorValue[sensorNum] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//				System.out.println(sensorNum + " - " + SensorValue[sensorNum]); // print v1114
				sensorPos = sensorPos + data.length() + 1;
				sensorNum ++;
			}
			
			data = split_dataBody[k].substring(sensorPos, split_dataBody[k].length());
			SensorValue[numOfPressure+i] = Double.parseDouble(data); //stretchable
			sensorPos = sensorPos + data.length() + 1;
//			System.out.println(numOfPressure+i + " - " + SensorValue[numOfPressure+i]); // print v1114
			i++;
			k++;
			sensorPos = 0;
		}


		while(i<6)
		{
			data = split_dataBody[k].substring(sensorPos, split_dataBody[k].indexOf("\t",sensorPos+1)); // S�뜝�럩逾� �뜝�럥瑜닷뜝�럥裕� NO.
			sensorPos = sensorPos + data.length() + 1; // L or R delete 
			if(!data.equals(finger[i]))
			{
				k ++;
				sensorPos = 0;
				continue;
			}
			for(j=0; j<numOfGyro-1; j++)		//IMU sensor datas
			{
				data = split_dataBody[k].substring(sensorPos, split_dataBody[k].indexOf("\t",sensorPos+1));
				SensorValue[numOfPressure+numOfStrech+j] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//				System.out.println((numOfPressure+numOfStrech+j) + " - " + data); // print v1114
				sensorPos = sensorPos + data.length() + 1;
			}
			data = split_dataBody[k].substring(sensorPos, split_dataBody[k].length());
			SensorValue[numOfPressure+numOfStrech+j] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//			System.out.println((numOfPressure+numOfStrech+j) + " - " + SensorValue[numOfPressure+numOfStrech+j]); // print v1114
			sensorPos = sensorPos + data.length() + 1;
//			System.out.println("Result : " + SensorValue); // print v1114
			i ++;
			k ++;
			sensorPos = 0;
		}

		return SensorValue;
	}

	public double[] getDatas3th1hand() throws IOException
	{
		
		final int packetSize = 256;
		//final int packetSize = 60;		
//		String starterArr[] = {"S0", "S1", "S2", "S3"};
//		String finisherArr[] = {"S1", "S2", "S3", "S0"};
		String starter = "S0";
		String finisher = "S0";
		
		if(serialPort == null || !serialPort.isOpen())
		{
			return null;
		}
		else if(sensingBuffer == null || sensingBuffer.length() < (packetSize + starter.length() + finisher.length()))
		{
			return null;
		}
		
		String dataBody = null;
		int starterIndex = sensingBuffer.indexOf(starter); // 癲ル슪�릻�댆洹잙뼀占쎈Ŋ占썬굝�쐻�뜝占� S0
		int finisherIndex = sensingBuffer.indexOf(starter, starterIndex+1) - 1; // 占쎈쐻占쎈윥占쎄콟占쎈탶�⑤베�맆�뜝�럥�쑐 S0
		final int startPosition = 0;
		final int initialValue = 0;

		int numOfGyro = 6;
		int numOfPressure = 29;
		int numOfSensingPoint = 6; // pressure indivisual
		int numOfStrech = 5;
		int allOfSensors = numOfPressure + numOfGyro + numOfStrech;
		int i, j, sensorPos=0, sensorNum; // 29 is 3th
		double[] SensorValue = new double[allOfSensors];
		Arrays.fill(SensorValue, initialValue);
//		System.out.printf("%d\n", sensingBuffer.length());
		String data;
		
		if(starterIndex <= -1 || finisherIndex <= -1){
			if(sensingBuffer.length() > 0)
			{
				sensingBuffer.delete(startPosition, sensingBuffer.length());				
			}
			return null;
		}
		else if(finisherIndex - starterIndex < 0)
		{
//			System.out.println("Error1 : " + sensingBuffer.toString());
			if (starterIndex > 0)
			{
				sensingBuffer.delete(startPosition, starterIndex-1);											
			}
			starterIndex = sensingBuffer.indexOf(starter);
			finisherIndex = sensingBuffer.indexOf(starter, starterIndex+1) - 1;
//			System.out.println("Search2 : " + finisherIndex + " " + starterIndex+ " " + (finisherIndex - starterIndex));
			if(finisherIndex - starterIndex - 1 < 0)
			{
				return null;
			}
		}
		
		
		//v1126		System.out.println(); // print v1114
		//v1126		System.out.println("Result : " + finisherIndex + " " + starterIndex+ " " + (finisherIndex - starterIndex)); // print v1114
//		System.out.println("Length : " + sensingBuffer.length());
		if(sensingBuffer.length() > packetSize*1000){
			sensingBuffer.setLength(0);
		}

		dataBody = sensingBuffer.substring(starterIndex, finisherIndex);
//		System.out.printf("%d\t%d\t", sensingBuffer.length(), dataBody.length());
		sensingBuffer.delete(0, (int)(sensingBuffer.length()/3*2));	// 		sensingBuffer.delete(startPosition, finisherIndex);
//		System.out.println("Data : " + dataBody); // print v1114
		/*		System.out.println("buffer1 : " + sensingBuffer.toString());
		sensingBuffer.delete(startPosition, finisherIndex-1);
		System.out.println("buffer2 : " + sensingBuffer.toString());*/
		
		
		if(dataBody.indexOf("Read fail!", 0) >= 0)
		{
			SensorValue[0] = -12345.67890;
			return SensorValue;
		}
		else if(dataBody.indexOf("S0", 0)<0 || dataBody.indexOf("S1", 0)<0 || dataBody.indexOf("S2", 0)<0 || dataBody.indexOf("S3", 0)<0 || dataBody.indexOf("S4", 0)<0)
		{
			SensorValue[0] = -12345.67890;
			return SensorValue;
		}
//		System.out.println("Data : \n" + dataBody); // print v1114
			

		sensorNum = 0;
		for(i=0; i<5; i++)
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
			sensorPos = sensorPos + data.length() + 1; // S0 ~ S4 delete 
			 // <count> flexiblesensor*8 stretchable			
			data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
			sensorPos = sensorPos + data.length() + 1; // count 
			
			for(j=0; j<numOfSensingPoint; j++) // flexiblesensor*6
			{
				data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
				SensorValue[sensorNum] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//				System.out.println(sensorNum + " - " + SensorValue[sensorNum]); // print v1114
				sensorPos = sensorPos + data.length() + 1;
				sensorNum ++;
			}
			
			data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));
			SensorValue[numOfPressure+i] = Double.parseDouble(data); //stretchable
			sensorPos = sensorPos + data.length() + 1;
//			System.out.println(numOfPressure+i + " - " + SensorValue[numOfPressure+i]); // print v1114
//			sensorNum ++;
			
		}
		sensorPos ++;
		for(j=0; j<numOfGyro-1; j++)		//IMU sensor datas
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf("\t",sensorPos+1));
			SensorValue[numOfPressure+numOfStrech+j] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//			System.out.println((numOfPressure+numOfStrech+j) + " - " + data); // print v1114
			sensorPos = sensorPos + data.length() + 1;
		}
		data = dataBody.substring(sensorPos, dataBody.length());
		SensorValue[numOfPressure+numOfStrech+j] = Double.parseDouble(data); // flexible - (double)Integer.parseInt(strHexValue, 16)); //16嶺뚯쉳�뫒占쎈빢占쎈ご�뜝占� 10嶺뚯쉳�뫒占쎈빢�슖�댙�삕
//		System.out.println((numOfPressure+numOfStrech+j) + " - " + SensorValue[numOfPressure+numOfStrech+j]); // print v1114
		sensorPos = sensorPos + data.length() + 1;
//		System.out.println("Result : " + SensorValue); // print v1114

		return SensorValue;
	}

	
	
	public double[] getDatas() throws IOException
	{
		String[] directionBuffer = {"None", "Up", "Down", "Left", "Right"};
		final int startPosition = 0;
		
		//final int packetSize = 180;
		//final int packetSize = 60;		
//		String starterArr[] = {"S0", "S1", "S2", "S3"};
//		String finisherArr[] = {"S1", "S2", "S3", "S0"};
		String starter = "S0";
		String finisher = "S4";
		
		if(serialPort == null || !serialPort.isOpen())
		{
			return null;
		}
		else if(sensingBuffer == null || sensingBuffer.length() < (packetSize + starter.length() + finisher.length()))
		{
			return null;
		}
		
		String dataBody = null;
		int starterIndex = sensingBuffer.indexOf(starter); // 癲ル슪�릻�댆洹잙뼀占쎈Ŋ占썬굝�쐻�뜝占� S0
		int finisherIndex = sensingBuffer.indexOf(starter, starterIndex+1) - 1; // 占쎈쐻占쎈윥占쎄콟占쎈탶�⑤베�맆�뜝�럥�쑐 S0
		
		final int initialValue = 0;
		int i, j, d, sensorNum=0, sensorPos=3;
		double[] SensorValue = new double[numOfSensingPoints];
		Arrays.fill(SensorValue, initialValue);
		String data;
/*
		int starterIndex = sensingBuffer.indexOf(starter)-78; // S0 癲ル슓�룱�젆�떘�눀占쎄섶占쎌굲占쎈빝�뜝占� 占쎌쐺獄�袁⑹굲 占쎈쐻占쎈윪占쎌벁 占쎈쐻占쎈윪占쎄껑占쎈쐻占쎈윪�얠±�뒙占쎈룱獒뺣돀�빝繹먮씮�굲占쎈빝�뜝占� 占쎈쐻占쎈윥占쎈ぅ占쎈쐻占쎈윪�얠±�쐻占쎈윞占쎈뙃嚥싲갭큔占쎈뮝占쎈쐻�뜝占�
		int finisherIndex = sensingBuffer.indexOf(finisher, starterIndex+80) + 100; // S4 癲ル슓�룱占쎌쑋占쎈눀占쎄섶占쎌굲占쎈빝�뜝占� 占쎈쐻占쎈윥占쎈뤅占쎈쐻占쎈윥�젆占� 占쎈쐻占쎈윥�뵳�씢�쐻占쎈윪占쎌죷占쎈쐻占쎈윞占쎈뤊占쎈쐻占쎈윞占쎈쭓占쎈쐻占쎈윥占쎈ぅ占쎈쐻占쎈윪�얠±�쐻占쎈윞占쎈뙃嚥싲갭큔占쎈뮝占쎈쐻�뜝占�
*/ // 占쎈쎗占쎈젻泳��떑�젂�뜝占� �뜝�럥�떛嶺뚮ㅎ�뫒占쎄껑占쎈쐻占쎈윪�굢�뀘�쐻占쎈윪筌뤿뱺異�嚥〓끃�굲 占쎈쐻占쎈윥占쎈군占쎈쐻占쎈윪占쎈첓占쎈쐻占쎈윞占쎈�� 占쎈눇�뙼�맪�쐭�뜝�럥�걬占쎌뒙占쎈뙔占쎌굲 占쎈쐻占쎈윪�뤃�눨�쐻占쎈윞筌띾�ｋ쐻占쎈윥壤쏉옙.
//		System.out.println("Search1 : " + finisherIndex + " " + starterIndex+ " " + (finisherIndex - starterIndex));
		
		if(starterIndex <= -1 || finisherIndex <= -1){
			if(sensingBuffer.length() > 0)
			{
				sensingBuffer.delete(startPosition, sensingBuffer.length());				
			}
			return null;
		}
		else if(finisherIndex - starterIndex < 0)
		{
//			System.out.println("Error1 : " + sensingBuffer.toString());
			if (starterIndex > 0)
			{
				sensingBuffer.delete(startPosition, starterIndex-1);											
			}
			starterIndex = sensingBuffer.indexOf(starter);
			finisherIndex = sensingBuffer.indexOf(starter, starterIndex+1) - 1;
//			System.out.println("Search2 : " + finisherIndex + " " + starterIndex+ " " + (finisherIndex - starterIndex));
			if(finisherIndex - starterIndex - 1 < 0)
			{
				return null;
			}
		}
		
		
		//v1126		System.out.println(); // print v1114
		//v1126		System.out.println("Result : " + finisherIndex + " " + starterIndex+ " " + (finisherIndex - starterIndex)); // print v1114
//		System.out.println("Length : " + sensingBuffer.length());
		if(sensingBuffer.length() > packetSize*20){
			sensingBuffer.setLength(0);
		}

		dataBody = sensingBuffer.substring(starterIndex, finisherIndex);
//		System.out.println("Data : " + dataBody); // print v1114
		sensingBuffer.delete(startPosition, finisherIndex);
		/*		System.out.println("buffer1 : " + sensingBuffer.toString());
		sensingBuffer.delete(startPosition, finisherIndex-1);
		System.out.println("buffer2 : " + sensingBuffer.toString());*/
		
		
		if(dataBody.indexOf("Read fail!", 0) >= 0)
		{
			SensorValue[0] = -12345.67890;
			return SensorValue;
		}
		else if(dataBody.indexOf("S0", 0)<0 || dataBody.indexOf("S1", 0)<0 || dataBody.indexOf("S2", 0)<0 || dataBody.indexOf("S3", 0)<0 || dataBody.indexOf("S4", 0)<0)
		{
			SensorValue[0] = -12345.67890;
			return SensorValue;
		}
//		System.out.println("Data : " + dataBody); // print v1114
			
		
		for(i=0; i<4; i++)
		{
			for(j=0; j<8; j++)
			{
				data = dataBody.substring(sensorPos, dataBody.indexOf(" ",sensorPos+1));
//				System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf(" ",sensorPos+1) + "] " + data + " " + SensorValue[sensorNum]);
				SensorValue[sensorNum] = Double.parseDouble(data);
				sensorPos = sensorPos + data.length() + 1;
				sensorNum ++;
			}
			data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));
			SensorValue[sensorNum] = Double.parseDouble(data);
//			System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf("\n",sensorPos+1) + "] " + data + " " + SensorValue[sensorNum]);
			sensorPos = sensorPos + data.length() + 1;
			sensorNum ++;

			data = dataBody.substring(sensorPos, dataBody.indexOf(",",sensorPos+1));
			try {
				SensorValue[sensorNum] = Double.parseDouble(data);
			}
			catch(Exception e)
			{
				data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1)-1);
//				System.out.println(data);
				for(d=1; d<5; d++)
				{
					if(data.equals(directionBuffer[d]))
					{
						SensorValue[sensorNum+9] = d;
//						System.out.println(i + "-" + (sensorNum+9) + " [" + sensorPos + " : " + dataBody.indexOf(",",sensorPos+1) + "] " + data +  " " + SensorValue[sensorNum+9] + d);
						break;
					}
				}
				sensorPos = sensorPos + data.length() + 1;
				data = dataBody.substring(sensorPos, dataBody.indexOf(",",sensorPos+1));				
				SensorValue[sensorNum] = Double.parseDouble(data);
			}
//			System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf(",",sensorPos+1) + "] " + data +  " " + SensorValue[sensorNum]);
			sensorPos = sensorPos + data.length() + 1;
			sensorNum ++;
			for(j=1; j<8; j++)
			{
				data = dataBody.substring(sensorPos, dataBody.indexOf(",",sensorPos+1));
				//SensorValue[sensorNum] = (double)Integer.parseInt(data);
				SensorValue[sensorNum] = Double.parseDouble(data);
//				System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf(",",sensorPos+1) + "] " + data +  " " + SensorValue[sensorNum]);
				sensorPos = sensorPos + data.length() + 1;
				sensorNum ++;
			}			
			
			data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));
			SensorValue[sensorNum] = Double.parseDouble(data);
//			System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf("\n",sensorPos+1) + "] " + data + " " + SensorValue[sensorNum]);
			sensorPos = sensorPos + data.length() + 4;
			sensorNum += 2;
		}

		// i== 4
		for(j=0; j<8; j++)
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf(" ",sensorPos+1));
			SensorValue[sensorNum] = Double.parseDouble(data);
//			System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf(" ",sensorPos+1) + "] " + data + " " + SensorValue[sensorNum]);
			sensorPos = sensorPos + data.length() + 1;
			sensorNum ++;
		}
		data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));
		SensorValue[sensorNum] = Double.parseDouble(data);
//		System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf("\n",sensorPos+1) + "] " + data + " " + SensorValue[sensorNum]);
		sensorPos = sensorPos + data.length() + 1;
		sensorNum ++;

///////////////////////////////////////////////////////////////////////// Count
		data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));
		try {
			SensorValue[numOfSensingPoints-1] = Double.parseDouble(data);
		}
		catch(Exception e)
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1)-1);
//			System.out.println(data);
			for(d=1; d<5; d++)
			{
				if(data.equals(directionBuffer[d]))
				{
					SensorValue[sensorNum+9] = d;
//					System.out.println(i + "-" + (sensorNum+9) + " [" + sensorPos + " : " + dataBody.indexOf(",",sensorPos+1) + "] " + data +  " " + SensorValue[sensorNum+9] + d);
					break;
				}
			}
			sensorPos = sensorPos + data.length() + 1;
			data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));				
			SensorValue[numOfSensingPoints-1] = Integer.parseInt(data);
			//SensorValue[numOfSensingPoints-1] = Double.parseDouble(data);
		}
//		System.out.println(i + "-" + (numOfSensingPoints-1) + " [" + sensorPos + " : " + dataBody.indexOf(" ",sensorPos+1) + "] " + data + " " + SensorValue[numOfSensingPoints-1]);
		sensorPos = sensorPos + data.length() + 1;
/*///////////////////////////////////////////////////////////////////////// Count
		data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1));
		SensorValue[numOfSensingPoints-1] = Integer.parseInt(data);
//		System.out.println(i + "-" + (numOfSensingPoints-1) + " [" + sensorPos + " : " + dataBody.indexOf(" ",sensorPos+1) + "] " + data + " " + SensorValue[numOfSensingPoints-1]);
		sensorPos = sensorPos + data.length() + 1;
///////////////////////////////////////////////////////////////////////// Count	- 癲꾧퀗�뫊�뜝�띁�빝筌먯뮆萸꾬옙�쐻�뜝占� 占쎈쐻占쎈윪占쎌맗 占쎌녃域밟뫁�굲占쎈눇�뙼蹂��굲 v1114 	
/*///////////////////////////////////////////////////////////////////////// Count
		
		data = dataBody.substring(sensorPos, dataBody.indexOf(",",sensorPos+1));
		try {
			SensorValue[sensorNum] = Double.parseDouble(data);
		}
		catch(Exception e)
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf("\n",sensorPos+1)-1);
//			System.out.println(data);
			for(d=1; d<5; d++)
			{
				if(data.equals(directionBuffer[d]))
				{
					SensorValue[sensorNum+9] = d;
//					System.out.println(i + "-" + (sensorNum+9) + " [" + sensorPos + " : " + dataBody.indexOf(",",sensorPos+1) + "] " + data +  " " + SensorValue[sensorNum+9] + d);
					break;
				}
			}
			sensorPos = sensorPos + data.length() + 1;
			data = dataBody.substring(sensorPos, dataBody.indexOf(",",sensorPos+1));				
			SensorValue[sensorNum] = Double.parseDouble(data);
		}	
//		System.out.println(sensorNum + " [" + sensorPos + " : " + dataBody.indexOf(",",sensorPos+1) + "] " + data +  " " + SensorValue[sensorNum]);
		sensorPos = sensorPos + data.length() + 1;
		sensorNum ++;
		for(j=1; j<8; j++)
		{
			data = dataBody.substring(sensorPos, dataBody.indexOf(",",sensorPos+1));
			//SensorValue[sensorNum] = (double)Integer.parseInt(data);
			SensorValue[sensorNum] = Double.parseDouble(data);
//			System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + dataBody.indexOf(",",sensorPos+1) + "] " + data +  " " + SensorValue[sensorNum]);
			sensorPos = sensorPos + data.length() + 1;
			sensorNum ++;
		}			
		data = dataBody.substring(sensorPos, dataBody.length()-1);
		SensorValue[sensorNum] = Double.parseDouble(data);
//		System.out.println(i + "-" + sensorNum + " [" + sensorPos + " : " + (dataBody.length()-1) + "] " + data + " " + SensorValue[sensorNum]);
		sensorPos = sensorPos + data.length() + 4;
		sensorNum += 2;
//		System.out.println((numOfSensingPoints-1) + "Count : " + SensorValue[numOfSensingPoints-1]);

		return SensorValue;
	}
	
	
	public void readDegreeMap(File fileObj) throws java.io.FileNotFoundException, java.io.IOException
	{
		final String mapDataSplitter = " ";
				
		FileInputStream fileInputStream = new FileInputStream(fileObj);
		DataInputStream inputStream = new DataInputStream(fileInputStream);
		
		byte[] mapBuffer = new byte[maxMapFileSize];
		
		//1
		Arrays.fill(mapBuffer, (byte)0);
		
		int actualMapSize = inputStream.read(mapBuffer);
		
		String mapData = new String(mapBuffer, 0, actualMapSize);
		
		StringTokenizer mapDataTokenizer = new StringTokenizer(mapData, mapDataSplitter);
		
		degreeMap = new int[numOfAngleUnits];
		
		for(int i = 0; i < numOfAngleUnits; ++i)
		{
			degreeMap[i] = Integer.parseInt(mapDataTokenizer.nextToken());
		}
		
		
		inputStream.close();
		fileInputStream.close();
	}
	
	public void readallfingerdata()
	{
		//�뜝�럥�맶�뜝�럥�쑅�뜝�럥�걛�뜝�럥�맶�뜝�럥�쑋占쎌뼚짹占쎌맶�뜝�럥�쐾�뜝�럥�셾?�뜝�럥�맶�뜝�럥吏쀥뜝�럩援꿨뜝�럥�맶占쎈쐻�뜝占�? �뜝�럥�맶�뜝�럥�쑋占쎌뼲�삕�뜝�럥�맶�뜝�럥�쑅�뜝�럡�맖 �뜝�럥�맶�뜝�럥�쑅�뜝�럡�맖�뜝�럥�맶�뜝�럥�쑋�뜝�럩�벂�뜝�럥�맶�뜝�럥�쑋占쎌뼚짹占쎌맶�뜝�럥�쑅�뜝�럥援� �뜝�럥�맶�뜝�럥吏쀥뜝�럩援꿨뜝�럥�맶�뜝�럥�쑋�뜝�럡�뀘�뜝�럥�맶�뜝�럥�쑅占쎈뎨占쎈쑕占쎌맶�뜝�럥�쑅占쎌젂�뜝占�.
		
		
	}
	
	//public int getpointdegree(int id, )
	
	private StringBuffer sensingBuffer;
	private final int defaultSensingBufferSize = 655350;
	private final int numOfSensingPoint = 19;
	private final int numOfSensingPoints = numOfSensingPoint*5+1;
	private final int maxBufferSize = 655350;
	private final int shrinkSize = 327670;
	private final int packetSize = 1024;
	

	private int[] degreeMap;
	
	private Port serialPort;
	
	private Thread sensingThread;
	private boolean continueSensing;
	private InputStream sensorInputStream;
	
	private final int numOfAngleUnits = 12;
	//private final int numOfSensingPoints = 15;
	private final int maxAngle = 120;
	
	private final int AngleInterval = 10;
	
	private final int maxMapFileSize = 255;
	
}
