package it.govpay.backoffice.v1.beans.converter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.jaxrs.RawObject;
import org.openspcoop2.utils.json.ValidationException;
import org.springframework.security.core.Authentication;

import it.govpay.backoffice.v1.beans.ConfigurazioneGenerazioneMessageAppIO;
import it.govpay.backoffice.v1.beans.ContiAccredito;
import it.govpay.backoffice.v1.beans.ContiAccreditoPost;
import it.govpay.backoffice.v1.beans.Dominio;
import it.govpay.backoffice.v1.beans.DominioIndex;
import it.govpay.backoffice.v1.beans.DominioPost;
import it.govpay.backoffice.v1.beans.DominioProfiloIndex;
import it.govpay.backoffice.v1.beans.DominioProfiloPost;
import it.govpay.backoffice.v1.beans.Entrata;
import it.govpay.backoffice.v1.beans.EntrataPost;
import it.govpay.backoffice.v1.beans.TipoContabilita;
import it.govpay.backoffice.v1.beans.TipoPendenzaAppIO;
import it.govpay.backoffice.v1.beans.TipoPendenzaDominio;
import it.govpay.backoffice.v1.beans.TipoPendenzaDominioPost;
import it.govpay.backoffice.v1.beans.TipoPendenzaForm;
import it.govpay.backoffice.v1.beans.TipoPendenzaPromemoria;
import it.govpay.backoffice.v1.beans.TipoPendenzaTrasformazione;
import it.govpay.backoffice.v1.beans.TracciatoCsv;
import it.govpay.backoffice.v1.beans.UnitaOperativa;
import it.govpay.backoffice.v1.beans.UnitaOperativaIndex;
import it.govpay.backoffice.v1.beans.UnitaOperativaPost;
import it.govpay.backoffice.v1.beans.TipoPendenzaTrasformazione.TipoEnum;
import it.govpay.backoffice.v1.controllers.ApplicazioniController;
import it.govpay.bd.model.TipoVersamentoDominio;
import it.govpay.bd.model.Tributo;
import it.govpay.core.dao.anagrafica.dto.GetTipoPendenzaDominioDTOResponse;
import it.govpay.core.dao.anagrafica.dto.GetTributoDTOResponse;
import it.govpay.core.dao.anagrafica.dto.PutDominioDTO;
import it.govpay.core.dao.anagrafica.dto.PutEntrataDominioDTO;
import it.govpay.core.dao.anagrafica.dto.PutIbanAccreditoDTO;
import it.govpay.core.dao.anagrafica.dto.PutTipoPendenzaDominioDTO;
import it.govpay.core.dao.anagrafica.dto.PutUnitaOperativaDTO;
import it.govpay.core.dao.commons.Dominio.Uo;
import it.govpay.core.utils.UriBuilderUtils;
import it.govpay.core.utils.rawutils.ConverterUtils;
import it.govpay.model.Anagrafica;

public class DominiConverter {

	public static PutEntrataDominioDTO getPutEntrataDominioDTO(EntrataPost entrataRequest, String idDominio, String idEntrata, Authentication user) throws ValidationException {
		PutEntrataDominioDTO entrataDTO = new PutEntrataDominioDTO(user);

		it.govpay.bd.model.Tributo tributo = new it.govpay.bd.model.Tributo();

		tributo.setAbilitato(entrataRequest.isAbilitato());
		tributo.setCodContabilitaCustom(entrataRequest.getCodiceContabilita());
		tributo.setCodTributo(idEntrata);
		if(entrataRequest.getTipoContabilita() != null) {

			// valore tipo contabilita non valido
			if(TipoContabilita.fromValue(entrataRequest.getTipoContabilita()) == null) {
				throw new ValidationException("Codifica inesistente per tipoContabilita. Valore fornito [" + entrataRequest.getTipoContabilita() + "] valori possibili " + ArrayUtils.toString(TipoContabilita.values()));
			}

			entrataRequest.setTipoContabilitaEnum(TipoContabilita.fromValue(entrataRequest.getTipoContabilita()));


			switch (entrataRequest.getTipoContabilitaEnum()) {
			case ALTRO:
				tributo.setTipoContabilitaCustom(it.govpay.bd.model.Tributo.TipoContabilita.ALTRO);
				break;
			case CAPITOLO:
				tributo.setTipoContabilitaCustom(it.govpay.bd.model.Tributo.TipoContabilita.CAPITOLO);
				break;
			case SIOPE:
				tributo.setTipoContabilitaCustom(it.govpay.bd.model.Tributo.TipoContabilita.SIOPE);
				break;
			case SPECIALE:
				tributo.setTipoContabilitaCustom(it.govpay.bd.model.Tributo.TipoContabilita.SPECIALE);
				break;
			}
		}

		entrataDTO.setIbanAccredito(entrataRequest.getIbanAccredito());
		entrataDTO.setIbanAppoggio(entrataRequest.getIbanAppoggio());
		entrataDTO.setTributo(tributo);
		entrataDTO.setIdDominio(idDominio);
		entrataDTO.setIdTributo(idEntrata);

		return entrataDTO;		
	}
	public static PutIbanAccreditoDTO getPutIbanAccreditoDTO(ContiAccreditoPost ibanAccreditoPost, String idDominio, String idIbanAccredito, Authentication user) {
		PutIbanAccreditoDTO ibanAccreditoDTO = new PutIbanAccreditoDTO(user);

		it.govpay.bd.model.IbanAccredito iban = new it.govpay.bd.model.IbanAccredito();

		iban.setAbilitato(ibanAccreditoPost.isAbilitato());
		iban.setAttivatoObep(ibanAccreditoPost.isMybank());
		iban.setCodBic(ibanAccreditoPost.getBic());
		iban.setCodIban(idIbanAccredito);
		iban.setPostale(ibanAccreditoPost.isPostale());

		ibanAccreditoDTO.setIban(iban);
		ibanAccreditoDTO.setIdDominio(idDominio);
		ibanAccreditoDTO.setIbanAccredito(idIbanAccredito);

		return ibanAccreditoDTO;		
	}

