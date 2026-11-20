import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  ArrowRight,
  Check,
  Eye,
  EyeOff,
  Loader2,
  LockKeyhole,
  Mail,
  MapPin,
  Phone,
  Store,
  Utensils,
  UploadCloud,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { errMsg } from '../utils/format';
import { api } from '../api/client';
import Logo from '../components/Logo';

const REGISTRATION_STEPS = ['Account', 'Business', 'Shop address', 'Bank & KYC'];
const INPUT_CLASS =
  'h-[54px] w-full rounded-md border border-gray-300 bg-white px-4 text-[15px] text-slate-900 outline-none transition placeholder:text-gray-500 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10';

function CertificateUpload({ id, label, file, onChange }) {
  return (
    <div>
      <label htmlFor={id} className="mb-2 block text-sm font-semibold text-slate-700">{label}</label>
      <label
        htmlFor={id}
        className="flex min-h-[72px] cursor-pointer items-center gap-3 rounded-md border border-dashed border-gray-300 bg-white px-4 transition hover:border-brand-500 hover:bg-brand-50"
      >
        <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-brand-50 text-brand-600">
          <UploadCloud size={19} />
        </span>
        <span className="min-w-0 flex-1">
          <span className="block truncate text-sm font-semibold text-slate-800">
            {file?.name || 'Choose a file to upload'}
          </span>
          <span className="mt-0.5 block text-xs text-slate-500">PDF, JPG, PNG or WEBP</span>
        </span>
      </label>
      <input
        id={id}
        type="file"
        accept=".pdf,.jpg,.jpeg,.png,.webp"
        className="sr-only"
        onChange={(event) => onChange(event.target.files?.[0] || null)}
      />
    </div>
  );
}

