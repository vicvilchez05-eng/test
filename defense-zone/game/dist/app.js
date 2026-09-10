(() => {
  // ../game/js/config.js
  var WORLD = {
    width: 1600,
    // ancho del mapa en unidades de mundo
    height: 720,
    // alto del mapa
    laneTop: 200,
    // borde superior de la pista transitable
    laneBottom: 620,
    // borde inferior de la pista
    spawnZone: 190,
    // ancho de la zona de aparición / meta en cada extremo
    goalMargin: 30
    // distancia al borde para considerar "llegó a la meta"
  };
  var MATCH = {
    duration: 15 * 60,
    // 15 minutos de partida
    rainStart: 6 * 60,
    // la lluvia empieza a los 6:00
    rainDuration: 2 * 60,
    // ...y dura 2 minutos
    frenzyStart: 9 * 60,
    // a los 9:00 se duplica la velocidad de todo
    frenzyMultiplier: 2
  };
  var MANA = {
    max: 10,
    start: 5,
    regenPerSecond: 1 / 3,
    // "medianamente lenta": 1 maná cada 3 s (Clash Royale usa 2,8 s)
    aiRegenBonus: 1
    // la IA usa la misma regeneración que el jugador
  };
  var HERO = {
    radius: 50,
    // notablemente más grande que cualquier estructura (~2x)
    maxHp: 12e3,
    regenPerSecond: 10,
    // regeneración pasiva: evita que un bloqueo largo sea mortal por sí solo
    speed: 11,
    // unidades / segundo (cruce sin oposición ≈ 1:55)
    attackRange: 190,
    attackDamage: 14,
    attackInterval: 0.4,
    // segundos entre disparos (35 DPS contra el héroe rival)
    structureDamageMult: 5
    // los héroes derriban estructuras muy rápido (175 DPS)
  };
  var CARDS = {
    log: {
      id: "log",
      name: "Tronco",
      cost: 2,
      kind: "log",
      icon: "🪵",
      desc: "Rueda hacia el enemigo y empuja a su héroe hacia atrás.",
      speed: 220,
      travel: 420,
      push: 130,
      radius: 30,
      damage: 60,
      structureDamage: 150
    },
    wire: {
      id: "wire",
      name: "Alambre de espino",
      cost: 3,
      kind: "wire",
      icon: "🕸️",
      desc: "Zona que ralentiza al héroe enemigo y le hace daño mientras la cruza.",
      radius: 90,
      duration: 20,
      slow: 0.45,
      dps: 6
    },
    wall: {
      id: "wall",
      name: "Muro",
      cost: 3,
      kind: "structure",
      icon: "🧱",
      desc: "Bloquea el paso del héroe enemigo hasta que lo derriba.",
      hp: 1800,
      radius: 26,
      blocks: true
    },
    turret: {
      id: "turret",
      name: "Torreta",
      cost: 4,
      kind: "structure",
      icon: "🔫",
      desc: "Ametralladora automática de corto alcance.",
      hp: 750,
      radius: 22,
      range: 240,
      damage: 8,
      interval: 0.5,
      projectileSpeed: 700
    },
    archer: {
      id: "archer",
      name: "Torre de arquero",
      cost: 5,
      kind: "structure",
      icon: "🏹",
      desc: "Arquero en lo alto: gran alcance y daño constante.",
      hp: 800,
      radius: 24,
      range: 300,
      damage: 24,
      interval: 1.2,
      projectileSpeed: 520
    },
    cannon: {
      id: "cannon",
      name: "Cañón",
      cost: 6,
      kind: "structure",
      icon: "💣",
      desc: "Disparo pesado y lento que además empuja al enemigo.",
      hp: 900,
      radius: 26,
      range: 290,
      damage: 60,
      interval: 2.4,
      projectileSpeed: 420,
      knockback: 12
    }
  };
  var DECK_ORDER = ["wall", "turret", "log", "archer", "wire", "cannon"];
  var HAND_SIZE = 4;
  var PLACEMENT = {
    minDistanceToEnemySpawn: 300,
    // no se puede construir pegado a la zona de aparición del enemigo (su base)
    minDistanceToOwnSpawn: 0,
    structureSpacing: 8
    // separación mínima entre estructuras (bordes)
  };
  var AI = {
    thinkInterval: 1.2,
    minSaveMana: 3,
    // la IA intenta guardar algo de maná para reaccionar
    reactDistance: 520,
    // distancia a la que la IA considera "amenaza" al héroe rival
    aggression: 0.55
    // probabilidad de jugar carta agresiva vs defensiva
  };
  var QUALITY = {
    low: { particles: 0.35, rainDrops: 120, shadows: false, dprCap: 1, decoDensity: 0.5 },
    medium: { particles: 0.7, rainDrops: 260, shadows: true, dprCap: 1.5, decoDensity: 0.8 },
    high: { particles: 1, rainDrops: 420, shadows: true, dprCap: 2, decoDensity: 1 }
  };
  var DEFAULT_SETTINGS = {
    quality: "medium",
    musicVolume: 0.6,
    sfxVolume: 0.8
  };

  // ../game/js/sim/rng.js
  function makeRng(seed = 1) {
    let a = seed >>> 0;
    const next = () => {
      a = a + 1831565813 >>> 0;
      let t = a;
      t = Math.imul(t ^ t >>> 15, t | 1);
      t ^= t + Math.imul(t ^ t >>> 7, t | 61);
      return ((t ^ t >>> 14) >>> 0) / 4294967296;
    };
    next.range = (min, max) => min + next() * (max - min);
    next.pick = (arr) => arr[Math.floor(next() * arr.length)];
    return next;
  }

  // ../game/js/sim/world.js
  var SIDE_PLAYER = 0;
  var SIDE_AI = 1;
  var LANE_CENTER = (WORLD.laneTop + WORLD.laneBottom) / 2;
  var HERO_TRACK_Y = [LANE_CENTER - 55, LANE_CENTER + 55];
  var nextId = 1;
  var Hero = class {
    constructor(side) {
      this.id = nextId++;
      this.type = "hero";
      this.side = side;
      this.dir = side === SIDE_PLAYER ? 1 : -1;
      this.radius = HERO.radius;
      this.x = side === SIDE_PLAYER ? WORLD.spawnZone * 0.45 : WORLD.width - WORLD.spawnZone * 0.45;
      this.y = HERO_TRACK_Y[side];
      this.hp = HERO.maxHp;
      this.maxHp = HERO.maxHp;
      this.cooldown = 0;
      this.slow = 0;
      this.stopped = false;
      this.targetId = null;
      this.lastHit = -10;
      this.lastShot = -10;
      this.facing = this.dir;
      this.walkPhase = 0;
      this.alive = true;
    }
    get goalX() {
      return this.side === SIDE_PLAYER ? WORLD.width - WORLD.spawnZone * 0.5 : WORLD.spawnZone * 0.5;
    }
    get startX() {
      return this.side === SIDE_PLAYER ? WORLD.spawnZone * 0.45 : WORLD.width - WORLD.spawnZone * 0.45;
    }
    /** Fracción del camino recorrida (0 = en su base, 1 = en la meta). */
    get progress() {
      const total = Math.abs(this.goalX - this.startX);
      return Math.max(0, Math.min(1, (this.x - this.startX) * this.dir / total));
    }
  };
  var Structure = class {
    constructor(side, card, x, y) {
      this.id = nextId++;
      this.type = "structure";
      this.card = card;
      this.kind = card.id;
      this.side = side;
      this.x = x;
      this.y = y;
      this.radius = card.radius;
      this.hp = card.hp;
      this.maxHp = card.hp;
      this.cooldown = 0.6;
      this.lastHit = -10;
      this.lastShot = -10;
      this.angle = side === SIDE_PLAYER ? 0 : Math.PI;
      this.alive = true;
      this.age = 0;
    }
  };
  var Log = class {
    constructor(side, card, x, y) {
      this.id = nextId++;
      this.type = "log";
      this.card = card;
      this.side = side;
      this.dir = side === SIDE_PLAYER ? 1 : -1;
      this.x = x;
      this.y = y;
      this.radius = card.radius;
      this.traveled = 0;
      this.pushed = 0;
      this.hitIds = /* @__PURE__ */ new Set();
      this.roll = 0;
      this.alive = true;
    }
  };
  var Wire = class {
    constructor(side, card, x, y) {
      this.id = nextId++;
      this.type = "wire";
      this.card = card;
      this.side = side;
      this.x = x;
      this.y = y;
      this.radius = card.radius;
      this.timeLeft = card.duration;
      this.alive = true;
    }
  };
  var Projectile = class {
    constructor(side, kind, x, y, target, speed, damage, knockback = 0, owner = "hero") {
      this.id = nextId++;
      this.type = "projectile";
      this.kind = kind;
      this.side = side;
      this.x = x;
      this.y = y;
      this.target = target;
      this.speed = speed;
      this.damage = damage;
      this.knockback = knockback;
      this.owner = owner;
      this.angle = Math.atan2(target.y - y, target.x - x);
      this.alive = true;
    }
  };
  var World = class {
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
      this.result = null;
    }
    _makeDeck() {
      return [...DECK_ORDER];
    }
    emit(type, data = {}) {
      this.events.push({ type, t: this.time, ...data });
    }
    drainEvents() {
      const e = this.events;
      this.events = [];
      return e;
    }
    nextCard(side) {
      return this.decks[side][0];
    }
    get manaRegen() {
      return MANA.regenPerSecond * this.speedMult;
    }
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
      if (!card) return "Carta desconocida";
      if (!this.hands[side].includes(cardId)) return "La carta no está en la mano";
      if (this.mana[side] < card.cost) return "Maná insuficiente";
      const b = this.placementBounds(side);
      const r = card.radius;
      if (x < b.minX + r || x > b.maxX - r) return "Fuera de la zona de despliegue";
      if (y < b.minY + r || y > b.maxY - r) return "Fuera de la pista";
      if (card.kind === "structure") {
        for (const s of this.structures) {
          const d = Math.hypot(s.x - x, s.y - y);
          if (d < s.radius + r + PLACEMENT.structureSpacing) return "Demasiado cerca de otra estructura";
        }
        for (const h of this.heroes) {
          if (Math.hypot(h.x - x, h.y - y) < h.radius + r) return "Hay un héroe encima";
        }
      }
      return null;
    }
    place(side, cardId, x, y) {
      const err = this.canPlace(side, cardId, x, y);
      if (err) return err;
      const card = CARDS[cardId];
      this.mana[side] -= card.cost;
      const hand = this.hands[side];
      const idx = hand.indexOf(cardId);
      const incoming = this.decks[side].shift();
      hand[idx] = incoming;
      this.decks[side].push(cardId);
      let ent;
      if (card.kind === "structure") {
        ent = new Structure(side, card, x, y);
        this.structures.push(ent);
      } else if (card.kind === "log") {
        ent = new Log(side, card, x, y);
        this.logs.push(ent);
      } else if (card.kind === "wire") {
        ent = new Wire(side, card, x, y);
        this.wires.push(ent);
      }
      this.emit("place", { side, cardId, x, y, id: ent.id });
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
        this.frenzy = true;
        this.speedMult = MATCH.frenzyMultiplier;
        this.emit("frenzy");
      }
      if (!this.rain && !this.rainDone && this.time >= MATCH.rainStart) {
        this.rain = true;
        this.emit("rainStart");
      }
      if (this.rain && this.time >= MATCH.rainStart + MATCH.rainDuration) {
        this.rain = false;
        this.rainDone = true;
        this.emit("rainEnd");
      }
    }
    _updateHero(h, dt, m) {
      if (!h.alive) return;
      h.hp = Math.min(h.maxHp, h.hp + HERO.regenPerSecond * dt);
      h.cooldown -= dt * m;
      h.slow = 0;
      for (const w of this.wires) {
        if (w.side !== h.side && Math.hypot(w.x - h.x, w.y - h.y) < w.radius + h.radius * 0.5) {
          h.slow = Math.max(h.slow, w.card.slow);
          this._damage(h, w.card.dps * dt, w.side);
        }
      }
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
        if (d <= HERO.attackRange + s.radius && d < bestD) {
          bestD = d;
          nearest = s;
        }
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
        const dmg = target.type === "structure" ? HERO.attackDamage * HERO.structureDamageMult : HERO.attackDamage;
        const melee = target === blocker;
        h.lastShot = this.time;
        if (melee) {
          this._damage(target, dmg, h.side);
          this.emit("melee", { x: target.x, y: target.y, side: h.side });
        } else {
          this.projectiles.push(new Projectile(h.side, "bullet", h.x + h.dir * 20, h.y - 10, target, 900, dmg));
          this.emit("shot", { kind: "bullet", x: h.x, y: h.y, side: h.side, owner: "hero" });
        }
      }
      h.facing = target ? Math.sign(target.x - h.x) || h.dir : h.dir;
      if (!h.stopped) {
        const v = HERO.speed * m * (1 - h.slow);
        h.x += h.dir * v * dt;
        h.walkPhase += v * dt * 0.08;
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
        const kind = s.kind === "archer" ? "arrow" : s.kind === "cannon" ? "shell" : "bullet";
        this.projectiles.push(new Projectile(s.side, kind, s.x, s.y - 14, e, s.card.projectileSpeed, s.card.damage, s.card.knockback || 0, s.kind));
        this.emit("shot", { kind, x: s.x, y: s.y, side: s.side, owner: s.kind });
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
        if (!l.hitIds.has(e.id)) {
          l.hitIds.add(e.id);
          this._damage(e, l.card.damage, l.side);
          this.emit("logHit", { x: e.x, y: e.y });
        }
        if (l.pushed < l.card.push) {
          const before = e.x;
          e.x = l.x + l.dir * (l.radius + e.radius - 4);
          l.pushed += Math.abs(e.x - before);
          e.stopped = false;
        }
      }
      for (const s of this.structures) {
        if (s.side === l.side || !s.alive || l.hitIds.has(s.id)) continue;
        if (Math.hypot(s.x - l.x, s.y - l.y) < l.radius + s.radius) {
          l.hitIds.add(s.id);
          this._damage(s, l.card.structureDamage, l.side);
        }
      }
      if (l.traveled >= l.card.travel || l.x < -60 || l.x > WORLD.width + 60) {
        l.alive = false;
        this.emit("logBreak", { x: l.x, y: l.y });
      }
    }
    _updateWire(w, dt, m) {
      w.timeLeft -= dt;
      if (w.timeLeft <= 0) w.alive = false;
    }
    _updateProjectile(p, dt, m) {
      if (!p.alive) return;
      const t = p.target;
      if (!t.alive) {
        p.alive = false;
        return;
      }
      const dx = t.x - p.x, dy = t.y - p.y;
      const d = Math.hypot(dx, dy);
      const step = p.speed * m * dt;
      p.angle = Math.atan2(dy, dx);
      if (d <= step + t.radius * 0.6) {
        p.alive = false;
        this._damage(t, p.damage, p.side);
        if (p.knockback && t.type === "hero") {
          t.x -= t.dir * p.knockback;
        }
        this.emit("hit", { kind: p.kind, owner: p.owner, damage: p.damage, x: t.x, y: t.y, targetType: t.type, targetId: t.id });
      } else {
        p.x += dx / d * step;
        p.y += dy / d * step;
      }
    }
    _damage(ent, amount, bySide) {
      if (!ent.alive) return;
      ent.hp -= amount;
      ent.lastHit = this.time;
      if (ent.hp <= 0) {
        ent.hp = 0;
        ent.alive = false;
        this.emit("destroy", { type: ent.type, kind: ent.kind || "hero", x: ent.x, y: ent.y, side: ent.side, id: ent.id });
      }
    }
    _cleanup() {
      this.structures = this.structures.filter((s) => s.alive);
      this.logs = this.logs.filter((l) => l.alive);
      this.wires = this.wires.filter((w) => w.alive);
      this.projectiles = this.projectiles.filter((p) => p.alive);
    }
    _checkEnd() {
      const [p, a] = this.heroes;
      if (!p.alive && !a.alive) return this._finish(null, "hp");
      if (!p.alive) return this._finish(SIDE_AI, "hp");
      if (!a.alive) return this._finish(SIDE_PLAYER, "hp");
      if (p.x >= p.goalX) return this._finish(SIDE_PLAYER, "goal");
      if (a.x <= a.goalX) return this._finish(SIDE_AI, "goal");
      if (this.time >= MATCH.duration) {
        const dp = p.progress - a.progress;
        if (Math.abs(dp) > 0.03) return this._finish(dp > 0 ? SIDE_PLAYER : SIDE_AI, "time");
        const dh = p.hp / p.maxHp - a.hp / a.maxHp;
        if (Math.abs(dh) > 0.02) return this._finish(dh > 0 ? SIDE_PLAYER : SIDE_AI, "time");
        return this._finish(null, "time");
      }
    }
    _finish(winner, reason) {
      this.result = { winner, reason, time: this.time };
      this.emit("end", this.result);
    }
  };

  // ../game/js/sim/ai.js
  var EnemyAI = class {
    constructor(world, { side = SIDE_AI, difficulty = 1 } = {}) {
      this.world = world;
      this.side = side;
      this.timer = AI.thinkInterval * 0.5;
      this.difficulty = difficulty;
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
      const affordable = hand.filter((id) => CARDS[id].cost <= mana);
      if (affordable.length === 0) return;
      const threat = foe.progress;
      const foeBlocked = foe.stopped;
      const near = w.structures.filter((s) => s.side === this.side && (s.x - foe.x) * foe.dir > -20 && Math.abs(s.x - foe.x) < 380);
      const hasWallNear = near.some((s) => s.kind === "wall");
      const hasShooterNear = near.some((s) => s.card.range);
      const reserve = threat > 0.6 ? 0 : threat > 0.3 ? 1 : AI.minSaveMana;
      if (mana - Math.min(...affordable.map((id) => CARDS[id].cost)) < reserve && threat < 0.5) return;
      if (!this.savingFor && hasWallNear && !hasShooterNear && rng() < 0.6) {
        this.savingFor = hand.includes("cannon") ? "cannon" : hand.includes("archer") ? "archer" : null;
      }
      if (this.savingFor) {
        if (!hand.includes(this.savingFor) || threat > 0.75) this.savingFor = null;
        else if (mana < CARDS[this.savingFor].cost) return;
        else {
          const c = this.savingFor;
          this.savingFor = null;
          if (affordable.includes(c)) {
            this._placeCard(c, foe, me, threat, false);
            return;
          }
        }
      }
      let choice = null;
      const pri = [];
      if (threat > 0.55 && affordable.includes("log")) pri.push("log");
      if (!hasWallNear && threat > 0.2) pri.push("wall");
      if (hasWallNear && !hasShooterNear) pri.push("cannon", "archer", "turret");
      if (threat > 0.35) pri.push("archer", "turret", "cannon", "wire");
      if (foeBlocked) pri.push("turret", "archer", "cannon");
      if (threat > 0.4 && affordable.includes("wire")) pri.push("wire");
      pri.push(...affordable);
      if (rng() < 0.25) pri.unshift(rng.pick(affordable));
      choice = pri.find((id) => affordable.includes(id));
      if (!choice) return;
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
        if (card.kind === "log") {
          x = foe.x + foe.dir * (foe.radius + card.radius + 30 + rng() * 60);
          y = foe.y;
        } else if (card.kind === "wire") {
          x = foe.x + foe.dir * (150 + rng() * 120);
          y = foe.y + rng.range(-20, 20);
        } else if (fortress) {
          const homeSide = this.side === SIDE_AI ? [WORLD.width * 0.58, WORLD.width * 0.85] : [WORLD.width * 0.15, WORLD.width * 0.42];
          x = rng.range(homeSide[0], homeSide[1]);
          y = foe.y + rng.range(-70, 70);
        } else {
          x = foe.x + foe.dir * (card.blocks ? rng.range(190, 260) : rng.range(230, 340));
          y = foe.y + rng.range(-60, 60) * (card.blocks ? 0.4 : 1);
        }
        x = Math.max(b.minX + card.radius, Math.min(b.maxX - card.radius, x));
        y = Math.max(b.minY + card.radius, Math.min(b.maxY - card.radius, y));
        if (!w.canPlace(this.side, choice, x, y)) {
          w.place(this.side, choice, x, y);
          return;
        }
      }
    }
  };

  // ../game/js/render/background.js
  function buildBackground(quality = "medium") {
    const q = QUALITY[quality] || QUALITY.medium;
    const W = WORLD.width, H = WORLD.height;
    const cv = document.createElement("canvas");
    cv.width = W;
    cv.height = H;
    const c = cv.getContext("2d");
    const rng = makeRng(2024);
    const base = c.createLinearGradient(0, 0, 0, H);
    base.addColorStop(0, "#3e3a2a");
    base.addColorStop(0.5, "#4a4331");
    base.addColorStop(1, "#3a3526");
    c.fillStyle = base;
    c.fillRect(0, 0, W, H);
    const blotches = Math.floor(900 * q.decoDensity);
    for (let i = 0; i < blotches; i++) {
      const x = rng() * W, y = rng() * H, r = 6 + rng() * 40;
      c.fillStyle = `rgba(${40 + rng() * 40 | 0},${34 + rng() * 30 | 0},${18 + rng() * 20 | 0},${0.12 + rng() * 0.25})`;
      c.beginPath();
      c.ellipse(x, y, r, r * (0.4 + rng() * 0.6), rng() * Math.PI, 0, Math.PI * 2);
      c.fill();
    }
    const top = WORLD.laneTop, bot = WORLD.laneBottom;
    const lane = c.createLinearGradient(0, top, 0, bot);
    lane.addColorStop(0, "#6b5c3f");
    lane.addColorStop(0.5, "#7a6a48");
    lane.addColorStop(1, "#6b5c3f");
    c.fillStyle = lane;
    c.fillRect(0, top, W, bot - top);
    for (let i = 0; i < 500 * q.decoDensity; i++) {
      const x = rng() * W, y = top + rng() * (bot - top), r = 4 + rng() * 22;
      c.fillStyle = `rgba(${70 + rng() * 50 | 0},${60 + rng() * 40 | 0},${35 + rng() * 25 | 0},${0.1 + rng() * 0.2})`;
      c.beginPath();
      c.ellipse(x, y, r, r * 0.5, 0, 0, Math.PI * 2);
      c.fill();
    }
    c.strokeStyle = "rgba(40,32,18,0.45)";
    c.lineWidth = 6;
    c.setLineDash([26, 14]);
    for (const yy of [top + 130, top + 165, bot - 165, bot - 130]) {
      c.beginPath();
      c.moveTo(0, yy);
      for (let x = 0; x <= W; x += 80) c.lineTo(x, yy + Math.sin(x * 0.01) * 6);
      c.stroke();
    }
    c.setLineDash([]);
    const sandbag = (x, y, w, h) => {
      c.fillStyle = "#9c8a5a";
      c.strokeStyle = "#5c4f30";
      c.lineWidth = 1.5;
      c.beginPath();
      c.ellipse(x, y, w, h, 0, 0, Math.PI * 2);
      c.fill();
      c.stroke();
      c.fillStyle = "rgba(255,255,255,0.12)";
      c.beginPath();
      c.ellipse(x - w * 0.2, y - h * 0.3, w * 0.5, h * 0.3, 0, 0, Math.PI * 2);
      c.fill();
    };
    for (let x = 20; x < W; x += 34) {
      if (rng() < 0.12) continue;
      sandbag(x, top - 8 + rng() * 4, 17, 9);
      sandbag(x + 17, top - 20 + rng() * 4, 17, 9);
      sandbag(x, bot + 8 + rng() * 4, 17, 9);
      sandbag(x + 17, bot + 20 + rng() * 4, 17, 9);
    }
    const trench = (y) => {
      c.strokeStyle = "#2a2418";
      c.lineWidth = 26;
      c.lineJoin = "round";
      c.beginPath();
      c.moveTo(-10, y);
      for (let x = 0; x <= W + 60; x += 60) c.lineTo(x, y + (x / 60 % 2 ? 18 : -18));
      c.stroke();
      c.strokeStyle = "#5b5138";
      c.lineWidth = 3;
      c.stroke();
    };
    trench(70);
    trench(H - 70);
    const wireRow = (y) => {
      c.strokeStyle = "#8a8a80";
      c.lineWidth = 1.2;
      c.beginPath();
      for (let x = 0; x <= W; x += 8) c.lineTo(x, y + Math.sin(x * 0.9) * 5);
      c.stroke();
      for (let x = 4; x < W; x += 24) {
        c.beginPath();
        c.moveTo(x - 4, y - 4);
        c.lineTo(x + 4, y + 4);
        c.moveTo(x + 4, y - 4);
        c.lineTo(x - 4, y + 4);
        c.stroke();
      }
    };
    wireRow(130);
    wireRow(H - 130);
    const crater = (x, y, r) => {
      const g = c.createRadialGradient(x, y, r * 0.1, x, y, r);
      g.addColorStop(0, "rgba(20,16,10,0.85)");
      g.addColorStop(0.7, "rgba(40,32,20,0.5)");
      g.addColorStop(1, "rgba(90,80,55,0)");
      c.fillStyle = g;
      c.beginPath();
      c.arc(x, y, r, 0, Math.PI * 2);
      c.fill();
      c.strokeStyle = "rgba(120,105,70,0.5)";
      c.lineWidth = 3;
      c.beginPath();
      c.arc(x, y, r * 0.75, 0, Math.PI * 2);
      c.stroke();
    };
    for (let i = 0; i < 18 * q.decoDensity; i++) {
      const x = 220 + rng() * (W - 440);
      const y = rng() < 0.5 ? 20 + rng() * 150 : H - 170 + rng() * 150;
      crater(x, y, 18 + rng() * 40);
    }
    for (let i = 0; i < 9 * q.decoDensity; i++) crater(260 + rng() * (W - 520), top + 40 + rng() * (bot - top - 80), 14 + rng() * 22);
    const wreck = (x, y, s, flip) => {
      c.save();
      c.translate(x, y);
      c.scale(flip ? -s : s, s);
      c.fillStyle = "#3d3f3a";
      c.fillRect(-40, -20, 80, 40);
      c.fillStyle = "#2b2d29";
      c.fillRect(-46, -26, 92, 10);
      c.fillRect(-46, 16, 92, 10);
      c.fillStyle = "#4a4c45";
      c.beginPath();
      c.arc(-5, 0, 16, 0, Math.PI * 2);
      c.fill();
      c.fillStyle = "#2b2d29";
      c.fillRect(0, -4, 48, 8);
      c.fillStyle = "rgba(0,0,0,0.5)";
      c.beginPath();
      c.arc(10, -6, 9, 0, Math.PI * 2);
      c.fill();
      c.restore();
    };
    wreck(W * 0.28, 105, 1, false);
    wreck(W * 0.72, H - 100, 1.1, true);
    wreck(W * 0.5, 40, 0.8, false);
    for (let i = 0; i < 80 * q.decoDensity; i++) {
      const x = rng() * W, y = rng() * H;
      c.fillStyle = `rgba(${60 + rng() * 40 | 0},${60 + rng() * 40 | 0},${55 + rng() * 30 | 0},0.7)`;
      c.fillRect(x, y, 3 + rng() * 8, 2 + rng() * 5);
    }
    for (let i = 0; i < 12 * q.decoDensity; i++) {
      const x = rng() * W, y = rng() < 0.5 ? 20 + rng() * 60 : H - 80 + rng() * 60;
      c.fillStyle = "#1e1a12";
      c.beginPath();
      c.arc(x, y, 6 + rng() * 5, 0, Math.PI * 2);
      c.fill();
      c.strokeStyle = "#1e1a12";
      c.lineWidth = 3;
      c.beginPath();
      c.moveTo(x, y);
      c.lineTo(x + (rng() - 0.5) * 30, y - 10 - rng() * 20);
      c.stroke();
    }
    const drawBase = (side) => {
      const isLeft = side === 0;
      const x0 = isLeft ? 0 : W - WORLD.spawnZone;
      const color = isLeft ? "#3fa7ff" : "#ff4b3e";
      c.fillStyle = isLeft ? "rgba(63,167,255,0.10)" : "rgba(255,75,62,0.10)";
      c.fillRect(x0, top, WORLD.spawnZone, bot - top);
      const gx = isLeft ? WORLD.spawnZone : W - WORLD.spawnZone;
      c.save();
      c.strokeStyle = color;
      c.lineWidth = 4;
      c.setLineDash([16, 10]);
      c.globalAlpha = 0.7;
      c.beginPath();
      c.moveTo(gx, top);
      c.lineTo(gx, bot);
      c.stroke();
      c.restore();
      const bx = isLeft ? 60 : W - 60, by = (top + bot) / 2;
      c.fillStyle = "#5a5d55";
      c.strokeStyle = "#2f312c";
      c.lineWidth = 3;
      c.beginPath();
      c.roundRect(bx - 45, by - 60, 90, 120, 10);
      c.fill();
      c.stroke();
      c.fillStyle = "#3c3f39";
      c.fillRect(bx - 30, by - 45, 60, 90);
      c.fillStyle = "#1c1d1a";
      for (let k = -1; k <= 1; k++) c.fillRect(bx + (isLeft ? 18 : -30), by - 32 + k * 30 + 8, 12, 14);
      c.fillStyle = "#222";
      c.fillRect(bx - 2, by - 100, 4, 44);
      c.fillStyle = color;
      c.beginPath();
      c.moveTo(bx + 2, by - 100);
      c.lineTo(bx + 40, by - 90);
      c.lineTo(bx + 2, by - 78);
      c.closePath();
      c.fill();
      c.save();
      c.translate(isLeft ? 24 : W - 24, by);
      c.rotate(isLeft ? -Math.PI / 2 : Math.PI / 2);
      c.fillStyle = "rgba(255,255,255,0.35)";
      c.font = "bold 22px Impact, Arial Black, sans-serif";
      c.textAlign = "center";
      c.fillText(isLeft ? "BASE ALIADA" : "BASE ENEMIGA", 0, 8);
      c.restore();
    };
    drawBase(0);
    drawBase(1);
    const vg = c.createRadialGradient(W / 2, H / 2, H * 0.5, W / 2, H / 2, W * 0.75);
    vg.addColorStop(0, "rgba(0,0,0,0)");
    vg.addColorStop(1, "rgba(0,0,0,0.55)");
    c.fillStyle = vg;
    c.fillRect(0, 0, W, H);
    return cv;
  }

  // ../game/js/render/renderer.js
  var TEAM = {
    0: { main: "#3fa7ff", dark: "#1d5f9c", light: "#bfe3ff", name: "ALIADO" },
    1: { main: "#ff4b3e", dark: "#8f1f18", light: "#ffc9c4", name: "ENEMIGO" }
  };
  var Renderer = class {
    constructor(canvas, settings) {
      this.canvas = canvas;
      this.ctx = canvas.getContext("2d");
      this.settings = settings;
      this.bottomInset = 0;
      this.topInset = 0;
      this.view = { scale: 1, ox: 0, oy: 0 };
      this.bg = buildBackground(settings.quality);
      this.quality = settings.quality;
      this.w = 0;
      this.h = 0;
      this.dpr = 1;
      this.menuT = 0;
      this.resize();
    }
    setQuality(q) {
      if (q === this.quality) return;
      this.quality = q;
      this.bg = buildBackground(q);
      this.resize();
    }
    resize() {
      const q = QUALITY[this.quality] || QUALITY.medium;
      this.dpr = Math.min(window.devicePixelRatio || 1, q.dprCap);
      this.w = window.innerWidth;
      this.h = window.innerHeight;
      this.canvas.width = Math.round(this.w * this.dpr);
      this.canvas.height = Math.round(this.h * this.dpr);
      this.computeView();
    }
    computeView() {
      const availW = this.w, availH = this.h - this.bottomInset - this.topInset;
      const laneCenter = (WORLD.laneTop + WORLD.laneBottom) / 2;
      const minVisibleH = WORLD.laneBottom - WORLD.laneTop + 60;
      const scale = Math.min(availW / WORLD.width, availH / minVisibleH);
      const visibleH = availH / scale;
      let oy;
      if (visibleH >= WORLD.height) oy = this.topInset + (availH - WORLD.height * scale) / 2;
      else {
        let top = laneCenter - visibleH / 2;
        top = Math.max(0, Math.min(WORLD.height - visibleH, top));
        oy = this.topInset - top * scale;
      }
      this.view = { scale, ox: (availW - WORLD.width * scale) / 2, oy };
    }
    setInsets(top, bottom) {
      this.topInset = top;
      this.bottomInset = bottom;
      this.computeView();
    }
    screenToWorld(sx, sy) {
      const v = this.view;
      return { x: (sx - v.ox) / v.scale, y: (sy - v.oy) / v.scale };
    }
    worldToScreen(x, y) {
      const v = this.view;
      return { x: v.ox + x * v.scale, y: v.oy + y * v.scale };
    }
    // ------------------------------------------------------------------
    //  Menú principal: escena animada de fondo
    // ------------------------------------------------------------------
    drawMenu(dt) {
      this.menuT += dt;
      const c = this.ctx, t = this.menuT;
      c.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
      const w = this.w, h = this.h;
      const scale = Math.max(w / WORLD.width, h / WORLD.height) * 1.15;
      const ox = (w - WORLD.width * scale) / 2 + Math.sin(t * 0.1) * 40;
      const oy = (h - WORLD.height * scale) / 2 + Math.cos(t * 0.13) * 20;
      c.save();
      c.translate(ox, oy);
      c.scale(scale, scale);
      c.drawImage(this.bg, 0, 0);
      c.restore();
      c.fillStyle = "rgba(5,8,5,0.55)";
      c.fillRect(0, 0, w, h);
      for (let i = 0; i < 2; i++) {
        const a = Math.sin(t * 0.35 + i * 2.1) * 0.5 + (i ? -0.2 : 0.2);
        const bx = i ? w * 0.92 : w * 0.08, by = h * 1.05;
        c.save();
        c.translate(bx, by);
        c.rotate(a);
        const g = c.createLinearGradient(0, 0, 0, -h * 1.4);
        g.addColorStop(0, "rgba(255,240,200,0.28)");
        g.addColorStop(1, "rgba(255,240,200,0)");
        c.fillStyle = g;
        c.beginPath();
        c.moveTo(0, 0);
        c.lineTo(-h * 0.25, -h * 1.4);
        c.lineTo(h * 0.25, -h * 1.4);
        c.closePath();
        c.fill();
        c.restore();
      }
      for (let i = 0; i < 14; i++) {
        const x = (i * 173 + t * 18 * (1 + i % 3)) % (w + 300) - 150;
        const y = h * (0.2 + i % 5 * 0.15) + Math.sin(t * 0.5 + i) * 20;
        c.fillStyle = `rgba(60,58,52,${0.12 + i % 3 * 0.05})`;
        c.beginPath();
        c.arc(x, y, 60 + i % 4 * 30, 0, Math.PI * 2);
        c.fill();
      }
      c.fillStyle = "rgba(255,170,60,0.8)";
      for (let i = 0; i < 25; i++) {
        const p = (t * 0.15 + i * 0.137) % 1;
        const x = w * (i * 0.618 % 1) + Math.sin(t + i) * 30, y = h * (1 - p);
        c.globalAlpha = 1 - p;
        c.fillRect(x, y, 3, 3);
      }
      c.globalAlpha = 1;
      const flash = Math.max(0, Math.sin(t * 1.3) - 0.97) * 30;
      if (flash > 0) {
        c.fillStyle = `rgba(255,220,150,${Math.min(0.3, flash)})`;
        c.fillRect(0, 0, w, h);
      }
    }
    // ------------------------------------------------------------------
    //  Partida
    // ------------------------------------------------------------------
    drawGame(world, fx, preview) {
      const c = this.ctx, v = this.view;
      c.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
      c.fillStyle = "#0b0f0a";
      c.fillRect(0, 0, this.w, this.h);
      let sx = 0, sy = 0;
      if (fx.shake > 0) {
        sx = (Math.random() - 0.5) * fx.shake;
        sy = (Math.random() - 0.5) * fx.shake;
      }
      c.save();
      c.beginPath();
      c.rect(0, this.topInset, this.w, this.h - this.topInset - this.bottomInset);
      c.clip();
      c.translate(v.ox + sx, v.oy + sy);
      c.scale(v.scale, v.scale);
      c.drawImage(this.bg, 0, 0);
      if (fx.rainAmount > 0.02) this.drawPuddles(c, fx);
      for (const w of world.wires) this.drawWire(c, w, world);
      if (preview) this.drawPreview(c, preview, world);
      const ents = [...world.structures, ...world.logs, ...world.heroes.filter((h) => h.alive)];
      ents.sort((a, b) => a.y - b.y);
      for (const e of ents) this.drawShadow(c, e);
      for (const e of ents) {
        if (e.type === "hero") this.drawHero(c, e, world);
        else if (e.type === "structure") this.drawStructure(c, e, world);
        else if (e.type === "log") this.drawLog(c, e);
      }
      for (const p of world.projectiles) this.drawProjectile(c, p);
      fx.drawWorld(c);
      for (const h of world.heroes) if (h.alive) this.drawHeroBar(c, h);
      c.restore();
      fx.drawScreen(c, this.w, this.h, world);
    }
    drawPuddles(c, fx) {
      c.globalAlpha = 0.35 * fx.rainAmount;
      c.fillStyle = "#6f86a8";
      const seeds = [[300, 260], [520, 560], [800, 300], [1050, 520], [1300, 280], [700, 480], [950, 400]];
      for (const [x, y] of seeds) {
        c.beginPath();
        c.ellipse(x, y, 60, 22, 0, 0, Math.PI * 2);
        c.fill();
      }
      c.globalAlpha = 1;
    }
    drawShadow(c, e) {
      const q = QUALITY[this.quality];
      if (!q.shadows) return;
      c.fillStyle = "rgba(0,0,0,0.35)";
      c.beginPath();
      c.ellipse(e.x + 6, e.y + e.radius * 0.55, e.radius * 1.05, e.radius * 0.5, 0, 0, Math.PI * 2);
      c.fill();
    }
    drawHero(c, h, world) {
      const T = TEAM[h.side], r = h.radius;
      const hit = world.time - h.lastHit < 0.12;
      c.save();
      c.translate(h.x, h.y);
      const step = h.stopped ? 0 : Math.sin(h.walkPhase * 4) * r * 0.35;
      c.fillStyle = "#2f3524";
      c.beginPath();
      c.ellipse(step * h.dir, r * 0.35, r * 0.3, r * 0.42, 0, 0, Math.PI * 2);
      c.fill();
      c.beginPath();
      c.ellipse(-step * h.dir, r * 0.35, r * 0.3, r * 0.42, 0, 0, Math.PI * 2);
      c.fill();
      const bg = c.createRadialGradient(-r * 0.3, -r * 0.3, r * 0.1, 0, 0, r);
      bg.addColorStop(0, T.light);
      bg.addColorStop(0.35, T.main);
      bg.addColorStop(1, T.dark);
      c.fillStyle = hit ? "#ffffff" : bg;
      c.strokeStyle = "#111";
      c.lineWidth = 3;
      c.beginPath();
      c.arc(0, 0, r, 0, Math.PI * 2);
      c.fill();
      c.stroke();
      c.strokeStyle = "rgba(0,0,0,0.35)";
      c.lineWidth = r * 0.16;
      c.beginPath();
      c.moveTo(-r * 0.5, -r * 0.8);
      c.lineTo(r * 0.5, r * 0.8);
      c.moveTo(r * 0.5, -r * 0.8);
      c.lineTo(-r * 0.5, r * 0.8);
      c.stroke();
      const ang = h.targetId ? this._aimAngle(h, world) : h.facing > 0 ? 0 : Math.PI;
      c.save();
      c.rotate(ang);
      c.fillStyle = "#1b1b1b";
      c.fillRect(r * 0.1, -r * 0.12, r * 1.5, r * 0.24);
      c.fillStyle = "#6b4a2b";
      c.fillRect(r * 0.2, -r * 0.2, r * 0.6, r * 0.4);
      c.fillStyle = "#d8a97a";
      c.beginPath();
      c.arc(r * 0.55, r * 0.35, r * 0.2, 0, Math.PI * 2);
      c.fill();
      c.beginPath();
      c.arc(r * 1, -r * 0.1, r * 0.18, 0, Math.PI * 2);
      c.fill();
      if (world.time - h.lastShot < 0.06 && !h.stopped) {
        c.fillStyle = "#ffe680";
        c.beginPath();
        c.arc(r * 1.7, 0, r * 0.28, 0, Math.PI * 2);
        c.fill();
      }
      c.restore();
      const hg = c.createRadialGradient(-r * 0.15, -r * 0.15, r * 0.05, 0, 0, r * 0.55);
      hg.addColorStop(0, "#7f8a5c");
      hg.addColorStop(1, "#3d4630");
      c.fillStyle = hit ? "#fff" : hg;
      c.strokeStyle = "#1d2216";
      c.lineWidth = 2.5;
      c.beginPath();
      c.arc(0, 0, r * 0.55, 0, Math.PI * 2);
      c.fill();
      c.stroke();
      c.strokeStyle = T.main;
      c.lineWidth = 3;
      c.beginPath();
      c.arc(0, 0, r * 0.42, 0, Math.PI * 2);
      c.stroke();
      c.fillStyle = "#fff";
      c.font = `bold ${r * 0.5 | 0}px Arial`;
      c.textAlign = "center";
      c.textBaseline = "middle";
      c.fillText("★", 0, 1);
      if (h.slow > 0) {
        c.strokeStyle = "rgba(255,255,255,0.7)";
        c.lineWidth = 2;
        c.setLineDash([4, 6]);
        c.beginPath();
        c.arc(0, 0, r + 6, 0, Math.PI * 2);
        c.stroke();
        c.setLineDash([]);
      }
      c.restore();
    }
    _aimAngle(h, world) {
      const t = world.structures.find((s) => s.id === h.targetId) || world.heroes.find((x) => x.id === h.targetId);
      return t ? Math.atan2(t.y - h.y, t.x - h.x) : 0;
    }
    drawHeroBar(c, h) {
      const T = TEAM[h.side], r = h.radius;
      const w = r * 3.6, hh = 22, x = h.x - w / 2, y = h.y - r - 44;
      c.fillStyle = "rgba(0,0,0,0.75)";
      c.beginPath();
      c.roundRect(x - 3, y - 3, w + 6, hh + 6, 6);
      c.fill();
      c.fillStyle = "#2a2a2a";
      c.fillRect(x, y, w, hh);
      const pct = h.hp / h.maxHp;
      const g = c.createLinearGradient(0, y, 0, y + hh);
      g.addColorStop(0, T.light);
      g.addColorStop(1, T.main);
      c.fillStyle = g;
      c.fillRect(x, y, w * pct, hh);
      c.strokeStyle = "rgba(255,255,255,0.35)";
      c.lineWidth = 1;
      for (let i = 1; i < 10; i++) {
        c.beginPath();
        c.moveTo(x + w * i / 10, y);
        c.lineTo(x + w * i / 10, y + hh);
        c.stroke();
      }
      c.fillStyle = "#fff";
      c.font = "bold 16px Arial";
      c.textAlign = "center";
      c.textBaseline = "middle";
      c.strokeStyle = "#000";
      c.lineWidth = 3;
      c.strokeText(`${Math.ceil(h.hp)}`, h.x, y + hh / 2);
      c.fillText(`${Math.ceil(h.hp)}`, h.x, y + hh / 2);
      c.font = "bold 14px Arial";
      c.fillStyle = T.main;
      c.strokeText(h.side === SIDE_PLAYER ? "TU HÉROE" : "HÉROE ENEMIGO", h.x, y - 10);
      c.fillText(h.side === SIDE_PLAYER ? "TU HÉROE" : "HÉROE ENEMIGO", h.x, y - 10);
    }
    drawStructure(c, s, world) {
      const T = TEAM[s.side], r = s.radius;
      const hit = world.time - s.lastHit < 0.1;
      const build = Math.min(1, s.age / 0.4);
      c.save();
      c.translate(s.x, s.y);
      c.scale(build, build);
      c.strokeStyle = T.main;
      c.lineWidth = 2;
      c.globalAlpha = 0.6;
      c.beginPath();
      c.arc(0, 0, r + 4, 0, Math.PI * 2);
      c.stroke();
      c.globalAlpha = 1;
      switch (s.kind) {
        case "wall": {
          c.fillStyle = hit ? "#fff" : "#8a8d84";
          c.strokeStyle = "#262824";
          c.lineWidth = 3;
          c.beginPath();
          c.roundRect(-r * 0.9, -r * 1.5, r * 1.8, r * 3, 5);
          c.fill();
          c.stroke();
          c.strokeStyle = "rgba(0,0,0,0.4)";
          c.lineWidth = 2;
          for (let i = -2; i <= 2; i++) {
            c.beginPath();
            c.moveTo(-r * 0.9, i * r * 0.6);
            c.lineTo(r * 0.9, i * r * 0.6);
            c.stroke();
            if (i < 2) {
              c.beginPath();
              c.moveTo(i % 2 ? -r * 0.3 : r * 0.3, i * r * 0.6);
              c.lineTo(i % 2 ? -r * 0.3 : r * 0.3, (i + 1) * r * 0.6);
              c.stroke();
            }
          }
          c.fillStyle = "#a89468";
          c.strokeStyle = "#4d4128";
          c.lineWidth = 1.5;
          for (let i = -1; i <= 1; i++) {
            c.beginPath();
            c.ellipse(r * 0.55 * (s.side === 0 ? -1 : 1), i * r * 0.9, r * 0.45, r * 0.32, 0, 0, Math.PI * 2);
            c.fill();
            c.stroke();
          }
          if (s.hp < s.maxHp * 0.5) {
            c.strokeStyle = "#1a1a1a";
            c.lineWidth = 2;
            c.beginPath();
            c.moveTo(-r * 0.5, -r);
            c.lineTo(0, -r * 0.2);
            c.lineTo(-r * 0.3, r * 0.6);
            c.moveTo(r * 0.4, -r * 0.6);
            c.lineTo(r * 0.1, r * 0.3);
            c.stroke();
          }
          break;
        }
        case "turret": {
          c.fillStyle = "#3c3f3a";
          c.strokeStyle = "#1b1c19";
          c.lineWidth = 3;
          c.beginPath();
          c.arc(0, 0, r, 0, Math.PI * 2);
          c.fill();
          c.stroke();
          c.fillStyle = hit ? "#fff" : "#5c6058";
          c.beginPath();
          c.arc(0, 0, r * 0.65, 0, Math.PI * 2);
          c.fill();
          c.save();
          c.rotate(s.angle);
          c.fillStyle = "#1b1c19";
          c.fillRect(0, -r * 0.16, r * 1.5, r * 0.32);
          c.fillRect(0, -r * 0.4, r * 0.5, r * 0.8);
          if (world.time - s.lastShot < 0.07) {
            c.fillStyle = "#ffe680";
            c.beginPath();
            c.arc(r * 1.6, 0, r * 0.3, 0, Math.PI * 2);
            c.fill();
          }
          c.restore();
          c.fillStyle = T.main;
          c.beginPath();
          c.arc(0, 0, r * 0.22, 0, Math.PI * 2);
          c.fill();
          break;
        }
        case "archer": {
          c.fillStyle = hit ? "#fff" : "#6d4a2c";
          c.strokeStyle = "#2b1c10";
          c.lineWidth = 3;
          c.beginPath();
          c.roundRect(-r, -r, r * 2, r * 2, 5);
          c.fill();
          c.stroke();
          c.strokeStyle = "rgba(0,0,0,0.35)";
          c.lineWidth = 2;
          for (let i = -2; i <= 2; i++) {
            c.beginPath();
            c.moveTo(-r, i * r * 0.4);
            c.lineTo(r, i * r * 0.4);
            c.stroke();
          }
          c.fillStyle = "#8b6a3e";
          c.beginPath();
          c.roundRect(-r * 0.7, -r * 0.7, r * 1.4, r * 1.4, 4);
          c.fill();
          c.save();
          c.rotate(s.angle);
          c.strokeStyle = "#e8d8a0";
          c.lineWidth = 2.5;
          c.beginPath();
          c.arc(r * 0.35, 0, r * 0.45, -Math.PI / 2, Math.PI / 2);
          c.stroke();
          c.beginPath();
          c.moveTo(r * 0.35, -r * 0.45);
          c.lineTo(r * 0.35, r * 0.45);
          c.stroke();
          c.restore();
          c.fillStyle = T.main;
          c.beginPath();
          c.arc(0, 0, r * 0.28, 0, Math.PI * 2);
          c.fill();
          c.fillStyle = "#5d6b3f";
          c.beginPath();
          c.arc(0, 0, r * 0.2, 0, Math.PI * 2);
          c.fill();
          break;
        }
        case "cannon": {
          c.fillStyle = "#4a3b2a";
          c.strokeStyle = "#1f1710";
          c.lineWidth = 3;
          for (const wx of [-r * 0.7, r * 0.7]) {
            c.beginPath();
            c.ellipse(wx, r * 0.3, r * 0.3, r * 0.7, 0, 0, Math.PI * 2);
            c.fill();
            c.stroke();
          }
          c.fillStyle = hit ? "#fff" : "#3a3d38";
          c.beginPath();
          c.arc(0, 0, r * 0.85, 0, Math.PI * 2);
          c.fill();
          c.stroke();
          c.save();
          c.rotate(s.angle);
          c.fillStyle = "#1f2220";
          c.fillRect(-r * 0.2, -r * 0.3, r * 1.7, r * 0.6);
          c.fillStyle = "#2f3330";
          c.beginPath();
          c.arc(0, 0, r * 0.5, 0, Math.PI * 2);
          c.fill();
          if (world.time - s.lastShot < 0.12) {
            c.fillStyle = "#ffb347";
            c.beginPath();
            c.arc(r * 1.8, 0, r * 0.5, 0, Math.PI * 2);
            c.fill();
          }
          c.restore();
          c.fillStyle = T.main;
          c.beginPath();
          c.arc(0, 0, r * 0.18, 0, Math.PI * 2);
          c.fill();
          break;
        }
      }
      c.restore();
      if (s.hp < s.maxHp) {
        const w = r * 2.2, x = s.x - w / 2, y = s.y - r - 16;
        c.fillStyle = "rgba(0,0,0,0.7)";
        c.fillRect(x - 1, y - 1, w + 2, 8);
        c.fillStyle = s.hp / s.maxHp > 0.4 ? "#7cd36c" : "#ffb347";
        c.fillRect(x, y, w * s.hp / s.maxHp, 6);
      }
    }
    drawLog(c, l) {
      const r = l.radius;
      c.save();
      c.translate(l.x, l.y);
      c.fillStyle = "#6b4626";
      c.strokeStyle = "#2e1d0e";
      c.lineWidth = 3;
      c.beginPath();
      c.roundRect(-r * 0.7, -r * 1.6, r * 1.4, r * 3.2, r * 0.5);
      c.fill();
      c.stroke();
      c.strokeStyle = "rgba(30,18,8,0.55)";
      c.lineWidth = 3;
      const off = l.roll * r * 0.5 % (r * 0.7);
      for (let x = -r * 0.7 + off - r * 0.7; x < r * 0.7; x += r * 0.35) {
        c.beginPath();
        c.moveTo(x, -r * 1.5);
        c.lineTo(x + 3, r * 1.5);
        c.stroke();
      }
      c.fillStyle = "#c9a06a";
      c.beginPath();
      c.ellipse(0, -r * 1.55, r * 0.68, r * 0.25, 0, 0, Math.PI * 2);
      c.fill();
      c.stroke();
      c.beginPath();
      c.ellipse(0, r * 1.55, r * 0.68, r * 0.25, 0, 0, Math.PI * 2);
      c.fill();
      c.stroke();
      c.restore();
    }
    drawWire(c, w, world) {
      const T = TEAM[w.side];
      const fade = Math.min(1, w.timeLeft / 3);
      c.save();
      c.translate(w.x, w.y);
      c.globalAlpha = 0.85 * fade;
      c.fillStyle = w.side === 0 ? "rgba(63,167,255,0.12)" : "rgba(255,75,62,0.12)";
      c.beginPath();
      c.arc(0, 0, w.radius, 0, Math.PI * 2);
      c.fill();
      c.strokeStyle = "#9a9a92";
      c.lineWidth = 1.6;
      for (let ring = 0.35; ring <= 1; ring += 0.32) {
        const R = w.radius * ring;
        c.beginPath();
        for (let a = 0; a <= Math.PI * 2 + 0.01; a += 0.12) {
          const rr = R + Math.sin(a * 18 + world.time * 2) * 4;
          c.lineTo(Math.cos(a) * rr, Math.sin(a) * rr);
        }
        c.stroke();
      }
      c.strokeStyle = T.main;
      c.setLineDash([6, 8]);
      c.lineWidth = 2;
      c.beginPath();
      c.arc(0, 0, w.radius, 0, Math.PI * 2);
      c.stroke();
      c.restore();
    }
    drawProjectile(c, p) {
      c.save();
      c.translate(p.x, p.y);
      c.rotate(p.angle);
      if (p.kind === "bullet") {
        c.strokeStyle = "#ffe680";
        c.lineWidth = 3;
        c.beginPath();
        c.moveTo(-14, 0);
        c.lineTo(4, 0);
        c.stroke();
      } else if (p.kind === "arrow") {
        c.strokeStyle = "#3b2a17";
        c.lineWidth = 2.5;
        c.beginPath();
        c.moveTo(-18, 0);
        c.lineTo(8, 0);
        c.stroke();
        c.fillStyle = "#ddd";
        c.beginPath();
        c.moveTo(12, 0);
        c.lineTo(4, -4);
        c.lineTo(4, 4);
        c.closePath();
        c.fill();
      } else {
        c.fillStyle = "#1a1a1a";
        c.beginPath();
        c.arc(0, 0, 9, 0, Math.PI * 2);
        c.fill();
        c.fillStyle = "#ff9a3a";
        c.beginPath();
        c.arc(-8, 0, 4, 0, Math.PI * 2);
        c.fill();
      }
      c.restore();
    }
    drawPreview(c, pv, world) {
      const card = CARDS[pv.cardId];
      if (!card) return;
      const ok = pv.ok;
      const col = ok ? "rgba(124,211,108," : "rgba(255,75,62,";
      c.save();
      c.translate(pv.x, pv.y);
      if (card.range) {
        c.strokeStyle = col + "0.6)";
        c.fillStyle = col + "0.06)";
        c.lineWidth = 2;
        c.setLineDash([8, 8]);
        c.beginPath();
        c.arc(0, 0, card.range, 0, Math.PI * 2);
        c.fill();
        c.stroke();
        c.setLineDash([]);
      }
      c.fillStyle = col + "0.35)";
      c.strokeStyle = col + "0.9)";
      c.lineWidth = 3;
      if (card.kind === "log") {
        c.beginPath();
        c.roundRect(-card.radius * 0.7, -card.radius * 1.6, card.radius * 1.4, card.radius * 3.2, 12);
        c.fill();
        c.stroke();
        c.beginPath();
        c.moveTo(card.radius, 0);
        c.lineTo(card.radius + card.travel * 0.3, 0);
        c.stroke();
      } else {
        c.beginPath();
        c.arc(0, 0, card.radius, 0, Math.PI * 2);
        c.fill();
        c.stroke();
      }
      c.fillStyle = "#fff";
      c.font = `${card.radius * 1.2 | 0}px sans-serif`;
      c.textAlign = "center";
      c.textBaseline = "middle";
      c.globalAlpha = 0.9;
      c.fillText(card.icon, 0, 2);
      c.restore();
    }
  };

  // ../game/js/render/effects.js
  var Effects = class {
    constructor(quality = "medium") {
      this.setQuality(quality);
      this.particles = [];
      this.texts = [];
      this.smokes = [];
      this.rain = [];
      this.raining = false;
      this.rainAmount = 0;
      this.lightning = 0;
      this.nextLightning = 4;
      this.shake = 0;
      this.time = 0;
      this.distantFlash = 0;
      this.nextDistant = 5;
    }
    setQuality(q) {
      this.q = QUALITY[q] || QUALITY.medium;
    }
    /** Traduce eventos de la simulación a efectos. */
    handleEvents(events, world) {
      for (const e of events) {
        switch (e.type) {
          case "shot":
            if (e.kind === "shell") {
              this.burst(e.x, e.y - 14, 8, "#ffb347", 120, 0.3);
              this.smoke(e.x, e.y - 14, 3);
              this.shake = Math.max(this.shake, 3);
            } else this.burst(e.x, e.y - 8, 2, "#ffe680", 60, 0.12);
            break;
          case "hit":
            if (e.kind === "shell") {
              this.burst(e.x, e.y, 22, "#ff8c3a", 220, 0.5);
              this.smoke(e.x, e.y, 5);
              this.shake = Math.max(this.shake, 7);
              this.text(e.x, e.y - 40, `-${e.damage | 0}`, "#ffb347");
            } else if (e.kind === "arrow") {
              this.burst(e.x, e.y, 4, "#ffd9a0", 90, 0.2);
            } else this.burst(e.x, e.y, 3, "#ffe680", 80, 0.15);
            break;
          case "melee":
            this.burst(e.x, e.y, 5, "#ffffff", 100, 0.2);
            break;
          case "logHit":
            this.burst(e.x, e.y, 10, "#b58a4b", 150, 0.4);
            this.shake = Math.max(this.shake, 4);
            break;
          case "logBreak":
            this.burst(e.x, e.y, 14, "#8a6238", 170, 0.6);
            break;
          case "place":
            this.burst(e.x, e.y, 14, "#d9c9a0", 130, 0.5);
            this.ring(e.x, e.y, 60, "#ffffff");
            break;
          case "destroy":
            if (e.type === "destroy" && e.kind === "hero") {
              this.burst(e.x, e.y, 60, "#ff5a3a", 320, 1.2);
              this.burst(e.x, e.y, 30, "#ffd27a", 260, 1);
              this.smoke(e.x, e.y, 14);
              this.shake = 18;
            } else {
              this.burst(e.x, e.y, 26, "#aaa", 200, 0.8);
              this.burst(e.x, e.y, 12, "#ff9a3a", 180, 0.5);
              this.smoke(e.x, e.y, 6);
              this.shake = Math.max(this.shake, 6);
            }
            break;
          case "frenzy":
            this.ring(WORLD.width / 2, WORLD.height / 2, 900, "#ff3b3b");
            this.shake = 10;
            break;
          case "rainStart":
            this.raining = true;
            break;
          case "rainEnd":
            this.raining = false;
            break;
        }
      }
    }
    burst(x, y, n, color, speed, life) {
      n = Math.max(1, Math.round(n * this.q.particles));
      for (let i = 0; i < n; i++) {
        const a = Math.random() * Math.PI * 2, v = speed * (0.3 + Math.random() * 0.7);
        this.particles.push({ x, y, vx: Math.cos(a) * v, vy: Math.sin(a) * v - 40, life, maxLife: life, color, size: 2 + Math.random() * 4, kind: "spark" });
      }
    }
    smoke(x, y, n) {
      n = Math.max(1, Math.round(n * this.q.particles));
      for (let i = 0; i < n; i++) {
        this.particles.push({ x: x + (Math.random() - 0.5) * 20, y, vx: (Math.random() - 0.5) * 30, vy: -20 - Math.random() * 30, life: 1.2 + Math.random(), maxLife: 2, color: "#5a5550", size: 10 + Math.random() * 16, kind: "smoke" });
      }
    }
    ring(x, y, maxR, color) {
      this.particles.push({ x, y, life: 0.6, maxLife: 0.6, maxR, color, kind: "ring" });
    }
    text(x, y, txt, color) {
      this.texts.push({ x, y, txt, color, life: 0.9 });
    }
    update(dt, world) {
      this.time += dt;
      for (const p of this.particles) {
        p.life -= dt;
        if (p.kind === "ring") continue;
        p.x += p.vx * dt;
        p.y += p.vy * dt;
        if (p.kind === "spark") {
          p.vy += 300 * dt;
          p.vx *= 0.96;
        } else {
          p.vx *= 0.98;
          p.size += 12 * dt;
        }
      }
      this.particles = this.particles.filter((p) => p.life > 0);
      for (const t of this.texts) {
        t.life -= dt;
        t.y -= 30 * dt;
      }
      this.texts = this.texts.filter((t) => t.life > 0);
      this.shake = Math.max(0, this.shake - dt * 30);
      if (this.smokes.length < 6 * this.q.particles && Math.random() < dt * 0.5) {
        this.smokes.push({ x: 200 + Math.random() * (WORLD.width - 400), y: Math.random() < 0.5 ? 60 + Math.random() * 100 : WORLD.height - 160 + Math.random() * 100, t: 0, dur: 8 + Math.random() * 8 });
      }
      for (const s of this.smokes) {
        s.t += dt;
        if (Math.random() < dt * 4 * this.q.particles) this.particles.push({ x: s.x, y: s.y, vx: 10 + Math.random() * 10, vy: -15 - Math.random() * 10, life: 2.5, maxLife: 2.5, color: "#3a3733", size: 8 + Math.random() * 10, kind: "smoke" });
      }
      this.smokes = this.smokes.filter((s) => s.t < s.dur);
      this.nextDistant -= dt;
      if (this.nextDistant <= 0) {
        this.distantFlash = 0.25;
        this.nextDistant = 6 + Math.random() * 14;
      }
      this.distantFlash = Math.max(0, this.distantFlash - dt);
      const target = this.raining ? 1 : 0;
      this.rainAmount += (target - this.rainAmount) * Math.min(1, dt * 0.6);
      if (this.rainAmount > 0.01) {
        this.nextLightning -= dt;
        if (this.nextLightning <= 0) {
          this.lightning = 0.5;
          this.nextLightning = 5 + Math.random() * 12;
          this.onThunder && this.onThunder();
        }
      }
      this.lightning = Math.max(0, this.lightning - dt);
    }
    /** Dibujo en coordenadas de mundo (ya transformado). */
    drawWorld(c) {
      for (const p of this.particles) {
        const a = Math.max(0, p.life / p.maxLife);
        if (p.kind === "ring") {
          c.globalAlpha = a;
          c.strokeStyle = p.color;
          c.lineWidth = 3;
          c.beginPath();
          c.arc(p.x, p.y, p.maxR * (1 - a), 0, Math.PI * 2);
          c.stroke();
        } else if (p.kind === "smoke") {
          c.globalAlpha = a * 0.35;
          c.fillStyle = p.color;
          c.beginPath();
          c.arc(p.x, p.y, p.size, 0, Math.PI * 2);
          c.fill();
        } else {
          c.globalAlpha = a;
          c.fillStyle = p.color;
          c.fillRect(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }
      }
      c.globalAlpha = 1;
      c.font = "bold 22px Arial Black, sans-serif";
      c.textAlign = "center";
      for (const t of this.texts) {
        c.globalAlpha = Math.min(1, t.life * 2);
        c.fillStyle = t.color;
        c.strokeStyle = "#000";
        c.lineWidth = 4;
        c.strokeText(t.txt, t.x, t.y);
        c.fillText(t.txt, t.x, t.y);
      }
      c.globalAlpha = 1;
    }
    /** Dibujo en coordenadas de pantalla (lluvia, relámpagos, tintes). */
    drawScreen(c, w, h, world) {
      if (this.distantFlash > 0) {
        c.fillStyle = `rgba(255,220,160,${this.distantFlash * 0.25})`;
        c.fillRect(0, 0, w, h);
      }
      if (this.rainAmount > 0.01) {
        const amt = this.rainAmount;
        c.fillStyle = `rgba(10,16,30,${0.35 * amt})`;
        c.fillRect(0, 0, w, h);
        const n = Math.floor(this.q.rainDrops * amt);
        while (this.rain.length < n) this.rain.push({ x: Math.random() * (w + 200), y: Math.random() * h, l: 10 + Math.random() * 18, s: 700 + Math.random() * 500 });
        if (this.rain.length > n) this.rain.length = n;
        c.strokeStyle = `rgba(190,210,255,${0.45 * amt})`;
        c.lineWidth = 1.2;
        c.beginPath();
        const dt = 1 / 60;
        for (const d of this.rain) {
          d.y += d.s * dt;
          d.x -= d.s * 0.25 * dt;
          if (d.y > h) {
            d.y = -20;
            d.x = Math.random() * (w + 200);
          }
          c.moveTo(d.x, d.y);
          c.lineTo(d.x - d.l * 0.25, d.y + d.l);
        }
        c.stroke();
        c.fillStyle = `rgba(200,220,255,${0.25 * amt})`;
        for (let i = 0; i < n / 6; i++) {
          const x = Math.random() * w, y = Math.random() * h;
          c.beginPath();
          c.ellipse(x, y, 3, 1.2, 0, 0, Math.PI * 2);
          c.fill();
        }
        if (this.lightning > 0) {
          c.fillStyle = `rgba(230,240,255,${Math.min(0.85, this.lightning * 1.6)})`;
          c.fillRect(0, 0, w, h);
        }
      }
      if (world && world.frenzy) {
        const pulse = 0.35 + Math.sin(this.time * 6) * 0.15;
        const g = c.createRadialGradient(w / 2, h / 2, h * 0.45, w / 2, h / 2, w * 0.7);
        g.addColorStop(0, "rgba(255,0,0,0)");
        g.addColorStop(1, `rgba(255,30,30,${pulse})`);
        c.fillStyle = g;
        c.fillRect(0, 0, w, h);
      }
    }
  };

  // ../game/js/audio/audio.js
  var AudioEngine = class {
    constructor(settings) {
      this.settings = settings;
      this.ctx = null;
      this.musicGain = null;
      this.sfxGain = null;
      this.noiseBuffer = null;
      this.seq = null;
      this.rainNode = null;
      this.muted = false;
    }
    /** Crea el contexto en el primer gesto del usuario (requisito de los navegadores). */
    ensure() {
      if (this.ctx) {
        if (this.ctx.state === "suspended") this.ctx.resume();
        return true;
      }
      const AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return false;
      this.ctx = new AC();
      this.master = this.ctx.createGain();
      this.master.connect(this.ctx.destination);
      this.musicGain = this.ctx.createGain();
      this.sfxGain = this.ctx.createGain();
      this.musicGain.connect(this.master);
      this.sfxGain.connect(this.master);
      const comp = this.ctx.createDynamicsCompressor();
      comp.threshold.value = -12;
      comp.ratio.value = 4;
      this.sfxGain.disconnect();
      this.sfxGain.connect(comp);
      comp.connect(this.master);
      const len = this.ctx.sampleRate * 2;
      this.noiseBuffer = this.ctx.createBuffer(1, len, this.ctx.sampleRate);
      const d = this.noiseBuffer.getChannelData(0);
      for (let i = 0; i < len; i++) d[i] = Math.random() * 2 - 1;
      this.applyVolumes();
      return true;
    }
    applyVolumes() {
      if (!this.ctx) return;
      const m = this.muted ? 0 : 1;
      this.musicGain.gain.setTargetAtTime(this.settings.musicVolume * 0.5 * m, this.ctx.currentTime, 0.05);
      this.sfxGain.gain.setTargetAtTime(this.settings.sfxVolume * m, this.ctx.currentTime, 0.05);
    }
    setMuted(v) {
      this.muted = v;
      this.applyVolumes();
    }
    // ------------------------------------------------------------------
    //  Utilidades de síntesis
    // ------------------------------------------------------------------
    _noise(dest, { duration = 0.2, filter = "lowpass", freq = 1e3, freqEnd = null, q = 1, gain = 0.5, attack = 5e-3, when = 0 } = {}) {
      const c = this.ctx, t = c.currentTime + when;
      const src = c.createBufferSource();
      src.buffer = this.noiseBuffer;
      src.loop = true;
      const f = c.createBiquadFilter();
      f.type = filter;
      f.frequency.setValueAtTime(freq, t);
      f.Q.value = q;
      if (freqEnd) f.frequency.exponentialRampToValueAtTime(Math.max(20, freqEnd), t + duration);
      const g = c.createGain();
      g.gain.setValueAtTime(0, t);
      g.gain.linearRampToValueAtTime(gain, t + attack);
      g.gain.exponentialRampToValueAtTime(1e-4, t + duration);
      src.connect(f);
      f.connect(g);
      g.connect(dest);
      src.start(t);
      src.stop(t + duration + 0.05);
    }
    _tone(dest, { type = "sine", freq = 440, freqEnd = null, duration = 0.2, gain = 0.3, attack = 5e-3, when = 0, decay = null } = {}) {
      const c = this.ctx, t = c.currentTime + when;
      const o = c.createOscillator();
      o.type = type;
      o.frequency.setValueAtTime(freq, t);
      if (freqEnd) o.frequency.exponentialRampToValueAtTime(Math.max(20, freqEnd), t + duration);
      const g = c.createGain();
      g.gain.setValueAtTime(0, t);
      g.gain.linearRampToValueAtTime(gain, t + attack);
      g.gain.exponentialRampToValueAtTime(1e-4, t + (decay || duration));
      o.connect(g);
      g.connect(dest);
      o.start(t);
      o.stop(t + (decay || duration) + 0.05);
    }
    // ------------------------------------------------------------------
    //  Efectos de sonido
    // ------------------------------------------------------------------
    sfx(name, opts = {}) {
      var _a;
      if (!this.ctx || this.muted) return;
      const d = this.sfxGain;
      const vol = (_a = opts.volume) != null ? _a : 1;
      switch (name) {
        case "shot":
          this._noise(d, { duration: 0.07, filter: "highpass", freq: 1800, gain: 0.35 * vol });
          this._tone(d, { type: "square", freq: 220, freqEnd: 60, duration: 0.06, gain: 0.12 * vol });
          break;
        case "turret":
          this._noise(d, { duration: 0.05, filter: "bandpass", freq: 2500, q: 2, gain: 0.25 * vol });
          break;
        case "arrow":
          this._noise(d, { duration: 0.25, filter: "bandpass", freq: 3e3, freqEnd: 600, q: 4, gain: 0.3 * vol });
          break;
        case "cannon":
          this._tone(d, { type: "sine", freq: 140, freqEnd: 35, duration: 0.6, gain: 0.9 * vol });
          this._noise(d, { duration: 0.5, filter: "lowpass", freq: 1200, freqEnd: 100, gain: 0.7 * vol });
          break;
        case "explosion":
          this._noise(d, { duration: 0.9, filter: "lowpass", freq: 2500, freqEnd: 80, gain: 0.9 * vol });
          this._tone(d, { type: "sine", freq: 90, freqEnd: 25, duration: 0.8, gain: 0.8 * vol });
          break;
        case "hit":
          this._noise(d, { duration: 0.05, filter: "bandpass", freq: 900, q: 3, gain: 0.2 * vol });
          break;
        case "melee":
          this._noise(d, { duration: 0.09, filter: "lowpass", freq: 700, gain: 0.35 * vol });
          this._tone(d, { type: "triangle", freq: 160, freqEnd: 70, duration: 0.09, gain: 0.25 * vol });
          break;
        case "place":
          this._tone(d, { type: "sine", freq: 110, freqEnd: 50, duration: 0.18, gain: 0.6 * vol });
          this._noise(d, { duration: 0.12, filter: "lowpass", freq: 900, gain: 0.3 * vol });
          break;
        case "log":
          this._noise(d, { duration: 1.2, filter: "lowpass", freq: 300, freqEnd: 120, gain: 0.6 * vol, attack: 0.1 });
          break;
        case "wire":
          this._noise(d, { duration: 0.3, filter: "highpass", freq: 3500, gain: 0.25 * vol });
          break;
        case "click":
          this._tone(d, { type: "square", freq: 1400, duration: 0.04, gain: 0.15 * vol });
          break;
        case "select":
          this._tone(d, { type: "triangle", freq: 660, freqEnd: 990, duration: 0.08, gain: 0.2 * vol });
          break;
        case "error":
          this._tone(d, { type: "sawtooth", freq: 160, freqEnd: 110, duration: 0.18, gain: 0.2 * vol });
          break;
        case "thunder":
          this._noise(d, { duration: 1.8, filter: "lowpass", freq: 600, freqEnd: 60, gain: 0.8 * vol, attack: 0.03 });
          this._tone(d, { type: "sine", freq: 60, freqEnd: 30, duration: 1.5, gain: 0.5 * vol });
          break;
        case "frenzy":
          for (let i = 0; i < 3; i++) this._tone(d, { type: "sawtooth", freq: 300, freqEnd: 900, duration: 0.35, gain: 0.25 * vol, when: i * 0.35 });
          break;
        case "countdown":
          this._tone(d, { type: "square", freq: 880, duration: 0.12, gain: 0.2 * vol });
          break;
        case "go":
          this._tone(d, { type: "square", freq: 1320, duration: 0.35, gain: 0.25 * vol });
          break;
        case "victory":
          [523, 659, 784, 1047, 1319].forEach((f, i) => this._tone(d, { type: "triangle", freq: f, duration: 0.5, gain: 0.35 * vol, when: i * 0.14, decay: 0.9 }));
          break;
        case "defeat":
          [392, 349, 311, 233].forEach((f, i) => this._tone(d, { type: "sawtooth", freq: f, duration: 0.7, gain: 0.25 * vol, when: i * 0.32, decay: 0.9 }));
          break;
        case "heroDeath":
          this._noise(d, { duration: 1.4, filter: "lowpass", freq: 3e3, freqEnd: 60, gain: 1 * vol });
          this._tone(d, { type: "sine", freq: 70, freqEnd: 20, duration: 1.3, gain: 0.9 * vol });
          break;
        case "rainStart":
          this._noise(d, { duration: 0.8, filter: "highpass", freq: 4e3, gain: 0.3 * vol, attack: 0.4 });
          break;
      }
    }
    startRain() {
      if (!this.ctx || this.rainNode) return;
      const c = this.ctx;
      const src = c.createBufferSource();
      src.buffer = this.noiseBuffer;
      src.loop = true;
      const f = c.createBiquadFilter();
      f.type = "bandpass";
      f.frequency.value = 5e3;
      f.Q.value = 0.6;
      const g = c.createGain();
      g.gain.setValueAtTime(1e-4, c.currentTime);
      g.gain.exponentialRampToValueAtTime(0.28, c.currentTime + 2.5);
      src.connect(f);
      f.connect(g);
      g.connect(this.sfxGain);
      src.start();
      this.rainNode = { src, g };
    }
    stopRain() {
      if (!this.rainNode) return;
      const { src, g } = this.rainNode;
      this.rainNode = null;
      g.gain.exponentialRampToValueAtTime(1e-4, this.ctx.currentTime + 2.5);
      src.stop(this.ctx.currentTime + 2.6);
    }
    // ------------------------------------------------------------------
    //  Música: secuenciador por pasos con lookahead
    // ------------------------------------------------------------------
    playMusic(mode = "menu") {
      if (!this.ctx) return;
      if (this.seq && this.seq.mode === mode) return;
      this.stopMusic();
      const bpm = mode === "menu" ? 84 : 126;
      this.seq = { mode, bpm, tempoMult: 1, step: 0, nextTime: this.ctx.currentTime + 0.1, timer: null, bar: 0 };
      this.seq.timer = setInterval(() => this._schedule(), 60);
    }
    setTempoMult(m) {
      if (this.seq) this.seq.tempoMult = m;
    }
    stopMusic() {
      if (!this.seq) return;
      clearInterval(this.seq.timer);
      this.seq = null;
    }
    _schedule() {
      const s = this.seq;
      if (!s) return;
      const c = this.ctx;
      const stepDur = 60 / (s.bpm * s.tempoMult) / 4;
      while (s.nextTime < c.currentTime + 0.25) {
        this._playStep(s, s.step, s.nextTime, stepDur);
        s.nextTime += stepDur;
        s.step++;
        if (s.step % 16 === 0) s.bar++;
      }
    }
    _playStep(s, step, t, stepDur) {
      const c = this.ctx, d = this.musicGain;
      const i = step % 16;
      const when = t - c.currentTime;
      const bar = s.bar % 4;
      const bassNotes = s.mode === "menu" ? [82.41, 82.41, 98, 82.41] : [82.41, 82.41, 98, 110];
      const root = bassNotes[bar];
      if (s.mode === "menu") {
        if (i === 0 || i === 8) this._tone(d, { type: "sine", freq: 70, freqEnd: 35, duration: 0.35, gain: 0.9, when });
        if (i === 4 || i === 12) this._noise(d, { duration: 0.15, filter: "bandpass", freq: 1800, q: 1, gain: 0.18, when });
        if (i % 4 === 0) this._tone(d, { type: "sawtooth", freq: root, duration: stepDur * 4, gain: 0.12, when, attack: 0.05 });
        if (i === 0) {
          this._tone(d, { type: "triangle", freq: root * 2, duration: stepDur * 16, gain: 0.06, when, attack: 0.6 });
          this._tone(d, { type: "triangle", freq: root * 3, duration: stepDur * 16, gain: 0.04, when, attack: 0.8 });
        }
        if (i === 14 && bar === 3) this._noise(d, { duration: 0.6, filter: "lowpass", freq: 500, freqEnd: 80, gain: 0.3, when });
      } else {
        if (i % 4 === 0) this._tone(d, { type: "sine", freq: 75, freqEnd: 38, duration: 0.25, gain: 0.9, when });
        if (i === 4 || i === 12 || i === 15 && bar % 2 === 1) this._noise(d, { duration: 0.14, filter: "bandpass", freq: 2200, q: 1, gain: 0.25, when });
        if (i % 2 === 0) this._noise(d, { duration: 0.03, filter: "highpass", freq: 7e3, gain: 0.08, when });
        if (i % 4 === 2) this._noise(d, { duration: 0.06, filter: "highpass", freq: 5e3, gain: 0.05, when });
        const pattern = [1, 0, 1, 0, 1, 0, 1.5, 0, 1, 0, 1, 0, 1.5, 0, 2, 0];
        if (pattern[i]) this._tone(d, { type: "sawtooth", freq: root * pattern[i], duration: stepDur * 1.8, gain: 0.14, when, attack: 0.01 });
        if (bar % 2 === 0 && (i === 0 || i === 3 || i === 6)) this._tone(d, { type: "square", freq: root * 4 * (i === 6 ? 1.2 : 1), duration: stepDur * 2.5, gain: 0.05, when, attack: 0.02 });
        if (s.tempoMult > 1 && i % 8 === 4) this._tone(d, { type: "square", freq: root * 6, duration: stepDur, gain: 0.04, when });
      }
    }
  };

  // ../game/js/ui/hud.js
  var $ = (id) => document.getElementById(id);
  var Hud = class {
    constructor() {
      this.root = $("hud");
      this.hand = $("hand");
      this.manaFill = $("mana-fill");
      this.manaText = $("mana-text");
      this.timer = $("timer");
      this.phaseFill = $("phase-fill");
      this.phaseLabel = $("phase-label");
      this.hpPlayer = $("hp-player");
      this.hpEnemy = $("hp-enemy");
      this.nextIcon = $("next-icon");
      this.toastEl = $("toast");
      this.bannerEl = $("banner");
      this.cardbar = $("cardbar");
      this.topbar = $("topbar");
      this.handKey = "";
      this.cardEls = /* @__PURE__ */ new Map();
      this.selected = null;
      this.onCardPointerDown = null;
      this.toastTimer = null;
      const marks = document.querySelectorAll("#phase-bar .mark");
      if (marks[0]) marks[0].style.left = `${MATCH.rainStart / MATCH.duration * 100}%`;
      if (marks[1]) marks[1].style.left = `${MATCH.frenzyStart / MATCH.duration * 100}%`;
    }
    show() {
      this.root.classList.remove("hidden");
    }
    hide() {
      this.root.classList.add("hidden");
    }
    get cardbarHeight() {
      return this.cardbar.getBoundingClientRect().height;
    }
    get topbarHeight() {
      return this.topbar.getBoundingClientRect().height;
    }
    select(cardId) {
      this.selected = cardId;
      for (const [id, el] of this.cardEls) el.classList.toggle("selected", id === cardId);
    }
    rebuildHand(world) {
      const hand = world.hands[SIDE_PLAYER];
      const key = hand.join(",");
      if (key === this.handKey) return;
      this.handKey = key;
      this.hand.innerHTML = "";
      this.cardEls.clear();
      for (const id of hand) {
        const card = CARDS[id];
        const el = document.createElement("div");
        el.className = "card";
        el.dataset.card = id;
        el.innerHTML = `<span class="cost">${card.cost}</span><span class="icon">${card.icon}</span><span class="name">${card.name}</span>`;
        el.title = card.desc;
        el.addEventListener("pointerdown", (ev) => this.onCardPointerDown && this.onCardPointerDown(id, ev, el));
        this.hand.appendChild(el);
        this.cardEls.set(id, el);
      }
      if (this.selected && !hand.includes(this.selected)) this.selected = null;
      this.select(this.selected);
    }
    update(world) {
      this.rebuildHand(world);
      const mana = world.mana[SIDE_PLAYER];
      this.manaFill.style.width = `${mana / MANA.max * 100}%`;
      this.manaText.textContent = `${Math.floor(mana)} / ${MANA.max}`;
      for (const [id, el] of this.cardEls) el.classList.toggle("unaffordable", CARDS[id].cost > mana);
      const next = CARDS[world.nextCard(SIDE_PLAYER)];
      this.nextIcon.textContent = next ? next.icon : "";
      const t = Math.min(world.time, MATCH.duration);
      const mm = String(Math.floor(t / 60)).padStart(2, "0"), ss = String(Math.floor(t % 60)).padStart(2, "0");
      this.timer.textContent = `${mm}:${ss}`;
      this.phaseFill.style.width = `${t / MATCH.duration * 100}%`;
      this.phaseLabel.textContent = world.frenzy ? "¡Velocidad x2!" : world.rain ? "Lluvia" : "";
      this.phaseLabel.style.color = world.frenzy ? "#ff5a4e" : "#8fc6ff";
      const [p, a] = world.heroes;
      this.hpPlayer.style.width = `${Math.max(0, p.hp / p.maxHp * 100)}%`;
      this.hpEnemy.style.width = `${Math.max(0, a.hp / a.maxHp * 100)}%`;
    }
    toast(msg, ms = 1400) {
      this.toastEl.textContent = msg;
      this.toastEl.classList.add("show");
      clearTimeout(this.toastTimer);
      this.toastTimer = setTimeout(() => this.toastEl.classList.remove("show"), ms);
    }
    banner(text, color = "#fff") {
      const el = this.bannerEl;
      el.classList.remove("hidden");
      el.style.color = color;
      el.textContent = text;
      el.style.animation = "none";
      void el.offsetWidth;
      el.style.animation = "";
      clearTimeout(this.bannerTimer);
      this.bannerTimer = setTimeout(() => el.classList.add("hidden"), 2600);
    }
  };

  // ../game/js/ui/screens.js
  var $2 = (id) => document.getElementById(id);
  var Screens = class {
    constructor({ settings, onSettingsChange }) {
      this.settings = settings;
      this.onSettingsChange = onSettingsChange;
      this.menu = $2("screen-menu");
      this.settingsScreen = $2("screen-settings");
      this.pause = $2("screen-pause");
      this.end = $2("screen-end");
      this.settingsReturn = null;
      this._bindSettings();
    }
    showMenu() {
      this.menu.classList.remove("hidden");
    }
    hideMenu() {
      this.menu.classList.add("hidden");
    }
    showPause() {
      this.pause.classList.remove("hidden");
    }
    hidePause() {
      this.pause.classList.add("hidden");
    }
    showSettings(returnTo) {
      this.settingsReturn = returnTo;
      this._syncSettingsUI();
      this.settingsScreen.classList.remove("hidden");
    }
    hideSettings() {
      this.settingsScreen.classList.add("hidden");
    }
    get settingsOpen() {
      return !this.settingsScreen.classList.contains("hidden");
    }
    showEnd(kind, reason) {
      this.end.className = `screen overlay ${kind}`;
      $2("end-title").textContent = kind === "victory" ? "¡VICTORIA!" : kind === "defeat" ? "DERROTA" : "EMPATE";
      $2("end-reason").textContent = reason;
      this.end.classList.remove("hidden");
    }
    hideEnd() {
      this.end.classList.add("hidden");
    }
    _bindSettings() {
      const seg = $2("quality-seg");
      seg.querySelectorAll("button").forEach((b) => b.addEventListener("click", () => {
        this.settings.quality = b.dataset.q;
        this._syncSettingsUI();
        this.onSettingsChange("quality");
      }));
      const music = $2("music-vol"), sfx = $2("sfx-vol");
      music.addEventListener("input", () => {
        this.settings.musicVolume = music.value / 100;
        $2("music-val").textContent = `${music.value}%`;
        this.onSettingsChange("music");
      });
      sfx.addEventListener("input", () => {
        this.settings.sfxVolume = sfx.value / 100;
        $2("sfx-val").textContent = `${sfx.value}%`;
        this.onSettingsChange("sfx");
      });
      sfx.addEventListener("change", () => this.onSettingsChange("sfxTest"));
    }
    _syncSettingsUI() {
      $2("quality-seg").querySelectorAll("button").forEach((b) => b.classList.toggle("active", b.dataset.q === this.settings.quality));
      const m = Math.round(this.settings.musicVolume * 100), s = Math.round(this.settings.sfxVolume * 100);
      $2("music-vol").value = m;
      $2("music-val").textContent = `${m}%`;
      $2("sfx-vol").value = s;
      $2("sfx-val").textContent = `${s}%`;
    }
  };

  // ../game/js/storage.js
  var KEY = "defensezone.settings.v1";
  function loadSettings() {
    try {
      const raw = localStorage.getItem(KEY);
      if (raw) return { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
    } catch (e) {
    }
    return { ...DEFAULT_SETTINGS };
  }
  function saveSettings(settings) {
    try {
      localStorage.setItem(KEY, JSON.stringify(settings));
    } catch (e) {
    }
  }

  // ../game/js/platform.js
  var platform = {
    get isAndroid() {
      return typeof window.Android !== "undefined";
    },
    exitApp() {
      if (this.isAndroid && window.Android.exitApp) {
        window.Android.exitApp();
        return;
      }
      window.close();
      alert("Para salir, cierra la pestaña o la aplicación.");
    },
    vibrate(ms) {
      try {
        if (navigator.vibrate) navigator.vibrate(ms);
      } catch (e) {
      }
    }
  };

  // ../game/js/main.js
  var $3 = (id) => document.getElementById(id);
  var STEP = 1 / 60;
  if (typeof CanvasRenderingContext2D !== "undefined" && !CanvasRenderingContext2D.prototype.roundRect) {
    CanvasRenderingContext2D.prototype.roundRect = function(x, y, w, h, r) {
      r = Math.min(typeof r === "number" ? r : r && r[0] || 0, w / 2, h / 2);
      this.moveTo(x + r, y);
      this.arcTo(x + w, y, x + w, y + h, r);
      this.arcTo(x + w, y + h, x, y + h, r);
      this.arcTo(x, y + h, x, y, r);
      this.arcTo(x, y, x + w, y, r);
      this.closePath();
      return this;
    };
  }
  var Game = class {
    constructor() {
      this.settings = loadSettings();
      this.canvas = $3("game");
      this.renderer = new Renderer(this.canvas, this.settings);
      this.fx = new Effects(this.settings.quality);
      this.audio = new AudioEngine(this.settings);
      this.hud = new Hud();
      this.screens = new Screens({ settings: this.settings, onSettingsChange: (k) => this.onSettingsChange(k) });
      this.state = "menu";
      this.world = null;
      this.ai = null;
      this.preview = null;
      this.drag = null;
      this.countdown = 0;
      this.endDelay = 0;
      this.last = performance.now();
      this.acc = 0;
      this.fx.onThunder = () => this.audio.sfx("thunder", { volume: 0.7 });
      this._bind();
      this.resize();
      requestAnimationFrame((t) => this.loop(t));
      window.__dz = this;
    }
    // ------------------------------------------------------------------
    //  Eventos de UI
    // ------------------------------------------------------------------
    _bind() {
      const unlock = () => {
        if (this.audio.ensure()) {
          if (this.state === "menu") this.audio.playMusic("menu");
        }
      };
      window.addEventListener("pointerdown", unlock, { passive: true });
      window.addEventListener("keydown", unlock);
      window.addEventListener("resize", () => this.resize());
      window.addEventListener("orientationchange", () => setTimeout(() => this.resize(), 200));
      document.addEventListener("visibilitychange", () => {
        if (document.hidden && this.state === "playing") this.pauseGame();
      });
      window.addEventListener("androidback", () => this.onBack());
      window.addEventListener("androidpause", () => {
        if (this.state === "playing") this.pauseGame();
      });
      window.addEventListener("keydown", (e) => {
        if (e.key === "Escape") this.onBack();
      });
      const click = (id, fn) => $3(id).addEventListener("click", () => {
        this.audio.sfx("click");
        fn();
      });
      click("btn-start", () => this.startGame());
      click("btn-settings", () => this.screens.showSettings("menu"));
      click("btn-exit", () => platform.exitApp());
      click("btn-settings-back", () => this.closeSettings());
      click("btn-pause", () => this.pauseGame());
      click("btn-resume", () => this.resumeGame());
      click("btn-pause-settings", () => this.screens.showSettings("pause"));
      click("btn-quit", () => this.toMenu());
      click("btn-again", () => this.startGame());
      click("btn-menu", () => this.toMenu());
      this.hud.onCardPointerDown = (cardId, ev, el) => {
        if (this.state !== "playing") return;
        ev.preventDefault();
        el.setPointerCapture(ev.pointerId);
        this.drag = { cardId, el, startX: ev.clientX, startY: ev.clientY, moved: false, id: ev.pointerId };
        const move = (e) => {
          if (!this.drag || e.pointerId !== this.drag.id) return;
          if (!this.drag.moved && Math.hypot(e.clientX - this.drag.startX, e.clientY - this.drag.startY) > 12) {
            this.drag.moved = true;
            el.classList.add("dragging");
            this.hud.select(cardId);
          }
          if (this.drag.moved) this.setPreview(cardId, e.clientX, e.clientY);
        };
        const up = (e) => {
          if (!this.drag || e.pointerId !== this.drag.id) return;
          el.removeEventListener("pointermove", move);
          el.removeEventListener("pointerup", up);
          el.removeEventListener("pointercancel", up);
          el.classList.remove("dragging");
          const d = this.drag;
          this.drag = null;
          if (d.moved) {
            if (e.clientY < window.innerHeight - this.hud.cardbarHeight) this.tryPlace(cardId, e.clientX, e.clientY);
            else {
              this.hud.select(null);
            }
            this.preview = null;
          } else {
            if (this.hud.selected === cardId) this.hud.select(null);
            else {
              this.hud.select(cardId);
              this.audio.sfx("select");
            }
            this.preview = null;
          }
        };
        el.addEventListener("pointermove", move);
        el.addEventListener("pointerup", up);
        el.addEventListener("pointercancel", up);
      };
      this.canvas.addEventListener("pointerdown", (e) => {
        if (this.state !== "playing" || !this.hud.selected) return;
        this.tryPlace(this.hud.selected, e.clientX, e.clientY);
      });
      this.canvas.addEventListener("pointermove", (e) => {
        if (this.state !== "playing" || !this.hud.selected || this.drag) {
          if (!this.drag) this.preview = null;
          return;
        }
        this.setPreview(this.hud.selected, e.clientX, e.clientY);
      });
      this.canvas.addEventListener("pointerleave", () => {
        if (!this.drag) this.preview = null;
      });
    }
    onBack() {
      if (this.screens.settingsOpen) return this.closeSettings();
      if (this.state === "playing") return this.pauseGame();
      if (this.state === "paused") return this.resumeGame();
      if (this.state === "ended") return this.toMenu();
      if (this.state === "menu") platform.exitApp();
    }
    onSettingsChange(kind) {
      saveSettings(this.settings);
      if (kind === "quality") {
        this.renderer.setQuality(this.settings.quality);
        this.fx.setQuality(this.settings.quality);
        this.resize();
      }
      if (kind === "music" || kind === "sfx") this.audio.applyVolumes();
      if (kind === "sfxTest") this.audio.sfx("cannon", { volume: 0.6 });
    }
    closeSettings() {
      this.screens.hideSettings();
    }
    resize() {
      this.renderer.setInsets(0, 0);
      this.renderer.resize();
      if (this.state !== "menu") {
        this.renderer.setInsets(0, this.hud.cardbarHeight);
      }
    }
    // ------------------------------------------------------------------
    //  Flujo de partida
    // ------------------------------------------------------------------
    startGame() {
      this.screens.hideMenu();
      this.screens.hideEnd();
      this.screens.hidePause();
      this.world = new World({ seed: Date.now() % 1e5 | 0 });
      this.ai = new EnemyAI(this.world, { side: SIDE_AI });
      this.fx = new Effects(this.settings.quality);
      this.fx.onThunder = () => this.audio.sfx("thunder", { volume: 0.7 });
      this.hud.handKey = "";
      this.hud.select(null);
      this.hud.show();
      this.hud.update(this.world);
      this.state = "countdown";
      this.countdown = 3.999;
      this.lastCount = 4;
      this.preview = null;
      this.audio.ensure();
      this.audio.playMusic("battle");
      this.audio.setTempoMult(1);
      this.resize();
    }
    pauseGame() {
      if (this.state !== "playing") return;
      this.state = "paused";
      this.screens.showPause();
      this.audio.setMuted(false);
    }
    resumeGame() {
      if (this.state !== "paused") return;
      this.screens.hidePause();
      this.screens.hideSettings();
      this.state = "playing";
      this.last = performance.now();
    }
    toMenu() {
      this.state = "menu";
      this.world = null;
      this.preview = null;
      this.hud.hide();
      this.screens.hidePause();
      this.screens.hideEnd();
      this.screens.hideSettings();
      this.screens.showMenu();
      this.audio.stopRain();
      this.audio.playMusic("menu");
      this.resize();
    }
    finish(result) {
      this.state = "ended";
      this.preview = null;
      this.hud.select(null);
      this.audio.stopMusic();
      this.audio.stopRain();
      const win = result.winner === SIDE_PLAYER, draw = result.winner === null;
      const reasons = {
        hp: win ? "El héroe enemigo ha caído en combate." : "Tu héroe ha caído en combate.",
        goal: win ? "Tu héroe ha alcanzado la base enemiga." : "El héroe enemigo ha llegado a tu base.",
        time: draw ? "Se agotó el tiempo con ambos héroes igualados." : win ? "Se agotó el tiempo: tu héroe estaba más cerca de su objetivo." : "Se agotó el tiempo: el héroe enemigo estaba más cerca de su objetivo."
      };
      if (draw) {
        this.fx.shake = 4;
      } else if (win) {
        this.audio.sfx("victory");
        for (let i = 0; i < 6; i++) setTimeout(() => this.fx && this.fx.burst(300 + Math.random() * 1e3, 150 + Math.random() * 400, 40, ["#ffcc4d", "#7cd36c", "#3fa7ff", "#fff"][i % 4], 300, 1.4), i * 350);
      } else {
        this.audio.sfx("defeat");
        this.fx.shake = 20;
      }
      setTimeout(() => this.screens.showEnd(draw ? "draw" : win ? "victory" : "defeat", reasons[result.reason]), 900);
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
      if (err) {
        this.hud.toast(err);
        this.audio.sfx("error");
        platform.vibrate(30);
        return false;
      }
      this.hud.select(null);
      this.preview = null;
      platform.vibrate(15);
      return true;
    }
    // ------------------------------------------------------------------
    //  Bucle principal
    // ------------------------------------------------------------------
    loop(now) {
      requestAnimationFrame((t) => this.loop(t));
      let dt = Math.min(0.1, (now - this.last) / 1e3);
      this.last = now;
      if (this.state === "menu") {
        this.renderer.drawMenu(dt);
        return;
      }
      if (!this.world) return;
      if (this.state === "countdown") {
        this.countdown -= dt;
        const n = Math.ceil(this.countdown);
        if (n !== this.lastCount) {
          this.lastCount = n;
          if (n > 0) {
            this.hud.banner(String(n), "#ffcc4d");
            this.audio.sfx("countdown");
          } else {
            this.hud.banner("¡A LUCHAR!", "#7cd36c");
            this.audio.sfx("go");
            this.state = "playing";
          }
        }
      }
      if (this.state === "playing") {
        this.acc += dt;
        let steps = 0;
        while (this.acc >= STEP && steps < 4) {
          this.ai.update(STEP);
          this.world.update(STEP);
          this.acc -= STEP;
          steps++;
        }
        this.handleEvents(this.world.drainEvents());
      }
      this.fx.update(dt, this.world);
      if (this.preview) this.preview.ok = !this.world.canPlace(SIDE_PLAYER, this.preview.cardId, this.preview.x, this.preview.y);
      this.renderer.drawGame(this.world, this.fx, this.state === "playing" ? this.preview : null);
      this.hud.update(this.world);
    }
    handleEvents(events) {
      this.fx.handleEvents(events, this.world);
      for (const e of events) {
        switch (e.type) {
          case "shot":
            this.audio.sfx(e.kind === "shell" ? "cannon" : e.kind === "arrow" ? "arrow" : e.owner === "turret" ? "turret" : "shot", { volume: e.owner === "hero" ? 0.5 : 0.7 });
            break;
          case "hit":
            if (e.kind === "shell") this.audio.sfx("explosion", { volume: 0.5 });
            else this.audio.sfx("hit", { volume: 0.4 });
            break;
          case "melee":
            this.audio.sfx("melee", { volume: 0.6 });
            break;
          case "place":
            this.audio.sfx(e.cardId === "log" ? "log" : e.cardId === "wire" ? "wire" : "place");
            if (e.side === SIDE_AI) this.hud.toast(`El enemigo juega ${CARDS[e.cardId].name}`, 1100);
            break;
          case "destroy":
            this.audio.sfx(e.kind === "hero" ? "heroDeath" : "explosion", { volume: e.kind === "hero" ? 1 : 0.7 });
            break;
          case "logHit":
            this.audio.sfx("melee");
            break;
          case "rainStart":
            this.hud.banner("¡Lluvia!", "#8fc6ff");
            this.audio.startRain();
            this.audio.sfx("rainStart");
            break;
          case "rainEnd":
            this.audio.stopRain();
            break;
          case "frenzy":
            this.hud.banner("¡VELOCIDAD x2!", "#ff5a4e");
            this.audio.sfx("frenzy");
            this.audio.setTempoMult(1.3);
            break;
          case "end":
            this.finish(e);
            break;
        }
      }
    }
  };
  new Game();
})();
