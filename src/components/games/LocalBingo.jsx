import React, { useMemo, useState } from "react";
import { tr } from "../../lib/i18n.js";
import { gameLevelSettings } from "../../lib/gamePreferences.js";
import { bingoLabel, hasBingo, makeBingoCalls, makeBingoCards } from "../../lib/miniGames.js";
export default function LocalBingo({ gameLevel = "gentle" }) {
  const defaults = gameLevelSettings(gameLevel);
  const [playerCount, setPlayerCount] = useState(defaults.defaultPlayers);
  const [cards, setCards] = useState(() => makeBingoCards(defaults.defaultPlayers));
  const [callOrder, setCallOrder] = useState(() => makeBingoCalls());
  const [called, setCalled] = useState([]);
  const [marks, setMarks] = useState(() => Array.from({ length: defaults.defaultPlayers }, () => []));
  const [activePlayer, setActivePlayer] = useState(0);
  const calledSet = useMemo(() => new Set(called), [called]);
  const winnerIndex = cards.findIndex((card, index) => hasBingo(card, marks[index] || []));
  const currentCall = called.at(-1);
  const newGame = (count = playerCount) => {
    const safeCount = Math.max(2, Math.min(6, Number(count) || 2));
    setPlayerCount(safeCount);
    setCards(makeBingoCards(safeCount));
    setCallOrder(makeBingoCalls());
    setCalled([]);
    setMarks(Array.from({ length: safeCount }, () => []));
    setActivePlayer(0);
  };
  const callNext = () => {
    if (!callOrder.length || winnerIndex >= 0) return;
    const [next, ...remaining] = callOrder;
    setCalled((previous) => [...previous, next]);
    setCallOrder(remaining);
  };
  const toggleMark = (value) => {
    if (value === "FREE" || !calledSet.has(value) || winnerIndex >= 0) return;
    setMarks((previous) => previous.map((playerMarks, index) => index !== activePlayer ? playerMarks : playerMarks.includes(value) ? playerMarks.filter((mark) => mark !== value) : [...playerMarks, value]));
  };
  return <section className="local-game game-bingo" aria-label={tr("Bingo together")}>
    <header className="local-game-heading">
      <div><span className="game-kicker">{tr("Play together")}</span><h3>{tr("Bingo together")}</h3></div>
      <span className="offline-pill">{tr("On this device")}</span>
    </header>
    <p className="game-note">{tr("Pass the device between players. No account, internet, or server.")}</p>

    <div className="bingo-controls">
      <label className="game-field">{tr("Players")}
        <select className="library-input" value={playerCount} onChange={(event) => newGame(event.target.value)} aria-label={tr("Number of players")}>
          {[2, 3, 4, 5, 6].map((number) => <option key={number} value={number}>{number}</option>)}
        </select>
      </label>
      <button type="button" className="game-primary" onClick={callNext} disabled={!callOrder.length || winnerIndex >= 0}>{tr("Call next number")}</button>
      <button type="button" className="game-secondary" onClick={() => newGame()}>{tr("New game")}</button>
    </div>

    <div className="bingo-call" aria-live="polite" aria-atomic="true">
      <span>{currentCall ? tr("Current call") : tr("Ready when you are")}</span>
      <strong>{currentCall ? bingoLabel(currentCall) : "B \xB7 I \xB7 N \xB7 G \xB7 O"}</strong>
      <span>{tr("{0} called", [called.length])}</span>
    </div>

    <div className="bingo-player-tabs" role="group" aria-label={tr("Choose player card")}>
      {cards.map((_, index) => <button key={index} type="button" aria-pressed={activePlayer === index} onClick={() => setActivePlayer(index)}>
        {tr("Player {0}", [index + 1])}{winnerIndex === index ? " \u2605" : ""}
      </button>)}
    </div>

    {winnerIndex >= 0 && <p role="status" className="game-success">{tr("Bingo! Player {0} has a line.", [winnerIndex + 1])}</p>}
    <div className="bingo-board-wrap">
      <div className="bingo-board" role="group" aria-label={tr("Player {0}", [activePlayer + 1])}>
        {["B", "I", "N", "G", "O"].map((letter) => <span key={letter} className="bingo-letter">{letter}</span>)}
        {(cards[activePlayer] || []).map((value, index) => {
    const marked = value === "FREE" || marks[activePlayer]?.includes(value);
    const ready = value === "FREE" || calledSet.has(value);
    return <button
      type="button"
      key={`${activePlayer}-${index}`}
      className={`bingo-cell ${marked ? "is-marked" : ""} ${!ready ? "is-waiting" : ""}`}
      aria-pressed={marked}
      aria-label={value === "FREE" ? tr("Free space") : `${bingoLabel(value)}${marked ? ` \xB7 ${tr("marked")}` : ""}`}
      disabled={!ready || value === "FREE" || winnerIndex >= 0}
      onClick={() => toggleMark(value)}
    >{value === "FREE" ? tr("Free") : value}</button>;
  })}
      </div>
    </div>
    <p className="game-status" role="status" aria-live="polite">
      {winnerIndex >= 0 ? tr("Bingo! Player {0} has a line.", [winnerIndex + 1]) : currentCall ? tr("Called {0}. Mark this card if it matches.", [bingoLabel(currentCall)]) : tr("Call a number to begin.")}
    </p>
    <div className="called-list"><span>{tr("Called numbers")}</span><div>{called.length ? called.slice().reverse().slice(0, 12).map((number) => <span key={number}>{bingoLabel(number)}</span>) : <span>—</span>}</div></div>
  </section>;
}
