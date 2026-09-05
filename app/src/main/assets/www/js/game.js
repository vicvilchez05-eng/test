/* ESFshoot — bucle principal: jugador, física, partida, menús */
(function () {
  'use strict';
  const W = ESF.World, In = ESF.Input, Wp = ESF.Weapon, En = ESF.Enemies, HUD = ESF.HUD, Au = ESF.Audio, FX = ESF.FX;

  const GOAL = 20;
  const MAX_ENEMIES = 5;
  const EYE_STAND = 1.7, EYE_CROUCH = 1.05, RADIUS = 0.38, GRAVITY = 20, JUMP_V = 7.2;
  const SPEED_WALK = 5.2, SPEED_CROUCH = 2.6;

  let renderer, scene, camera, yawObj, pitchObj;
  const player = {
    pos: new THREE.Vector3(0, 0.6, 0), vel: new THREE.Vector3(), yaw: 0, pitch: 0, height: EYE_STAND,
    grounded: true, crouch: false, alive: true,
    hp: 100, hpMax: 100, shield: 100, shieldMax: 100, lastDamageT: 0, respawnT: 0, speed: 0, fall: 0
  };
  const game = { state: 'menu', kills: 0, deaths: 0, time: 0, streak: 0, lastKillT: -10, multi: 0, prevState: 'menu' };
  const quality = { value: 'med' };
  let stepT = 0;
  const clock = new THREE.Clock();

  function $(id) { return document.getElementById(id); }

  // ---------------- Inicialización ----------------
  function init() {
    renderer = new THREE.WebGLRenderer({ canvas: $('gl'), antialias: quality.value !== 'low', powerPreference: 'high-performance' });
    renderer.setPixelRatio(pixelRatio());
    renderer.setSize(window.innerWidth, window.innerHeight, false);
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.1;

    scene = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.05, 120);
    pitchObj = new THREE.Object3D(); pitchObj.add(camera);
    yawObj = new THREE.Object3D(); yawObj.add(pitchObj);
    scene.add(yawObj);

    W.build(scene, quality.value);
    FX.init(scene);
    Wp.init(camera, scene);
    En.init(scene);
    HUD.init();
    In.init();
    bindMenus();
    loadSettings();

    window.addEventListener('resize', onResize);
    onResize();
    $('loading').classList.add('hidden');
    requestAnimationFrame(loop);
  }

  function pixelRatio() {
    const dpr = window.devicePixelRatio || 1;
    if (quality.value === 'low') return Math.min(dpr, 0.75);
    if (quality.value === 'med') return Math.min(dpr, 1.25);
    return Math.min(dpr, 2);
  }

  function onResize() {
    const w = window.innerWidth, h = window.innerHeight;
    camera.aspect = w / h; camera.updateProjectionMatrix();
    renderer.setPixelRatio(pixelRatio());
    renderer.setSize(w, h, false);
  }

  // ---------------- Menús ----------------
  function showOverlay(id) {
    ['menu', 'options', 'help', 'pause', 'death', 'gameover'].forEach(o => $(o).classList.toggle('hidden', o !== id));
  }

  function bindMenus() {
    const tap = (id, fn) => { const b = $(id); const h = e => { e.preventDefault(); Au.init(); Au.resume(); Au.ui(); fn(); }; b.addEventListener('click', h); b.addEventListener('touchend', h); };
    tap('m-play', startGame);
    tap('m-options', () => { game.prevState = 'menu'; showOverlay('options'); });
    tap('m-help', () => showOverlay('help'));
    tap('o-back', () => { saveSettings(); showOverlay(game.prevState === 'pause' ? 'pause' : 'menu'); });
    tap('h-back', () => showOverlay('menu'));
    tap('p-resume', resumeGame);
    tap('p-options', () => { game.prevState = 'pause'; showOverlay('options'); });
    tap('p-quit', () => endGame(false, true));
    tap('go-again', startGame);
    tap('go-menu', () => { game.state = 'menu'; showOverlay('menu'); HUD.show(false); });

    const sens = $('opt-sens');
    sens.addEventListener('input', () => { In.settings.sensitivity = parseFloat(sens.value); $('opt-sens-v').textContent = parseFloat(sens.value).toFixed(1); });
    $('opt-inv').addEventListener('change', e => { In.settings.invertY = e.target.checked; });
    $('opt-sound').addEventListener('change', e => { Au.setEnabled(e.target.checked); });
    $('opt-quality').addEventListener('change', e => { quality.value = e.target.value; onResize(); saveSettings(); });

    // Botón "atrás" de Android → pausa
    window.ESFshootAndroid = { onBack: () => { if (game.state === 'playing') pauseGame(); else if (game.state === 'paused') resumeGame(); else if (game.state !== 'menu') { showOverlay('menu'); game.state = 'menu'; HUD.show(false); } return game.state !== 'menu'; }, pause: () => { if (game.state === 'playing') pauseGame(); } };
    document.addEventListener('visibilitychange', () => { if (document.hidden && game.state === 'playing') pauseGame(); });
  }

  function loadSettings() {
    try {
      const s = JSON.parse(localStorage.getItem('esfshoot.settings') || '{}');
      if (s.sens) { In.settings.sensitivity = s.sens; $('opt-sens').value = s.sens; $('opt-sens-v').textContent = s.sens.toFixed(1); }
      if (s.inv !== undefined) { In.settings.invertY = s.inv; $('opt-inv').checked = s.inv; }
      if (s.auto !== undefined) $('opt-auto').checked = s.auto;
      if (s.sound !== undefined) { $('opt-sound').checked = s.sound; Au.setEnabled(s.sound); }
      if (s.quality) { quality.value = s.quality; $('opt-quality').value = s.quality; onResize(); }
    } catch (e) { /* sin almacenamiento */ }
  }
  function saveSettings() {
    try { localStorage.setItem('esfshoot.settings', JSON.stringify({ sens: In.settings.sensitivity, inv: In.settings.invertY, auto: $('opt-auto').checked, sound: $('opt-sound').checked, quality: quality.value })); } catch (e) { /* ignorado */ }
  }

  // ---------------- Partida ----------------
  function startGame() {
    Au.init(); Au.resume(); Au.ambient(true);
    game.state = 'playing'; game.kills = 0; game.deaths = 0; game.time = 0; game.streak = 0; game.multi = 0; game.lastKillT = -10;
    En.reset(); FX.clear(); Wp.reset();
    respawnPlayer(true);
    En.spawnWave(MAX_ENEMIES, player.pos);
    HUD.setScore(0, 0, GOAL);
    HUD.show(true);
    showOverlay(null);
    In.setEnabled(true);
    In.resetCrouch();
    HUD.feed('<b>MISIÓN INICIADA</b> · Asegura la cubierta 7');
  }

  function pauseGame() {
    game.state = 'paused';
    In.setEnabled(false);
    $('p-kills').textContent = game.kills; $('p-deaths').textContent = game.deaths;
    showOverlay('pause');
  }
  function resumeGame() {
    game.state = 'playing';
    showOverlay(null);
    In.setEnabled(true);
    Au.resume();
    clock.getDelta();
  }

  function endGame(victory, aborted) {
    game.state = 'gameover';
    In.setEnabled(false);
    Au.ambient(false);
    HUD.show(false);
    $('go-title').textContent = victory ? 'CUBIERTA ASEGURADA' : (aborted ? 'MISIÓN ABORTADA' : 'FIN DE LA MISIÓN');
    $('go-kills').textContent = game.kills; $('go-deaths').textContent = game.deaths;
    $('go-acc').textContent = (Wp.st.shots ? Math.round(Wp.st.hits / Wp.st.shots * 100) : 0) + '%';
    const m = Math.floor(game.time / 60), s = Math.floor(game.time % 60);
    $('go-time').textContent = m + ':' + (s < 10 ? '0' : '') + s;
    showOverlay('gameover');
  }

  function respawnPlayer(first) {
    const p = first ? new THREE.Vector3(-2.2, 0.6, 2.2) : W.pickPlayerSpawn(En.list);
    player.pos.copy(p); player.vel.set(0, 0, 0);
    player.yaw = Math.atan2(p.x, p.z); player.pitch = 0; // mirar hacia el centro del hangar
    player.hp = player.hpMax; player.shield = player.shieldMax; player.alive = true; player.crouch = false; player.height = EYE_STAND;
    player.lastDamageT = -10;
    In.resetCrouch();
    Wp.reset();
    HUD.flash(0.6);
    Au.shieldUp();
  }

  function onPlayerHit(dmg, from, enemy) {
    if (!player.alive || game.state !== 'playing') return;
    player.lastDamageT = game.time;
    let d = dmg;
    if (player.shield > 0) {
      const take = Math.min(player.shield, d); player.shield -= take; d -= take;
      if (player.shield <= 0) { Au.shieldDown(); }
    }
    if (d > 0) player.hp -= d;
    Au.hurt();
    const ang = Math.atan2(-(from.x - player.pos.x), -(from.z - player.pos.z));
    HUD.damage(ang, 0.35 + dmg / 60);
    if (player.hp <= 0) killPlayer(enemy);
  }

  function killPlayer(enemy) {
    player.alive = false; player.hp = 0; game.deaths++; game.streak = 0;
    player.respawnT = 3;
    Au.die();
    HUD.setScore(game.kills, game.deaths, GOAL);
    HUD.feed('<b>' + (enemy ? enemy.name : 'Xeno') + '</b> te ha eliminado', true);
    $('death-by').textContent = 'Eliminado por ' + (enemy ? enemy.name.toLowerCase() : 'un xenomorfo');
    $('death-timer').textContent = '3';
    showOverlay('death');
    In.resetCrouch();
  }

  function onEnemyKilled(e, head) {
    game.kills++;
    game.streak++;
    Wp.st.hits++;
    Au.kill();
    HUD.hitmarker(true);
    HUD.setScore(game.kills, game.deaths, GOAL);
    HUD.feed('<b>TÚ</b> ' + (head ? '☠ (cabeza) ' : '✕ ') + e.name);
    // medallas tipo Halo
    if (game.time - game.lastKillT < 4) game.multi++; else game.multi = 1;
    game.lastKillT = game.time;
    if (game.multi === 2) HUD.medal('DOBLE BAJA', 'dos eliminaciones seguidas');
    else if (game.multi === 3) HUD.medal('TRIPLE BAJA', 'racha letal');
    else if (game.multi >= 4) HUD.medal('EXTERMINIO', game.multi + ' bajas seguidas');
    else if (head) HUD.medal('TIRO A LA CABEZA', 'precisión quirúrgica');
    else if (game.streak === 5) HUD.medal('RACHA ×5', 'imparable');
    else if (game.streak === 10) HUD.medal('RACHA ×10', 'leyenda de la ESF');
    if (game.kills >= GOAL) setTimeout(() => endGame(true), 1200);
  }

  // ---------------- Actualización del jugador ----------------
  function updatePlayer(dt) {
    const S = In.state;
    // mira
    const look = In.consumeLook();
    if (player.alive) {
      player.yaw += look.yaw;
      player.pitch = Math.max(-1.45, Math.min(1.45, player.pitch + look.pitch));
    }

    // agacharse
    const wantCrouch = S.crouch && player.alive;
    player.crouch = wantCrouch;
    const targetH = player.crouch ? EYE_CROUCH : EYE_STAND;
    player.height += (targetH - player.height) * Math.min(1, dt * 12);

    // movimiento
    let mx = 0, mz = 0;
    if (player.alive) {
      const f = S.move.y, r = S.move.x;
      const sy = Math.sin(player.yaw), cy = Math.cos(player.yaw);
      // adelante = -z en el espacio local de la cámara
      mx = (-sy * f + cy * r); mz = (-cy * f - sy * r);
    }
    const ml = Math.hypot(mx, mz);
    const speed = player.crouch ? SPEED_CROUCH : SPEED_WALK;
    const targetVX = ml > 1 ? mx / ml * speed : mx * speed;
    const targetVZ = ml > 1 ? mz / ml * speed : mz * speed;
    const accel = player.grounded ? 14 : 4;
    player.vel.x += (targetVX - player.vel.x) * Math.min(1, dt * accel);
    player.vel.z += (targetVZ - player.vel.z) * Math.min(1, dt * accel);

    // salto
    if (In.consume('jump') && player.alive && player.grounded && !player.crouch) { player.vel.y = JUMP_V; player.grounded = false; Au.jump(); }
    player.vel.y -= GRAVITY * dt;

    // integrar
    const feetBefore = player.pos.y;
    player.pos.x += player.vel.x * dt;
    player.pos.z += player.vel.z * dt;
    W.resolve(player.pos, RADIUS, player.pos.y, player.height);
    player.pos.y += player.vel.y * dt;
    const ground = W.groundAt(player.pos.x, player.pos.z, RADIUS, feetBefore);
    if (player.pos.y <= ground) {
      if (!player.grounded && player.vel.y < -3) { player.fall = Math.min(1, -player.vel.y / 12); Au.step(); }
      player.pos.y = ground; player.vel.y = 0; player.grounded = true;
    } else if (player.pos.y > ground + 0.02) player.grounded = false;
    if (player.pos.y + player.height > W.H - 0.1) { player.pos.y = W.H - 0.1 - player.height; player.vel.y = Math.min(0, player.vel.y); }
    player.fall = Math.max(0, player.fall - dt * 4);
    player.speed = Math.hypot(player.vel.x, player.vel.z);

    // pasos
    if (player.grounded && player.speed > 1) { stepT += dt * player.speed; if (stepT > 3.2) { stepT = 0; Au.step(); } }

    // cámara
    yawObj.position.set(player.pos.x, player.pos.y + player.height, player.pos.z);
    yawObj.rotation.y = player.yaw;
    pitchObj.rotation.x = player.pitch;
    // inclinación al agacharse/caer
    camera.rotation.z = -S.move.x * 0.015 * (player.speed / SPEED_WALK);

    // escudo: regeneración
    if (player.alive && game.time - player.lastDamageT > 4 && player.shield < player.shieldMax) {
      const was = player.shield;
      player.shield = Math.min(player.shieldMax, player.shield + dt * 35);
      if (was <= 0 && player.shield > 0) Au.shieldUp();
    }

    // reaparición
    if (!player.alive) {
      player.respawnT -= dt;
      $('death-timer').textContent = Math.max(1, Math.ceil(player.respawnT));
      if (player.respawnT <= 0) { respawnPlayer(false); showOverlay(null); }
    }
  }

  // ---------------- Arma / disparo ----------------
  const raycaster = new THREE.Raycaster();
  function updateWeapon(dt, lookYaw, lookPitch) {
    const S = In.state;
    if (player.alive) {
      if (In.consume('reload')) Wp.startReload();
      // ¿enemigo bajo la mira? (para color de mira y disparo automático)
      let aiming = false;
      if (En.hitboxes.length) {
        raycaster.setFromCamera({ x: 0, y: 0 }, camera); raycaster.far = 60;
        const hits = raycaster.intersectObjects(En.hitboxes.concat(W.wallMeshes), false);
        aiming = hits.length > 0 && !!hits[0].object.userData.enemy;
      }
      HUD.setCrosshairEnemy(aiming);
      const auto = $('opt-auto').checked && aiming;
      if (S.fire || auto) {
        const targets = En.hitboxes.concat(W.wallMeshes);
        const res = Wp.tryFire(targets, player.speed > 1.5, player.crouch);
        if (res && res.enemy && res.enemy.alive) {
          const head = res.part === 'head';
          const dmg = head ? 30 : 14;
          Au.hit();
          const killed = En.damage(res.enemy, dmg, res.part);
          if (killed) onEnemyKilled(res.enemy, head);
          else { Wp.st.hits++; HUD.hitmarker(false); }
        }
      }
    } else { In.consume('reload'); HUD.setCrosshairEnemy(false); }
    Wp.update(dt, { speed: player.speed, grounded: player.grounded, crouch: player.crouch, lookYaw, lookPitch, fall: player.fall });
    const a = Wp.ammoText();
    HUD.setAmmo(a.mag, a.reserve, a.max, Wp.st.reloading);
  }

  // ---------------- Bucle ----------------
  let lastLookYaw = 0, lastLookPitch = 0;
  function loop() {
    requestAnimationFrame(loop);
    let dt = clock.getDelta();
    if (dt > 0.05) dt = 0.05;
    const t = clock.elapsedTime;

    if (game.state === 'playing') {
      game.time += dt;
      if (In.consume('pause')) { pauseGame(); return; }
      const before = { yaw: player.yaw, pitch: player.pitch };
      updatePlayer(dt);
      lastLookYaw = (player.yaw - before.yaw) / Math.max(dt, 1e-3) * 0.02;
      lastLookPitch = (player.pitch - before.pitch) / Math.max(dt, 1e-3) * 0.02;
      updateWeapon(dt, lastLookYaw, lastLookPitch);
      En.update(dt, player, t, { onPlayerHit }, MAX_ENEMIES);
      FX.update(dt);
      W.update(t);
      HUD.setVitals(player.shield, player.shieldMax, player.hp, player.hpMax, game.time - player.lastDamageT < 1);
      HUD.update(dt, player, En.list, En.projectiles);
      const fw = parseFloat($('flash-white').style.opacity || 0); if (fw > 0) HUD.flash(Math.max(0, fw - dt * 2));
    } else if (game.state === 'menu' || game.state === 'gameover') {
      // cámara orbitando el hangar de fondo
      In.consume('pause');
      const a = t * 0.15;
      yawObj.position.set(Math.sin(a) * 7, 2.2 + Math.sin(t * 0.5) * 0.3, Math.cos(a) * 7);
      yawObj.rotation.y = a + Math.PI / 2 + Math.sin(t * 0.3) * 0.3;
      pitchObj.rotation.x = -0.15;
      W.update(t);
      FX.update(dt);
    } else {
      In.consume('pause');
    }
    renderer.render(scene, camera);
  }

  ESF.Game = { player, game, get camera() { return camera; } };

  window.addEventListener('load', () => { try { init(); } catch (e) { $('loading').innerHTML = '<div class="loader">ERROR: ' + e.message + '</div>'; console.error(e); } });
})();