	public static PutUnitaOperativaDTO getPutUnitaOperativaDTO(UnitaOperativaPost uoPost, String idDominio, String idUo, Authentication user) {
		PutUnitaOperativaDTO uoDTO = new PutUnitaOperativaDTO(user);

		it.govpay.bd.model.UnitaOperativa uo = new it.govpay.bd.model.UnitaOperativa();
		uo.setAbilitato(uoPost.isAbilitato());
		Anagrafica anagrafica = new Anagrafica();
		anagrafica.setCap(uoPost.getCap());
		anagrafica.setCivico(uoPost.getCivico());
		anagrafica.setCodUnivoco(idUo);
		anagrafica.setEmail(uoPost.getEmail());
		anagrafica.setPec(uoPost.getPec());
		anagrafica.setFax(uoPost.getFax());
		anagrafica.setIndirizzo(uoPost.getIndirizzo());
		anagrafica.setLocalita(uoPost.getLocalita());
		anagrafica.setNazione(uoPost.getNazione());
		anagrafica.setProvincia(uoPost.getProvincia());
		anagrafica.setRagioneSociale(uoPost.getRagioneSociale());
		anagrafica.setTelefono(uoPost.getTel());
		anagrafica.setUrlSitoWeb(uoPost.getWeb());
		anagrafica.setArea(uoPost.getArea());

		uo.setAnagrafica(anagrafica);
		uo.setCodUo(idUo);

		uoDTO.setUo(uo );
		uoDTO.setIdDominio(idDominio);
		uoDTO.setIdUo(idUo);

		return uoDTO;		
	}

	public static PutDominioDTO getPutDominioDTO(DominioPost dominioPost, String idDominio, Authentication user) {
		PutDominioDTO dominioDTO = new PutDominioDTO(user);

		it.govpay.bd.model.Dominio dominio = new it.govpay.bd.model.Dominio();
		dominio.setAbilitato(dominioPost.isAbilitato());
		Anagrafica anagrafica = new Anagrafica();
		anagrafica.setCap(dominioPost.getCap());
		anagrafica.setCivico(dominioPost.getCivico());
		anagrafica.setCodUnivoco(idDominio);
		anagrafica.setEmail(dominioPost.getEmail());
		anagrafica.setFax(dominioPost.getFax());
		anagrafica.setIndirizzo(dominioPost.getIndirizzo());
		anagrafica.setLocalita(dominioPost.getLocalita());
		anagrafica.setNazione(dominioPost.getNazione());
		anagrafica.setProvincia(dominioPost.getProvincia());
		anagrafica.setRagioneSociale(dominioPost.getRagioneSociale());
		anagrafica.setTelefono(dominioPost.getTel());
		anagrafica.setUrlSitoWeb(dominioPost.getWeb());
		anagrafica.setPec(dominioPost.getPec());
		anagrafica.setArea(dominioPost.getArea()); 

		dominio.setAnagrafica(anagrafica );
		if(dominioPost.getAuxDigit() != null)
			dominio.setAuxDigit(Integer.parseInt(dominioPost.getAuxDigit()));
		dominio.setCbill(dominioPost.getCbill());
		dominio.setCodDominio(idDominio);
		dominio.setGln(dominioPost.getGln());
		dominio.setIdApplicazioneDefault(null);

		dominio.setIuvPrefix(dominioPost.getIuvPrefix());
		if(dominioPost.getLogo() != null) {
			dominio.setLogo(dominioPost.getLogo().getBytes(StandardCharsets.UTF_8));
		}
		dominio.setNdpData(null);
		dominio.setNdpDescrizione(null);
		dominio.setNdpOperazione(null);
		dominio.setNdpStato(null);
		dominio.setRagioneSociale(dominioPost.getRagioneSociale());
		if(dominioPost.getSegregationCode() != null)
			dominio.setSegregationCode(Integer.parseInt(dominioPost.getSegregationCode()));

		dominio.setAutStampaPoste(dominioPost.getAutStampaPosteItaliane());

		dominioDTO.setDominio(dominio);
		dominioDTO.setIdDominio(idDominio);
		dominioDTO.setCodStazione(dominioPost.getStazione());

		return dominioDTO;		
	}



	public static DominioIndex toRsModelIndex(it.govpay.bd.model.Dominio dominio) throws ServiceException {
		DominioIndex rsModel = new DominioIndex();
		rsModel.setWeb(dominio.getAnagrafica().getUrlSitoWeb());
		rsModel.setIdDominio(dominio.getCodDominio()); 
		rsModel.setRagioneSociale(dominio.getRagioneSociale());
		rsModel.setIndirizzo(dominio.getAnagrafica().getIndirizzo());
		rsModel.setCivico(dominio.getAnagrafica().getCivico());
		rsModel.setCap(dominio.getAnagrafica().getCap());
		rsModel.setLocalita(dominio.getAnagrafica().getLocalita());
		rsModel.setProvincia(dominio.getAnagrafica().getProvincia());
		rsModel.setNazione(dominio.getAnagrafica().getNazione());
		rsModel.setEmail(dominio.getAnagrafica().getEmail());
		rsModel.setPec(dominio.getAnagrafica().getPec()); 
		rsModel.setTel(dominio.getAnagrafica().getTelefono());
		rsModel.setFax(dominio.getAnagrafica().getFax());
		rsModel.setArea(dominio.getAnagrafica().getArea());
		rsModel.setGln(dominio.getGln());
		rsModel.setCbill(dominio.getCbill());
		rsModel.setAuxDigit("" + dominio.getAuxDigit());
		if(dominio.getSegregationCode() != null)
			rsModel.setSegregationCode(String.format("%02d", dominio.getSegregationCode()));

		if(dominio.getLogo() != null) {
			rsModel.setLogo(UriBuilderUtils.getLogoDominio(dominio.getCodDominio()));
		}
		rsModel.setIuvPrefix(dominio.getIuvPrefix());
		rsModel.setStazione(dominio.getStazione().getCodStazione());
		rsModel.setContiAccredito(UriBuilderUtils.getContiAccreditoByDominio(dominio.getCodDominio()));
		rsModel.setUnitaOperative(UriBuilderUtils.getListUoByDominio(dominio.getCodDominio()));
		rsModel.setEntrate(UriBuilderUtils.getEntrateByDominio(dominio.getCodDominio()));
		rsModel.setTipiPendenza(UriBuilderUtils.getTipiPendenzaByDominio(dominio.getCodDominio()));
		rsModel.setAbilitato(dominio.isAbilitato());
		rsModel.setAutStampaPosteItaliane(dominio.getAutStampaPoste());

		return rsModel;
	}

