-- 29/08/2019 Autorizzazione utenze per UO
ALTER TABLE utenze_domini ADD id_uo NUMBER;
ALTER TABLE utenze_domini ADD CONSTRAINT fk_nzd_id_uo FOREIGN KEY (id_uo) REFERENCES uo(id);
ALTER TABLE utenze_domini MODIFY (id_dominio NULL);

-- 26/09/2019 Identificativo univoco di creazione del versamento
ALTER TABLE versamenti ADD id_sessione VARCHAR2(35 CHAR);

DROP VIEW versamenti_incassi;

CREATE VIEW versamenti_incassi AS
SELECT
versamenti.id as id,
MAX(versamenti.cod_versamento_ente) as cod_versamento_ente,
MAX(versamenti.nome) as nome,
MAX(versamenti.importo_totale) as importo_totale,
versamenti.stato_versamento as stato_versamento,
MAX(versamenti.descrizione_stato) as descrizione_stato,
MAX(CASE WHEN versamenti.aggiornabile = 1 THEN 'TRUE' ELSE 'FALSE' END) AS aggiornabile,
MAX(versamenti.data_creazione) as data_creazione,
MAX(versamenti.data_validita) as data_validita,
MAX(versamenti.data_scadenza) as data_scadenza,
MAX(versamenti.data_ora_ultimo_aggiornamento) as data_ora_ultimo_aggiornamento,
MAX(versamenti.causale_versamento) as causale_versamento,
MAX(versamenti.debitore_tipo) as debitore_tipo,
versamenti.debitore_identificativo as debitore_identificativo,
MAX(versamenti.debitore_anagrafica) as debitore_anagrafica,
MAX(versamenti.debitore_indirizzo) as debitore_indirizzo,
MAX(versamenti.debitore_civico) as debitore_civico,
MAX(versamenti.debitore_cap) as debitore_cap,
MAX(versamenti.debitore_localita) as debitore_localita,
MAX(versamenti.debitore_provincia) as debitore_provincia,
MAX(versamenti.debitore_nazione) as debitore_nazione,
MAX(versamenti.debitore_email) as debitore_email,
MAX(versamenti.debitore_telefono) as debitore_telefono,
MAX(versamenti.debitore_cellulare) as debitore_cellulare,
MAX(versamenti.debitore_fax) as debitore_fax,
MAX(versamenti.tassonomia_avviso) as tassonomia_avviso,
MAX(versamenti.tassonomia) as tassonomia,
MAX(versamenti.cod_lotto) as cod_lotto,
MAX(versamenti.cod_versamento_lotto) as cod_versamento_lotto,
MAX(versamenti.cod_anno_tributario) as cod_anno_tributario,
MAX(versamenti.cod_bundlekey) as cod_bundlekey,
MAX(dbms_lob.substr(versamenti.dati_allegati)) as dati_allegati,
MAX(versamenti.incasso) as incasso,
MAX(dbms_lob.substr(versamenti.anomalie)) as anomalie,
MAX(versamenti.iuv_versamento) as iuv_versamento,
MAX(versamenti.numero_avviso) as numero_avviso,
MAX(versamenti.id_dominio) as id_dominio,
MAX(versamenti.id_tipo_versamento) AS id_tipo_versamento,
MAX(versamenti.id_tipo_versamento_dominio) AS id_tipo_versamento_dominio,
MAX(versamenti.id_uo) as id_uo,
MAX(versamenti.id_applicazione) as id_applicazione,
MAX(CASE WHEN versamenti.avvisatura_abilitata = 1 THEN 'TRUE' ELSE 'FALSE' END) AS avvisatura_abilitata,
MAX(CASE WHEN versamenti.avvisatura_da_inviare = 1 THEN 'TRUE' ELSE 'FALSE' END) AS avvisatura_da_inviare,
MAX(versamenti.avvisatura_operazione) as avvisatura_operazione,               
MAX(versamenti.avvisatura_modalita) as avvisatura_modalita,
MAX(versamenti.avvisatura_tipo_pagamento) as avvisatura_tipo_pagamento,                   
MAX(versamenti.avvisatura_cod_avvisatura) as avvisatura_cod_avvisatura,  
MAX(versamenti.divisione) as divisione,  
MAX(versamenti.direzione) as direzione,     
MAX(versamenti.id_tracciato) as id_tracciato,
MAX(versamenti.id_sessione) as id_sessione,
MAX(CASE WHEN versamenti.ack = 1 THEN 'TRUE' ELSE 'FALSE' END) AS ack,
MAX(CASE WHEN versamenti.anomalo = 1 THEN 'TRUE' ELSE 'FALSE' END) AS anomalo,
MAX(pagamenti.data_pagamento) as data_pagamento,
SUM(CASE WHEN pagamenti.importo_pagato IS NOT NULL THEN pagamenti.importo_pagato ELSE 0 END) AS importo_pagato,
SUM(CASE WHEN pagamenti.stato = 'INCASSATO' THEN pagamenti.importo_pagato ELSE 0 END) AS importo_incassato,
MAX(CASE WHEN pagamenti.stato IS NULL THEN 'NON_PAGATO' WHEN pagamenti.stato = 'INCASSATO' THEN 'INCASSATO' ELSE 'PAGATO' END) AS stato_pagamento,
MAX(pagamenti.iuv) AS iuv_pagamento,
MAX(CASE WHEN versamenti.stato_versamento = 'NON_ESEGUITO' AND versamenti.data_validita > CURRENT_DATE THEN 0 ELSE 1 END) AS smart_order_rank,
MIN(ABS((date_to_unix_for_smart_order(CURRENT_DATE) * 1000) - (date_to_unix_for_smart_order(COALESCE(pagamenti.data_pagamento, versamenti.data_validita, versamenti.data_creazione))) *1000)) AS smart_order_date
FROM versamenti LEFT JOIN singoli_versamenti ON versamenti.id = singoli_versamenti.id_versamento LEFT join pagamenti on singoli_versamenti.id = pagamenti.id_singolo_versamento 
JOIN tipi_versamento ON tipi_versamento.id = versamenti.id_tipo_versamento 
GROUP BY versamenti.id, versamenti.debitore_identificativo, versamenti.stato_versamento;


