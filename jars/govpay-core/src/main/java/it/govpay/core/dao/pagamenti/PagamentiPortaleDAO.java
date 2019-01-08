package it.govpay.core.dao.pagamenti;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.json.ValidationException;
import org.openspcoop2.utils.logger.beans.Property;
import org.slf4j.Logger;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.DominiBD;
import it.govpay.bd.model.Dominio;
import it.govpay.bd.model.Nota;
import it.govpay.bd.model.Nota.TipoNota;
import it.govpay.bd.model.PagamentoPortale;
import it.govpay.bd.model.PagamentoPortale.CODICE_STATO;
import it.govpay.bd.model.PagamentoPortale.STATO;
import it.govpay.bd.model.eventi.EventoNota;
import it.govpay.bd.model.Rpt;
import it.govpay.bd.model.SingoloVersamento;
import it.govpay.bd.model.UnitaOperativa;
import it.govpay.bd.model.Utenza;
import it.govpay.bd.model.Versamento;
import it.govpay.bd.pagamento.PagamentiPortaleBD;
import it.govpay.bd.pagamento.filters.PagamentoPortaleFilter;
import it.govpay.core.business.GiornaleEventi;
import it.govpay.core.dao.anagrafica.utils.UtenzaPatchUtils;
import it.govpay.core.dao.commons.BaseDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiPagamentoPortaleDTO;
import it.govpay.core.dao.pagamenti.dto.LeggiPagamentoPortaleDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaPagamentiPortaleDTO;
import it.govpay.core.dao.pagamenti.dto.ListaPagamentiPortaleDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaPendenzeDTO;
import it.govpay.core.dao.pagamenti.dto.ListaPendenzeDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaRptDTO;
import it.govpay.core.dao.pagamenti.dto.ListaRptDTOResponse;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTO;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTO.RefVersamentoAvviso;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTO.RefVersamentoPendenza;
import it.govpay.core.dao.pagamenti.dto.PagamentiPortaleDTOResponse;
import it.govpay.core.dao.pagamenti.dto.PagamentoPatchDTO;
import it.govpay.core.dao.pagamenti.exception.PagamentoPortaleNonTrovatoException;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.exceptions.NotAuthenticatedException;
import it.govpay.core.exceptions.NotAuthorizedException;
import it.govpay.core.rs.v1.beans.base.PatchOp;
import it.govpay.core.rs.v1.costanti.EsitoOperazione;
import it.govpay.core.utils.AclEngine;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.PagamentoPortaleUtils;
import it.govpay.core.utils.UrlUtils;
import it.govpay.core.utils.VersamentoUtils;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Anagrafica;
import it.govpay.model.Utenza.TIPO_UTENZA;
import it.govpay.orm.IdVersamento;
import it.govpay.servizi.v2_3.commons.Mittente;
import it.govpay.servizi.v2_5.gpprt.GpAvviaTransazionePagamentoResponse;
import it.govpay.servizi.v2_5.gpprt.GpAvviaTransazionePagamentoResponse.RifTransazione;

public class PagamentiPortaleDAO extends BaseDAO {
	
	private static final String PATH_NOTA = PendenzeDAO.PATH_NOTA;

	public PagamentiPortaleDAO() {
	}

