/**
 * 
 */
package fr.ipgp.clickatell;

import java.net.UnknownHostException;
/**
 * @author boissier
 *
 */
public class ClickatellJava {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("SMS sending test");
		// Account parameters
		String username = "boissier";
		String password = "monq1976";
		String apiId = "3364793";
		
		// Create New object (Assign auth straight away)
		ClickatellHttp click = new ClickatellHttp(username, apiId, password);
		
		try {
			if (click.testAuth()) {
				System.out.println("Authentication was successful");
			} else {
				System.out.println("Your authentication details are not correct");
			}
			
			// Get account balance
			double responseBalance = click.getBalance();
			System.out.println("Balance: " + responseBalance);
			
			// Send SMS message
			ClickatellHttp.Message response = click.sendMessage("262692703856", "Hello, this is a test message!");
			System.out.println("Response: "+ response);
			if (response.error != null) {
				System.out.println("Error: " + response.error);
			} else {
				System.out.println("Status: " + click.getMessageStatus(response.message_id));
				ClickatellHttp.Message replies = click.getMessageCharge(response.message_id);
				System.out.println("Charge: " + replies.charge);
				System.out.println("Status: " + replies.status);
			}

		} catch (UnknownHostException e) {
			System.out.println("Host could not be found");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		
	}
}
