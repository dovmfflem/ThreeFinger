package kr.re.keti.threefinger;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;



import com.fazecast.jSerialComm.SerialPort;

import kr.re.keti.sensorvalue.PortWorker;
import kr.re.keti.sensorvalue.Sensor;

public class FingerPointer extends JFrame {

	private final int NumberofFinger = 3;
	private final int NumberofPressure = 1;
	
	private String[] portList;
	private SerialPort[] portObj;
	
	private int watchingpoint[];
	private int degree[];
	private int pressure[];
	
	private boolean isConnected;
	
	private int selectedPortIndex;
	
	private JPanel contentPane;
	
	private JComboBox comboBox;
	
	private Sensor sensorObj;
	
	
	private Thread DegreeReader; 
	
	private JLabel labelFinger[];
	private JCheckBox checkFinger[];
	private JCheckBox checkReverse;
	private JSpinner spinner[];
	
	private JLabel labelkPa[];
	private JLabel labelpressure[];
	private JSlider slider[];
	private JCheckBox checkPressure[];
	
	private JCheckBox checkrawdata;
	
	private JLabel labeldegree[];
	
	private int[] fingerdatam[][];
	
	int loadpoint[];
	
	private final int maxMapFileSize = 255;
	
	private final int AngleInterval = 5;		//edited
	private final int PressureInterval = 2;
	private final int numOfAngleUnits = 24;		//edited	
	private final int numOfPressureUnits = 25;
	
	private final static int degree_row = 3;	
	private final static int degree_column = 24;	//edited
	
	private final static int pressure_row = 2;
	private final static int pressure_column = 25;
	
	private final int numofpoint = 35;

	private int findata[][];
	private int pressuredata[][];
	
	private int fingerIndex;
	private int PressWatchingPoint = 4;
	
	private double[] send_data;
	private boolean gender = false; //false: female; true: male	
	
	File csvfilename;;
	boolean isloadcsv;
	
	private JRadioButton radio_famale, radio_male;
	private ButtonGroup btngrp;
	
	public int[] ReadFingerData(File fileObj) throws java.io.FileNotFoundException, java.io.IOException
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
		int degreeMap[] = new int[numOfAngleUnits];
		for(int i = 0; i < numOfAngleUnits; ++i)
		{
			degreeMap[i] = Integer.parseInt(mapDataTokenizer.nextToken());
		}
		inputStream.close();
		fileInputStream.close();
		
