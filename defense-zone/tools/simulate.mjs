// Simulación sin pantalla: IA contra IA para comprobar el balance.
// Uso: node tools/simulate.mjs [partidas] [semilla]
import { World, SIDE_PLAYER } from '../game/js/sim/world.js';
import { EnemyAI } from '../game/js/sim/ai.js';

const games = Number(process.argv[2] || 20);
const baseSeed = Number(process.argv[3] || 1);
const DT = 1 / 30;
const stats = { p: 0, a: 0, draw: 0, reasons: {}, times: [], cards: {} };

for (let g = 0; g < games; g++) {
  const w = new World({ seed: baseSeed + g });
  const ais = [new EnemyAI(w, { side: 0 }), new EnemyAI(w, { side: 1 })];
  while (!w.result) {
    for (const ai of ais) ai.update(DT);
    w.update(DT);
    for (const e of w.drainEvents()) if (e.type === 'place') stats.cards[e.cardId] = (stats.cards[e.cardId] || 0) + 1;
  }
  const r = w.result;
  if (r.winner === SIDE_PLAYER) stats.p++; else if (r.winner === null) stats.draw++; else stats.a++;
  stats.reasons[r.reason] = (stats.reasons[r.reason] || 0) + 1;
  stats.times.push(r.time);
  const [p, a] = w.heroes;
  console.log(`#${g + 1}: ganador=${r.winner === null ? 'empate' : r.winner === 0 ? 'izq' : 'der'} motivo=${r.reason} t=${(r.time / 60).toFixed(1)}min  hp=${p.hp.toFixed(0)}/${a.hp.toFixed(0)} avance=${p.progress.toFixed(2)}/${a.progress.toFixed(2)}`);
}
const avg = stats.times.reduce((s, t) => s + t, 0) / stats.times.length;
console.log('---');
console.log(`Izq ${stats.p}  Der ${stats.a}  Empates ${stats.draw}`);
console.log('Motivos:', stats.reasons);
console.log(`Duración media ${(avg / 60).toFixed(1)} min, mín ${(Math.min(...stats.times) / 60).toFixed(1)}, máx ${(Math.max(...stats.times) / 60).toFixed(1)}`);
console.log('Cartas jugadas:', stats.cards);
