/* ESFshoot — audio sintetizado con WebAudio (sin archivos externos) */
window.ESF = window.ESF || {};

ESF.Audio = (function () {
  let ctx = null;
  let master = null;
  let enabled = true;

  function init() {
    if (ctx) return;
    try {
      const AC = window.AudioContext || window.webkitAudioContext;
      ctx = new AC();
      master = ctx.createGain();
      master.gain.value = 0.5;
      master.connect(ctx.destination);
    } catch (e) { ctx = null; }
  }

  function resume() {
    if (ctx && ctx.state === 'suspended') ctx.resume();
  }

  function setEnabled(v) {
    enabled = v;
    if (master) master.gain.value = v ? 0.5 : 0;
  }

  function noise(duration) {
    const len = Math.floor(ctx.sampleRate * duration);
    const buf = ctx.createBuffer(1, len, ctx.sampleRate);
    const d = buf.getChannelData(0);
    for (let i = 0; i < len; i++) d[i] = Math.random() * 2 - 1;
    const src = ctx.createBufferSource();
    src.buffer = buf;
    return src;
  }

  function env(gainNode, t, a, peak, decay) {
    gainNode.gain.setValueAtTime(0.0001, t);
    gainNode.gain.linearRampToValueAtTime(peak, t + a);
    gainNode.gain.exponentialRampToValueAtTime(0.0001, t + a + decay);
  }

  function tone(type, f0, f1, dur, peak, t0) {
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = type;
    o.frequency.setValueAtTime(f0, t0);
    o.frequency.exponentialRampToValueAtTime(Math.max(1, f1), t0 + dur);
    env(g, t0, 0.005, peak, dur);
    o.connect(g); g.connect(master);
    o.start(t0); o.stop(t0 + dur + 0.05);
  }

  // Disparo del fusil de pulso: ruido corto + clic grave
  function shoot() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    const n = noise(0.18);
    const f = ctx.createBiquadFilter();
    f.type = 'lowpass'; f.frequency.setValueAtTime(3800, t); f.frequency.exponentialRampToValueAtTime(400, t + 0.18);
    const g = ctx.createGain();
    env(g, t, 0.003, 0.7, 0.16);
    n.connect(f); f.connect(g); g.connect(master);
    n.start(t);
    tone('square', 220, 60, 0.08, 0.35, t);
    tone('sine', 1400, 900, 0.05, 0.15, t);
  }

  function dry() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('square', 900, 700, 0.04, 0.2, t);
  }

  // Recarga: clics mecánicos en varios pasos
  function reloadClick(step) {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    if (step === 0) { tone('triangle', 500, 200, 0.08, 0.3, t); }
    else if (step === 1) { const n = noise(0.06); const g = ctx.createGain(); env(g, t, 0.002, 0.3, 0.05); n.connect(g); g.connect(master); n.start(t); }
    else { tone('square', 300, 1200, 0.06, 0.25, t); tone('triangle', 800, 300, 0.1, 0.25, t + 0.04); }
  }

  function plasma() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('sawtooth', 320, 90, 0.35, 0.25, t);
    tone('sine', 900, 200, 0.3, 0.15, t);
  }

  function laser() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('sawtooth', 2200, 300, 0.22, 0.22, t);
    tone('square', 1100, 150, 0.18, 0.12, t);
  }

  function hit() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('sine', 1800, 1200, 0.06, 0.3, t);
  }

  function kill() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('sine', 700, 1400, 0.12, 0.3, t);
    tone('sine', 1400, 2100, 0.16, 0.25, t + 0.08);
  }

  function hurt() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    const n = noise(0.25);
    const f = ctx.createBiquadFilter(); f.type = 'bandpass'; f.frequency.value = 500;
    const g = ctx.createGain(); env(g, t, 0.01, 0.5, 0.22);
    n.connect(f); f.connect(g); g.connect(master); n.start(t);
    tone('sawtooth', 160, 60, 0.3, 0.3, t);
  }

  function shieldDown() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('square', 800, 200, 0.4, 0.25, t);
    tone('square', 600, 150, 0.4, 0.2, t + 0.12);
  }

  function shieldUp() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('sine', 400, 1600, 0.5, 0.18, t);
  }

  function jump() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('triangle', 200, 400, 0.12, 0.15, t);
  }

  function step() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    const n = noise(0.05);
    const f = ctx.createBiquadFilter(); f.type = 'lowpass'; f.frequency.value = 600;
    const g = ctx.createGain(); env(g, t, 0.002, 0.12, 0.05);
    n.connect(f); f.connect(g); g.connect(master); n.start(t);
  }

  function die() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('sawtooth', 300, 40, 1.2, 0.35, t);
    tone('sine', 150, 30, 1.2, 0.3, t);
  }

  function explode() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    const n = noise(0.6);
    const f = ctx.createBiquadFilter(); f.type = 'lowpass'; f.frequency.setValueAtTime(2000, t); f.frequency.exponentialRampToValueAtTime(100, t + 0.6);
    const g = ctx.createGain(); env(g, t, 0.01, 0.6, 0.55);
    n.connect(f); f.connect(g); g.connect(master); n.start(t);
  }

  function ui() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    tone('sine', 1200, 1800, 0.08, 0.15, t);
  }

  function medal() {
    if (!ctx || !enabled) return;
    const t = ctx.currentTime;
    [660, 880, 1320].forEach((f, i) => tone('triangle', f, f, 0.18, 0.2, t + i * 0.09));
  }

  // Zumbido ambiental de la nave
  let amb = null;
  function ambient(on) {
    if (!ctx) return;
    if (on && !amb) {
      const o = ctx.createOscillator(); o.type = 'sawtooth'; o.frequency.value = 48;
      const o2 = ctx.createOscillator(); o2.type = 'sine'; o2.frequency.value = 96.5;
      const f = ctx.createBiquadFilter(); f.type = 'lowpass'; f.frequency.value = 180;
      const g = ctx.createGain(); g.gain.value = 0.06;
      o.connect(f); o2.connect(f); f.connect(g); g.connect(master);
      o.start(); o2.start();
      amb = { o, o2, g };
    } else if (!on && amb) {
      amb.o.stop(); amb.o2.stop(); amb = null;
    }
  }

  return { init, resume, setEnabled, shoot, dry, reloadClick, plasma, laser, hit, kill, hurt, shieldDown, shieldUp, jump, step, die, explode, ui, medal, ambient };
})();
