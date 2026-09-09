// =====================================================================
//  Simulación del juego (sin dependencias del navegador).
//  Lado 0 = jugador (empieza a la izquierda, avanza hacia +x).
//  Lado 1 = máquina (empieza a la derecha, avanza hacia -x).
// =====================================================================
import { WORLD, MATCH, MANA, HERO, CARDS, DECK_ORDER, HAND_SIZE, PLACEMENT } from '../config.js';
import { makeRng } from './rng.js';

export const SIDE_PLAYER = 0;
export const SIDE_AI = 1;

const LANE_CENTER = (WORLD.laneTop + WORLD.laneBottom) / 2;
// Cada héroe camina por su propia "vía" para que se crucen sin chocar.
const HERO_TRACK_Y = [LANE_CENTER - 55, LANE_CENTER + 55];

let nextId = 1;

export class Hero {
  constructor(side) {
    this.id = nextId++;
    this.type = 'hero';
    this.side = side;
    this.dir = side === SIDE_PLAYER ? 1 : -1;
    this.radius = HERO.radius;
    this.x = side === SIDE_PLAYER ? WORLD.spawnZone * 0.45 : WORLD.width - WORLD.spawnZone * 0.45;
    this.y = HERO_TRACK_Y[side];
    this.hp = HERO.maxHp;
    this.maxHp = HERO.maxHp;
    this.cooldown = 0;
    this.slow = 0;          // factor de ralentización actual (0 = ninguna)
    this.stopped = false;   // true si está atacando una estructura que le cierra el paso
    this.targetId = null;
    this.lastHit = -10;
    this.lastShot = -10;
    this.facing = this.dir; // hacia dónde mira (para el dibujado)
    this.walkPhase = 0;
    this.alive = true;
  }
  get goalX() { return this.side === SIDE_PLAYER ? WORLD.width - WORLD.spawnZone * 0.5 : WORLD.spawnZone * 0.5; }
  get startX() { return this.side === SIDE_PLAYER ? WORLD.spawnZone * 0.45 : WORLD.width - WORLD.spawnZone * 0.45; }
  /** Fracción del camino recorrida (0 = en su base, 1 = en la meta). */
  get progress() {
    const total = Math.abs(this.goalX - this.startX);
    return Math.max(0, Math.min(1, (this.x - this.startX) * this.dir / total));
  }
}

export class Structure {
  constructor(side, card, x, y) {
    this.id = nextId++;
    this.type = 'structure';
    this.card = card;
    this.kind = card.id;
    this.side = side;
    this.x = x; this.y = y;
    this.radius = card.radius;
    this.hp = card.hp; this.maxHp = card.hp;
    this.cooldown = 0.6; // pequeño retardo de "construcción"
    this.lastHit = -10;
    this.lastShot = -10;
    this.angle = side === SIDE_PLAYER ? 0 : Math.PI;
    this.alive = true;
    this.age = 0;
  }
}

export class Log {
  constructor(side, card, x, y) {
    this.id = nextId++;
    this.type = 'log';
    this.card = card;
    this.side = side;
    this.dir = side === SIDE_PLAYER ? 1 : -1;
    this.x = x; this.y = y;
    this.radius = card.radius;
    this.traveled = 0;
    this.pushed = 0;
    this.hitIds = new Set();
    this.roll = 0;
    this.alive = true;
  }
}

export class Wire {
  constructor(side, card, x, y) {
    this.id = nextId++;
    this.type = 'wire';
    this.card = card;
    this.side = side;
    this.x = x; this.y = y;
    this.radius = card.radius;
    this.timeLeft = card.duration;
    this.alive = true;
  }
}

export class Projectile {
  constructor(side, kind, x, y, target, speed, damage, knockback = 0, owner = 'hero') {
    this.id = nextId++;
    this.type = 'projectile';
    this.kind = kind; // 'bullet' | 'arrow' | 'shell'
    this.side = side;
    this.x = x; this.y = y;
    this.target = target;
    this.speed = speed;
    this.damage = damage;
    this.knockback = knockback;
    this.owner = owner;
    this.angle = Math.atan2(target.y - y, target.x - x);
    this.alive = true;
  }
}

