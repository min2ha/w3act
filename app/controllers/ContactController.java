package controllers;

import models.User;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security;
import uk.bl.Const;
import uk.bl.scope.EmailHelper;
import views.html.contact;

/**
 * Describe W3ACT project contacts.
 */
@Security.Authenticated(SecuredController.class)
public class ContactController extends AbstractController {
  
    /**
     * Display the About tab.
     */
    public static Result index() {
		return ok(
            contact.render("Contact", User.findByEmail(request().username()))
        );
    }
    
    /**
     * This method extracts form values and sends an email.
     */
    public static Result send() {
		Logger.debug("send contact request");
        String name = getFormParam(Const.NAME);
        String messageSubject = getFormParam(Const.SUBJECT);
        String addMail = getFormParam(Const.FROM);
        String toMail = Const.BL_MAIL_ADDRESS;
        String content = getFormParam(Const.CONTENT);
        String messageBody = "User name is " + name + "\n" + "User email is " + addMail + "\n" + content;
        Logger.debug("subject: " + messageSubject + ", to: " + toMail + ", messageBody: " + messageBody);
        EmailHelper.sendMessage(toMail, messageSubject, messageBody);                	

        Result res = redirect(routes.ContactController.index()); 
        return res;
    }
    
	
}

