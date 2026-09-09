// =====================================================================
//  IA de la máquina. Juega con el mismo maná y las mismas cartas que el
//  jugador; sólo decide QUÉ carta jugar y DÓNDE, a intervalos regulares.
// =====================================================================
import { WORLD, CARDS, AI, MANA } from '../config.js';
import { SIDE_AI, SIDE_PLAYER } from './world.js';

export class EnemyAI {
  constructor(world, { side = SIDE_AI, difficulty = 1 } = {}) {
    this.world = world;
    this.side = side;
    this.timer = AI.thinkInterval * 0.5;
    this.difficulty = difficulty; // 1 = normal. <1 más torpe, >1 más agresiva.
  }

  update(dt) {
    this.timer -= dt;
    if (this.timer > 0) return;
    this.timer = AI.thinkInterval / this.difficulty * (0.7 + this.world.rng() * 0.6);
    this.think();
  }

  think() {
    const w = this.world;
    const rng = w.rng;
    const me = w.heroes[this.side];
    const foe = w.heroes[1 - this.side];
    if (!foe.alive || !me.alive) return;
    const mana = w.mana[this.side];
    const hand = w.hands[this.side];
    const affordable = hand.filter(id => CARDS[id].cost <= mana);
    if (affordable.length === 0) return;

    // Cuánto ha avanzado el rival hacia mi base (0..1)
    const threat = foe.progress;
    const foeBlocked = foe.stopped;
    // ¿Tengo ya estructuras cerca del rival, por delante de él?
    const near = w.structures.filter(s => s.side === this.side && (s.x - foe.x) * foe.dir > -20 && Math.abs(s.x - foe.x) < 380);
    const hasWallNear = near.some(s => s.kind === 'wall');
    const hasShooterNear = near.some(s => s.card.range);

    // Reserva de maná: cuanto menos amenaza, más ahorra.
    const reserve = threat > 0.6 ? 0 : threat > 0.3 ? 1 : AI.minSaveMana;
    if (mana - Math.min(...affordable.map(id => CARDS[id].cost)) < reserve && threat < 0.5) return;
    // Ahorra para una torre cara si ya hay un muro delante del rival y no hay torres.
    if (!this.savingFor && hasWallNear && !hasShooterNear && rng() < 0.6) {
      this.savingFor = hand.includes('cannon') ? 'cannon' : hand.includes('archer') ? 'archer' : null;
    }
    if (this.savingFor) {
      if (!hand.includes(this.savingFor) || threat > 0.75) this.savingFor = null;
      else if (mana < CARDS[this.savingFor].cost) return;
      else { const c = this.savingFor; this.savingFor = null; if (affordable.includes(c)) { this._placeCard(c, foe, me, threat, false); return; } }
    }

    let choice = null;
    const pri = [];
    // Prioridades según la situación
    if (threat > 0.55 && affordable.includes('log')) pri.push('log');
    if (!hasWallNear && threat > 0.2) pri.push('wall');
    if (hasWallNear && !hasShooterNear) pri.push('cannon', 'archer', 'turret');
    if (threat > 0.35) pri.push('archer', 'turret', 'cannon', 'wire');
    if (foeBlocked) pri.push('turret', 'archer', 'cannon');
    if (threat > 0.4 && affordable.includes('wire')) pri.push('wire');
    pri.push(...affordable); // relleno con lo que haya
    // Un poco de aleatoriedad para no ser predecible
    if (rng() < 0.25) pri.unshift(rng.pick(affordable));
    choice = pri.find(id => affordable.includes(id));
    if (!choice) return;

    // Si la amenaza es baja y tengo mucho maná, construyo "fortaleza" en mi mitad.
    const fortress = threat < 0.3 && mana >= MANA.max - 2 && rng() > AI.aggression;
    this._placeCard(choice, foe, me, threat, fortress);
  }

  _placeCard(choice, foe, me, threat, fortress) {
    const w = this.world;
    const rng = w.rng;
    const b = w.placementBounds(this.side);
    for (let attempt = 0; attempt < 8; attempt++) {
      let x, y;
      const card = CARDS[choice];
      if (card.kind === 'log') {
        // El tronco nace justo delante del rival (en su sentido de avance) y rueda hacia él.
        x = foe.x + foe.dir * (foe.radius + card.radius + 30 + rng() * 60);
        y = foe.y;
      } else if (card.kind === 'wire') {
        x = foe.x + foe.dir * (150 + rng() * 120);
        y = foe.y + rng.range(-20, 20);
      } else if (fortress) {
        const homeSide = this.side === SIDE_AI ? [WORLD.width * 0.58, WORLD.width * 0.85] : [WORLD.width * 0.15, WORLD.width * 0.42];
        x = rng.range(homeSide[0], homeSide[1]);
        y = foe.y + rng.range(-70, 70);
      } else {
        // Delante del rival, a una distancia que le obligue a detenerse pronto.
        x = foe.x + foe.dir * (card.blocks ? rng.range(190, 260) : rng.range(230, 340));
        y = foe.y + rng.range(-60, 60) * (card.blocks ? 0.4 : 1);
      }
      x = Math.max(b.minX + card.radius, Math.min(b.maxX - card.radius, x));
      y = Math.max(b.minY + card.radius, Math.min(b.maxY - card.radius, y));
      if (!w.canPlace(this.side, choice, x, y)) { w.place(this.side, choice, x, y); return; }
    }
  }
}
