package it.govpay.rs.v1.beans.converter;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.core.dao.anagrafica.dto.PutEntrataDTO;
import it.govpay.core.rs.v1.beans.base.TipoEntrata;
import it.govpay.core.rs.v1.beans.base.TipoContabilita;
import it.govpay.core.rs.v1.beans.base.TipoEntrataPost;
import it.govpay.core.rs.v1.beans.base.TipoTributoOnline;
import it.govpay.core.rs.v1.beans.base.TipoTributoPagaTerzi;
import it.govpay.model.IAutorizzato;

public class EntrateConverter {

	public static PutEntrataDTO getPutEntrataDTO(TipoEntrataPost entrataPost, String idEntrata, IAutorizzato user) throws ServiceException {
		PutEntrataDTO entrataDTO = new PutEntrataDTO(user);
		
		it.govpay.model.TipoTributo tipoTributo = new it.govpay.model.TipoTributo();

		tipoTributo.setCodContabilitaDefault(entrataPost.getCodiceContabilita());
		if(entrataPost.getCodificaIUV()!=null)
			tipoTributo.setCodTributoIuvDefault(entrataPost.getCodificaIUV().toString());
		tipoTributo.setCodTributo(idEntrata);
		tipoTributo.setDescrizione(entrataPost.getDescrizione());
		if(entrataPost.getTipoContabilita() != null) {
			switch (entrataPost.getTipoContabilita()) {
			case ALTRO:
				tipoTributo.setTipoContabilitaDefault(it.govpay.model.Tributo.TipoContabilita.ALTRO);
				break;
			case CAPITOLO:
				tipoTributo.setTipoContabilitaDefault(it.govpay.model.Tributo.TipoContabilita.CAPITOLO);
				break;
			case SIOPE:
				tipoTributo.setTipoContabilitaDefault(it.govpay.model.Tributo.TipoContabilita.SIOPE);
				break;
			case SPECIALE:
			default:
				tipoTributo.setTipoContabilitaDefault(it.govpay.model.Tributo.TipoContabilita.SPECIALE);
				break;
			}
		}

		entrataDTO.setCodTributo(idEntrata);
		entrataDTO.setTipoTributo(tipoTributo);
		
		if(entrataPost.getOnline() != null) {
			switch (entrataPost.getOnline()) {
			case TRUE:
				tipoTributo.setOnlineDefault(it.govpay.model.Tributo.CustomBooleanType.SI);
				break;
			case FALSE:
			default:
				tipoTributo.setOnlineDefault(it.govpay.model.Tributo.CustomBooleanType.NO);				
				break;
			}
		}
		
		if(entrataPost.getPagaTerzi() != null) {
			switch (entrataPost.getPagaTerzi()) {
			case TRUE:
				tipoTributo.setPagaTerziDefault(it.govpay.model.Tributo.CustomBooleanType.SI);
				break;
			case FALSE:
			default:
				tipoTributo.setPagaTerziDefault(it.govpay.model.Tributo.CustomBooleanType.NO);				
				break;
			}
		}
		
		return entrataDTO;		
	}
	
	public static TipoEntrata toTipoEntrataRsModel(it.govpay.model.TipoTributo tributo) {
		TipoEntrata rsModel = new TipoEntrata();
		
		rsModel.codiceContabilita(tributo.getCodContabilitaDefault())
		.codificaIUV(tributo.getCodTributoIuvDefault())
		.descrizione(tributo.getDescrizione())
		.idEntrata(tributo.getCodTributo());
		
		if(tributo.getTipoContabilitaDefault() != null) {
			switch (tributo.getTipoContabilitaDefault()) {
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
		
		if(tributo.getOnlineDefault() != null) {
			switch (tributo.getOnlineDefault()) {
			case SI:
				rsModel.setOnline(TipoTributoOnline.TRUE);
				break;
			case NO:
			default:
				rsModel.setOnline(TipoTributoOnline.FALSE);
				break;
			}
		}
		
		if(tributo.getPagaTerziDefault() != null) {
			switch (tributo.getPagaTerziDefault()) {
			case SI:
				rsModel.setPagaTerzi(TipoTributoPagaTerzi.TRUE);
				break;
			case NO:
			default:
				rsModel.setPagaTerzi(TipoTributoPagaTerzi.FALSE);
				break;
			}
		}
		
		return rsModel;
	}
}
