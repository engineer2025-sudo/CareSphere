export const ACTIVITIES = [
  { id: "sortTogether", title: "Sort together", category: "thinking", summary: "Find what belongs together. Hints whenever you want.", symbol: "\u25C8" },
  { id: "completePattern", title: "Complete the pattern", category: "thinking", summary: "Notice what comes next in a repeating row.", symbol: "\u25CC" },
  { id: "findSame", title: "Find the same", category: "thinking", summary: "Keep a word in view and find its matching choice.", symbol: "\u25CE" },
  { id: "everydayOrder", title: "Everyday order", category: "everyday", summary: "Explore a familiar task, one step at a time.", symbol: "\u2600" },
  { id: "conversation", title: "Conversation cards", category: "connection", summary: "Share a preference or imagine a story. Nothing is recorded.", symbol: "\u260F" },
  { id: "picturePairs", title: "Picture pairs", category: "thinking", summary: "Reveal familiar pictures and find a matching pair.", symbol: "\u25A6" },
  { id: "feelings", title: "Feelings explorer", category: "connection", summary: "Explore feeling words. Every choice is welcome.", symbol: "\u2661" },
  { id: "pictureStory", title: "Picture story", category: "everyday", summary: "Move through an imagined story at your own pace.", symbol: "\u25A4" },
  { id: "patternRecall", title: "Remember a pattern", category: "thinking", summary: "Look, recall, and try again. No timer or score.", symbol: "\u2727" },
  { id: "sequenceRecall", title: "Picture pattern recall", category: "thinking", summary: "View a picture pattern, hide it when ready, and choose the same pictures.", symbol: "square.grid.3x3" },
  { id: "soundscapes", title: "Soundscapes", category: "calm", summary: "Optional sound. Nothing plays until you choose.", symbol: "\u2248" },
  { id: "breathing", title: "Paced breathing", category: "calm", summary: "A gentle pace with clear pause and stop controls.", symbol: "\u274B" },
  { id: "socialMoments", title: "Social moment cards", category: "connection", summary: "Explore everyday situations. There is no best response.", symbol: "\u2637" },
  { id: "bingo", title: "Bingo together", category: "connection", summary: "A pass-and-play bingo game for two to six people on one device.", symbol: "\u25A6" },
  { id: "solitaire", title: "Solitaire", category: "thinking", summary: "Play a familiar card game. No timer or score.", symbol: "\u2660" },
  { id: "wordSearch", title: "Word search", category: "thinking", summary: "Find familiar words across or down in a quiet puzzle.", symbol: "\u2315" }
];
export const CATEGORIES = { all: "All activities", thinking: "Notice & think", everyday: "Everyday steps", connection: "Connect & express", calm: "Find calm" };
export function normalizeFavorites(ids) {
  return [...new Set((Array.isArray(ids) ? ids : []).filter((id) => ACTIVITIES.some((a) => a.id === id)))].sort();
}
export function toggleFavorite(id, ids) {
  const current = normalizeFavorites(ids);
  return normalizeFavorites(current.includes(id) ? current.filter((item) => item !== id) : [...current, id]);
}
export function matchesSearch(text, query) {
  const fold = (value) => String(value).normalize("NFD").replace(/\p{M}/gu, "").toLocaleLowerCase();
  const haystack = fold(text);
  return fold(query).trim().split(/\s+/).every((word) => haystack.includes(word));
}
export function filterActivities({ query = "", category = "all", favoritesOnly = false, favorites = [], translate = (value) => value } = {}) {
  const saved = normalizeFavorites(favorites);
  return ACTIVITIES.filter((a) => (category === "all" || a.category === category) && (!favoritesOnly || saved.includes(a.id)) && matchesSearch(`${a.title} ${a.summary} ${translate(a.title)} ${translate(a.summary)}`, query));
}
export const CHOICE_ROUNDS = {
  sortTogether: [
    { prompt: "Which one is a fruit?", options: ["Apple", "Chair", "Shoe"], answer: 0, hint: "An apple grows on a tree." },
    { prompt: "Which one do you wear?", options: ["Cup", "Hat", "Pear"], answer: 1, hint: "A hat goes on your head." },
    { prompt: "Which one is used for a drink?", options: ["Sock", "Leaf", "Cup"], answer: 2, hint: "You can fill a cup with water." }
  ],
  completePattern: [
    { prompt: "Circle \xB7 Star \xB7 Circle \xB7 \u2026", options: ["Square", "Star", "Leaf"], answer: 1, hint: "Circle and star take turns." },
    { prompt: "Leaf \xB7 Leaf \xB7 Flower \xB7 Leaf \xB7 Leaf \xB7 \u2026", options: ["Flower", "Star", "Circle"], answer: 0, hint: "Two leaves, then a flower." }
  ],
  findSame: [
    { prompt: "Find the same: Flower", options: ["Leaf", "Flower", "Star"], answer: 1, hint: "Look for Flower." },
    { prompt: "Find the same: Circle", options: ["Circle", "Square", "Star"], answer: 0, hint: "Look for Circle." }
  ],
  everydayOrder: [
    { prompt: "When washing your hands, what comes after wetting them?", options: ["Dry hands", "Add soap", "Put on gloves"], answer: 1, hint: "Soap helps clean your hands." },
    { prompt: "Before going out in the rain, what could you take?", options: ["An umbrella", "A pillow", "A plate"], answer: 0, hint: "An umbrella keeps the rain off." }
  ]
};
export const SOCIAL_MOMENTS = [
  { prompt: "A room feels noisy. What might feel helpful?", options: ["Move somewhere quieter", "Ask for a quieter moment", "Take a break outside"] },
  { prompt: "Someone says hello. What feels comfortable for you?", options: ["Say hello", "Wave or smile", "Take time before responding"] },
  { prompt: "A plan changes. What could help right now?", options: ["Ask what happens next", "Look at the new plan", "Pause before deciding"] }
];
