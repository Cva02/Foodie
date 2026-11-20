import { useEffect, useState } from 'react';
import { MessageSquareText, Star } from 'lucide-react';
import { api } from '../api/client';
import { errMsg, formatDateTime } from '../utils/format';

export default function Feedback() {
  const [feedback, setFeedback] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let alive = true;
    api.getFeedback()
      .then((data) => alive && setFeedback(data))
      .catch((err) => alive && setError(errMsg(err)))
      .finally(() => alive && setLoading(false));
    return () => { alive = false; };
  }, []);

  return (
    <section aria-label="Customer feedback">
      <h2 className="text-xl font-bold">Customer Feedback</h2>
      <p className="mb-4 text-sm text-gray-500">Reviews and comments from delivered orders</p>

      {loading ? (
        <div className="grid gap-3 md:grid-cols-2">
          {[0, 1, 2].map((i) => <div key={i} className="card h-36 animate-pulse" />)}
        </div>
      ) : error ? (
        <p role="alert" className="card px-4 py-6 text-center text-sm font-medium text-brand-700">{error}</p>
      ) : feedback.length === 0 ? (
        <div className="card flex flex-col items-center px-6 py-12 text-center">
          <MessageSquareText size={40} className="text-gray-300" />
          <p className="mt-3 font-semibold">No customer feedback yet</p>
        </div>
      ) : (
        <div className="grid gap-3 md:grid-cols-2">
          {feedback.map((item) => (
            <article key={item.id} className="card p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="font-bold">{item.customerName}</h3>
                  <p className="text-xs text-gray-400">Order #{item.orderNumber}</p>
                </div>
                <div className="flex items-center gap-0.5" aria-label={`${item.rating} out of 5 stars`}>
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                      key={star}
                      size={16}
                      className={star <= item.rating ? 'fill-amber-400 text-amber-400' : 'text-gray-200'}
                    />
                  ))}
                </div>
              </div>
              <p className="mt-3 text-[15px] leading-relaxed text-gray-700">"{item.comment}"</p>
              <p className="mt-3 border-t border-gray-100 pt-3 text-xs text-gray-400">
                {formatDateTime(item.createdAt)}
              </p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