-- 01/10/2019 Configurazione Regole di Hardening API Public
INSERT INTO configurazione (NOME,VALORE) values ('hardening', '{"abilitato": true, "googleCatpcha": {"serverURL":"https://www.google.com/recaptcha/api/siteverify","siteKey":"CHANGE_ME","secretKey":"CHANGE_ME","soglia":1.0,"responseParameter":"gRecaptchaResponse","denyOnFail":true,"readTimeout":5000,"connectionTimeout":5000}}');

-- 07/11/2019 Abilitazione dei promemoria per tipo pendenza
ALTER TABLE tipi_versamento MODIFY promemoria_avviso_pdf NULL;
ALTER TABLE tipi_versamento MODIFY promemoria_avviso_pdf DEFAULT NULL;
ALTER TABLE tipi_versamento MODIFY promemoria_ricevuta_pdf NULL;
ALTER TABLE tipi_versamento MODIFY promemoria_ricevuta_pdf DEFAULT NULL;
ALTER TABLE tipi_versamento ADD promemoria_avviso_abilitato NUMBER NOT NULL;
ALTER TABLE tipi_versamento MODIFY promemoria_avviso_abilitato DEFAULT 0;
ALTER TABLE tipi_versamento ADD promemoria_ricevuta_abilitato NUMBER NOT NULL;
ALTER TABLE tipi_versamento MODIFY promemoria_ricevuta_abilitato DEFAULT 0;
UPDATE tipi_versamento SET promemoria_avviso_abilitato = 1 WHERE promemoria_avviso_oggetto IS NOT NULL AND promemoria_avviso_messaggio IS NOT NULL;
UPDATE tipi_versamento SET promemoria_ricevuta_abilitato = 1 WHERE promemoria_ricevuta_oggetto IS NOT NULL AND promemoria_ricevuta_messaggio IS NOT NULL;
ALTER TABLE tipi_versamento ADD trac_csv_tipo VARCHAR2(35 CHAR);

ALTER TABLE tipi_vers_domini ADD promemoria_avviso_abilitato NUMBER;
ALTER TABLE tipi_vers_domini ADD promemoria_ricevuta_abilitato NUMBER;
ALTER TABLE tipi_vers_domini ADD trac_csv_tipo VARCHAR2(35 CHAR);

