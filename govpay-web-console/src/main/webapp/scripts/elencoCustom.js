function defaultFormatter(a){var i="<div>";return i+=a.titolo,i+="</div>",i+="<div secondary>",i+=a.sottotitolo,i+="</div>"}function versamenti1(a){var i="<div>";return i+=a.sottotitolo,i+="</div>",i+="<div secondary>",i+=a.titolo,i+="</div>"}function versamenti2(a){var i="icon_div_ok",s="icon_ok",e="check",o=_getEtichetta(a.voci.statoVersamento,!1);"NON_ESEGUITO"===getValore(a.voci.statoVersamento)&&(i="icon_div_ai",s="icon_ai",e=""),"ANNULLATO"===getValore(a.voci.statoVersamento)&&(i="div_no_border",s="icon_ai",e="block"),"ANOMALO"===getValore(a.voci.statoVersamento)&&(i="icon_div_ko",s="icon_ko",e="notification:priority-high");var t='<div class="custom_item_box">';return t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.codDominio),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.codDominio),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.id),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.id),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.cf),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.cf),t+="</span>",t+="</div>",t+="</div>",t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.importoTotale),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.importoTotale),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.iuv),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.iuv),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.scadenza),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.scadenza),t+="</span>",t+="</div>",t+="</div>",t+="</div>",t+="</div>"}function getEtichetta(a){return _getEtichetta(a,!0)}function _getEtichetta(a,i){return a?a.valore&&i?a.etichetta+": ":a.etichetta:""}function getValore(a){return a&&a.valore?a.valore:"&nbsp;"}function flussoRendicontazioni1(a){var i="icon_div_ok",s="icon_ok",e="check",o=_getEtichetta(a.voci.stato,!1);"ACCETTATA"!=getValore(a.voci.stato)&&(i="icon_div_ko",s="icon_ko",e="notification:priority-high");var t='<div class="custom_item_box">';t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna min_width">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.codFlusso),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.codFlusso),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.idDominio),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.idDominio),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.psp),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.psp),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.trn),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.trn),t+="</span>",t+="</div>",t+="</div>",t+="</div>",t+='<div class="vers_colonna valign_middle">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+='<div class="rend_badge rend_ok icon_ok" title="'+getEtichetta(a.voci.numRendicontazioniOk)+getValore(a.voci.numRendicontazioniOk)+'">'+getValore(a.voci.numRendicontazioniOk)+"</div>";var c=getValore(a.voci.numRendicontazioniAnomale);c&&c>0&&(t+='<div class="rend_badge rend_ko icon_ko" title="'+getEtichetta(a.voci.numRendicontazioniAnomale)+c+'">'+c+"</div>");var v=getValore(a.voci.numRendicontazioniAltroIntermediario);return v&&v>0&&(t+='<div class="rend_badge rend_ai icon_ai" title="'+getEtichetta(a.voci.numRendicontazioniAltroIntermediario)+v+'">'+v+"</div>"),t+="</span>",t+="</div>",t+="</div>",t+="</div>"}function rendicontazioni1(a){var i="icon_div_ok",s="icon_ok",e="add",o=_getEtichetta(a.voci.stato,!1);"3"===getValore(a.voci.esito)&&(e="remove"),"ANOMALA"===getValore(a.voci.stato)&&(i="icon_div_ko",s="icon_ko"),"ALTRO_INTERMEDIARIO"===getValore(a.voci.stato)&&(i="icon_div_ai",s="icon_ai");var t='<div class="custom_item_box">';if(t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.dominio),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.dominio),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.iuv),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.iuv),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.iur),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.iur),t+="</span>",t+="</div>",t+="</div>",t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.importo),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.importo),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.data),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.data),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.codSingoloVersamento),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.codSingoloVersamento),t+="</span>",t+="</div>",t+="</div>",a.voci.anomalie){t+='<div class="rend_padding_left">',t+='<span class="vers_etichetta">',t+=_getEtichetta(a.voci.anomalie,!1),t+="</span>";var c=getVoci(a.voci,"anomalie_");t+='<div class="">';for(var v in c)t+='<span class="vers_etichetta">',t+=getEtichetta(c[v]),t+="</span>",t+='<span class="vers_valore">',t+=getValore(c[v]),t+="</span>";t+="</div>",t+="</div>"}return t+="</div>",t+="</div>"}function getVoci(a,i){var s=[],e=Object.keys(a);for(var o in e){var t=e[o];t.startsWith(i)&&s.push(a[t])}return s}function transazioni1(a){var i="icon_div_ok",s="icon_ok",e="add",o=_getEtichetta(a.voci.statoTransazione,!1);"rr"===getValore(a.voci.tipo)&&(e="remove"),"finaleKo"===getValore(a.voci.stato)&&(i="icon_div_ko",s="icon_ko"),"inCorso"===getValore(a.voci.stato)&&(i="icon_div_ai",s="icon_ai");var t='<div class="custom_item_box">';return t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.data),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.data),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.iuv),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.iuv),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.ccp),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.ccp),t+="</span>",t+="</div>",t+="</div>",t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.importo),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.importo),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.psp),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.psp),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.canale),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.canale),t+="</span>",t+="</div>",t+="</div>",t+="</div>",t+="</div>"}function pagamenti1(a){var i="icon_div_ok",s="icon_ok",e="add",o=_getEtichetta(a.voci.statoPagamento,!1);"revocato"===getValore(a.voci.statoPagamento)&&(e="remove",i="icon_div_ko",s="icon_ko");var t='<div class="custom_item_box">';return t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.codSingoloVersamentoEnte),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.codSingoloVersamentoEnte),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.iur),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.iur),t+="</span>",t+="</div>",t+="</div>",t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.importoPagato),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.importoPagato),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.dataPagamento),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.dataPagamento),t+="</span>",t+="</div>",t+="</div>",t+="</div>",t+="</div>"}function estrattoContoPagamenti1(a){var i="icon_div_ok",s="icon_ok",e="add",o=_getEtichetta(a.voci.statoVersamento,!1);"ANOMALO"===getValore(a.voci.statoVersamento)&&(i="icon_div_ko",s="icon_ko"),"revocato"===getValore(a.voci.statoPagamento)&&(e="remove");var t='<div class="custom_item_box">';return t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.codSingoloVersamentoEnte),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.codSingoloVersamentoEnte),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.iur),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.iur),t+="</span>",t+="</div>",t+="</div>",t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.importoPagato),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.importoPagato),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.dataPagamento),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.dataPagamento),t+="</span>",t+="</div>",t+="</div>",t+="</div>",t+="</div>"}function domini1(a){var i="icon_div_ok",s="icon_ok",e="add",o=_getEtichetta(a.voci.stato,!1);"disabilitato"!==getValore(a.voci.stato)&&"nonVerificato"!==getValore(a.voci.stato)||(e="",i="icon_div_ai",s="icon_ai"),"errore"===getValore(a.voci.stato)&&(i="icon_div_ko",s="icon_ko",e="notification:priority-high");var t='<div class="custom_item_box">';return t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_valore_16">',t+=_getEtichetta(a.voci.ragioneSociale,!1),t+="</span>",t+="</div>",t+='<div class="vers_voce">',t+='<span class="vers_etichetta">',t+=getEtichetta(a.voci.codDominio),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.codDominio),t+="</span>",t+='<span class="vers_etichetta rend_padding_left">',t+=getEtichetta(a.voci.codIntermediario),t+="</span>",t+='<span class="vers_valore">',t+=getValore(a.voci.codIntermediario),t+="</span>",t+="</div>",t+="</div>",a.voci.anomalia&&(t+='<div class="rend_padding_left">',t+='<span class="vers_etichetta">',t+=_getEtichetta(a.voci.anomalia,!1),t+="</span>",t+='<div class="">',t+='<span class="vers_etichetta">',t+=getValore(a.voci.anomalia),t+="</span>",t+="</div>",t+="</div>"),t+="</div>",t+="</div>"}function stazioni1(a){var i="icon_div_ok",s="icon_ok",e="add",o=_getEtichetta(a.voci.stato,!1);"disabilitato"!==getValore(a.voci.stato)&&"nonVerificato"!==getValore(a.voci.stato)||(e="",i="icon_div_ai",s="icon_ai"),"errore"===getValore(a.voci.stato)&&(i="icon_div_ko",s="icon_ko",e="notification:priority-high");var t='<div class="custom_item_box">';return t+='<div class="vers_colonna-nopadding valign_middle">',t+='<div class="icon_div '+i+'" title="'+o+'"><iron-icon icon="'+e+'" class="'+s+'"></iron-icon></div>',t+="</div>",t+='<div class="custom_item_box rend_padding_left">',t+='<div class="vers_colonna">',t+='<div class="vers_voce">',t+='<span class="vers_valore_16">',t+=_getEtichetta(a.voci.codStazione,!1),t+="</span>",t+="</div>",t+="</div>",a.voci.anomalia&&(t+='<div class="rend_padding_left">',t+='<span class="vers_etichetta">',t+=_getEtichetta(a.voci.anomalia,!1),t+="</span>",t+='<div class="">',t+='<span class="vers_etichetta">',t+=getValore(a.voci.anomalia),t+="</span>",t+="</div>",t+="</div>"),t+="</div>",t+="</div>"}