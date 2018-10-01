package gov.nara.opa.api.user.accounts.email;

/**
 * Contains the content strings of the user account registration emails
 */
public class UserAccountEmailBody {

	private static final String centeredImageEmail = "<img style=\"vertical-align:middle\" src=\"%1$s\" alt=\"NARA Logo\" height=\"110\" width=\"110\" />";

	public static final String registrationBody = "<html> <head><title>User Registration</title> </head> "
			+ "<body> <p style=\"border-style: none none solid none; border-width: thin; border-color: #000000; font-family: Arial, Helvetica, sans-serif; font-size: medium; font-weight: bold; font-style: normal; font-variant: normal; text-transform: none; color: #000000;\">     "
			+ "Thank you for registering for a Catalog User Account </p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     %4$s </p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     "
			+ "Thank you for registering for a National Archives Catalog account at the National Archives and Records Administration. You are registered with the following      information. </p>  "
			+ "<ul>     <li style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         User Name: %1$s     </li>     "
			+ "<li style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         Full Name: %2$s     </li> </ul> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     "
			+ "Click on this link below to complete the registration process. </p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     %3$s </p> <p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     Your friends on the Online Catalog Team <br />     National Archives and Records Administration</p> </body> </html>";

	public static final String passwordChangeRequestBody = "<html> <head>     <title>Password change confirmation</title> </head> "
			+ "<body>     <p style=\"border-style: none none solid none; border-width: thin; border-color: #000000; font-family: Arial, Helvetica, sans-serif; font-size: medium; font-weight: bold; font-style: normal; font-variant: normal; text-transform: none; color: #000000;\">         "
			+ "Catalog User Account Change Confirmation</p>     "
			+ "<p>         &nbsp;%3$s</p>     "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "You requested to reset your password. Click on below link to reset your password:</p>"
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "%1$s</p>     "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "If you did not request to reset your password, contact %2$s immediately.     </p>     "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "Your friends on the Online Catalog Team         <br />         National Archives and Records Administration</p> </body> </html>";

	public static final String forgotUsernameBody = "<html> <head> <title>Forgot username</title> </head> "
			+ "<body>      <p style=\"border-style: none none solid none; border-width: thin; border-color: #000000; font-family: Arial, Helvetica, sans-serif; font-size: medium; font-weight: bold; font-style: normal; font-variant: normal; text-transform: none; color: #000000;\">         "
			+ "Your Catalog Username</p> "
			+ "<p>         &nbsp;%3$s</p>     "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "You requested a reminder of your Username.     </p>     "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "Your Username is: %1$s</p>     "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "If you did not request your username, contact %2$s immediately.</p>     "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">         "
			+ "Your friends on the Online Catalog Team         <br />         "
			+ "National Archives and Records Administration</p>  </body>  </html>";

	public static final String deactivatedAccount = "<html> <head> <title>Catalog User Account Change Confirmation</title> </head>"
			+ "<body> <p style=\"border-style: none none solid none; border-width: thin; border-color: #000000; font-family: Arial, Helvetica, sans-serif; font-size: medium; font-weight: bold; font-style: normal; font-variant: normal; text-transform: none; color: #000000;\"> "
			+ "Catalog User Account Change Confirmation</p> "
			+ "<p> &nbsp;%2$s</p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "You have deactivated your account.</p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "If you did not deactivate your account, or if you would like to re-activate your account, contact %1$s immediately. </p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "Your friends on the Online Catalog Team <br /> "
			+ "National Archives and Records Administration</p> </body> </html>";

	public static final String deactivationWarning = "<html> <head> <title>Important Information About Your Catalog Account</title> </head>"
			+ "<body> <p style=\"border-style: none none solid none; border-width: thin; border-color: #000000; font-family: Arial, Helvetica, sans-serif; font-size: medium; font-weight: bold; font-style: normal; font-variant: normal; text-transform: none; color: #000000;\"> "
			+ "Important Information About Your Catalog Account</p> "
			+ "<p> &nbsp;%3$s</p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "Your Catalog account has been inactive for some time. In %2$s, it will be deactivated, and all saved data associated with your account will be lost. To prevent this, please "
			+ "log into your account.</p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "If you have any questions or concerns, contact %1$s.</p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "Your friends on the Online Catalog Team <br /> "
			+ "National Archives and Records Administration</p> </body> </html>";

	public static final String emailVerification = "<html> <head><title>Email address change request</title> </head> "
			+ "<body> <p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">"
			+ centeredImageEmail
			+ "<span> A request has been received to change the email address for National Archives Catalog username <em>%2$s</em></span></p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     "
			+ "Click the link below to complete the email address change:</p>  "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     %3$s </p> "
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "If you are the owner of this account but did not request this change, please contact us on 1-800-555-5555 or <a href=\"mailto:catalog@nara.gov\">catalog@nara.gov</a> </p>"
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\">     "
			+ "If you are not the owner of this account, please ignore this message. </p>"
			+ "<p style=\"font-family: Arial, Helvetica, sans-serif; font-size: smaller; font-weight: normal; font-style: normal; font-variant: normal; text-transform: none; color: #000000\"> "
			+ "Your friends on the National Archives Catalog team <br /> "
			+ "U.S. National Archives and Records Administration</p> </body> </html>";
}
