// =====================================================================
//  DEFENSE ZONE — controlador principal
// =====================================================================
import { CARDS, MATCH } from './config.js';
import { World, SIDE_PLAYER, SIDE_AI } from './sim/world.js';
import { EnemyAI } from './sim/ai.js';
import { Renderer } from './render/renderer.js';
import { Effects } from './render/effects.js';
import { AudioEngine } from './audio/audio.js';
import { Hud } from './ui/hud.js';
import { Screens } from './ui/screens.js';
import { loadSettings, saveSettings } from './storage.js';
import { platform } from './platform.js';

const $ = (id) => document.getElementById(id);
const STEP = 1 / 60;

// Polyfill de roundRect para WebViews anteriores a Chrome 99
if (typeof CanvasRenderingContext2D !== 'undefined' && !CanvasRenderingContext2D.prototype.roundRect) {
  CanvasRenderingContext2D.prototype.roundRect = function (x, y, w, h, r) {
    r = Math.min(typeof r === 'number' ? r : (r && r[0]) || 0, w / 2, h / 2);
    this.moveTo(x + r, y); this.arcTo(x + w, y, x + w, y + h, r); this.arcTo(x + w, y + h, x, y + h, r);
    this.arcTo(x, y + h, x, y, r); this.arcTo(x, y, x + w, y, r); this.closePath();
    return this;
  };
}

class Game {
  constructor() {
    this.settings = loadSettings();
    this.canvas = $('game');
    this.renderer = new Renderer(this.canvas, this.settings);
    this.fx = new Effects(this.settings.quality);
    this.audio = new AudioEngine(this.settings);
    this.hud = new Hud();
    this.screens = new Screens({ settings: this.settings, onSettingsChange: (k) => this.onSettingsChange(k) });
    this.state = 'menu';
    this.world = null;
    this.ai = null;
    this.preview = null;   // { cardId, x, y, ok }
    this.drag = null;
    this.countdown = 0;
    this.endDelay = 0;
    this.last = performance.now();
    this.acc = 0;
    this.fx.onThunder = () => this.audio.sfx('thunder', { volume: 0.7 });
    this._bind();
    this.resize();
    requestAnimationFrame((t) => this.loop(t));
    window.__dz = this; // acceso para depuración / pruebas automáticas
  }

  // ------------------------------------------------------------------
  //  Eventos de UI
  // ------------------------------------------------------------------
  _bind() {
    const unlock = () => { if (this.audio.ensure()) { if (this.state === 'menu') this.audio.playMusic('menu'); } };
    window.addEventListener('pointerdown', unlock, { passive: true });
    window.addEventListener('keydown', unlock);
    window.addEventListener('resize', () => this.resize());
    window.addEventListener('orientationchange', () => setTimeout(() => this.resize(), 200));
    document.addEventListener('visibilitychange', () => { if (document.hidden && this.state === 'playing') this.pauseGame(); });
    window.addEventListener('androidback', () => this.onBack());
    window.addEventListener('androidpause', () => { if (this.state === 'playing') this.pauseGame(); });
    window.addEventListener('keydown', (e) => { if (e.key === 'Escape') this.onBack(); });

    const click = (id, fn) => $(id).addEventListener('click', () => { this.audio.sfx('click'); fn(); });
    click('btn-start', () => this.startGame());
    click('btn-settings', () => this.screens.showSettings('menu'));
    click('btn-exit', () => platform.exitApp());
    click('btn-settings-back', () => this.closeSettings());
    click('btn-pause', () => this.pauseGame());
    click('btn-resume', () => this.resumeGame());
    click('btn-pause-settings', () => this.screens.showSettings('pause'));
    click('btn-quit', () => this.toMenu());
    click('btn-again', () => this.startGame());
    click('btn-menu', () => this.toMenu());

    // Entrada de cartas: tocar para seleccionar / arrastrar para colocar
    this.hud.onCardPointerDown = (cardId, ev, el) => {
      if (this.state !== 'playing') return;
      ev.preventDefault();
      el.setPointerCapture(ev.pointerId);
      this.drag = { cardId, el, startX: ev.clientX, startY: ev.clientY, moved: false, id: ev.pointerId };
      const move = (e) => {
        if (!this.drag || e.pointerId !== this.drag.id) return;
        if (!this.drag.moved && Math.hypot(e.clientX - this.drag.startX, e.clientY - this.drag.startY) > 12) { this.drag.moved = true; el.classList.add('dragging'); this.hud.select(cardId); }
        if (this.drag.moved) this.setPreview(cardId, e.clientX, e.clientY);
      };
      const up = (e) => {
        if (!this.drag || e.pointerId !== this.drag.id) return;
        el.removeEventListener('pointermove', move); el.removeEventListener('pointerup', up); el.removeEventListener('pointercancel', up);
        el.classList.remove('dragging');
        const d = this.drag; this.drag = null;
        if (d.moved) {
          if (e.clientY < window.innerHeight - this.hud.cardbarHeight) this.tryPlace(cardId, e.clientX, e.clientY);
          else { this.hud.select(null); }
          this.preview = null;
        } else {
          // Toque simple: alterna la selección
          if (this.hud.selected === cardId) this.hud.select(null);
          else { this.hud.select(cardId); this.audio.sfx('select'); }
          this.preview = null;
        }
      };
      el.addEventListener('pointermove', move); el.addEventListener('pointerup', up); el.addEventListener('pointercancel', up);
    };

    this.canvas.addEventListener('pointerdown', (e) => {
      if (this.state !== 'playing' || !this.hud.selected) return;
      this.tryPlace(this.hud.selected, e.clientX, e.clientY);
    });
    this.canvas.addEventListener('pointermove', (e) => {
      if (this.state !== 'playing' || !this.hud.selected || this.drag) { if (!this.drag) this.preview = null; return; }
      this.setPreview(this.hud.selected, e.clientX, e.clientY);
    });
    this.canvas.addEventListener('pointerleave', () => { if (!this.drag) this.preview = null; });
  }

