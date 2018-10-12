/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2018 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.bd.viste.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openspcoop2.utils.serialization.IDeserializer;
import org.openspcoop2.utils.serialization.IOException;
import org.openspcoop2.utils.serialization.ISerializer;
import org.openspcoop2.utils.serialization.SerializationConfig;
import org.openspcoop2.utils.serialization.SerializationFactory;
import org.openspcoop2.utils.serialization.SerializationFactory.SERIALIZATION_TYPE;

import it.govpay.bd.model.ListaNote;
import it.govpay.bd.model.Nota;
import it.govpay.bd.model.Versamento;
import it.govpay.core.utils.SimpleDateFormatUtils;

public class VersamentoIncasso extends Versamento{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L; 
	
	public enum StatoPagamento {
		PAGATO, INCASSATO, NON_PAGATO
	}
	
	public enum StatoVersamento {
		NON_ESEGUITO,
		ESEGUITO,
		PARZIALMENTE_ESEGUITO,
		ANNULLATO,
		ANOMALO,
		ESEGUITO_SENZA_RPT,
		INCASSATO;
	}
	
	
	private Date dataPagamento;
	private BigDecimal importoPagato;
	private BigDecimal importoIncassato;
	private StatoPagamento statoPagamento;
	private List<Nota> note;
	private String iuvPagamento;
	
	public Date getDataPagamento() {
		return dataPagamento;
	}
	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}
	public BigDecimal getImportoPagato() {
		return importoPagato;
	}
	public void setImportoPagato(BigDecimal importoPagato) {
		this.importoPagato = importoPagato;
	}
	public BigDecimal getImportoIncassato() {
		return importoIncassato;
	}
	public void setImportoIncassato(BigDecimal importoIncassato) {
		this.importoIncassato = importoIncassato;
	}
	public StatoPagamento getStatoPagamento() {
		return statoPagamento;
	}
	public void setStatoPagamento(StatoPagamento statoPagamento) {
		this.statoPagamento = statoPagamento;
	}
	public List<Nota> getNote() {
		if(this.note == null) this.note = new ArrayList<>();
		return this.note;
	}

	public String getNoteString() throws IOException {
		SerializationConfig serializationConfig = new SerializationConfig();
		serializationConfig.setDf(SimpleDateFormatUtils.newSimpleDateFormat());
		ISerializer serializer = SerializationFactory.getSerializer(SERIALIZATION_TYPE.JSON_JACKSON, serializationConfig);
		ListaNote listaNote = new ListaNote();
		listaNote.setLista(this.note);
		return serializer.getObject(listaNote);
	}

	public void setNote(String note) throws IOException {
		SerializationConfig serializationConfig = new SerializationConfig();
		serializationConfig.setDf(SimpleDateFormatUtils.newSimpleDateFormat());
		IDeserializer deserializer = SerializationFactory.getDeserializer(SERIALIZATION_TYPE.JSON_JACKSON, serializationConfig);
		ListaNote listaNote = (ListaNote) deserializer.getObject(note, ListaNote.class);
		this.note = listaNote.getLista();
	}

	public void setNote(List<Nota> note) {
		this.note = note;
	}
	public String getIuvPagamento() {
		return iuvPagamento;
	}
	public void setIuvPagamento(String iuvPagamento) {
		this.iuvPagamento = iuvPagamento;
	}
}
