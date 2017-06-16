/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2017 Link.it srl (http://www.link.it).
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
package it.govpay.core.utils.thread;

import it.govpay.bd.BasicBD;
import it.govpay.bd.loader.OperazioniBD;
import it.govpay.bd.loader.TracciatiBD;
import it.govpay.bd.loader.filters.OperazioneFilter;
import it.govpay.bd.loader.filters.OperazioneFilter.SortFields;
import it.govpay.bd.loader.model.Operazione;
import it.govpay.bd.loader.model.Tracciato;
import it.govpay.core.loader.utils.TimerCaricamentoTracciatoSmistatore;
import it.govpay.core.utils.GpContext;
import it.govpay.model.loader.Tracciato.StatoTracciatoType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openspcoop2.generic_project.exception.ServiceException;

public class CaricaTracciatoThread implements Runnable {

	private static Logger log = LogManager.getLogger();
	private Tracciato tracciato;
	private boolean completed = false;
	private BasicBD bd;

	public CaricaTracciatoThread(Tracciato tracciato, BasicBD bd) throws ServiceException {
		this.tracciato = tracciato;
		this.bd = bd;
	}

	@Override
	public void run() {
		
		GpContext ctx = null;
		try {
			
			TracciatiBD tracciatiBD = new TracciatiBD(bd);
			OperazioniBD operazioniBD = new OperazioniBD(bd);

			Date dataInizio = new Date();
			CaricaTracciatoThread.log.info("Caricamento tracciato ["+tracciato.getNomeFile()+"] iniziato alle ore: " + dataInizio);

			if(!tracciato.getStato().equals(StatoTracciatoType.IN_CARICAMENTO)) {
				tracciato.setLineaElaborazione(0);
				tracciato.setStato(StatoTracciatoType.IN_CARICAMENTO);
				tracciatiBD.updateTracciato(tracciato);
				bd.commit();
			}

			try {
				TimerCaricamentoTracciatoSmistatore smist = new TimerCaricamentoTracciatoSmistatore();
				List<List<List<byte[]>>> lstLst = splitCSV(tracciato);


				for(List<List<byte[]>> lst: lstLst) {
					int numLineeElaborate = smist.smista(lst, tracciato);
					tracciato.setLineaElaborazione(tracciato.getLineaElaborazione() + numLineeElaborate);
					tracciatiBD.updateTracciato(tracciato);
					bd.commit();
				}

				OperazioneFilter operazioneFilter = operazioniBD.newFilter();
				operazioneFilter.setIdTracciato(tracciato.getId());
				operazioneFilter.addSortField(SortFields.LINEA, true);
				List<Operazione> operazioniInserite = operazioniBD.findAll(operazioneFilter);
				
				long countErrori = 0;
				long countOK = 0;
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				for(Operazione op: operazioniInserite) {
					
					baos.write(op.getDatiRisposta());
				}
				tracciato.setNumOperazioniKo(countErrori);
				if(countErrori > 1) {
					tracciato.setStato(StatoTracciatoType.CARICAMENTO_KO);
				} else {
					tracciato.setStato(StatoTracciatoType.CARICAMENTO_OK);
				}
				

				tracciato.setNumOperazioniKo(countOK);

			} catch(Exception e) {
				CaricaTracciatoThread.log.error("Errore durante l'inserimento del tracciato ["+tracciato.getId()+"]: " + e.getMessage(), e);
				tracciato.setDataUltimoAggiornamento(new Date());
			}

			tracciatiBD.updateTracciato(tracciato);
			Date dataFine = new Date();
			CaricaTracciatoThread.log.info("Caricamento tracciato ["+tracciato.getNomeFile()+"] completato alle ore: " + dataFine);
			CaricaTracciatoThread.log.info("Caricamento tracciato ["+tracciato.getNomeFile()+"] in ["+((dataFine.getTime()-dataInizio.getTime()) / 1000)+"] secondi");
			
			bd.commit();

		} catch(Exception e) {
			CaricaTracciatoThread.log.error("Errore durante il caricamento tracciato ["+tracciato.getNomeFile()+"]: ", e.getMessage(), e);
			try {bd.rollback();} catch(Exception ex) {}
		} finally {
			completed = true;
			if(bd != null) bd.closeConnection(); 
			if(ctx != null) ctx.log();
		}
	}

	public boolean isCompleted() {
		return completed;
	}

	private List<List<List<byte[]>>> splitCSV(Tracciato tracciato) throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(tracciato.getRawDataRichiesta());
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(isr);

		br.skip(tracciato.getLineaElaborazione()); //skip prime n linee gia' lette
		
		int nFinestre = (int) ((tracciato.getNumLineeTotali() - tracciato.getLineaElaborazione()) / 100);
		if(nFinestre == 0)
			nFinestre = 1;

		int lineePerThread = (int) (nFinestre / 10);
		if(lineePerThread == 0)
			lineePerThread = 1;

		return getListListList(br, lineePerThread, nFinestre);
	}

	private List<byte[]> getNextList(BufferedReader br, int limit) throws Exception {
		List<byte[]> lst = new ArrayList<byte[]>();

		while(br.ready() && lst.size() < limit) {
			lst.add(br.readLine().getBytes());
		}

		if(lst.isEmpty()) return null;
		return lst;

	}

	private List<List<byte[]>> getNextListList(BufferedReader br, int limit1, int limit2) throws Exception {
		List<List<byte[]>> lst = new ArrayList<List<byte[]>>();

		while(br.ready() && lst.size() < limit2) {
			List<byte[]> nextList = getNextList(br, limit1);
			if(nextList != null)
				lst.add(nextList);
		}

		if(lst.isEmpty()) return null;
		return lst;

	}

	private List<List<List<byte[]>>> getListListList(BufferedReader br, int limit1, int limit2) throws Exception {
		List<List<List<byte[]>>> lst = new ArrayList<List<List<byte[]>>>();

		while(br.ready()) {
			List<List<byte[]>> nextListList = getNextListList(br, limit1, limit2);
			if(nextListList != null)
				lst.add(nextListList);
		}

		return lst;

	}

}