  onBack() {
    if (this.screens.settingsOpen) return this.closeSettings();
    if (this.state === 'playing') return this.pauseGame();
    if (this.state === 'paused') return this.resumeGame();
    if (this.state === 'ended') return this.toMenu();
    if (this.state === 'menu') platform.exitApp();
  }

  onSettingsChange(kind) {
    saveSettings(this.settings);
    if (kind === 'quality') { this.renderer.setQuality(this.settings.quality); this.fx.setQuality(this.settings.quality); this.resize(); }
    if (kind === 'music' || kind === 'sfx') this.audio.applyVolumes();
    if (kind === 'sfxTest') this.audio.sfx('cannon', { volume: 0.6 });
  }

  closeSettings() {
    this.screens.hideSettings();
  }

  resize() {
    this.renderer.setInsets(0, 0);
    this.renderer.resize();
    if (this.state !== 'menu') {
      // La barra superior se superpone al margen decorativo del mapa; sólo la
      // barra de cartas resta espacio real.
      this.renderer.setInsets(0, this.hud.cardbarHeight);
    }
  }

  // ------------------------------------------------------------------
  //  Flujo de partida
  // ------------------------------------------------------------------
  startGame() {
    this.screens.hideMenu(); this.screens.hideEnd(); this.screens.hidePause();
    this.world = new World({ seed: (Date.now() % 100000) | 0 });
    this.ai = new EnemyAI(this.world, { side: SIDE_AI });
    this.fx = new Effects(this.settings.quality);
    this.fx.onThunder = () => this.audio.sfx('thunder', { volume: 0.7 });
    this.hud.handKey = ''; this.hud.select(null);
    this.hud.show();
    this.hud.update(this.world);
    this.state = 'countdown';
    this.countdown = 3.999;
    this.lastCount = 4;
    this.preview = null;
    this.audio.ensure();
    this.audio.playMusic('battle');
    this.audio.setTempoMult(1);
    this.resize();
  }

  pauseGame() {
    if (this.state !== 'playing') return;
    this.state = 'paused';
    this.screens.showPause();
    this.audio.setMuted(false);
  }
  resumeGame() {
    if (this.state !== 'paused') return;
    this.screens.hidePause(); this.screens.hideSettings();
    this.state = 'playing';
    this.last = performance.now();
  }
  toMenu() {
    this.state = 'menu';
    this.world = null; this.preview = null;
    this.hud.hide(); this.screens.hidePause(); this.screens.hideEnd(); this.screens.hideSettings();
    this.screens.showMenu();
    this.audio.stopRain();
    this.audio.playMusic('menu');
    this.resize();
  }

