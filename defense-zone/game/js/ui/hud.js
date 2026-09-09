// =====================================================================
//  HUD de partida: mano de cartas, maná, temporizador, barras de vida.
// =====================================================================
import { CARDS, MANA, MATCH } from '../config.js';
import { SIDE_PLAYER } from '../sim/world.js';

const $ = (id) => document.getElementById(id);

export class Hud {
  constructor() {
    this.root = $('hud');
    this.hand = $('hand');
    this.manaFill = $('mana-fill');
    this.manaText = $('mana-text');
    this.timer = $('timer');
    this.phaseFill = $('phase-fill');
    this.phaseLabel = $('phase-label');
    this.hpPlayer = $('hp-player');
    this.hpEnemy = $('hp-enemy');
    this.nextIcon = $('next-icon');
    this.toastEl = $('toast');
    this.bannerEl = $('banner');
    this.cardbar = $('cardbar');
    this.topbar = $('topbar');
    this.handKey = '';
    this.cardEls = new Map();
    this.selected = null;
    this.onCardPointerDown = null;
    this.toastTimer = null;
    // Marcas de fase en la barra de tiempo
    const marks = document.querySelectorAll('#phase-bar .mark');
    if (marks[0]) marks[0].style.left = `${MATCH.rainStart / MATCH.duration * 100}%`;
    if (marks[1]) marks[1].style.left = `${MATCH.frenzyStart / MATCH.duration * 100}%`;
  }

  show() { this.root.classList.remove('hidden'); }
  hide() { this.root.classList.add('hidden'); }
  get cardbarHeight() { return this.cardbar.getBoundingClientRect().height; }
  get topbarHeight() { return this.topbar.getBoundingClientRect().height; }

  select(cardId) {
    this.selected = cardId;
    for (const [id, el] of this.cardEls) el.classList.toggle('selected', id === cardId);
  }

  rebuildHand(world) {
    const hand = world.hands[SIDE_PLAYER];
    const key = hand.join(',');
    if (key === this.handKey) return;
    this.handKey = key;
    this.hand.innerHTML = '';
    this.cardEls.clear();
    for (const id of hand) {
      const card = CARDS[id];
      const el = document.createElement('div');
      el.className = 'card';
      el.dataset.card = id;
      el.innerHTML = `<span class="cost">${card.cost}</span><span class="icon">${card.icon}</span><span class="name">${card.name}</span>`;
      el.title = card.desc;
      el.addEventListener('pointerdown', (ev) => this.onCardPointerDown && this.onCardPointerDown(id, ev, el));
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
    for (const [id, el] of this.cardEls) el.classList.toggle('unaffordable', CARDS[id].cost > mana);
    const next = CARDS[world.nextCard(SIDE_PLAYER)];
    this.nextIcon.textContent = next ? next.icon : '';
    const t = Math.min(world.time, MATCH.duration);
    const mm = String(Math.floor(t / 60)).padStart(2, '0'), ss = String(Math.floor(t % 60)).padStart(2, '0');
    this.timer.textContent = `${mm}:${ss}`;
    this.phaseFill.style.width = `${t / MATCH.duration * 100}%`;
    this.phaseLabel.textContent = world.frenzy ? '¡Velocidad x2!' : world.rain ? 'Lluvia' : '';
    this.phaseLabel.style.color = world.frenzy ? '#ff5a4e' : '#8fc6ff';
    const [p, a] = world.heroes;
    this.hpPlayer.style.width = `${Math.max(0, p.hp / p.maxHp * 100)}%`;
    this.hpEnemy.style.width = `${Math.max(0, a.hp / a.maxHp * 100)}%`;
  }

  toast(msg, ms = 1400) {
    this.toastEl.textContent = msg;
    this.toastEl.classList.add('show');
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toastEl.classList.remove('show'), ms);
  }

  banner(text, color = '#fff') {
    const el = this.bannerEl;
    el.classList.remove('hidden');
    el.style.color = color;
    el.textContent = text;
    // reinicia la animación
    el.style.animation = 'none'; void el.offsetWidth; el.style.animation = '';
    clearTimeout(this.bannerTimer);
    this.bannerTimer = setTimeout(() => el.classList.add('hidden'), 2600);
  }
}