-- 07/11/2019 Configurazione Regole di sistema per la Spedizione Promemoria 
INSERT INTO configurazione (NOME,VALORE) values ('tracciato_csv', '{"tipo":"freemarker","intestazione":"idA2A,idPendenza,idDominio,tipoPendenza,numeroAvviso,pdfAvviso,tipoSoggettoPagatore,identificativoPagatore,anagraficaPagatore,indirizzoPagatore,civicoPagatore,capPagatore,localitaPagatore,provinciaPagatore,nazionePagatore,emailPagatore,cellularePagatore,errore","richiesta":"\"PCNhc3NpZ24gY3N2VXRpbHMgPSBjbGFzc1siaXQuZ292cGF5LmNvcmUudXRpbHMuQ1NWVXRpbHMiXS5nZXRJbnN0YW5jZSgpPgo8I2Fzc2lnbiBjc3ZSZWNvcmQgPSBjc3ZVdGlscy5nZXRDU1ZSZWNvcmQobGluZWFDc3ZSaWNoaWVzdGEpPgp7CgkiaWRBMkEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMCl9LAoJImlkUGVuZGVuemEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMSl9LAoJImlkRG9taW5pbyI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAyKX0sCgkiaWRUaXBvUGVuZGVuemEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMyl9LAoJImlkVW5pdGFPcGVyYXRpdmEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNCl9LAogCSJjYXVzYWxlIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDUpfSwKIAkiYW5ub1JpZmVyaW1lbnRvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDYpfSwKIAkiY2FydGVsbGFQYWdhbWVudG8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNyl9LAogCSJkYXRpQWxsZWdhdGkiIDogewogCQkidGVzdEFjY2VudGkiOiAiw7LDoMOhw6LDo8Okw6XDpsOnw6jDqcOqw6vDrMOtw67DryIsCiAJCSJ0ZXN0Q2FyYXR0ZXJpIiA6ICIhIyQlJicoKSorLC0uLyIsCiAJCTwjaWYgIWNzdlV0aWxzLmlzRW1wdHkoY3N2UmVjb3JkLCA4KT4iZGF0aUFsbGVnYXRpQ1NWIjogJHtjc3ZSZWNvcmQuZ2V0KDgpfTwvI2lmPgogCX0sCiAJCiAJImRpcmV6aW9uZSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA5KX0sCiAJImRpdmlzaW9uZSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAxMCl9LAogCSJpbXBvcnRvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDExKX0sCgkiZGF0YVZhbGlkaXRhIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDEyKX0sCgkiZGF0YVNjYWRlbnphIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDEzKX0sCgkidGFzc29ub21pYUF2dmlzbyI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAxNCl9LAoJInNvZ2dldHRvUGFnYXRvcmUiOiB7CgkJInRpcG8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMTUpfSwKCQkiaWRlbnRpZmljYXRpdm8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMTYpfSwKCQkiYW5hZ3JhZmljYSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAxNyl9LAoJCSJpbmRpcml6em8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMTgpfSwKCQkiY2l2aWNvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDE5KX0sCgkJImNhcCI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAyMCl9LAoJCSJsb2NhbGl0YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAyMSl9LAoJCSJwcm92aW5jaWEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMjIpfSwKCQkibmF6aW9uZSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAyMyl9LAoJCSJlbWFpbCI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAyNCl9LAoJCSJjZWxsdWxhcmUiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMjUpfQoJfSwKCSJ2b2NpIjogWwoJCXsKCQkJImlkVm9jZVBlbmRlbnphIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDI2KX0sCgkJCSJpbXBvcnRvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDI3KX0sCgkJCSJkZXNjcml6aW9uZSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAyOCl9LAoJCQk8I2lmICFjc3ZVdGlscy5pc0VtcHR5KGNzdlJlY29yZCwgMzMpPgoJCQkidGlwb0VudHJhdGEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMzMpfQoJCQk8I2Vsc2U+CgkJCSJpYmFuQWNjcmVkaXRvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDI5KX0sCgkJCSJpYmFuQXBwb2dnaW8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMzApfSwKCQkJInRpcG9Db250YWJpbGl0YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAzMSl9LAoJCQkiY29kaWNlQ29udGFiaWxpdGEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMzIpfQoJCQk8LyNpZj4KCQl9CgkJPCNpZiAhY3N2VXRpbHMuaXNFbXB0eShjc3ZSZWNvcmQsIDM0KT4KCQksewoJCQkiaWRWb2NlUGVuZGVuemEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMzQpfSwKCQkJImltcG9ydG8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMzUpfSwKCQkJImRlc2NyaXppb25lIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDM2KX0sCgkJCTwjaWYgIWNzdlV0aWxzLmlzRW1wdHkoY3N2UmVjb3JkLCA0MSk+CgkJCSJ0aXBvRW50cmF0YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA0MSl9CgkJCTwjZWxzZT4KCQkJImliYW5BY2NyZWRpdG8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgMzcpfSwKCQkJImliYW5BcHBvZ2dpbyI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCAzOCl9LAoJCQkidGlwb0NvbnRhYmlsaXRhIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDM5KX0sCgkJCSJjb2RpY2VDb250YWJpbGl0YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA0MCl9CgkJCTwvI2lmPgoJCX0KCQk8LyNpZj4KCQk8I2lmICFjc3ZVdGlscy5pc0VtcHR5KGNzdlJlY29yZCwgNDIpPgkJCgkJLHsKCQkJImlkVm9jZVBlbmRlbnphIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDQyKX0sCgkJCSJpbXBvcnRvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDQzKX0sCgkJCSJkZXNjcml6aW9uZSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA0NCl9LAoJCQk8I2lmICFjc3ZVdGlscy5pc0VtcHR5KGNzdlJlY29yZCwgNDkpPgoJCQkidGlwb0VudHJhdGEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNDkpfQoJCQk8I2Vsc2U+CgkJCSJpYmFuQWNjcmVkaXRvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDQ1KX0sCgkJCSJpYmFuQXBwb2dnaW8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNDYpfSwKCQkJInRpcG9Db250YWJpbGl0YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA0Nyl9LAoJCQkiY29kaWNlQ29udGFiaWxpdGEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNDgpfQoJCQk8LyNpZj4KCQl9CgkJPC8jaWY+CgkJPCNpZiAhY3N2VXRpbHMuaXNFbXB0eShjc3ZSZWNvcmQsIDUwKT4KCQksewoJCQkiaWRWb2NlUGVuZGVuemEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNTApfSwKCQkJImltcG9ydG8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNTEpfSwKCQkJImRlc2NyaXppb25lIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDUyKX0sCgkJCTwjaWYgIWNzdlV0aWxzLmlzRW1wdHkoY3N2UmVjb3JkLCA1Nyk+CgkJCSJ0aXBvRW50cmF0YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA1Nyl9CgkJCTwjZWxzZT4KCQkJImliYW5BY2NyZWRpdG8iOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNTMpfSwKCQkJImliYW5BcHBvZ2dpbyI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA1NCl9LAoJCQkidGlwb0NvbnRhYmlsaXRhIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDU1KX0sCgkJCSJjb2RpY2VDb250YWJpbGl0YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA1Nil9CgkJCTwvI2lmPgoJCX0KCQk8LyNpZj4KCQk8I2lmICFjc3ZVdGlscy5pc0VtcHR5KGNzdlJlY29yZCwgNTgpPgoJCSx7CgkJCSJpZFZvY2VQZW5kZW56YSI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA1OCl9LAoJCQkiaW1wb3J0byI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA1OSl9LAoJCQkiZGVzY3JpemlvbmUiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNjApfSwKCQkJPCNpZiAhY3N2VXRpbHMuaXNFbXB0eShjc3ZSZWNvcmQsIDg1KT4KCQkJInRpcG9FbnRyYXRhIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDY1KX0KCQkJPCNlbHNlPgoJCQkiaWJhbkFjY3JlZGl0byI6ICR7Y3N2VXRpbHMudG9Kc29uVmFsdWUoY3N2UmVjb3JkLCA2MSl9LAoJCQkiaWJhbkFwcG9nZ2lvIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDYyKX0sCgkJCSJ0aXBvQ29udGFiaWxpdGEiOiAke2NzdlV0aWxzLnRvSnNvblZhbHVlKGNzdlJlY29yZCwgNjMpfSwKCQkJImNvZGljZUNvbnRhYmlsaXRhIjogJHtjc3ZVdGlscy50b0pzb25WYWx1ZShjc3ZSZWNvcmQsIDY0KX0KCQkJPC8jaWY+CgkJfQoJCTwvI2lmPgoJXQp9\"","risposta":"\"PCNhc3NpZ24gY3N2VXRpbHMgPSBjbGFzc1siaXQuZ292cGF5LmNvcmUudXRpbHMuQ1NWVXRpbHMiXS5nZXRJbnN0YW5jZSgpIC8+CjwjaWYgZXNpdG9PcGVyYXppb25lID09ICJFU0VHVUlUT19PSyI+CjwjYXNzaWduIGlkQTJBID0gYXBwbGljYXppb25lLmdldENvZEFwcGxpY2F6aW9uZSgpIC8+CjwjYXNzaWduIGlkUGVuZGVuemEgPSB2ZXJzYW1lbnRvLmdldENvZFZlcnNhbWVudG9FbnRlKCkgLz4KPCNhc3NpZ24gaWREb21pbmlvID0gZG9taW5pby5nZXRDb2REb21pbmlvKCkgLz4KPCNhc3NpZ24gdGlwb1BlbmRlbnphID0gaWRUaXBvVmVyc2FtZW50byAvPgo8I2Fzc2lnbiBudW1lcm9BdnZpc28gPSB2ZXJzYW1lbnRvLmdldE51bWVyb0F2dmlzbygpISAvPgo8I2lmIG51bWVyb0F2dmlzbz9oYXNfY29udGVudD4KCTwjYXNzaWduIHBkZkF2dmlzbyA9IGlkRG9taW5pbyArICJfIiArIG51bWVyb0F2dmlzbyArICIucGRmIiAvPgo8LyNpZj4KPCNhc3NpZ24gdGlwbyA9IHZlcnNhbWVudG8uZ2V0QW5hZ3JhZmljYURlYml0b3JlKCkuZ2V0VGlwbygpLnRvU3RyaW5nKCkgLz4KPCNhc3NpZ24gaWRlbnRpZmljYXRpdm8gPSB2ZXJzYW1lbnRvLmdldEFuYWdyYWZpY2FEZWJpdG9yZSgpLmdldENvZFVuaXZvY28oKSEgLz4KPCNhc3NpZ24gYW5hZ3JhZmljYSA9IHZlcnNhbWVudG8uZ2V0QW5hZ3JhZmljYURlYml0b3JlKCkuZ2V0UmFnaW9uZVNvY2lhbGUoKSEgLz4KPCNhc3NpZ24gaW5kaXJpenpvID0gdmVyc2FtZW50by5nZXRBbmFncmFmaWNhRGViaXRvcmUoKS5nZXRJbmRpcml6em8oKSEgLz4KPCNhc3NpZ24gY2l2aWNvID0gdmVyc2FtZW50by5nZXRBbmFncmFmaWNhRGViaXRvcmUoKS5nZXRDaXZpY28oKSEgLz4KPCNhc3NpZ24gY2FwID0gdmVyc2FtZW50by5nZXRBbmFncmFmaWNhRGViaXRvcmUoKS5nZXRDYXAoKSEgLz4KPCNhc3NpZ24gbG9jYWxpdGEgPSB2ZXJzYW1lbnRvLmdldEFuYWdyYWZpY2FEZWJpdG9yZSgpLmdldExvY2FsaXRhKCkhIC8+CjwjYXNzaWduIHByb3ZpbmNpYSA9IHZlcnNhbWVudG8uZ2V0QW5hZ3JhZmljYURlYml0b3JlKCkuZ2V0UHJvdmluY2lhKCkhIC8+CjwjYXNzaWduIG5hemlvbmUgPSB2ZXJzYW1lbnRvLmdldEFuYWdyYWZpY2FEZWJpdG9yZSgpLmdldE5hemlvbmUoKSEgLz4KPCNhc3NpZ24gZW1haWwgPSB2ZXJzYW1lbnRvLmdldEFuYWdyYWZpY2FEZWJpdG9yZSgpLmdldEVtYWlsKCkhIC8+CjwjYXNzaWduIGNlbGx1bGFyZSA9IHZlcnNhbWVudG8uZ2V0QW5hZ3JhZmljYURlYml0b3JlKCkuZ2V0Q2VsbHVsYXJlKCkhIC8+CjwjYXNzaWduIGNzdlJlY29yZCA9IGNzdlV0aWxzLnRvQ3N2KGlkQTJBLCBpZFBlbmRlbnphLCBpZERvbWluaW8sIHRpcG9QZW5kZW56YSwgbnVtZXJvQXZ2aXNvLCBwZGZBdnZpc28sIHRpcG8sIGlkZW50aWZpY2F0aXZvLCBhbmFncmFmaWNhLCBpbmRpcml6em8sIGNpdmljbywgY2FwLCBsb2NhbGl0YSwgcHJvdmluY2lhLCBuYXppb25lLCBlbWFpbCwgY2VsbHVsYXJlKSAvPgoke2NzdlJlY29yZH0KPCNlbHNlPgosLCwsLCwsLCwsLCwsLCwsLCR7ZGVzY3JpemlvbmVFc2l0b09wZXJhemlvbmV9CjwvI2lmPg==\""}');
INSERT INTO configurazione (NOME,VALORE) values ('mail_batch', '{"abilitato": false, "mailserver": {"host": null, "port": null, "username": null, "password": null, "from": null, "readTimeout": 120000, "connectionTimeout": 10000 }}');
INSERT INTO configurazione (NOME,VALORE) values ('mail_promemoria', '{ "tipo": "freemarker", "oggetto": "\"UHJvbWVtb3JpYSBwYWdhbWVudG86ICR7dmVyc2FtZW50by5nZXRDYXVzYWxlVmVyc2FtZW50bygpLmdldFNpbXBsZSgpfQ==\"", "messaggio": "\"R2VudGlsZSAke3ZlcnNhbWVudG8uZ2V0QW5hZ3JhZmljYURlYml0b3JlKCkuZ2V0UmFnaW9uZVNvY2lhbGUoKX0sCgpsZSBub3RpZmljaGlhbW8gY2hlIMOoIHN0YXRhIGVtZXNzYSB1bmEgcmljaGllc3RhIGRpIHBhZ2FtZW50byBhIHN1byBjYXJpY286ICR7dmVyc2FtZW50by5nZXRDYXVzYWxlVmVyc2FtZW50bygpLmdldFNpbXBsZSgpfQoKPCNpZiB2ZXJzYW1lbnRvLmdldE51bWVyb0F2dmlzbygpP2hhc19jb250ZW50PgpQdcOyIGVmZmV0dHVhcmUgaWwgcGFnYW1lbnRvIHRyYW1pdGUgbCdhcHAgbW9iaWxlIElPIG9wcHVyZSBwcmVzc28gdW5vIGRlaSBwcmVzdGF0b3JpIGRpIHNlcnZpemkgZGkgcGFnYW1lbnRvIGFkZXJlbnRpIGFsIGNpcmN1aXRvIHBhZ29QQSB1dGlsaXp6YW5kbyBsJ2F2dmlzbyBkaSBwYWdhbWVudG8gYWxsZWdhdG8uCjwjZWxzZT4KUHVvJyBlZmZldHR1YXJlIGlsIHBhZ2FtZW50byBvbi1saW5lIHByZXNzbyBpbCBwb3J0YWxlIGRlbGwnZW50ZSBjcmVkaXRvcmU6ICR7ZG9taW5pby5nZXRSYWdpb25lU29jaWFsZSgpfSAKPC8jaWY+CgpEaXN0aW50aSBzYWx1dGku\"", "allegaPdf": true }');
INSERT INTO configurazione (NOME,VALORE) values ('mail_ricevuta', '{ "tipo": "freemarker", "oggetto": "\"PCNpZiBycHQuZ2V0RXNpdG9QYWdhbWVudG8oKS5nZXRDb2RpZmljYSgpID0gMD4KTm90aWZpY2EgcGFnYW1lbnRvIGVzZWd1aXRvOiAke3JwdC5nZXRDb2REb21pbmlvKCl9LyR7cnB0LmdldEl1digpfS8ke3JwdC5nZXRDY3AoKX0KPCNlbHNlaWYgcnB0LmdldEVzaXRvUGFnYW1lbnRvKCkuZ2V0Q29kaWZpY2EoKSA9IDE+Ck5vdGlmaWNhIHBhZ2FtZW50byBub24gZXNlZ3VpdG86ICR7cnB0LmdldENvZERvbWluaW8oKX0vJHtycHQuZ2V0SXV2KCl9LyR7cnB0LmdldENjcCgpfQo8I2Vsc2VpZiBycHQuZ2V0RXNpdG9QYWdhbWVudG8oKS5nZXRDb2RpZmljYSgpID0gMj4KTm90aWZpY2EgcGFnYW1lbnRvIGVzZWd1aXRvIHBhcnppYWxtZW50ZTogJHtycHQuZ2V0Q29kRG9taW5pbygpfS8ke3JwdC5nZXRJdXYoKX0vJHtycHQuZ2V0Q2NwKCl9CjwjZWxzZWlmIHJwdC5nZXRFc2l0b1BhZ2FtZW50bygpLmdldENvZGlmaWNhKCkgPSAzPgpOb3RpZmljYSBkZWNvcnJlbnphIHRlcm1pbmkgcGFnYW1lbnRvOiAke3JwdC5nZXRDb2REb21pbmlvKCl9LyR7cnB0LmdldEl1digpfS8ke3JwdC5nZXRDY3AoKX0KPCNlbHNlaWYgcnB0LmdldEVzaXRvUGFnYW1lbnRvKCkuZ2V0Q29kaWZpY2EoKSA9IDQ+Ck5vdGlmaWNhIGRlY29ycmVuemEgdGVybWluaSBwYWdhbWVudG86ICR7cnB0LmdldENvZERvbWluaW8oKX0vJHtycHQuZ2V0SXV2KCl9LyR7cnB0LmdldENjcCgpfQo8LyNpZj4=\"", "messaggio": "\"PCNhc3NpZ24gZGF0YVJpY2hpZXN0YSA9IHJwdC5nZXREYXRhTXNnUmljaGllc3RhKCk/c3RyaW5nKCJ5eXl5LU1NLWRkIEhIOm1tOnNzIik+CklsIHBhZ2FtZW50byBkaSAiJHt2ZXJzYW1lbnRvLmdldENhdXNhbGVWZXJzYW1lbnRvKCkuZ2V0U2ltcGxlKCl9IiBlZmZldHR1YXRvIGlsICR7ZGF0YVJpY2hpZXN0YX0gcmlzdWx0YSBjb25jbHVzbyBjb24gZXNpdG8gJHtycHQuZ2V0RXNpdG9QYWdhbWVudG8oKS5uYW1lKCl9OgoKRW50ZSBjcmVkaXRvcmU6ICR7ZG9taW5pby5nZXRSYWdpb25lU29jaWFsZSgpfSAoJHtkb21pbmlvLmdldENvZERvbWluaW8oKX0pCklzdGl0dXRvIGF0dGVzdGFudGU6ICR7cnB0LmdldERlbm9taW5hemlvbmVBdHRlc3RhbnRlKCl9ICgke3JwdC5nZXRJZGVudGlmaWNhdGl2b0F0dGVzdGFudGUoKX0pCklkZW50aWZpY2F0aXZvIHVuaXZvY28gdmVyc2FtZW50byAoSVVWKTogJHtycHQuZ2V0SXV2KCl9CkNvZGljZSBjb250ZXN0byBwYWdhbWVudG8gKENDUCk6ICR7cnB0LmdldENjcCgpfQpJbXBvcnRvIHBhZ2F0bzogJHtycHQuZ2V0SW1wb3J0b1RvdGFsZVBhZ2F0bygpfQoKRGlzdGludGkgc2FsdXRpLg==\"", "allegaPdf": true }');

