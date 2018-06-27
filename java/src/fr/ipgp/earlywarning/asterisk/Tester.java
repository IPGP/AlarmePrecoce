package fr.ipgp.earlywarning.asterisk;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class allows us to test the CallOriginator / AGI script interactions (and inherently, Asterisk originate feature).
 * Also, it's (a little bit too) well explained, so if anyone is picking up the project from here, it should be a good starting point for understanding the system.
 * @author Thomas Kowalski
 */
public class Tester {
    public static void main(String[] args) throws Exception {
        // Start the AGI server.
        // It will handle the AGI request from Asterisk and will tell it what to do
        // What is great for us is that at the same time, it will communicate with the CallOriginator
        // For example, it'll tell it when the call is picked up, what code was entered...
        @SuppressWarnings("unused") LocalAgiServer server = new LocalAgiServer();

        // The phone list
        List<String> callList = new ArrayList<>();
        callList.add("0262275596");
        callList.add("0262275621");
        callList.add("0783841930");

        // The confirmation code
        String code = "1256";

        // Create a ManagerFactory.
        // Creating one here allows us to use the same for multiple calls with the CallOriginator
        ManagerConnectionFactory factory = new ManagerConnectionFactory("195.83.188.41", 5038, "manager", "ovpf");
        ManagerConnection managerConnection = factory.createManagerConnection();

        // Construct the CallOriginator with our ManagerConnection and the confirmation code defined up
        // The CallOriginator's task is to emit the call and then wait for data from the AGI script.
        // The AGI script doesn't do any choice (it doesn't know the confirmation code)
        // So the AGI script sends signals to the CallOriginator, the latter answers what it should do (retry, give up...)
        CallOriginator originator = new CallOriginator(managerConnection, code);

        // CallOriginator.call gives us a CallResult. We'll use it as a loop variable and define it as Initial (first iteration)
        CallOriginator.CallResult result = CallOriginator.CallResult.Initial;

        // We're gonna iterate over a simple call list: an ArrayList
        Iterator<String> it = callList.iterator();

        while ((result == CallOriginator.CallResult.Initial // If it's the first iteration
                || result != CallOriginator.CallResult.CorrectCode) // Or if the call didn't result in a correct code
                && it.hasNext()) { // AND, either way, if we still have people to call

            // Get the next phone number
            String number = it.next();

            // Originate the call and store the result
            // This call is blocking the thread, btw.
            result = originator.call(number);

            if (result != CallOriginator.CallResult.CorrectCode) {
                // If we didn't get a correct code, we're gonna retry
                // This is perfectible and could be simplified by moving it to the beginning of the loop
                // But we'll keep it here because it's simpler to understand
                // Wait a bit for Asterisk to finish its housekeeping work before retrying
                System.out.println("Waiting a bit.");
                Thread.sleep(3000);
            }
        }

        // Display the result of our calls
        if (result == CallOriginator.CallResult.CorrectCode)
            System.out.println("Correct code!");
        else if (!it.hasNext())
            System.err.println("No one left to call.");
        else
            // This, here, should not happen.
            throw new IllegalStateException("hasNext and not CorrectCode");

        // Close the connection.
        // It's not necessary in this case (because it's the end of the program anyway)
        // But in a real-world case, we don't want to keep a useless open connection.
        managerConnection.logoff();
    }
}
