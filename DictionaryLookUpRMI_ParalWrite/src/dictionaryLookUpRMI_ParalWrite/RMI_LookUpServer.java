package dictionaryLookUpRMI_ParalWrite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

public class RMI_LookUpServer extends UnicastRemoteObject implements RMI_LookUpInterface {

	private static final long serialVersionUID = -4672211104630929703L;
	private final int MAX_CLIENTS = (Runtime.getRuntime().availableProcessors() * 10);
	private final Semaphore writeFileLock = new Semaphore(MAX_CLIENTS, true);

	private File dictFile = null;
 
    public RMI_LookUpServer(File f) throws RemoteException {

        super(0);// required to avoid the 'rmic' step, see below
        dictFile = f;

    }

    private void addWord(File dictFile, String word) throws IOException {

		try {
			writeFileLock.acquire(MAX_CLIENTS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		PrintWriter pr = null;
		BufferedReader br = null;
		File temp = new File(Paths.get(System.getProperty("user.dir"), "temp.txt").toUri());
		temp.setExecutable(dictFile.canExecute());
		temp.setReadable(dictFile.canRead());
		temp.setWritable(true);
		try {
			temp.createNewFile();
			pr = new PrintWriter(new FileWriter(temp, true));
			br = new BufferedReader(new FileReader(dictFile));
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}

		String line = "";
		boolean wroteWord = false;
		while ((line = br.readLine()) != null) {

			if (line.equals(word.toUpperCase()))
				wroteWord = true;

			if (!wroteWord && line.matches("([A-Z0-9-])+") && line.compareTo(word.toUpperCase()) > 0) {
				pr.println(word.toUpperCase() + "\n");
				wroteWord = true;
			}

			if (!wroteWord && line.equals("End of Project Gutenberg's Webster's Unabridged Dictionary, by Various")) {

				pr.println(word.toUpperCase() + "\n");
				wroteWord = true;

			}
			pr.println(line);

		}
		temp.setWritable(dictFile.canWrite());
		dictFile.delete();
		temp.renameTo(dictFile);
		br.close();
		pr.close();
		writeFileLock.release(MAX_CLIENTS);

	}

    public String getDef(String word) throws InterruptedException, RemoteException {

    	word = word.toUpperCase();
    	System.out.println("Thread ID: " + Thread.currentThread().getId() + "-- searching for word: " + word + " in the dictionary.");
		BufferedReader br = null;
		writeFileLock.acquire();
		try {
			br = new BufferedReader(new FileReader(dictFile));
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		String line = "";
		String output = "";

		if (word.equals("")) {

			writeFileLock.release();
			return "No word given\r\n";
		}

		try {

			boolean wordFound = false;
			while ((line = br.readLine()) != null) {

				if (line.equals(word)) {

					wordFound = true;
					output += line;
					output += "\r\n";
					while((line = br.readLine()) != null) {

						if (line.matches("([A-Z0-9-])+") && !line.equals(word) && !line.equals(""))
							break;
						if (line.equals("End of Project Gutenberg's Webster's Unabridged Dictionary, by Various")) {

							line = null;
							break;

						}
						output += line;
						output += "\r\n";

					}
					if (line == null)
						break;

				}
			}
			writeFileLock.release();
			if (!wordFound) {

				output += word;
				output += " not found. Adding word to dictionary.\r\n";
				System.out.println("Thread ID: " + Thread.currentThread().getId() + "-- did not find " + word + " in the dictionary.");
				System.out.println("Thread ID: " + Thread.currentThread().getId() + "-- adding: " + word + " to the dictionary.");
			    addWord(dictFile, word);

			}

		}
		catch (IOException e) {
			e.printStackTrace();
		}

		try {
			br.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return output;

	}

    public static void startRMI(File f, String ip) throws RemoteException, MalformedURLException {

    	System.out.println("RMI server started");

        try { //special exception handler for registry creation

            LocateRegistry.createRegistry(1099); 
            System.out.println("java RMI registry created.");

        }
        catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.out.println("java RMI registry already exists.");
        }

        //Instantiate RmiServer
        RMI_LookUpServer obj = new RMI_LookUpServer(f);

        // Bind this object instance to the name "RmiServer"
        System.setProperty("java.rmi.server.hostname", ip);
        InetAddress addr = null;
		try {
			addr = InetAddress.getByName(ip);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
        String host = addr.getHostName();
        Naming.rebind("//" + host + ":1099" + "/RMI_LookUpServer", obj);
        System.out.println("Server bound in registry at: " + "//" + host + ":1099" + "/RMI_LookUpServer");

    }
    
    public static void main(String args[]) {

		try {

			String ip = InetAddress.getLocalHost().getHostAddress();
		    File file = Paths.get(System.getProperty("user.dir"), "dictionary.txt").toFile();
		    String filePath = "";

			if (args.length > 0) {

				if (args.length > 1) {

			    	ip = args[0];
			    	filePath = args[1];

				}
				else {

					ip = args[0];

				}

			}

			if (!filePath.equals(""))
				file = new File(filePath);

			if (file.exists() && !file.isDirectory() && file.isFile() && file.canRead()) {

				startRMI(file, ip);

			}
			else {

				System.err.println("Error: " + file.getPath() + " does not exists, it is not a regular file, or it cannont be read.");
				System.exit(1);

			}

		}
		catch (IOException e) {
            e.printStackTrace();
		}

    }

}