	public static DominioProfiloIndex toRsModelProfiloIndex(it.govpay.core.dao.commons.Dominio dominio) throws ServiceException {
		return toRsModelProfiloIndex(dominio, dominio.getUo());

	}
	public static DominioProfiloIndex toRsModelProfiloIndex(it.govpay.core.dao.commons.Dominio dominio, List<it.govpay.core.dao.commons.Dominio.Uo> uoLst) throws ServiceException {
		DominioProfiloIndex rsModel = new DominioProfiloIndex();
		//		rsModel.setWeb(dominio.getAnagrafica().getUrlSitoWeb());
		rsModel.setIdDominio(dominio.getCodDominio()); 
		rsModel.setRagioneSociale(dominio.getRagioneSociale());
		//		rsModel.setIndirizzo(dominio.getAnagrafica().getIndirizzo());
		//		rsModel.setCivico(dominio.getAnagrafica().getCivico());
		//		rsModel.setCap(dominio.getAnagrafica().getCap());
		//		rsModel.setLocalita(dominio.getAnagrafica().getLocalita());
		//		rsModel.setProvincia(dominio.getAnagrafica().getProvincia());
		//		rsModel.setNazione(dominio.getAnagrafica().getNazione());
		//		rsModel.setEmail(dominio.getAnagrafica().getEmail());
		//		rsModel.setPec(dominio.getAnagrafica().getPec()); 
		//		rsModel.setTel(dominio.getAnagrafica().getTelefono());
		//		rsModel.setFax(dominio.getAnagrafica().getFax());
		//		rsModel.setArea(dominio.getAnagrafica().getArea());
		//		rsModel.setGln(dominio.getGln());
		//		rsModel.setCbill(dominio.getCbill());
		//		rsModel.setAuxDigit("" + dominio.getAuxDigit());
		//		if(dominio.getSegregationCode() != null)
		//			rsModel.setSegregationCode(String.format("%02d", dominio.getSegregationCode()));
		//		
		//		if(dominio.getLogo() != null) {
		//			rsModel.setLogo(UriBuilderUtils.getLogoDominio(dominio.getCodDominio()));
		//		}
		//		rsModel.setIuvPrefix(dominio.getIuvPrefix());
		//		rsModel.setStazione(dominio.getStazione().getCodStazione());
		if(uoLst != null && !uoLst.isEmpty()) {
			List<UnitaOperativaIndex> unitaOperative = new ArrayList<>();

			for(it.govpay.core.dao.commons.Dominio.Uo uo: uoLst) {
				if(uo.getCodUo() != null)
					unitaOperative.add(toUnitaOperativaRsModelIndex(uo));
			}
			rsModel.setUnitaOperative(unitaOperative);
		}
		//		rsModel.setAbilitato(dominio.isAbilitato());
		//		rsModel.setAutStampaPosteItaliane(dominio.getAutStampaPoste());

		return rsModel;
	}

	public static DominioProfiloIndex toRsModelProfiloIndex(it.govpay.bd.model.Dominio dominio) throws ServiceException {
		return toRsModelProfiloIndex(dominio, dominio.getUnitaOperative());

	}
	public static DominioProfiloIndex toRsModelProfiloIndex(it.govpay.bd.model.Dominio dominio, List<it.govpay.bd.model.UnitaOperativa> uoLst) throws ServiceException {
		DominioProfiloIndex rsModel = new DominioProfiloIndex();
		//		rsModel.setWeb(dominio.getAnagrafica().getUrlSitoWeb());
		rsModel.setIdDominio(dominio.getCodDominio()); 
		rsModel.setRagioneSociale(dominio.getRagioneSociale());
		//		rsModel.setIndirizzo(dominio.getAnagrafica().getIndirizzo());
		//		rsModel.setCivico(dominio.getAnagrafica().getCivico());
		//		rsModel.setCap(dominio.getAnagrafica().getCap());
		//		rsModel.setLocalita(dominio.getAnagrafica().getLocalita());
		//		rsModel.setProvincia(dominio.getAnagrafica().getProvincia());
		//		rsModel.setNazione(dominio.getAnagrafica().getNazione());
		//		rsModel.setEmail(dominio.getAnagrafica().getEmail());
		//		rsModel.setPec(dominio.getAnagrafica().getPec()); 
		//		rsModel.setTel(dominio.getAnagrafica().getTelefono());
		//		rsModel.setFax(dominio.getAnagrafica().getFax());
		//		rsModel.setArea(dominio.getAnagrafica().getArea());
		//		rsModel.setGln(dominio.getGln());
		//		rsModel.setCbill(dominio.getCbill());
		//		rsModel.setAuxDigit("" + dominio.getAuxDigit());
		//		if(dominio.getSegregationCode() != null)
		//			rsModel.setSegregationCode(String.format("%02d", dominio.getSegregationCode()));
		//		
		//		if(dominio.getLogo() != null) {
		//			rsModel.setLogo(UriBuilderUtils.getLogoDominio(dominio.getCodDominio()));
		//		}
		//		rsModel.setIuvPrefix(dominio.getIuvPrefix());
		//		rsModel.setStazione(dominio.getStazione().getCodStazione());
		if(uoLst != null) {
			List<UnitaOperativaIndex> unitaOperative = new ArrayList<>();

			for(it.govpay.bd.model.UnitaOperativa uo: uoLst) {
				unitaOperative.add(toUnitaOperativaRsModelIndex(uo));
			}
			rsModel.setUnitaOperative(unitaOperative);
		}
		//		rsModel.setAbilitato(dominio.isAbilitato());
		//		rsModel.setAutStampaPosteItaliane(dominio.getAutStampaPoste());

		return rsModel;
	}

