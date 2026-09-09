// =====================================================================
//  Efectos visuales: partículas, lluvia, relámpagos, sacudida de cámara
//  y textos flotantes. Todo se dibuja encima de la simulación.
// =====================================================================
import { QUALITY, WORLD } from '../config.js';

export class Effects {
  constructor(quality = 'medium') {
    this.setQuality(quality);
    this.particles = [];
    this.texts = [];
    this.smokes = [];      // columnas de humo ambientales (mundo)
    this.rain = [];        // gotas (pantalla)
    this.raining = false;
    this.rainAmount = 0;   // 0..1, entra/sale suavemente
    this.lightning = 0;
    this.nextLightning = 4;
    this.shake = 0;
    this.time = 0;
    this.distantFlash = 0;
    this.nextDistant = 5;
  }

  setQuality(q) { this.q = QUALITY[q] || QUALITY.medium; }

  /** Traduce eventos de la simulación a efectos. */
  handleEvents(events, world) {
    for (const e of events) {
      switch (e.type) {
        case 'shot':
          if (e.kind === 'shell') { this.burst(e.x, e.y - 14, 8, '#ffb347', 120, 0.3); this.smoke(e.x, e.y - 14, 3); this.shake = Math.max(this.shake, 3); }
          else this.burst(e.x, e.y - 8, 2, '#ffe680', 60, 0.12);
          break;
        case 'hit':
          if (e.kind === 'shell') { this.burst(e.x, e.y, 22, '#ff8c3a', 220, 0.5); this.smoke(e.x, e.y, 5); this.shake = Math.max(this.shake, 7); this.text(e.x, e.y - 40, `-${e.damage | 0}`, '#ffb347'); }
          else if (e.kind === 'arrow') { this.burst(e.x, e.y, 4, '#ffd9a0', 90, 0.2); }
          else this.burst(e.x, e.y, 3, '#ffe680', 80, 0.15);
          break;
        case 'melee': this.burst(e.x, e.y, 5, '#ffffff', 100, 0.2); break;
        case 'logHit': this.burst(e.x, e.y, 10, '#b58a4b', 150, 0.4); this.shake = Math.max(this.shake, 4); break;
        case 'logBreak': this.burst(e.x, e.y, 14, '#8a6238', 170, 0.6); break;
        case 'place': this.burst(e.x, e.y, 14, '#d9c9a0', 130, 0.5); this.ring(e.x, e.y, 60, '#ffffff'); break;
        case 'destroy':
          if (e.type === 'destroy' && e.kind === 'hero') { this.burst(e.x, e.y, 60, '#ff5a3a', 320, 1.2); this.burst(e.x, e.y, 30, '#ffd27a', 260, 1.0); this.smoke(e.x, e.y, 14); this.shake = 18; }
          else { this.burst(e.x, e.y, 26, '#aaa', 200, 0.8); this.burst(e.x, e.y, 12, '#ff9a3a', 180, 0.5); this.smoke(e.x, e.y, 6); this.shake = Math.max(this.shake, 6); }
          break;
        case 'frenzy': this.ring(WORLD.width / 2, WORLD.height / 2, 900, '#ff3b3b'); this.shake = 10; break;
        case 'rainStart': this.raining = true; break;
        case 'rainEnd': this.raining = false; break;
      }
    }
  }

  burst(x, y, n, color, speed, life) {
    n = Math.max(1, Math.round(n * this.q.particles));
    for (let i = 0; i < n; i++) {
      const a = Math.random() * Math.PI * 2, v = speed * (0.3 + Math.random() * 0.7);
      this.particles.push({ x, y, vx: Math.cos(a) * v, vy: Math.sin(a) * v - 40, life, maxLife: life, color, size: 2 + Math.random() * 4, kind: 'spark' });
    }
  }
  smoke(x, y, n) {
    n = Math.max(1, Math.round(n * this.q.particles));
    for (let i = 0; i < n; i++) {
      this.particles.push({ x: x + (Math.random() - 0.5) * 20, y, vx: (Math.random() - 0.5) * 30, vy: -20 - Math.random() * 30, life: 1.2 + Math.random(), maxLife: 2, color: '#5a5550', size: 10 + Math.random() * 16, kind: 'smoke' });
    }
  }
  ring(x, y, maxR, color) { this.particles.push({ x, y, life: 0.6, maxLife: 0.6, maxR, color, kind: 'ring' }); }
  text(x, y, txt, color) { this.texts.push({ x, y, txt, color, life: 0.9 }); }

