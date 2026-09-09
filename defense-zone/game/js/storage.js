// Persistencia de ajustes en localStorage (con valores por defecto).
import { DEFAULT_SETTINGS } from './config.js';
const KEY = 'defensezone.settings.v1';

export function loadSettings() {
  try {
    const raw = localStorage.getItem(KEY);
    if (raw) return { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
  } catch (e) { /* almacenamiento no disponible */ }
  return { ...DEFAULT_SETTINGS };
}

export function saveSettings(settings) {
  try { localStorage.setItem(KEY, JSON.stringify(settings)); } catch (e) { /* ignorar */ }
}
