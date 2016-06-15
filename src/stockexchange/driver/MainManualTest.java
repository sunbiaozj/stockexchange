package stockexchange.driver;


import stockexchange.client.User;
import stockexchange.client.UserImpl;
import stockexchange.common.GlobalConstants.MarketState;
import stockexchange.common.InvalidDataException;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import stockexchange.book.ProductService;


public class MainManualTest {

    public static CountDownLatch countDownLatch;

    public static void main(String[] args) {
        setupTradingSystem();
        try {
			manualTestMode();
		} catch (InvalidDataException e) {
			e.printStackTrace();
		}
    }


    private static void setupTradingSystem() {
        try {
            ProductService.getInstance().createProduct("IBM");
            ProductService.getInstance().createProduct("CBOE");
            ProductService.getInstance().createProduct("GOOG");
            ProductService.getInstance().createProduct("AAPL");
            ProductService.getInstance().createProduct("GE");
            ProductService.getInstance().createProduct("T");
            ProductService.getInstance().setMarketState(MarketState.PREOPEN); // Replace PREOPEN with your preresenation of PREOPEN
            ProductService.getInstance().setMarketState(MarketState.OPEN);  // Replace OPEN with your preresenation of OPEN
        } catch (Exception ex) {
            Logger.getLogger(MainManualTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void manualTestMode() throws InvalidDataException {

        String name = "REX";
        boolean goodName = false;
        String error = "";
        int errorCount = 0;
        do {
            name = JOptionPane.showInputDialog(null, error + "Enter your First name", "Name", JOptionPane.INFORMATION_MESSAGE);

            if (name == null) {
                System.out.println("No Name provided - Defaulting to 'REX'");
                name = "REX";
                goodName = true;
                JOptionPane.showMessageDialog(null, "You have been assigned the default user name of 'REX'", "Default Name", JOptionPane.INFORMATION_MESSAGE);
            } else if (name.matches("^[a-zA-Z]+$")) {
                goodName = true;
            } else {
                errorCount++;
                if (errorCount >= 3) {
                    System.out.println("Too many tried - Defaulting to 'REX'");
                    name = "REX";
                    goodName = true;
                    JOptionPane.showMessageDialog(null, "You have been assigned the default user name of 'REX'", "Default Name", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    error = "Names must consist of letters only - please try again.\n\n";
                }
            }
        } while (!goodName);
        
                            
        JOptionPane.showMessageDialog(null, "User '" + name.toUpperCase() + "' and 'ANN' are connected.", "Users", JOptionPane.INFORMATION_MESSAGE);

        User user1 = new UserImpl(name.toUpperCase());
        User user2 = new UserImpl("ANN");

        try {
            user1.connect();
            user1.showMarketDisplay();
            user2.connect();
            user2.showMarketDisplay();
        } catch (Exception ex) {
            Logger.getLogger(MainManualTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