-- 26/11/2019 Aggiunto identificativo_debitore alla vista pagamenti_portale;

DROP VIEW v_pagamenti_portale_ext;
CREATE VIEW v_pagamenti_portale_ext AS
 SELECT 
  pagamenti_portale.cod_canale,
  pagamenti_portale.nome,
  pagamenti_portale.importo,
  pagamenti_portale.versante_identificativo,
  pagamenti_portale.id_sessione,
  pagamenti_portale.id_sessione_portale,
  pagamenti_portale.id_sessione_psp,
  pagamenti_portale.stato,
  pagamenti_portale.codice_stato,
  pagamenti_portale.descrizione_stato,
  pagamenti_portale.psp_redirect_url,
  pagamenti_portale.psp_esito,
  pagamenti_portale.json_request,
  pagamenti_portale.wisp_id_dominio,
  pagamenti_portale.wisp_key_pa,
  pagamenti_portale.wisp_key_wisp,
  pagamenti_portale.wisp_html,
  pagamenti_portale.data_richiesta,
  pagamenti_portale.url_ritorno,
  pagamenti_portale.cod_psp,
  pagamenti_portale.tipo_versamento,
  pagamenti_portale.multi_beneficiario,
  pagamenti_portale.ack,
  pagamenti_portale.tipo,
  pagamenti_portale.principal,
  pagamenti_portale.tipo_utenza,
  pagamenti_portale.id,
  pagamenti_portale.id_applicazione,
  versamenti.debitore_identificativo,
  versamenti.id_dominio, 
  versamenti.id_uo, 
  versamenti.id_tipo_versamento 
