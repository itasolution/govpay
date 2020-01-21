package it.govpay.user.v1.controller;

import java.net.URI;
import java.text.MessageFormat;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.springframework.security.core.Authentication;

public class LoginController extends BaseController {

     public LoginController(String nomeServizio,Logger log) {
		super(nomeServizio,log);
     }

    public Response login(Authentication user, UriInfo uriInfo, HttpHeaders httpHeaders , String redirectURL) {
    	String methodName = "login";  
		String transactionId = this.context.getTransactionId();
		this.log.debug(MessageFormat.format(BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			if(redirectURL == null) {
				this.log.debug(MessageFormat.format(BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName));
				return this.handleResponseOk(Response.status(Status.OK),transactionId).build();
			} else {
				this.log.info("Esecuzione " + methodName + " completata con redirect verso la URL ["+ redirectURL +"].");	
				return this.handleResponseOk(Response.seeOther(new URI(redirectURL)),transactionId).build();
			}
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			this.log(this.context);
		}
    }


}


