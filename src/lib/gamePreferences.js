const PREFERRED_CATEGORIES = {
  thinking: ["thinking", "everyday", "connection", "calm"],
  familiar: ["everyday", "thinking", "connection", "calm"],
  connection: ["connection", "everyday", "calm", "thinking"]
};
export function orderActivitiesByFocus(activities, focus = "all") {
  const order = PREFERRED_CATEGORIES[focus];
  if (!order) return activities;
  return activities.map((activity, index) => ({ activity, index })).sort((left, right) => order.indexOf(left.activity.category) - order.indexOf(right.activity.category) || left.index - right.index).map(({ activity }) => activity);
}
export function gameLevelSettings(level = "gentle") {
  if (level === "challenge") return { pairCount: 12, wordGridSize: 12, defaultPlayers: 4 };
  if (level === "steady") return { pairCount: 4, wordGridSize: 10, defaultPlayers: 3 };
  return { pairCount: 2, wordGridSize: 10, defaultPlayers: 2 };
}
