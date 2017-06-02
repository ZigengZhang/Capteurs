package fr.insalyon.p2i2.javaarduino.tdtp;

import java.io.BufferedReader;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ArrayList;
import fr.insalyon.p2i2.javaarduino.usb.ArduinoUsbChannel;
import fr.insalyon.p2i2.javaarduino.util.Console;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import jssc.SerialPortException;

public class JavaArduino {

    private Connection conn;
    private PreparedStatement insertObjectStatement;
    private PreparedStatement selectMesuresStatement;
    private static ArrayList<String> listeRFID = new ArrayList<String>();
    private static ArrayList<String> listeVR = new ArrayList<String>();
    private String port,port2;
    boolean sleep = true;
    boolean finiParler = false;
    BDRecVoc bdRecVoc;
    Main mainClass;
    
    
    public static void main(String[] args) {
    	JavaArduino jaar = new JavaArduino();
    	jaar.displayList(listeVR);
    	jaar.displayList(listeRFID);
    }
    

    public JavaArduino() {
    	final Console console = new Console();
        
        console.log( "DEBUT du programme TestArduino !.." );
       
        do {
        
            console.log( "RECHERCHE d'un port disponible..." );
            port = ArduinoUsbChannel.getOneComPort();
            
            if (port == null) {
                console.log( "Aucun port disponible!" );
                console.log( "Nouvel essai dans 5s" );
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    // Ignorer l'Exception
                }
            }

        } while (port == null);
        
        port = "COM3";
        port2 = "COM5";
        
        console.println("Connection au Port " + port+" et "+port2);
        try {
            final ArduinoUsbChannel vcpChannel = new ArduinoUsbChannel(port);
            final ArduinoUsbChannel vcpChannel2 = new ArduinoUsbChannel(port2);
       
            Thread readingThreadVR = new Thread(new Runnable() {
                public void run() {
                	
                    BufferedReader vcpInput2 = new BufferedReader(new InputStreamReader(vcpChannel2.getReader()));
                    
            		
					
                    String line;
                    try {
                    	
                        while (true) {
                        	console.println("");
                        	console.println("");
                        	
                        	line = vcpInput2.readLine();
                        	console.println(line);
                        	if(sleep){
                        		console.println("dans le sleep");
        						vcpChannel2.getWriter().write("w2".getBytes("UTF-8"));
        						vcpChannel2.getWriter().write('\n');
        						sleep = false;
                        	}
                        	
                        	if("FINI".equals(line)){
                        		finiParler = true;
                        	}
                        	if(!finiParler){
                        		switch(line){
                        			case "PARC" : 
                        				listeVR.add(line); 
                        				break;
                        			case "TRAVAIL" : 
                        				listeVR.add(line);
                        				break;
                        			case "COPINE" :
                        				listeVR.add(line);
                        				break;
                        			case "COURS" :
                        				listeVR.add(line);
                        				break;
                        			case "DINGDING" :
                        				listeVR.add(line);
                        				break;
                        			case "BORDEL" :
                        				listeVR.add(line);
                        				break;
                        		}
                        	}
                        	if("SORS".equals(line)){
                        		console.println("init");
                        		listeVR = new ArrayList<String>();
                        		listeRFID = new ArrayList<String>();
        						finiParler = false;
        						console.println("dans le sleep");
        						vcpChannel2.getWriter().write("w2".getBytes("UTF-8"));
        						vcpChannel2.getWriter().write('\n');
                        	}
                        	traiterListe(listeVR);
                        	console.println("////");
                        	displayList(listeVR);
                        	console.println("////");
                        }
                       

                    }  catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            	
            }
            );
            
            Thread readingThread = new Thread(new Runnable() {
                public void run() {
                    BufferedReader vcpInput = new BufferedReader(new InputStreamReader(vcpChannel.getReader()));
                    
                    String line;
                    try {

                        while ((line = vcpInput.readLine()) != null) {
                        	console.log(line);
                        	listeRFID.add(line); //ADAPTER POUR REC VOC
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                    
                }
            }
            );
            
            readingThread.start();
            readingThreadVR.start();
            vcpChannel.open();
            vcpChannel2.open();

            boolean exit = false;
            
            while (!exit) {
            
                String line = console.readLine("Envoyer une ligne (ou 'fin') > ");
                
                if (line.length() == 0) {
                	continue;
                }
                
                if ("fin".equals(line)) {
                    exit = true;
                    continue;
                }
                
                
                
                
                vcpChannel2.getWriter().write(line.getBytes("UTF-8"));
                vcpChannel2.getWriter().write('\n');
            
            }
            
            vcpChannel.close();
            vcpChannel2.close();

            //readingThread.interrupt();
            //readingThreadVR.interrupt();

            try {
                readingThread.join(1000);
                readingThreadVR.join(1000);

            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            }

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (SerialPortException ex) {
            ex.printStackTrace(System.err);
        }
        
    
    }
   

    public void displayList(ArrayList<String> liste){
       	for(String item : liste)
       	{   
       		System.out.println(item);
    	}
    }
    
    public void addToList (String idObjets){
    	listeRFID.add(idObjets);
    }


	public ArrayList<String> getListeRFID() {
		return listeRFID;
	}


	public void setListeRFID(ArrayList<String> listeRFID) {
		this.listeRFID = listeRFID;
	}
	
	public void traiterListe(ArrayList<String> listeVR){
		ArrayList<String> liste = new ArrayList<String>();
    	if(listeVR.size()!=0){
    		for(int i =1;i<listeVR.size();i++){
    			liste.add(listeVR.get(i));
    		}
    		listeVR=liste;
    	}
	}
	
	
	public ArrayList<String> connection(){
		bdRecVoc = new BDRecVoc("sql11172522", "sql11172522", "Tclw7Ag8uh");
		mainClass = new Main("sql11172522", "sql11172522", "Tclw7Ag8uh");
		bdRecVoc.selectObject("parc");
		ArrayList<String> listeObjets = bdRecVoc.getListeObjets();
		if(listeObjets!=null){
			return listeObjets;
		}else{
			System.out.println("la liste objets est vide");
			return listeObjets;
		}
	}
	
	
}
