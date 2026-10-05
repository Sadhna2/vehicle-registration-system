import { useState } from "react";
import { money } from "../api";
import Status from "./Status";

export default function ApplicationsPage({ data, busy, onOpen, onRefresh }) {
  const [query, setQuery] = useState("");
  const [reference, setReference] = useState("");
  const rows = data.applications.filter((row) =>
    `${row.reference} ${row.owner} ${row.vehicle}`
      .toLowerCase()
      .includes(query.toLowerCase()),
  );
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
          <button
            className="btn btn-primary"
            disabled={busy || !reference.trim()}
          >
            Track
          </button>
        </div>
      </form>
      <div className="d-flex justify-content-between mb-3">
        <h2>Application register ({data.applications.length})</h2>
        <button
          className="btn btn-outline-primary"
          disabled={busy}
          onClick={onRefresh}
        >
          Refresh
        </button>
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
      <div className="table-responsive">
        <table className="table align-middle">
          <thead>
            <tr>
              <th>Reference</th>
              <th>Owner / vehicle</th>
              <th>Status</th>
              <th>Fee</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.reference}>
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
                  <button
                    className="btn btn-outline-primary"
                    disabled={busy}
                    onClick={() => onOpen(row.reference)}
                  >
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
    </section>
  );
}
