/* ESFshoot — enemigos alienígenas (plasma / láser), IA y proyectiles */
window.ESF = window.ESF || {};

ESF.Enemies = (function () {
  const TYPES = {
    plasma: { name: 'Xeno de plasma', skin: 0x2e8a5c, skin2: 0x1c5a3c, core: 0x5cff9d, hp: 70, speed: 2.6, range: [4.5, 9], cooldown: 2.4, burst: 3, burstGap: 0.16, projSpeed: 15, dmg: 11, spread: 0.06 },
    laser: { name: 'Xeno láser', skin: 0x6a2e8a, skin2: 0x3e1c5a, core: 0xff3c5a, hp: 55, speed: 3.2, range: [7, 13], cooldown: 1.6, burst: 1, burstGap: 0, projSpeed: 40, dmg: 16, spread: 0.035 }
  };
  const list = [];
  const hitboxes = [];
  const projectiles = [];
  let scene = null;
  let nextId = 1;
  const respawnQueue = [];
  const tmp = new THREE.Vector3(), tmp2 = new THREE.Vector3();
  let plasmaTex = null;

  function init(sc) { scene = sc; plasmaTex = ESF.FX.radialTex('rgba(255,255,255,1)', 'rgba(92,255,157,0)'); reset(); }

  function reset() {
    for (const e of list) scene.remove(e.group);
    for (const p of projectiles) scene.remove(p.mesh);
    list.length = 0; hitboxes.length = 0; projectiles.length = 0; respawnQueue.length = 0; nextId = 1;
  }

  // ---------------- Modelo del alien ----------------
  function buildModel(e) {
    const T = TYPES[e.type];
    const skin = new THREE.MeshStandardMaterial({ color: T.skin, roughness: 0.55, metalness: 0.25, emissive: 0x000000 });
    const skin2 = new THREE.MeshStandardMaterial({ color: T.skin2, roughness: 0.6, metalness: 0.3, emissive: 0x000000 });
    const core = new THREE.MeshStandardMaterial({ color: T.core, emissive: T.core, emissiveIntensity: 1.6 });
    const armor = new THREE.MeshStandardMaterial({ color: 0x2a3441, roughness: 0.4, metalness: 0.9 });
    e.mats = [skin, skin2, core, armor];
    const g = new THREE.Group();

    // torso
    const torso = new THREE.Mesh(new THREE.CylinderGeometry(0.22, 0.3, 0.75, 10), skin); torso.position.y = 1.05; g.add(torso);
    const chest = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.34, 0.36), armor); chest.position.set(0, 1.2, 0.02); g.add(chest);
    const coreM = new THREE.Mesh(new THREE.SphereGeometry(0.09, 10, 8), core); coreM.position.set(0, 1.2, 0.2); g.add(coreM);
    const pelvis = new THREE.Mesh(new THREE.SphereGeometry(0.24, 10, 8), skin2); pelvis.position.y = 0.72; pelvis.scale.set(1, 0.6, 0.8); g.add(pelvis);
    // cabeza alargada
    const head = new THREE.Group(); head.position.y = 1.62;
    const skull = new THREE.Mesh(new THREE.SphereGeometry(0.2, 12, 10), skin); skull.scale.set(0.9, 1.15, 1.3); skull.position.z = -0.08; head.add(skull);
    const crest = new THREE.Mesh(new THREE.ConeGeometry(0.1, 0.5, 8), skin2); crest.rotation.x = -Math.PI / 2 - 0.3; crest.position.set(0, 0.1, -0.35); head.add(crest);
    const eyeMat = new THREE.MeshStandardMaterial({ color: T.core, emissive: T.core, emissiveIntensity: 2.2 });
    [-0.08, 0.08].forEach(x => { const eye = new THREE.Mesh(new THREE.SphereGeometry(0.045, 8, 6), eyeMat); eye.position.set(x, 0.02, 0.17); eye.scale.set(1, 0.6, 1); head.add(eye); });
    const jaw = new THREE.Mesh(new THREE.BoxGeometry(0.16, 0.06, 0.16), skin2); jaw.position.set(0, -0.16, 0.06); head.add(jaw);
    g.add(head);
    // piernas
    const legs = [];
    [-0.14, 0.14].forEach(x => {
      const hip = new THREE.Group(); hip.position.set(x, 0.72, 0);
      const thigh = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.06, 0.42, 8), skin); thigh.position.y = -0.21; hip.add(thigh);
      const knee = new THREE.Group(); knee.position.y = -0.42;
      const shin = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.05, 0.36, 8), skin2); shin.position.y = -0.18; knee.add(shin);
      const foot = new THREE.Mesh(new THREE.BoxGeometry(0.12, 0.06, 0.26), armor); foot.position.set(0, -0.36, 0.06); knee.add(foot);
      hip.add(knee); g.add(hip); legs.push({ hip, knee });
    });
    // brazos
    const arms = [];
    [-0.3, 0.3].forEach((x, i) => {
      const sh = new THREE.Group(); sh.position.set(x, 1.32, 0);
      const upper = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.05, 0.36, 8), skin); upper.position.y = -0.18; sh.add(upper);
      const elbow = new THREE.Group(); elbow.position.y = -0.36;
      const fore = new THREE.Mesh(new THREE.CylinderGeometry(0.05, 0.045, 0.34, 8), skin2); fore.position.y = -0.17; elbow.add(fore);
      const claw = new THREE.Mesh(new THREE.ConeGeometry(0.05, 0.14, 6), skin2); claw.position.y = -0.4; claw.rotation.x = Math.PI; elbow.add(claw);
      sh.add(elbow); g.add(sh); arms.push({ sh, elbow });
    });
    // arma (brazo derecho)
    // (el antebrazo apunta hacia -y local, que al apuntar equivale a "hacia delante")
    const gun = new THREE.Group();
    const gunBody = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.5, 0.12), armor); gunBody.position.y = -0.1; gun.add(gunBody);
    const gunTop = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.34, 0.06), skin2); gunTop.position.set(0, -0.08, 0.08); gun.add(gunTop);
    const tip = new THREE.Mesh(e.type === 'plasma' ? new THREE.SphereGeometry(0.06, 8, 6) : new THREE.CylinderGeometry(0.03, 0.03, 0.2, 8), core);
    tip.position.set(0, -0.4, 0); gun.add(tip);
    gun.position.set(0, -0.3, 0.06);
    arms[1].elbow.add(gun);
    e.gunTip = tip;

    // hitboxes invisibles
    const hbMat = new THREE.MeshBasicMaterial({ visible: false });
    const bodyHB = new THREE.Mesh(new THREE.BoxGeometry(0.75, 1.45, 0.6), hbMat); bodyHB.position.y = 0.72; bodyHB.userData = { enemy: e, part: 'body' }; g.add(bodyHB);
    const headHB = new THREE.Mesh(new THREE.BoxGeometry(0.42, 0.45, 0.6), hbMat); headHB.position.set(0, 1.64, -0.05); headHB.userData = { enemy: e, part: 'head' }; g.add(headHB);
    e.hb = [bodyHB, headHB];
    hitboxes.push(bodyHB, headHB);

    e.parts = { head, legs, arms, torso, gun };
    e.group = g;
    scene.add(g);
  }

  function spawn(type, x, z) {
    const e = {
      id: nextId++, type, name: TYPES[type].name + ' ' + String.fromCharCode(0x3b1 + (nextId % 12)), // α β γ…
      pos: new THREE.Vector3(x, 0, z), yaw: 0, hp: TYPES[type].hp, alive: true,
      shootCd: 1 + Math.random() * 2, burstLeft: 0, burstT: 0, walkT: 0, moving: false,
      strafeDir: Math.random() < 0.5 ? -1 : 1, strafeT: 0, avoidT: 0, avoidDir: 0, hitT: 0, dieT: -1, spawnT: 0, lastPos: new THREE.Vector3(x, 0, z), stuckT: 0
    };
    buildModel(e);
    e.group.position.copy(e.pos);
    e.group.scale.setScalar(0.01);
    list.push(e);
    ESF.FX.burst(new THREE.Vector3(x, 1, z), TYPES[type].core, 1.5, 0.5);
    return e;
  }

  function spawnWave(n, playerPos) {
    for (let i = 0; i < n; i++) {
      const s = ESF.World.pickEnemySpawn(playerPos, list.map(e => e.pos));
      spawn(i % 3 === 2 ? 'laser' : (Math.random() < 0.5 ? 'laser' : 'plasma'), s.x, s.z);
    }
  }

  function removeEnemy(e) {
    scene.remove(e.group);
    for (const hb of e.hb) { const i = hitboxes.indexOf(hb); if (i >= 0) hitboxes.splice(i, 1); }
    const i = list.indexOf(e); if (i >= 0) list.splice(i, 1);
  }

  // Daño recibido. Devuelve true si ha muerto
  function damage(e, amount, part) {
    if (!e.alive) return false;
    e.hp -= amount;
    e.hitT = 0.12;
    if (e.hp <= 0) {
      e.alive = false; e.dieT = 0;
      for (const hb of e.hb) { const i = hitboxes.indexOf(hb); if (i >= 0) hitboxes.splice(i, 1); }
      e.hb.forEach(h => { h.userData.enemy = null; });
      const T = TYPES[e.type];
      ESF.FX.gibs(new THREE.Vector3(e.pos.x, e.pos.y + 1, e.pos.z), T.skin);
      ESF.FX.burst(new THREE.Vector3(e.pos.x, e.pos.y + 1.1, e.pos.z), T.core, 2.2, 0.5);
      ESF.FX.sparks(new THREE.Vector3(e.pos.x, e.pos.y + 1.1, e.pos.z), T.core, 14, 5);
      respawnQueue.push({ t: 4 + Math.random() * 2, type: Math.random() < 0.5 ? 'plasma' : 'laser' });
      return true;
    }
    return false;
  }

  // ---------------- Proyectiles ----------------
  function fireProjectile(e, playerEye) {
    const T = TYPES[e.type];
    e.gunTip.getWorldPosition(tmp);
    const dir = tmp2.copy(playerEye).sub(tmp);
    // predicción ligera + dispersión
    dir.y -= 0.15;
    const dist = dir.length(); dir.normalize();
    const sp = T.spread * (0.5 + Math.min(1, dist / 15));
    dir.x += (Math.random() - 0.5) * sp; dir.y += (Math.random() - 0.5) * sp * 0.6; dir.z += (Math.random() - 0.5) * sp;
    dir.normalize();
    let mesh;
    if (e.type === 'plasma') {
      mesh = new THREE.Group();
      const ball = new THREE.Mesh(new THREE.SphereGeometry(0.13, 8, 6), new THREE.MeshBasicMaterial({ color: 0xbaffd4 }));
      const glow = new THREE.Sprite(new THREE.SpriteMaterial({ map: plasmaTex, color: 0x5cff9d, transparent: true, blending: THREE.AdditiveBlending, depthWrite: false }));
      glow.scale.set(0.7, 0.7, 0.7);
      mesh.add(ball); mesh.add(glow);
      ESF.Audio.plasma();
    } else {
      mesh = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.05, 1.1), new THREE.MeshBasicMaterial({ color: 0xff6a80 }));
      mesh.quaternion.setFromUnitVectors(new THREE.Vector3(0, 0, -1), dir);
      ESF.Audio.laser();
    }
    mesh.position.copy(tmp);
    scene.add(mesh);
    projectiles.push({ mesh, vel: dir.clone().multiplyScalar(T.projSpeed), type: e.type, dmg: T.dmg, life: 3, owner: e, prev: tmp.clone() });
    ESF.FX.burst(tmp.clone(), T.core, 0.6, 0.12);
  }

  function updateProjectiles(dt, player, cb) {
    const pc = tmp.set(player.pos.x, player.pos.y + player.height * 0.5, player.pos.z);
    for (let i = projectiles.length - 1; i >= 0; i--) {
      const p = projectiles[i];
      p.life -= dt;
      p.prev.copy(p.mesh.position);
      p.mesh.position.addScaledVector(p.vel, dt);
      if (p.type === 'plasma') p.mesh.rotation.y += dt * 6;
      let dead = p.life <= 0;
      // impacto contra el jugador (cápsula aproximada)
      if (!dead && player.alive) {
        const dx = p.mesh.position.x - pc.x, dz = p.mesh.position.z - pc.z;
        const dy = p.mesh.position.y - pc.y;
        if (dx * dx + dz * dz < 0.42 * 0.42 && Math.abs(dy) < player.height * 0.55 + 0.2) {
          cb.onPlayerHit(p.dmg, p.mesh.position.clone(), p.owner);
          ESF.FX.burst(p.mesh.position.clone(), p.type === 'plasma' ? 0x5cff9d : 0xff3c5a, 0.9, 0.2);
          dead = true;
        }
      }
      // impacto contra paredes
      if (!dead && !ESF.World.lineOfSight(p.prev, p.mesh.position)) {
        ESF.FX.sparks(p.mesh.position.clone(), p.type === 'plasma' ? 0x5cff9d : 0xff3c5a, 6, 3);
        ESF.FX.burst(p.mesh.position.clone(), p.type === 'plasma' ? 0x5cff9d : 0xff3c5a, 0.7, 0.2);
        dead = true;
      }
      if (!dead && (p.mesh.position.y < 0 || p.mesh.position.y > ESF.World.H)) dead = true;
      if (dead) { scene.remove(p.mesh); projectiles.splice(i, 1); }
    }
  }

  // ---------------- IA ----------------
  function updateEnemy(e, dt, player, t) {
    const T = TYPES[e.type];
    // aparición
    if (e.spawnT < 0.5) { e.spawnT += dt; const k = Math.min(1, e.spawnT / 0.5); e.group.scale.setScalar(k); }

    if (!e.alive) {
      e.dieT += dt;
      const k = Math.min(1, e.dieT / 0.5);
      e.group.rotation.x = -k * k * Math.PI / 2 * 0.9;
      e.group.position.y = e.pos.y - Math.max(0, e.dieT - 1.2) * 0.8;
      if (e.dieT > 1.2) e.mats.forEach(m => { m.transparent = true; m.opacity = Math.max(0, 1 - (e.dieT - 1.2) / 0.8); });
      if (e.dieT > 2.1) removeEnemy(e);
      return;
    }

    const toP = tmp.set(player.pos.x - e.pos.x, 0, player.pos.z - e.pos.z);
    const dist = toP.length();
    const eye = tmp2.set(e.pos.x, e.pos.y + 1.6, e.pos.z);
    const playerEye = new THREE.Vector3(player.pos.x, player.pos.y + player.height - 0.1, player.pos.z);
    const los = player.alive && dist < 26 && ESF.World.lineOfSight(eye, playerEye);

    // orientación hacia el jugador
    const targetYaw = Math.atan2(toP.x, toP.z);
    let dy = targetYaw - e.yaw; dy = Math.atan2(Math.sin(dy), Math.cos(dy));
    e.yaw += dy * Math.min(1, dt * 6);

    // decisión de movimiento
    let mx = 0, mz = 0;
    e.strafeT -= dt;
    if (e.strafeT <= 0) { e.strafeT = 1 + Math.random() * 2; e.strafeDir = Math.random() < 0.5 ? -1 : 1; }
    const fwd = toP.clone().normalize();
    const side = new THREE.Vector3(-fwd.z, 0, fwd.x);
    if (!player.alive) { mx = 0; mz = 0; }
    else if (!los || dist > T.range[1]) { mx = fwd.x; mz = fwd.z; }
    else if (dist < T.range[0]) { mx = -fwd.x * 0.8 + side.x * e.strafeDir * 0.6; mz = -fwd.z * 0.8 + side.z * e.strafeDir * 0.6; }
    else { mx = side.x * e.strafeDir; mz = side.z * e.strafeDir; }

    // evitación de obstáculos si está atascado
    if (e.avoidT > 0) { e.avoidT -= dt; mx = Math.cos(e.avoidDir); mz = Math.sin(e.avoidDir); }

    const speed = T.speed * (e.hitT > 0 ? 0.4 : 1);
    const ml = Math.hypot(mx, mz);
    e.moving = ml > 0.05;
    if (e.moving) {
      e.pos.x += mx / ml * speed * dt; e.pos.z += mz / ml * speed * dt;
      ESF.World.resolve(e.pos, 0.42, 0, 1.8);
      e.walkT += dt * 7;
    }
    // separación entre aliens
    for (const o of list) {
      if (o === e || !o.alive) continue;
      const dx = e.pos.x - o.pos.x, dz = e.pos.z - o.pos.z; const d = Math.hypot(dx, dz);
      if (d < 1.1 && d > 0.001) { e.pos.x += dx / d * (1.1 - d) * 0.5; e.pos.z += dz / d * (1.1 - d) * 0.5; }
    }
    // detección de atasco
    e.stuckT += dt;
    if (e.stuckT > 0.6) {
      const moved = e.pos.distanceTo(e.lastPos);
      if (e.moving && moved < 0.25) { e.avoidT = 0.7 + Math.random() * 0.5; e.avoidDir = Math.atan2(mz, mx) + (Math.random() < 0.5 ? 1 : -1) * (Math.PI / 2 + Math.random() * 0.6); }
      e.lastPos.copy(e.pos); e.stuckT = 0;
    }

    // disparo
    e.shootCd -= dt;
    if (e.burstLeft > 0) {
      e.burstT -= dt;
      if (e.burstT <= 0) { fireProjectile(e, playerEye); e.burstLeft--; e.burstT = T.burstGap; }
    } else if (los && e.shootCd <= 0 && Math.abs(dy) < 0.35 && dist < 24) {
      e.burstLeft = T.burst; e.burstT = 0.05;
      e.shootCd = T.cooldown * (0.8 + Math.random() * 0.5);
    }

    // animación
    const P = e.parts;
    const sw = e.moving ? Math.sin(e.walkT) : 0;
    P.legs[0].hip.rotation.x = sw * 0.7; P.legs[1].hip.rotation.x = -sw * 0.7;
    P.legs[0].knee.rotation.x = Math.max(0, -sw) * 0.9; P.legs[1].knee.rotation.x = Math.max(0, sw) * 0.9;
    P.arms[0].sh.rotation.x = -sw * 0.5 - 0.2; P.arms[0].elbow.rotation.x = -0.6;
    // brazo derecho apunta al jugador
    const pitch = Math.atan2(playerEye.y - (e.pos.y + 1.3), Math.max(0.5, dist));
    P.arms[1].sh.rotation.x = -Math.PI / 2 - pitch + 0.1; P.arms[1].elbow.rotation.x = 0.2;
    P.arms[1].sh.rotation.z = -0.15;
    P.head.rotation.x = -pitch * 0.5; P.head.rotation.y = Math.sin(t * 1.3 + e.id) * 0.15;
    P.torso.position.y = 1.05 + Math.abs(sw) * 0.03;
    // reacción al impacto
    if (e.hitT > 0) { e.hitT -= dt; e.mats[0].emissive.setHex(0xffffff); e.mats[0].emissiveIntensity = 0.6; e.mats[1].emissive.setHex(0xffffff); e.mats[1].emissiveIntensity = 0.6; }
    else { e.mats[0].emissive.setHex(0x000000); e.mats[1].emissive.setHex(0x000000); }
    e.mats[2].emissiveIntensity = 1.3 + Math.sin(t * 6 + e.id) * 0.4;

    e.group.position.copy(e.pos);
    e.group.rotation.y = e.yaw;
  }

  function update(dt, player, t, cb, maxEnemies) {
    for (let i = list.length - 1; i >= 0; i--) updateEnemy(list[i], dt, player, t);
    updateProjectiles(dt, player, cb);
    // reapariciones
    for (let i = respawnQueue.length - 1; i >= 0; i--) {
      respawnQueue[i].t -= dt;
      if (respawnQueue[i].t <= 0) {
        const alive = list.filter(e => e.alive).length;
        if (alive < maxEnemies) {
          const s = ESF.World.pickEnemySpawn(player.pos, list.map(e => e.pos));
          spawn(respawnQueue[i].type, s.x, s.z);
        }
        respawnQueue.splice(i, 1);
      }
    }
  }

  function aliveCount() { return list.filter(e => e.alive).length; }

  return { TYPES, list, hitboxes, projectiles, init, reset, spawn, spawnWave, damage, update, aliveCount };
})();
