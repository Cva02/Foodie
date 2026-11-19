import { STATUS_LABEL, STATUS_STEPS } from '../util.js';

export default function StatusTracker({ status }) {
  const idx = STATUS_STEPS.indexOf(status);
  return (
    <ol className="tracker">
      {STATUS_STEPS.map((s, i) => (
        <li key={s} className={i < idx ? 'done' : i === idx ? 'now' : ''}>
          <span className="dot" />
          <span className="lbl">{STATUS_LABEL[s]}</span>
        </li>
      ))}
    </ol>
  );
}