FROM pagamenti_portale 
JOIN pag_port_versamenti ON pagamenti_portale.id = pag_port_versamenti.id_pagamento_portale 
JOIN versamenti ON versamenti.id=pag_port_versamenti.id_versamento;


-- 27/11/2019 Aggiunti riferimenti Incasso, Fr e Tracciato alla tabella eventi
ALTER TABLE eventi ADD id_fr NUMBER;
ALTER TABLE eventi ADD id_incasso NUMBER;
ALTER TABLE eventi ADD id_tracciato NUMBER;
ALTER TABLE eventi ADD CONSTRAINT fk_evt_id_fr FOREIGN KEY (id_fr) REFERENCES fr(id);
ALTER TABLE eventi ADD CONSTRAINT fk_evt_id_incasso FOREIGN KEY (id_incasso) REFERENCES incassi(id);
ALTER TABLE eventi ADD CONSTRAINT fk_evt_id_tracciato FOREIGN KEY (id_tracciato) REFERENCES tracciati(id);

-- 28/11/2019 Vista Eventi per Versamenti

CREATE VIEW v_eventi_vers_rendicontazioni AS (
        SELECT DISTINCT 
               versamenti.cod_versamento_ente as cod_versamento_ente,
               applicazioni.cod_applicazione as cod_applicazione,
               eventi.id
        FROM eventi 
        JOIN rendicontazioni ON rendicontazioni.id_fr = eventi.id_fr
        JOIN pagamenti ON pagamenti.id = rendicontazioni.id_pagamento
        JOIN singoli_versamenti ON pagamenti.id_singolo_versamento=singoli_versamenti.id
        JOIN versamenti ON singoli_versamenti.id_versamento=versamenti.id
        JOIN applicazioni ON versamenti.id_applicazione = applicazioni.id
);

