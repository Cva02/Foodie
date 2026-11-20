import { ChevronDown, Mail, MessageCircle, Phone } from 'lucide-react';

const FAQS = [
  {
    question: 'How do I accept a new order?',
    answer: 'Open Incoming orders on the dashboard, review the items, and tap Accept or Reject.',
  },
  {
    question: 'How do I mark a dish out of stock?',
    answer: 'Go to Menu, find the dish, and switch off the In stock toggle beside it.',
  },
  {
    question: 'How do payouts work?',
    answer: 'Completed and settled orders appear in Earnings. Request a payout when a balance is available.',
  },
];

export default function HelpSupport() {
  return (
    <section className="mx-auto max-w-2xl space-y-5" aria-label="Help and support">
      <div>
        <h2 className="text-xl font-bold">Help &amp; Support</h2>
        <p className="mt-1 text-sm text-gray-500">Get help with orders, menu updates, payouts, and account settings.</p>
      </div>

      <div className="card p-5">
        <h3 className="font-bold">Frequently asked questions</h3>
        <div className="mt-3 divide-y divide-gray-100">
          {FAQS.map((faq) => (
            <details key={faq.question} className="group py-3">
              <summary className="flex cursor-pointer list-none items-center justify-between gap-3 font-semibold">
                {faq.question}
                <ChevronDown size={18} className="shrink-0 text-gray-400 transition group-open:rotate-180" />
              </summary>
              <p className="mt-2 text-sm leading-6 text-gray-500">{faq.answer}</p>
            </details>
          ))}
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <a href="tel:+918000000000" className="card flex items-center gap-3 p-4 transition hover:border-brand-200">
          <span className="flex h-11 w-11 items-center justify-center rounded-full bg-brand-50 text-brand-600">
            <Phone size={20} />
          </span>
          <span>
            <span className="block text-sm font-bold">Call support</span>
            <span className="text-xs text-gray-500">+91 80000 00000</span>
          </span>
        </a>
        <a href="mailto:sellersupport@foodie.in" className="card flex items-center gap-3 p-4 transition hover:border-brand-200">
          <span className="flex h-11 w-11 items-center justify-center rounded-full bg-brand-50 text-brand-600">
            <Mail size={20} />
          </span>
          <span>
            <span className="block text-sm font-bold">Email support</span>
            <span className="text-xs text-gray-500">sellersupport@foodie.in</span>
          </span>
        </a>
      </div>

      <div className="flex items-start gap-3 rounded-2xl bg-surface px-4 py-3 text-sm text-gray-600">
        <MessageCircle size={20} className="mt-0.5 shrink-0 text-brand-600" />
        Support hours: Monday to Saturday, 9:00 AM to 9:00 PM.
      </div>
    </section>
  );
}