	public PagamentiPortaleDTOResponse inserisciPagamenti(PagamentiPortaleDTO pagamentiPortaleDTO) throws GovPayException, NotAuthorizedException, ServiceException, NotAuthenticatedException {
		PagamentiPortaleDTOResponse response  = new PagamentiPortaleDTOResponse();
		GpAvviaTransazionePagamentoResponse transazioneResponse = new GpAvviaTransazionePagamentoResponse();
		Logger log = LoggerWrapperFactory.getLogger(WebControllerDAO.class);
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		
		try {
			bd = BasicBD.newInstance(ctx.getTransactionId());
			DominiBD dominiBD = new DominiBD(bd);
			GiornaleEventi giornaleEventi = new GiornaleEventi(bd);
			List<Versamento> versamenti = new ArrayList<>();

			// Aggiungo il codSessionePortale al PaymentContext
			ctx.getPagamentoCtx().setCodSessionePortale(pagamentiPortaleDTO.getIdSessionePortale());
			ctx.getContext().getRequest().addGenericProperty(new Property("codSessionePortale", pagamentiPortaleDTO.getIdSessionePortale() != null ? pagamentiPortaleDTO.getIdSessionePortale() : "--Non fornito--"));

			ctx.log("ws.ricevutaRichiesta");
			this.autorizzaRichiesta(pagamentiPortaleDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.SCRITTURA,true); 
			ctx.log("ws.autorizzazione");

			String codDominio = null;
			String nome = null;
			List<IdVersamento> idVersamento = new ArrayList<>();
			it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento(bd);
			StringBuilder sbNomeVersamenti = new StringBuilder();
			List<String> listaMultibeneficiari = new ArrayList<>();
			Anagrafica versanteModel = VersamentoUtils.toAnagraficaModel(pagamentiPortaleDTO.getVersante());
			// 1. Lista Id_versamento
			for(int i = 0; i < pagamentiPortaleDTO.getPendenzeOrPendenzeRef().size(); i++) {
				Object v = pagamentiPortaleDTO.getPendenzeOrPendenzeRef().get(i);
				Versamento versamentoModel = null;
				if(v instanceof it.govpay.core.dao.commons.Versamento) {
					it.govpay.core.dao.commons.Versamento versamento = (it.govpay.core.dao.commons.Versamento) v;
					ctx.log("rpt.acquisizioneVersamento", versamento.getCodApplicazione(), versamento.getCodVersamentoEnte());
					versamentoModel = versamentoBusiness.chiediVersamento(versamento);
					
					// se l'utenza che ha caricato la pendenza inline e' un cittadino sono necessari dei controlli supplementari.
					if(pagamentiPortaleDTO.getUser().getTipoUtenza().equals(TIPO_UTENZA.CITTADINO)) {
						for (SingoloVersamento sv : versamentoModel.getSingoliVersamenti(bd)) {
							// il tributo deve essere passato all'interno della pendenza
							if(sv.getTributo(bd) == null) {
								throw new GovPayException(EsitoOperazione.CIT_001, pagamentiPortaleDTO.getUser().getIdentificativo(),versamentoModel.getApplicazione(bd).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());
							}
									
							// se il tributo non e' online non puo' essere pagato spontaneamente
							if(!sv.getTributo(bd).isOnline()) {
								throw new GovPayException(EsitoOperazione.CIT_002, pagamentiPortaleDTO.getUser().getIdentificativo(),versamentoModel.getApplicazione(bd).getCodApplicazione(), versamentoModel.getCodVersamentoEnte(),sv.getTributo(bd).getCodTributo());
							}
							
							// se il tributo non puo' essere pagato da terzi allora debitore e versante (se presente) devono coincidere con chi sta effettuando il pagamento.
							if(!sv.getTributo(bd).isPagaTerzi()) {
								if(!versamento.getDebitore().getCodUnivoco().equals(pagamentiPortaleDTO.getUser().getIdentificativo()))
									throw new GovPayException(EsitoOperazione.CIT_003, pagamentiPortaleDTO.getUser().getIdentificativo(),versamentoModel.getApplicazione(bd).getCodApplicazione(), versamentoModel.getCodVersamentoEnte(),versamento.getDebitore().getCodUnivoco());
								
								if(versanteModel != null && !versanteModel.getCodUnivoco().equals(pagamentiPortaleDTO.getUser().getIdentificativo()))
									throw new GovPayException(EsitoOperazione.CIT_004, pagamentiPortaleDTO.getUser().getIdentificativo(),versamentoModel.getApplicazione(bd).getCodApplicazione(), versamentoModel.getCodVersamentoEnte(),versanteModel.getCodUnivoco());
							}
						}
					}
				}  else if(v instanceof RefVersamentoAvviso) {
					String idDominio = ((RefVersamentoAvviso)v).getIdDominio();
					String cfToCheck = ((RefVersamentoAvviso)v).getIdDebitore();
					try {
						Dominio dominio = dominiBD.getDominio(idDominio);
						versamentoModel = versamentoBusiness.chiediVersamento((RefVersamentoAvviso)v,dominio);

						// controllo che l'utenza anonima possa effettuare il pagamento dell'avviso	
						this.autorizzaAccessoAnonimoVersamento(pagamentiPortaleDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.SCRITTURA, true, cfToCheck, versamentoModel.getAnagraficaDebitore().getCodUnivoco());
						
					}catch(NotFoundException e) {
						throw new GovPayException("Il pagamento non puo' essere avviato poiche' uno dei versamenti risulta associato ad un dominio non disponibile [Dominio:"+idDominio+"].", EsitoOperazione.DOM_000, idDominio);
					}
				}  else if(v instanceof RefVersamentoPendenza) {
					versamentoModel = versamentoBusiness.chiediVersamento((RefVersamentoPendenza)v);
				}

				if(versamentoModel != null) {
					UnitaOperativa uo = versamentoModel.getUo(bd);
					if(!uo.isAbilitato()) {
						throw new GovPayException("Il pagamento non puo' essere avviato poiche' uno dei versamenti risulta associato ad una unita' operativa disabilitata [Uo:"+uo.getCodUo()+"].", EsitoOperazione.UOP_001, uo.getCodUo());
					}
	
					Dominio dominio = uo.getDominio(bd); 
					if(!dominio.isAbilitato()) {
						throw new GovPayException("Il pagamento non puo' essere avviato poiche' uno dei versamenti risulta associato ad un dominio disabilitato [Dominio:"+dominio.getCodDominio()+"].", EsitoOperazione.DOM_001, dominio.getCodDominio());
					}
	
	
					if(i == 0) {
						// la prima pendenza da il nome al pagamento, eventualmente si appende il numero di pendenze ulteriori
						if(versamentoModel.getNome()!=null) {
							sbNomeVersamenti.append(versamentoModel.getNome());
						} else {
							try {
								sbNomeVersamenti.append(versamentoModel.getCausaleVersamento().getSimple());
							} catch (UnsupportedEncodingException e) {}						
						}
						if(pagamentiPortaleDTO.getPendenzeOrPendenzeRef().size() > 1) {
							sbNomeVersamenti.append(" ed altre "+pagamentiPortaleDTO.getPendenzeOrPendenzeRef().size()+" pendenze.");
						}
						
						// 	2. Codice dominio della prima pendenza
						codDominio = dominio.getCodDominio();
						// 3. ente creditore
					}
	
					versamenti.add(versamentoModel);
	
					// colleziono i domini inserendo solo se non presente in lista
					if(!listaMultibeneficiari.contains(dominio.getCodDominio()))
						listaMultibeneficiari.add(dominio.getCodDominio());
				}
			}

			// 5. somma degli importi delle pendenze
			double sommaImporti = 0;

			for (Versamento vTmp : versamenti) {
				sommaImporti += vTmp.getImportoTotale().doubleValue();
			}

			nome = sbNomeVersamenti.length() > 255 ? (sbNomeVersamenti.substring(0, 252) + "...") : sbNomeVersamenti.toString();
			STATO stato = null;
			CODICE_STATO codiceStato = null;
			String redirectUrl = null;
			String idSessionePsp = null;
			String pspRedirect = null;
			String codCanale = null;		
			String codPsp = null;
			String tipoVersamento = null;
			String descrizioneStato = null;

			PagamentoPortale pagamentoPortale = new PagamentoPortale();
			pagamentoPortale.setPrincipal(pagamentiPortaleDTO.getUser().getIdentificativo()); 
			pagamentoPortale.setTipoUtenza(pagamentiPortaleDTO.getUser().getTipoUtenza());
			pagamentoPortale.setIdSessione(pagamentiPortaleDTO.getIdSessione());
			pagamentoPortale.setIdSessionePortale(pagamentiPortaleDTO.getIdSessionePortale());
			pagamentoPortale.setJsonRequest(pagamentiPortaleDTO.getJsonRichiesta());
			if(pagamentiPortaleDTO.getUrlRitorno() != null)
				pagamentoPortale.setUrlRitorno(UrlUtils.addParameter(pagamentiPortaleDTO.getUrlRitorno() , "idPagamento",pagamentiPortaleDTO.getIdSessione()));
			pagamentoPortale.setDataRichiesta(new Date());
			pagamentoPortale.setWispIdDominio(codDominio);
			pagamentoPortale.setNome(nome);
			pagamentoPortale.setImporto(sommaImporti); 
			
			// gestione multibeneficiari
			// se ho solo un dominio all'interno della lista allora vuol dire che i tutti pagamenti riferiscono lo stesso dominio
			// lascio null se il numero di domini e' > 1
			if(listaMultibeneficiari.size() == 1) {
				pagamentoPortale.setMultiBeneficiario(listaMultibeneficiari.get(0)); 
			}

			pagamentoPortale.setStato(STATO.IN_CORSO);
			pagamentoPortale.setCodiceStato(CODICE_STATO.PAGAMENTO_IN_CORSO_AL_PSP);
			pagamentoPortale.setTipo(1); //Pagamento iniziativa ente
			PagamentiPortaleBD pagamentiPortaleBD = new PagamentiPortaleBD(bd);
			pagamentiPortaleBD.insertPagamento(pagamentoPortale);

			// procedo al pagamento
			it.govpay.core.business.Rpt rptBD = new it.govpay.core.business.Rpt(bd);
			List<Rpt> rpts = null;
			
			// sessione di pagamento non in corso
			codiceStato = CODICE_STATO.PAGAMENTO_IN_CORSO_AL_PSP;
			stato = STATO.IN_CORSO;

			try {
				
				
				rpts = rptBD.avviaTransazione(versamenti, (Utenza) pagamentiPortaleDTO.getUser(), null, pagamentiPortaleDTO.getIbanAddebito(), versanteModel, pagamentiPortaleDTO.getAutenticazioneSoggetto(), pagamentiPortaleDTO.getUrlRitorno(), true, pagamentoPortale);

				Rpt rpt = rpts.get(0);

				GpAvviaTransazionePagamentoResponse.RifTransazione rifTransazione = new GpAvviaTransazionePagamentoResponse.RifTransazione();
				rifTransazione.setCcp(rpt.getCcp());
				rifTransazione.setCodApplicazione(rpt.getVersamento(bd).getApplicazione(bd).getCodApplicazione());
				rifTransazione.setCodDominio(rpt.getCodDominio());
				rifTransazione.setCodVersamentoEnte(rpt.getVersamento(bd).getCodVersamentoEnte());
				rifTransazione.setIuv(rpt.getIuv());
				transazioneResponse.getRifTransazione().add(rifTransazione);

				// se ho un redirect 			
				if(rpt.getPspRedirectURL() != null) {
					codiceStato = CODICE_STATO.PAGAMENTO_IN_CORSO_AL_PSP;
					stato = STATO.IN_CORSO;
					idSessionePsp = rpt.getCodSessione();
					if(pagamentoPortale.getUrlRitorno()!= null)
						pagamentoPortale.setUrlRitorno(UrlUtils.addParameter(pagamentoPortale.getUrlRitorno() , "idSession", idSessionePsp));
					pspRedirect = rpt.getPspRedirectURL(); 
					response.setRedirectUrl(pspRedirect);
					response.setIdSessione(idSessionePsp); 
				} else {
					stato = STATO.IN_CORSO;
					codiceStato = CODICE_STATO.PAGAMENTO_IN_ATTESA_DI_ESITO;
					response.setRedirectUrl(pagamentiPortaleDTO.getUrlRitorno());
				}

				transazioneResponse.setPspSessionId(idSessionePsp);
				transazioneResponse.setUrlRedirect(redirectUrl);
				transazioneResponse.setCodEsito(EsitoOperazione.OK.toString());
				transazioneResponse.setDescrizioneEsito("Operazione completata con successo");
				transazioneResponse.setMittente(Mittente.GOV_PAY);

			}catch(GovPayException e) {
				transazioneResponse = (GpAvviaTransazionePagamentoResponse) e.getWsResponse(transazioneResponse, "ws.ricevutaRichiestaKo", log);
				for(Versamento versamentoModel: versamenti) {
					if(versamentoModel.getId() != null) {
						IdVersamento idV = new IdVersamento();
						idV.setCodVersamentoEnte(versamentoModel.getCodVersamentoEnte());
						idV.setId(versamentoModel.getId());
						idVersamento.add(idV);
					}
				}
				Nota nota = PagamentoPortaleUtils.addNota(pagamentoPortale, Nota.UTENTE_SISTEMA, e, false);
				
				pagamentoPortale.setIdVersamento(idVersamento); 
				pagamentoPortale.setCodiceStato(CODICE_STATO.PAGAMENTO_FALLITO);
				pagamentoPortale.setStato(STATO.FALLITO);
				pagamentoPortale.setDescrizioneStato(e.getMessage());
				pagamentiPortaleBD.updatePagamento(pagamentoPortale, true);
				
				List<EventoNota> listaEventoNota = new ArrayList<>();
				
				if(!transazioneResponse.getRifTransazione().isEmpty()) {
					for(RifTransazione rifTransazione: transazioneResponse.getRifTransazione()) {
						EventoNota eventoNota = new EventoNota();
						eventoNota.setAutore(nota.getAutore());
						eventoNota.setOggetto(nota.getOggetto());
						eventoNota.setTesto(nota.getTesto());
						eventoNota.setPrincipal(nota.getPrincipal());
						eventoNota.setData(nota.getData());
						eventoNota.setTipoEvento(nota.getTipo() != null  ? nota.getTipo().name() : it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo.name()); 
						eventoNota.setCodDominio(rifTransazione.getCodDominio());
						eventoNota.setIuv(rifTransazione.getIuv());
						eventoNota.setCcp(rifTransazione.getCcp());
						eventoNota.setIdPagamentoPortale(pagamentoPortale.getId());
						
						for(Versamento versamentoModel: versamenti) {
							if(rifTransazione.getCodApplicazione().equals(versamentoModel.getApplicazione(bd).getCodApplicazione()) 
									&& rifTransazione.getCodVersamentoEnte().equals(versamentoModel.getCodVersamentoEnte()))  {
								eventoNota.setIdVersamento(versamentoModel.getId());
								break;
							}
						}
						eventoNota.setTipoEvento(it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo);
						listaEventoNota.add(eventoNota);
					}
				} else {
					for(Versamento versamentoModel: versamenti) {
						EventoNota eventoNota = new EventoNota();
						eventoNota.setAutore(nota.getAutore());
						eventoNota.setOggetto(nota.getOggetto());
						eventoNota.setTesto(nota.getTesto());
						eventoNota.setPrincipal(nota.getPrincipal());
						eventoNota.setData(nota.getData());
						eventoNota.setTipoEvento(nota.getTipo() != null  ? nota.getTipo().name() : it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo.name()); 
						eventoNota.setCodDominio(versamentoModel.getUo(bd).getDominio(bd).getCodDominio());
						eventoNota.setIuv(versamentoModel.getIuvVersamento());
						eventoNota.setIdPagamentoPortale(pagamentoPortale.getId());
						eventoNota.setIdVersamento(versamentoModel.getId());					
						eventoNota.setTipoEvento(it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo);
						listaEventoNota.add(eventoNota);
					}
				}
				
				for (EventoNota eventoNota : listaEventoNota) {
					giornaleEventi.registraEventoNota(eventoNota);	
				}
				
				
				e.setParam(pagamentoPortale);
				throw e;
			} catch (Exception e) {
				transazioneResponse = (GpAvviaTransazionePagamentoResponse) new GovPayException(e).getWsResponse(transazioneResponse, "ws.ricevutaRichiestaKo", log);
				for(Versamento versamentoModel: versamenti) {
					if(versamentoModel.getId() != null) {
						IdVersamento idV = new IdVersamento();
						idV.setCodVersamentoEnte(versamentoModel.getCodVersamentoEnte());
						idV.setId(versamentoModel.getId());
						idVersamento.add(idV);
					}
				}
				Nota nota = PagamentoPortaleUtils.addNota(pagamentoPortale, Nota.UTENTE_SISTEMA, transazioneResponse.getDescrizioneEsito(), TipoNota.SISTEMA_FATAL, transazioneResponse.getDettaglioEsito(), false); 
				pagamentoPortale.setIdVersamento(idVersamento); 
				pagamentoPortale.setCodiceStato(CODICE_STATO.PAGAMENTO_FALLITO);
				pagamentoPortale.setStato(STATO.FALLITO);
				pagamentoPortale.setDescrizioneStato(e.getMessage());
				pagamentiPortaleBD.updatePagamento(pagamentoPortale, true);
				
				List<EventoNota> listaEventoNota = new ArrayList<>();
				
				if(!transazioneResponse.getRifTransazione().isEmpty()) {
					for(RifTransazione rifTransazione: transazioneResponse.getRifTransazione()) {
						EventoNota eventoNota = new EventoNota();
						eventoNota.setAutore(nota.getAutore());
						eventoNota.setOggetto(nota.getOggetto());
						eventoNota.setTesto(nota.getTesto());
						eventoNota.setPrincipal(nota.getPrincipal());
						eventoNota.setData(nota.getData());
						eventoNota.setTipoEvento(nota.getTipo() != null  ? nota.getTipo().name() : it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo.name()); 
						eventoNota.setCodDominio(rifTransazione.getCodDominio());
						eventoNota.setIuv(rifTransazione.getIuv());
						eventoNota.setCcp(rifTransazione.getCcp());
						eventoNota.setIdPagamentoPortale(pagamentoPortale.getId());
						
						for(Versamento versamentoModel: versamenti) {
							if(rifTransazione.getCodApplicazione().equals(versamentoModel.getApplicazione(bd).getCodApplicazione()) 
									&& rifTransazione.getCodVersamentoEnte().equals(versamentoModel.getCodVersamentoEnte()))  {
								eventoNota.setIdVersamento(versamentoModel.getId());
								break;
							}
						}
						eventoNota.setTipoEvento(it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo);
						listaEventoNota.add(eventoNota);
					}
				} else {
					for(Versamento versamentoModel: versamenti) {
						EventoNota eventoNota = new EventoNota();
						eventoNota.setAutore(nota.getAutore());
						eventoNota.setOggetto(nota.getOggetto());
						eventoNota.setTesto(nota.getTesto());
						eventoNota.setPrincipal(nota.getPrincipal());
						eventoNota.setData(nota.getData());
						eventoNota.setTipoEvento(nota.getTipo() != null  ? nota.getTipo().name() : it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo.name()); 
						eventoNota.setCodDominio(versamentoModel.getUo(bd).getDominio(bd).getCodDominio());
						eventoNota.setIuv(versamentoModel.getIuvVersamento());
						eventoNota.setIdPagamentoPortale(pagamentoPortale.getId());
						eventoNota.setIdVersamento(versamentoModel.getId());					
						eventoNota.setTipoEvento(it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo);
						listaEventoNota.add(eventoNota);
					}
				}
				
				for (EventoNota eventoNota : listaEventoNota) {
					giornaleEventi.registraEventoNota(eventoNota);	
				}
				
				throw e;
			}
			
			for(Versamento versamentoModel: versamenti) {
				if(versamentoModel.getId() != null) {
					IdVersamento idV = new IdVersamento();
					idV.setCodVersamentoEnte(versamentoModel.getCodVersamentoEnte());
					idV.setId(versamentoModel.getId());
					idVersamento.add(idV);
				}
			}
			pagamentoPortale.setIdVersamento(idVersamento); 
			pagamentoPortale.setIdSessionePsp(idSessionePsp);
			pagamentoPortale.setPspRedirectUrl(pspRedirect);
			pagamentoPortale.setCodiceStato(codiceStato);
			pagamentoPortale.setStato(stato);
			pagamentoPortale.setDescrizioneStato(descrizioneStato);
			pagamentoPortale.setCodPsp(codPsp);
			pagamentoPortale.setTipoVersamento(tipoVersamento);
			pagamentoPortale.setCodCanale(codCanale); 
			pagamentiPortaleBD.updatePagamento(pagamentoPortale, true); //inserisce anche i versamenti
			response.setId(pagamentoPortale.getIdSessione());
			return response;
		}finally {
			if(ctx != null) {
				ctx.setResult(transazioneResponse);
				ctx.log();
			}
			if(bd != null)
				bd.closeConnection();
		}
	}

