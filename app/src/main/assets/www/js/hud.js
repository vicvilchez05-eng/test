/* ESFshoot — HUD estilo Halo: escudo, munición, marcador, radar, killfeed, medallas */
window.ESF = window.ESF || {};

ESF.HUD = (function () {
  const el = {};
  let radarCtx = null;
  let lastMag = -1, lastRes = -1;
  let vignette = 0;
  let hitT = 0, dmgDirT = 0, dmgAngle = 0;

  function $(id) { return document.getElementById(id); }

  function init() {
    ['hud', 'shield-fill', 'health-fill', 'shield-alert', 'kills', 'deaths', 'goal', 'killfeed', 'medal', 'crosshair', 'hitmarker', 'radar', 'ammo-main', 'ammo-mag', 'ammo-res', 'ammo-bullets', 'reload-hint', 'dmg-vignette', 'dmg-dir', 'flash-white']
      .forEach(id => { el[id] = $(id); });
    radarCtx = el.radar.getContext('2d');
    buildBullets(30);
  }

  function buildBullets(n) {
    el['ammo-bullets'].innerHTML = '';
    for (let i = 0; i < n; i++) { const b = document.createElement('i'); el['ammo-bullets'].appendChild(b); }
  }

  function show(v) { el.hud.classList.toggle('hidden', !v); }

  function setScore(k, d, goal) { el.kills.textContent = k; el.deaths.textContent = d; el.goal.textContent = goal; }

  function setAmmo(mag, res, max, reloading) {
    if (mag !== lastMag) {
      el['ammo-mag'].textContent = mag;
      const kids = el['ammo-bullets'].children;
      for (let i = 0; i < kids.length; i++) kids[i].classList.toggle('empty', i >= mag);
      el['ammo-main'].classList.toggle('low', mag <= max * 0.2);
      lastMag = mag;
    }
    if (res !== lastRes) { el['ammo-res'].textContent = res; lastRes = res; }
    el['reload-hint'].classList.toggle('hidden', !reloading);
  }

  function setVitals(shield, shieldMax, hp, hpMax, hurtRecently) {
    el['shield-fill'].style.width = Math.max(0, shield / shieldMax * 100) + '%';
    el['shield-fill'].classList.toggle('hurt', hurtRecently && shield > 0);
    el['health-fill'].style.width = Math.max(0, hp / hpMax * 100) + '%';
    el['shield-alert'].classList.toggle('hidden', shield > 0);
  }

  function feed(text, bad) {
    const d = document.createElement('div');
    d.innerHTML = text;
    if (bad) d.classList.add('bad');
    el.killfeed.prepend(d);
    while (el.killfeed.children.length > 5) el.killfeed.lastChild.remove();
    setTimeout(() => { if (d.parentNode) d.remove(); }, 6000);
  }

  function medal(title, sub) {
    el.medal.innerHTML = title + (sub ? '<small>' + sub + '</small>' : '');
    el.medal.classList.remove('hidden');
    void el.medal.offsetWidth; // reinicia animación
    el.medal.style.animation = 'none'; void el.medal.offsetWidth; el.medal.style.animation = '';
    ESF.Audio.medal();
  }

  function hitmarker(kill) {
    el.hitmarker.classList.remove('hidden');
    el.hitmarker.classList.toggle('kill', !!kill);
    hitT = kill ? 0.35 : 0.12;
  }

  function damage(angle, strength) {
    vignette = Math.min(1, vignette + strength);
    dmgAngle = angle; dmgDirT = 0.7;
  }

  function flash(v) { el['flash-white'].style.opacity = v; }
  function setCrosshairEnemy(v) { el.crosshair.classList.toggle('enemy', v); }

  function drawRadar(player, enemies, projectiles) {
    const c = radarCtx, W = el.radar.width, R = W / 2, range = 22;
    c.clearRect(0, 0, W, W);
    // fondo
    c.beginPath(); c.arc(R, R, R - 2, 0, Math.PI * 2);
    c.fillStyle = 'rgba(0,20,35,0.6)'; c.fill();
    c.strokeStyle = 'rgba(25,230,255,0.5)'; c.lineWidth = 2; c.stroke();
    c.beginPath(); c.arc(R, R, (R - 2) * 0.5, 0, Math.PI * 2); c.strokeStyle = 'rgba(25,230,255,0.2)'; c.lineWidth = 1; c.stroke();
    c.beginPath(); c.moveTo(R, 4); c.lineTo(R, W - 4); c.moveTo(4, R); c.lineTo(W - 4, R); c.stroke();
    // barrido
    const sweep = (performance.now() / 1200) % (Math.PI * 2);
    const grd = c.createConicGradient ? c.createConicGradient(sweep, R, R) : null;
    if (grd) { grd.addColorStop(0, 'rgba(25,230,255,0.35)'); grd.addColorStop(0.15, 'rgba(25,230,255,0)'); grd.addColorStop(1, 'rgba(25,230,255,0)'); c.fillStyle = grd; c.beginPath(); c.arc(R, R, R - 3, 0, Math.PI * 2); c.fill(); }
    // cono de visión
    c.fillStyle = 'rgba(25,230,255,0.12)'; c.beginPath(); c.moveTo(R, R); c.arc(R, R, R - 3, -Math.PI / 2 - 0.6, -Math.PI / 2 + 0.6); c.closePath(); c.fill();
    const cy = Math.cos(player.yaw), sy = Math.sin(player.yaw);
    const toRadar = (x, z) => {
      const dx = x - player.pos.x, dz = z - player.pos.z;
      // rotar según orientación del jugador (yaw 0 = mirando -z)
      const rx = dx * cy - dz * sy, rz = dx * sy + dz * cy;
      return [R + rx / range * (R - 6), R + rz / range * (R - 6)];
    };
    for (const e of enemies) {
      if (!e.alive) continue;
      const d = Math.hypot(e.pos.x - player.pos.x, e.pos.z - player.pos.z);
      if (d > range) continue;
      const p = toRadar(e.pos.x, e.pos.z);
      c.fillStyle = e.type === 'plasma' ? '#5cff9d' : '#ff3c5a';
      c.shadowColor = c.fillStyle; c.shadowBlur = 6;
      c.beginPath(); c.arc(p[0], p[1], 3.5, 0, Math.PI * 2); c.fill();
      c.shadowBlur = 0;
    }
    for (const pr of projectiles) {
      const p = toRadar(pr.mesh.position.x, pr.mesh.position.z);
      c.fillStyle = 'rgba(255,255,255,0.6)'; c.fillRect(p[0] - 1, p[1] - 1, 2, 2);
    }
    // jugador
    c.fillStyle = '#ffffff'; c.beginPath(); c.moveTo(R, R - 6); c.lineTo(R + 4, R + 4); c.lineTo(R - 4, R + 4); c.closePath(); c.fill();
  }

  function update(dt, player, enemies, projectiles) {
    if (hitT > 0) { hitT -= dt; if (hitT <= 0) el.hitmarker.classList.add('hidden'); }
    vignette = Math.max(0, vignette - dt * 1.2);
    const lowHp = player.hp / player.hpMax < 0.35 && player.alive ? 0.25 + 0.15 * Math.sin(performance.now() / 150) : 0;
    el['dmg-vignette'].style.opacity = Math.min(1, vignette + lowHp);
    if (dmgDirT > 0) {
      dmgDirT -= dt;
      // ángulo relativo: dónde está el atacante respecto a la mira
      const rel = dmgAngle - player.yaw;
      el['dmg-dir'].style.transform = `rotate(${-rel}rad)`;
      el['dmg-dir'].style.opacity = Math.min(1, dmgDirT * 2);
    } else el['dmg-dir'].style.opacity = 0;
    drawRadar(player, enemies, projectiles);
  }

  return { init, show, setScore, setAmmo, setVitals, feed, medal, hitmarker, damage, flash, setCrosshairEnemy, update };
})();