export default function Login() {
  const { session, login, register } = useAuth();
  const navigate = useNavigate();

  const [mode, setMode] = useState('login');
  const [registerStep, setRegisterStep] = useState(0);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    identifier: '',
    password: '',
    restaurantName: '',
    ownerName: '',
    email: '',
    phone: '',
    cuisine: '',
    address: '',
    otp: '',
    newPassword: '',
    bankAccountNumber: '',
    bankIfscCode: '',
    bankAccountHolderName: '',
  });
  const [forgotStep, setForgotStep] = useState('identifier');
  const [foodLicenseCertificate, setFoodLicenseCertificate] = useState(null);
  const [gstinCertificate, setGstinCertificate] = useState(null);

  if (session) return <Navigate to="/dashboard" replace />;

  const set = (key) => (event) => setForm((current) => ({ ...current, [key]: event.target.value }));

  function switchMode(next) {
    setMode(next);
    setError('');
    if (next === 'register') setRegisterStep(0);
    if (next === 'login') setForgotStep('identifier');
  }

  async function run(action) {
    setError('');
    setLoading(true);
    try {
      await action();
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(errMsg(err));
      setLoading(false);
    }
  }

  function validateRegistrationStep(step) {
    if (step === 0) {
      if (!form.ownerName.trim()) return setError('Enter your full name');
      if (!/^[6-9]\d{9}$/.test(form.phone)) return setError('Enter a valid 10-digit mobile number');
      if (!/^\S+@\S+\.\S+$/.test(form.email)) return setError('Enter a valid email address');
      if (form.password.length < 6 || form.password.length > 72) {
        return setError('Your password must be between 6 and 72 characters');
      }
    }
    if (step === 1 && !form.restaurantName.trim()) return setError('Enter your restaurant name');
    if (step === 2 && !form.address.trim()) return setError('Enter your shop address');
    if (step === 3) {
      if (!/^\d{9,18}$/.test(form.bankAccountNumber)) return setError('Enter a valid bank account number');
      if (!/^[A-Za-z]{4}0[A-Za-z0-9]{6}$/.test(form.bankIfscCode)) return setError('Enter a valid IFSC code');
      if (!form.bankAccountHolderName.trim()) return setError('Enter the bank account holder name');
      if (!foodLicenseCertificate) return setError('Upload your Food License certificate');
      if (!gstinCertificate) return setError('Upload your GSTIN certificate');
    }
    return true;
  }

  function submit(e) {
    e.preventDefault();
    if (mode === 'forgot') {
      if (!form.identifier.trim()) return setError('Enter your registered mobile number or email');
      setError('');
      setLoading(true);
      if (forgotStep === 'identifier') {
        api.requestPasswordOtp(form.identifier.trim())
          .then(() => setForgotStep('otp'))
          .catch((err) => setError(errMsg(err)))
          .finally(() => setLoading(false));
        return;
      }
      if (!/^\d{6}$/.test(form.otp)) {
        setLoading(false);
        return setError('Enter the 6-digit OTP');
      }
      if (forgotStep === 'otp') {
        api.verifyPasswordOtp(form.identifier.trim(), form.otp)
          .then(() => setForgotStep('reset'))
          .catch((err) => setError(errMsg(err)))
          .finally(() => setLoading(false));
        return;
      }
      if (form.newPassword.length < 6) {
        setLoading(false);
        return setError('Password must be at least 6 characters');
      }
      api.resetPassword(form.identifier.trim(), form.otp, form.newPassword)
        .then(() => {
          setMode('login');
          setForgotStep('identifier');
          setForm((current) => ({ ...current, password: '', otp: '', newPassword: '' }));
          setError('Password reset successfully. Please log in.');
        })
        .catch((err) => setError(errMsg(err)))
        .finally(() => setLoading(false));
      return;
    }

    if (mode === 'login') {
      if (!form.identifier.trim() || !form.password) return setError('Enter your mobile/email and password');
      return run(() => login(form.identifier.trim(), form.password));
    }

    if (!validateRegistrationStep(registerStep)) return;
    if (registerStep < REGISTRATION_STEPS.length - 1) {
      setError('');
      setRegisterStep((step) => step + 1);
      return;
    }

    return run(async () => {
      const [foodLicense, gstin] = await Promise.all([
        api.uploadRegistrationCertificate(foodLicenseCertificate),
        api.uploadRegistrationCertificate(gstinCertificate),
      ]);
      await register({
        restaurantName: form.restaurantName.trim(),
        ownerName: form.ownerName.trim(),
        email: form.email.trim(),
        phone: form.phone,
        password: form.password,
        cuisine: form.cuisine.trim(),
        address: form.address.trim(),
        foodLicenseCertificateUrl: foodLicense.url,
        gstinCertificateUrl: gstin.url,
        bankAccountNumber: form.bankAccountNumber.trim(),
        bankIfscCode: form.bankIfscCode.trim().toUpperCase(),
        bankAccountHolderName: form.bankAccountHolderName.trim(),
      });
    });
  }

  const title = mode === 'login'
    ? 'Sign in'
    : mode === 'register'
      ? 'Sign up'
      : 'Reset password';

  return (
    <div className="min-h-screen bg-white lg:grid lg:grid-cols-[52%_48%]">
      <aside className="relative hidden min-h-screen overflow-hidden bg-[#1c1c1c] px-10 py-12 text-white lg:flex lg:flex-col">
        <div className="my-auto flex flex-col items-center text-center">
          <div className="flex h-60 w-56 items-center justify-center border-l-2 border-t-2 border-white/90">
            <Logo size={108} withWordmark wordmarkClass="text-white" />
          </div>
          <h1 className="mt-6 text-4xl font-extrabold tracking-tight xl:text-[42px]">
            Hungry? We’ve got you.
          </h1>
          <p className="mt-5 max-w-[420px] text-base leading-7 text-gray-300">
            Join Foodie to connect your kitchen with hungry customers and manage every order in one place.
          </p>
        </div>
        <p className="text-center text-sm text-gray-500">Foodie Seller Partner</p>
      </aside>

      <section className="flex min-h-screen flex-col">
        <header className="flex items-center justify-between bg-white px-5 py-5 sm:px-8 lg:px-10">
          <div className="flex items-center gap-2.5 lg:hidden">
            <Logo size={38} />
            <div>
              <p className="text-lg font-extrabold leading-5 text-slate-900">foodie</p>
              <p className="text-[10px] font-semibold tracking-[0.16em] text-slate-500">SELLER PARTNER</p>
            </div>
          </div>
          <div className="hidden lg:block" />
          <div className="text-sm text-gray-600">
            {mode === 'login' ? (
              <>
                New to Foodie?{' '}
                <button type="button" onClick={() => switchMode('register')} className="font-bold text-brand-600 hover:text-brand-700">
                  Create account
                </button>
              </>
            ) : mode === 'register' ? (
              <>
                Already a partner?{' '}
                <button type="button" onClick={() => switchMode('login')} className="font-bold text-brand-600 hover:text-brand-700">
                  Log in
                </button>
              </>
            ) : (
              <button type="button" onClick={() => switchMode('login')} className="font-bold text-brand-600 hover:text-brand-700">
                Back to login
              </button>
            )}
          </div>
        </header>

        <main className="mx-auto flex w-full max-w-[480px] flex-1 flex-col justify-center px-5 pb-10 pt-2 sm:px-8 lg:px-10">
          <div className="mb-7">
            {mode === 'register' && registerStep > 0 && (
              <button
                type="button"
                onClick={() => {
                  setError('');
                  setRegisterStep((step) => Math.max(0, step - 1));
                }}
                className="mb-5 inline-flex items-center gap-2 text-sm font-semibold text-slate-500 transition hover:text-slate-900"
              >
                <ArrowLeft size={17} />
                Previous step
              </button>
            )}
            <h2 className="text-3xl font-extrabold tracking-tight text-slate-950 sm:text-4xl">{title}</h2>
            <p className="mt-2 text-[15px] leading-6 text-slate-500">
              {mode === 'login'
                ? 'Log in to manage your restaurant on Foodie.'
                : mode === 'register'
                  ? 'Set up your seller account in four quick steps.'
                  : 'Verify your identity before setting a new password.'}
            </p>
          </div>

          {mode === 'register' && (
            <nav aria-label="Registration progress" className="mb-8 grid grid-cols-4 gap-2">
              {REGISTRATION_STEPS.map((step, index) => {
                const complete = index < registerStep;
                const active = index === registerStep;
                return (
                  <div key={step} aria-current={active ? 'step' : undefined}>
                    <div className={`h-1.5 rounded-full ${index <= registerStep ? 'bg-brand-600' : 'bg-gray-200'}`} />
                    <p className={`mt-2 text-[11px] font-semibold leading-4 sm:text-xs ${active ? 'text-slate-950' : complete ? 'text-brand-700' : 'text-slate-400'}`}>
                      <span className="mr-1">{complete ? <Check size={12} className="inline" /> : `${index + 1}.`}</span>
                      {step}
                    </p>
                  </div>
                );
              })}
            </nav>
          )}

          <form onSubmit={submit} className="space-y-5" noValidate>
            {mode === 'register' && registerStep === 0 && (
              <div className="animate-fade-up space-y-4">
                <div>
                  <label htmlFor="ownerName" className="mb-2 block text-sm font-semibold text-slate-700">Full name</label>
                  <input id="ownerName" className={INPUT_CLASS} value={form.ownerName} onChange={set('ownerName')} autoComplete="name" placeholder="Your full name" maxLength={120} />
                </div>
                <div>
                  <label htmlFor="phone" className="mb-2 block text-sm font-semibold text-slate-700">Mobile number</label>
                  <div className="flex h-[54px] overflow-hidden rounded-md border border-gray-300 bg-white focus-within:border-brand-500 focus-within:ring-4 focus-within:ring-brand-500/10">
                    <span className="flex items-center gap-2 border-r border-slate-200 px-4 text-sm font-semibold text-slate-500">
                      <Phone size={16} />
                      +91
                    </span>
                    <input id="phone" className="min-w-0 flex-1 px-4 text-[15px] outline-none" inputMode="numeric" maxLength={10} value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value.replace(/\D/g, '') }))} autoComplete="tel-national" placeholder="10-digit mobile number" />
                  </div>
                </div>
                <div>
                  <label htmlFor="email" className="mb-2 block text-sm font-semibold text-slate-700">Email address</label>
                  <div className="relative">
                    <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
                    <input id="email" type="email" className={`${INPUT_CLASS} pl-11`} value={form.email} onChange={set('email')} autoComplete="email" placeholder="you@restaurant.com" maxLength={160} />
                  </div>
                </div>
                <div>
                  <label htmlFor="password" className="mb-2 block text-sm font-semibold text-slate-700">Create password</label>
                  <div className="relative">
                    <LockKeyhole size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
                    <input id="password" type={showPassword ? 'text' : 'password'} className={`${INPUT_CLASS} pl-11 pr-12`} value={form.password} onChange={set('password')} autoComplete="new-password" placeholder="At least 6 characters" maxLength={72} />
                    <button type="button" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? 'Hide password' : 'Show password'} className="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg p-2 text-slate-400 hover:text-slate-700">
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>
              </div>
            )}

            {mode === 'register' && registerStep === 1 && (
              <div className="animate-fade-up space-y-4">
                <div>
                  <label htmlFor="restaurantName" className="mb-2 block text-sm font-semibold text-slate-700">Restaurant or business name</label>
                  <div className="relative">
                    <Store size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
                    <input id="restaurantName" className={`${INPUT_CLASS} pl-11`} value={form.restaurantName} onChange={set('restaurantName')} autoComplete="organization" placeholder="e.g. Spice Garden Kitchen" maxLength={120} />
                  </div>
                </div>
                <div>
                  <label htmlFor="cuisine" className="mb-2 block text-sm font-semibold text-slate-700">
                    Cuisine <span className="font-normal text-slate-400">Optional</span>
                  </label>
                  <div className="relative">
                    <Utensils size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
                    <input id="cuisine" className={`${INPUT_CLASS} pl-11`} value={form.cuisine} onChange={set('cuisine')} placeholder="e.g. South Indian, Biryani" maxLength={120} />
                  </div>
                </div>
                <div className="rounded-xl border border-brand-100 bg-brand-50 p-4">
                  <p className="text-sm font-semibold text-slate-800">Show customers what makes you special</p>
                  <p className="mt-1 text-sm leading-5 text-slate-600">You can update your restaurant details and menu after creating your account.</p>
                </div>
              </div>
            )}

            {mode === 'register' && registerStep === 2 && (
              <div className="animate-fade-up space-y-4">
                <div>
                  <label htmlFor="address" className="mb-2 block text-sm font-semibold text-slate-700">Restaurant address</label>
                  <div className="relative">
                    <MapPin size={18} className="absolute left-4 top-4 text-slate-400" />
                    <textarea id="address" className="min-h-36 w-full resize-y rounded-md border border-gray-300 bg-white px-4 py-3 pl-11 text-[15px] text-slate-900 outline-none transition placeholder:text-gray-500 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10" value={form.address} onChange={set('address')} placeholder="Building, street, area, city and PIN code" maxLength={255} />
                  </div>
                </div>
                <div className="flex gap-3 rounded-xl border border-gray-200 bg-white p-4">
                <MapPin size={20} className="mt-0.5 shrink-0 text-brand-600" />
                  <p className="text-sm leading-5 text-slate-600">Use the address where your restaurant prepares and hands over orders.</p>
                </div>
              </div>
            )}

            {mode === 'register' && registerStep === 3 && (
              <div className="animate-fade-up space-y-5">
                <div>
                  <label htmlFor="bankAccountHolderName" className="mb-2 block text-sm font-semibold text-slate-700">Account holder name</label>
                  <input id="bankAccountHolderName" className={INPUT_CLASS} value={form.bankAccountHolderName} onChange={set('bankAccountHolderName')} autoComplete="name" placeholder="Name as shown on bank account" maxLength={120} />
                </div>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label htmlFor="bankAccountNumber" className="mb-2 block text-sm font-semibold text-slate-700">Bank account number</label>
                    <input id="bankAccountNumber" className={INPUT_CLASS} inputMode="numeric" value={form.bankAccountNumber} onChange={(event) => setForm((current) => ({ ...current, bankAccountNumber: event.target.value.replace(/\D/g, '') }))} autoComplete="off" placeholder="Account number" />
                  </div>
                  <div>
                    <label htmlFor="bankIfscCode" className="mb-2 block text-sm font-semibold text-slate-700">IFSC code</label>
                    <input id="bankIfscCode" className={`${INPUT_CLASS} uppercase`} maxLength={11} value={form.bankIfscCode} onChange={(event) => setForm((current) => ({ ...current, bankIfscCode: event.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '') }))} autoComplete="off" placeholder="ABCD0123456" />
                  </div>
                </div>
                <div className="grid gap-4 sm:grid-cols-2">
                  <CertificateUpload id="foodLicenseCertificate" label="Food License certificate" file={foodLicenseCertificate} onChange={setFoodLicenseCertificate} />
                  <CertificateUpload id="gstinCertificate" label="GSTIN certificate" file={gstinCertificate} onChange={setGstinCertificate} />
                </div>
                <p className="text-xs leading-5 text-slate-500">Your documents and bank details are used to verify your seller account.</p>
              </div>
            )}

            {(mode === 'login' || mode === 'forgot') && (
              <div className="animate-fade-up space-y-4">
                <div>
                  <label htmlFor="identifier" className="mb-2 block text-sm font-semibold text-slate-700">Mobile number or email</label>
                  <input id="identifier" className={INPUT_CLASS} value={form.identifier} onChange={set('identifier')} autoComplete="username" placeholder="9876543210 or you@restaurant.com" />
                </div>

                {mode === 'login' && (
                  <div>
                    <div className="mb-2 flex items-center justify-between">
                      <label htmlFor="loginPassword" className="text-sm font-semibold text-slate-700">Password</label>
                      <button type="button" onClick={() => switchMode('forgot')} className="text-sm font-semibold text-brand-600 hover:text-brand-700">
                        Forgot password?
                      </button>
                    </div>
                    <div className="relative">
                      <LockKeyhole size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
                      <input id="loginPassword" type={showPassword ? 'text' : 'password'} className={`${INPUT_CLASS} pl-11 pr-12`} value={form.password} onChange={set('password')} autoComplete="current-password" placeholder="Enter your password" />
                      <button type="button" onClick={() => setShowPassword((visible) => !visible)} aria-label={showPassword ? 'Hide password' : 'Show password'} className="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg p-2 text-slate-400 hover:text-slate-700">
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                      </button>
                    </div>
                  </div>
                )}

                {mode === 'forgot' && forgotStep !== 'identifier' && (
                  <div>
                    <label htmlFor="otp" className="mb-2 block text-sm font-semibold text-slate-700">Verification OTP</label>
                    <input id="otp" className={`${INPUT_CLASS} text-center text-lg tracking-[0.45em]`} inputMode="numeric" maxLength={6} value={form.otp} onChange={(event) => setForm((current) => ({ ...current, otp: event.target.value.replace(/\D/g, '') }))} placeholder="------" />
                  </div>
                )}

                {mode === 'forgot' && forgotStep === 'reset' && (
                  <div>
                    <label htmlFor="newPassword" className="mb-2 block text-sm font-semibold text-slate-700">New password</label>
                    <input id="newPassword" type="password" className={INPUT_CLASS} value={form.newPassword} onChange={set('newPassword')} autoComplete="new-password" placeholder="At least 6 characters" />
                  </div>
                )}
              </div>
            )}

            {error && (
              <p role="alert" className="rounded-xl border border-rose-100 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
                {error}
              </p>
            )}

            {mode === 'register' && registerStep < REGISTRATION_STEPS.length - 1 && (
              <button type="submit" className="flex h-[54px] w-full items-center justify-center gap-2 rounded-md bg-brand-600 px-5 text-base font-bold text-white shadow-sm transition hover:bg-brand-700 active:scale-[.99]">
                Next: {REGISTRATION_STEPS[registerStep + 1]}
                <ArrowRight size={18} />
              </button>
            )}

            {(mode !== 'register' || registerStep === REGISTRATION_STEPS.length - 1) && (
              <button type="submit" disabled={loading} className="flex h-[54px] w-full items-center justify-center gap-2 rounded-md bg-brand-600 px-5 text-base font-bold text-white shadow-sm transition hover:bg-brand-700 active:scale-[.99] disabled:cursor-not-allowed disabled:opacity-60">
                {loading && <Loader2 size={18} className="animate-spin" />}
                {mode === 'login'
                  ? 'Log in to your account'
                  : mode === 'register'
                    ? 'Create seller account'
                    : forgotStep === 'identifier'
                      ? 'Send verification code'
                      : forgotStep === 'otp'
                        ? 'Verify code'
                        : 'Reset password'}
                {!loading && <ArrowRight size={18} />}
              </button>
            )}
          </form>

          {mode === 'register' && (
            <p className="mt-5 text-center text-xs leading-5 text-slate-500">
              Your details and documents will be used to set up your Foodie seller profile.
            </p>
          )}

          {mode === 'login' && (
            <div className="mt-6 flex items-center justify-center gap-2 text-xs text-slate-500">
              <Check size={14} className="text-emerald-600" />
              Your seller account and information stay secure.
            </div>
          )}
        </main>
      </section>
    </div>
  );
}
