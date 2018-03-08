package kr.re.keti.threefinger;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import kr.re.keti.degree.Degree;
import kr.re.keti.degree.PortWorker;

import com.fazecast.jSerialComm.SerialPort;

public class FingerPointer extends JFrame {

	private final int NumberofFinger = 9;
	private final int NumberofPressure = 3;
	
	private String[] portList;
	private SerialPort[] portObj;
	
	private int watchingpoint[];
	private int degree[];
	private int pressure[];
	
	private boolean isConnected;
	
	private int selectedPortIndex;
	
	private JPanel contentPane;
	
	private JComboBox comboBox;
	
	private Degree degreeObj;
	
	private Thread DegreeReader; 
	
	private JLabel labelFinger[];
	private JCheckBox checkFinger[];
	private JSpinner spinner[];
	
	private JLabel labelkPa[];
	private JSlider slider[];
	private JCheckBox checkPressure[];
	
	private JCheckBox checkrawdata;
	
	private int[] fingerdatam[][];
	
	int loadpoint[];
	
	private final int maxMapFileSize = 255;
	
	private final int AngleInterval = 10;
	private final int PressureInterval = 10;
	private final int numOfAngleUnits = 12;
	private final int numOfPressureUnits = 5;
	
	private final static int degree_row = 9;
	private final static int degree_column = 12;
	
	private final static int pressure_row = 3;
	private final static int pressure_column = 5;

	private int findata[][];
	private int pressuredata[][];
	
	File csvfilename;;
	boolean isloadcsv;
	
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
	
		pressureMap = pressuredata[id];
					
