export function validManualVital(kind, first, second = null) {
  const s = second === void 0 ? null : second;
  if (kind === "bp") return Number.isInteger(first) && first >= 1 && first <= 400 && Number.isInteger(s) && s >= 1 && s <= 300 && first >= s;
  if (kind === "oxygen") return Number.isInteger(first) && first >= 1 && first <= 100 && s === null;
  return false;
}
export function formatManualVital(record) {
  return record.kind === "bp" ? `${record.first}/${record.second} mmHg` : `${record.first}% SpO\u2082`;
}
export function manualVitalRow(kind, first, second, at = ( new Date()).toISOString()) {
  if (!validManualVital(kind, first, second)) throw Error("Check the units and values. This is not a medical interpretation.");
  return { id: crypto.randomUUID(), kind, first, ...kind === "bp" ? { second } : {}, at };
}
