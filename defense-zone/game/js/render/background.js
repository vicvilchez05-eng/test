// =====================================================================
//  Fondo del mapa (ambiente de guerra) pre-dibujado una vez en un canvas
//  fuera de pantalla, a resolución de mundo, para no repintar cada frame.
// =====================================================================
import { WORLD, QUALITY } from '../config.js';
import { makeRng } from '../sim/rng.js';

export function buildBackground(quality = 'medium') {
  const q = QUALITY[quality] || QUALITY.medium;
  const W = WORLD.width, H = WORLD.height;
  const cv = document.createElement('canvas');
  cv.width = W; cv.height = H;
  const c = cv.getContext('2d');
  const rng = makeRng(2024);

  // --- Tierra base (barro) ---
  const base = c.createLinearGradient(0, 0, 0, H);
  base.addColorStop(0, '#3e3a2a'); base.addColorStop(0.5, '#4a4331'); base.addColorStop(1, '#3a3526');
  c.fillStyle = base; c.fillRect(0, 0, W, H);
  const blotches = Math.floor(900 * q.decoDensity);
  for (let i = 0; i < blotches; i++) {
    const x = rng() * W, y = rng() * H, r = 6 + rng() * 40;
    c.fillStyle = `rgba(${40 + rng() * 40 | 0},${34 + rng() * 30 | 0},${18 + rng() * 20 | 0},${0.12 + rng() * 0.25})`;
    c.beginPath(); c.ellipse(x, y, r, r * (0.4 + rng() * 0.6), rng() * Math.PI, 0, Math.PI * 2); c.fill();
  }

  // --- Pista central (camino de tierra transitable) ---
  const top = WORLD.laneTop, bot = WORLD.laneBottom;
  const lane = c.createLinearGradient(0, top, 0, bot);
  lane.addColorStop(0, '#6b5c3f'); lane.addColorStop(0.5, '#7a6a48'); lane.addColorStop(1, '#6b5c3f');
  c.fillStyle = lane; c.fillRect(0, top, W, bot - top);
  for (let i = 0; i < 500 * q.decoDensity; i++) {
    const x = rng() * W, y = top + rng() * (bot - top), r = 4 + rng() * 22;
    c.fillStyle = `rgba(${70 + rng() * 50 | 0},${60 + rng() * 40 | 0},${35 + rng() * 25 | 0},${0.1 + rng() * 0.2})`;
    c.beginPath(); c.ellipse(x, y, r, r * 0.5, 0, 0, Math.PI * 2); c.fill();
  }
  // Rodadas de vehículos
  c.strokeStyle = 'rgba(40,32,18,0.45)'; c.lineWidth = 6; c.setLineDash([26, 14]);
  for (const yy of [top + 130, top + 165, bot - 165, bot - 130]) {
    c.beginPath(); c.moveTo(0, yy);
    for (let x = 0; x <= W; x += 80) c.lineTo(x, yy + Math.sin(x * 0.01) * 6);
    c.stroke();
  }
  c.setLineDash([]);

  // --- Sacos terreros bordeando la pista ---
  const sandbag = (x, y, w, h) => {
    c.fillStyle = '#9c8a5a'; c.strokeStyle = '#5c4f30'; c.lineWidth = 1.5;
    c.beginPath(); c.ellipse(x, y, w, h, 0, 0, Math.PI * 2); c.fill(); c.stroke();
    c.fillStyle = 'rgba(255,255,255,0.12)'; c.beginPath(); c.ellipse(x - w * 0.2, y - h * 0.3, w * 0.5, h * 0.3, 0, 0, Math.PI * 2); c.fill();
  };
  for (let x = 20; x < W; x += 34) {
    if (rng() < 0.12) continue;
    sandbag(x, top - 8 + rng() * 4, 17, 9);
    sandbag(x + 17, top - 20 + rng() * 4, 17, 9);
    sandbag(x, bot + 8 + rng() * 4, 17, 9);
    sandbag(x + 17, bot + 20 + rng() * 4, 17, 9);
  }

  // --- Trincheras arriba y abajo ---
  const trench = (y) => {
    c.strokeStyle = '#2a2418'; c.lineWidth = 26; c.lineJoin = 'round';
    c.beginPath(); c.moveTo(-10, y);
    for (let x = 0; x <= W + 60; x += 60) c.lineTo(x, y + (x / 60 % 2 ? 18 : -18));
    c.stroke();
    c.strokeStyle = '#5b5138'; c.lineWidth = 3; c.stroke();
  };
  trench(70); trench(H - 70);

  // --- Alambre de espino en el borde ---
  const wireRow = (y) => {
    c.strokeStyle = '#8a8a80'; c.lineWidth = 1.2;
    c.beginPath();
    for (let x = 0; x <= W; x += 8) c.lineTo(x, y + Math.sin(x * 0.9) * 5);
    c.stroke();
    for (let x = 4; x < W; x += 24) { c.beginPath(); c.moveTo(x - 4, y - 4); c.lineTo(x + 4, y + 4); c.moveTo(x + 4, y - 4); c.lineTo(x - 4, y + 4); c.stroke(); }
  };
  wireRow(130); wireRow(H - 130);

  // --- Cráteres ---
  const crater = (x, y, r) => {
    const g = c.createRadialGradient(x, y, r * 0.1, x, y, r);
    g.addColorStop(0, 'rgba(20,16,10,0.85)'); g.addColorStop(0.7, 'rgba(40,32,20,0.5)'); g.addColorStop(1, 'rgba(90,80,55,0)');
    c.fillStyle = g; c.beginPath(); c.arc(x, y, r, 0, Math.PI * 2); c.fill();
    c.strokeStyle = 'rgba(120,105,70,0.5)'; c.lineWidth = 3; c.beginPath(); c.arc(x, y, r * 0.75, 0, Math.PI * 2); c.stroke();
  };
  for (let i = 0; i < 18 * q.decoDensity; i++) {
    const x = 220 + rng() * (W - 440);
    const y = rng() < 0.5 ? 20 + rng() * 150 : H - 170 + rng() * 150;
    crater(x, y, 18 + rng() * 40);
  }
  for (let i = 0; i < 9 * q.decoDensity; i++) crater(260 + rng() * (W - 520), top + 40 + rng() * (bot - top - 80), 14 + rng() * 22);

  // --- Escombros y vehículos destruidos ---
  const wreck = (x, y, s, flip) => {
    c.save(); c.translate(x, y); c.scale(flip ? -s : s, s);
    c.fillStyle = '#3d3f3a'; c.fillRect(-40, -20, 80, 40);
    c.fillStyle = '#2b2d29'; c.fillRect(-46, -26, 92, 10); c.fillRect(-46, 16, 92, 10);
    c.fillStyle = '#4a4c45'; c.beginPath(); c.arc(-5, 0, 16, 0, Math.PI * 2); c.fill();
    c.fillStyle = '#2b2d29'; c.fillRect(0, -4, 48, 8);
    c.fillStyle = 'rgba(0,0,0,0.5)'; c.beginPath(); c.arc(10, -6, 9, 0, Math.PI * 2); c.fill();
    c.restore();
  };
  wreck(W * 0.28, 105, 1, false); wreck(W * 0.72, H - 100, 1.1, true); wreck(W * 0.5, 40, 0.8, false);
  for (let i = 0; i < 80 * q.decoDensity; i++) {
    const x = rng() * W, y = rng() * H;
    c.fillStyle = `rgba(${60 + rng() * 40 | 0},${60 + rng() * 40 | 0},${55 + rng() * 30 | 0},0.7)`;
    c.fillRect(x, y, 3 + rng() * 8, 2 + rng() * 5);
  }
  // Tocones quemados
  for (let i = 0; i < 12 * q.decoDensity; i++) {
    const x = rng() * W, y = rng() < 0.5 ? 20 + rng() * 60 : H - 80 + rng() * 60;
    c.fillStyle = '#1e1a12'; c.beginPath(); c.arc(x, y, 6 + rng() * 5, 0, Math.PI * 2); c.fill();
    c.strokeStyle = '#1e1a12'; c.lineWidth = 3; c.beginPath(); c.moveTo(x, y); c.lineTo(x + (rng() - 0.5) * 30, y - 10 - rng() * 20); c.stroke();
  }

  // --- Bases / zonas objetivo en cada extremo ---
  const drawBase = (side) => {
    const isLeft = side === 0;
    const x0 = isLeft ? 0 : W - WORLD.spawnZone;
    const color = isLeft ? '#3fa7ff' : '#ff4b3e';
    c.fillStyle = isLeft ? 'rgba(63,167,255,0.10)' : 'rgba(255,75,62,0.10)';
    c.fillRect(x0, top, WORLD.spawnZone, bot - top);
    // Franja de meta con chevrones
    const gx = isLeft ? WORLD.spawnZone : W - WORLD.spawnZone;
    c.save(); c.strokeStyle = color; c.lineWidth = 4; c.setLineDash([16, 10]); c.globalAlpha = 0.7;
    c.beginPath(); c.moveTo(gx, top); c.lineTo(gx, bot); c.stroke(); c.restore();
    // Búnker
    const bx = isLeft ? 60 : W - 60, by = (top + bot) / 2;
    c.fillStyle = '#5a5d55'; c.strokeStyle = '#2f312c'; c.lineWidth = 3;
    c.beginPath(); c.roundRect(bx - 45, by - 60, 90, 120, 10); c.fill(); c.stroke();
    c.fillStyle = '#3c3f39'; c.fillRect(bx - 30, by - 45, 60, 90);
    c.fillStyle = '#1c1d1a'; for (let k = -1; k <= 1; k++) c.fillRect(bx + (isLeft ? 18 : -30), by - 32 + k * 30 + 8, 12, 14);
    // Bandera
    c.fillStyle = '#222'; c.fillRect(bx - 2, by - 100, 4, 44);
    c.fillStyle = color; c.beginPath(); c.moveTo(bx + 2, by - 100); c.lineTo(bx + 40, by - 90); c.lineTo(bx + 2, by - 78); c.closePath(); c.fill();
    // Texto
    c.save(); c.translate(isLeft ? 24 : W - 24, by); c.rotate(isLeft ? -Math.PI / 2 : Math.PI / 2);
    c.fillStyle = 'rgba(255,255,255,0.35)'; c.font = 'bold 22px Impact, Arial Black, sans-serif'; c.textAlign = 'center';
    c.fillText(isLeft ? 'BASE ALIADA' : 'BASE ENEMIGA', 0, 8); c.restore();
  };
  drawBase(0); drawBase(1);

  // --- Viñeta ---
  const vg = c.createRadialGradient(W / 2, H / 2, H * 0.5, W / 2, H / 2, W * 0.75);
  vg.addColorStop(0, 'rgba(0,0,0,0)'); vg.addColorStop(1, 'rgba(0,0,0,0.55)');
  c.fillStyle = vg; c.fillRect(0, 0, W, H);

  return cv;
}