		return degreeMap;
	}
	
	public int getDegree(int r, int id)
	{
		int resultDegree = 120;
		int[] degreeMap = null;
	
		degreeMap = findata[id];
					
			for(int i = 0; i < numOfAngleUnits; i++)
			{
				if(r == 0)
				{
					resultDegree = 0;
					break;
				}
				if(r >= degreeMap[i])
				{
					resultDegree = (AngleInterval * (i));
					break;
				}
				
			}
		
		return resultDegree;
	}
	
	public int getPressure(int r, int id)
	{
		int resultPressure = 50;
		int[] pressureMap = null;
	
		if(checkReverse.isSelected() != true){
			pressureMap = pressuredata[1];
			for(int i = 0; i < numOfPressureUnits; i++)
			{
				if(r == 0)
				{
					resultPressure = 0;
					break;
				}
				if(r <= pressureMap[i])
				{
					resultPressure = (PressureInterval * (i));
					break;
				}
				
			}
		}else{
			pressureMap = pressuredata[0];
			for(int i = 0; i < numOfPressureUnits; i++)
			{
				if(r == 0)
				{
					resultPressure = 0;
					break;
				}
				if(r >= pressureMap[i])
				{
					resultPressure = (PressureInterval * (i));
					break;
				}
				
			}
		}
		
					

		
		return resultPressure;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FingerPointer frame = new FingerPointer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	
	public void ReadDegreeCSV(File csvname)
	{
		findata = new int[degree_row][degree_column]; 
		try {
			// csv �뜲�씠�� �뙆�씪
			BufferedReader br = new BufferedReader(new FileReader(csvname));
			String line = "";
			int row =0 ,i;

			while ((line = br.readLine()) != null) {
				String[] token = line.split(",", -1);
				for(i=0;i<degree_column;i++)    findata[row][i] = Integer.parseInt(token[i]);
				// CSV�뿉�꽌 �씫�뼱 諛곗뿴�뿉 �삷湲� �옄猷� �솗�씤�븯湲� �쐞�븳 異쒕젰
//				for(i=0;i<degree_column;i++)    System.out.print(findata[row][i] + " ");
//				System.out.println("");
				
				row++;
			}
			br.close();

		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ReadPressureCSV(File csvname)
	{
		pressuredata = new int[pressure_row][pressure_column]; 
		try {
			// csv �뜲�씠�� �뙆�씪
			BufferedReader br = new BufferedReader(new FileReader(csvname));
			String line = "";
			int row =0 ,i;

			while ((line = br.readLine()) != null) {
				String[] token = line.split(",", -1);
				for(i=0;i<pressure_column;i++)    pressuredata[row][i] = Integer.parseInt(token[i]);
				// CSV�뿉�꽌 �씫�뼱 諛곗뿴�뿉 �삷湲� �옄猷� �솗�씤�븯湲� �쐞�븳 異쒕젰
//				for(i=0;i<pressure_column;i++)    System.out.print(pressuredata[row][i] + " ");
//				System.out.println("");
				
				row++;
			}
			br.close();

		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void save()
	{
		try {
			FileOutputStream savewriter = new FileOutputStream("save.dat");
			
			for(int i = 0; i < NumberofFinger; i++)
			{
				savewriter.write(watchingpoint[i]);
				//savewriter.write(watchingpoint[i] + " ");
			}
			//savewriter.write("\n");
			for(int i = 0; i < NumberofFinger; i++)
			{
				if(checkFinger[i].isSelected() == true)
					savewriter.write(1);
				else
					savewriter.write(0);
			}
			savewriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		
	}
	
	private void load()
	{
		int val;
				
		
		try {
			FileInputStream loadreader = new FileInputStream("save.dat");
			
			for(int i = 0; i < NumberofFinger; i++)
			{
				try {
					//loadpoint[i] = loadreader.read(); 
					//System.out.print(loadpoint[i] + " ");
					spinner[i].setValue(Integer.valueOf(loadreader.read()));
					//System.out.print((int) loadreader.read() + " ");
					 //
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
			
			for(int i = 0; i < NumberofFinger; i++)
			{
				try {
					val = loadreader.read();
					if(val == 1)
						checkFinger[i].setSelected(true);
					else
						checkFinger[i].setSelected(false);
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public FingerPointer() {
		
		portObj = null;
		portList = null;
		
		isConnected = false;

		selectedPortIndex = 0;

		watchingpoint = new int[NumberofFinger];
		degree = new int[NumberofFinger];
		pressure = new int[NumberofPressure];
		
		labelFinger = new JLabel[NumberofFinger];
		checkFinger = new JCheckBox[NumberofFinger];
		
		spinner = new JSpinner[NumberofFinger];
		
		loadpoint = new int[NumberofFinger]; 
		
		labelkPa = new JLabel[NumberofPressure];
		slider = new JSlider[NumberofPressure];
		checkPressure = new JCheckBox[NumberofPressure];
		labeldegree = new JLabel[NumberofFinger];
		labelpressure = new JLabel[NumberofPressure];
		
		isloadcsv = false;
		
		ReadDegreeCSV(new File("findata5.csv"));
		ReadPressureCSV(new File("pressuredata.csv"));
		initPorts();
		initialize();
		initPortSettingUI();
		fingerIndex = 0;
		//load();
		
		//connectUDP.start();
		//誘몄닔�젙�릺�뿀湲� �븣臾몄뿉 二쇱꽍泥섎━
		
	}
	private void initPortSettingUI()
	{
		final int emptyCount = 0;
		
		if(portList != null && portObj != null)
		{
			if(comboBox.getItemCount() > emptyCount)
			{
				comboBox.removeAllItems();
			}
			
			for(int i = 0; i < portList.length; ++i)
			{
				comboBox.addItem(portList[i]);
			}
		}
	}
	
	private void initPorts()
	{
		portObj = SerialPort.getCommPorts();
		
		int numOfPorts = portObj.length;
		
		portList = new String[numOfPorts];
		
		for(int i = 0; i < numOfPorts; ++i)
		{
			portList[i] = portObj[i].getSystemPortName();
		}
	}
	
	private void initialize(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 713, 628);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSerialPort = new JLabel("Serial Port");
		lblSerialPort.setBounds(31, 10, 98, 15);
		contentPane.add(lblSerialPort);
		
		JLabel lblNotice = new JLabel("�삱諛붾Ⅸ �룞�옉�엯�땲�떎.");
		lblNotice.setBounds(100, 600, 400, 60);
		lblNotice.setFont(new Font("굴림", Font.PLAIN, 40));
		lblNotice.setOpaque(true);
		lblNotice.setBackground(Color.WHITE);
		lblNotice.setHorizontalAlignment(SwingConstants.CENTER);
		//contentPane.add(lblNotice);
				
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(31, 35, 835, 7);
		contentPane.add(separator_1);
		

		comboBox = new JComboBox();
		comboBox.setBounds(111, 7, 128, 21);
		contentPane.add(comboBox);
		
		comboBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final int firstIndex = 0;
				
				int selectedIndex = comboBox.getSelectedIndex();
				
				if(selectedIndex >= firstIndex)
				{
					selectedPortIndex = selectedIndex;
				}
			}
		});
		

		
		JButton btnNewButton = new JButton("Connect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String connectLabel = "Connect";
				String disConnectLabel = "Disconnect";
				
				if(isConnected)
				{
					btnNewButton.setText(connectLabel);
					comboBox.setEnabled(true);
					
					// Disconnection routine
					// 占쏙옙占쏙옙 占쏙옙占쏙옙占쏙옙 클占쏙옙占쏙옙
					isConnected = false;
					sensorObj.closePort();
					//save();
					//DegreeReader.stop();
					
					
				}
				else
				{
					btnNewButton.setText(disConnectLabel);
					
					String selectedPortName = portList[selectedPortIndex];
					
					// Connection routine
					final int baudRate = 921600;
					
					sensorObj = new Sensor(new PortWorker(selectedPortName, baudRate));
					sensorObj.openPort();
					
					DegreeReader = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							while(isConnected)
							{
								//int[] result = degreeObj.getDegrees();
								
								//double[] result = sensorObj.getDatas();
								
								//int[] result = sensorObj.getRawData();
								double[] result = null;
								try {
									result = sensorObj.getDatas3th();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								if(result == null)
									continue;
								
								send_data = new double[result.length];
								for(int i = 0; i < result.length; i++){
									send_data[i] = result[i]* 10;									
								}
								
								if(result[5] == 9999) {
									//System.out.println("female");
									gender = false;				
								}else {
									gender = true;
								}

								
//								while(result == null)
//								{
//									result = sensorObj.getDegrees();
//								}
								
								for(int i = 0; i < result.length; i++)
								{
									System.out.printf("%4d ", (int)result[i]);
								}
								System.out.println();
								
								
								for(int i = 0; i < NumberofFinger; i++)
								{
									degree[i] = (int)result[watchingpoint[i]];
								}
								//for(int i = 0; i < NumberofPressure; i++)
								//{
									pressure[0] = (int)result[PressWatchingPoint];
									//System.out.print(result[(i+1)*15-1] + " ");
								//}
								
								updateUI();
							}
							
						}
					});
					
					isConnected = true;
					DegreeReader.start();
					//占쏙옙占승곤옙 占쏙옙占쏙옙 占쏙옙占쏙옙
					
					comboBox.setEnabled(false);
				}
			}
		});
		btnNewButton.setBounds(267, 6, 140, 23);
		contentPane.add(btnNewButton);
		
		JButton btnRefreshPortList = new JButton("Refresh port list");
		btnRefreshPortList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initPorts();
				initPortSettingUI();
			}
		});
		btnRefreshPortList.setBounds(442, 6, 134, 23);
		contentPane.add(btnRefreshPortList);
		
		
		
		JButton btnFingerS1 = new JButton("Finger Thumb");
		btnFingerS1.setBounds(370, 100, 200, 30);
		btnFingerS1.setFont(new Font("Arial", Font.PLAIN, 20));
		btnFingerS1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				fingerIndex = 0;		
				if(gender == false) {
					PressWatchingPoint = 4;
				}else {
					PressWatchingPoint = 5;
				}
				watchingpoint[0] = ((Integer)spinner[0].getValue()).intValue() + fingerIndex;
				watchingpoint[1] = ((Integer)spinner[1].getValue()).intValue() + fingerIndex;
				watchingpoint[2] = ((Integer)spinner[2].getValue()).intValue() + fingerIndex;
				spinner[0].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[1].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[2].setModel(new SpinnerNumberModel(0, 0, 4, 1));
			}
		});
		contentPane.add(btnFingerS1);
		
		JButton btnFingerS2 = new JButton("Finger 2");
		btnFingerS2.setBounds(370, 140, 200, 30);
		btnFingerS2.setFont(new Font("Arial", Font.PLAIN, 20));
		btnFingerS2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				fingerIndex = 6;		
				PressWatchingPoint = fingerIndex+5;
				watchingpoint[0] = ((Integer)spinner[0].getValue()).intValue() + fingerIndex;
				watchingpoint[1] = ((Integer)spinner[1].getValue()).intValue() + fingerIndex;
				watchingpoint[2] = ((Integer)spinner[2].getValue()).intValue() + fingerIndex;
				spinner[0].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[1].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[2].setModel(new SpinnerNumberModel(0, 0, 4, 1));
			}
		});
		contentPane.add(btnFingerS2);
		
		JButton btnFingerS3 = new JButton("Finger 3");
		btnFingerS3.setBounds(370, 180, 200, 30);
		btnFingerS3.setFont(new Font("Arial", Font.PLAIN, 20));
		btnFingerS3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				fingerIndex = 12;	
				PressWatchingPoint = fingerIndex+5;
				watchingpoint[0] = ((Integer)spinner[0].getValue()).intValue() + fingerIndex;
				watchingpoint[1] = ((Integer)spinner[1].getValue()).intValue() + fingerIndex;
				watchingpoint[2] = ((Integer)spinner[2].getValue()).intValue() + fingerIndex;
				spinner[0].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[1].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[2].setModel(new SpinnerNumberModel(0, 0, 4, 1));
			}
		});
		contentPane.add(btnFingerS3);
		
		JButton btnFingerS4 = new JButton("Finger 4");
		btnFingerS4.setBounds(370, 220, 200, 30);
		btnFingerS4.setFont(new Font("Arial", Font.PLAIN, 20));
		btnFingerS4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				fingerIndex = 18;		
				PressWatchingPoint = fingerIndex+5;
				watchingpoint[0] = ((Integer)spinner[0].getValue()).intValue() + fingerIndex;
				watchingpoint[1] = ((Integer)spinner[1].getValue()).intValue() + fingerIndex;
				watchingpoint[2] = ((Integer)spinner[2].getValue()).intValue() + fingerIndex;
				spinner[0].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[1].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[2].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				
			}
		});
		contentPane.add(btnFingerS4);
		
		JButton btnFingerS5 = new JButton("Finger 5");
		btnFingerS5.setFont(new Font("Arial", Font.PLAIN, 20));
		btnFingerS5.setBounds(370, 260, 200, 30);
		btnFingerS5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				fingerIndex = 24;		
				PressWatchingPoint = fingerIndex+5;
				watchingpoint[0] = ((Integer)spinner[0].getValue()).intValue() + fingerIndex;
				watchingpoint[1] = ((Integer)spinner[1].getValue()).intValue() + fingerIndex;
				watchingpoint[2] = ((Integer)spinner[2].getValue()).intValue() + fingerIndex;
				spinner[0].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[1].setModel(new SpinnerNumberModel(0, 0, 4, 1));
				spinner[2].setModel(new SpinnerNumberModel(0, 0, 4, 1));
			}
		});
		contentPane.add(btnFingerS5);
		
		JLabel label = new JLabel("1");
		label.setFont(new Font("굴림", Font.PLAIN, 25));
		label.setBounds(92, 287, 87, 35);
		contentPane.add(label);
		
		JLabel label_2 = new JLabel("2");
		label_2.setFont(new Font("굴림", Font.PLAIN, 25));
		label_2.setBounds(92, 383, 87, 35);
		contentPane.add(label_2);
		
		JLabel label_4 = new JLabel("3");
		label_4.setFont(new Font("굴림", Font.PLAIN, 25));
		label_4.setBounds(92, 483, 87, 35);
		contentPane.add(label_4);
		
		
		
		checkFinger[0] = new JCheckBox("Point 1");
		checkFinger[0].setSelected(true);
		checkFinger[0].setBounds(234, 155, 91, 23);
		checkFinger[0].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label.setVisible(checkFinger[0].isSelected());
			}
		});
		contentPane.add(checkFinger[0]);
		
		labelFinger[0] = new JLabel("0");
		labelFinger[0].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[0].setBounds(109, 194, 119, 42);
		contentPane.add(labelFinger[0]);
		
		labeldegree[0] = new JLabel("degree");
		labeldegree[0].setFont(new Font("굴림", Font.PLAIN, 30));
		labeldegree[0].setBounds(233, 199, 119, 35);
		contentPane.add(labeldegree[0]);
		
		spinner[0] = new JSpinner();
		spinner[0].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[0] = ((Integer)spinner[0].getValue()).intValue() + fingerIndex;
			}
		});
		spinner[0].setModel(new SpinnerNumberModel(0, 0, 4, 1));
		spinner[0].setBounds(188, 156, 38, 22);
		contentPane.add(spinner[0]);
		
		checkFinger[1] = new JCheckBox("Point 2");
		checkFinger[1].setSelected(true);
		checkFinger[1].setBounds(230, 295, 91, 23);
		checkFinger[1].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_2.setVisible(checkFinger[1].isSelected());
			}
		});
		contentPane.add(checkFinger[1]);
		
		spinner[1] = new JSpinner();
		spinner[1].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[1] = ((Integer)spinner[1].getValue()).intValue() + fingerIndex;
			}
		});
		spinner[1].setModel(new SpinnerNumberModel(0, 0, 5, 1));
		spinner[1].setBounds(184, 296, 38, 22);
		contentPane.add(spinner[1]);
		
		labeldegree[1] = new JLabel("degree");
		labeldegree[1].setFont(new Font("굴림", Font.PLAIN, 30));
		labeldegree[1].setBounds(233, 339, 119, 35);
		contentPane.add(labeldegree[1]);
		
		labelFinger[1] = new JLabel("0");
		labelFinger[1].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[1].setBounds(109, 334, 119, 42);
		contentPane.add(labelFinger[1]);
		
		checkFinger[2] = new JCheckBox("Point 3");
		checkFinger[2].setSelected(true);
		checkFinger[2].setBounds(230, 441, 91, 23);
		checkFinger[2].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_4.setVisible(checkFinger[2].isSelected());
			}
		});
		contentPane.add(checkFinger[2]);
		
		spinner[2] = new JSpinner();
		spinner[2].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[2] = ((Integer)spinner[2].getValue()).intValue() + fingerIndex;
			}
		});
		spinner[2].setModel(new SpinnerNumberModel(0, 0, 4, 1));
		spinner[2].setBounds(184, 442, 38, 22);
		contentPane.add(spinner[2]);
		
		labeldegree[2] = new JLabel("degree");
		labeldegree[2].setFont(new Font("굴림", Font.PLAIN, 30));
		labeldegree[2].setBounds(233, 485, 119, 35);
		contentPane.add(labeldegree[2]);
		
		labelFinger[2] = new JLabel("0");
		labelFinger[2].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[2].setBounds(109, 480, 119, 42);
		contentPane.add(labelFinger[2]);
		
		ImageIcon finger1;
		URL imageURL = getClass().getClassLoader().getResource("fingerblank.png");
		finger1 = new ImageIcon(imageURL);
		
		JLabel labelfinger1 = new JLabel("",finger1,JLabel.CENTER);
		labelfinger1.setBounds(50, 173, 97, 366);
		contentPane.add(labelfinger1);
		
		ImageIcon logo;
		URL logoURL = getClass().getClassLoader().getResource("logo.png");
		logo = new ImageIcon(logoURL);
		JLabel lablelogo = new JLabel("",logo,JLabel.CENTER);
		lablelogo.setBounds(260, 360, 550, 365);
		contentPane.add(lablelogo);
		
		checkReverse = new JCheckBox("Rev");
		checkReverse.setSelected(false);
		checkReverse.setBounds(640, 10, 100, 20);
		contentPane.add(checkReverse);

				
		for(int i = 0; i < NumberofFinger; i++)
		{
			labelFinger[i].setHorizontalAlignment(SwingConstants.TRAILING);
		}
		
		slider[0] = new JSlider();
		slider[0].setMajorTickSpacing(10);
		slider[0].setValue(0);
		slider[0].setPaintLabels(true);
		slider[0].setPaintTicks(true);
		slider[0].setMinorTickSpacing(10);
		slider[0].setMaximum(50);
		slider[0].setFont(new Font("굴림", Font.PLAIN, 12));
		slider[0].setBounds(70, 100, 250, 48);
		contentPane.add(slider[0]);
		
		labelpressure[0] = new JLabel("kPa");
		labelpressure[0].setFont(new Font("굴림", Font.PLAIN, 30));
		labelpressure[0].setBounds(230, 60, 119, 35);
		contentPane.add(labelpressure[0]);
		
		labelkPa[0] = new JLabel("0");
		labelkPa[0].setFont(new Font("굴림", Font.PLAIN, 30));
		labelkPa[0].setBounds(104, 55, 119, 42);
		labelkPa[0].setHorizontalAlignment(SwingConstants.TRAILING);
		contentPane.add(labelkPa[0]);
		
		checkPressure[0] = new JCheckBox("Pressure 1");
		checkPressure[0].setSelected(true);
		checkPressure[0].setBounds(230, 40, 91, 23);
		checkPressure[0].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				labelpressure[0].setVisible(checkPressure[0].isSelected());
				labelkPa[0].setVisible(checkPressure[0].isSelected());
				slider[0].setVisible(checkPressure[0].isSelected());
			}
		});
		contentPane.add(checkPressure[0]);
		

		
		checkrawdata = new JCheckBox("raw");
		checkrawdata.setBounds(590, 10, 50, 20);
		checkrawdata.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				for(int i = 0; i < NumberofFinger; i++)
				{
					labeldegree[i].setVisible(!checkrawdata.isSelected());
				}
				for(int i = 0; i < NumberofPressure; i++)
				{
					labelpressure[i].setVisible(!checkrawdata.isSelected());
				}
			}
		});
		contentPane.add(checkrawdata);
		
		
		JLabel lbaddress = new JLabel("IP ");
		lbaddress.setBounds(370, 420, 100, 20);
		contentPane.add(lbaddress);
		
		JTextField tfaddress = new JTextField("10.0.5.21");
		tfaddress.setBounds(420, 420, 100, 20);
		contentPane.add(tfaddress);
		
		JLabel lbport = new JLabel("PORT ");
		lbport.setBounds(370, 450, 100, 20);
		contentPane.add(lbport);
		
		JTextField tfport = new JTextField("9999");
		tfport.setBounds(420, 450, 100, 20);
		contentPane.add(tfport);
		
		
		JButton btnConnect = new JButton("Send");
		btnConnect.setBounds(540, 420, 100, 20);
		btnConnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				isudp = true;
				issend = true;
				hostaddress = tfaddress.getText();
				port = Integer.parseInt(tfport.getText());
			}
		});
		contentPane.add(btnConnect);
		
		JButton btnDisconnect = new JButton("Stop");
		btnDisconnect.setBounds(540, 450, 100, 20);
		btnDisconnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				issend = false;
			}
		});
		contentPane.add(btnDisconnect);
		
		
		
		radio_famale = new JRadioButton("Female");
		radio_famale.setBounds(590, 40, 100, 20);
		radio_famale.setSelected(true);
		radio_famale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				gender = radio_male.isSelected();
				if(fingerIndex == 0) {
					if(gender == false) {
						PressWatchingPoint = 4;
					}else {
						PressWatchingPoint = 5;
					}
				}
			}
		});
		//contentPane.add(radio_famale);
		
		radio_male = new JRadioButton("Male");
		radio_male.setBounds(590, 60, 100, 20);
		radio_male.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				gender = radio_male.isSelected();
				if(fingerIndex == 0) {
					if(gender == false) {
						PressWatchingPoint = 4;
					}else {
						PressWatchingPoint = 5;
					}
				}
			}
		});
		//contentPane.add(radio_male);
		
		btngrp = new ButtonGroup();
		btngrp.add(radio_famale);
		btngrp.add(radio_male);
	}
	
	private boolean isudp = false;
	private String hostaddress = null;
	private int port = 9999;
	private DatagramSocket socket;
	private DatagramPacket packet;
	private byte[] buf;
	int count = 0;
	private boolean issend = false;
	
	
	private Thread connectUDP = new Thread(new Runnable() {
		
		@Override
		public void run() {
			buf = new byte[50];
			// TODO Auto-generated method stub
			while(!isudp){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//isudp
			System.out.println("Address : " + hostaddress);
			System.out.println("Port : " + port);
			try {
				socket = new DatagramSocket();
			} catch (SocketException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(hostaddress), port);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while(issend){
				try {
					for(int i = 0; i < 5; i++){
						for(int j = 0; j < 9; j++){
							buf[count] = (byte)getDegree((int) send_data[j+(i*19)], 0);
							//buf[count++] = (byte)send_data[j+(i*19)];
							System.out.printf("%d " , buf[count++]);
						}
					}
					
					System.out.print(" $ ");
					
					if(send_data[18] + send_data[37] + send_data[56] + send_data[75] + send_data[94] != 0){
						buf[count++] = (byte)send_data[18];					
						buf[count++] = (byte)send_data[37];
						buf[count++] = (byte)send_data[56];
						buf[count++] = (byte)send_data[75];
						buf[count++] = (byte)send_data[94];
						System.out.print((byte)send_data[18]+ " ");
						System.out.print((byte)send_data[37]+ " ");
						System.out.print((byte)send_data[56]+ " ");
						System.out.print((byte)send_data[75]+ " ");
						System.out.print((byte)send_data[94]+ " ");	
					}else{
						for(int i = 0; i < 5; i++){
							buf[count++] = 0;
						}
					}
					
					System.out.println();
					count = 0;

//					System.out.print("degree data : ");
//					for(int i = 0; i < 45; i++){
//						System.out.printf("%d ", buf[i]);
//					}
//					System.out.println();

					socket.send(packet);
					Thread.sleep(500);

				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	});
	
	 

	
	private void updateUI()
	{
	
		
		for(int i = 0; i < NumberofFinger; i++)
		{
			if(checkFinger[i].isSelected()){
				if(!checkrawdata.isSelected())
				{
					labelFinger[i].setText(" " + getDegree(degree[i], i));
				}else
				{
					labelFinger[i].setText(" " + degree[i]);
				}
			}
		}
		
		for(int i = 0; i < NumberofPressure; i++)
		{
			if(checkPressure[i].isSelected()){
				if(!checkrawdata.isSelected()){
					labelkPa[i].setText(" " + getPressure(pressure[i], i));
					slider[i].setValue(getPressure(pressure[i], i));
					
				}else
				{
					labelkPa[i].setText(" " + pressure[i]);
						
				}
			}
		}
	}
}