CREATE VIEW v_eventi_vers_pagamenti AS (
        SELECT DISTINCT 
               versamenti.cod_versamento_ente as cod_versamento_ente,
               applicazioni.cod_applicazione as cod_applicazione,
               eventi.id
        FROM eventi LEFT JOIN pagamenti_portale ON eventi.id_sessione = pagamenti_portale.id_sessione
        JOIN pag_port_versamenti ON pagamenti_portale.id = pag_port_versamenti.id_pagamento_portale
        JOIN versamenti ON versamenti.id = pag_port_versamenti.id_versamento
        JOIN applicazioni ON versamenti.id_applicazione = applicazioni.id
);

CREATE VIEW v_eventi_vers_riconciliazioni AS (
        SELECT DISTINCT 
               versamenti.cod_versamento_ente as cod_versamento_ente,
               applicazioni.cod_applicazione as cod_applicazione,
               eventi.id
        FROM eventi
        JOIN pagamenti ON pagamenti.id_incasso = eventi.id_incasso
        JOIN singoli_versamenti ON pagamenti.id_singolo_versamento=singoli_versamenti.id
        JOIN versamenti ON singoli_versamenti.id_versamento=versamenti.id
        JOIN applicazioni ON versamenti.id_applicazione = applicazioni.id
);

