// =====================================================================
//  Motor de audio: música y efectos 100% sintetizados con Web Audio.
//  No hay ficheros de audio, así el paquete es mínimo y sin licencias.
// =====================================================================
export class AudioEngine {
  constructor(settings) {
    this.settings = settings;
    this.ctx = null;
    this.musicGain = null;
    this.sfxGain = null;
    this.noiseBuffer = null;
    this.seq = null;      // estado del secuenciador de música
    this.rainNode = null;
    this.muted = false;
  }

  /** Crea el contexto en el primer gesto del usuario (requisito de los navegadores). */
  ensure() {
    if (this.ctx) { if (this.ctx.state === 'suspended') this.ctx.resume(); return true; }
    const AC = window.AudioContext || window.webkitAudioContext;
    if (!AC) return false;
    this.ctx = new AC();
    this.master = this.ctx.createGain();
    this.master.connect(this.ctx.destination);
    this.musicGain = this.ctx.createGain();
    this.sfxGain = this.ctx.createGain();
    this.musicGain.connect(this.master);
    this.sfxGain.connect(this.master);
    // Compresor suave para que los efectos no saturen
    const comp = this.ctx.createDynamicsCompressor();
    comp.threshold.value = -12; comp.ratio.value = 4;
    this.sfxGain.disconnect(); this.sfxGain.connect(comp); comp.connect(this.master);
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

  setMuted(v) { this.muted = v; this.applyVolumes(); }

  // ------------------------------------------------------------------
  //  Utilidades de síntesis
  // ------------------------------------------------------------------
  _noise(dest, { duration = 0.2, filter = 'lowpass', freq = 1000, freqEnd = null, q = 1, gain = 0.5, attack = 0.005, when = 0 } = {}) {
    const c = this.ctx, t = c.currentTime + when;
    const src = c.createBufferSource(); src.buffer = this.noiseBuffer; src.loop = true;
    const f = c.createBiquadFilter(); f.type = filter; f.frequency.setValueAtTime(freq, t); f.Q.value = q;
    if (freqEnd) f.frequency.exponentialRampToValueAtTime(Math.max(20, freqEnd), t + duration);
    const g = c.createGain(); g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(gain, t + attack); g.gain.exponentialRampToValueAtTime(0.0001, t + duration);
    src.connect(f); f.connect(g); g.connect(dest);
    src.start(t); src.stop(t + duration + 0.05);
  }
  _tone(dest, { type = 'sine', freq = 440, freqEnd = null, duration = 0.2, gain = 0.3, attack = 0.005, when = 0, decay = null } = {}) {
    const c = this.ctx, t = c.currentTime + when;
    const o = c.createOscillator(); o.type = type; o.frequency.setValueAtTime(freq, t);
    if (freqEnd) o.frequency.exponentialRampToValueAtTime(Math.max(20, freqEnd), t + duration);
    const g = c.createGain(); g.gain.setValueAtTime(0, t); g.gain.linearRampToValueAtTime(gain, t + attack);
    g.gain.exponentialRampToValueAtTime(0.0001, t + (decay || duration));
    o.connect(g); g.connect(dest); o.start(t); o.stop(t + (decay || duration) + 0.05);
  }

  // ------------------------------------------------------------------
  //  Efectos de sonido
  // ------------------------------------------------------------------
  sfx(name, opts = {}) {
    if (!this.ctx || this.muted) return;
    const d = this.sfxGain;
    const vol = opts.volume ?? 1;
    switch (name) {
      case 'shot': this._noise(d, { duration: 0.07, filter: 'highpass', freq: 1800, gain: 0.35 * vol }); this._tone(d, { type: 'square', freq: 220, freqEnd: 60, duration: 0.06, gain: 0.12 * vol }); break;
      case 'turret': this._noise(d, { duration: 0.05, filter: 'bandpass', freq: 2500, q: 2, gain: 0.25 * vol }); break;
      case 'arrow': this._noise(d, { duration: 0.25, filter: 'bandpass', freq: 3000, freqEnd: 600, q: 4, gain: 0.3 * vol }); break;
      case 'cannon': this._tone(d, { type: 'sine', freq: 140, freqEnd: 35, duration: 0.6, gain: 0.9 * vol }); this._noise(d, { duration: 0.5, filter: 'lowpass', freq: 1200, freqEnd: 100, gain: 0.7 * vol }); break;
      case 'explosion': this._noise(d, { duration: 0.9, filter: 'lowpass', freq: 2500, freqEnd: 80, gain: 0.9 * vol }); this._tone(d, { type: 'sine', freq: 90, freqEnd: 25, duration: 0.8, gain: 0.8 * vol }); break;
      case 'hit': this._noise(d, { duration: 0.05, filter: 'bandpass', freq: 900, q: 3, gain: 0.2 * vol }); break;
      case 'melee': this._noise(d, { duration: 0.09, filter: 'lowpass', freq: 700, gain: 0.35 * vol }); this._tone(d, { type: 'triangle', freq: 160, freqEnd: 70, duration: 0.09, gain: 0.25 * vol }); break;
      case 'place': this._tone(d, { type: 'sine', freq: 110, freqEnd: 50, duration: 0.18, gain: 0.6 * vol }); this._noise(d, { duration: 0.12, filter: 'lowpass', freq: 900, gain: 0.3 * vol }); break;
      case 'log': this._noise(d, { duration: 1.2, filter: 'lowpass', freq: 300, freqEnd: 120, gain: 0.6 * vol, attack: 0.1 }); break;
      case 'wire': this._noise(d, { duration: 0.3, filter: 'highpass', freq: 3500, gain: 0.25 * vol }); break;
      case 'click': this._tone(d, { type: 'square', freq: 1400, duration: 0.04, gain: 0.15 * vol }); break;
      case 'select': this._tone(d, { type: 'triangle', freq: 660, freqEnd: 990, duration: 0.08, gain: 0.2 * vol }); break;
      case 'error': this._tone(d, { type: 'sawtooth', freq: 160, freqEnd: 110, duration: 0.18, gain: 0.2 * vol }); break;
      case 'thunder': this._noise(d, { duration: 1.8, filter: 'lowpass', freq: 600, freqEnd: 60, gain: 0.8 * vol, attack: 0.03 }); this._tone(d, { type: 'sine', freq: 60, freqEnd: 30, duration: 1.5, gain: 0.5 * vol }); break;
      case 'frenzy': for (let i = 0; i < 3; i++) this._tone(d, { type: 'sawtooth', freq: 300, freqEnd: 900, duration: 0.35, gain: 0.25 * vol, when: i * 0.35 }); break;
      case 'countdown': this._tone(d, { type: 'square', freq: 880, duration: 0.12, gain: 0.2 * vol }); break;
      case 'go': this._tone(d, { type: 'square', freq: 1320, duration: 0.35, gain: 0.25 * vol }); break;
      case 'victory': [523, 659, 784, 1047, 1319].forEach((f, i) => this._tone(d, { type: 'triangle', freq: f, duration: 0.5, gain: 0.35 * vol, when: i * 0.14, decay: 0.9 })); break;
      case 'defeat': [392, 349, 311, 233].forEach((f, i) => this._tone(d, { type: 'sawtooth', freq: f, duration: 0.7, gain: 0.25 * vol, when: i * 0.32, decay: 0.9 })); break;
      case 'heroDeath': this._noise(d, { duration: 1.4, filter: 'lowpass', freq: 3000, freqEnd: 60, gain: 1.0 * vol }); this._tone(d, { type: 'sine', freq: 70, freqEnd: 20, duration: 1.3, gain: 0.9 * vol }); break;
      case 'rainStart': this._noise(d, { duration: 0.8, filter: 'highpass', freq: 4000, gain: 0.3 * vol, attack: 0.4 }); break;
    }
  }

  startRain() {
    if (!this.ctx || this.rainNode) return;
    const c = this.ctx;
    const src = c.createBufferSource(); src.buffer = this.noiseBuffer; src.loop = true;
    const f = c.createBiquadFilter(); f.type = 'bandpass'; f.frequency.value = 5000; f.Q.value = 0.6;
    const g = c.createGain(); g.gain.setValueAtTime(0.0001, c.currentTime); g.gain.exponentialRampToValueAtTime(0.28, c.currentTime + 2.5);
    src.connect(f); f.connect(g); g.connect(this.sfxGain); src.start();
    this.rainNode = { src, g };
  }
  stopRain() {
    if (!this.rainNode) return;
    const { src, g } = this.rainNode; this.rainNode = null;
    g.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + 2.5);
    src.stop(this.ctx.currentTime + 2.6);
  }

