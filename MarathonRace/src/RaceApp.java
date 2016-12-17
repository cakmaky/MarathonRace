import java.util.*;
import java.io.*;
import java.nio.file.*;
import javax.xml.stream.*;  // StAX API
import java.sql.*;
/**
 * RaceApp program implements an application that simulates a race between
 * various participants. The runners differ in their speed and how often
 * they need to rest. The program reads data from derby database, XML file
 * or text file.  
 * @author Yalcin Cakmak
 * @version 1.0
 */
public class RaceApp {

	/**The runningThreads is to store all running threats*/
	public static ArrayList<ThreadRunner> runningThreads = new ArrayList<>();
	
	/** 
	 *Prints the winner's name and interrupts other threats. Other runners
	 *print message on the screen
	 *@param winner - threat who wins the run
	 **/
	public synchronized static void finished(Thread winner){	
		String winnerName = winner.getName();
		System.out.println("\nThe race is over! The " + winnerName + " is the winner.");		

		for(int i=0; i<runningThreads.size(); i++){
			if(!winnerName.equals(runningThreads.get(i).getName())){
					runningThreads.get(i).interrupt();	
					System.out.println(runningThreads.get(i).getName() + " : You beat me fair and square."); 
			}	
		}	
	}
	/** 
	 * Defines two default runners namely Tortoise and Hare with their speed
	 * and rest percentage
	 **/
	public static void defaultTwoRunners(){	
			runningThreads.add(new ThreadRunner("Tortoise", 10, 0));
			runningThreads.add(new ThreadRunner("Hare", 100, 90));	
	}
	
	/**
	 * Connects to derby database namely RunnerDB and reads name, speed and 
	 * rest percentage information from the RunnersStats table. Then, defines
	 * a threat for each entry 
	 */
	public static void readDerbyDatabase(){
		Connection connection=null;
		try{
        	 String dbDirectory = "Resources";
        	 System.setProperty("derby.system.home", dbDirectory);
        	 
        	 String dbUrl = "jdbc:derby:RunnersDB";
        	 
        	 String username = "";
             String password = "";
             connection = DriverManager.getConnection(dbUrl, username, password);        	
        } catch(SQLException e){
            e.printStackTrace();          
        }
		try{
        	Statement statement = connection.createStatement();
        	ResultSet rs = statement.executeQuery("select Name, RunnersSpeed , RestPercentage "
        			+ "from RunnersStats "
        			+ "order by name asc");
        	while(rs.next()){
        			String name = rs.getString("Name");
        			int speed = (int)rs.getDouble("RunnersSpeed");
        			int restRatio = (int) rs.getDouble("RestPercentage");
        		
        			runningThreads.add(new ThreadRunner(name, speed, restRatio));
            }
        	rs.close();
            statement.close();   
      }catch(SQLException e){
            e.printStackTrace();  // for debugging
        }
		try{
            connection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
	}
	/**
	 * Reads XML file and gets name, speed and rest percentage information.
	 * Then, defines a threat for each entry.
	 */
	public static void readXMLFile(){
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter XML file name: ");
		String fileName = sc.nextLine();
		
		Path filePath = Paths.get(fileName);
		String name="";
		int speed=0, restRatio=0;
		while(!Files.exists(filePath)){
			System.out.println("Input file " + fileName + " does not exist. Please try again"); 
			System.out.print("Enter XML file name: ");
			fileName = sc.nextLine();
			filePath = Paths.get(fileName);
		}
		if (Files.exists(filePath)) {
			 XMLInputFactory inputFactory = XMLInputFactory.newFactory();
	            try{
	                // create a XMLStreamReader object
	                FileReader fileReader = new FileReader(filePath.toFile());
	                XMLStreamReader reader = inputFactory.createXMLStreamReader(fileReader);	
	               
	                while (reader.hasNext()){
	                    int eventType = reader.getEventType();
	                    switch (eventType){
	                        case XMLStreamConstants.START_ELEMENT:
	                            String elementName = reader.getLocalName();
	                            if (elementName.equals("Runner")){	                     
	                                name = reader.getAttributeValue(0);	                             
	                            }
	                            if (elementName.equals("RunnersMoveIncrement")){                           
	                                speed = Integer.parseInt(reader.getElementText());	                          
	                            }
	                            if (elementName.equals("RestPercentage")){
	                                restRatio = Integer.parseInt(reader.getElementText());
	                            }
	                            break;
	                        case XMLStreamConstants.END_ELEMENT:
	                            elementName = reader.getLocalName();
	                            if (elementName.equals("Runner")){
	                            	runningThreads.add(new ThreadRunner(name, speed, restRatio));	
	                            }
	                            break;
	                        default:
	                            break;
	                    }
	                    reader.next();
	                }
	            }
	            catch (IOException | XMLStreamException e){
	                System.out.println(e);
	            }
		 }	
	}
	/**
	 * Reads text file and gets name, speed and rest percentage information.
	 * Then, defines a threat for each entry.  
	 */
	public static void readTextFile(){
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter Text file name: ");
		String fileName = sc.nextLine();
		File inputFile = new File(fileName);
		
		while(!inputFile.exists()) {  
			System.out.println("Input file " + fileName + " does not exist. Please try again"); 
			System.out.print("Enter Text file name: ");
			fileName = sc.nextLine();
			inputFile = new File(fileName);
		}
		try {
			Scanner input = new Scanner(inputFile);
			String line;
			while (input.hasNextLine()) {   
				String[] values = input.nextLine().split("\\s+");
				String name = values[0];
				int speed = Integer.parseInt(values[1]);
				int restRatio = Integer.parseInt(values[2]);
				runningThreads.add(new ThreadRunner(name, speed, restRatio));		
			}
			input.close();
		} catch (IOException e) {  
			System.out.println("Error reading from input file: " + fileName); // warn the user if error occurs 
		}	
	}
	/**
	 * Prints the main menu
	 */
	public static void printMenu(){
		System.out.println("Welcome to the Marathon Race Runner Program");
		System.out.println("Select your data source:");
		System.out.println("1. Derby database");
		System.out.println("2. XML file");
		System.out.println("3. Text file");
		System.out.println("4. Default two runners");
		System.out.println("5. Exit");	
	}
	/**
	 * Starts the race with starting all defined threats.
	 * Threats are running concurrently.
	 */
	public static void startRace(){
		System.out.println("The race is started...Go!");
		
		for(int i=0; i<runningThreads.size(); i++){
			runningThreads.get(i).start();
		}
		
		for(int i = 0; i < runningThreads.size(); i++ ){
            try{
                runningThreads.get(i).join();
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
	}
	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
	
		while(true){
			printMenu();								//Display main menu
			System.out.print("\nEnter your choice: ");  // get users choice
			String choice = sc.nextLine();
			if(choice.equals("1")){
				readDerbyDatabase();					// read data from derby database
			}
			else if(choice.equals("2")){
				readXMLFile();							// read data from XML file
			}
			else if(choice.equals("3")){
				readTextFile();							// read data from text file
			}
			else if(choice.equals("4")){
				defaultTwoRunners();					// just define two default runners
				
			}
			else if(choice.equals("5")){				// terminate the program,
				break;	
			}
			else {
				System.out.println("Invalid choice! Please try again.\n");   // Warn the user and ask for a choice again
				continue;
			}
			
			startRace();								// start the race
			runningThreads.clear();						// clear the ArrayList for new race
			System.out.println("Press any key to continue...");
			try{
	            System.in.read();
	        }  
	        catch(Exception e){} 
			}
		System.out.println("\nThank you for using my Marathon Race Program");	
		}
	}
	
	