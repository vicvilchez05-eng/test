// =====================================================================
//  Renderizador: dibuja el mundo (cámara cenital) en el canvas.
// =====================================================================
import { WORLD, HERO, CARDS, QUALITY } from '../config.js';
import { SIDE_PLAYER } from '../sim/world.js';
import { buildBackground } from './background.js';

const TEAM = {
  0: { main: '#3fa7ff', dark: '#1d5f9c', light: '#bfe3ff', name: 'ALIADO' },
  1: { main: '#ff4b3e', dark: '#8f1f18', light: '#ffc9c4', name: 'ENEMIGO' },
};

export class Renderer {
  constructor(canvas, settings) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.settings = settings;
    this.bottomInset = 0; // altura de la barra de cartas (px CSS)
    this.topInset = 0;
    this.view = { scale: 1, ox: 0, oy: 0 };
    this.bg = buildBackground(settings.quality);
    this.quality = settings.quality;
    this.w = 0; this.h = 0; this.dpr = 1;
    this.menuT = 0;
    this.resize();
  }

  setQuality(q) {
    if (q === this.quality) return;
    this.quality = q; this.bg = buildBackground(q); this.resize();
  }

  resize() {
    const q = QUALITY[this.quality] || QUALITY.medium;
    this.dpr = Math.min(window.devicePixelRatio || 1, q.dprCap);
    this.w = window.innerWidth; this.h = window.innerHeight;
    this.canvas.width = Math.round(this.w * this.dpr);
    this.canvas.height = Math.round(this.h * this.dpr);
    this.computeView();
  }

  computeView() {
    // Llena el ancho de la pantalla. Verticalmente garantiza que la pista
    // (más un margen) siempre sea visible; la decoración sobrante se recorta.
    const availW = this.w, availH = this.h - this.bottomInset - this.topInset;
    const laneCenter = (WORLD.laneTop + WORLD.laneBottom) / 2;
    const minVisibleH = (WORLD.laneBottom - WORLD.laneTop) + 60;
    const scale = Math.min(availW / WORLD.width, availH / minVisibleH);
    const visibleH = availH / scale;
    let oy;
    if (visibleH >= WORLD.height) oy = this.topInset + (availH - WORLD.height * scale) / 2;
    else {
      // centra la pista y limita para no salirse del mapa
      let top = laneCenter - visibleH / 2;
      top = Math.max(0, Math.min(WORLD.height - visibleH, top));
      oy = this.topInset - top * scale;
    }
    this.view = { scale, ox: (availW - WORLD.width * scale) / 2, oy };
  }

  setInsets(top, bottom) { this.topInset = top; this.bottomInset = bottom; this.computeView(); }

  screenToWorld(sx, sy) { const v = this.view; return { x: (sx - v.ox) / v.scale, y: (sy - v.oy) / v.scale }; }
  worldToScreen(x, y) { const v = this.view; return { x: v.ox + x * v.scale, y: v.oy + y * v.scale }; }

  // ------------------------------------------------------------------
  //  Menú principal: escena animada de fondo
  // ------------------------------------------------------------------
  drawMenu(dt) {
    this.menuT += dt;
    const c = this.ctx, t = this.menuT;
    c.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
    const w = this.w, h = this.h;
    // Mapa al fondo, ligeramente escalado y moviéndose
    const scale = Math.max(w / WORLD.width, h / WORLD.height) * 1.15;
    const ox = (w - WORLD.width * scale) / 2 + Math.sin(t * 0.1) * 40;
    const oy = (h - WORLD.height * scale) / 2 + Math.cos(t * 0.13) * 20;
    c.save(); c.translate(ox, oy); c.scale(scale, scale); c.drawImage(this.bg, 0, 0); c.restore();
    c.fillStyle = 'rgba(5,8,5,0.55)'; c.fillRect(0, 0, w, h);
    // Focos de búsqueda (searchlights)
    for (let i = 0; i < 2; i++) {
      const a = Math.sin(t * 0.35 + i * 2.1) * 0.5 + (i ? -0.2 : 0.2);
      const bx = i ? w * 0.92 : w * 0.08, by = h * 1.05;
      c.save(); c.translate(bx, by); c.rotate(a);
      const g = c.createLinearGradient(0, 0, 0, -h * 1.4);
      g.addColorStop(0, 'rgba(255,240,200,0.28)'); g.addColorStop(1, 'rgba(255,240,200,0)');
      c.fillStyle = g; c.beginPath(); c.moveTo(0, 0); c.lineTo(-h * 0.25, -h * 1.4); c.lineTo(h * 0.25, -h * 1.4); c.closePath(); c.fill();
      c.restore();
    }
    // Humo a la deriva
    for (let i = 0; i < 14; i++) {
      const x = ((i * 173 + t * 18 * (1 + i % 3)) % (w + 300)) - 150;
      const y = h * (0.2 + (i % 5) * 0.15) + Math.sin(t * 0.5 + i) * 20;
      c.fillStyle = `rgba(60,58,52,${0.12 + (i % 3) * 0.05})`;
      c.beginPath(); c.arc(x, y, 60 + (i % 4) * 30, 0, Math.PI * 2); c.fill();
    }
    // Chispas
    c.fillStyle = 'rgba(255,170,60,0.8)';
    for (let i = 0; i < 25; i++) {
      const p = (t * 0.15 + i * 0.137) % 1;
      const x = w * ((i * 0.618) % 1) + Math.sin(t + i) * 30, y = h * (1 - p);
      c.globalAlpha = 1 - p; c.fillRect(x, y, 3, 3);
    }
    c.globalAlpha = 1;
    // Fogonazo de artillería ocasional
    const flash = Math.max(0, Math.sin(t * 1.3) - 0.97) * 30;
    if (flash > 0) { c.fillStyle = `rgba(255,220,150,${Math.min(0.3, flash)})`; c.fillRect(0, 0, w, h); }
  }

  // ------------------------------------------------------------------
  //  Partida
  // ------------------------------------------------------------------
  drawGame(world, fx, preview) {
    const c = this.ctx, v = this.view;
    c.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
    c.fillStyle = '#0b0f0a'; c.fillRect(0, 0, this.w, this.h);
    let sx = 0, sy = 0;
    if (fx.shake > 0) { sx = (Math.random() - 0.5) * fx.shake; sy = (Math.random() - 0.5) * fx.shake; }
    c.save();
    c.beginPath(); c.rect(0, this.topInset, this.w, this.h - this.topInset - this.bottomInset); c.clip();
    c.translate(v.ox + sx, v.oy + sy); c.scale(v.scale, v.scale);
    c.drawImage(this.bg, 0, 0);
    if (fx.rainAmount > 0.02) this.drawPuddles(c, fx);
    // Orden de dibujo: zonas, sombras, entidades por Y, proyectiles, efectos
    for (const w of world.wires) this.drawWire(c, w, world);
    if (preview) this.drawPreview(c, preview, world);
    const ents = [...world.structures, ...world.logs, ...world.heroes.filter(h => h.alive)];
    ents.sort((a, b) => a.y - b.y);
    for (const e of ents) this.drawShadow(c, e);
    for (const e of ents) {
      if (e.type === 'hero') this.drawHero(c, e, world);
      else if (e.type === 'structure') this.drawStructure(c, e, world);
      else if (e.type === 'log') this.drawLog(c, e);
    }
    for (const p of world.projectiles) this.drawProjectile(c, p);
    fx.drawWorld(c);
    for (const h of world.heroes) if (h.alive) this.drawHeroBar(c, h);
    c.restore();
    fx.drawScreen(c, this.w, this.h, world);
  }

  drawPuddles(c, fx) {
    c.globalAlpha = 0.35 * fx.rainAmount;
    c.fillStyle = '#6f86a8';
    const seeds = [[300, 260], [520, 560], [800, 300], [1050, 520], [1300, 280], [700, 480], [950, 400]];
    for (const [x, y] of seeds) { c.beginPath(); c.ellipse(x, y, 60, 22, 0, 0, Math.PI * 2); c.fill(); }
    c.globalAlpha = 1;
  }

  drawShadow(c, e) {
    const q = QUALITY[this.quality];
    if (!q.shadows) return;
    c.fillStyle = 'rgba(0,0,0,0.35)';
    c.beginPath(); c.ellipse(e.x + 6, e.y + e.radius * 0.55, e.radius * 1.05, e.radius * 0.5, 0, 0, Math.PI * 2); c.fill();
  }

  drawHero(c, h, world) {
    const T = TEAM[h.side], r = h.radius;
    const hit = world.time - h.lastHit < 0.12;
    c.save(); c.translate(h.x, h.y);
    // Piernas (animación de marcha)
    const step = h.stopped ? 0 : Math.sin(h.walkPhase * 4) * r * 0.35;
    c.fillStyle = '#2f3524';
    c.beginPath(); c.ellipse(step * h.dir, r * 0.35, r * 0.3, r * 0.42, 0, 0, Math.PI * 2); c.fill();
    c.beginPath(); c.ellipse(-step * h.dir, r * 0.35, r * 0.3, r * 0.42, 0, 0, Math.PI * 2); c.fill();
    // Cuerpo (hombros) con color de equipo
    const bg = c.createRadialGradient(-r * 0.3, -r * 0.3, r * 0.1, 0, 0, r);
    bg.addColorStop(0, T.light); bg.addColorStop(0.35, T.main); bg.addColorStop(1, T.dark);
    c.fillStyle = hit ? '#ffffff' : bg; c.strokeStyle = '#111'; c.lineWidth = 3;
    c.beginPath(); c.arc(0, 0, r, 0, Math.PI * 2); c.fill(); c.stroke();
    // Chaleco / correajes
    c.strokeStyle = 'rgba(0,0,0,0.35)'; c.lineWidth = r * 0.16;
    c.beginPath(); c.moveTo(-r * 0.5, -r * 0.8); c.lineTo(r * 0.5, r * 0.8); c.moveTo(r * 0.5, -r * 0.8); c.lineTo(-r * 0.5, r * 0.8); c.stroke();
    // Rifle apuntando hacia donde mira
    const ang = h.targetId ? this._aimAngle(h, world) : (h.facing > 0 ? 0 : Math.PI);
    c.save(); c.rotate(ang);
    c.fillStyle = '#1b1b1b'; c.fillRect(r * 0.1, -r * 0.12, r * 1.5, r * 0.24);
    c.fillStyle = '#6b4a2b'; c.fillRect(r * 0.2, -r * 0.2, r * 0.6, r * 0.4);
    // Manos
    c.fillStyle = '#d8a97a'; c.beginPath(); c.arc(r * 0.55, r * 0.35, r * 0.2, 0, Math.PI * 2); c.fill();
    c.beginPath(); c.arc(r * 1.0, -r * 0.1, r * 0.18, 0, Math.PI * 2); c.fill();
    if (world.time - h.lastShot < 0.06 && !h.stopped) { c.fillStyle = '#ffe680'; c.beginPath(); c.arc(r * 1.7, 0, r * 0.28, 0, Math.PI * 2); c.fill(); }
    c.restore();
    // Casco
    const hg = c.createRadialGradient(-r * 0.15, -r * 0.15, r * 0.05, 0, 0, r * 0.55);
    hg.addColorStop(0, '#7f8a5c'); hg.addColorStop(1, '#3d4630');
    c.fillStyle = hit ? '#fff' : hg; c.strokeStyle = '#1d2216'; c.lineWidth = 2.5;
    c.beginPath(); c.arc(0, 0, r * 0.55, 0, Math.PI * 2); c.fill(); c.stroke();
    c.strokeStyle = T.main; c.lineWidth = 3; c.beginPath(); c.arc(0, 0, r * 0.42, 0, Math.PI * 2); c.stroke();
    // Estrella del héroe
    c.fillStyle = '#fff'; c.font = `bold ${r * 0.5 | 0}px Arial`; c.textAlign = 'center'; c.textBaseline = 'middle'; c.fillText('★', 0, 1);
    // Ralentizado
    if (h.slow > 0) { c.strokeStyle = 'rgba(255,255,255,0.7)'; c.lineWidth = 2; c.setLineDash([4, 6]); c.beginPath(); c.arc(0, 0, r + 6, 0, Math.PI * 2); c.stroke(); c.setLineDash([]); }
    c.restore();
  }

  _aimAngle(h, world) {
    const t = world.structures.find(s => s.id === h.targetId) || world.heroes.find(x => x.id === h.targetId);
    return t ? Math.atan2(t.y - h.y, t.x - h.x) : 0;
  }

  drawHeroBar(c, h) {
    const T = TEAM[h.side], r = h.radius;
    const w = r * 3.6, hh = 22, x = h.x - w / 2, y = h.y - r - 44;
    c.fillStyle = 'rgba(0,0,0,0.75)'; c.beginPath(); c.roundRect(x - 3, y - 3, w + 6, hh + 6, 6); c.fill();
    c.fillStyle = '#2a2a2a'; c.fillRect(x, y, w, hh);
    const pct = h.hp / h.maxHp;
    const g = c.createLinearGradient(0, y, 0, y + hh);
    g.addColorStop(0, T.light); g.addColorStop(1, T.main);
    c.fillStyle = g; c.fillRect(x, y, w * pct, hh);
    c.strokeStyle = 'rgba(255,255,255,0.35)'; c.lineWidth = 1;
    for (let i = 1; i < 10; i++) { c.beginPath(); c.moveTo(x + w * i / 10, y); c.lineTo(x + w * i / 10, y + hh); c.stroke(); }
    c.fillStyle = '#fff'; c.font = 'bold 16px Arial'; c.textAlign = 'center'; c.textBaseline = 'middle';
    c.strokeStyle = '#000'; c.lineWidth = 3; c.strokeText(`${Math.ceil(h.hp)}`, h.x, y + hh / 2); c.fillText(`${Math.ceil(h.hp)}`, h.x, y + hh / 2);
    c.font = 'bold 14px Arial'; c.fillStyle = T.main; c.strokeText(h.side === SIDE_PLAYER ? 'TU HÉROE' : 'HÉROE ENEMIGO', h.x, y - 10); c.fillText(h.side === SIDE_PLAYER ? 'TU HÉROE' : 'HÉROE ENEMIGO', h.x, y - 10);
  }

  drawStructure(c, s, world) {
    const T = TEAM[s.side], r = s.radius;
    const hit = world.time - s.lastHit < 0.1;
    const build = Math.min(1, s.age / 0.4); // aparece "creciendo"
    c.save(); c.translate(s.x, s.y); c.scale(build, build);
    // Anillo de equipo
    c.strokeStyle = T.main; c.lineWidth = 2; c.globalAlpha = 0.6;
    c.beginPath(); c.arc(0, 0, r + 4, 0, Math.PI * 2); c.stroke(); c.globalAlpha = 1;
    switch (s.kind) {
      case 'wall': {
        c.fillStyle = hit ? '#fff' : '#8a8d84'; c.strokeStyle = '#262824'; c.lineWidth = 3;
        c.beginPath(); c.roundRect(-r * 0.9, -r * 1.5, r * 1.8, r * 3, 5); c.fill(); c.stroke();
        c.strokeStyle = 'rgba(0,0,0,0.4)'; c.lineWidth = 2;
        for (let i = -2; i <= 2; i++) { c.beginPath(); c.moveTo(-r * 0.9, i * r * 0.6); c.lineTo(r * 0.9, i * r * 0.6); c.stroke(); if (i < 2) { c.beginPath(); c.moveTo(i % 2 ? -r * 0.3 : r * 0.3, i * r * 0.6); c.lineTo(i % 2 ? -r * 0.3 : r * 0.3, (i + 1) * r * 0.6); c.stroke(); } }
        c.fillStyle = '#a89468'; c.strokeStyle = '#4d4128'; c.lineWidth = 1.5;
        for (let i = -1; i <= 1; i++) { c.beginPath(); c.ellipse(r * 0.55 * (s.side === 0 ? -1 : 1), i * r * 0.9, r * 0.45, r * 0.32, 0, 0, Math.PI * 2); c.fill(); c.stroke(); }
        if (s.hp < s.maxHp * 0.5) { c.strokeStyle = '#1a1a1a'; c.lineWidth = 2; c.beginPath(); c.moveTo(-r * 0.5, -r); c.lineTo(0, -r * 0.2); c.lineTo(-r * 0.3, r * 0.6); c.moveTo(r * 0.4, -r * 0.6); c.lineTo(r * 0.1, r * 0.3); c.stroke(); }
        break;
      }
      case 'turret': {
        c.fillStyle = '#3c3f3a'; c.strokeStyle = '#1b1c19'; c.lineWidth = 3;
        c.beginPath(); c.arc(0, 0, r, 0, Math.PI * 2); c.fill(); c.stroke();
        c.fillStyle = hit ? '#fff' : '#5c6058'; c.beginPath(); c.arc(0, 0, r * 0.65, 0, Math.PI * 2); c.fill();
        c.save(); c.rotate(s.angle);
        c.fillStyle = '#1b1c19'; c.fillRect(0, -r * 0.16, r * 1.5, r * 0.32); c.fillRect(0, -r * 0.4, r * 0.5, r * 0.8);
        if (world.time - s.lastShot < 0.07) { c.fillStyle = '#ffe680'; c.beginPath(); c.arc(r * 1.6, 0, r * 0.3, 0, Math.PI * 2); c.fill(); }
        c.restore();
        c.fillStyle = T.main; c.beginPath(); c.arc(0, 0, r * 0.22, 0, Math.PI * 2); c.fill();
        break;
      }
      case 'archer': {
        c.fillStyle = hit ? '#fff' : '#6d4a2c'; c.strokeStyle = '#2b1c10'; c.lineWidth = 3;
        c.beginPath(); c.roundRect(-r, -r, r * 2, r * 2, 5); c.fill(); c.stroke();
        c.strokeStyle = 'rgba(0,0,0,0.35)'; c.lineWidth = 2;
        for (let i = -2; i <= 2; i++) { c.beginPath(); c.moveTo(-r, i * r * 0.4); c.lineTo(r, i * r * 0.4); c.stroke(); }
        c.fillStyle = '#8b6a3e'; c.beginPath(); c.roundRect(-r * 0.7, -r * 0.7, r * 1.4, r * 1.4, 4); c.fill();
        // Arquero
        c.save(); c.rotate(s.angle);
        c.strokeStyle = '#e8d8a0'; c.lineWidth = 2.5; c.beginPath(); c.arc(r * 0.35, 0, r * 0.45, -Math.PI / 2, Math.PI / 2); c.stroke();
        c.beginPath(); c.moveTo(r * 0.35, -r * 0.45); c.lineTo(r * 0.35, r * 0.45); c.stroke();
        c.restore();
        c.fillStyle = T.main; c.beginPath(); c.arc(0, 0, r * 0.28, 0, Math.PI * 2); c.fill();
        c.fillStyle = '#5d6b3f'; c.beginPath(); c.arc(0, 0, r * 0.2, 0, Math.PI * 2); c.fill();
        break;
      }
      case 'cannon': {
        c.fillStyle = '#4a3b2a'; c.strokeStyle = '#1f1710'; c.lineWidth = 3;
        for (const wx of [-r * 0.7, r * 0.7]) { c.beginPath(); c.ellipse(wx, r * 0.3, r * 0.3, r * 0.7, 0, 0, Math.PI * 2); c.fill(); c.stroke(); }
        c.fillStyle = hit ? '#fff' : '#3a3d38'; c.beginPath(); c.arc(0, 0, r * 0.85, 0, Math.PI * 2); c.fill(); c.stroke();
        c.save(); c.rotate(s.angle);
        c.fillStyle = '#1f2220'; c.fillRect(-r * 0.2, -r * 0.3, r * 1.7, r * 0.6);
        c.fillStyle = '#2f3330'; c.beginPath(); c.arc(0, 0, r * 0.5, 0, Math.PI * 2); c.fill();
        if (world.time - s.lastShot < 0.12) { c.fillStyle = '#ffb347'; c.beginPath(); c.arc(r * 1.8, 0, r * 0.5, 0, Math.PI * 2); c.fill(); }
        c.restore();
        c.fillStyle = T.main; c.beginPath(); c.arc(0, 0, r * 0.18, 0, Math.PI * 2); c.fill();
        break;
      }
    }
    c.restore();
    // Barra de vida de la estructura
    if (s.hp < s.maxHp) {
      const w = r * 2.2, x = s.x - w / 2, y = s.y - r - 16;
      c.fillStyle = 'rgba(0,0,0,0.7)'; c.fillRect(x - 1, y - 1, w + 2, 8);
      c.fillStyle = s.hp / s.maxHp > 0.4 ? '#7cd36c' : '#ffb347'; c.fillRect(x, y, w * s.hp / s.maxHp, 6);
    }
  }

  drawLog(c, l) {
    const r = l.radius;
    c.save(); c.translate(l.x, l.y);
    c.fillStyle = '#6b4626'; c.strokeStyle = '#2e1d0e'; c.lineWidth = 3;
    c.beginPath(); c.roundRect(-r * 0.7, -r * 1.6, r * 1.4, r * 3.2, r * 0.5); c.fill(); c.stroke();
    // Vetas que se desplazan según el giro
    c.strokeStyle = 'rgba(30,18,8,0.55)'; c.lineWidth = 3;
    const off = (l.roll * r * 0.5) % (r * 0.7);
    for (let x = -r * 0.7 + off - r * 0.7; x < r * 0.7; x += r * 0.35) { c.beginPath(); c.moveTo(x, -r * 1.5); c.lineTo(x + 3, r * 1.5); c.stroke(); }
    // Extremos (anillos)
    c.fillStyle = '#c9a06a'; c.beginPath(); c.ellipse(0, -r * 1.55, r * 0.68, r * 0.25, 0, 0, Math.PI * 2); c.fill(); c.stroke();
    c.beginPath(); c.ellipse(0, r * 1.55, r * 0.68, r * 0.25, 0, 0, Math.PI * 2); c.fill(); c.stroke();
    c.restore();
  }

  drawWire(c, w, world) {
    const T = TEAM[w.side];
    const fade = Math.min(1, w.timeLeft / 3);
    c.save(); c.translate(w.x, w.y); c.globalAlpha = 0.85 * fade;
    c.fillStyle = w.side === 0 ? 'rgba(63,167,255,0.12)' : 'rgba(255,75,62,0.12)';
    c.beginPath(); c.arc(0, 0, w.radius, 0, Math.PI * 2); c.fill();
    c.strokeStyle = '#9a9a92'; c.lineWidth = 1.6;
    for (let ring = 0.35; ring <= 1; ring += 0.32) {
      const R = w.radius * ring;
      c.beginPath();
      for (let a = 0; a <= Math.PI * 2 + 0.01; a += 0.12) { const rr = R + Math.sin(a * 18 + world.time * 2) * 4; c.lineTo(Math.cos(a) * rr, Math.sin(a) * rr); }
      c.stroke();
    }
    c.strokeStyle = T.main; c.setLineDash([6, 8]); c.lineWidth = 2; c.beginPath(); c.arc(0, 0, w.radius, 0, Math.PI * 2); c.stroke();
    c.restore();
  }

  drawProjectile(c, p) {
    c.save(); c.translate(p.x, p.y); c.rotate(p.angle);
    if (p.kind === 'bullet') { c.strokeStyle = '#ffe680'; c.lineWidth = 3; c.beginPath(); c.moveTo(-14, 0); c.lineTo(4, 0); c.stroke(); }
    else if (p.kind === 'arrow') { c.strokeStyle = '#3b2a17'; c.lineWidth = 2.5; c.beginPath(); c.moveTo(-18, 0); c.lineTo(8, 0); c.stroke(); c.fillStyle = '#ddd'; c.beginPath(); c.moveTo(12, 0); c.lineTo(4, -4); c.lineTo(4, 4); c.closePath(); c.fill(); }
    else { c.fillStyle = '#1a1a1a'; c.beginPath(); c.arc(0, 0, 9, 0, Math.PI * 2); c.fill(); c.fillStyle = '#ff9a3a'; c.beginPath(); c.arc(-8, 0, 4, 0, Math.PI * 2); c.fill(); }
    c.restore();
  }

  drawPreview(c, pv, world) {
    const card = CARDS[pv.cardId];
    if (!card) return;
    const ok = pv.ok;
    const col = ok ? 'rgba(124,211,108,' : 'rgba(255,75,62,';
    c.save(); c.translate(pv.x, pv.y);
    if (card.range) { c.strokeStyle = col + '0.6)'; c.fillStyle = col + '0.06)'; c.lineWidth = 2; c.setLineDash([8, 8]); c.beginPath(); c.arc(0, 0, card.range, 0, Math.PI * 2); c.fill(); c.stroke(); c.setLineDash([]); }
    c.fillStyle = col + '0.35)'; c.strokeStyle = col + '0.9)'; c.lineWidth = 3;
    if (card.kind === 'log') { c.beginPath(); c.roundRect(-card.radius * 0.7, -card.radius * 1.6, card.radius * 1.4, card.radius * 3.2, 12); c.fill(); c.stroke(); c.beginPath(); c.moveTo(card.radius, 0); c.lineTo(card.radius + card.travel * 0.3, 0); c.stroke(); }
    else { c.beginPath(); c.arc(0, 0, card.radius, 0, Math.PI * 2); c.fill(); c.stroke(); }
    c.fillStyle = '#fff'; c.font = `${card.radius * 1.2 | 0}px sans-serif`; c.textAlign = 'center'; c.textBaseline = 'middle'; c.globalAlpha = 0.9;
    c.fillText(card.icon, 0, 2);
    c.restore();
  }
}
