import { useCallback, useEffect, useState } from "react";
import { api, categories, downloadFile, label, money, today } from "../api";
import useSelection from "../hooks/useSelection";
import BulkResult from "./BulkResult";
import Form from "./Form";
import ImportPanel from "./ImportPanel";
import SelectionCheckbox from "./SelectionCheckbox";

const FEE_COLUMNS = ["vehicleCategory", "applicationType", "feeAmount", "effectiveFrom"];

// Fee rules for new registrations. RTO administrators configure them (create, CSV import,
// bulk delete of unused rules); the system administrator can view and export them.
export default function FeeRulesPage({ user, run, busy, onError }) {
  const [fees, setFees] = useState([]);
  const [result, setResult] = useState(null);
  const canManage = user.role === "RTO_ADMIN";
  const selection = useSelection(fees.map((fee) => fee.feeRuleId));

  const load = useCallback(async () => setFees(await api("/fees")), []);
  useEffect(() => {
    load().catch((failure) => onError(failure.message));
  }, [load, onError]);

  const now = today();
  // The rule a new application gets today: the latest one already in effect for its category.
  const current = new Set(
    categories
      .map(
        (category) =>
          fees
            .filter((fee) => fee.vehicleCategory === category && fee.effectiveFrom <= now)
            .sort((a, b) => b.effectiveFrom.localeCompare(a.effectiveFrom) || b.feeRuleId - a.feeRuleId)[0],
      )
      .filter(Boolean)
      .map((fee) => fee.feeRuleId),
  );

  return (
    <section className="card p-4">
      <div className="d-flex flex-wrap justify-content-between gap-2">
        <h2>Registration fee rules ({fees.length})</h2>
        <button className="btn btn-outline-primary" disabled={busy} onClick={() => run(() => downloadFile("/export/fees.csv"))}>
          Export CSV
        </button>
      </div>
      <p className="text-secondary">
        A new application is charged the latest rule already in effect for its vehicle category. Fees of
        submitted applications never change.
        {!canManage && " Fee rules are configured by RTO administrators."}
      </p>

      {canManage && (
        <>
          <Form
            busy={busy}
            button="Add fee rule"
            fields={[
              { name: "vehicleCategory", options: categories },
              { name: "applicationType", options: ["NEW"] },
              { name: "feeAmount", type: "number", min: 0.01, step: 0.01 },
              { name: "effectiveFrom", type: "date" },
            ]}
            onSubmit={(values) =>
              run(async () => {
                await api("/fees", "POST", { ...values, feeAmount: Number(values.feeAmount) });
                await load();
              })
            }
          />
          <ImportPanel
            path="/import/fees"
            columns={FEE_COLUMNS}
            title="Import fee rules from CSV"
            run={run}
            busy={busy}
            onImported={() => run(load)}
          />
        </>
      )}

      <div className="mt-4">
        <BulkResult result={result?.data} title={result?.title} onClose={() => setResult(null)} />
      </div>
      {canManage && selection.count > 0 && (
        <div className="bulk-bar" role="region" aria-label="Bulk fee rule actions">
          <strong>{selection.count} selected</strong>
          <button
            className="btn btn-outline-danger"
            disabled={busy}
            onClick={() => {
              if (!window.confirm(`Delete ${selection.count} fee rules? Rules used by applications are kept.`)) return;
              run(async () => {
                setResult({
                  title: "Delete fee rules",
                  data: await api("/fees/bulk/delete", "POST", { ids: selection.ids.map(Number) }),
                });
                selection.clear();
                await load();
              });
            }}
          >
            Delete {selection.count}
          </button>
          <button className="btn btn-link" disabled={busy} onClick={selection.clear}>
            Clear
          </button>
        </div>
      )}
      <div className="table-responsive">
        <table className="table">
          <thead>
            <tr>
              {canManage && (
                <th className="select-column">
                  <SelectionCheckbox
                    checked={selection.allSelected}
                    indeterminate={selection.someSelected}
                    disabled={busy || fees.length === 0}
                    onChange={selection.toggleAll}
                    label="Select all fee rules"
                  />
                </th>
              )}
              <th>Category</th>
              <th>Type</th>
              <th>Fee</th>
              <th>Effective from</th>
              <th>Applies now</th>
            </tr>
          </thead>
          <tbody>
            {fees.map((fee) => (
              <tr key={fee.feeRuleId} className={selection.has(fee.feeRuleId) ? "table-active" : ""}>
                {canManage && (
                  <td className="select-column">
                    <SelectionCheckbox
                      checked={selection.has(fee.feeRuleId)}
                      disabled={busy}
                      onChange={() => selection.toggle(fee.feeRuleId)}
                      label={`Select ${label(fee.vehicleCategory)} fee from ${fee.effectiveFrom}`}
                    />
                  </td>
                )}
                <td>{label(fee.vehicleCategory)}</td>
                <td>{fee.applicationType}</td>
                <td>{money(fee.feeAmount)}</td>
                <td>{fee.effectiveFrom}</td>
                <td>
                  {current.has(fee.feeRuleId) ? (
                    <span className="badge text-bg-success">CURRENT</span>
                  ) : fee.effectiveFrom > now ? (
                    <span className="badge text-bg-primary">UPCOMING</span>
                  ) : (
                    <span className="text-secondary small">Replaced</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {fees.length === 0 && <p className="text-secondary">No fee rules yet.</p>}
    </section>
  );
}
