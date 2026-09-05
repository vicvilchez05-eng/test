/* ESFshoot — mapa: interior de la nave espacial "Vanguardia", colisiones y texturas procedurales */
window.ESF = window.ESF || {};

ESF.World = (function () {
  const H = 5;              // altura del techo
  const STEP = 0.4;         // altura que se puede "subir" sin saltar
  const solids = [];        // cajas de colisión {x1,z1,x2,z2,y1,y2}
  const wallMeshes = [];    // mallas para raycast de disparos
  let scene = null;
  const T = {};             // texturas
  const lights = [];
  let stripMats = [];

  // ---------------- Texturas procedurales ----------------
  function canvasTex(w, h, draw, repeatX, repeatY) {
    const c = document.createElement('canvas');
    c.width = w; c.height = h;
    draw(c.getContext('2d'), w, h);
    const t = new THREE.CanvasTexture(c);
    t.wrapS = t.wrapT = THREE.RepeatWrapping;
    t.repeat.set(repeatX || 1, repeatY || 1);
    t.anisotropy = 4;
    return t;
  }

  function makeTextures() {
    T.panel = canvasTex(256, 256, (g, w, h) => {
      g.fillStyle = '#1b2530'; g.fillRect(0, 0, w, h);
      for (let i = 0; i < 2; i++) for (let j = 0; j < 2; j++) {
        const x = i * 128 + 6, y = j * 128 + 6, s = 116;
        const grd = g.createLinearGradient(x, y, x + s, y + s);
        grd.addColorStop(0, '#2b3a49'); grd.addColorStop(1, '#17212b');
        g.fillStyle = grd; g.fillRect(x, y, s, s);
        g.strokeStyle = '#0b1219'; g.lineWidth = 3; g.strokeRect(x, y, s, s);
        g.strokeStyle = 'rgba(255,255,255,0.08)'; g.lineWidth = 1; g.strokeRect(x + 4, y + 4, s - 8, s - 8);
        // remaches
        g.fillStyle = '#0d1419';
        [[x + 10, y + 10], [x + s - 10, y + 10], [x + 10, y + s - 10], [x + s - 10, y + s - 10]].forEach(p => { g.beginPath(); g.arc(p[0], p[1], 3, 0, 7); g.fill(); });
      }
      // línea cian
      g.fillStyle = '#19e6ff'; g.fillRect(0, 124, w, 3);
      g.fillStyle = 'rgba(25,230,255,0.25)'; g.fillRect(0, 120, w, 11);
      // suciedad
      for (let i = 0; i < 300; i++) { g.fillStyle = `rgba(0,0,0,${Math.random() * 0.25})`; g.fillRect(Math.random() * w, Math.random() * h, 3, 3); }
    });

    T.floor = canvasTex(256, 256, (g, w, h) => {
      g.fillStyle = '#141b22'; g.fillRect(0, 0, w, h);
      for (let i = 0; i < 4; i++) for (let j = 0; j < 4; j++) {
        const x = i * 64, y = j * 64;
        g.fillStyle = (i + j) % 2 ? '#1a232c' : '#171f27'; g.fillRect(x + 2, y + 2, 60, 60);
        g.strokeStyle = '#0a0f14'; g.lineWidth = 2; g.strokeRect(x + 2, y + 2, 60, 60);
        g.fillStyle = 'rgba(255,255,255,0.05)'; g.fillRect(x + 6, y + 6, 52, 2);
      }
      for (let i = 0; i < 500; i++) { g.fillStyle = `rgba(0,0,0,${Math.random() * 0.3})`; g.fillRect(Math.random() * w, Math.random() * h, 2, 2); }
      // rejilla central
      g.strokeStyle = 'rgba(25,230,255,0.18)'; g.lineWidth = 1;
      g.strokeRect(0.5, 0.5, w - 1, h - 1);
    }, 12, 12);

    T.ceiling = canvasTex(256, 256, (g, w, h) => {
      g.fillStyle = '#0e141a'; g.fillRect(0, 0, w, h);
      g.strokeStyle = '#05080b'; g.lineWidth = 4;
      for (let i = 0; i <= 256; i += 64) { g.beginPath(); g.moveTo(i, 0); g.lineTo(i, h); g.stroke(); g.beginPath(); g.moveTo(0, i); g.lineTo(w, i); g.stroke(); }
      g.fillStyle = '#1a232c';
      for (let i = 0; i < 4; i++) for (let j = 0; j < 4; j++) g.fillRect(i * 64 + 20, j * 64 + 20, 24, 24);
    }, 12, 12);

    T.crate = canvasTex(256, 256, (g, w, h) => {
      g.fillStyle = '#2a3441'; g.fillRect(0, 0, w, h);
      g.strokeStyle = '#111820'; g.lineWidth = 10; g.strokeRect(5, 5, w - 10, h - 10);
      g.strokeStyle = '#3a4756'; g.lineWidth = 6; g.strokeRect(24, 24, w - 48, h - 48);
      g.fillStyle = '#19e6ff'; g.font = 'bold 44px Arial'; g.textAlign = 'center'; g.fillText('ESF', w / 2, h / 2 + 14);
      g.fillStyle = 'rgba(25,230,255,0.5)'; g.fillRect(40, h / 2 + 30, w - 80, 4);
      g.fillStyle = '#ffb347'; g.font = 'bold 14px Arial'; g.fillText('SUMINISTROS · CUBIERTA 7', w / 2, h - 34);
      for (let i = 0; i < 200; i++) { g.fillStyle = `rgba(0,0,0,${Math.random() * 0.3})`; g.fillRect(Math.random() * w, Math.random() * h, 3, 3); }
    });

    T.stars = canvasTex(1024, 512, (g, w, h) => {
      g.fillStyle = '#01030a'; g.fillRect(0, 0, w, h);
      // nebulosa
      for (let i = 0; i < 40; i++) {
        const x = Math.random() * w, y = Math.random() * h, r = 60 + Math.random() * 160;
        const grd = g.createRadialGradient(x, y, 0, x, y, r);
        const col = Math.random() < 0.5 ? '25,120,255' : '120,40,200';
        grd.addColorStop(0, `rgba(${col},0.10)`); grd.addColorStop(1, 'rgba(0,0,0,0)');
        g.fillStyle = grd; g.fillRect(x - r, y - r, r * 2, r * 2);
      }
      for (let i = 0; i < 1400; i++) {
        const s = Math.random();
        g.fillStyle = `rgba(255,255,255,${0.3 + s * 0.7})`;
        const r = s < 0.95 ? 1 : 2;
        g.fillRect(Math.random() * w, Math.random() * h, r, r);
      }
      // planeta
      const px = 760, py = 300, pr = 110;
      const pg = g.createRadialGradient(px - 40, py - 40, 10, px, py, pr);
      pg.addColorStop(0, '#5ec8ff'); pg.addColorStop(0.6, '#1a4f8a'); pg.addColorStop(1, '#040a18');
      g.fillStyle = pg; g.beginPath(); g.arc(px, py, pr, 0, 7); g.fill();
      g.strokeStyle = 'rgba(25,230,255,0.4)'; g.lineWidth = 6; g.beginPath(); g.ellipse(px, py, pr * 1.7, pr * 0.35, -0.3, 0, 7); g.stroke();
    });

    T.holo = canvasTex(256, 128, (g, w, h) => {
      g.fillStyle = '#021a26'; g.fillRect(0, 0, w, h);
      g.strokeStyle = '#19e6ff'; g.lineWidth = 2; g.strokeRect(4, 4, w - 8, h - 8);
      g.fillStyle = '#19e6ff'; g.font = 'bold 16px Arial';
      g.fillText('ESF · SISTEMA DE NAVEGACIÓN', 14, 26);
      g.font = '12px Arial'; g.fillStyle = '#7ff4ff';
      ['CUBIERTA 7  ·  ESTADO: ALERTA ROJA', 'BRECHA DE CASCO: NEGATIVO', 'XENOFORMAS DETECTADAS: 5', 'PROTOCOLO ECLIPSE ACTIVO'].forEach((s, i) => g.fillText(s, 14, 48 + i * 17));
      for (let i = 0; i < 12; i++) { g.fillStyle = `rgba(25,230,255,${0.2 + Math.random() * 0.6})`; g.fillRect(180 + i * 5, 110 - Math.random() * 40, 3, 60); }
    });
  }

  // ---------------- Construcción ----------------
  function box(x1, y1, z1, x2, y2, z2, mat, collide, castShadow) {
    const w = x2 - x1, h = y2 - y1, d = z2 - z1;
    const m = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
    m.position.set((x1 + x2) / 2, (y1 + y2) / 2, (z1 + z2) / 2);
    scene.add(m);
    if (collide !== false) solids.push({ x1, z1, x2, z2, y1, y2 });
    wallMeshes.push(m);
    return m;
  }

  function wallMat(rx, ry) {
    const t = T.panel.clone(); t.needsUpdate = true; t.repeat.set(rx, ry);
    return new THREE.MeshStandardMaterial({ map: t, roughness: 0.7, metalness: 0.5 });
  }

  function build(sc, quality) {
    scene = sc;
    solids.length = 0; wallMeshes.length = 0; lights.length = 0; stripMats = [];
    makeTextures();

    scene.background = new THREE.Color(0x02060c);
    scene.fog = new THREE.Fog(0x03101a, 18, 60);

    // Suelo y techo
    const floor = new THREE.Mesh(new THREE.PlaneGeometry(46, 46), new THREE.MeshStandardMaterial({ map: T.floor, roughness: 0.55, metalness: 0.6 }));
    floor.rotation.x = -Math.PI / 2; scene.add(floor); wallMeshes.push(floor);
    const ceil = new THREE.Mesh(new THREE.PlaneGeometry(46, 46), new THREE.MeshStandardMaterial({ map: T.ceiling, roughness: 0.9, metalness: 0.3 }));
    ceil.rotation.x = Math.PI / 2; ceil.position.y = H; scene.add(ceil); wallMeshes.push(ceil);

    const matWall = wallMat(4, 2);
    const matBig = wallMat(6, 2.5);
    const matCorner = wallMat(6, 2.5);

    // Paredes exteriores (±22)
    box(-23, 0, -23, 23, H, -22, matBig);
    box(-23, 0, 22, 23, H, 23, matBig);
    box(-23, 0, -23, -22, H, 23, matBig);
    box(22, 0, -23, 23, H, 23, matBig);

    // Bloques de esquina (macizos) → dejan un hangar central 20x20 y 4 salas laterales
    box(10, 0, 10, 22, H, 22, matCorner);
    box(-22, 0, 10, -10, H, 22, matCorner);
    box(10, 0, -22, 22, H, -10, matCorner);
    box(-22, 0, -22, -10, H, -10, matCorner);

    // Tabiques entre hangar y salas, dejando puertas de 4 m
    // Este / Oeste
    box(10, 0, -10, 12, H, -2, matWall); box(10, 0, 2, 12, H, 10, matWall);
    box(-12, 0, -10, -10, H, -2, matWall); box(-12, 0, 2, -10, H, 10, matWall);
    // Norte / Sur
    box(-10, 0, 10, -2, H, 12, matWall); box(2, 0, 10, 10, H, 12, matWall);
    box(-10, 0, -12, -2, H, -10, matWall); box(2, 0, -12, 10, H, -10, matWall);
    // Dinteles de las puertas (no colisionan con el jugador; altura > cabeza)
    const lintel = new THREE.MeshStandardMaterial({ color: 0x1a2430, roughness: 0.6, metalness: 0.6 });
    [[10, 12, -2, 2], [-12, -10, -2, 2]].forEach(a => box(a[0], 3.2, a[2], a[1], H, a[3], lintel, false));
    [[-2, 2, 10, 12], [-2, 2, -12, -10]].forEach(a => box(a[0], 3.2, a[2], a[1], H, a[3], lintel, false));
    // Marcos de puerta luminosos
    const frameMat = new THREE.MeshStandardMaterial({ color: 0x19e6ff, emissive: 0x19e6ff, emissiveIntensity: 1.2 });
    [[11, 0, -2.05], [11, 0, 2.05], [-11, 0, -2.05], [-11, 0, 2.05]].forEach(p => { const m = new THREE.Mesh(new THREE.BoxGeometry(2.05, 3.25, 0.08), frameMat); m.position.set(p[0], 1.6, p[2]); scene.add(m); });
    [[-2.05, 0, 11], [2.05, 0, 11], [-2.05, 0, -11], [2.05, 0, -11]].forEach(p => { const m = new THREE.Mesh(new THREE.BoxGeometry(0.08, 3.25, 2.05), frameMat); m.position.set(p[0], 1.6, p[2]); scene.add(m); });

    // Pilares del hangar
    const pillarMat = new THREE.MeshStandardMaterial({ color: 0x2d3947, roughness: 0.5, metalness: 0.8 });
    [[-6, -6], [6, -6], [-6, 6], [6, 6]].forEach(p => {
      const m = new THREE.Mesh(new THREE.CylinderGeometry(0.6, 0.7, H, 12), pillarMat);
      m.position.set(p[0], H / 2, p[1]); scene.add(m); wallMeshes.push(m);
      solids.push({ x1: p[0] - 0.6, z1: p[1] - 0.6, x2: p[0] + 0.6, z2: p[1] + 0.6, y1: 0, y2: H });
      const ring = new THREE.Mesh(new THREE.TorusGeometry(0.72, 0.05, 8, 24), frameMat);
      ring.rotation.x = Math.PI / 2; ring.position.set(p[0], 2.6, p[1]); scene.add(ring);
    });

    // Plataforma central elevada (se puede subir de un salto)
    const platMat = new THREE.MeshStandardMaterial({ map: T.floor, roughness: 0.5, metalness: 0.7 });
    box(-3, 0, -3, 3, 0.6, 3, platMat);
    const platEdge = new THREE.Mesh(new THREE.BoxGeometry(6.2, 0.05, 6.2), frameMat);
    platEdge.position.set(0, 0.6, 0); scene.add(platEdge);
    const core = new THREE.Mesh(new THREE.CylinderGeometry(0.5, 0.7, 1.6, 10), new THREE.MeshStandardMaterial({ color: 0x0b2a3a, emissive: 0x19e6ff, emissiveIntensity: 0.6, roughness: 0.3, metalness: 0.9 }));
    core.position.set(0, 1.4, 0); scene.add(core); wallMeshes.push(core);
    solids.push({ x1: -0.7, z1: -0.7, x2: 0.7, z2: 0.7, y1: 0.6, y2: 2.2 });

    // Cajas de suministros
    const crateMat = new THREE.MeshStandardMaterial({ map: T.crate, roughness: 0.6, metalness: 0.4 });
    const crates = [
      [-8, -1, 1.2], [-8, 1.5, 1.2], [8, 0, 1.6], [0, 8, 1.2], [1.4, 8, 1.2], [0, 8, 1.2, 1.2], [0, -8, 1.6],
      [-4, 4, 1.2], [4, -4, 1.2], [4, -4, 1.2, 1.2],
      [16, -6, 1.4], [17.5, -6, 1.4], [16, 6, 1.6], [15, 0, 1.2], [19, 3, 1.2],
      [-16, 6, 1.4], [-17.5, 6, 1.4], [-16, -6, 1.6], [-15, 0, 1.2], [-19, -3, 1.2],
      [6, 16, 1.4], [-6, 16, 1.4], [0, 15, 1.2], [0, 15, 1.2, 1.2], [3, 19, 1.2],
      [6, -16, 1.4], [-6, -16, 1.4], [0, -15, 1.2], [-3, -19, 1.2]
    ];
    crates.forEach(c => {
      const s = c[2], y0 = c[3] || 0;
      const m = box(c[0] - s / 2, y0, c[1] - s / 2, c[0] + s / 2, y0 + s, c[1] + s / 2, crateMat);
      m.rotation.y = 0;
    });

    // Ventanas al espacio en las salas laterales
    const starMat = new THREE.MeshBasicMaterial({ map: T.stars });
    const winFrame = new THREE.MeshStandardMaterial({ color: 0x0f161d, roughness: 0.5, metalness: 0.8 });
    function windowAt(x, z, rotY, w) {
      const g = new THREE.Group();
      const pane = new THREE.Mesh(new THREE.PlaneGeometry(w, 2.4), starMat);
      pane.position.y = 2.4; g.add(pane);
      const fr = new THREE.Mesh(new THREE.BoxGeometry(w + 0.4, 2.8, 0.12), winFrame); fr.position.set(0, 2.4, -0.07); g.add(fr);
      const bar = new THREE.Mesh(new THREE.BoxGeometry(0.1, 2.4, 0.06), winFrame); bar.position.set(0, 2.4, 0.03); g.add(bar);
      const glow = new THREE.Mesh(new THREE.BoxGeometry(w + 0.4, 0.06, 0.06), frameMat); glow.position.set(0, 1.0, 0.03); g.add(glow);
      g.position.set(x, 0, z); g.rotation.y = rotY; scene.add(g);
    }
    windowAt(21.9, 0, -Math.PI / 2, 12);
    windowAt(-21.9, 0, Math.PI / 2, 12);
    windowAt(0, 21.9, Math.PI, 12);
    windowAt(0, -21.9, 0, 12);

    // Pantallas holográficas
    const holoMat = new THREE.MeshBasicMaterial({ map: T.holo, transparent: true, opacity: 0.9 });
    [[-9.9, 6, Math.PI / 2], [9.9, -6, -Math.PI / 2], [6, 9.9, Math.PI], [-6, -9.9, 0]].forEach(p => {
      const m = new THREE.Mesh(new THREE.PlaneGeometry(2.4, 1.2), holoMat);
      m.position.set(p[0], 2, p[1]); m.rotation.y = p[2]; scene.add(m);
    });

    // Tuberías junto al techo
    const pipeMat = new THREE.MeshStandardMaterial({ color: 0x3a4756, roughness: 0.4, metalness: 0.9 });
    for (let i = 0; i < 2; i++) {
      const p1 = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 20, 8), pipeMat);
      p1.rotation.z = Math.PI / 2; p1.position.set(0, H - 0.4 - i * 0.3, -9.6 + i * 0.3); scene.add(p1);
      const p2 = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 20, 8), pipeMat);
      p2.rotation.x = Math.PI / 2; p2.position.set(9.6 - i * 0.3, H - 0.4 - i * 0.3, 0); scene.add(p2);
    }

    // Tiras de luz en el suelo y techo
    const stripMat = new THREE.MeshBasicMaterial({ color: 0x19e6ff });
    stripMats.push(stripMat);
    function strip(x1, z1, x2, z2, y) {
      const m = new THREE.Mesh(new THREE.BoxGeometry(Math.max(0.06, x2 - x1), 0.04, Math.max(0.06, z2 - z1)), stripMat);
      m.position.set((x1 + x2) / 2, y, (z1 + z2) / 2); scene.add(m);
    }
    strip(-9.9, -9.9, 9.9, -9.84, 0.02); strip(-9.9, 9.84, 9.9, 9.9, 0.02);
    strip(-9.9, -9.9, -9.84, 9.9, 0.02); strip(9.84, -9.9, 9.9, 9.9, 0.02);
    // Techo: barras luminosas
    const lampMat = new THREE.MeshBasicMaterial({ color: 0xdff9ff });
    const lampPositions = [[0, 0], [-16, 0], [16, 0], [0, -16], [0, 16], [-6, 0], [6, 0], [0, 6], [0, -6]];
    lampPositions.forEach(p => {
      const m = new THREE.Mesh(new THREE.BoxGeometry(3, 0.08, 0.5), lampMat);
      m.position.set(p[0], H - 0.05, p[1]); scene.add(m);
    });

    // ---------------- Luces ----------------
    scene.add(new THREE.HemisphereLight(0x8fd8ff, 0x2a3440, 1.3));
    scene.add(new THREE.AmbientLight(0x4a6a80, 1.5));
    const lightDefs = [[0, 0, 0x9fe8ff, 120], [16, 0, 0x7ad4ff, 90], [-16, 0, 0x7ad4ff, 90], [0, 16, 0xff9a6a, 80], [0, -16, 0x8affc0, 80]];
    const count = quality === 'low' ? 1 : (quality === 'med' ? 3 : 5);
    lightDefs.slice(0, count).forEach(l => {
      const pl = new THREE.PointLight(l[2], l[3], 30, 1.6);
      pl.position.set(l[0], H - 0.6, l[1]); scene.add(pl); lights.push(pl);
    });
    const dir = new THREE.DirectionalLight(0xbfefff, 1.2);
    dir.position.set(4, 10, 6); scene.add(dir);
  }

  // ---------------- Colisiones ----------------
  function clamp(v, a, b) { return v < a ? a : v > b ? b : v; }

  // Empuja `pos` (x,z) fuera de los sólidos para un cilindro de radio r, con pies en feetY y altura h
  function resolve(pos, r, feetY, h) {
    for (let i = 0; i < solids.length; i++) {
      const s = solids[i];
      if (s.y1 >= feetY + h || s.y2 <= feetY + STEP) continue;
      const cx = clamp(pos.x, s.x1, s.x2), cz = clamp(pos.z, s.z1, s.z2);
      const dx = pos.x - cx, dz = pos.z - cz;
      const d2 = dx * dx + dz * dz;
      if (d2 >= r * r) continue;
      if (d2 > 1e-8) {
        const d = Math.sqrt(d2), push = r - d;
        pos.x += dx / d * push; pos.z += dz / d * push;
      } else {
        // centro dentro de la caja: salir por el lado más cercano
        const l = pos.x - s.x1, rt = s.x2 - pos.x, f = pos.z - s.z1, b = s.z2 - pos.z;
        const m = Math.min(l, rt, f, b);
        if (m === l) pos.x = s.x1 - r; else if (m === rt) pos.x = s.x2 + r; else if (m === f) pos.z = s.z1 - r; else pos.z = s.z2 + r;
      }
    }
    pos.x = clamp(pos.x, -21.5, 21.5); pos.z = clamp(pos.z, -21.5, 21.5);
  }

  // Altura del suelo bajo un cilindro (para poder subirse a cajas/plataforma)
  function groundAt(x, z, r, feetY) {
    let g = 0;
    const rr = r * 0.6;
    for (let i = 0; i < solids.length; i++) {
      const s = solids[i];
      if (s.y2 > feetY + STEP || s.y2 <= g) continue;
      if (x + rr < s.x1 || x - rr > s.x2 || z + rr < s.z1 || z - rr > s.z2) continue;
      g = s.y2;
    }
    return g;
  }

  // Línea de visión 3D entre dos puntos (segmento vs AABB)
  function lineOfSight(a, b) {
    const dx = b.x - a.x, dy = b.y - a.y, dz = b.z - a.z;
    for (let i = 0; i < solids.length; i++) {
      const s = solids[i];
      let t0 = 0, t1 = 1;
      let ok = true;
      const axes = [[a.x, dx, s.x1, s.x2], [a.y, dy, s.y1, s.y2], [a.z, dz, s.z1, s.z2]];
      for (let k = 0; k < 3; k++) {
        const o = axes[k][0], d = axes[k][1], lo = axes[k][2], hi = axes[k][3];
        if (Math.abs(d) < 1e-9) { if (o < lo || o > hi) { ok = false; break; } continue; }
        let ta = (lo - o) / d, tb = (hi - o) / d;
        if (ta > tb) { const tmp = ta; ta = tb; tb = tmp; }
        if (ta > t0) t0 = ta;
        if (tb < t1) t1 = tb;
        if (t0 > t1) { ok = false; break; }
      }
      if (ok) return false; // el segmento cruza la caja
    }
    return true;
  }

  // Punto de aparición aleatorio para enemigos (salas laterales), lejos del jugador
  const enemySpawns = [[16, -5], [17, 5], [-16, 5], [-17, -5], [5, 16], [-5, 17], [5, -17], [-5, -16], [19, 0], [-19, 0], [0, 19], [0, -19]];
  const playerSpawns = [[-2.2, 0.6, 2.2], [-8, 0, 8], [8, 0, -8], [16, 0, 0], [-16, 0, 0], [0, 0, 16], [0, 0, -16]];

  function pickEnemySpawn(playerPos, others) {
    let best = null, bestScore = -1;
    for (let i = 0; i < 6; i++) {
      const s = enemySpawns[Math.floor(Math.random() * enemySpawns.length)];
      let score = Math.hypot(s[0] - playerPos.x, s[1] - playerPos.z);
      for (const o of others) score = Math.min(score, Math.hypot(s[0] - o.x, s[1] - o.z) * 2);
      if (score > bestScore) { bestScore = score; best = s; }
    }
    return { x: best[0], z: best[1] };
  }

  function pickPlayerSpawn(enemies) {
    let best = playerSpawns[0], bestD = -1;
    for (const s of playerSpawns) {
      let d = 1e9;
      for (const e of enemies) if (e.alive) d = Math.min(d, Math.hypot(s[0] - e.pos.x, s[2] - e.pos.z));
      d += Math.random() * 3;
      if (d > bestD) { bestD = d; best = s; }
    }
    return new THREE.Vector3(best[0], best[1], best[2]);
  }

  function update(t) {
    // parpadeo sutil de las tiras luminosas
    const k = 0.85 + 0.15 * Math.sin(t * 3) * Math.sin(t * 7.3);
    for (const m of stripMats) m.color.setRGB(0.1 * k, 0.9 * k, 1.0 * k);
  }

  return { H, STEP, solids, wallMeshes, build, resolve, groundAt, lineOfSight, pickEnemySpawn, pickPlayerSpawn, update };
})();
