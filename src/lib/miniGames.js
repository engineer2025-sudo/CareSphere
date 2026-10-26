const BINGO_COLUMNS = [[1, 15], [16, 30], [31, 45], [46, 60], [61, 75]];
function shuffled(values, random = Math.random) {
  const result = [...values];
  for (let index = result.length - 1; index > 0; index -= 1) {
    const swap = Math.floor(random() * (index + 1));
    [result[index], result[swap]] = [result[swap], result[index]];
  }
  return result;
}
export function makeBingoCalls(random = Math.random) {
  return shuffled(Array.from({ length: 75 }, (_, index) => index + 1), random);
}
export function makeBingoCards(playerCount = 2, random = Math.random) {
  const count = Math.max(2, Math.min(6, Math.trunc(Number(playerCount) || 2)));
  return Array.from({ length: count }, () => {
    const columns = BINGO_COLUMNS.map(([min, max], column) => {
      const numbers = shuffled(Array.from({ length: max - min + 1 }, (_, index) => min + index), random);
      return numbers.slice(0, column === 2 ? 4 : 5);
    });
    return Array.from({ length: 25 }, (_, index) => {
      const row = Math.floor(index / 5);
      const column = index % 5;
      if (row === 2 && column === 2) return "FREE";
      const columnIndex = column === 2 && row > 2 ? row - 1 : row;
      return columns[column][columnIndex];
    });
  });
}
export function bingoLabel(number) {
  if (!Number.isInteger(number) || number < 1 || number > 75) return "";
  const letter = number <= 15 ? "B" : number <= 30 ? "I" : number <= 45 ? "N" : number <= 60 ? "G" : "O";
  return `${letter} ${number}`;
}
export function hasBingo(card, markedValues) {
  if (!Array.isArray(card) || card.length !== 25) return false;
  const marked = new Set(markedValues);
  const lines = [];
  for (let index = 0; index < 5; index += 1) {
    lines.push(Array.from({ length: 5 }, (_, offset) => index * 5 + offset));
    lines.push(Array.from({ length: 5 }, (_, offset) => offset * 5 + index));
  }
  lines.push([0, 6, 12, 18, 24], [4, 8, 12, 16, 20]);
  return lines.some((line) => line.every((index) => card[index] === "FREE" || marked.has(card[index])));
}
export function normalizeWord(word) {
  return String(word ?? "").normalize("NFC").replace(/[\s-]+/gu, "").toLocaleUpperCase();
}
export function makeWordSearch(words, size = 8, random = Math.random) {
  const dimension = Math.max(5, Math.min(12, Math.trunc(Number(size) || 8)));
  const normalized = [...new Set(words.map(normalizeWord).filter((word) => word.length > 1 && Array.from(word).length <= dimension))];
  const grid = Array.from({ length: dimension }, () => Array(dimension).fill(""));
  const placed = [];
  const directions = [[0, 1], [1, 0]];
  for (const word of [...normalized].sort((a, b) => Array.from(b).length - Array.from(a).length)) {
    const letters = Array.from(word);
    let success = false;
    for (let attempt = 0; attempt < 160 && !success; attempt += 1) {
      const [rowStep, columnStep] = directions[Math.floor(random() * directions.length)];
      const row = Math.floor(random() * dimension);
      const column = Math.floor(random() * dimension);
      const endRow = row + rowStep * (letters.length - 1);
      const endColumn = column + columnStep * (letters.length - 1);
      if (endRow >= dimension || endColumn >= dimension) continue;
      const fits = letters.every((letter, index) => {
        const existing = grid[row + rowStep * index][column + columnStep * index];
        return !existing || existing === letter;
      });
      if (!fits) continue;
      letters.forEach((letter, index) => {
        grid[row + rowStep * index][column + columnStep * index] = letter;
      });
      placed.push(word);
      success = true;
    }
  }
  const alphabet = Array.from("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
  grid.forEach((row) => row.forEach((letter, index) => {
    if (!letter) row[index] = alphabet[Math.floor(random() * alphabet.length)];
  }));
  return { grid, words: placed };
}
export function wordSearchSelection(grid, start, end, words) {
  if (!grid?.length || !start || !end) return null;
  const rowDelta = end.row - start.row;
  const columnDelta = end.column - start.column;
  if (rowDelta !== 0 && columnDelta !== 0) return null;
  if (rowDelta < 0 || columnDelta < 0) return null;
  const length = Math.max(rowDelta, columnDelta) + 1;
  if (rowDelta > 0 && columnDelta > 0) return null;
  const letters = Array.from({ length }, (_, index) => grid[start.row + (rowDelta ? index : 0)]?.[start.column + (columnDelta ? index : 0)] ?? "");
  if (letters.some((letter) => !letter)) return null;
  const selected = normalizeWord(letters.join(""));
  return words.find((word) => normalizeWord(word) === selected) ?? null;
}
export const SOLITAIRE_SUITS = [
  { id: "clubs", symbol: "\u2663", red: false },
  { id: "diamonds", symbol: "\u2666", red: true },
  { id: "hearts", symbol: "\u2665", red: true },
  { id: "spades", symbol: "\u2660", red: false }
];
export function createSolitaireDeck(random = Math.random) {
  return shuffled(SOLITAIRE_SUITS.flatMap((suit) => Array.from({ length: 13 }, (_, index) => ({
    id: `${suit.id}-${index + 1}`,
    suit: suit.id,
    symbol: suit.symbol,
    red: suit.red,
    rank: index + 1,
    faceUp: false
  }))), random);
}
export function canPlaceOnTableau(card, destination) {
  if (!card || !card.faceUp) return false;
  if (!destination) return card.rank === 13;
  return destination.faceUp && destination.rank === card.rank + 1 && destination.red !== card.red;
}
export function canPlaceOnFoundation(card, foundation) {
  if (!card || !card.faceUp) return false;
  if (!foundation.length) return card.rank === 1;
  const top = foundation[foundation.length - 1];
  return top.suit === card.suit && top.rank + 1 === card.rank;
}
