// Puente con la app Android (WebView). En navegador de escritorio degrada con elegancia.
export const platform = {
  get isAndroid() { return typeof window.Android !== 'undefined'; },
  exitApp() {
    if (this.isAndroid && window.Android.exitApp) { window.Android.exitApp(); return; }
    // En navegador no se puede cerrar la pestaña programáticamente salvo que la abriera un script.
    window.close();
    alert('Para salir, cierra la pestaña o la aplicación.');
  },
  vibrate(ms) {
    try { if (navigator.vibrate) navigator.vibrate(ms); } catch (e) { /* ignorar */ }
  },
};
