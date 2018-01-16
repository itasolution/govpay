package it.govpay.pagamento.api.rs.pagamenti;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import it.govpay.core.dao.pagamenti.PagamentiPortaleDAO;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTO;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTOResponse;
import it.govpay.core.utils.GpContext;
import it.govpay.pagamento.api.rs.BaseRsService;
import it.govpay.pagamento.api.rs.pagamenti.v1.converter.PagamentiPortaleConverter;
import it.govpay.pagamento.api.rs.pagamenti.v1.model.FaultBean;
import it.govpay.pagamento.api.rs.pagamenti.v1.model.FaultBean.CATEGORIA;
import it.govpay.pagamento.api.rs.pagamenti.v1.model.PagamentiPortaleRequest;
import it.govpay.pagamento.api.rs.pagamenti.v1.model.PagamentiPortaleResponseOk;

@Path("/")
public class PagamentiPortale extends BaseRsService{
	
	
	public PagamentiPortale() {
		super();
	}

	@POST
	@Path("/pagamenti")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response pagamenti(InputStream is , @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders) {
		String methodName = "pagamenti";  
		GpContext ctx = null;
		ByteArrayOutputStream baos= null;
		this.log.info("Esecuzione " + methodName + " in corso..."); 
		try{
			baos = new ByteArrayOutputStream();
			// salvo il json ricevuto
			copy(is, baos);
			//this.logRequest(uriInfo, httpHeaders, methodName, baos);
			//ctx =  GpThreadLocal.get();
			String principal = this.getPrincipal();
			
			PagamentiPortaleRequest pagamentiPortaleRequest = PagamentiPortaleConverter.readFromJson(baos);
			
			PagamentiPortaleDTO pagamentiPortaleDTO = PagamentiPortaleConverter.getPagamentiPortaleDTO(pagamentiPortaleRequest, baos.toString(), principal);
			
			PagamentiPortaleDAO pagamentiPortaleDAO = new PagamentiPortaleDAO();
			
			PagamentiPortaleDTOResponse pagamentiPortaleDTOResponse = pagamentiPortaleDAO.inserisciPagamenti(pagamentiPortaleDTO);
						
			PagamentiPortaleResponseOk responseOk = PagamentiPortaleConverter.getPagamentiPortaleResponseOk(pagamentiPortaleDTOResponse);
			
			this.log.info("Esecuzione " + methodName + " completata."); 
			return Response.status(Status.OK).entity(responseOk).build();
		} catch (Exception e) {
			log.error("Errore interno durante il processo di pagamento", e);
			FaultBean respKo = new FaultBean();
			respKo.setCategoria(CATEGORIA.INTERNO);
			respKo.setCodice("");
			respKo.setDescrizione(e.getMessage());
			
			//this.logResponse(uriInfo, httpHeaders, methodName, new byte[0], 500);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(respKo).build();
		} finally {
			if(ctx != null) ctx.log();
		}
		
	}
}
