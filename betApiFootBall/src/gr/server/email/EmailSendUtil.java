package gr.server.email;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSendUtil {
	
	public static void doSend(String email) {
		
		  String host="smtp.gmail.com";  
		  final String user="";//change accordingly  
		  final String password="";//   change accordingly  
		    		  
		   //Get the session object  
		   Properties props = new Properties();  
		   props.put("mail.smtp.host",host);
		   props.put("mail.smtp.port",587); 
		   props.put("mail.smtp.starttls.enable", "true"); 
		   props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		   props.put("mail.smtp.ssl.trust", "*");
		   props.put("mail.smtp.auth", "true");  
		     
		   Session session = Session.getDefaultInstance(props,  
		    new javax.mail.Authenticator() {  
		      protected PasswordAuthentication getPasswordAuthentication() {  
		    return new PasswordAuthentication(user,password);  
		      }  
		    });  
		  
		   //Compose the message  
		    try {  
		     MimeMessage message = new MimeMessage(session);  
		     message.setFrom(new InternetAddress(user));  
		     message.addRecipient(Message.RecipientType.TO,new InternetAddress(email));  
		     message.setSubject("flutter athentication email");  
		    // message.setText("<html><body><a href=\"http://192.168.1.2:8080/betCoreServer/betServer/validateUser/" + to +"\"> Click to validate your registration</a></body></html>");  
		       
		     
		     message.setContent("<html><body><a href=\"http://192.168.1.2:8080/betCoreServer/betServer/"+ email +"/validateUser/\"> Click to validate your registration</a></body></html>",  "text/html");
		    //send the message  
		     Transport.send(message);  
		  
		     System.out.println("message sent successfully...");  
		   
		     } catch (MessagingException e) {e.printStackTrace();}  
		 }  
	


}