export class World {
  constructor({ seed = 1 } = {}) {
    this.rng = makeRng(seed);
    this.time = 0;
    this.speedMult = 1;
    this.frenzy = false;
    this.rain = false;
    this.rainDone = false;
    this.heroes = [new Hero(SIDE_PLAYER), new Hero(SIDE_AI)];
    this.structures = [];
    this.logs = [];
    this.wires = [];
    this.projectiles = [];
    this.mana = [MANA.start, MANA.start];
    this.decks = [this._makeDeck(), this._makeDeck()];
    this.hands = [this.decks[0].splice(0, HAND_SIZE), this.decks[1].splice(0, HAND_SIZE)];
    this.events = [];
    this.result = null; // { winner: 0|1|null, reason: 'hp'|'goal'|'time' }
  }

  _makeDeck() { return [...DECK_ORDER]; }

  emit(type, data = {}) { this.events.push({ type, t: this.time, ...data }); }
  drainEvents() { const e = this.events; this.events = []; return e; }

  nextCard(side) { return this.decks[side][0]; }

  get manaRegen() { return MANA.regenPerSecond * this.speedMult; }

  // ---------------------------------------------------------------
  //  Colocación de cartas
  // ---------------------------------------------------------------
  placementBounds(side) {
    const minX = side === SIDE_PLAYER ? 40 : PLACEMENT.minDistanceToEnemySpawn;
    const maxX = side === SIDE_PLAYER ? WORLD.width - PLACEMENT.minDistanceToEnemySpawn : WORLD.width - 40;
    return { minX, maxX, minY: WORLD.laneTop + 20, maxY: WORLD.laneBottom - 20 };
  }

  /** Devuelve null si se puede colocar, o una cadena con el motivo si no. */
  canPlace(side, cardId, x, y) {
    const card = CARDS[cardId];
    if (!card) return 'Carta desconocida';
    if (!this.hands[side].includes(cardId)) return 'La carta no está en la mano';
    if (this.mana[side] < card.cost) return 'Maná insuficiente';
    const b = this.placementBounds(side);
    const r = card.radius;
    if (x < b.minX + r || x > b.maxX - r) return 'Fuera de la zona de despliegue';
    if (y < b.minY + r || y > b.maxY - r) return 'Fuera de la pista';
    if (card.kind === 'structure') {
      for (const s of this.structures) {
        const d = Math.hypot(s.x - x, s.y - y);
        if (d < s.radius + r + PLACEMENT.structureSpacing) return 'Demasiado cerca de otra estructura';
      }
      for (const h of this.heroes) {
        if (Math.hypot(h.x - x, h.y - y) < h.radius + r) return 'Hay un héroe encima';
      }
    }
    return null;
  }

  place(side, cardId, x, y) {
    const err = this.canPlace(side, cardId, x, y);
    if (err) return err;
    const card = CARDS[cardId];
    this.mana[side] -= card.cost;
    // Rotación de mano al estilo Clash Royale: la carta jugada va al final del mazo.
    const hand = this.hands[side];
    const idx = hand.indexOf(cardId);
    const incoming = this.decks[side].shift();
    hand[idx] = incoming;
    this.decks[side].push(cardId);

    let ent;
    if (card.kind === 'structure') { ent = new Structure(side, card, x, y); this.structures.push(ent); }
    else if (card.kind === 'log') { ent = new Log(side, card, x, y); this.logs.push(ent); }
    else if (card.kind === 'wire') { ent = new Wire(side, card, x, y); this.wires.push(ent); }
    this.emit('place', { side, cardId, x, y, id: ent.id });
    return null;
  }

  // ---------------------------------------------------------------
  //  Bucle de simulación
  // ---------------------------------------------------------------
  update(dt) {
    if (this.result) return;
    this.time += dt;
    this._updatePhases();
    const m = this.speedMult;

    for (let s = 0; s < 2; s++) this.mana[s] = Math.min(MANA.max, this.mana[s] + this.manaRegen * dt);

    for (const h of this.heroes) this._updateHero(h, dt, m);
    for (const s of this.structures) this._updateStructure(s, dt, m);
    for (const l of this.logs) this._updateLog(l, dt, m);
    for (const w of this.wires) this._updateWire(w, dt, m);
    for (const p of this.projectiles) this._updateProjectile(p, dt, m);

    this._cleanup();
    this._checkEnd();
  }