CREATE VIEW v_eventi_vers_tracciati AS (
        SELECT DISTINCT 
               versamenti.cod_versamento_ente as cod_versamento_ente,
               applicazioni.cod_applicazione as cod_applicazione,
               eventi.id
        FROM eventi
        JOIN operazioni ON operazioni.id_tracciato = eventi.id_tracciato
        JOIN versamenti ON operazioni.id_applicazione = versamenti.id_applicazione AND operazioni.cod_versamento_ente = versamenti.cod_versamento_ente
        JOIN applicazioni ON versamenti.id_applicazione = applicazioni.id
);


DROP VIEW v_eventi_vers_base;

CREATE VIEW v_eventi_vers_base AS (
        SELECT DISTINCT 
               cod_versamento_ente,
               cod_applicazione,
               id
        FROM eventi
        UNION SELECT * FROM v_eventi_vers_pagamenti
        UNION SELECT * FROM v_eventi_vers_rendicontazioni
        UNION SELECT * FROM v_eventi_vers_riconciliazioni
        UNION SELECT * FROM v_eventi_vers_tracciati
        );
         
DROP VIEW v_eventi_vers;
CREATE VIEW v_eventi_vers AS (
        SELECT eventi.componente,
               eventi.ruolo,
               eventi.categoria_evento,
               eventi.tipo_evento,
               eventi.sottotipo_evento,
               eventi.data,
               eventi.intervallo,
               eventi.esito,
               eventi.sottotipo_esito,
               eventi.dettaglio_esito,
               eventi.parametri_richiesta,
               eventi.parametri_risposta,
               eventi.dati_pago_pa,
               v_eventi_vers_base.cod_versamento_ente,
               v_eventi_vers_base.cod_applicazione,
               eventi.iuv,
               eventi.cod_dominio,
               eventi.ccp,
               eventi.id_sessione,
               eventi.id
               FROM v_eventi_vers_base JOIN eventi ON v_eventi_vers_base.id = eventi.id
         );         