	public static Dominio toRsModel(it.govpay.bd.model.Dominio dominio, List<it.govpay.bd.model.UnitaOperativa> uoLst, List<it.govpay.bd.model.Tributo> tributoLst, List<it.govpay.bd.model.IbanAccredito> ibanAccreditoLst,
			List<TipoVersamentoDominio> tipoVersamentoDominioLst) throws ServiceException {
		Dominio rsModel = new Dominio();
		rsModel.setWeb(dominio.getAnagrafica().getUrlSitoWeb()); 
		rsModel.setIdDominio(dominio.getCodDominio()); 
		rsModel.setRagioneSociale(dominio.getRagioneSociale());
		rsModel.setIndirizzo(dominio.getAnagrafica().getIndirizzo());
		rsModel.setCivico(dominio.getAnagrafica().getCivico());
		rsModel.setCap(dominio.getAnagrafica().getCap());
		rsModel.setLocalita(dominio.getAnagrafica().getLocalita());
		rsModel.setProvincia(dominio.getAnagrafica().getProvincia());
		rsModel.setNazione(dominio.getAnagrafica().getNazione());
		rsModel.setEmail(dominio.getAnagrafica().getEmail());
		rsModel.setPec(dominio.getAnagrafica().getPec()); 
		rsModel.setTel(dominio.getAnagrafica().getTelefono());
		rsModel.setFax(dominio.getAnagrafica().getFax());
		rsModel.setArea(dominio.getAnagrafica().getArea());
		rsModel.setGln(dominio.getGln());
		rsModel.setCbill(dominio.getCbill());
		rsModel.setAuxDigit("" + dominio.getAuxDigit());
		if(dominio.getSegregationCode() != null)
			rsModel.setSegregationCode(String.format("%02d", dominio.getSegregationCode()));

		if(dominio.getLogo() != null) {
			rsModel.setLogo(UriBuilderUtils.getLogoDominio(dominio.getCodDominio()));
		}
		rsModel.setIuvPrefix(dominio.getIuvPrefix());
		rsModel.setStazione(dominio.getStazione().getCodStazione());

		if(uoLst != null) {
			List<UnitaOperativa> unitaOperative = new ArrayList<>();

			for(it.govpay.bd.model.UnitaOperativa uo: uoLst) {
				unitaOperative.add(toUnitaOperativaRsModel(uo));
			}
			rsModel.setUnitaOperative(unitaOperative);
		}

		if(ibanAccreditoLst != null) {
			List<ContiAccredito> contiAccredito = new ArrayList<>();

			for(it.govpay.bd.model.IbanAccredito iban: ibanAccreditoLst) {
				contiAccredito.add(toIbanRsModel(iban));
			}
			rsModel.setContiAccredito(contiAccredito);
		}

		if(tributoLst != null) {
			List<Entrata> entrate = new ArrayList<>();

			for(Tributo tributo: tributoLst) {
				entrate.add(toEntrataRsModel(tributo, tributo.getIbanAccredito(), tributo.getIbanAppoggio()));
			}
			rsModel.setEntrate(entrate);
		}

		if(tipoVersamentoDominioLst != null) {
			List<TipoPendenzaDominio> tipiPendenzaDominio = new ArrayList<>();

			for(TipoVersamentoDominio tvd: tipoVersamentoDominioLst) {
				tipiPendenzaDominio.add(toTipoPendenzaRsModel(tvd));
			}
			rsModel.setTipiPendenza(tipiPendenzaDominio);
		}

		rsModel.setAbilitato(dominio.isAbilitato());
		rsModel.setAutStampaPosteItaliane(dominio.getAutStampaPoste());

		if(dominio.getLogo() != null) {
			rsModel.setLogo(new String(dominio.getLogo(), StandardCharsets.UTF_8));  
		}

		return rsModel;
	}

	public static ContiAccredito toIbanRsModel(it.govpay.bd.model.IbanAccredito iban) throws ServiceException {
		ContiAccredito rsModel = new ContiAccredito();
		rsModel.abilitato(iban.isAbilitato())
		.bic(iban.getCodBic())
		.iban(iban.getCodIban())
		.mybank(iban.isAttivatoObep())
		.postale(iban.isPostale());

		return rsModel;
	}

	public static UnitaOperativaIndex toUnitaOperativaRsModelIndex(it.govpay.core.dao.commons.Dominio.Uo uo) throws IllegalArgumentException, ServiceException {
		UnitaOperativaIndex rsModel = new UnitaOperativaIndex();

		rsModel.setIdUnita(uo.getCodUo());
		rsModel.setRagioneSociale(uo.getRagioneSociale());

		return rsModel;
	}

	public static UnitaOperativaIndex toUnitaOperativaRsModelIndex(it.govpay.bd.model.UnitaOperativa uo) throws IllegalArgumentException, ServiceException {
		UnitaOperativaIndex rsModel = new UnitaOperativaIndex();

		rsModel.setIdUnita(uo.getCodUo());
		rsModel.setRagioneSociale(uo.getAnagrafica().getRagioneSociale());

		return rsModel;
	}


	public static UnitaOperativa toUnitaOperativaRsModel(it.govpay.bd.model.UnitaOperativa uo) throws IllegalArgumentException, ServiceException {
		UnitaOperativa rsModel = new UnitaOperativa();

		rsModel.setCap(uo.getAnagrafica().getCap());
		rsModel.setCivico(uo.getAnagrafica().getCivico());
		rsModel.setIdUnita(uo.getCodUo());
		rsModel.setIndirizzo(uo.getAnagrafica().getIndirizzo());
		rsModel.setLocalita(uo.getAnagrafica().getLocalita());
		rsModel.setRagioneSociale(uo.getAnagrafica().getRagioneSociale());
		rsModel.setWeb(uo.getAnagrafica().getUrlSitoWeb());
		rsModel.setProvincia(uo.getAnagrafica().getProvincia());
		rsModel.setNazione(uo.getAnagrafica().getNazione());
		rsModel.setEmail(uo.getAnagrafica().getEmail());
		rsModel.setPec(uo.getAnagrafica().getPec());
		rsModel.setTel(uo.getAnagrafica().getTelefono());
		rsModel.setFax(uo.getAnagrafica().getFax());
		rsModel.setArea(uo.getAnagrafica().getArea());
		rsModel.setAbilitato(uo.isAbilitato());

		return rsModel;
	}

