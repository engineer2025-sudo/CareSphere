import React, { useEffect, useMemo, useState } from "react";
import { getLanguage, tr, useLanguage } from "../../lib/i18n.js";
import { wordSearchThemes } from "../../lib/gameContent.js";
import { gameLevelSettings } from "../../lib/gamePreferences.js";
import { makeWordSearch, normalizeWord, wordSearchSelection } from "../../lib/miniGames.js";
function newPuzzle(theme, size) {
  return makeWordSearch(theme.words, size);
}
function normalizedEndpoints(start, end) {
  if (start.row === end.row && start.column > end.column) return [end, start];
  if (start.column === end.column && start.row > end.row) return [end, start];
  return [start, end];
}
function cellsBetween(start, end) {
  const [first, last] = normalizedEndpoints(start, end);
  const delta = first.row === last.row ? { row: 0, column: 1 } : { row: 1, column: 0 };
  const length = Math.max(Math.abs(last.row - first.row), Math.abs(last.column - first.column)) + 1;
  return Array.from({ length }, (_, index) => `${first.row + delta.row * index}:${first.column + delta.column * index}`);
}
export default function WordSearch({ gameLevel = "gentle" }) {
  const languagePreference = useLanguage();
  const themes = useMemo(() => wordSearchThemes(getLanguage()), [languagePreference]);
  const [themeId, setThemeId] = useState(themes[0]?.id || "nature");
  const [size, setSize] = useState(gameLevelSettings(gameLevel).wordGridSize);
  const theme = themes.find((item) => item.id === themeId) || themes[0];
  const [puzzle, setPuzzle] = useState(() => newPuzzle(themes[0], gameLevelSettings(gameLevel).wordGridSize));
  const [start, setStart] = useState(null);
  const [foundWords, setFoundWords] = useState([]);
  const [foundCells, setFoundCells] = useState([]);
  const [status, setStatus] = useState("Choose the first and last letter of a word in the same row or column.");
  useEffect(() => {
    const localizedTheme = themes.find((item) => item.id === themeId) || themes[0];
    setPuzzle(newPuzzle(localizedTheme, size));
    setStart(null);
    setFoundWords([]);
    setFoundCells([]);
    setStatus("Choose the first and last letter of a word in the same row or column.");
  }, [languagePreference]);
  const foundSet = new Set(foundCells);
  const startKey = start ? `${start.row}:${start.column}` : "";
  const restart = (nextTheme = theme, nextSize = size) => {
    setPuzzle(newPuzzle(nextTheme, nextSize));
    setStart(null);
    setFoundWords([]);
    setFoundCells([]);
    setStatus("Choose the first and last letter of a word in the same row or column.");
  };
  const chooseTheme = (event) => {
    const next = themes.find((item) => item.id === event.target.value) || themes[0];
    setThemeId(next.id);
    restart(next);
  };
  const chooseSize = (event) => {
    const nextSize = Number(event.target.value);
    setSize(nextSize);
    restart(theme, nextSize);
  };
  const chooseCell = (row, column) => {
    const cell = { row, column };
    if (!start) {
      setStart(cell);
      setStatus("Choose the last letter of that word.");
      return;
    }
    if (start.row !== row && start.column !== column) {
      setStart(cell);
      setStatus("Choose cells in the same row or column. Try again from this letter.");
      return;
    }
    const [first, last] = normalizedEndpoints(start, cell);
    const word = wordSearchSelection(puzzle.grid, first, last, puzzle.words);
    setStart(null);
    if (!word) {
      setStatus("No listed word there. Choose another first letter.");
      return;
    }
    if (foundWords.some((item) => normalizeWord(item) === normalizeWord(word))) {
      setStatus("That word is already found. Choose another one.");
      return;
    }
    setFoundWords((previous) => [...previous, word]);
    setFoundCells((previous) => [... new Set([...previous, ...cellsBetween(first, last)])]);
    setStatus(tr("Found: {0}", [word]));
  };
  const isComplete = puzzle.words.length > 0 && foundWords.length === puzzle.words.length;
  return <section className="local-game word-search-game" aria-label={tr("Word search")}>
    <header className="local-game-heading">
      <div><span className="game-kicker">{tr("A quiet word puzzle")}</span><h3>{tr("Word search")}</h3></div>
      <span className="offline-pill">{tr("No timer")}</span>
    </header>
    <p className="game-note">{tr("Find each word in a straight line across or down. Tap its first and last letter; reverse direction is fine.")}</p>
    <div className="word-search-controls">
      <label className="game-field">{tr("Word theme")}
        <select className="library-input" value={themeId} onChange={chooseTheme}>{themes.map((item) => <option key={item.id} value={item.id}>{item.label}</option>)}</select>
      </label>
      <label className="game-field">{tr("Grid size")}
        <select className="library-input" value={size} onChange={chooseSize}>{[10, 12].map((value) => <option key={value} value={value}>{tr("{0} by {0}", [value])}</option>)}</select>
      </label>
      <button type="button" className="game-secondary" onClick={() => restart()}>{tr("New puzzle")}</button>
    </div>
    {isComplete && <p className="game-success" role="status">{tr("All words found. You can start a new puzzle whenever you like.")}</p>}
    <div className="word-search-layout">
      <div className="word-search-grid" role="group" aria-label={tr("{0} by {0} word grid", [size])} style={{ "--word-grid-size": size }}>
        {puzzle.grid.flatMap((row, rowIndex) => row.map((letter, columnIndex) => {
    const key = `${rowIndex}:${columnIndex}`;
    return <button
      key={key}
      type="button"
      className={`word-search-cell ${foundSet.has(key) ? "is-found" : ""} ${key === startKey ? "is-start" : ""}`}
      aria-pressed={foundSet.has(key) || key === startKey}
      aria-label={tr("Row {0}, column {1}, {2}", [rowIndex + 1, columnIndex + 1, letter])}
      onClick={() => chooseCell(rowIndex, columnIndex)}
    >{letter}</button>;
  }))}
      </div>
      <div className="word-search-list">
        <h4>{tr("Words to find")}</h4>
        <ul>{puzzle.words.map((word) => {
    const isFound = foundWords.some((item) => normalizeWord(item) === normalizeWord(word));
    return <li key={word} className={isFound ? "is-found" : ""}>{isFound && <span aria-hidden="true">✓ </span>}{word}</li>;
  })}</ul>
        {!puzzle.words.length && <p>{tr("No words fit this grid. Choose a larger grid.")}</p>}
      </div>
    </div>
    <p className="game-status" role="status" aria-live="polite">{tr(status)}</p>
  </section>;
}
