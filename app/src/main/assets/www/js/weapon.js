/* ESFshoot — fusil de pulso: modelo en primera persona, animaciones de disparo/recarga, munición y efectos */
window.ESF = window.ESF || {};

// ---------------- Efectos compartidos (chispas, trazadoras, brillos) ----------------
ESF.FX = (function () {
  let scene = null;
  const items = [];
  let glowTex = null, sparkTex = null;

  function radialTex(inner, outer) {
    const c = document.createElement('canvas'); c.width = c.height = 64;
    const g = c.getContext('2d');
    const grd = g.createRadialGradient(32, 32, 0, 32, 32, 32);
    grd.addColorStop(0, inner); grd.addColorStop(0.35, inner); grd.addColorStop(1, outer);
    g.fillStyle = grd; g.fillRect(0, 0, 64, 64);
    return new THREE.CanvasTexture(c);
  }

  function init(sc) {
    scene = sc; items.length = 0;
    glowTex = radialTex('rgba(255,255,255,1)', 'rgba(255,255,255,0)');
    sparkTex = radialTex('rgba(255,240,200,1)', 'rgba(255,150,50,0)');
  }

  function sprite(tex, color, size, additive) {
    const m = new THREE.SpriteMaterial({ map: tex, color, transparent: true, depthWrite: false, blending: additive ? THREE.AdditiveBlending : THREE.NormalBlending });
    const s = new THREE.Sprite(m);
    s.scale.set(size, size, size);
    return s;
  }

  // Estallido de chispas en un punto
  function sparks(pos, color, n, speed) {
    for (let i = 0; i < n; i++) {
      const s = sprite(sparkTex, color || 0xffc070, 0.12 + Math.random() * 0.1, true);
      s.position.copy(pos);
      const v = new THREE.Vector3((Math.random() - 0.5), Math.random() * 0.8, (Math.random() - 0.5)).normalize().multiplyScalar((speed || 4) * (0.4 + Math.random()));
      scene.add(s);
      items.push({ obj: s, vel: v, life: 0.25 + Math.random() * 0.25, t: 0, gravity: true, type: 'spark' });
    }
  }

  // Resplandor que se expande y desvanece
  function burst(pos, color, size, life) {
    const s = sprite(glowTex, color, size, true);
    s.position.copy(pos); scene.add(s);
    items.push({ obj: s, life: life || 0.3, t: 0, grow: size * 2.5, type: 'burst' });
  }

  // Trazadora (línea brillante) desde a hasta b
  function tracer(a, b, color) {
    const dir = new THREE.Vector3().subVectors(b, a);
    const len = dir.length();
    if (len < 0.05) return;
    const geo = new THREE.CylinderGeometry(0.012, 0.012, len, 4, 1, true);
    const mat = new THREE.MeshBasicMaterial({ color: color || 0x9ff6ff, transparent: true, opacity: 0.9, blending: THREE.AdditiveBlending, depthWrite: false });
    const m = new THREE.Mesh(geo, mat);
    m.position.copy(a).addScaledVector(dir, 0.5);
    m.quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), dir.normalize());
    scene.add(m);
    items.push({ obj: m, life: 0.07, t: 0, type: 'tracer' });
  }

  // Trozos de alien al morir
  function gibs(pos, color) {
    for (let i = 0; i < 10; i++) {
      const g = new THREE.Mesh(new THREE.BoxGeometry(0.12, 0.12, 0.12), new THREE.MeshBasicMaterial({ color }));
      g.position.copy(pos).add(new THREE.Vector3((Math.random() - 0.5) * 0.6, Math.random() * 1.2, (Math.random() - 0.5) * 0.6));
      scene.add(g);
      items.push({ obj: g, vel: new THREE.Vector3((Math.random() - 0.5) * 6, 2 + Math.random() * 4, (Math.random() - 0.5) * 6), life: 1.2, t: 0, gravity: true, type: 'gib', spin: new THREE.Vector3(Math.random() * 8, Math.random() * 8, Math.random() * 8) });
    }
  }

  function update(dt) {
    for (let i = items.length - 1; i >= 0; i--) {
      const it = items[i];
      it.t += dt;
      const k = 1 - it.t / it.life;
      if (it.t >= it.life) { scene.remove(it.obj); if (it.obj.material) it.obj.material.dispose(); if (it.obj.geometry) it.obj.geometry.dispose(); items.splice(i, 1); continue; }
      if (it.vel) {
        if (it.gravity) it.vel.y -= 12 * dt;
        it.obj.position.addScaledVector(it.vel, dt);
        if (it.obj.position.y < 0.05) { it.obj.position.y = 0.05; it.vel.y *= -0.3; it.vel.x *= 0.7; it.vel.z *= 0.7; }
      }
      if (it.spin) { it.obj.rotation.x += it.spin.x * dt; it.obj.rotation.y += it.spin.y * dt; }
      if (it.type === 'burst') { const s = it.grow * (1 - k * 0.6); it.obj.scale.set(s, s, s); }
      if (it.obj.material && it.type !== 'gib') it.obj.material.opacity = k;
    }
  }

  function clear() { for (const it of items) scene.remove(it.obj); items.length = 0; }

  return { init, sparks, burst, tracer, gibs, update, clear, radialTex };
})();