  finish(result) {
    this.state = 'ended';
    this.preview = null; this.hud.select(null);
    this.audio.stopMusic(); this.audio.stopRain();
    const win = result.winner === SIDE_PLAYER, draw = result.winner === null;
    const reasons = {
      hp: win ? 'El héroe enemigo ha caído en combate.' : 'Tu héroe ha caído en combate.',
      goal: win ? 'Tu héroe ha alcanzado la base enemiga.' : 'El héroe enemigo ha llegado a tu base.',
      time: draw ? 'Se agotó el tiempo con ambos héroes igualados.' : win ? 'Se agotó el tiempo: tu héroe estaba más cerca de su objetivo.' : 'Se agotó el tiempo: el héroe enemigo estaba más cerca de su objetivo.',
    };
    if (draw) { this.fx.shake = 4; }
    else if (win) { this.audio.sfx('victory'); for (let i = 0; i < 6; i++) setTimeout(() => this.fx && this.fx.burst(300 + Math.random() * 1000, 150 + Math.random() * 400, 40, ['#ffcc4d', '#7cd36c', '#3fa7ff', '#fff'][i % 4], 300, 1.4), i * 350); }
    else { this.audio.sfx('defeat'); this.fx.shake = 20; }
    setTimeout(() => this.screens.showEnd(draw ? 'draw' : win ? 'victory' : 'defeat', reasons[result.reason]), 900);
  }

  // ------------------------------------------------------------------
  //  Colocación de cartas
  // ------------------------------------------------------------------
  setPreview(cardId, sx, sy) {
    const p = this.renderer.screenToWorld(sx, sy);
    this.preview = { cardId, x: p.x, y: p.y, ok: !this.world.canPlace(SIDE_PLAYER, cardId, p.x, p.y) };
  }

  tryPlace(cardId, sx, sy) {
    const p = this.renderer.screenToWorld(sx, sy);
    const err = this.world.place(SIDE_PLAYER, cardId, p.x, p.y);
    if (err) { this.hud.toast(err); this.audio.sfx('error'); platform.vibrate(30); return false; }
    this.hud.select(null); this.preview = null;
    platform.vibrate(15);
    return true;
  }

  // ------------------------------------------------------------------
  //  Bucle principal
  // ------------------------------------------------------------------
  loop(now) {
    requestAnimationFrame((t) => this.loop(t));
    let dt = Math.min(0.1, (now - this.last) / 1000);
    this.last = now;

    if (this.state === 'menu') { this.renderer.drawMenu(dt); return; }
    if (!this.world) return;

    if (this.state === 'countdown') {
      this.countdown -= dt;
      const n = Math.ceil(this.countdown);
      if (n !== this.lastCount) { this.lastCount = n; if (n > 0) { this.hud.banner(String(n), '#ffcc4d'); this.audio.sfx('countdown'); } else { this.hud.banner('¡A LUCHAR!', '#7cd36c'); this.audio.sfx('go'); this.state = 'playing'; } }
    }

    if (this.state === 'playing') {
      this.acc += dt;
      let steps = 0;
      while (this.acc >= STEP && steps < 4) {
        this.ai.update(STEP);
        this.world.update(STEP);
        this.acc -= STEP; steps++;
      }
      this.handleEvents(this.world.drainEvents());
    }
    this.fx.update(dt, this.world);
    if (this.preview) this.preview.ok = !this.world.canPlace(SIDE_PLAYER, this.preview.cardId, this.preview.x, this.preview.y);
    this.renderer.drawGame(this.world, this.fx, this.state === 'playing' ? this.preview : null);
    this.hud.update(this.world);
  }

  handleEvents(events) {
    this.fx.handleEvents(events, this.world);
    for (const e of events) {
      switch (e.type) {
        case 'shot': this.audio.sfx(e.kind === 'shell' ? 'cannon' : e.kind === 'arrow' ? 'arrow' : e.owner === 'turret' ? 'turret' : 'shot', { volume: e.owner === 'hero' ? 0.5 : 0.7 }); break;
        case 'hit': if (e.kind === 'shell') this.audio.sfx('explosion', { volume: 0.5 }); else this.audio.sfx('hit', { volume: 0.4 }); break;
        case 'melee': this.audio.sfx('melee', { volume: 0.6 }); break;
        case 'place': this.audio.sfx(e.cardId === 'log' ? 'log' : e.cardId === 'wire' ? 'wire' : 'place'); if (e.side === SIDE_AI) this.hud.toast(`El enemigo juega ${CARDS[e.cardId].name}`, 1100); break;
        case 'destroy': this.audio.sfx(e.kind === 'hero' ? 'heroDeath' : 'explosion', { volume: e.kind === 'hero' ? 1 : 0.7 }); break;
        case 'logHit': this.audio.sfx('melee'); break;
        case 'rainStart': this.hud.banner('¡Lluvia!', '#8fc6ff'); this.audio.startRain(); this.audio.sfx('rainStart'); break;
        case 'rainEnd': this.audio.stopRain(); break;
        case 'frenzy': this.hud.banner('¡VELOCIDAD x2!', '#ff5a4e'); this.audio.sfx('frenzy'); this.audio.setTempoMult(1.3); break;
        case 'end': this.finish(e); break;
      }
    }
  }
}

new Game();
