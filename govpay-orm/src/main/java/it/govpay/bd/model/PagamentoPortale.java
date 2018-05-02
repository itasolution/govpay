package it.govpay.bd.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.pagamento.VersamentiBD;
import it.govpay.bd.pagamento.filters.VersamentoFilter;
import it.govpay.model.BasicModel;
import it.govpay.orm.IdVersamento;

public class PagamentoPortale extends BasicModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum VersioneInterfacciaWISP {
		WISP_1_3("1.3"),  
		WISP_2_0("2.0");
		
		private String codifica; 
		
		private VersioneInterfacciaWISP(String codifica) {
			this.codifica = codifica;
		}
		
		public String getCodifica() {
			return codifica;
		}
		
		public static VersioneInterfacciaWISP toEnum(String codifica) throws ServiceException {
			for(VersioneInterfacciaWISP v : VersioneInterfacciaWISP.values()){
				if(v.getCodifica().equals(codifica))
					return v;
			}
			
			throw new ServiceException("Codifica inesistente per VersioneInterfacciaWISP. Valore fornito [" + codifica + "] valori possibili " + ArrayUtils.toString(VersioneInterfacciaWISP.values()));
		}
	}

	public enum STATO { 
		IN_CORSO, // codici: DA_REDIRIGERE_AL_WISP, PAGAMENTO_IN_CORSO_AL_PSP, PAGAMENTO_IN_ATTESA_DI_ESITO, SELEZIONE_WISP_IN_CORSO
		ANNULLATO, // codici: SELEZIONE_WISP_ANNULLATA
		FALLITO, // codici: PAGAMENTO_FALLITO, SELEZIONE_WISP_FALLITA, SELEZIONE_WISP_TIMEOUT
		ESEGUITO, // codici: PAGAMENTO_ESEGUITO
		NON_ESEGUITO, // codici: PAGAMENTO_NON_ESEGUITO
		ESEGUITO_PARZIALE // codici: PAGAMENTO_PARZIALMENTE_ESEGUITO
	}

	public enum CODICE_STATO { 
		DA_REDIRIGERE_AL_WISP, 
		PAGAMENTO_IN_CORSO_AL_PSP, 
		PAGAMENTO_IN_ATTESA_DI_ESITO, 
		PAGAMENTO_ESEGUITO,
		PAGAMENTO_NON_ESEGUITO,
		PAGAMENTO_PARZIALMENTE_ESEGUITO,
		PAGAMENTO_FALLITO,
		SELEZIONE_WISP_IN_CORSO,
		SELEZIONE_WISP_FALLITA,
		SELEZIONE_WISP_TIMEOUT,
		SELEZIONE_WISP_ANNULLATA
	}

	private VersioneInterfacciaWISP versioneInterfacciaWISP = VersioneInterfacciaWISP.WISP_2_0;
	private String codApplicazione = null;
	private String nome = null;
	private String versanteIdentificativo = null;
	private String idSessione = null;
	private String idSessionePortale = null;
	private String idSessionePsp = null;
	private List<IdVersamento> idVersamento = null;
	private STATO stato = null;
	private CODICE_STATO codiceStato = null;
	private String descrizioneStato = null;
	private String pspRedirectUrl = null;
	private String pspEsito = null;
	private String jsonRequest = null;

	private String wispIdDominio = null;
	private String wispKeyPA = null;
	private String wispKeyWisp = null;

	private String wispHtml =null;

	private Date dataRichiesta = null;
	private Long id;
	private String urlRitorno = null;

	private String codPsp = null;
	private String tipoVersamento = null;
	private String codCanale = null;
	
	private Double importo = null; 
	private String multiBeneficiario = null;

	public String getCodApplicazione() {
		return codApplicazione;
	}
	public void setCodApplicazione(String codApplicazione) {
		this.codApplicazione = codApplicazione;
	}
	public String getIdSessione() {
		return idSessione;
	}
	public void setIdSessione(String idSessione) {
		this.idSessione = idSessione;
	}
	public String getIdSessionePortale() {
		return idSessionePortale;
	}
	public void setIdSessionePortale(String idSessionePortale) {
		this.idSessionePortale = idSessionePortale;
	}
	public String getIdSessionePsp() {
		return idSessionePsp;
	}
	public void setIdSessionePsp(String idSessionePsp) {
		this.idSessionePsp = idSessionePsp;
	}
	public STATO getStato() {
		return stato;
	}
	public void setStato(STATO stato) {
		this.stato = stato;
	}
	public String getPspRedirectUrl() {
		return pspRedirectUrl;
	}
	public void setPspRedirectUrl(String pspRedirectUrl) {
		this.pspRedirectUrl = pspRedirectUrl;
	}
	public String getJsonRequest() {
		return jsonRequest;
	}
	public void setJsonRequest(String jsonRequest) {
		this.jsonRequest = jsonRequest;
	}
	public String getWispIdDominio() {
		return wispIdDominio;
	}
	public List<IdVersamento> getIdVersamento() {
		return idVersamento;
	}
	public void setIdVersamento(List<IdVersamento> idVersamento) {
		this.idVersamento = idVersamento;
	}
	public void setWispIdDominio(String wispIdDominio) {
		this.wispIdDominio = wispIdDominio;
	}
	public String getWispHtml() {
		return wispHtml;
	}
	public void setWispHtml(String wispHtml) {
		this.wispHtml = wispHtml;
	}
	public Date getDataRichiesta() {
		return dataRichiesta;
	}
	public void setDataRichiesta(Date dataRichiesta) {
		this.dataRichiesta = dataRichiesta;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUrlRitorno() {
		return urlRitorno;
	}
	public void setUrlRitorno(String urlRitorno) {
		this.urlRitorno = urlRitorno;
	}
	public String getPspEsito() {
		return pspEsito;
	}
	public void setPspEsito(String pspEsito) {
		this.pspEsito = pspEsito;
	}
	public String getWispKeyPA() {
		return wispKeyPA;
	}
	public void setWispKeyPA(String wispKeyPA) {
		this.wispKeyPA = wispKeyPA;
	}
	public String getWispKeyWisp() {
		return wispKeyWisp;
	}
	public void setWispKeyWisp(String wispKeyWisp) {
		this.wispKeyWisp = wispKeyWisp;
	}
	public String getCodPsp() {
		return codPsp;
	}
	public void setCodPsp(String codPsp) {
		this.codPsp = codPsp;
	}
	public String getTipoVersamento() {
		return tipoVersamento;
	}
	public void setTipoVersamento(String tipoVersamento) {
		this.tipoVersamento = tipoVersamento;
	}
	public String getCodCanale() {
		return codCanale;
	}
	public void setCodCanale(String codCanale) {
		this.codCanale = codCanale;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getVersanteIdentificativo() {
		return versanteIdentificativo;
	}
	public void setVersanteIdentificativo(String versanteIdentificativo) {
		this.versanteIdentificativo = versanteIdentificativo;
	}
	public VersioneInterfacciaWISP getVersioneInterfacciaWISP() {
		return versioneInterfacciaWISP;
	}
	public void setVersioneInterfacciaWISP(VersioneInterfacciaWISP versioneInterfacciaWISP) {
		this.versioneInterfacciaWISP = versioneInterfacciaWISP;
	}


	// business
	private transient List<Versamento> versamenti;

	public List<Versamento> getVersamenti(BasicBD bd) throws ServiceException {
		if(versamenti != null)
			return versamenti;

		if(this.idVersamento != null && this.idVersamento.size() > 0) {
			VersamentiBD versamentiBD = new VersamentiBD(bd);
			VersamentoFilter filter = versamentiBD.newFilter();
			List<Long> ids = new ArrayList<Long>();
			for (IdVersamento idVs : this.idVersamento) {
				ids.add(idVs.getId());
			}
			filter.setIdVersamento(ids);
			this.versamenti = versamentiBD.findAll(filter );
		}
		return versamenti;
	}
	public CODICE_STATO getCodiceStato() {
		return codiceStato;
	}
	public void setCodiceStato(CODICE_STATO codiceStato) {
		this.codiceStato = codiceStato;
	}
	public String getDescrizioneStato() {
		return descrizioneStato;
	}
	public void setDescrizioneStato(String descrizioneStato) {
		this.descrizioneStato = descrizioneStato;
	}
	public Double getImporto() {
		return importo;
	}
	public void setImporto(Double importo) {
		this.importo = importo;
	}
	public String getMultiBeneficiario() {
		return multiBeneficiario;
	}
	public void setMultiBeneficiario(String multiBeneficiario) {
		this.multiBeneficiario = multiBeneficiario;
	}

}