  // ------------------------------------------------------------------
  //  Música: secuenciador por pasos con lookahead
  // ------------------------------------------------------------------
  playMusic(mode = 'menu') {
    if (!this.ctx) return;
    if (this.seq && this.seq.mode === mode) return;
    this.stopMusic();
    const bpm = mode === 'menu' ? 84 : 126;
    this.seq = { mode, bpm, tempoMult: 1, step: 0, nextTime: this.ctx.currentTime + 0.1, timer: null, bar: 0 };
    this.seq.timer = setInterval(() => this._schedule(), 60);
  }
  setTempoMult(m) { if (this.seq) this.seq.tempoMult = m; }
  stopMusic() {
    if (!this.seq) return;
    clearInterval(this.seq.timer);
    this.seq = null;
  }

  _schedule() {
    const s = this.seq; if (!s) return;
    const c = this.ctx;
    const stepDur = 60 / (s.bpm * s.tempoMult) / 4; // semicorcheas
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
    // Escala menor de Mi (tensión bélica): E2 G2 A2 B2 C3 D3 E3
    const bassNotes = s.mode === 'menu'
      ? [82.41, 82.41, 98, 82.41]
      : [82.41, 82.41, 98, 110];
    const root = bassNotes[bar];
    if (s.mode === 'menu') {
      // Menú: tambores lentos, bajo profundo y colchón grave
      if (i === 0 || i === 8) this._tone(d, { type: 'sine', freq: 70, freqEnd: 35, duration: 0.35, gain: 0.9, when });
      if (i === 4 || i === 12) this._noise(d, { duration: 0.15, filter: 'bandpass', freq: 1800, q: 1, gain: 0.18, when });
      if (i % 4 === 0) this._tone(d, { type: 'sawtooth', freq: root, duration: stepDur * 4, gain: 0.12, when, attack: 0.05 });
      if (i === 0) { this._tone(d, { type: 'triangle', freq: root * 2, duration: stepDur * 16, gain: 0.06, when, attack: 0.6 }); this._tone(d, { type: 'triangle', freq: root * 3, duration: stepDur * 16, gain: 0.04, when, attack: 0.8 }); }
      if (i === 14 && bar === 3) this._noise(d, { duration: 0.6, filter: 'lowpass', freq: 500, freqEnd: 80, gain: 0.3, when });
    } else {
      // Batalla: marcha militar rápida
      if (i % 4 === 0) this._tone(d, { type: 'sine', freq: 75, freqEnd: 38, duration: 0.25, gain: 0.9, when });
      if (i === 4 || i === 12 || (i === 15 && bar % 2 === 1)) this._noise(d, { duration: 0.14, filter: 'bandpass', freq: 2200, q: 1, gain: 0.25, when });
      if (i % 2 === 0) this._noise(d, { duration: 0.03, filter: 'highpass', freq: 7000, gain: 0.08, when });
      if (i % 4 === 2) this._noise(d, { duration: 0.06, filter: 'highpass', freq: 5000, gain: 0.05, when });
      const pattern = [1, 0, 1, 0, 1, 0, 1.5, 0, 1, 0, 1, 0, 1.5, 0, 2, 0];
      if (pattern[i]) this._tone(d, { type: 'sawtooth', freq: root * pattern[i], duration: stepDur * 1.8, gain: 0.14, when, attack: 0.01 });
      // Fanfarria de metales cada 2 compases
      if (bar % 2 === 0 && (i === 0 || i === 3 || i === 6)) this._tone(d, { type: 'square', freq: root * 4 * (i === 6 ? 1.2 : 1), duration: stepDur * 2.5, gain: 0.05, when, attack: 0.02 });
      if (s.tempoMult > 1 && i % 8 === 4) this._tone(d, { type: 'square', freq: root * 6, duration: stepDur, gain: 0.04, when });
    }
  }
}
