package it.govpay.core.dao.pagamenti.dto;

import java.util.Date;

import org.openspcoop2.generic_project.expression.SortOrder;
import org.springframework.security.core.Authentication;

import it.govpay.core.dao.anagrafica.dto.BasicFindRequestDTO;
import it.govpay.orm.Promemoria;

public class ListaPromemoriaDTO extends BasicFindRequestDTO{
	
	
	public ListaPromemoriaDTO(Authentication user) {
		super(user);
		this.addSortField("data", Promemoria.model().DATA_CREAZIONE);
		this.addDefaultSort(Promemoria.model().DATA_CREAZIONE,SortOrder.DESC);
	}
	private Date dataA;
	private Date dataDa;
	private it.govpay.model.Promemoria.StatoSpedizione stato;
	private it.govpay.model.Promemoria.TipoPromemoria tipo;
	private String idDominio;
	private String idPagamento;
	private String idDebitore;
	private String idA2A;
	private String idPendenza;
	private String iuv;
	
	public Date getDataA() {
		return this.dataA;
	}
	public void setDataA(Date dataA) {
		this.dataA = dataA;
	}
	public Date getDataDa() {
		return this.dataDa;
	}
	public void setDataDa(Date dataDa) {
		this.dataDa = dataDa;
	}
	public String getIdDominio() {
		return this.idDominio;
	}
	public void setIdDominio(String idDominio) {
		this.idDominio = idDominio;
	}
	public String getIdPagamento() {
		return this.idPagamento;
	}
	public void setIdPagamento(String idPagamento) {
		this.idPagamento = idPagamento;
	}
	public String getIdDebitore() {
		return this.idDebitore;
	}
	public void setIdDebitore(String idDebitore) {
		this.idDebitore = idDebitore;
	}
	public String getIdA2A() {
		return this.idA2A;
	}
	public void setIdA2A(String idA2A) {
		this.idA2A = idA2A;
	}
	public String getIdPendenza() {
		return idPendenza;
	}
	public void setIdPendenza(String idPendenza) {
		this.idPendenza = idPendenza;
	}
	public String getIuv() {
		return iuv;
	}
	public void setIuv(String iuv) {
		this.iuv = iuv;
	}
	public it.govpay.model.Promemoria.StatoSpedizione getStato() {
		return stato;
	}
	public void setStato(it.govpay.model.Promemoria.StatoSpedizione stato) {
		this.stato = stato;
	}
	public it.govpay.model.Promemoria.TipoPromemoria getTipo() {
		return tipo;
	}
	public void setTipo(it.govpay.model.Promemoria.TipoPromemoria tipo) {
		this.tipo = tipo;
	}
}
