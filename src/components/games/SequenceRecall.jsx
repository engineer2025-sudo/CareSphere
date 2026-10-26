import React, { useMemo, useState } from "react";
import { getLanguage, tr, useLanguage } from "../../lib/i18n.js";
import { pairThemes } from "../../lib/gameContent.js";
import { gameLevelSettings } from "../../lib/gamePreferences.js";
import MemoryCardBack from "./MemoryCardBack.jsx";
function shuffle(items) {
  const result = [...items];
  for (let index = result.length - 1; index > 0; index -= 1) {
    const swap = Math.floor(Math.random() * (index + 1));
    [result[index], result[swap]] = [result[swap], result[index]];
  }
  return result;
}
export default function SequenceRecall({ gameLevel = "gentle" }) {
  const languagePreference = useLanguage();
  const pictures = useMemo(() => pairThemes(getLanguage()).find((theme) => theme.id === "nature")?.pairs || [], [languagePreference]);
  const defaultCount = gameLevel === "challenge" ? 6 : gameLevel === "steady" ? 4 : 3;
  const [patternSize, setPatternSize] = useState(defaultCount);
  const [patternIds, setPatternIds] = useState([]);
  const [chosenIds, setChosenIds] = useState([]);
  const [patternVisible, setPatternVisible] = useState(true);
  const [started, setStarted] = useState(false);
  const [message, setMessage] = useState("Start a picture pattern whenever you are ready.");
  const isMatched = started && !patternVisible && chosenIds.length === patternIds.length && patternIds.every((id) => chosenIds.includes(id));
  const startNew = () => {
    setPatternIds(shuffle(pictures).slice(0, patternSize).map((picture) => picture.pairId));
    setChosenIds([]);
    setPatternVisible(true);
    setStarted(true);
    setMessage("The picture pattern is visible. Hide it when you are ready.");
  };
  const changePatternSize = (event) => {
    setPatternSize(Number(event.target.value));
    setStarted(false);
    setPatternIds([]);
    setChosenIds([]);
    setMessage("Start a picture pattern whenever you are ready.");
  };
  const togglePicture = (picture) => {
    if (!started || patternVisible || isMatched) return;
    const next = chosenIds.includes(picture.pairId) ? chosenIds.filter((id) => id !== picture.pairId) : chosenIds.length < patternIds.length ? [...chosenIds, picture.pairId] : chosenIds;
    setChosenIds(next);
    if (next.length === patternIds.length) {
      const correct = patternIds.every((id) => next.includes(id));
      setMessage(correct ? "Pattern matched. You can start again whenever you like." : "That is different. Show the pattern or change your choices.");
    }
  };
  const showPattern = () => {
    setPatternVisible(true);
    setChosenIds([]);
    setMessage("The picture pattern is visible. Hide it when you are ready.");
  };
  const hidePattern = () => {
    setPatternVisible(false);
    setChosenIds([]);
    setMessage("Pattern hidden. Select the same pictures.");
  };
  return <section className="local-game sequence-recall-game" aria-label={tr("Picture pattern recall")}>
    <header className="local-game-heading">
      <div><span className="game-kicker">{tr("Picture memory game")}</span><h3>{tr("Picture pattern recall")}</h3></div>
      <span className="offline-pill">{tr("No timer \xB7 no score")}</span>
    </header>
    <p id="sequence-recall-help" className="game-note">{tr("Look at the picture pattern, hide it when ready, then choose the same pictures. You can reveal it again at any time.")}</p>
    <div className="memory-controls">
      <label className="game-field">{tr("Pattern size")}
        <select className="library-input" value={patternSize} onChange={changePatternSize}>
          {[3, 4, 6].map((count) => <option key={count} value={count}>{tr("{0} pictures", [count])}</option>)}
        </select>
      </label>
      <button type="button" className="game-secondary" onClick={startNew}>{tr("Start a new pattern")}</button>
      {started && <button type="button" className="game-secondary" onClick={patternVisible ? hidePattern : showPattern}>{tr(patternVisible ? "Hide pattern" : "Show pattern")}</button>}
      {!patternVisible && chosenIds.length > 0 && <button type="button" className="game-secondary" onClick={() => {
    setChosenIds([]);
    setMessage("Choices cleared. Select the pictures whenever you are ready.");
  }}>{tr("Clear choices")}</button>}
    </div>
    <p className="game-status" role="status" aria-live="polite">{tr(message)}</p>
    <div className="memory-board sequence-recall-board" style={{ "--memory-columns": 4 }} role="group" aria-label={started && patternVisible ? tr("Pattern: {0}", [pictures.filter((picture) => patternIds.includes(picture.pairId)).map((picture) => picture.label).join(", ")]) : tr("Picture choices")}>
      {pictures.map((picture, index) => {
    const inPattern = patternIds.includes(picture.pairId);
    const isOpen = started && patternVisible && inPattern;
    const isChosen = chosenIds.includes(picture.pairId);
    const isOpenOrChosen = isOpen || isChosen;
    return <button
      key={picture.pairId}
      type="button"
      className={`memory-card ${isOpenOrChosen ? "is-open" : ""} ${isMatched && isChosen ? "is-matched" : ""}`}
      onClick={() => togglePicture(picture)}
      disabled={!started || patternVisible || isMatched}
      aria-pressed={isOpenOrChosen}
      aria-label={isOpenOrChosen ? `${picture.label}${isChosen ? ` \xB7 ${tr("Selected")}` : ""}` : tr("Picture choice {0}", [index + 1])}
      aria-describedby="sequence-recall-help"
    >
          {isOpenOrChosen ? <span className="memory-card-picture" aria-hidden="true"><img className="memory-card-art" src={picture.asset} alt="" onError={(event) => {
      event.currentTarget.hidden = true;
      event.currentTarget.nextElementSibling.hidden = false;
    }} /><span className="memory-card-fallback" hidden>{picture.emoji}</span></span> : <MemoryCardBack />}
          <span>{isOpenOrChosen ? picture.label : tr("Card {0}", [index + 1])}</span>
        </button>;
  })}
    </div>
  </section>;
}
