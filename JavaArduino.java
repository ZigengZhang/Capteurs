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
    boolean fini2 = false;
    BDRecVoc bdRecVoc;
    Main mainClass;
	
    
    public static void main(String[] args) {
    	JavaArduino jaar = new JavaArduino();
    	
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
        	BDRecVoc bdRecVoc = new BDRecVoc("sql11172522", "sql11172522", "Tclw7Ag8uh");
    		Main mainClass = new Main("sql11172522", "sql11172522", "Tclw7Ag8uh");
    		
    		//Interface myInterface = new Interface(800,800,mainClass);  
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

                            	listeVR.add("obligatoire");
                            	if(!("".equals(Meteo.tagMeteo()))){
                            		listeVR.add(Meteo.tagMeteo());
                            	}
                            	
                        	}
                        	
                        	if("FINI".equals(line)){
                        		finiParler = true;
                        		console.log("ls listVR    "+listeVR.get(0));
                        		
                        		mainClass.listeLieu = listeVR;
                        		

                        		console.log("listeliu     "+mainClass.listeLieu.get(0));
                        		console.log(mainClass.listeLieu.get(0));
//                        		if(mainClass.listeLieu.get(i).equals("pluie")){
//                    				mainClass.listeLieu.add("obligatoire");
//                    			}
                        		for(int i =0;i<mainClass.listeLieu.size();i++){
                        
                        			bdRecVoc.selectObject(mainClass.listeLieu.get(i));
                        			console.log("2222");
                        			displayList(bdRecVoc.getListeObjets());
                        			mainClass.listeObjets.addAll(bdRecVoc.getListeObjets());
                        		}
                        		console.log("555555");
                        		displayList(mainClass.listeObjets);
                        		console.log("555555");
                        	}
                        	if(!finiParler){
                        		switch(line){
	                        		case "PARCMALEK" : 
	                    				listeVR.add("PARC"); 
	                    				break;
                        			case "PARC" : 
                        				listeVR.add(line); 
                        				break;
                        			case "SPORTOLI" : 
                        				listeVR.add("sport");
                        				break;
                        			case "COURS" :
                        				listeVR.add(line);
                        				break;
                        		}
                        	}
                        	if("FINI2".equals(line)){
                        		fini2 = true;
                        		mainClass.compareLists(mainClass.listeObjets,listeRFID);
                        		displayList(mainClass.listeNomsObjetsManquants);
                        		Interface myInterface = new Interface(800,800,mainClass);  
                        		
                        	}
                        	
                        	
                        	if("SORS".equals(line)){
                        		console.println("init");
                        		listeVR = new ArrayList<String>();
                        		listeRFID = new ArrayList<String>();
        						finiParler = false;
        						console.println("dans le sleep");
        						vcpChannel2.getWriter().write("w2".getBytes("UTF-8"));
        						vcpChannel2.getWriter().write('\n');

                            	listeVR.add("obligatoire");
                            	listeVR.add(Meteo.tagMeteo());
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
                        	if(!fini2){
                        	console.log(line);
                        	listeRFID.add(line); //ADAPTER POUR REC VOC
                        	}
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
	
	
	public ArrayList<String> connection(ArrayList<String> listeVR){
		bdRecVoc = new BDRecVoc("sql11172522", "sql11172522", "Tclw7Ag8uh");
		mainClass = new Main("sql11172522", "sql11172522", "Tclw7Ag8uh");
		ArrayList<String> listeObjets = new ArrayList<String>();
		for(int i=0;i<listeVR.size();i++){
			bdRecVoc.selectObject(listeVR.get(i));
		}
		listeObjets.addAll(bdRecVoc.getListeObjets());
		if(listeObjets!=null){
			return listeObjets;
		}else{
			System.out.println("la liste objets est vide");
			return listeObjets;
		}
	}
	
	public ArrayList<String> comparer(ArrayList<String> listeVR,ArrayList<String> listeRFID){
		ArrayList<String> listeObjets = connection(listeVR);
		ArrayList<String> listeOublies = new ArrayList<String>();
		boolean existe = false;
		for(int i=0;i<listeObjets.size();i++){
			for(int j=0;j<listeRFID.size();j++){
				if(listeObjets.get(i).equals(listeRFID.get(j))){
					existe = true;
					break;
				}
			}
			if(!existe){
				listeOublies.add(listeObjets.get(i));
			}
		}
		return listeOublies;
	}
	
	
}

