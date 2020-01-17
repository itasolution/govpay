package it.govpay.rs.v1.authentication.handler;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.UtilsException;
import org.openspcoop2.utils.service.context.ContextThreadLocal;
import org.openspcoop2.utils.service.context.IContext;
import org.springframework.security.core.Authentication;

import it.govpay.core.beans.Costanti;
import it.govpay.core.utils.EventoContext.Componente;
import it.govpay.core.utils.EventoContext.Esito;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.service.context.GpContextFactory;
import it.govpay.rs.v1.exception.CodiceEccezione;

public class RedirectAuthenticationSuccessHandler extends org.openspcoop2.utils.service.authentication.handler.jaxrs.DefaultAuthenticationSuccessHandler{

	@Override
	public Response getPayload(HttpServletRequest request, HttpServletResponse res, Authentication authentication) {
		IContext ctx = null;
		try{
			ctx = ContextThreadLocal.get();
			
			if(ctx == null) {
				GpContextFactory factory  = new GpContextFactory();
				String user = authentication != null ? authentication.getName() : null;
				ctx = factory.newContext(request.getRequestURI(), "profilo", "getProfilo", request.getMethod(), 1, user, Componente.API_PAGAMENTO);
				ContextThreadLocal.set(ctx);
			}
			
			GpContext gpContext = (GpContext) ctx.getApplicationContext();
			
			gpContext.getEventoCtx().setEsito(Esito.OK);
			gpContext.getEventoCtx().setIdTransazione(ctx.getTransactionId());
			
			String redirectUrl = "http://localhost:8080/govpay/frontend/api/pagamento/rs/cookie/v1/profilo";
			
			return Response.seeOther(new URI(redirectUrl)).header(Costanti.HEADER_NAME_OUTPUT_TRANSACTION_ID, ctx.getTransactionId()).build();
		}catch (Exception e) {
			return CodiceEccezione.AUTENTICAZIONE.toFaultResponse(e);
		} finally {
			if(ctx != null)
				try {
					ctx.getApplicationLogger().log();
				} catch (UtilsException e) {
					LoggerWrapperFactory.getLogger(getClass()).error("Errore durante il log dell'operazione: "+e.getMessage(), e);
				}
		}
		
		
	}
}