			for(int i = 0; i < numOfPressureUnits; i++)
			{
				if(r >= pressureMap[i])
				{
					resultPressure = (PressureInterval * (i));
					break;
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
			// csv 데이타 파일
			BufferedReader br = new BufferedReader(new FileReader(csvname));
			String line = "";
			int row =0 ,i;

			while ((line = br.readLine()) != null) {
				String[] token = line.split(",", -1);
				for(i=0;i<degree_column;i++)    findata[row][i] = Integer.parseInt(token[i]);
				// CSV에서 읽어 배열에 옮긴 자료 확인하기 위한 출력
				for(i=0;i<degree_column;i++)    System.out.print(findata[row][i] + " ");
				System.out.println("");
				
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
			// csv 데이타 파일
			BufferedReader br = new BufferedReader(new FileReader(csvname));
			String line = "";
			int row =0 ,i;

			while ((line = br.readLine()) != null) {
				String[] token = line.split(",", -1);
				for(i=0;i<pressure_column;i++)    pressuredata[row][i] = Integer.parseInt(token[i]);
				// CSV에서 읽어 배열에 옮긴 자료 확인하기 위한 출력
				for(i=0;i<pressure_column;i++)    System.out.print(pressuredata[row][i] + " ");
				System.out.println("");
				
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
		
		isloadcsv = false;
		
		ReadDegreeCSV(new File("findata.csv"));
		ReadPressureCSV(new File("pressuredata.csv"));
		initPorts();
		initialize();
		initPortSettingUI();
		load();
		
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
		setBounds(100, 100, 913, 628);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSerialPort = new JLabel("Serial Port");
		lblSerialPort.setBounds(31, 10, 98, 15);
		contentPane.add(lblSerialPort);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(288, 54, 29, 404);
		contentPane.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(31, 35, 835, 7);
		contentPane.add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBounds(572, 54, 16, 404);
		contentPane.add(separator_2);
		
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
					// ���� ������ Ŭ����
					isConnected = false;
					degreeObj.closePort();
					save();
					//DegreeReader.stop();
					
					
				}
				else
				{
					btnNewButton.setText(disConnectLabel);
					
					String selectedPortName = portList[selectedPortIndex];
					
					// Connection routine
					final int baudRate = 115200;
										
					//final String mapFileName = "sensor_data.txt";
					//final String mapFileName2 = "finger2_data.txt";
					//final String mapFileName3 = "finger3_data.txt";
					
					degreeObj = new Degree(new PortWorker(selectedPortName, baudRate));
					
//					try
//					{
//						degreeObj.readDegreeMap(new java.io.File(mapFileName));
//					} catch (FileNotFoundException e1)
//					{
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (IOException e1)
//					{
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
					
					
					
					degreeObj.openPort();
					
					//DegreeReader.run();
					
					DegreeReader = new Thread(new Runnable()
					{
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							while(isConnected)
							{
								//int[] result = degreeObj.getDegrees();
								int[] result = degreeObj.getDatas();
								
								if(result == null)
									continue;
								/*
								while(result == null)
								{
									result = degreeObj.getDegrees();
								}
								
								for(int i = 0; i < 15; i++)
								{
									System.out.printf("%4d ", result[i]);
								}
								System.out.println();
								*/
								
								for(int i = 0; i < NumberofFinger; i++)
								{
									degree[i] = result[watchingpoint[i]];
								}
								for(int i = 0; i < NumberofPressure; i++)
								{
									pressure[i] = result[(i+1)*15-1];
									//System.out.print(result[(i+1)*15-1] + " ");
								}
								
								updateUI();
							}
							
						}
					});
					
					isConnected = true;
					DegreeReader.start();
					//���°� ���� ����
					
					comboBox.setEnabled(false);
				}
			}
		});
		btnNewButton.setBounds(267, 6, 140, 23);
		contentPane.add(btnNewButton);
		
		JButton btnselectcsv = new JButton("Load csv");
		btnselectcsv.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File("./"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("csv 파일", "csv");
				//fileChooser.addChoosableFileFilter(filter);
				fileChooser.setFileFilter(filter);
				int returnVal = fileChooser.showDialog(null, "열기");
				
				if( returnVal == JFileChooser.APPROVE_OPTION)
	            {
	                //열기 버튼을 누르면
	           
	                File choosed = fileChooser.getSelectedFile();
					ReadDegreeCSV(choosed);
	                
	            }
	            else
	            {
	                //취소 버튼을 누르면
	                
	                
	            }
				
				
			}
		});
		btnselectcsv.setBounds(620, 6, 140, 23);
		contentPane.add(btnselectcsv);
		
		JButton btnRefreshPortList = new JButton("Refresh port list");
		btnRefreshPortList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initPorts();
				initPortSettingUI();
			}
		});
		btnRefreshPortList.setBounds(442, 6, 134, 23);
		contentPane.add(btnRefreshPortList);
		
		JLabel label = new JLabel("1-1");
		label.setFont(new Font("굴림", Font.PLAIN, 25));
		label.setBounds(32, 287, 87, 35);
		contentPane.add(label);
		
		JLabel label_2 = new JLabel("1-2");
		label_2.setFont(new Font("굴림", Font.PLAIN, 25));
		label_2.setBounds(31, 383, 87, 35);
		contentPane.add(label_2);
		
		JLabel label_4 = new JLabel("1-3");
		label_4.setFont(new Font("굴림", Font.PLAIN, 25));
		label_4.setBounds(31, 483, 87, 35);
		contentPane.add(label_4);
		
		JLabel label_6 = new JLabel("2-1");
		label_6.setFont(new Font("굴림", Font.PLAIN, 25));
		label_6.setBounds(313, 285, 87, 35);
		contentPane.add(label_6);
		
		JLabel label_8 = new JLabel("2-2");
		label_8.setFont(new Font("굴림", Font.PLAIN, 25));
		label_8.setBounds(312, 388, 87, 35);
		contentPane.add(label_8);
		
		JLabel label_10 = new JLabel("2-3");
		label_10.setFont(new Font("굴림", Font.PLAIN, 25));
		label_10.setBounds(312, 484, 87, 35);
		contentPane.add(label_10);
		
		JLabel label_12 = new JLabel("3-1");
		label_12.setFont(new Font("굴림", Font.PLAIN, 25));
		label_12.setBounds(597, 284, 87, 35);
		contentPane.add(label_12);
		
		JLabel label_14 = new JLabel("3-2");
		label_14.setFont(new Font("굴림", Font.PLAIN, 25));
		label_14.setBounds(596, 387, 87, 35);
		contentPane.add(label_14);
		
		JLabel label_16 = new JLabel("3-3");
		label_16.setFont(new Font("굴림", Font.PLAIN, 25));
		label_16.setBounds(596, 483, 87, 35);
		contentPane.add(label_16);

		
		
		checkFinger[0] = new JCheckBox("Finger 1-1");
		checkFinger[0].setSelected(true);
		checkFinger[0].setBounds(184, 155, 91, 23);
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
		labelFinger[0].setBounds(59, 194, 119, 42);
		contentPane.add(labelFinger[0]);
		
		JLabel lblDegree = new JLabel("degree");
		lblDegree.setFont(new Font("굴림", Font.PLAIN, 30));
		lblDegree.setBounds(183, 199, 119, 35);
		contentPane.add(lblDegree);
		
		spinner[0] = new JSpinner();
		spinner[0].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[0] = ((Integer)spinner[0].getValue()).intValue();
			}
		});
		spinner[0].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[0].setBounds(138, 156, 38, 22);
		contentPane.add(spinner[0]);
		
		checkFinger[1] = new JCheckBox("Finger 1-2");
		checkFinger[1].setSelected(true);
		checkFinger[1].setBounds(180, 295, 91, 23);
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
				watchingpoint[1] = ((Integer)spinner[1].getValue()).intValue();
			}
		});
		spinner[1].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[1].setBounds(134, 296, 38, 22);
		contentPane.add(spinner[1]);
		
		JLabel label_1 = new JLabel("degree");
		label_1.setFont(new Font("굴림", Font.PLAIN, 30));
		label_1.setBounds(183, 339, 119, 35);
		contentPane.add(label_1);
		
		labelFinger[1] = new JLabel("0");
		labelFinger[1].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[1].setBounds(59, 334, 119, 42);
		contentPane.add(labelFinger[1]);
		
		checkFinger[2] = new JCheckBox("Finger 1-3");
		checkFinger[2].setSelected(true);
		checkFinger[2].setBounds(180, 441, 91, 23);
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
				watchingpoint[2] = ((Integer)spinner[2].getValue()).intValue();
			}
		});
		spinner[2].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[2].setBounds(134, 442, 38, 22);
		contentPane.add(spinner[2]);
		
		JLabel label_3 = new JLabel("degree");
		label_3.setFont(new Font("굴림", Font.PLAIN, 30));
		label_3.setBounds(183, 485, 119, 35);
		contentPane.add(label_3);
		
		labelFinger[2] = new JLabel("0");
		labelFinger[2].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[2].setBounds(59, 480, 119, 42);
		contentPane.add(labelFinger[2]);
		
		checkFinger[3] = new JCheckBox("Finger 2-1");
		checkFinger[3].setSelected(true);
		checkFinger[3].setBounds(459, 154, 91, 23);
		checkFinger[3].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_6.setVisible(checkFinger[3].isSelected());
			}
		});
		contentPane.add(checkFinger[3]);
		
		spinner[3] = new JSpinner();
		spinner[3].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[3] = ((Integer)spinner[3].getValue()).intValue();
			}
		});
		spinner[3].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[3].setBounds(413, 155, 38, 22);
		contentPane.add(spinner[3]);
		
		JLabel label_5 = new JLabel("degree");
		label_5.setFont(new Font("굴림", Font.PLAIN, 30));
		label_5.setBounds(468, 198, 119, 35);
		contentPane.add(label_5);
		
		labelFinger[3] = new JLabel("0");
		labelFinger[3].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[3].setBounds(343, 193, 119, 42);
		contentPane.add(labelFinger[3]);
		
		checkFinger[4] = new JCheckBox("Finger 2-2");
		checkFinger[4].setSelected(true);
		checkFinger[4].setBounds(459, 295, 91, 23);
		checkFinger[4].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_8.setVisible(checkFinger[4].isSelected());
			}
		});
		contentPane.add(checkFinger[4]);
		
		spinner[4] = new JSpinner();
		spinner[4].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[4] = ((Integer)spinner[4].getValue()).intValue();
			}
		});
		spinner[4].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[4].setBounds(413, 296, 38, 22);
		contentPane.add(spinner[4]);
		
		JLabel label_7 = new JLabel("degree");
		label_7.setFont(new Font("굴림", Font.PLAIN, 30));
		label_7.setBounds(468, 339, 119, 35);
		contentPane.add(label_7);
		
		labelFinger[4] = new JLabel("0");
		labelFinger[4].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[4].setBounds(343, 334, 119, 42);
		contentPane.add(labelFinger[4]);
		
		checkFinger[5] = new JCheckBox("Finger 2-3");
		checkFinger[5].setSelected(true);
		checkFinger[5].setBounds(459, 441, 91, 23);
		checkFinger[5].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_10.setVisible(checkFinger[5].isSelected());
			}
		});
		contentPane.add(checkFinger[5]);
		
		spinner[5] = new JSpinner();
		spinner[5].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[5] = ((Integer)spinner[5].getValue()).intValue();
			}
		});
		spinner[5].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[5].setBounds(413, 442, 38, 22);
		contentPane.add(spinner[5]);
		
		JLabel label_9 = new JLabel("degree");
		label_9.setFont(new Font("굴림", Font.PLAIN, 30));
		label_9.setBounds(468, 485, 119, 35);
		contentPane.add(label_9);
		
		labelFinger[5] = new JLabel("0");
		labelFinger[5].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[5].setBounds(343, 480, 119, 42);
		contentPane.add(labelFinger[5]);
		
		checkFinger[6] = new JCheckBox("Finger 3-1");
		checkFinger[6].setSelected(true);
		checkFinger[6].setBounds(738, 154, 91, 23);
		checkFinger[6].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_12.setVisible(checkFinger[6].isSelected());
			}
		});
		contentPane.add(checkFinger[6]);
		
		spinner[6] = new JSpinner();
		spinner[6].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[6] = ((Integer)spinner[6].getValue()).intValue();
			}
		});
		spinner[6].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[6].setBounds(692, 155, 38, 22);
		contentPane.add(spinner[6]);
		
		JLabel label_11 = new JLabel("degree");
		label_11.setFont(new Font("굴림", Font.PLAIN, 30));
		label_11.setBounds(757, 198, 119, 35);
		contentPane.add(label_11);
		
		labelFinger[6] = new JLabel("0");
		labelFinger[6].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[6].setBounds(627, 193, 119, 42);
		contentPane.add(labelFinger[6]);
		
		checkFinger[7] = new JCheckBox("Finger 3-2");
		checkFinger[7].setSelected(true);
		checkFinger[7].setBounds(738, 295, 91, 23);
		checkFinger[7].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_14.setVisible(checkFinger[7].isSelected());
			}
		});
		contentPane.add(checkFinger[7]);
		
		spinner[7] = new JSpinner();
		spinner[7].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[7] = ((Integer)spinner[7].getValue()).intValue();
			}
		});
		spinner[7].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[7].setBounds(692, 296, 38, 22);
		contentPane.add(spinner[7]);
		
		JLabel label_13 = new JLabel("degree");
		label_13.setFont(new Font("굴림", Font.PLAIN, 30));
		label_13.setBounds(757, 339, 119, 35);
		contentPane.add(label_13);
		
		labelFinger[7] = new JLabel("0");
		labelFinger[7].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[7].setBounds(627, 334, 119, 42);
		contentPane.add(labelFinger[7]);
		
		checkFinger[8] = new JCheckBox("Finger 3-3");
		checkFinger[8].setSelected(true);
		checkFinger[8].setBounds(738, 439, 91, 23);
		checkFinger[8].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_16.setVisible(checkFinger[8].isSelected());
			}
		});
		contentPane.add(checkFinger[8]);
		
		spinner[8] = new JSpinner();
		spinner[8].addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				watchingpoint[8] = ((Integer)spinner[8].getValue()).intValue();
			}
		});
		spinner[8].setModel(new SpinnerNumberModel(0, 0, 44, 1));
		spinner[8].setBounds(692, 440, 38, 22);
		contentPane.add(spinner[8]);
		
		JLabel label_15 = new JLabel("degree");
		label_15.setFont(new Font("굴림", Font.PLAIN, 30));
		label_15.setBounds(757, 483, 119, 35);
		contentPane.add(label_15);
		
		labelFinger[8] = new JLabel("0");
		labelFinger[8].setFont(new Font("굴림", Font.PLAIN, 30));
		labelFinger[8].setBounds(627, 488, 119, 42);
		contentPane.add(labelFinger[8]);
		
		ImageIcon finger1;
		finger1 = new ImageIcon("fingerblank.png");
		
		JLabel labelfinger1 = new JLabel("",new ImageIcon("C:\\Users\\keti_khlee\\workspace\\ThreeFinger\\fingerblank.png"),JLabel.CENTER);
		labelfinger1.setBounds(0, 173, 97, 366);
		contentPane.add(labelfinger1);
		
		ImageIcon finger2;
		finger2 = new ImageIcon("fingerblank.png");
		
		JLabel labelfinger2 = new JLabel("",new ImageIcon("C:\\Users\\keti_khlee\\workspace\\ThreeFinger\\fingerblank.png"),JLabel.CENTER);
		labelfinger2.setBounds(286, 173, 97, 366);
		contentPane.add(labelfinger2);
		
		ImageIcon finger3;
		finger3 = new ImageIcon("fingerblank.png");
		
		JLabel labelfinger3 = new JLabel("",new ImageIcon("C:\\Users\\keti_khlee\\workspace\\ThreeFinger\\fingerblank.png"),SwingConstants.CENTER);
		labelfinger3.setBounds(569, 173, 97, 366);
		contentPane.add(labelfinger3);
				
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
		slider[0].setBounds(20, 100, 250, 48);
		contentPane.add(slider[0]);
		
		JLabel label_slider0 = new JLabel("kPa");
		label_slider0.setFont(new Font("굴림", Font.PLAIN, 30));
		label_slider0.setBounds(180, 60, 119, 35);
		contentPane.add(label_slider0);
		
		labelkPa[0] = new JLabel("0");
		labelkPa[0].setFont(new Font("굴림", Font.PLAIN, 30));
		labelkPa[0].setBounds(54, 55, 119, 42);
		labelkPa[0].setHorizontalAlignment(SwingConstants.TRAILING);
		contentPane.add(labelkPa[0]);
		
		checkPressure[0] = new JCheckBox("Pressure 1");
		checkPressure[0].setSelected(true);
		checkPressure[0].setBounds(180, 40, 91, 23);
		checkPressure[0].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_slider0.setVisible(checkPressure[0].isSelected());
				labelkPa[0].setVisible(checkPressure[0].isSelected());
				slider[0].setVisible(checkPressure[0].isSelected());
			}
		});
		contentPane.add(checkPressure[0]);
		
		
		slider[1] = new JSlider();
		slider[1].setMajorTickSpacing(10);
		slider[1].setValue(0);
		slider[1].setPaintLabels(true);
		slider[1].setPaintTicks(true);
		slider[1].setMinorTickSpacing(10);
		slider[1].setMaximum(50);
		slider[1].setFont(new Font("굴림", Font.PLAIN, 12));
		slider[1].setBounds(300, 100, 250, 48);
		contentPane.add(slider[1]);
		
		JLabel label_slider1 = new JLabel("kPa");
		label_slider1.setFont(new Font("굴림", Font.PLAIN, 30));
		label_slider1.setBounds(460, 60, 119, 35);
		contentPane.add(label_slider1);
		
		labelkPa[1] = new JLabel("0");
		labelkPa[1].setFont(new Font("굴림", Font.PLAIN, 30));
		labelkPa[1].setBounds(333, 55, 119, 42);
		labelkPa[1].setHorizontalAlignment(SwingConstants.TRAILING);
		contentPane.add(labelkPa[1]);
		
		checkPressure[1] = new JCheckBox("Pressure 2");
		checkPressure[1].setSelected(true);
		checkPressure[1].setBounds(460, 40, 91, 23);
		checkPressure[1].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_slider1.setVisible(checkPressure[1].isSelected());
				labelkPa[1].setVisible(checkPressure[1].isSelected());
				slider[1].setVisible(checkPressure[1].isSelected());
			}
		});
		contentPane.add(checkPressure[1]);
		
		
		slider[2] = new JSlider();
		slider[2].setMajorTickSpacing(10);
		slider[2].setValue(0);
		slider[2].setPaintLabels(true);
		slider[2].setPaintTicks(true);
		slider[2].setMinorTickSpacing(10);
		slider[2].setMaximum(50);
		slider[2].setFont(new Font("굴림", Font.PLAIN, 12));
		slider[2].setBounds(600, 100, 250, 48);
		contentPane.add(slider[2]);
		
		JLabel label_slider2 = new JLabel("kPa");
		label_slider2.setFont(new Font("굴림", Font.PLAIN, 30));
		label_slider2.setBounds(750, 60, 119, 35);
		contentPane.add(label_slider2);
		
		labelkPa[2] = new JLabel("0");
		labelkPa[2].setFont(new Font("굴림", Font.PLAIN, 30));
		labelkPa[2].setBounds(622, 55, 119, 42);
		labelkPa[2].setHorizontalAlignment(SwingConstants.TRAILING);
		contentPane.add(labelkPa[2]);
		
		checkPressure[2] = new JCheckBox("Pressure 3");
		checkPressure[2].setSelected(true);
		checkPressure[2].setBounds(740, 40, 91, 23);
		checkPressure[2].addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				label_slider2.setVisible(checkPressure[2].isSelected());
				labelkPa[2].setVisible(checkPressure[2].isSelected());
				slider[2].setVisible(checkPressure[2].isSelected());
			}
		});
		contentPane.add(checkPressure[2]);
		
		checkrawdata = new JCheckBox("raw");
		checkrawdata.setBounds(800, 10, 100, 20);
		contentPane.add(checkrawdata);
		
				
	}
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
					if(!slider[i].isVisible())
					{
						slider[i].setVisible(true);
					}
				}else
				{
					labelkPa[i].setText(" " + pressure[i]);
					if(slider[i].isVisible())
					{
						slider[i].setVisible(false);
					}	
				}
			}
		}
	}
}
