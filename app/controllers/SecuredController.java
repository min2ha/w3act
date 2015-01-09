package controllers;

import play.mvc.*;
import play.mvc.Http.*;

public class SecuredController extends Security.Authenticator {
    
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }
    
    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.ApplicationController.login());
    }
    
    // Access rights
    
    public static boolean isMemberOf(Long project) {
        return true;
    }
    
    public static boolean isOwnerOf(Long task) {
        return true;
    }
    
}