	public static Entrata toEntrataRsModel(GetTributoDTOResponse response) throws ServiceException {
		return toEntrataRsModel(response.getTributo(), response.getIbanAccredito(), response.getIbanAppoggio());
	}

	public static Entrata toEntrataRsModel(it.govpay.bd.model.Tributo tributo, it.govpay.model.IbanAccredito ibanAccredito,
			it.govpay.model.IbanAccredito ibanAppoggio) throws ServiceException {
		Entrata rsModel = new Entrata();
		rsModel.codiceContabilita(tributo.getCodContabilitaCustom())
		.abilitato(tributo.isAbilitato())
		.idEntrata(tributo.getCodTributo())
		.tipoEntrata(EntrateConverter.toTipoEntrataRsModel(tributo));

		if(tributo.getTipoContabilitaCustom() != null) {
			switch (tributo.getTipoContabilitaCustom()) {
			case ALTRO:
				rsModel.tipoContabilita(TipoContabilita.ALTRO);
				break;
			case CAPITOLO:
				rsModel.tipoContabilita(TipoContabilita.CAPITOLO);
				break;
			case SIOPE:
				rsModel.tipoContabilita(TipoContabilita.SIOPE);
				break;
			case SPECIALE:
				rsModel.tipoContabilita(TipoContabilita.SPECIALE);
				break;
			}
		}

		if(ibanAccredito != null)
			rsModel.setIbanAccredito(ibanAccredito.getCodIban());

		if(ibanAppoggio != null)
			rsModel.setIbanAppoggio(ibanAppoggio.getCodIban());

		return rsModel;
	}

	public static TipoPendenzaDominio toTipoPendenzaRsModel(GetTipoPendenzaDominioDTOResponse response) throws ServiceException {
		return toTipoPendenzaRsModel(response.getTipoVersamento());
	}

