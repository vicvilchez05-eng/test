/* ESFshoot — entrada: joystick táctil, zona de mira, botones, teclado y ratón */
window.ESF = window.ESF || {};

ESF.Input = (function () {
  const state = {
    move: { x: 0, y: 0 },      // joystick: x = derecha, y = adelante (-1..1)
    lookDX: 0, lookDY: 0,      // delta de mira acumulado por frame (px)
    fire: false,
    jump: false,               // pulsación (se consume)
    crouch: false,             // toggle en móvil, mantener en PC
    reload: false,             // pulsación (se consume)
    pause: false,              // pulsación
    touch: false,
    pointerLocked: false
  };

  const settings = { sensitivity: 1.0, invertY: false };

  let stickTouch = null;   // id del toque que controla el joystick
  let lookTouch = null;    // id del toque que controla la mira
  let stickCenter = { x: 0, y: 0 };
  const STICK_RADIUS = 46;
  let el = {};
  let enabled = false;

  function $(id) { return document.getElementById(id); }

  function init() {
    el = {
      zoneMove: $('zone-move'), zoneLook: $('zone-look'), base: $('stick-base'), knob: $('stick-knob'),
      fire: $('btn-fire'), reload: $('btn-reload'), jump: $('btn-jump'), crouch: $('btn-crouch'), pause: $('btn-pause'),
      canvas: $('gl')
    };
    state.touch = ('ontouchstart' in window) || navigator.maxTouchPoints > 0;
    if (!state.touch) document.body.classList.add('desktop');

    // ----- Joystick -----
    el.zoneMove.addEventListener('touchstart', onStickStart, { passive: false });
    el.zoneMove.addEventListener('touchmove', onStickMove, { passive: false });
    el.zoneMove.addEventListener('touchend', onStickEnd, { passive: false });
    el.zoneMove.addEventListener('touchcancel', onStickEnd, { passive: false });

    // ----- Zona de mira -----
    el.zoneLook.addEventListener('touchstart', onLookStart, { passive: false });
    el.zoneLook.addEventListener('touchmove', onLookMove, { passive: false });
    el.zoneLook.addEventListener('touchend', onLookEnd, { passive: false });
    el.zoneLook.addEventListener('touchcancel', onLookEnd, { passive: false });

    // ----- Botones -----
    bindHold(el.fire, v => { state.fire = v; });
    bindTap(el.reload, () => { state.reload = true; });
    bindTap(el.jump, () => { state.jump = true; });
    bindTap(el.crouch, () => { state.crouch = !state.crouch; el.crouch.classList.toggle('on', state.crouch); });
    bindTap(el.pause, () => { state.pause = true; });

    // ----- Teclado / ratón (PC) -----
    window.addEventListener('keydown', onKey(true));
    window.addEventListener('keyup', onKey(false));
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mousedown', e => { if (!enabled) return; if (e.button === 0 && state.pointerLocked) state.fire = true; });
    document.addEventListener('mouseup', e => { if (e.button === 0) state.fire = false; });
    document.addEventListener('pointerlockchange', () => { state.pointerLocked = document.pointerLockElement === el.canvas; });
    el.canvas.addEventListener('click', () => { if (enabled && !state.touch && !state.pointerLocked) el.canvas.requestPointerLock(); });
    window.addEventListener('contextmenu', e => e.preventDefault());
  }

  function bindHold(btn, cb) {
    const down = e => { e.preventDefault(); e.stopPropagation(); btn.classList.add('on'); cb(true); };
    const up = e => { e.preventDefault(); e.stopPropagation(); btn.classList.remove('on'); cb(false); };
    btn.addEventListener('touchstart', down, { passive: false });
    btn.addEventListener('touchend', up, { passive: false });
    btn.addEventListener('touchcancel', up, { passive: false });
    btn.addEventListener('mousedown', down);
    btn.addEventListener('mouseup', up);
    btn.addEventListener('mouseleave', up);
  }

  function bindTap(btn, cb) {
    const down = e => { e.preventDefault(); e.stopPropagation(); btn.classList.add('flash'); cb(); setTimeout(() => btn.classList.remove('flash'), 120); };
    btn.addEventListener('touchstart', down, { passive: false });
    btn.addEventListener('mousedown', e => { if (e.button === 0) down(e); });
    btn.addEventListener('touchend', e => { e.preventDefault(); e.stopPropagation(); }, { passive: false });
  }

  // ----- Joystick -----
  function onStickStart(e) {
    e.preventDefault();
    if (stickTouch !== null) return;
    const t = e.changedTouches[0];
    stickTouch = t.identifier;
    const r = el.base.getBoundingClientRect();
    // El joystick aparece donde el jugador toca (flotante) pero limitado a la zona
    stickCenter = { x: t.clientX, y: t.clientY };
    el.base.style.left = (t.clientX - r.width / 2) + 'px';
    el.base.style.top = (t.clientY - r.height / 2) + 'px';
    el.base.style.bottom = 'auto';
    el.base.style.opacity = '1';
    updateStick(t.clientX, t.clientY);
  }
  function onStickMove(e) {
    e.preventDefault();
    for (const t of e.changedTouches) if (t.identifier === stickTouch) updateStick(t.clientX, t.clientY);
  }
  function onStickEnd(e) {
    e.preventDefault();
    for (const t of e.changedTouches) if (t.identifier === stickTouch) {
      stickTouch = null;
      state.move.x = 0; state.move.y = 0;
      el.knob.style.transform = 'translate(0px, 0px)';
      el.base.style.opacity = '0.85';
    }
  }
  function updateStick(x, y) {
    let dx = x - stickCenter.x, dy = y - stickCenter.y;
    const d = Math.hypot(dx, dy);
    if (d > STICK_RADIUS) { dx = dx / d * STICK_RADIUS; dy = dy / d * STICK_RADIUS; }
    el.knob.style.transform = `translate(${dx}px, ${dy}px)`;
    const dead = 0.12;
    let nx = dx / STICK_RADIUS, ny = -dy / STICK_RADIUS;
    const m = Math.hypot(nx, ny);
    if (m < dead) { nx = 0; ny = 0; }
    state.move.x = nx; state.move.y = ny;
  }

  // ----- Mira táctil -----
  let lookLast = { x: 0, y: 0 };
  function onLookStart(e) {
    e.preventDefault();
    if (lookTouch !== null) return;
    const t = e.changedTouches[0];
    lookTouch = t.identifier;
    lookLast = { x: t.clientX, y: t.clientY };
  }
  function onLookMove(e) {
    e.preventDefault();
    for (const t of e.changedTouches) if (t.identifier === lookTouch) {
      state.lookDX += (t.clientX - lookLast.x);
      state.lookDY += (t.clientY - lookLast.y);
      lookLast = { x: t.clientX, y: t.clientY };
    }
  }
  function onLookEnd(e) {
    e.preventDefault();
    for (const t of e.changedTouches) if (t.identifier === lookTouch) lookTouch = null;
  }

  // ----- Teclado -----
  const keys = {};
  function onKey(down) {
    return function (e) {
      if (!enabled && down && e.code !== 'Escape') return;
      const c = e.code;
      keys[c] = down;
      if (['Space', 'KeyW', 'KeyA', 'KeyS', 'KeyD', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(c)) e.preventDefault();
      if (down && !e.repeat) {
        if (c === 'Space') state.jump = true;
        if (c === 'KeyR') state.reload = true;
        if (c === 'Escape' || c === 'KeyP') state.pause = true;
      }
      if (c === 'KeyC' || c === 'ControlLeft' || c === 'ShiftLeft') state.crouch = !!(keys.KeyC || keys.ControlLeft || keys.ShiftLeft);
      const fx = (keys.KeyD || keys.ArrowRight ? 1 : 0) - (keys.KeyA || keys.ArrowLeft ? 1 : 0);
      const fy = (keys.KeyW || keys.ArrowUp ? 1 : 0) - (keys.KeyS || keys.ArrowDown ? 1 : 0);
      if (!state.touch || fx || fy) { state.move.x = fx; state.move.y = fy; }
    };
  }
  function onMouseMove(e) {
    if (!state.pointerLocked || !enabled) return;
    state.lookDX += e.movementX;
    state.lookDY += e.movementY;
  }

  function setEnabled(v) {
    enabled = v;
    if (!v) {
      state.fire = false; state.move.x = 0; state.move.y = 0; stickTouch = null; lookTouch = null;
      state.lookDX = 0; state.lookDY = 0;
      el.fire.classList.remove('on');
      if (document.pointerLockElement) document.exitPointerLock();
    } else if (!state.touch) {
      try { el.canvas.requestPointerLock(); } catch (err) { /* ignorado */ }
    }
  }

  // Devuelve y consume los deltas de mira (en radianes)
  function consumeLook() {
    const k = 0.0028 * settings.sensitivity;
    const yaw = -state.lookDX * k;
    const pitch = -state.lookDY * k * (settings.invertY ? -1 : 1);
    state.lookDX = 0; state.lookDY = 0;
    return { yaw, pitch };
  }
  function consume(name) { const v = state[name]; state[name] = false; return v; }
  function resetCrouch() { state.crouch = false; el.crouch.classList.remove('on'); }

  return { state, settings, init, setEnabled, consumeLook, consume, resetCrouch };
})();
