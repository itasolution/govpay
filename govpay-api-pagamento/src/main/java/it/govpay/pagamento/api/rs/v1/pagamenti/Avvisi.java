package it.govpay.pagamento.api.rs.v1.pagamenti;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.rs.v1.controllers.base.AvvisiController;


@Path("/avvisi")

public class Avvisi extends BaseRsServiceV1{


	private AvvisiController controller = null;

	public Avvisi() throws ServiceException {
		super("avvisi");
		this.controller = new AvvisiController(this.nomeServizio,this.log);
	}



    @GET
    @Path("/{idDominio}/{iuv}")
    
    @Produces({ "application/json", "application/pdf" })
    public Response avvisiIdDominioIuvGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("idDominio") String idDominio, @PathParam("iuv") String iuv){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.avvisiIdDominioIuvGET(this.getUser(), uriInfo, httpHeaders,  idDominio,  iuv);
    }

}