	public LeggiPagamentoPortaleDTOResponse leggiPagamentoPortale(LeggiPagamentoPortaleDTO leggiPagamentoPortaleDTO) throws ServiceException,PagamentoPortaleNonTrovatoException, NotAuthorizedException, NotAuthenticatedException{
		LeggiPagamentoPortaleDTOResponse leggiPagamentoPortaleDTOResponse = new LeggiPagamentoPortaleDTOResponse();
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			this.autorizzaRichiesta(leggiPagamentoPortaleDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA,bd);

			PagamentiPortaleBD pagamentiPortaleBD = new PagamentiPortaleBD(bd);
			PagamentoPortale pagamentoPortale = null;
			if(leggiPagamentoPortaleDTO.getId() != null) 
				pagamentoPortale = pagamentiPortaleBD.getPagamentoFromCodSessione(leggiPagamentoPortaleDTO.getId());
			else
				pagamentoPortale = pagamentiPortaleBD.getPagamentoFromCodSessionePsp(leggiPagamentoPortaleDTO.getIdSessionePsp());

			if(pagamentoPortale.getVersamenti(bd) != null && pagamentoPortale.getVersamenti(bd).size() > 0) {
				for(Versamento versamento: pagamentoPortale.getVersamenti(bd)) {
					versamento.getDominio(bd);
					versamento.getSingoliVersamenti(bd);
				}
			}
			if(pagamentoPortale.getMultiBeneficiario() != null) {
				// controllo che il dominio sia autorizzato
				this.autorizzaRichiesta(leggiPagamentoPortaleDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, pagamentoPortale.getMultiBeneficiario(), null, bd);
			}
			leggiPagamentoPortaleDTOResponse.setPagamento(pagamentoPortale); 

			if(leggiPagamentoPortaleDTO.isRisolviLink()) {
				PendenzeDAO pendenzeDao = new PendenzeDAO();
				ListaPendenzeDTO listaPendenzaDTO = new ListaPendenzeDTO(leggiPagamentoPortaleDTO.getUser());
				listaPendenzaDTO.setIdPagamento(pagamentoPortale.getIdSessione());
				ListaPendenzeDTOResponse listaPendenze = pendenzeDao.listaPendenze(listaPendenzaDTO, bd);
				leggiPagamentoPortaleDTOResponse.setListaPendenze(listaPendenze.getResults());

				RptDAO rptDao = new RptDAO(); 
				ListaRptDTO listaRptDTO = new ListaRptDTO(leggiPagamentoPortaleDTO.getUser());
				listaRptDTO.setIdPagamento(pagamentoPortale.getIdSessione());
				ListaRptDTOResponse listaRpt = rptDao.listaRpt(listaRptDTO, bd);
				leggiPagamentoPortaleDTOResponse.setListaRpp(listaRpt.getResults());
			}

			return leggiPagamentoPortaleDTOResponse;
		}catch(NotFoundException e) {
			throw new PagamentoPortaleNonTrovatoException("Non esiste un pagamento associato all'ID ["+leggiPagamentoPortaleDTO.getId()+"]");
		}finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