  _updatePhases() {
    if (!this.frenzy && this.time >= MATCH.frenzyStart) {
      this.frenzy = true; this.speedMult = MATCH.frenzyMultiplier; this.emit('frenzy');
    }
    if (!this.rain && !this.rainDone && this.time >= MATCH.rainStart) { this.rain = true; this.emit('rainStart'); }
    if (this.rain && this.time >= MATCH.rainStart + MATCH.rainDuration) { this.rain = false; this.rainDone = true; this.emit('rainEnd'); }
  }

  _updateHero(h, dt, m) {
    if (!h.alive) return;
    h.hp = Math.min(h.maxHp, h.hp + HERO.regenPerSecond * dt);
    h.cooldown -= dt * m;

    // Ralentización por alambre de espino enemigo
    h.slow = 0;
    for (const w of this.wires) {
      if (w.side !== h.side && Math.hypot(w.x - h.x, w.y - h.y) < w.radius + h.radius * 0.5) {
        h.slow = Math.max(h.slow, w.card.slow);
        this._damage(h, w.card.dps * dt, w.side);
      }
    }

    // Reglas de combate del héroe:
    //  1) Las estructuras enemigas son sólidas: si una toca su frente, se detiene
    //     y la golpea hasta derribarla (los muros SÓLO se atacan por contacto).
    //  2) Si no está bloqueado, dispara mientras camina a la torre enemiga más
    //     cercana dentro de alcance (excepto muros), o al héroe rival.
    let blocker = null, nearest = null, bestD = Infinity;
    for (const s of this.structures) {
      if (s.side === h.side || !s.alive) continue;
      const dx = (s.x - h.x) * h.dir;
      const dy = Math.abs(s.y - h.y);
      if (dx >= 0 && dx <= s.radius + h.radius + 4 && dy < s.radius + h.radius * 0.6) {
        if (!blocker || dx < (blocker.x - h.x) * h.dir) blocker = s;
      }
      if (s.card.blocks) continue;
      const d = Math.hypot(s.x - h.x, s.y - h.y);
      if (d <= HERO.attackRange + s.radius && d < bestD) { bestD = d; nearest = s; }
    }
    h.stopped = !!blocker;
    let target = blocker || nearest;
    if (!target) {
      const e = this.heroes[1 - h.side];
      if (e.alive && Math.hypot(e.x - h.x, e.y - h.y) <= HERO.attackRange + e.radius) target = e;
    }
    h.targetId = target ? target.id : null;

    if (target && h.cooldown <= 0) {
      h.cooldown = HERO.attackInterval;
      const dmg = target.type === 'structure' ? HERO.attackDamage * HERO.structureDamageMult : HERO.attackDamage;
      const melee = target === blocker;
      h.lastShot = this.time;
      if (melee) {
        this._damage(target, dmg, h.side);
        this.emit('melee', { x: target.x, y: target.y, side: h.side });
      } else {
        this.projectiles.push(new Projectile(h.side, 'bullet', h.x + h.dir * 20, h.y - 10, target, 900, dmg));
        this.emit('shot', { kind: 'bullet', x: h.x, y: h.y, side: h.side, owner: 'hero' });
      }
    }
    h.facing = target ? Math.sign(target.x - h.x) || h.dir : h.dir;

    if (!h.stopped) {
      const v = HERO.speed * m * (1 - h.slow);
      h.x += h.dir * v * dt;
      h.walkPhase += v * dt * 0.08;
      // Vuelve suavemente a su vía tras un empujón
      h.y += (HERO_TRACK_Y[h.side] - h.y) * Math.min(1, dt * 2);
    }
    const minX = h.radius + 10, maxX = WORLD.width - h.radius - 10;
    h.x = Math.max(minX, Math.min(maxX, h.x));
  }

  _updateStructure(s, dt, m) {
    if (!s.alive) return;
    s.age += dt;
    if (!s.card.range) return;
    s.cooldown -= dt * m;
    const e = this.heroes[1 - s.side];
    if (!e.alive) return;
    const d = Math.hypot(e.x - s.x, e.y - s.y);
    if (d > s.card.range + e.radius) return;
    s.angle = Math.atan2(e.y - s.y, e.x - s.x);
    if (s.cooldown <= 0) {
      s.cooldown = s.card.interval;
      s.lastShot = this.time;
      const kind = s.kind === 'archer' ? 'arrow' : s.kind === 'cannon' ? 'shell' : 'bullet';
      this.projectiles.push(new Projectile(s.side, kind, s.x, s.y - 14, e, s.card.projectileSpeed, s.card.damage, s.card.knockback || 0, s.kind));
      this.emit('shot', { kind, x: s.x, y: s.y, side: s.side, owner: s.kind });
    }
  }

