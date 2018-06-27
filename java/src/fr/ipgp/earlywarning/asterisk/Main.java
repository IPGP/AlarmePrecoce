import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        @SuppressWarnings("unused") LocalAgiServer server = new LocalAgiServer();

        // The phone list
        List<String> callList = new ArrayList<>();
        callList.add("0262275596");
        callList.add("0262275621");
        callList.add("0783841930");

        String code = "1256";

        // CallOriginator callout = new CallOriginator("localhost", 5038, "manager", "ovpf", code);

        ManagerConnectionFactory factory = new ManagerConnectionFactory("localhost", 5038, "manager", "ovpf");
        ManagerConnection managerConnection = factory.createManagerConnection();

        CallOriginator callout = new CallOriginator(managerConnection, code);

        CallOriginator.CallResult result = CallOriginator.CallResult.IncorrectCode;
        Iterator<String> it = callList.iterator();
        while (result != CallOriginator.CallResult.CorrectCode && it.hasNext()) {
            String number = it.next();
            result = callout.call(number);
            // Wait a bit for Asterisk to finish its housekeeping work before retrying
            System.out.println("Waiting a bit.");
            Thread.sleep(3000);
        }

        if (result == CallOriginator.CallResult.CorrectCode)
            System.out.println("Correct code!");
        else if (!it.hasNext())
            System.err.println("No one left to call.");
        else
            throw new IllegalStateException("hasNext and not CorrectCode");

        managerConnection.logoff();
    }
}
