import { label } from "../api";

// Summary of a bulk operation: how many succeeded, and why each failure failed.
export default function BulkResult({ result, title, onClose }) {
  if (!result) return null;
  const failures = result.results.filter((item) => !item.success);
  const tone = result.failed === 0 ? "success" : result.succeeded === 0 ? "danger" : "warning";
  return (
    <div className={`alert alert-${tone} bulk-result`} role="status">
      <button type="button" className="btn-close float-end" aria-label="Dismiss result" onClick={onClose} />
      <strong>{title}:</strong> {result.succeeded} of {result.requested} succeeded
      {result.failed > 0 && `, ${result.failed} failed`}.
      {failures.length > 0 && (
        <ul className="mb-0 mt-2 small">
          {failures.map((item) => (
            <li key={item.id} className="text-break">
              <strong>{item.id}</strong>: {item.message || label(item.code)}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
