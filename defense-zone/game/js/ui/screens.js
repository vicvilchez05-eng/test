// =====================================================================
//  Pantallas: menú principal, ajustes, pausa y fin de partida.
// =====================================================================
const $ = (id) => document.getElementById(id);

export class Screens {
  constructor({ settings, onSettingsChange }) {
    this.settings = settings;
    this.onSettingsChange = onSettingsChange;
    this.menu = $('screen-menu');
    this.settingsScreen = $('screen-settings');
    this.pause = $('screen-pause');
    this.end = $('screen-end');
    this.settingsReturn = null; // a qué pantalla volver al cerrar ajustes
    this._bindSettings();
  }

  showMenu() { this.menu.classList.remove('hidden'); }
  hideMenu() { this.menu.classList.add('hidden'); }
  showPause() { this.pause.classList.remove('hidden'); }
  hidePause() { this.pause.classList.add('hidden'); }

  showSettings(returnTo) {
    this.settingsReturn = returnTo;
    this._syncSettingsUI();
    this.settingsScreen.classList.remove('hidden');
  }
  hideSettings() { this.settingsScreen.classList.add('hidden'); }
  get settingsOpen() { return !this.settingsScreen.classList.contains('hidden'); }

  showEnd(kind, reason) {
    this.end.className = `screen overlay ${kind}`;
    $('end-title').textContent = kind === 'victory' ? '¡VICTORIA!' : kind === 'defeat' ? 'DERROTA' : 'EMPATE';
    $('end-reason').textContent = reason;
    this.end.classList.remove('hidden');
  }
  hideEnd() { this.end.classList.add('hidden'); }

  _bindSettings() {
    const seg = $('quality-seg');
    seg.querySelectorAll('button').forEach(b => b.addEventListener('click', () => {
      this.settings.quality = b.dataset.q; this._syncSettingsUI(); this.onSettingsChange('quality');
    }));
    const music = $('music-vol'), sfx = $('sfx-vol');
    music.addEventListener('input', () => { this.settings.musicVolume = music.value / 100; $('music-val').textContent = `${music.value}%`; this.onSettingsChange('music'); });
    sfx.addEventListener('input', () => { this.settings.sfxVolume = sfx.value / 100; $('sfx-val').textContent = `${sfx.value}%`; this.onSettingsChange('sfx'); });
    sfx.addEventListener('change', () => this.onSettingsChange('sfxTest'));
  }

  _syncSettingsUI() {
    $('quality-seg').querySelectorAll('button').forEach(b => b.classList.toggle('active', b.dataset.q === this.settings.quality));
    const m = Math.round(this.settings.musicVolume * 100), s = Math.round(this.settings.sfxVolume * 100);
    $('music-vol').value = m; $('music-val').textContent = `${m}%`;
    $('sfx-vol').value = s; $('sfx-val').textContent = `${s}%`;
  }
}
