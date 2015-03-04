package dictionaryLookUpRMI_ParalWrite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RMI_LookUpClient { 

	private File wordsFile = null;

	public RMI_LookUpClient(File f) {

		super();
		wordsFile = f;

	}

	public String getWord(BufferedReader br) {

		String word = "";
        do {

		    try {
		        word = br.readLine();
		    }
		    catch (IOException ioe) {
		        System.err.println("getWord() IO error trying to read word, because:" + ioe.getLocalizedMessage());
		        System.exit(1);
		    }
		    if (word == null)
		    	return word;
		    word = word.trim();

        } while (word.equals(""));

	    word = word.toUpperCase();
		return word;

	}

	public void runClient(RMI_LookUpInterface rlui) throws RemoteException, InterruptedException {

		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(wordsFile));

		}
		catch (IOException e1) {
			e1.printStackTrace();
		}

		String word = "";
		while ((word = getWord(br)) != null)
			System.out.println(rlui.getDef(word) + "\n");

		try {
			br.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void startClient(String ip) throws MalformedURLException, RemoteException, NotBoundException {

		
		RMI_LookUpInterface rlui = (RMI_LookUpInterface)Naming.lookup("//" + ip + "/RMI_LookUpServer");
        try {
			runClient(rlui);
		}
        catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

    public static void main(String args[]) throws Exception {

		try {

			String ip = InetAddress.getLocalHost().getHostAddress();
		    File file = Paths.get(System.getProperty("user.dir"), "words.txt").toFile();
		    String filePath = "";

			if (args.length > 0) {

			    if (args.length > 1) {

			    	if (args[0].contains(".") && !args[1].contains(".")) {

			    		ip = args[0];
			    		filePath = args[1];

			    	}
			    	else if (!args[0].contains(".") && args[1].contains(".")) {

			    		ip = args[1];
			    		filePath = args[0];

			    	}

				}
				else {

					if (args[0].contains("."))
			    		ip = args[0];
			    	else
			    		filePath = args[0];

				}

			}

			if (!filePath.equals(""))
				file = new File(filePath);

			if (file.exists() && !file.isDirectory() && file.isFile() && file.canRead()) {

				RMI_LookUpClient rluc = new RMI_LookUpClient(file);
				rluc.startClient(ip);

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