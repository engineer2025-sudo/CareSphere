import React, { useEffect, useMemo, useState } from "react";
import { getLanguage, tr, useLanguage } from "../../lib/i18n.js";
import { pairThemes } from "../../lib/gameContent.js";
import { gameLevelSettings } from "../../lib/gamePreferences.js";
import MemoryCardBack from "./MemoryCardBack.jsx";
function shuffled(cards) {
  const result = [...cards];
  for (let index = result.length - 1; index > 0; index -= 1) {
    const swap = Math.floor(Math.random() * (index + 1));
    [result[index], result[swap]] = [result[swap], result[index]];
  }
  return result;
}
function makeDeck(theme, count) {
  const cards = shuffled(theme.pairs.slice(0, count).flatMap((pair) => [
    { ...pair, cardId: `${pair.pairId}-a` },
    { ...pair, cardId: `${pair.pairId}-b` }
  ]));
  if (count === 12) cards.splice(12, 0, { pairId: "free-space", cardId: "free-space", emoji: "\u2726", label: "Free space", isFree: true });
  return cards;
}
export default function MemoryPairs({ gameLevel = "gentle" }) {
  const languagePreference = useLanguage();
  const themes = useMemo(() => pairThemes(getLanguage()), [languagePreference]);
  const [themeId, setThemeId] = useState(themes[0]?.id || "nature");
  const [pairCount, setPairCount] = useState(gameLevelSettings(gameLevel).pairCount);
  const theme = themes.find((item) => item.id === themeId) || themes[0];
  const [cards, setCards] = useState(() => makeDeck(themes[0], gameLevelSettings(gameLevel).pairCount));
  const [opened, setOpened] = useState([]);
  const [matched, setMatched] = useState([]);
  const [status, setStatus] = useState("Choose two cards. You can take your time.");
  useEffect(() => {
    const localizedTheme = themes.find((item) => item.id === themeId) || themes[0];
    setCards(makeDeck(localizedTheme, pairCount));
    setOpened([]);
    setMatched([]);
    setStatus("Choose two cards. You can take your time.");
  }, [languagePreference]);
  const restart = (nextTheme = theme, nextCount = pairCount) => {
    setCards(makeDeck(nextTheme, nextCount));
    setOpened([]);
    setMatched([]);
    setStatus("Choose two cards. You can take your time.");
  };
  const chooseTheme = (event) => {
    const next = themes.find((item) => item.id === event.target.value) || themes[0];
    setThemeId(next.id);
    restart(next);
  };
  const chooseCount = (event) => {
    const count = Number(event.target.value);
    setPairCount(count);
    restart(theme, count);
  };
  const reveal = (card) => {
    if (opened.length >= 2 || matched.includes(card.pairId) || opened.includes(card.cardId)) return;
    const nextOpened = [...opened, card.cardId];
    setOpened(nextOpened);
    if (nextOpened.length < 2) return;
    const first = cards.find((item) => item.cardId === nextOpened[0]);
    if (first?.pairId === card.pairId) {
      const nextMatched = [...matched, card.pairId];
      setMatched(nextMatched);
      setOpened([]);
      setStatus(nextMatched.length === pairCount ? "All pairs are found. Start over or choose another theme." : "A pair matches. Continue whenever you like.");
    } else {
      setStatus("These pictures are different. They will stay open until you choose to hide them.");
    }
  };
  const canHide = opened.length === 2 && !matched.includes(cards.find((card) => card.cardId === opened[0])?.pairId);
  return <section className="local-game memory-game" aria-label={tr("Picture pairs")}>
    <header className="local-game-heading">
      <div><span className="game-kicker">{tr("Look, match, and enjoy")}</span><h3>{tr("Picture pairs")}</h3></div>
      <span className="offline-pill">{tr("No timer \xB7 no score")}</span>
    </header>
    <p className="game-note">{tr("Optional picture matching for enjoyment. This is not a memory test or a treatment. Unmatched cards stay visible until you choose to hide them.")}</p>
    {pairCount === 12 && <p className="game-note">{tr("The 5\xD75 board has twelve pairs and one open center tile.")}</p>}
    <div className="memory-controls">
      <label className="game-field">{tr("Picture theme")}
        <select className="library-input" value={themeId} onChange={chooseTheme}>{themes.map((item) => <option key={item.id} value={item.id}>{item.label}</option>)}</select>
      </label>
      <label className="game-field">{tr("Number of pairs")}
        <select className="library-input" value={pairCount} onChange={chooseCount}>{[2, 4, 6, 8, 12].map((count) => <option key={count} value={count}>{count === 12 ? tr("12 pairs \xB7 5\xD75") : tr("{0} pairs", [count])}</option>)}</select>
      </label>
      <button type="button" className="game-secondary" onClick={() => restart()}>{tr("Start over")}</button>
    </div>
    <p className="game-status" role="status" aria-live="polite">{tr(status)}</p>
    <div className="memory-board" style={{ "--memory-columns": pairCount === 12 ? 5 : pairCount <= 2 ? 2 : 4 }}>
      {cards.map((card, index) => {
    const isOpen = card.isFree || opened.includes(card.cardId) || matched.includes(card.pairId);
    const isMatched = card.isFree || matched.includes(card.pairId);
    const cardLabel = card.isFree ? tr("Free space") : card.label;
    return <button
      key={card.cardId}
      type="button"
      className={`memory-card ${isOpen ? "is-open" : ""} ${isMatched ? "is-matched" : ""} ${card.isFree ? "is-free" : ""}`}
      onClick={() => reveal(card)}
      disabled={isMatched || opened.length === 2}
      aria-pressed={isOpen}
      aria-label={isOpen ? `${cardLabel}${isMatched && !card.isFree ? ` \xB7 ${tr("pair found")}` : ""}` : tr("Hidden picture card {0}", [index + 1])}
    >
          {isOpen && !card.isFree ? <span className="memory-card-picture" aria-hidden="true"><img className="memory-card-art" src={card.asset} alt="" onError={(event) => {
      event.currentTarget.hidden = true;
      event.currentTarget.nextElementSibling.hidden = false;
    }} /><span className="memory-card-fallback" hidden>{card.emoji}</span></span> : <MemoryCardBack free={card.isFree} />}
          <span>{isOpen ? cardLabel : tr("Card {0}", [index + 1])}</span>
        </button>;
  })}
    </div>
    <div className="game-actions">
      {canHide && <button type="button" className="game-secondary" onClick={() => {
    setOpened([]);
    setStatus("Cards hidden. Choose two whenever you are ready.");
  }}>{tr("Hide cards")}</button>}
    </div>
    <details className="media-credit"><summary>{tr("Artwork credit")}</summary><p>All emojis designed by OpenMoji – the open-source emoji and icon project. <a href="https://creativecommons.org/licenses/by-sa/4.0/" target="_blank" rel="noreferrer">CC BY-SA 4.0</a>. {tr("Artwork is bundled for offline play. Attribution details are included with the assets.")}</p></details>
  </section>;
}