  update(dt, world) {
    this.time += dt;
    for (const p of this.particles) {
      p.life -= dt;
      if (p.kind === 'ring') continue;
      p.x += p.vx * dt; p.y += p.vy * dt;
      if (p.kind === 'spark') { p.vy += 300 * dt; p.vx *= 0.96; }
      else { p.vx *= 0.98; p.size += 12 * dt; }
    }
    this.particles = this.particles.filter(p => p.life > 0);
    for (const t of this.texts) { t.life -= dt; t.y -= 30 * dt; }
    this.texts = this.texts.filter(t => t.life > 0);
    this.shake = Math.max(0, this.shake - dt * 30);

    // Humo ambiental de cráteres
    if (this.smokes.length < 6 * this.q.particles && Math.random() < dt * 0.5) {
      this.smokes.push({ x: 200 + Math.random() * (WORLD.width - 400), y: Math.random() < 0.5 ? 60 + Math.random() * 100 : WORLD.height - 160 + Math.random() * 100, t: 0, dur: 8 + Math.random() * 8 });
    }
    for (const s of this.smokes) { s.t += dt; if (Math.random() < dt * 4 * this.q.particles) this.particles.push({ x: s.x, y: s.y, vx: 10 + Math.random() * 10, vy: -15 - Math.random() * 10, life: 2.5, maxLife: 2.5, color: '#3a3733', size: 8 + Math.random() * 10, kind: 'smoke' }); }
    this.smokes = this.smokes.filter(s => s.t < s.dur);

    // Fogonazos lejanos (artillería en el horizonte)
    this.nextDistant -= dt;
    if (this.nextDistant <= 0) { this.distantFlash = 0.25; this.nextDistant = 6 + Math.random() * 14; }
    this.distantFlash = Math.max(0, this.distantFlash - dt);

    // Lluvia
    const target = this.raining ? 1 : 0;
    this.rainAmount += (target - this.rainAmount) * Math.min(1, dt * 0.6);
    if (this.rainAmount > 0.01) {
      this.nextLightning -= dt;
      if (this.nextLightning <= 0) { this.lightning = 0.5; this.nextLightning = 5 + Math.random() * 12; this.onThunder && this.onThunder(); }
    }
    this.lightning = Math.max(0, this.lightning - dt);
  }

  /** Dibujo en coordenadas de mundo (ya transformado). */
  drawWorld(c) {
    for (const p of this.particles) {
      const a = Math.max(0, p.life / p.maxLife);
      if (p.kind === 'ring') {
        c.globalAlpha = a; c.strokeStyle = p.color; c.lineWidth = 3;
        c.beginPath(); c.arc(p.x, p.y, p.maxR * (1 - a), 0, Math.PI * 2); c.stroke();
      } else if (p.kind === 'smoke') {
        c.globalAlpha = a * 0.35; c.fillStyle = p.color;
        c.beginPath(); c.arc(p.x, p.y, p.size, 0, Math.PI * 2); c.fill();
      } else {
        c.globalAlpha = a; c.fillStyle = p.color;
        c.fillRect(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
      }
    }
    c.globalAlpha = 1;
    c.font = 'bold 22px Arial Black, sans-serif'; c.textAlign = 'center';
    for (const t of this.texts) {
      c.globalAlpha = Math.min(1, t.life * 2); c.fillStyle = t.color; c.strokeStyle = '#000'; c.lineWidth = 4;
      c.strokeText(t.txt, t.x, t.y); c.fillText(t.txt, t.x, t.y);
    }
    c.globalAlpha = 1;
  }

  /** Dibujo en coordenadas de pantalla (lluvia, relámpagos, tintes). */
  drawScreen(c, w, h, world) {
    if (this.distantFlash > 0) { c.fillStyle = `rgba(255,220,160,${this.distantFlash * 0.25})`; c.fillRect(0, 0, w, h); }
    if (this.rainAmount > 0.01) {
      const amt = this.rainAmount;
      c.fillStyle = `rgba(10,16,30,${0.35 * amt})`; c.fillRect(0, 0, w, h);
      const n = Math.floor(this.q.rainDrops * amt);
      while (this.rain.length < n) this.rain.push({ x: Math.random() * (w + 200), y: Math.random() * h, l: 10 + Math.random() * 18, s: 700 + Math.random() * 500 });
      if (this.rain.length > n) this.rain.length = n;
      c.strokeStyle = `rgba(190,210,255,${0.45 * amt})`; c.lineWidth = 1.2;
      c.beginPath();
      const dt = 1 / 60;
      for (const d of this.rain) {
        d.y += d.s * dt; d.x -= d.s * 0.25 * dt;
        if (d.y > h) { d.y = -20; d.x = Math.random() * (w + 200); }
        c.moveTo(d.x, d.y); c.lineTo(d.x - d.l * 0.25, d.y + d.l);
      }
      c.stroke();
      // Salpicaduras en el suelo
      c.fillStyle = `rgba(200,220,255,${0.25 * amt})`;
      for (let i = 0; i < n / 6; i++) { const x = Math.random() * w, y = Math.random() * h; c.beginPath(); c.ellipse(x, y, 3, 1.2, 0, 0, Math.PI * 2); c.fill(); }
      if (this.lightning > 0) {
        c.fillStyle = `rgba(230,240,255,${Math.min(0.85, this.lightning * 1.6)})`; c.fillRect(0, 0, w, h);
      }
    }
    if (world && world.frenzy) {
      const pulse = 0.35 + Math.sin(this.time * 6) * 0.15;
      const g = c.createRadialGradient(w / 2, h / 2, h * 0.45, w / 2, h / 2, w * 0.7);
      g.addColorStop(0, 'rgba(255,0,0,0)'); g.addColorStop(1, `rgba(255,30,30,${pulse})`);
      c.fillStyle = g; c.fillRect(0, 0, w, h);
    }
  }
}