	public ListaPagamentiPortaleDTOResponse listaPagamentiPortale(ListaPagamentiPortaleDTO listaPagamentiPortaleDTO) throws ServiceException, NotAuthorizedException, NotAuthenticatedException{
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			this.autorizzaRichiesta(listaPagamentiPortaleDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA,bd);
			// Autorizzazione sui domini
			List<String> codDomini = AclEngine.getDominiAutorizzati((Utenza) listaPagamentiPortaleDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA);
			if(codDomini == null) {
				throw new NotAuthorizedException("L'utenza autenticata ["+listaPagamentiPortaleDTO.getUser().getPrincipal()+"] non e' autorizzata ai servizi " + Servizio.PAGAMENTI_E_PENDENZE + " per alcun dominio");
			}

			PagamentiPortaleBD pagamentiPortaleBD = new PagamentiPortaleBD(bd);
			PagamentoPortaleFilter filter = pagamentiPortaleBD.newFilter();

			filter.setOffset(listaPagamentiPortaleDTO.getOffset());
			filter.setLimit(listaPagamentiPortaleDTO.getLimit());
			filter.setDataInizio(listaPagamentiPortaleDTO.getDataDa());
			filter.setDataFine(listaPagamentiPortaleDTO.getDataA());
			filter.setAck(listaPagamentiPortaleDTO.getVerificato());
			filter.setIdSessionePortale(listaPagamentiPortaleDTO.getIdSessionePortale()); 
			filter.setIdSessionePsp(listaPagamentiPortaleDTO.getIdSessionePsp());
			if(listaPagamentiPortaleDTO.getStato()!=null) {
				try {
					filter.setStato(STATO.valueOf(listaPagamentiPortaleDTO.getStato()));
				} catch(Exception e) {
					return new ListaPagamentiPortaleDTOResponse(0, new ArrayList<PagamentoPortale>());
				}
			}
			filter.setVersante(listaPagamentiPortaleDTO.getVersante());
			filter.setFilterSortList(listaPagamentiPortaleDTO.getFieldSortList());
			if(listaPagamentiPortaleDTO.getUser().getTipoUtenza().equals(TIPO_UTENZA.CITTADINO)) {
				filter.setCfCittadino(listaPagamentiPortaleDTO.getUser().getIdentificativo()); 
			}

			if(codDomini != null && codDomini.size() > 0)
				filter.setCodDomini(codDomini);

			long count = pagamentiPortaleBD.count(filter);

			if(count > 0) {
				return new ListaPagamentiPortaleDTOResponse(count, pagamentiPortaleBD.findAll(filter));
			} else {
				return new ListaPagamentiPortaleDTOResponse(count, new ArrayList<PagamentoPortale>());
			}
		}finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

//	public LeggiPagamentoPortaleDTOResponse inserisciNota(VerificaPagamentoPortaleDTO verificaPagamentoDTO) throws ServiceException,PagamentoPortaleNonTrovatoException, NotAuthorizedException, NotAuthenticatedException{
//		LeggiPagamentoPortaleDTOResponse leggiPagamentoPortaleDTOResponse = new LeggiPagamentoPortaleDTOResponse();
//		
//		BasicBD bd = null;
//
//		try {
//			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
//			this.autorizzaRichiesta(verificaPagamentoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA,bd);
//
//			PagamentiPortaleBD pagamentiPortaleBD = new PagamentiPortaleBD(bd);
//			PagamentoPortale pagamentoPortale = pagamentiPortaleBD.getPagamentoFromCodSessione(verificaPagamentoDTO.getIdSessione());
//
//			for(Versamento versamento: pagamentoPortale.getVersamenti(bd)) {
//				versamento.getDominio(bd);
//				versamento.getSingoliVersamenti(bd);
//			}
//			if(pagamentoPortale.getMultiBeneficiario() != null) {
//				// controllo che il dominio sia autorizzato
//				this.autorizzaRichiesta(verificaPagamentoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, pagamentoPortale.getMultiBeneficiario(), null, bd);
//			}
//			leggiPagamentoPortaleDTOResponse.setPagamento(pagamentoPortale); 
//
//			PendenzeDAO pendenzeDao = new PendenzeDAO();
//			ListaPendenzeDTO listaPendenzaDTO = new ListaPendenzeDTO(verificaPagamentoDTO.getUser());
//			listaPendenzaDTO.setIdPagamento(verificaPagamentoDTO.getIdSessione());
//			ListaPendenzeDTOResponse listaPendenze = pendenzeDao.listaPendenze(listaPendenzaDTO, bd);
//			leggiPagamentoPortaleDTOResponse.setListaPendenze(listaPendenze.getResults());
//
//			RptDAO rptDao = new RptDAO(); 
//			ListaRptDTO listaRptDTO = new ListaRptDTO(verificaPagamentoDTO.getUser());
//			listaRptDTO.setIdPagamento(pagamentoPortale.getIdSessione());
//			ListaRptDTOResponse listaRpt = rptDao.listaRpt(listaRptDTO, bd);
//			leggiPagamentoPortaleDTOResponse.setListaRpp(listaRpt.getResults());
//
//			
//			
//			
//			return leggiPagamentoPortaleDTOResponse;
//		}catch(NotFoundException e) {
//			throw new PagamentoPortaleNonTrovatoException("Non esiste un pagamento associato all'ID ["+verificaPagamentoDTO.getIdSessione()+"]");
//		}finally {
//			if(bd != null)
//				bd.closeConnection();
//		}
//	}

