// =====================================================================
//  DEFENSE ZONE — Configuración y balance del juego
//  Todos los números "tuneables" viven aquí. Ver handoff.md para el
//  razonamiento detrás de cada valor.
// =====================================================================

export const WORLD = {
  width: 1600,          // ancho del mapa en unidades de mundo
  height: 720,          // alto del mapa
  laneTop: 200,         // borde superior de la pista transitable
  laneBottom: 620,      // borde inferior de la pista
  spawnZone: 190,       // ancho de la zona de aparición / meta en cada extremo
  goalMargin: 30,       // distancia al borde para considerar "llegó a la meta"
};

export const MATCH = {
  duration: 15 * 60,    // 15 minutos de partida
  rainStart: 6 * 60,    // la lluvia empieza a los 6:00
  rainDuration: 2 * 60, // ...y dura 2 minutos
  frenzyStart: 9 * 60,  // a los 9:00 se duplica la velocidad de todo
  frenzyMultiplier: 2,
};

export const MANA = {
  max: 10,
  start: 5,
  regenPerSecond: 1 / 3.0,   // "medianamente lenta": 1 maná cada 3 s (Clash Royale usa 2,8 s)
  aiRegenBonus: 1.0,         // la IA usa la misma regeneración que el jugador
};

export const HERO = {
  radius: 50,               // notablemente más grande que cualquier estructura (~2x)
  maxHp: 12000,
  regenPerSecond: 10,       // regeneración pasiva: evita que un bloqueo largo sea mortal por sí solo
  speed: 11,                // unidades / segundo (cruce sin oposición ≈ 1:55)
  attackRange: 190,
  attackDamage: 14,
  attackInterval: 0.4,      // segundos entre disparos (35 DPS contra el héroe rival)
  structureDamageMult: 5,   // los héroes derriban estructuras muy rápido (175 DPS)
};

// Cartas disponibles. Coste de maná entre 2 y 6.
// "kind": structure = edificio con vida; log = proyectil rodante; wire = zona de área.
export const CARDS = {
  log: {
    id: 'log', name: 'Tronco', cost: 2, kind: 'log', icon: '🪵',
    desc: 'Rueda hacia el enemigo y empuja a su héroe hacia atrás.',
    speed: 220, travel: 420, push: 130, radius: 30, damage: 60, structureDamage: 150,
  },
  wire: {
    id: 'wire', name: 'Alambre de espino', cost: 3, kind: 'wire', icon: '🕸️',
    desc: 'Zona que ralentiza al héroe enemigo y le hace daño mientras la cruza.',
    radius: 90, duration: 20, slow: 0.45, dps: 6,
  },
  wall: {
    id: 'wall', name: 'Muro', cost: 3, kind: 'structure', icon: '🧱',
    desc: 'Bloquea el paso del héroe enemigo hasta que lo derriba.',
    hp: 1800, radius: 26, blocks: true,
  },
  turret: {
    id: 'turret', name: 'Torreta', cost: 4, kind: 'structure', icon: '🔫',
    desc: 'Ametralladora automática de corto alcance.',
    hp: 750, radius: 22, range: 240, damage: 8, interval: 0.5, projectileSpeed: 700,
  },
  archer: {
    id: 'archer', name: 'Torre de arquero', cost: 5, kind: 'structure', icon: '🏹',
    desc: 'Arquero en lo alto: gran alcance y daño constante.',
    hp: 800, radius: 24, range: 300, damage: 24, interval: 1.2, projectileSpeed: 520,
  },
  cannon: {
    id: 'cannon', name: 'Cañón', cost: 6, kind: 'structure', icon: '💣',
    desc: 'Disparo pesado y lento que además empuja al enemigo.',
    hp: 900, radius: 26, range: 290, damage: 60, interval: 2.4, projectileSpeed: 420, knockback: 12,
  },
};

export const DECK_ORDER = ['wall', 'turret', 'log', 'archer', 'wire', 'cannon'];
export const HAND_SIZE = 4;

export const PLACEMENT = {
  minDistanceToEnemySpawn: 300, // no se puede construir pegado a la zona de aparición del enemigo (su base)
  minDistanceToOwnSpawn: 0,
  structureSpacing: 8,          // separación mínima entre estructuras (bordes)
};

export const AI = {
  thinkInterval: 1.2,
  minSaveMana: 3,        // la IA intenta guardar algo de maná para reaccionar
  reactDistance: 520,    // distancia a la que la IA considera "amenaza" al héroe rival
  aggression: 0.55,      // probabilidad de jugar carta agresiva vs defensiva
};

export const QUALITY = {
  low:    { particles: 0.35, rainDrops: 120, shadows: false, dprCap: 1.0, decoDensity: 0.5 },
  medium: { particles: 0.7,  rainDrops: 260, shadows: true,  dprCap: 1.5, decoDensity: 0.8 },
  high:   { particles: 1.0,  rainDrops: 420, shadows: true,  dprCap: 2.0, decoDensity: 1.0 },
};

export const DEFAULT_SETTINGS = {
  quality: 'medium',
  musicVolume: 0.6,
  sfxVolume: 0.8,
};
