package it.govpay.user.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.user.v1.authentication.handler.RedirectAuthenticationSuccessHandler;
import it.govpay.user.v1.controller.LoginController;


@Path("/login")

public class Login extends BaseRsServiceV1{


	private LoginController controller = null;

	public Login() throws ServiceException {
		super("login");
		this.controller = new LoginController(this.nomeServizio,this.log);
	}



    @GET
    @Path("/")
    
    
    public Response login(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @QueryParam(RedirectAuthenticationSuccessHandler.REDIRECT_URL_PARAMETER_NAME) String redirectURL){
        this.controller.setContext(this.getContext());
        return this.controller.login(this.getUser(), uriInfo, httpHeaders, redirectURL);
    }

}