	public static TipoPendenzaDominio toTipoPendenzaRsModel(it.govpay.model.TipoVersamentoDominio tipoVersamentoDominio) throws ServiceException {
		TipoPendenzaDominio rsModel = new TipoPendenzaDominio();

		rsModel.descrizione(tipoVersamentoDominio.getDescrizione())
		.idTipoPendenza(tipoVersamentoDominio.getCodTipoVersamento())
		.codificaIUV(tipoVersamentoDominio.getCodificaIuvDefault())
		.abilitato(tipoVersamentoDominio.isAbilitatoDefault())
		.pagaTerzi(tipoVersamentoDominio.getPagaTerziDefault());

		if(tipoVersamentoDominio.getTipoDefault() != null) {
			switch (tipoVersamentoDominio.getTipoDefault()) {
			case DOVUTO:
				rsModel.setTipo(it.govpay.backoffice.v1.beans.TipoPendenzaTipologia.DOVUTO);
				break;
			case SPONTANEO:
				rsModel.setTipo(it.govpay.backoffice.v1.beans.TipoPendenzaTipologia.SPONTANEO);
				break;
			}
		}

		if(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormDefinizioneDefault() != null && tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormTipoDefault() != null) {
			TipoPendenzaForm form = new TipoPendenzaForm();
			form.setTipo(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormTipoDefault());
			form.setDefinizione(new RawObject(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormDefinizioneDefault())); 
			rsModel.setForm(form);
		}

		TipoPendenzaPromemoria avvisaturaMailPromemoriaAvviso = new TipoPendenzaPromemoria();
		avvisaturaMailPromemoriaAvviso.setAbilitato(tipoVersamentoDominio.isAvvisaturaMailPromemoriaAvvisoAbilitatoDefault());

		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoOggettoDefault() != null)
			avvisaturaMailPromemoriaAvviso.setOggetto(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoOggettoDefault()));
		
		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoMessaggioDefault() != null)
			avvisaturaMailPromemoriaAvviso.setMessaggio(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoMessaggioDefault()));

		avvisaturaMailPromemoriaAvviso.setAllegaPdf(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoPdfDefault());
		avvisaturaMailPromemoriaAvviso.setTipo(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoTipoDefault());

		rsModel.setPromemoriaAvviso(avvisaturaMailPromemoriaAvviso);

		TipoPendenzaPromemoria avvisaturaMailPromemoriaRicevuta = new TipoPendenzaPromemoria();
		avvisaturaMailPromemoriaRicevuta.setAbilitato(tipoVersamentoDominio.isAvvisaturaMailPromemoriaRicevutaAbilitatoDefault());

		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaOggettoDefault() != null)
			avvisaturaMailPromemoriaRicevuta.setOggetto(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaOggettoDefault()));

		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaMessaggioDefault() != null)
			avvisaturaMailPromemoriaRicevuta.setMessaggio(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaMessaggioDefault()));
		
		avvisaturaMailPromemoriaRicevuta.setAllegaPdf(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaPdfDefault());
		avvisaturaMailPromemoriaRicevuta.setTipo(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaTipoDefault());

		rsModel.setPromemoriaRicevuta(avvisaturaMailPromemoriaRicevuta);

		if(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneTipoDefault() != null && tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneDefinizioneDefault() != null) {
			TipoPendenzaTrasformazione trasformazione  = new TipoPendenzaTrasformazione();
			trasformazione.setTipo(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneTipoDefault());
			trasformazione.setDefinizione(new RawObject(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneDefinizioneDefault())); 
			rsModel.setTrasformazione(trasformazione);
		}
		if(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeValidazioneDefinizioneDefault() != null)
			rsModel.setValidazione(new RawObject(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeValidazioneDefinizioneDefault()));

		if(tipoVersamentoDominio.getTracciatoCsvTipoDefault() != null &&  
				tipoVersamentoDominio.getTracciatoCsvIntestazioneDefault() != null && 
				tipoVersamentoDominio.getTracciatoCsvRichiestaDefault() != null && 
				tipoVersamentoDominio.getTracciatoCsvRispostaDefault() != null) {
			TracciatoCsv tracciatoCsv = new TracciatoCsv();
			tracciatoCsv.setTipo(tipoVersamentoDominio.getTracciatoCsvTipoDefault());
			tracciatoCsv.setIntestazione(tipoVersamentoDominio.getTracciatoCsvIntestazioneDefault());
			tracciatoCsv.setRichiesta(new RawObject(tipoVersamentoDominio.getTracciatoCsvRichiestaDefault()));
			tracciatoCsv.setRisposta(new RawObject(tipoVersamentoDominio.getTracciatoCsvRispostaDefault()));
			rsModel.setTracciatoCsv(tracciatoCsv);
		}

		rsModel.setInoltro(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeCodApplicazioneDefault());

		if(tipoVersamentoDominio.getVisualizzazioneDefinizioneDefault() != null)
			rsModel.setVisualizzazione(new RawObject(tipoVersamentoDominio.getVisualizzazioneDefinizioneDefault()));
		
		if(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoTipoDefault() != null && tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoMessaggioDefault() != null && 
				tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoOggettoDefault() != null ) {
			
			ConfigurazioneGenerazioneMessageAppIO appIO = new ConfigurazioneGenerazioneMessageAppIO();
			appIO.setTipo(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoTipoDefault());
			appIO.setBody(new RawObject(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoMessaggioDefault()));
			appIO.setSubject(new RawObject(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoOggettoDefault()));
			rsModel.setAppIO(appIO);
		}

		TipoPendenzaDominioPost valori = new TipoPendenzaDominioPost();

		valori.codificaIUV(tipoVersamentoDominio.getCodificaIuvCustom())
		.pagaTerzi(tipoVersamentoDominio.getPagaTerziCustom())
		.abilitato(tipoVersamentoDominio.getAbilitatoCustom());

		if(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormDefinizioneCustom() != null && tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormTipoCustom() != null) {
			TipoPendenzaForm form = new TipoPendenzaForm();
			form.setTipo(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormTipoCustom());
			form.setDefinizione(new RawObject(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeFormDefinizioneCustom())); 
			valori.setForm(form);
		}

		TipoPendenzaPromemoria valoriAvvisaturaMailPromemoriaAvviso = new TipoPendenzaPromemoria();
		valoriAvvisaturaMailPromemoriaAvviso.setAbilitato(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoAbilitatoCustom() != null ? tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoAbilitatoCustom() : false);

		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoOggettoCustom() != null)
			valoriAvvisaturaMailPromemoriaAvviso.setOggetto(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoOggettoCustom()));

		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoMessaggioCustom() != null)
			valoriAvvisaturaMailPromemoriaAvviso.setMessaggio(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoMessaggioCustom()));
		
		valoriAvvisaturaMailPromemoriaAvviso.setAllegaPdf(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoPdfCustom());
		valoriAvvisaturaMailPromemoriaAvviso.setTipo(tipoVersamentoDominio.getAvvisaturaMailPromemoriaAvvisoTipoCustom());

		valori.setPromemoriaAvviso(valoriAvvisaturaMailPromemoriaAvviso);

		TipoPendenzaPromemoria valoriAvvisaturaMailPromemoriaRicevuta = new TipoPendenzaPromemoria();
		valoriAvvisaturaMailPromemoriaRicevuta.setAbilitato(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaAbilitatoCustom() != null ? tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaAbilitatoCustom() : false);

		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaOggettoCustom() != null)
			valoriAvvisaturaMailPromemoriaRicevuta.setOggetto(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaOggettoCustom()));

		if(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaMessaggioCustom() != null)
			valoriAvvisaturaMailPromemoriaRicevuta.setMessaggio(new RawObject(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaMessaggioCustom()));
		
		valoriAvvisaturaMailPromemoriaRicevuta.setAllegaPdf(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaPdfCustom());
		valoriAvvisaturaMailPromemoriaRicevuta.setTipo(tipoVersamentoDominio.getAvvisaturaMailPromemoriaRicevutaTipoCustom());

		valori.setPromemoriaRicevuta(valoriAvvisaturaMailPromemoriaRicevuta);

		if(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneTipoCustom() != null && tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneDefinizioneCustom() != null) {
			TipoPendenzaTrasformazione trasformazione  = new TipoPendenzaTrasformazione();
			trasformazione.setTipo(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneTipoCustom());
			trasformazione.setDefinizione(new RawObject(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeTrasformazioneDefinizioneCustom())); 
			valori.setTrasformazione(trasformazione);
		}
		if(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeValidazioneDefinizioneCustom() != null)
			valori.setValidazione(new RawObject(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeValidazioneDefinizioneCustom()));

		valori.setInoltro(tipoVersamentoDominio.getCaricamentoPendenzePortaleBackofficeCodApplicazioneCustom());

		if(tipoVersamentoDominio.getVisualizzazioneDefinizioneCustom() != null)
			valori.setVisualizzazione(new RawObject(tipoVersamentoDominio.getVisualizzazioneDefinizioneCustom()));

		if(tipoVersamentoDominio.getTracciatoCsvTipoCustom() != null &&  
				tipoVersamentoDominio.getTracciatoCsvIntestazioneCustom() != null && 
				tipoVersamentoDominio.getTracciatoCsvRichiestaCustom() != null && 
				tipoVersamentoDominio.getTracciatoCsvRispostaCustom() != null) {
			TracciatoCsv tracciatoCsv = new TracciatoCsv();
			tracciatoCsv.setTipo(tipoVersamentoDominio.getTracciatoCsvTipoCustom());
			tracciatoCsv.setIntestazione(tipoVersamentoDominio.getTracciatoCsvIntestazioneCustom());
			tracciatoCsv.setRichiesta(new RawObject(tipoVersamentoDominio.getTracciatoCsvRichiestaCustom()));
			tracciatoCsv.setRisposta(new RawObject(tipoVersamentoDominio.getTracciatoCsvRispostaCustom()));
			valori.setTracciatoCsv(tracciatoCsv);
		}
		
		TipoPendenzaAppIO valoriAppIO = new TipoPendenzaAppIO();
		valoriAppIO.setAbilitato(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoAbilitatoCustom());
		valoriAppIO.setApiKey(tipoVersamentoDominio.getAppIOAPIKey());
		
		if(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoTipoCustom() != null && tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoMessaggioCustom() != null && 
				tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoOggettoCustom() != null ) {
			ConfigurazioneGenerazioneMessageAppIO message = new ConfigurazioneGenerazioneMessageAppIO();
			
			message.setTipo(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoTipoCustom());
			message.setBody(new RawObject(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoMessaggioCustom()));
			message.setSubject(new RawObject(tipoVersamentoDominio.getAvvisaturaAppIoPromemoriaAvvisoOggettoCustom()));
			valoriAppIO.setMessage(message);
		}
		valori.setAppIO(valoriAppIO);

		rsModel.setValori(valori);

		return rsModel;
	}

	public static PutTipoPendenzaDominioDTO getPutTipoPendenzaDominioDTO(TipoPendenzaDominioPost tipoPendenzaRequest, String idDominio, String idTipoPendenza, Authentication user) throws ValidationException, ServiceException {
		PutTipoPendenzaDominioDTO tipoPendenzaDTO = new PutTipoPendenzaDominioDTO(user);

		it.govpay.bd.model.TipoVersamentoDominio tipoVersamentoDominio = new it.govpay.bd.model.TipoVersamentoDominio();

		tipoVersamentoDominio.setCodTipoVersamento(idTipoPendenza);
		tipoVersamentoDominio.setCodificaIuvCustom(tipoPendenzaRequest.getCodificaIUV());
		tipoVersamentoDominio.setAbilitatoCustom(tipoPendenzaRequest.Abilitato());
		tipoVersamentoDominio.setPagaTerziCustom(tipoPendenzaRequest.PagaTerzi());

		if(tipoPendenzaRequest.getForm() != null) {
			Object definizione = tipoPendenzaRequest.getForm().getDefinizione();
			if(definizione != null) {
				tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeFormDefinizioneCustom(ConverterUtils.toJSON(definizione,null));
			} else 
				tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeFormDefinizioneCustom(null);

			tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeFormTipoCustom(tipoPendenzaRequest.getForm().getTipo());
		}

		if(tipoPendenzaRequest.getPromemoriaAvviso() != null) {

			if(tipoPendenzaRequest.getPromemoriaAvviso().Abilitato() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoAbilitatoCustom(tipoPendenzaRequest.getPromemoriaAvviso().Abilitato());
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoAbilitatoCustom(null);
			}

			if(tipoPendenzaRequest.getPromemoriaAvviso().getMessaggio() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoMessaggioCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getPromemoriaAvviso().getMessaggio(),null));
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoMessaggioCustom(null);
			}
			if(tipoPendenzaRequest.getPromemoriaAvviso().getOggetto() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoOggettoCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getPromemoriaAvviso().getOggetto(),null));
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoOggettoCustom(null);
			}
			if(tipoPendenzaRequest.getPromemoriaAvviso().AllegaPdf() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoPdfCustom(tipoPendenzaRequest.getPromemoriaAvviso().AllegaPdf());
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoPdfCustom(null);
			}
			if(tipoPendenzaRequest.getPromemoriaAvviso().getTipo() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoTipoCustom(tipoPendenzaRequest.getPromemoriaAvviso().getTipo());
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaAvvisoTipoCustom(null);
			}

			if(tipoPendenzaRequest.getPromemoriaAvviso().getTipo() != null) {
				// valore tipo contabilita non valido
				if(it.govpay.backoffice.v1.beans.TipoPendenzaPromemoria.TipoEnum.fromValue(tipoPendenzaRequest.getPromemoriaAvviso().getTipo()) == null) {
					throw new ValidationException("Codifica inesistente per tipo trasformazione. Valore fornito [" +
							tipoPendenzaRequest.getPromemoriaAvviso().getTipo() + "] valori possibili " + ArrayUtils.toString(it.govpay.backoffice.v1.beans.TipoPendenzaPromemoria.TipoEnum.values()));
				}
			}
		}

		if(tipoPendenzaRequest.getPromemoriaRicevuta() != null) {
			if(tipoPendenzaRequest.getPromemoriaRicevuta().Abilitato() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaAbilitatoCustom(tipoPendenzaRequest.getPromemoriaRicevuta().Abilitato());
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaAbilitatoCustom(null);
			}

			if(tipoPendenzaRequest.getPromemoriaRicevuta().getMessaggio() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaMessaggioCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getPromemoriaRicevuta().getMessaggio(),null));
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaMessaggioCustom(null);
			}

			if(tipoPendenzaRequest.getPromemoriaRicevuta().getOggetto() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaOggettoCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getPromemoriaRicevuta().getOggetto(),null));
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaOggettoCustom(null);
			}

			if(tipoPendenzaRequest.getPromemoriaRicevuta().AllegaPdf() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaPdfCustom(tipoPendenzaRequest.getPromemoriaRicevuta().AllegaPdf());
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaPdfCustom(null);
			}

			if(tipoPendenzaRequest.getPromemoriaRicevuta().getTipo() != null) {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaTipoCustom(tipoPendenzaRequest.getPromemoriaRicevuta().getTipo());
			}else {
				tipoVersamentoDominio.setAvvisaturaMailPromemoriaRicevutaTipoCustom(null);
			}


			if(tipoPendenzaRequest.getPromemoriaRicevuta().getTipo() != null) {
				// valore tipo contabilita non valido
				if(it.govpay.backoffice.v1.beans.TipoPendenzaPromemoria.TipoEnum.fromValue(tipoPendenzaRequest.getPromemoriaRicevuta().getTipo()) == null) {
					throw new ValidationException("Codifica inesistente per tipo trasformazione. Valore fornito [" +
							tipoPendenzaRequest.getPromemoriaRicevuta().getTipo() + "] valori possibili " + ArrayUtils.toString(it.govpay.backoffice.v1.beans.TipoPendenzaPromemoria.TipoEnum.values()));
				}
			}
		}

		if(tipoPendenzaRequest.getTrasformazione() != null) {
			if(tipoPendenzaRequest.getTrasformazione().getTipo() != null) {
				// valore tipo contabilita non valido
				if(TipoEnum.fromValue(tipoPendenzaRequest.getTrasformazione().getTipo()) == null) {
					throw new ValidationException("Codifica inesistente per tipo trasformazione. Valore fornito [" + tipoPendenzaRequest.getTrasformazione().getTipo() + "] valori possibili " + ArrayUtils.toString(TipoEnum.values()));
				}
			}

			Object definizione = tipoPendenzaRequest.getTrasformazione().getDefinizione();
			if(definizione ==null)
				tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeTrasformazioneDefinizioneCustom(null);
			else
				tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeTrasformazioneDefinizioneCustom(ConverterUtils.toJSON(definizione,null));

			tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeTrasformazioneTipoCustom(tipoPendenzaRequest.getTrasformazione().getTipo());
		}
		if(tipoPendenzaRequest.getValidazione() != null)
			tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeValidazioneDefinizioneCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getValidazione(),null));

		if(tipoPendenzaRequest.getInoltro() != null)
			tipoVersamentoDominio.setCaricamentoPendenzePortaleBackofficeCodApplicazioneCustom(tipoPendenzaRequest.getInoltro());

		if(tipoPendenzaRequest.getVisualizzazione() != null)
			tipoVersamentoDominio.setVisualizzazioneDefinizioneCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getVisualizzazione(),null));

		if(tipoPendenzaRequest.getTracciatoCsv() != null
				&& tipoPendenzaRequest.getTracciatoCsv().getTipo() != null
				&& tipoPendenzaRequest.getTracciatoCsv().getIntestazione() != null
				&& tipoPendenzaRequest.getTracciatoCsv().getRichiesta() != null
				&& tipoPendenzaRequest.getTracciatoCsv().getRisposta() != null) {
			tipoVersamentoDominio.setTracciatoCsvTipoCustom(tipoPendenzaRequest.getTracciatoCsv().getTipo());
			if(tipoPendenzaRequest.getTracciatoCsv().getTipo() != null) {
				// valore tipo contabilita non valido
				if(it.govpay.backoffice.v1.beans.TracciatoCsv.TipoEnum.fromValue(tipoPendenzaRequest.getTracciatoCsv().getTipo()) == null) {
					throw new ValidationException("Codifica inesistente per tipo trasformazione. Valore fornito [" +
							tipoPendenzaRequest.getTracciatoCsv().getTipo() + "] valori possibili " + ArrayUtils.toString(it.govpay.backoffice.v1.beans.TracciatoCsv.TipoEnum.values()));
				}
			}

			tipoVersamentoDominio.setTracciatoCsvIntestazioneCustom(tipoPendenzaRequest.getTracciatoCsv().getIntestazione());
			tipoVersamentoDominio.setTracciatoCsvRichiestaCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getTracciatoCsv().getRichiesta(),null));
			tipoVersamentoDominio.setTracciatoCsvRispostaCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getTracciatoCsv().getRisposta(),null));
		}

		tipoPendenzaDTO.setTipoVersamentoDominio(tipoVersamentoDominio);
		tipoPendenzaDTO.setIdDominio(idDominio);
		tipoPendenzaDTO.setCodTipoVersamento(idTipoPendenza);

		tipoVersamentoDominio.setAvvisaturaAppIoPromemoriaAvvisoAbilitatoCustom(false);
		if(tipoPendenzaRequest.getAppIO() != null) {
			tipoVersamentoDominio.setAvvisaturaAppIoPromemoriaAvvisoAbilitatoCustom(tipoPendenzaRequest.getAppIO().Abilitato());
			tipoVersamentoDominio.setAppIOAPIKey(tipoPendenzaRequest.getAppIO().getApiKey());
			
			if(tipoPendenzaRequest.getAppIO() != null &&  
					tipoPendenzaRequest.getAppIO().getMessage() != null && 
					tipoPendenzaRequest.getAppIO().getMessage().getTipo() != null && tipoPendenzaRequest.getAppIO().getMessage().getSubject() != null && 
					tipoPendenzaRequest.getAppIO().getMessage().getBody() != null ) {
				
				tipoVersamentoDominio.setAvvisaturaAppIoPromemoriaAvvisoTipoCustom(tipoPendenzaRequest.getAppIO().getMessage().getTipo());
				
				// valore tipo contabilita non valido
				if(it.govpay.backoffice.v1.beans.ConfigurazioneGenerazioneMessageAppIO.TipoEnum.fromValue(tipoPendenzaRequest.getAppIO().getMessage().getTipo()) == null) {
					throw new ValidationException("Codifica inesistente per tipo trasformazione. Valore fornito [" +
							tipoPendenzaRequest.getAppIO().getMessage().getTipo() + "] valori possibili " + ArrayUtils.toString(it.govpay.backoffice.v1.beans.ConfigurazioneGenerazioneMessageAppIO.TipoEnum.values()));
				}
							
				tipoVersamentoDominio.setAvvisaturaAppIoPromemoriaAvvisoMessaggioCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getAppIO().getMessage().getBody(),null));
				tipoVersamentoDominio.setAvvisaturaAppIoPromemoriaAvvisoOggettoCustom(ConverterUtils.toJSON(tipoPendenzaRequest.getAppIO().getMessage().getSubject(),null));
			}
		}
		

		return tipoPendenzaDTO;		
	}
	public static it.govpay.core.dao.commons.Dominio getDominioCommons(DominioProfiloPost dominio) { 
		it.govpay.core.dao.commons.Dominio dominioCommons = new it.govpay.core.dao.commons.Dominio();

		dominioCommons.setCodDominio(dominio.getIdDominio());
		if(dominio.getUnitaOperative() != null) {
			List<Uo> uoList = new ArrayList<>();

			for (String uo : dominio.getUnitaOperative()) {
				if(uo.equals(ApplicazioniController.AUTORIZZA_UO_STAR)) {
					uoList.clear();
					break;
				}

				Uo uoCommons = new Uo();
				uoCommons.setCodUo(uo);
				uoList.add(uoCommons);
			}

			dominioCommons.setUo(uoList );
		}

		return dominioCommons;
	}

	public static it.govpay.core.dao.commons.Dominio getDominioCommons(String codDominio) { 
		it.govpay.core.dao.commons.Dominio dominioCommons = new it.govpay.core.dao.commons.Dominio();

		dominioCommons.setCodDominio(codDominio);
		return dominioCommons;
	}
}
