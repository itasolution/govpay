/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2015 Link.it srl (http://www.link.it).
 * Copyright (c) 2014-2015 TAS S.p.A. (http://www.tasgroup.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
package it.govpay.web.console.anagrafica.form;

import it.govpay.ejb.core.model.OperatoreModel;
import it.govpay.web.console.utils.Utils;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.openspcoop2.generic_project.web.form.SearchForm;
import org.openspcoop2.generic_project.web.impl.jsf1.form.BaseSearchForm;


@Named("enteSearchForm") @SessionScoped
public class EnteSearchForm extends BaseSearchForm implements SearchForm,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Operatore collegato, utilizzo le informazioni presenti in questo bean come filtro di ricerca per gli enti
	private OperatoreModel loggedUtente= null;
	
	@PostConstruct	
	@Override
	public void init() {
		this.setLoggedUtente(Utils.getLoggedUtente());
		resetParametriPaginazione();
	}

	@Override
	public void reset() {
		resetParametriPaginazione();
	}

	public OperatoreModel getLoggedUtente() {
		return this.loggedUtente;
	}

	public void setLoggedUtente(OperatoreModel loggedUtente) {
		this.loggedUtente = loggedUtente;
	}
	
}
