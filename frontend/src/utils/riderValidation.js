export function isValidServicePinCodePair(value) {
  if (typeof value !== "string") return false;
  const pinCodes = value.trim().split(/\s*,\s*/);
  return pinCodes.length === 2
    && /^[0-9]{6}$/.test(pinCodes[0])
    && /^[0-9]{6}$/.test(pinCodes[1])
    && pinCodes[0] !== pinCodes[1];
}

export function normalizeOtpInput(value) {
  return String(value || "").replace(/\D/g, "").slice(0, 6);
}
