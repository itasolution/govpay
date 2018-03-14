package it.govpay.ragioneria.api.rs.v1.ragioneria;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import it.govpay.core.rs.v1.costanti.Costanti;
import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.rs.v1.controllers.base.IncassiController;


@Path("/incassi")

public class Incassi extends BaseRsServiceV1{


	private IncassiController controller = null;

	public Incassi() {
		super("incassi");
		this.controller = new IncassiController(this.nomeServizio,this.log);
	}



    @GET
    @Path("/")
    
    @Produces({ "application/json" })
    public Response incassiGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @QueryParam(value=Costanti.PARAMETRO_PAGINA) @DefaultValue(value="1") Integer pagina, @QueryParam(value=Costanti.PARAMETRO_RISULTATI_PER_PAGINA) @DefaultValue(value="25") Integer risultatiPerPagina){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.incassiGET(this.getUser(), uriInfo, httpHeaders, pagina, risultatiPerPagina);
    }

    @POST
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response incassiPOST(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, java.io.InputStream is){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.incassiPOST(this.getUser(), uriInfo, httpHeaders, is);
    }

    @GET
    @Path("/{idDominio}/{idIncasso}")
    
    @Produces({ "application/json" })
    public Response incassiIdGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("idDominio") String idDominio, @PathParam("idIncasso") String idIncasso){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.incassiIdGET(this.getUser(), uriInfo, httpHeaders, idDominio, idIncasso);
    }

}