  _updateLog(l, dt, m) {
    if (!l.alive) return;
    const step = l.card.speed * m * dt;
    l.x += l.dir * step;
    l.traveled += step;
    l.roll += step / l.radius;
    const e = this.heroes[1 - l.side];
    if (e.alive && Math.hypot(e.x - l.x, e.y - l.y) < l.radius + e.radius) {
      if (!l.hitIds.has(e.id)) { l.hitIds.add(e.id); this._damage(e, l.card.damage, l.side); this.emit('logHit', { x: e.x, y: e.y }); }
      // Empuja al héroe una distancia limitada; después el tronco le pasa por encima.
      if (l.pushed < l.card.push) {
        const before = e.x;
        e.x = l.x + l.dir * (l.radius + e.radius - 4);
        l.pushed += Math.abs(e.x - before);
        e.stopped = false;
      }
    }
    for (const s of this.structures) {
      if (s.side === l.side || !s.alive || l.hitIds.has(s.id)) continue;
      if (Math.hypot(s.x - l.x, s.y - l.y) < l.radius + s.radius) { l.hitIds.add(s.id); this._damage(s, l.card.structureDamage, l.side); }
    }
    if (l.traveled >= l.card.travel || l.x < -60 || l.x > WORLD.width + 60) { l.alive = false; this.emit('logBreak', { x: l.x, y: l.y }); }
  }

  _updateWire(w, dt, m) {
    w.timeLeft -= dt;
    if (w.timeLeft <= 0) w.alive = false;
  }

  _updateProjectile(p, dt, m) {
    if (!p.alive) return;
    const t = p.target;
    if (!t.alive) { p.alive = false; return; }
    const dx = t.x - p.x, dy = t.y - p.y;
    const d = Math.hypot(dx, dy);
    const step = p.speed * m * dt;
    p.angle = Math.atan2(dy, dx);
    if (d <= step + t.radius * 0.6) {
      p.alive = false;
      this._damage(t, p.damage, p.side);
      if (p.knockback && t.type === 'hero') {
        t.x -= t.dir * p.knockback;
      }
      this.emit('hit', { kind: p.kind, owner: p.owner, damage: p.damage, x: t.x, y: t.y, targetType: t.type, targetId: t.id });
    } else {
      p.x += dx / d * step; p.y += dy / d * step;
    }
  }

  _damage(ent, amount, bySide) {
    if (!ent.alive) return;
    ent.hp -= amount;
    ent.lastHit = this.time;
    if (ent.hp <= 0) {
      ent.hp = 0; ent.alive = false;
      this.emit('destroy', { type: ent.type, kind: ent.kind || 'hero', x: ent.x, y: ent.y, side: ent.side, id: ent.id });
    }
  }

  _cleanup() {
    this.structures = this.structures.filter(s => s.alive);
    this.logs = this.logs.filter(l => l.alive);
    this.wires = this.wires.filter(w => w.alive);
    this.projectiles = this.projectiles.filter(p => p.alive);
  }

  _checkEnd() {
    const [p, a] = this.heroes;
    if (!p.alive && !a.alive) return this._finish(null, 'hp');
    if (!p.alive) return this._finish(SIDE_AI, 'hp');
    if (!a.alive) return this._finish(SIDE_PLAYER, 'hp');
    if (p.x >= p.goalX) return this._finish(SIDE_PLAYER, 'goal');
    if (a.x <= a.goalX) return this._finish(SIDE_AI, 'goal');
    if (this.time >= MATCH.duration) {
      // Desempate al agotarse el tiempo: mayor avance; si es casi igual, más vida.
      const dp = p.progress - a.progress;
      if (Math.abs(dp) > 0.03) return this._finish(dp > 0 ? SIDE_PLAYER : SIDE_AI, 'time');
      const dh = p.hp / p.maxHp - a.hp / a.maxHp;
      if (Math.abs(dh) > 0.02) return this._finish(dh > 0 ? SIDE_PLAYER : SIDE_AI, 'time');
      return this._finish(null, 'time');
    }
  }

  _finish(winner, reason) {
    this.result = { winner, reason, time: this.time };
    this.emit('end', this.result);
  }
}