	public LeggiPagamentoPortaleDTOResponse patch(PagamentoPatchDTO patchDTO) 
			throws ServiceException,PagamentoPortaleNonTrovatoException, NotAuthorizedException, NotAuthenticatedException,ValidationException{
		LeggiPagamentoPortaleDTOResponse leggiPagamentoPortaleDTOResponse = new LeggiPagamentoPortaleDTOResponse();
		
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			this.autorizzaRichiesta(patchDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA,bd);

			PagamentiPortaleBD pagamentiPortaleBD = new PagamentiPortaleBD(bd);
			PagamentoPortale pagamentoPortale = pagamentiPortaleBD.getPagamentoFromCodSessione(patchDTO.getIdSessione());

			for(Versamento versamento: pagamentoPortale.getVersamenti(bd)) {
				versamento.getDominio(bd);
				versamento.getSingoliVersamenti(bd);
			}
			if(pagamentoPortale.getMultiBeneficiario() != null) {
				// controllo che il dominio sia autorizzato
				this.autorizzaRichiesta(patchDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, pagamentoPortale.getMultiBeneficiario(), null, bd);
			}
			leggiPagamentoPortaleDTOResponse.setPagamento(pagamentoPortale); 

			PendenzeDAO pendenzeDao = new PendenzeDAO();
			ListaPendenzeDTO listaPendenzaDTO = new ListaPendenzeDTO(patchDTO.getUser());
			listaPendenzaDTO.setIdPagamento(patchDTO.getIdSessione());
			ListaPendenzeDTOResponse listaPendenze = pendenzeDao.listaPendenze(listaPendenzaDTO, bd);
			leggiPagamentoPortaleDTOResponse.setListaPendenze(listaPendenze.getResults());

			RptDAO rptDao = new RptDAO(); 
			ListaRptDTO listaRptDTO = new ListaRptDTO(patchDTO.getUser());
			listaRptDTO.setIdPagamento(pagamentoPortale.getIdSessione());
			ListaRptDTOResponse listaRpt = rptDao.listaRpt(listaRptDTO, bd);
			leggiPagamentoPortaleDTOResponse.setListaRpp(listaRpt.getResults());

			GiornaleEventi giornaleEventi = new GiornaleEventi(bd);
			List<EventoNota> listaNote = new ArrayList<>();
			for(PatchOp op: patchDTO.getOp()) {
				
				if(PATH_NOTA.equals(op.getPath())) {
					switch(op.getOp()) {
					case ADD: 
						Nota nota = UtenzaPatchUtils.getNotaFromPatch(patchDTO.getUser(), this.getOperatoreFromUser(patchDTO.getUser(), bd), op, bd); 
						// TODO eliminare
						pagamentoPortale.getNote().add(0,nota);
						
						EventoNota eventoNota = new EventoNota();
						eventoNota.setAutore(nota.getAutore());
						eventoNota.setOggetto(nota.getOggetto());
						eventoNota.setTesto(nota.getTesto());
						eventoNota.setPrincipal(nota.getPrincipal());
						eventoNota.setData(nota.getData());
						eventoNota.setTipoEvento(nota.getTipo() != null  ? nota.getTipo().name() : it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo.name()); 
						eventoNota.setIdPagamentoPortale(pagamentoPortale.getId());
						eventoNota.setTipoEvento(it.govpay.bd.model.eventi.EventoNota.TipoNota.SistemaInfo);
						listaNote.add(eventoNota);
						
						break;
					default: throw new ServiceException("Op '"+op.getOp()+"' non valida per il path '"+op.getPath()+"'");
					}
				} else if("/verificato".equals(op.getPath())) {
					switch(op.getOp()) {
					case REPLACE: 
						pagamentoPortale.setAck((Boolean)op.getValue()); 
						break;
					default: throw new ServiceException("Op '"+op.getOp()+"' non valida per il path '"+op.getPath()+"'");
					}
					
				} else {
					throw new ServiceException("Path '"+op.getPath()+"' non valido");
				}
			}
			
			pagamentiPortaleBD.updatePagamento(pagamentoPortale, false);
			
			for (EventoNota eventoNota : listaNote) {
				giornaleEventi.registraEventoNota(eventoNota);
			}
			
			
			return leggiPagamentoPortaleDTOResponse;
		}catch(NotFoundException e) {
			throw new PagamentoPortaleNonTrovatoException("Non esiste un pagamento associato all'ID ["+patchDTO.getIdSessione()+"]");
		}finally {
			if(bd != null)
				bd.closeConnection();
		}
	}
}