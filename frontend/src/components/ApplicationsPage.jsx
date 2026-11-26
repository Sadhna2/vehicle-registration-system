import { useMemo, useState } from "react";
import { api, downloadFile, label, money, today } from "../api";
import useSelection from "../hooks/useSelection";
import BulkResult from "./BulkResult";
import SelectionCheckbox from "./SelectionCheckbox";
import Status from "./Status";

// Actions an officer may apply to many applications at once. Inspection results
// and approval stay one application at a time on the details page.
const BULK_REVIEW_ACTIONS = {
  START: "Start verification",
  VERIFY: "Verify details",
  SCHEDULE: "Schedule inspection",
  REJECT: "Reject",
};
const WITHDRAWABLE = ["SUBMITTED", "CORRECTION_REQUIRED"];

export default function ApplicationsPage({ data, user, run, busy, onOpen, onRefresh }) {
  const [query, setQuery] = useState("");
  const [reference, setReference] = useState("");
  const [action, setAction] = useState("START");
  const [remarks, setRemarks] = useState("");
  const [appointment, setAppointment] = useState(today());
  const [result, setResult] = useState(null);
  const owner = user.role === "OWNER";
  const reviewer = ["RTO_OFFICER", "RTO_ADMIN"].includes(user.role);

  const rows = data.applications.filter((row) =>
    `${row.reference} ${row.owner} ${row.vehicle}`.toLowerCase().includes(query.toLowerCase()),
  );
  // Owners can only withdraw applications that are not yet under review.
  const selectable = useMemo(
    () => rows.filter((row) => !owner || WITHDRAWABLE.includes(row.status)).map((row) => row.reference),
    [rows, owner],
  );
  const selection = useSelection(selectable);
  const canSelect = owner || reviewer;

  const applyReview = () => {
    if (action === "REJECT" && !window.confirm(`Reject ${selection.count} applications? This closes them.`)) return;
    run(async () => {
      setResult({
        title: BULK_REVIEW_ACTIONS[action],
        data: await api("/applications/bulk/review", "POST", {
          references: selection.ids,
          action,
          remarks: remarks || undefined,
          appointment: action === "SCHEDULE" ? appointment : undefined,
        }),
      });
      selection.clear();
      onRefresh();
    });
  };

  const withdraw = () => {
    if (!window.confirm(`Withdraw ${selection.count} applications? They and their documents will be deleted.`)) return;
    run(async () => {
      setResult({
        title: "Withdraw",
        data: await api("/applications/bulk/withdraw", "POST", { references: selection.ids }),
      });
      selection.clear();
      onRefresh();
    });
  };

  return (
    <section className="card p-4">
      <form
        className="mb-4"
        onSubmit={(event) => {
          event.preventDefault();
          onOpen(reference.trim());
        }}
      >
        <label className="form-label" htmlFor="tracking-reference">
          Track by application reference
        </label>
        <div className="d-flex gap-2">
          <input
            id="tracking-reference"
            className="form-control"
            value={reference}
            onChange={(event) => setReference(event.target.value)}
            required
            maxLength={100}
          />
          <button className="btn btn-primary" disabled={busy || !reference.trim()}>
            Track
          </button>
        </div>
      </form>
      <div className="d-flex flex-wrap justify-content-between gap-2 mb-3">
        <h2>Application register ({data.applications.length})</h2>
        <div className="d-flex gap-2">
          <button
            className="btn btn-outline-primary"
            disabled={busy}
            onClick={() => run(() => downloadFile("/export/applications.csv"))}
          >
            Export CSV
          </button>
          <button className="btn btn-outline-primary" disabled={busy} onClick={onRefresh}>
            Refresh
          </button>
        </div>
      </div>
      <label className="form-label" htmlFor="application-filter">
        Filter applications
      </label>
      <input
        id="application-filter"
        className="form-control mb-3"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
      />

      {canSelect && selection.count > 0 && (
        <div className="bulk-bar" role="region" aria-label="Bulk actions">
          <strong>{selection.count} selected</strong>
          {reviewer && (
            <>
              <select
                className="form-select"
                aria-label="Bulk action"
                value={action}
                disabled={busy}
                onChange={(event) => setAction(event.target.value)}
              >
                {Object.entries(BULK_REVIEW_ACTIONS).map(([value, name]) => (
                  <option key={value} value={value}>
                    {name}
                  </option>
                ))}
              </select>
              {action === "SCHEDULE" && (
                <input
                  type="date"
                  className="form-control"
                  aria-label="Inspection date"
                  min={today()}
                  value={appointment}
                  disabled={busy}
                  onChange={(event) => setAppointment(event.target.value)}
                />
              )}
              <input
                className="form-control"
                aria-label="Remarks"
                placeholder={action === "REJECT" ? "Remarks (required)" : "Remarks (optional)"}
                maxLength={500}
                value={remarks}
                disabled={busy}
                onChange={(event) => setRemarks(event.target.value)}
              />
              <button
                className={`btn ${action === "REJECT" ? "btn-outline-danger" : "btn-primary"}`}
                disabled={busy || (action === "REJECT" && !remarks.trim())}
                onClick={applyReview}
              >
                Apply to {selection.count}
              </button>
            </>
          )}
          {owner && (
            <button className="btn btn-outline-danger" disabled={busy} onClick={withdraw}>
              Withdraw {selection.count}
            </button>
          )}
          <button className="btn btn-link" disabled={busy} onClick={selection.clear}>
            Clear
          </button>
        </div>
      )}
      <BulkResult result={result?.data} title={result?.title} onClose={() => setResult(null)} />

      <div className="table-responsive">
        <table className="table align-middle">
          <thead>
            <tr>
              {canSelect && (
                <th className="select-column">
                  <SelectionCheckbox
                    checked={selection.allSelected}
                    indeterminate={selection.someSelected}
                    disabled={busy || selectable.length === 0}
                    onChange={selection.toggleAll}
                    label="Select all applications shown"
                  />
                </th>
              )}
              <th>Reference</th>
              <th>Owner / vehicle</th>
              <th>Status</th>
              <th>Fee</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.reference} className={selection.has(row.reference) ? "table-active" : ""}>
                {canSelect && (
                  <td className="select-column">
                    {selectable.includes(row.reference) && (
                      <SelectionCheckbox
                        checked={selection.has(row.reference)}
                        disabled={busy}
                        onChange={() => selection.toggle(row.reference)}
                        label={`Select ${row.reference}`}
                      />
                    )}
                  </td>
                )}
                <td className="text-break">{row.reference}</td>
                <td>
                  {row.owner}
                  <br />
                  {row.vehicle}
                </td>
                <td>
                  <Status value={row.status} />
                </td>
                <td>{money(row.amount)}</td>
                <td>
                  <button className="btn btn-outline-primary" disabled={busy} onClick={() => onOpen(row.reference)}>
                    View
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {rows.length === 0 && (
        <p className="text-secondary">
          {data.loaded
            ? "No matching applications."
            : data.error
              ? "Could not load applications. Select Refresh to try again."
              : "Loading applications…"}
        </p>
      )}
      {owner && rows.length > 0 && (
        <p className="small text-secondary mb-0">
          You can withdraw applications that are {WITHDRAWABLE.map(label).join(" or ").toLowerCase()} and not yet paid.
        </p>
      )}
    </section>
  );
}
