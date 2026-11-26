import { describe, expect, it } from "vitest";
import { isValidServicePinCodePair, normalizeOtpInput } from "./riderValidation.js";

describe("rider registration and OTP validation", () => {
  it("accepts two distinct six-digit service PIN codes with optional spaces", () => {
    expect(isValidServicePinCodePair("531162, 531163")).toBe(true);
    expect(isValidServicePinCodePair(" 531162 ,531163 ")).toBe(true);
  });

  it("rejects duplicate or malformed service PIN codes", () => {
    expect(isValidServicePinCodePair("531162,531162")).toBe(false);
    expect(isValidServicePinCodePair("531162,53116")).toBe(false);
    expect(isValidServicePinCodePair("531162,531163,531164")).toBe(false);
  });

  it("limits an OTP entry to six digits", () => {
    expect(normalizeOtpInput("12a34-56")).toBe("123456");
    expect(normalizeOtpInput("12345678")).toBe("123456");
  });
});
