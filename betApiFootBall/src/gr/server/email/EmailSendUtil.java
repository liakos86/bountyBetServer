package gr.server.email;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import gr.server.common.CommonConstants;
import gr.server.common.IgnoreConstants;
import gr.server.common.logging.CommonLogger;
import gr.server.common.util.FileHelperUtils;

public class EmailSendUtil {
	
	public static boolean doSend(String email) {
		  final String host= IgnoreConstants.SMTP_HOST;  
		  final String user= IgnoreConstants.MAIL_USER;
		  final String password= IgnoreConstants.MAIL_PASS;  
		    		  
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
		     message.setSubject("FantasyTips authentication email");  
		       
            String template;
			try {
				template = new FileHelperUtils().getFileContents("html/send_email.html");
			} catch (IOException e) {
				e.printStackTrace();
				CommonLogger.logger.error(e.getMessage());
				return false;
			}

            // Replace placeholders
            template = template.replace("{SERVER_IP}", CommonConstants.SERVER_IP);

            template = template.replace("{EMAIL}", email);
	     
            message.setContent(template,  "text/html");
		     
		     //send the message  
		     Transport.send(message);  
		  
		     //System.out.println("message sent successfully...");  
		   
		     } catch (MessagingException e) {
		    	 e.printStackTrace();
		    	 CommonLogger.logger.error("EmailSendUtil " + e.getMessage());
		    	 return false;
		     }
		    
		    return true;
		 }  
	
}
