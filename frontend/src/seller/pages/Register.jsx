import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  Building2,
  Check,
  CreditCard,
  Eye,
  EyeOff,
  FileText,
  Landmark,
  Lock,
  Mail,
  MapPin,
  Phone,
  Store,
  Upload,
  User,
  Utensils,
} from "lucide-react";
import FoodieBrand from "../../components/FoodieBrand.jsx";
import { sellerApi } from "../api";
import { useToast } from "../../context.jsx";
import "../seller.css";

const STEPS = [
  { key: "personal", label: "Personal" },
  { key: "restaurant", label: "Restaurant" },
  { key: "bank", label: "Bank" },
];

const initialForm = {
  ownerName: "",
  email: "",
  phone: "",
  password: "",
  restaurantName: "",
  cuisine: "",
  address: "",
  city: "",
  fssaiNumber: "",
  image: "",
  bankAccountHolderName: "",
  bankAccountNumber: "",
  bankIfscCode: "",
};

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^[6-9]\d{9}$/;
const FSSAI_RE = /^\d{14}$/;
const IFSC_RE = /^[A-Za-z]{4}0[A-Za-z0-9]{6}$/;

export default function SellerRegister() {
  const navigate = useNavigate();
  const toast = useToast();

  const [step, setStep] = useState(0);
  const [form, setForm] = useState(initialForm);
  const [showPassword, setShowPassword] = useState(false);
  const [fssaiFile, setFssaiFile] = useState(null);
  const [gstFile, setGstFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;
    setError("");
    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  function validateFile(file) {
    if (!file) return "";
    const allowedTypes = ["image/jpeg", "image/png", "application/pdf"];
    if (!allowedTypes.includes(file.type)) {
      return "Only JPG, PNG, and PDF files are allowed.";
    }
    if (file.size > 5 * 1024 * 1024) {
      return "Each document must be smaller than 5 MB.";
    }
    return "";
  }

  const personalComplete = useMemo(() => {
    return (
      form.ownerName.trim().length > 0 &&
      EMAIL_RE.test(form.email.trim()) &&
      PHONE_RE.test(form.phone.trim()) &&
      form.password.length >= 6 &&
      form.password.length <= 72
    );
  }, [form.ownerName, form.email, form.phone, form.password]);

  const restaurantComplete = useMemo(() => {
    return (
      form.restaurantName.trim().length > 0 &&
      form.cuisine.trim().length > 0 &&
      form.address.trim().length > 0 &&
      form.city.trim().length > 0 &&
      FSSAI_RE.test(form.fssaiNumber.trim()) &&
      !validateFile(fssaiFile) &&
      !validateFile(gstFile)
    );
  }, [
    form.restaurantName,
    form.cuisine,
    form.address,
    form.city,
    form.fssaiNumber,
    fssaiFile,
    gstFile,
  ]);

  const bankComplete = useMemo(() => {
    return (
      form.bankAccountHolderName.trim().length > 0 &&
      form.bankAccountNumber.trim().length >= 6 &&
      IFSC_RE.test(form.bankIfscCode.trim())
    );
  }, [form.bankAccountHolderName, form.bankAccountNumber, form.bankIfscCode]);

  function validateStep(currentStep) {
    if (currentStep === 0) {
      if (!form.ownerName.trim()) return "Enter the owner's full name.";
      if (!EMAIL_RE.test(form.email.trim())) return "Enter a valid email address.";
      if (!PHONE_RE.test(form.phone.trim())) {
        return "Enter a valid 10-digit Indian mobile number starting with 6–9.";
      }
      if (form.password.length < 6 || form.password.length > 72) {
        return "Password must contain between 6 and 72 characters.";
      }
      return "";
    }

    if (currentStep === 1) {
      if (!form.restaurantName.trim()) return "Enter your restaurant name.";
      if (!form.cuisine.trim()) return "Enter at least one cuisine.";
      if (!form.address.trim()) return "Enter the full restaurant address.";
      if (!form.city.trim()) return "Enter the city.";
      if (!FSSAI_RE.test(form.fssaiNumber.trim())) {
        return "FSSAI licence number must contain exactly 14 digits.";
      }
      const fssaiError = validateFile(fssaiFile);
      if (fssaiError) return fssaiError;
      const gstError = validateFile(gstFile);
      if (gstError) return gstError;
      return "";
    }

    if (!form.bankAccountHolderName.trim()) return "Enter the account holder name.";
    if (form.bankAccountNumber.trim().length < 6) {
      return "Enter a valid bank account number (at least 6 characters).";
    }
    if (!IFSC_RE.test(form.bankIfscCode.trim())) {
      return "Enter a valid 11-character IFSC code (e.g. SBIN0001234).";
    }
    return "";
  }

  function handleNextStep() {
    const validationError = validateStep(step);
    if (validationError) {
      setError(validationError);
      return;
    }
    setError("");
    setStep((current) => Math.min(current + 1, 2));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (step < 2) {
      handleNextStep();
      return;
    }

    const validationError = validateStep(2);
    if (validationError) {
      setError(validationError);
      return;
    }

    setError("");
    setLoading(true);

    try {
      let fssaiDocumentFileName = "";
      let gstDocumentFileName = "";

      if (fssaiFile) {
        const result = await sellerApi.uploadApplicationDocument("FSSAI", fssaiFile);
        fssaiDocumentFileName = result.fileName;
      }

      if (gstFile) {
        const result = await sellerApi.uploadApplicationDocument("GST", gstFile);
        gstDocumentFileName = result.fileName;
      }

      const application = {
        ...form,
        restaurantName: form.restaurantName.trim(),
        ownerName: form.ownerName.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
        cuisine: form.cuisine.trim(),
        address: form.address.trim(),
        city: form.city.trim(),
        fssaiNumber: form.fssaiNumber.trim(),
        image: form.image.trim(),
        bankAccountNumber: form.bankAccountNumber.trim(),
        bankIfscCode: form.bankIfscCode.trim().toUpperCase(),
        bankAccountHolderName: form.bankAccountHolderName.trim(),
        fssaiDocumentFileName,
        gstDocumentFileName,
      };

      await sellerApi.registerApplication(application);

      toast(
        "Seller application submitted successfully. Please wait for Admin approval.",
        "success"
      );

      navigate("/seller/login", { replace: true });
    } catch (err) {
      const message = err.message || "Registration failed. Please try again.";
      setError(message);
      toast(message, "error");
    } finally {
      setLoading(false);
    }
  }

  const canProceed =
    (step === 0 && personalComplete) ||
    (step === 1 && restaurantComplete) ||
    (step === 2 && bankComplete);

  return (
    <div className="auth-page auth-page-login">
      <div className="auth-side">
        <FoodieBrand className="auth-brand" subtitle="Restaurant Partner" />
        <span className="auth-side-kicker">FOODIE · SELLER ONBOARDING</span>
        <h1>Partner with<br />Foodie.</h1>
        <p>3-step seller registration</p>
        <span className="auth-side-note">
          Complete your personal details, restaurant info, and bank payout details to submit your application for Admin approval.
        </span>
      </div>

      <div className="auth-main">
        <form
          className="auth-card seller-register-auth-card"
          onSubmit={handleSubmit}
          noValidate
        >
          <div className="auth-card-eyebrow"><span /> SELLER REGISTRATION</div>
          <h2>Create seller account</h2>
          <p className="muted">
            {step === 0 && "Step 1 of 3 — Fill in your personal account details."}
            {step === 1 && "Step 2 of 3 — Fill in your restaurant & FSSAI details."}
            {step === 2 && "Step 3 of 3 — Fill in your bank details for payouts."}
          </p>

          <ol className="seller-stepper" aria-label="Registration steps">
            {STEPS.map((item, index) => (
              <li
                key={item.key}
                className={
                  index === step
                    ? "is-current"
                    : index < step
                    ? "is-complete"
                    : ""
                }
                aria-current={index === step ? "step" : undefined}
              >
                <span>{index < step ? <Check size={12} /> : index + 1}</span>
                {item.label}
              </li>
            ))}
          </ol>

          <div className="seller-step-fields">
            {step === 0 && (
              <>
                <label className="field">
                  <span className="field-label">Owner full name <b className="req">*</b></span>
                  <span className="input-icon">
                    <User size={16} />
                    <input
                      id="ownerName"
                      name="ownerName"
                      value={form.ownerName}
                      onChange={handleChange}
                      placeholder="Full name"
                      maxLength={120}
                      autoFocus
                      required
                    />
                  </span>
                </label>

                <label className="field">
                  <span className="field-label">Email address <b className="req">*</b></span>
                  <span className="input-icon">
                    <Mail size={16} />
                    <input
                      id="email"
                      name="email"
                      type="email"
                      value={form.email}
                      onChange={handleChange}
                      placeholder="owner@restaurant.com"
                      autoComplete="email"
                      required
                    />
                  </span>
                </label>

                <label className="field">
                  <span className="field-label">Mobile number <b className="req">*</b></span>
                  <span className="input-icon">
                    <Phone size={16} />
                    <input
                      id="phone"
                      name="phone"
                      type="tel"
                      value={form.phone}
                      onChange={(e) =>
                        handleChange({
                          target: {
                            name: "phone",
                            value: e.target.value.replace(/\D/g, "").slice(0, 10),
                          },
                        })
                      }
                      placeholder="10-digit mobile number"
                      maxLength={10}
                      inputMode="numeric"
                      required
                    />
                  </span>
                  <span className="field-hint">10 digits starting with 6, 7, 8, or 9.</span>
                </label>

                <label className="field">
                  <span className="field-label">Create password <b className="req">*</b></span>
                  <span className="input-icon">
                    <Lock size={16} />
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? "text" : "password"}
                      value={form.password}
                      onChange={handleChange}
                      placeholder="Minimum 6 characters"
                      minLength={6}
                      maxLength={72}
                      autoComplete="new-password"
                      required
                    />
                    <button
                      type="button"
                      className="eye"
                      onClick={() => setShowPassword((value) => !value)}
                      aria-label={showPassword ? "Hide password" : "Show password"}
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </span>
                </label>
              </>
            )}

            {step === 1 && (
              <>
                <label className="field">
                  <span className="field-label">Restaurant name <b className="req">*</b></span>
                  <span className="input-icon">
                    <Store size={16} />
                    <input
                      id="restaurantName"
                      name="restaurantName"
                      value={form.restaurantName}
                      onChange={handleChange}
                      placeholder="e.g. Spice Route Kitchen"
                      maxLength={120}
                      autoFocus
                      required
                    />
                  </span>
                </label>

                <label className="field">
                  <span className="field-label">Cuisine <b className="req">*</b></span>
                  <span className="input-icon">
                    <Utensils size={16} />
                    <input
                      id="cuisine"
                      name="cuisine"
                      value={form.cuisine}
                      onChange={handleChange}
                      placeholder="e.g. South Indian, Biryani, Chinese"
                      maxLength={120}
                      required
                    />
                  </span>
                </label>

                <label className="field">
                  <span className="field-label">Full restaurant address <b className="req">*</b></span>
                  <textarea
                    id="address"
                    name="address"
                    value={form.address}
                    onChange={handleChange}
                    rows={2}
                    placeholder="Building, street, landmark"
                    maxLength={1000}
                    required
                  />
                </label>

                <div className="form-grid">
                  <label className="field">
                    <span className="field-label">City <b className="req">*</b></span>
                    <span className="input-icon">
                      <MapPin size={16} />
                      <input
                        id="city"
                        name="city"
                        value={form.city}
                        onChange={handleChange}
                        placeholder="City"
                        maxLength={120}
                        required
                      />
                    </span>
                  </label>

                  <label className="field">
                    <span className="field-label">FSSAI licence (14 digits) <b className="req">*</b></span>
                    <span className="input-icon">
                      <FileText size={16} />
                      <input
                        id="fssaiNumber"
                        name="fssaiNumber"
                        value={form.fssaiNumber}
                        onChange={(e) =>
                          handleChange({
                            target: {
                              name: "fssaiNumber",
                              value: e.target.value.replace(/\D/g, "").slice(0, 14),
                            },
                          })
                        }
                        placeholder="14-digit FSSAI number"
                        maxLength={14}
                        inputMode="numeric"
                        required
                      />
                    </span>
                  </label>
                </div>

                <label className="field">
                  <span className="field-label">Restaurant cover image URL (optional)</span>
                  <input
                    id="image"
                    name="image"
                    type="url"
                    value={form.image}
                    onChange={handleChange}
                    placeholder="https://example.com/restaurant.jpg"
                  />
                </label>

                <div className="form-grid">
                  <label className="field">
                    <span className="field-label">FSSAI certificate (optional)</span>
                    <input
                      id="fssaiFile"
                      className="seller-file-input"
                      type="file"
                      accept=".jpg,.jpeg,.png,.pdf"
                      onChange={(event) => {
                        setError("");
                        setFssaiFile(event.target.files?.[0] || null);
                      }}
                    />
                  </label>

                  <label className="field">
                    <span className="field-label">GST certificate (optional)</span>
                    <input
                      id="gstFile"
                      className="seller-file-input"
                      type="file"
                      accept=".jpg,.jpeg,.png,.pdf"
                      onChange={(event) => {
                        setError("");
                        setGstFile(event.target.files?.[0] || null);
                      }}
                    />
                  </label>
                </div>
                <span className="field-hint">Accepted certificates: JPG, PNG, PDF up to 5 MB each.</span>
              </>
            )}

            {step === 2 && (
              <>
                <label className="field">
                  <span className="field-label">Account holder name <b className="req">*</b></span>
                  <span className="input-icon">
                    <User size={16} />
                    <input
                      id="bankAccountHolderName"
                      name="bankAccountHolderName"
                      value={form.bankAccountHolderName}
                      onChange={handleChange}
                      placeholder="Name on bank account"
                      maxLength={120}
                      autoFocus
                      required
                    />
                  </span>
                </label>

                <label className="field">
                  <span className="field-label">Bank account number <b className="req">*</b></span>
                  <span className="input-icon">
                    <CreditCard size={16} />
                    <input
                      id="bankAccountNumber"
                      name="bankAccountNumber"
                      value={form.bankAccountNumber}
                      onChange={(e) =>
                        handleChange({
                          target: {
                            name: "bankAccountNumber",
                            value: e.target.value.replace(/[^0-9A-Za-z]/g, "").slice(0, 34),
                          },
                        })
                      }
                      placeholder="Bank account number"
                      maxLength={34}
                      required
                    />
                  </span>
                </label>

                <label className="field">
                  <span className="field-label">IFSC code <b className="req">*</b></span>
                  <span className="input-icon">
                    <Landmark size={16} />
                    <input
                      id="bankIfscCode"
                      name="bankIfscCode"
                      value={form.bankIfscCode}
                      onChange={(e) =>
                        handleChange({
                          target: {
                            name: "bankIfscCode",
                            value: e.target.value.toUpperCase().slice(0, 11),
                          },
                        })
                      }
                      maxLength={11}
                      placeholder="Example: SBIN0001234"
                      required
                    />
                  </span>
                  <span className="field-hint">11-character IFSC code (4 letters, 0, then 6 characters).</span>
                </label>
              </>
            )}
          </div>

          {error && (
            <div className="form-error" role="alert">
              {error}
            </div>
          )}

          {!canProceed && !error && (
            <p className="field-hint">
              Fill in all required fields in this step to continue.
            </p>
          )}

          <div className="seller-step-actions">
            {step > 0 && (
              <button
                type="button"
                className="btn btn-outline"
                onClick={() => {
                  setError("");
                  setStep((current) => current - 1);
                }}
                disabled={loading}
              >
                <ArrowLeft size={16} /> Back
              </button>
            )}

            {step < 2 ? (
              <button
                type="button"
                className="btn btn-danger btn-block"
                onClick={handleNextStep}
                disabled={!canProceed}
              >
                Next: {STEPS[step + 1].label} details <ArrowRight size={16} />
              </button>
            ) : (
              <button
                type="submit"
                className="btn btn-danger btn-block"
                disabled={loading || !canProceed}
              >
                {loading ? (
                  "Submitting application…"
                ) : (
                  <>
                    <Upload size={16} /> Submit application
                  </>
                )}
              </button>
            )}
          </div>

          <div className="portal-auth-links">
            <Link to="/seller/login" className="password-reset-link">
              Already registered? Sign in
            </Link>
          </div>

          <div className="admin-portal-links">
            <span>Switch portal</span>
            <Link to="/login">Customer</Link>
            <Link to="/rider">Rider</Link>
            <Link to="/admin/login">Admin</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