// ---------------- Arma ----------------
ESF.Weapon = (function () {
  const MAG = 30, RESERVE_START = 120, FIRE_INTERVAL = 0.095, RELOAD_TIME = 1.75;
  const st = { mag: MAG, reserve: RESERVE_START, reloading: false, reloadT: 0, cooldown: 0, shots: 0, hits: 0, recoil: 0, bobT: 0, muzzleT: 0, dryT: 0 };
  let camera = null, scene = null;
  let group = null, mag = null, muzzle = null, flash = null, flashLight = null, barrelTip = null, slide = null;
  const base = { pos: new THREE.Vector3(0.32, -0.28, -0.55), rot: new THREE.Euler(0, 0, 0) };
  const raycaster = new THREE.Raycaster();
  const tmpV = new THREE.Vector3(), tmpV2 = new THREE.Vector3();
  const sway = { x: 0, y: 0 };
  const reloadSteps = [false, false, false];

  function build() {
    group = new THREE.Group();
    const dark = new THREE.MeshStandardMaterial({ color: 0x1c242c, roughness: 0.45, metalness: 0.85 });
    const mid = new THREE.MeshStandardMaterial({ color: 0x3a4652, roughness: 0.5, metalness: 0.7 });
    const glow = new THREE.MeshStandardMaterial({ color: 0x19e6ff, emissive: 0x19e6ff, emissiveIntensity: 1.5 });
    const glow2 = new THREE.MeshStandardMaterial({ color: 0x7ff4ff, emissive: 0x19e6ff, emissiveIntensity: 0.8, transparent: true, opacity: 0.8 });

    // cuerpo
    const body = new THREE.Mesh(new THREE.BoxGeometry(0.11, 0.13, 0.55), mid); body.position.set(0, 0, 0); group.add(body);
    const top = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.05, 0.62), dark); top.position.set(0, 0.09, -0.02); group.add(top);
    const rail = new THREE.Mesh(new THREE.BoxGeometry(0.03, 0.02, 0.4), glow); rail.position.set(0, 0.12, -0.05); group.add(rail);
    // cañón
    const barrel = new THREE.Mesh(new THREE.CylinderGeometry(0.022, 0.026, 0.42, 10), dark); barrel.rotation.x = Math.PI / 2; barrel.position.set(0, 0.02, -0.45); group.add(barrel);
    const shroud = new THREE.Mesh(new THREE.BoxGeometry(0.09, 0.09, 0.3), mid); shroud.position.set(0, 0.01, -0.38); group.add(shroud);
    muzzle = new THREE.Mesh(new THREE.TorusGeometry(0.035, 0.012, 6, 16), glow); muzzle.position.set(0, 0.02, -0.66); group.add(muzzle);
    for (let i = 0; i < 3; i++) { const v = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.012, 0.02), glow2); v.position.set(0, 0.06, -0.3 - i * 0.07); group.add(v); }
    // corredera (se mueve al disparar)
    slide = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.04, 0.2), dark); slide.position.set(0.055, 0.03, -0.05); group.add(slide);
    // cargador
    mag = new THREE.Group();
    const magBody = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.2, 0.09), dark); magBody.position.set(0, -0.1, 0); mag.add(magBody);
    const magGlow = new THREE.Mesh(new THREE.BoxGeometry(0.062, 0.03, 0.02), glow); magGlow.position.set(0, -0.16, 0.05); mag.add(magGlow);
    mag.position.set(0, -0.06, -0.12); mag.rotation.x = 0.15; group.add(mag);
    // empuñadura y culata
    const grip = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.16, 0.07), dark); grip.position.set(0, -0.12, 0.12); grip.rotation.x = 0.35; group.add(grip);
    const stock = new THREE.Mesh(new THREE.BoxGeometry(0.07, 0.1, 0.25), mid); stock.position.set(0, -0.02, 0.35); group.add(stock);
    // mira holográfica
    const sightBase = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.04, 0.08), dark); sightBase.position.set(0, 0.14, 0.05); group.add(sightBase);
    const sightGlass = new THREE.Mesh(new THREE.PlaneGeometry(0.05, 0.04), glow2); sightGlass.position.set(0, 0.18, 0.02); group.add(sightGlass);
    // manos (guantes) simplificadas
    const hand = new THREE.MeshStandardMaterial({ color: 0x24303a, roughness: 0.8, metalness: 0.2 });
    const h1 = new THREE.Mesh(new THREE.BoxGeometry(0.09, 0.09, 0.12), hand); h1.position.set(0.0, -0.13, 0.16); group.add(h1);
    const h2 = new THREE.Mesh(new THREE.BoxGeometry(0.09, 0.08, 0.12), hand); h2.position.set(-0.03, -0.05, -0.3); group.add(h2);
    const arm = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.1, 0.5), hand); arm.position.set(0.05, -0.16, 0.45); arm.rotation.x = -0.2; group.add(arm);

    // fogonazo
    const flashTex = ESF.FX.radialTex('rgba(255,255,255,1)', 'rgba(25,230,255,0)');
    flash = new THREE.Sprite(new THREE.SpriteMaterial({ map: flashTex, color: 0xbff6ff, transparent: true, blending: THREE.AdditiveBlending, depthWrite: false }));
    flash.position.set(0, 0.02, -0.72); flash.scale.set(0.12, 0.12, 0.12); flash.visible = false; group.add(flash);
    flashLight = new THREE.PointLight(0x7fe9ff, 0, 8, 2); flashLight.position.set(0, 0.05, -0.6); group.add(flashLight);
    barrelTip = new THREE.Object3D(); barrelTip.position.set(0, 0.02, -0.68); group.add(barrelTip);

    group.position.copy(base.pos);
    camera.add(group);
  }

  function init(cam, sc) {
    camera = cam; scene = sc;
    build();
    reset();
  }

  function reset() {
    st.mag = MAG; st.reserve = RESERVE_START; st.reloading = false; st.reloadT = 0; st.cooldown = 0; st.shots = 0; st.hits = 0; st.recoil = 0; st.muzzleT = 0;
    mag.position.set(0, -0.06, -0.12); mag.visible = true;
  }

  function startReload() {
    if (st.reloading || st.mag >= MAG || st.reserve <= 0) return false;
    st.reloading = true; st.reloadT = 0;
    reloadSteps[0] = reloadSteps[1] = reloadSteps[2] = false;
    return true;
  }

  // Intenta disparar. Devuelve {hit, point, enemy, part} o null
  function tryFire(targets, moving, crouching) {
    if (st.reloading || st.cooldown > 0) return null;
    if (st.mag <= 0) {
      if (st.dryT <= 0) { ESF.Audio.dry(); st.dryT = 0.3; }
      if (st.reserve > 0) startReload();
      return null;
    }
    st.mag--; st.shots++;
    st.cooldown = FIRE_INTERVAL;
    st.recoil = 1; st.muzzleT = 0.05;
    flash.visible = true; flash.scale.setScalar(0.09 + Math.random() * 0.08); flash.material.rotation = Math.random() * 6.28;
    flashLight.intensity = 25;
    ESF.Audio.shoot();

    // dispersión
    let spread = 0.010 + (moving ? 0.018 : 0) + (crouching ? -0.004 : 0);
    const dir = new THREE.Vector3(0, 0, -1).applyQuaternion(camera.quaternion);
    dir.x += (Math.random() - 0.5) * spread * 2; dir.y += (Math.random() - 0.5) * spread * 2; dir.z += (Math.random() - 0.5) * spread * 2;
    dir.normalize();
    camera.getWorldPosition(tmpV);
    raycaster.set(tmpV, dir);
    raycaster.far = 80;
    const hits = raycaster.intersectObjects(targets, false);
    let res = null;
    barrelTip.getWorldPosition(tmpV2);
    if (hits.length) {
      const h = hits[0];
      res = { point: h.point, enemy: h.object.userData.enemy || null, part: h.object.userData.part || null, dist: h.distance };
      ESF.FX.tracer(tmpV2, h.point, 0x9ff6ff);
      if (res.enemy) { ESF.FX.sparks(h.point, res.enemy.type === 'plasma' ? 0x6cff9c : 0xd48cff, 6, 3); ESF.FX.burst(h.point, 0xffffff, 0.35, 0.15); }
      else { ESF.FX.sparks(h.point, 0xffc070, 5, 3); ESF.FX.burst(h.point, 0x9ff6ff, 0.25, 0.12); }
    } else {
      const far = tmpV.clone().addScaledVector(dir, 60);
      ESF.FX.tracer(tmpV2, far, 0x9ff6ff);
    }
    return res;
  }

  function update(dt, ctx) {
    if (st.cooldown > 0) st.cooldown -= dt;
    if (st.dryT > 0) st.dryT -= dt;
    if (st.muzzleT > 0) { st.muzzleT -= dt; if (st.muzzleT <= 0) { flash.visible = false; } }
    flashLight.intensity = Math.max(0, flashLight.intensity - dt * 300);
    st.recoil = Math.max(0, st.recoil - dt * 9);

    // balanceo al andar
    const speed = ctx.speed || 0;
    if (ctx.grounded && speed > 0.5) st.bobT += dt * (ctx.crouch ? 6 : 9);
    const bobA = Math.min(1, speed / 5) * 0.012;
    const bobX = Math.sin(st.bobT) * bobA, bobY = Math.abs(Math.cos(st.bobT)) * bobA * 1.4;

    // vaivén al mirar
    sway.x += ((ctx.lookYaw || 0) * 0.6 - sway.x) * Math.min(1, dt * 10);
    sway.y += ((ctx.lookPitch || 0) * 0.6 - sway.y) * Math.min(1, dt * 10);

    // recarga
    let rPosY = 0, rPosZ = 0, rRotX = 0, rRotZ = 0;
    if (st.reloading) {
      st.reloadT += dt;
      const t = st.reloadT;
      const ease = x => x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
      // 0-0.25: inclinar arma
      const tilt = ease(Math.min(1, t / 0.25)) * (t < 1.35 ? 1 : 1 - ease(Math.min(1, (t - 1.35) / 0.4)));
      rRotZ = -0.55 * tilt; rRotX = 0.35 * tilt; rPosY = -0.08 * tilt; rPosZ = 0.06 * tilt;
      // 0.25-0.55: sale el cargador
      if (t > 0.25 && t < 0.55) { const k = (t - 0.25) / 0.3; mag.position.y = -0.06 - k * k * 0.9; mag.rotation.x = 0.15 + k * 1.2; if (!reloadSteps[0]) { reloadSteps[0] = true; ESF.Audio.reloadClick(0); } }
      else if (t >= 0.55 && t < 0.9) { const k = ease((t - 0.55) / 0.35); mag.position.y = -0.7 + k * 0.64; mag.rotation.x = 0.15; mag.visible = true; }
      else if (t >= 0.9) { mag.position.y = -0.06; mag.rotation.x = 0.15; if (!reloadSteps[1]) { reloadSteps[1] = true; ESF.Audio.reloadClick(1); } }
      // 1.1-1.35: montar
      if (t >= 1.1 && t < 1.35) { const k = Math.sin(((t - 1.1) / 0.25) * Math.PI); slide.position.z = -0.05 + k * 0.08; rRotX += k * 0.12; if (!reloadSteps[2]) { reloadSteps[2] = true; ESF.Audio.reloadClick(2); } }
      if (t >= 1.35 && st.mag < MAG) { const need = MAG - st.mag; const take = Math.min(need, st.reserve); st.mag += take; st.reserve -= take; }
      if (t >= RELOAD_TIME) { st.reloading = false; slide.position.z = -0.05; }
    }

    // retroceso
    const rc = st.recoil;
    const recoilZ = rc * 0.07, recoilRotX = rc * 0.12;
    slide.position.z = st.reloading ? slide.position.z : -0.05 + rc * 0.05;

    group.position.set(
      base.pos.x + bobX + sway.x + (ctx.crouch ? -0.03 : 0),
      base.pos.y + bobY + sway.y + rPosY + (ctx.crouch ? 0.02 : 0) - (ctx.fall || 0) * 0.02,
      base.pos.z + recoilZ + rPosZ
    );
    group.rotation.set(recoilRotX + rRotX + sway.y * 2 - (ctx.fall || 0) * 0.05, sway.x * 2, rRotZ + bobX * 2);
  }

  function ammoText() { return { mag: st.mag, reserve: st.reserve, max: MAG }; }

  return { st, MAG, init, reset, startReload, tryFire, update, ammoText };
})();
