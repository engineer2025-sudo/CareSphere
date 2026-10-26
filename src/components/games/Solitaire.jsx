import React, { useState } from "react";
import { tr } from "../../lib/i18n.js";
import { canPlaceOnFoundation, canPlaceOnTableau, createSolitaireDeck, SOLITAIRE_SUITS } from "../../lib/miniGames.js";
function newGame() {
  const deck = createSolitaireDeck();
  const tableau = Array.from({ length: 7 }, (_, column) => {
    const pile = deck.splice(0, column + 1);
    return pile.map((card, index) => ({ ...card, faceUp: index === pile.length - 1 }));
  });
  return { stock: deck, waste: [], tableau, foundations: Array.from({ length: 4 }, () => []), moves: 0 };
}
function cloneGame(game) {
  return JSON.parse(JSON.stringify(game));
}
function rankLabel(rank) {
  return { 1: "A", 11: "J", 12: "Q", 13: "K" }[rank] || String(rank);
}
function cardName(card) {
  const suit = tr({ hearts: "hearts", diamonds: "diamonds", clubs: "clubs", spades: "spades" }[card.suit]);
  return tr("{0} of {1}", [rankLabel(card.rank), suit]);
}
const SUIT_PATHS = {
  hearts: "M12 21.2 2.8 11.7C-1.1 7.7 1.7 1.8 7 2.3c2.1.2 3.7 1.6 5 3.4 1.3-1.8 2.9-3.2 5-3.4 5.3-.5 8.1 5.4 4.2 9.4z",
  diamonds: "M12 1 23 12 12 23 1 12Z",
  clubs: "M12 2a4.5 4.5 0 0 0-4 6.6A4.5 4.5 0 1 0 11 16v4H7v2h10v-2h-4v-4a4.5 4.5 0 1 0 3-7.4A4.5 4.5 0 0 0 12 2Z",
  spades: "M12 1C9.1 4.2 2 8.7 2 14a6 6 0 0 0 10 4.2V22H8v1h8v-1h-4v-3.8A6 6 0 0 0 22 14c0-5.3-7.1-9.8-10-13Z"
};
function SuitSymbol({ suit, className = "" }) {
  return <svg className={className} viewBox="0 0 24 24" aria-hidden="true"><path d={SUIT_PATHS[suit]} fill="currentColor" /></svg>;
}
const PIP_POSITIONS = {
  2: [[50, 27], [50, 113]],
  3: [[50, 25], [50, 70], [50, 115]],
  4: [[31, 29], [69, 29], [31, 111], [69, 111]],
  5: [[31, 29], [69, 29], [50, 70], [31, 111], [69, 111]],
  6: [[31, 27], [69, 27], [31, 70], [69, 70], [31, 113], [69, 113]],
  7: [[31, 25], [69, 25], [50, 48], [31, 70], [69, 70], [31, 115], [69, 115]],
  8: [[31, 25], [69, 25], [31, 51], [69, 51], [31, 89], [69, 89], [31, 115], [69, 115]],
  9: [[31, 24], [69, 24], [31, 49], [69, 49], [50, 70], [31, 91], [69, 91], [31, 116], [69, 116]],
  10: [[31, 23], [69, 23], [31, 47], [69, 47], [31, 70], [69, 70], [31, 93], [69, 93], [31, 117], [69, 117]]
};
function CourtCardArt({ card }) {
  const accent = card.suit === "hearts" ? "#b52c3d" : card.suit === "diamonds" ? "#a84438" : card.suit === "clubs" ? "#14584a" : "#233a5c";
  const gold = "#d5aa53";
  const id = `court-${card.id}`;
  const crown = card.rank === 13 ? "M31 24 L27 12 L39 19 L50 8 L61 19 L73 12 L69 25 Z" : card.rank === 12 ? "M31 24 Q34 12 42 18 Q50 6 58 18 Q66 12 69 24 Z" : "M30 23 L33 12 L42 18 L50 9 L58 18 L67 12 L70 23 Z";
  return <svg className="playing-card-art court-card-art" viewBox="0 0 100 140" aria-hidden="true">
    <defs>
      <linearGradient id={`${id}-paper`} x1="0" y1="0" x2="1" y2="1"><stop stopColor="#fff9e8" /><stop offset=".5" stopColor="#f3dfb0" /><stop offset="1" stopColor="#fffdf4" /></linearGradient>
      <linearGradient id={`${id}-robe`} x1="0" y1="0" x2="1" y2="1"><stop stopColor={accent} /><stop offset="1" stopColor="#182a31" /></linearGradient>
      <pattern id={`${id}-dots`} width="8" height="8" patternUnits="userSpaceOnUse"><circle cx="2" cy="2" r=".8" fill={gold} /></pattern>
    </defs>
    <rect x="4" y="4" width="92" height="132" rx="12" fill={`url(#${id}-paper)`} stroke={gold} strokeWidth="2" />
    <rect x="9" y="9" width="82" height="122" rx="9" fill="none" stroke={accent} strokeWidth="1.5" />
    <path d="M13 33 Q50 6 87 33 L87 50 Q50 37 13 50Z" fill={`url(#${id}-dots)`} opacity=".9" />
    <path d="M17 128 Q18 99 34 83 L50 77 L66 83 Q82 99 83 128Z" fill={`url(#${id}-robe)`} stroke={accent} strokeWidth="2" />
    <path d="M30 102 L50 87 L70 102 L63 128 L37 128Z" fill="#f4e8c8" stroke={gold} strokeWidth="2" />
    <path d="M43 92 L50 86 L57 92 L54 116 L46 116Z" fill={accent} />
    <ellipse cx="50" cy="48" rx="17" ry="21" fill="#efc49a" stroke="#805b48" strokeWidth="1.5" />
    <path d="M33 46 Q34 25 50 25 Q66 25 67 46 L62 41 Q58 35 52 37 Q43 33 37 43Z" fill={card.rank === 11 ? "#38291f" : "#412b22"} />
    <path d={crown} fill={gold} stroke="#8d672e" strokeWidth="2" strokeLinejoin="round" />
    <circle cx="39" cy="14" r="2" fill={accent} /><circle cx="50" cy="11" r="2.5" fill={accent} /><circle cx="61" cy="14" r="2" fill={accent} />
    <path d="M43 51 Q46 49 48 51 M53 51 Q56 49 58 51" fill="none" stroke="#49352e" strokeWidth="1.6" strokeLinecap="round" />
    <path d="M46 61 Q50 64 54 61" fill="none" stroke="#a55049" strokeWidth="1.5" strokeLinecap="round" />
    <path d="M36 78 L50 88 L64 78 L59 73 L50 78 L41 73Z" fill="#fff8e8" stroke={gold} strokeWidth="1.5" />
    <path d="M22 76 Q31 66 36 73 L33 87 Q26 88 22 76 M78 76 Q69 66 64 73 L67 87 Q74 88 78 76" fill={accent} stroke={gold} strokeWidth="1.5" />
    <path d="M18 31 Q25 23 30 30 M82 31 Q75 23 70 30" fill="none" stroke={gold} strokeWidth="2" />
    <path d={SUIT_PATHS[card.suit]} transform="translate(42 114) scale(.65)" fill={accent} />
  </svg>;
}
function CardArtwork({ card }) {
  const rank = card.rank;
  if (rank >= 11) return <CourtCardArt card={card} />;
  if (rank === 1) return <span className="playing-card-art ace-card-art" aria-hidden="true">
    <span className="ace-medallion" style={{ color: card.red ? "#b52c3d" : "#183d35" }}><SuitSymbol suit={card.suit} className="ace-suit-icon" /></span>
  </span>;
  const positions = PIP_POSITIONS[rank] || [];
  return <span className="playing-card-art pip-card-art" aria-hidden="true">
    {positions.map(([x, y], index) => {
    const row = y < 37 ? 1 : y < 60 ? 2 : y < 82 ? 3 : y < 104 ? 4 : 5;
    const column = x === 50 ? "1 / span 2" : x < 50 ? "1" : "2";
    return <span key={`${x}-${y}-${index}`} className="card-pip" style={{ gridColumn: column, gridRow: row, color: card.red ? "#b52c3d" : "#183d35", transform: y > 70 ? "rotate(180deg)" : void 0 }}><SuitSymbol suit={card.suit} /></span>;
  })}
  </span>;
}
function CardFace({ card, selected = false }) {
  return <span className={`playing-card ${card?.faceUp ? "" : "is-back"} ${card?.red ? "is-red" : ""} ${selected ? "is-selected" : ""}`}>
    {card?.faceUp ? <>
      <span className="playing-card-index">{rankLabel(card.rank)}<SuitSymbol suit={card.suit} /></span>
      <CardArtwork card={card} />
      <span className="playing-card-index playing-card-index-lower" aria-hidden="true">{rankLabel(card.rank)}<SuitSymbol suit={card.suit} /></span>
    </> : <span className="card-back-pattern" aria-hidden="true"><svg viewBox="0 0 100 140"><rect x="5" y="5" width="90" height="130" rx="12" /><rect x="12" y="12" width="76" height="116" rx="9" /><path d="M50 24 78 70 50 116 22 70Z" /><path d="M50 38 68 70 50 102 32 70Z" /><circle cx="50" cy="70" r="8" /><path d="M50 53v34M33 70h34" /></svg></span>}
  </span>;
}
function PlayingCard({ card, selected = false, onClick, disabled = false, label }) {
  return <button type="button" className={`playing-card-button ${selected ? "is-selected" : ""}`} aria-label={label || (card?.faceUp ? cardName(card) : tr("Face-down card"))} aria-pressed={selected} disabled={disabled} onClick={onClick}>
    <CardFace card={card} selected={selected} />
  </button>;
}
export default function Solitaire({ gameLevel = "gentle" }) {
  const [game, setGame] = useState(newGame);
  const [history, setHistory] = useState([]);
  const [selected, setSelected] = useState(null);
  const [drawCount, setDrawCount] = useState(gameLevel === "challenge" ? 3 : 1);
  const completed = game.foundations.reduce((count, pile) => count + pile.length, 0) === 52;
  const commit = (next) => {
    setHistory((previous) => [...previous.slice(-39), cloneGame(game)]);
    setGame({ ...next, moves: game.moves + 1 });
    setSelected(null);
  };
  const getSelectedCards = () => {
    if (!selected) return [];
    if (selected.source === "waste") return game.waste.length ? [game.waste.at(-1)] : [];
    if (selected.source === "tableau") return game.tableau[selected.pile]?.slice(selected.index) || [];
    return [];
  };
  const takeSelectedFromSource = (next) => {
    if (selected.source === "waste") {
      next.waste = next.waste.slice(0, -1);
    } else {
      const pile = [...next.tableau[selected.pile]];
      pile.splice(selected.index);
      if (pile.length && !pile.at(-1).faceUp) pile[pile.length - 1] = { ...pile.at(-1), faceUp: true };
      next.tableau[selected.pile] = pile;
    }
  };
  const moveToTableau = (targetPile) => {
    if (!selected || selected.source === "tableau" && selected.pile === targetPile) return false;
    const moving = getSelectedCards();
    const destination = game.tableau[targetPile];
    const top = destination?.at(-1) || null;
    if (!moving.length || !canPlaceOnTableau(moving[0], top)) return false;
    const next = cloneGame(game);
    takeSelectedFromSource(next);
    next.tableau[targetPile] = [...next.tableau[targetPile], ...moving];
    commit(next);
    return true;
  };
  const moveToFoundation = (foundationIndex) => {
    if (!selected) return;
    const moving = getSelectedCards();
    const foundation = game.foundations[foundationIndex];
    if (moving.length !== 1 || !canPlaceOnFoundation(moving[0], foundation)) return;
    const next = cloneGame(game);
    takeSelectedFromSource(next);
    next.foundations[foundationIndex] = [...next.foundations[foundationIndex], moving[0]];
    commit(next);
  };
  const selectWaste = () => {
    if (selected?.source === "waste") setSelected(null);
    else if (game.waste.length) setSelected({ source: "waste" });
  };
  const selectTableau = (pileIndex, cardIndex) => {
    const pile = game.tableau[pileIndex];
    const card = pile[cardIndex];
    if (!card.faceUp) {
      if (cardIndex === pile.length - 1) {
        const next = cloneGame(game);
        next.tableau[pileIndex][cardIndex] = { ...card, faceUp: true };
        commit(next);
      }
      return;
    }
    if (selected && !(selected.source === "tableau" && selected.pile === pileIndex)) {
      if (moveToTableau(pileIndex)) return;
    }
    if (selected?.source === "tableau" && selected.pile === pileIndex && selected.index === cardIndex) setSelected(null);
    else setSelected({ source: "tableau", pile: pileIndex, index: cardIndex });
  };
  const clickStock = () => {
    setSelected(null);
    if (game.stock.length) {
      const next = cloneGame(game);
      const drawn = next.stock.splice(-Math.min(drawCount, next.stock.length)).reverse().map((card) => ({ ...card, faceUp: true }));
      next.waste = [...next.waste, ...drawn];
      commit(next);
      return;
    }
    if (game.waste.length) {
      const next = cloneGame(game);
      next.stock = [...next.waste].reverse().map((card) => ({ ...card, faceUp: false }));
      next.waste = [];
      commit(next);
    }
  };
  const restart = () => {
    setGame(newGame());
    setHistory([]);
    setSelected(null);
  };
  const undo = () => {
    if (!history.length) return;
    setGame(history.at(-1));
    setHistory((previous) => previous.slice(0, -1));
    setSelected(null);
  };
  return <section className="local-game solitaire-game" aria-label={tr("Solitaire")}>
    <header className="local-game-heading">
      <div><span className="game-kicker">{tr("Card game")}</span><h3>{tr("Solitaire")}</h3></div>
      <span className="offline-pill">{tr("No timer")}</span>
    </header>
    <div className="solitaire-toolbar">
      <label className="game-field">{tr("Draw cards")}
        <select className="library-input" value={drawCount} onChange={(event) => setDrawCount(Number(event.target.value))}>
          <option value="1">{tr("Draw one")}</option><option value="3">{tr("Draw three")}</option>
        </select>
      </label>
      <button type="button" className="game-secondary" onClick={undo} disabled={!history.length}>{tr("Undo")}</button>
      <button type="button" className="game-secondary" onClick={restart}>{tr("New game")}</button>
    </div>
    {completed && <p className="game-success" role="status">{tr("All cards are home. Play again whenever you like.")}</p>}
    <div className="solitaire-top-row">
      <button type="button" className="solitaire-slot stock-control" onClick={clickStock} aria-label={game.stock.length ? tr("Stock: {0} cards. Draw cards", [game.stock.length]) : tr("Return waste to stock")}>
        {game.stock.length ? <CardFace card={{ faceUp: false }} /> : <span className="solitaire-empty">↻</span>}
      </button>
      <div className="solitaire-slot" role="group" aria-label={tr("Waste pile")}>{game.waste.length ? <PlayingCard card={game.waste.at(-1)} selected={selected?.source === "waste"} onClick={selectWaste} /> : <span className="solitaire-empty">{tr("Waste")}</span>}</div>
      <div className="solitaire-foundations">
        {SOLITAIRE_SUITS.map((suit, index) => {
    const pile = game.foundations[index];
    const card = pile.at(-1);
    return <button type="button" key={suit.id} className="solitaire-slot" aria-label={tr("Foundation {0}", [tr(suit.id)])} onClick={() => moveToFoundation(index)}>
            {card ? <CardFace card={card} /> : <span className={`solitaire-empty ${suit.red ? "is-red" : ""}`}>{suit.symbol}</span>}
          </button>;
  })}
      </div>
    </div>
    <div className="solitaire-tableau" role="group" aria-label={tr("Tableau piles")}>
      {game.tableau.map((pile, pileIndex) => <div className="tableau-pile" key={pileIndex}>
        {pile.length ? pile.map((card, cardIndex) => <div className="tableau-card" key={card.id}>
          <PlayingCard
    card={card}
    selected={selected?.source === "tableau" && selected.pile === pileIndex && cardIndex >= selected.index}
    label={card.faceUp ? tr("Pile {0}, {1}", [pileIndex + 1, cardName(card)]) : tr("Pile {0}, face-down card", [pileIndex + 1])}
    onClick={() => selectTableau(pileIndex, cardIndex)}
  />
        </div>) : <button type="button" className="solitaire-empty tableau-empty" aria-label={tr("Empty pile {0}", [pileIndex + 1])} onClick={() => moveToTableau(pileIndex)}>K</button>}
      </div>)}
    </div>
    <p className="game-status" role="status" aria-live="polite">{selected ? tr("Card selected. Choose a legal pile, or tap it again to cancel.") : tr("Select a card, then choose a legal pile. The exposed card turns over automatically.")}</p>
  </section>;
}
