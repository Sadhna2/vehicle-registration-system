import { useCallback, useEffect, useState } from "react";
import { api, categories, downloadFile, label, money } from "../api";
import useSelection from "../hooks/useSelection";
import BulkResult from "./BulkResult";
import Form from "./Form";
import ImportPanel from "./ImportPanel";
import SelectionCheckbox from "./SelectionCheckbox";

const STATUSES = ["ACTIVE", "INACTIVE", "SUSPENDED"];
const ROLES = ["RTO_OFFICER", "RTO_ADMIN", "SYSTEM_ADMIN"];
const FEE_COLUMNS = ["vehicleCategory", "applicationType", "feeAmount", "effectiveFrom"];
const EMPLOYEE_COLUMNS = ["firstName", "lastName", "emailAddress", "phoneNumber", "password", "designation", "role"];

// Bar shown above a table when rows are ticked: set one status (and, for
// employees, optionally one role) on every selected account.
function AccountBulkBar({ selection, withRole, busy, onApply }) {
  const [status, setStatus] = useState("ACTIVE");
  const [role, setRole] = useState("");
  if (selection.count === 0) return null;
  return (
    <div className="bulk-bar" role="region" aria-label="Bulk account update">
      <strong>{selection.count} selected</strong>
      <select className="form-select" aria-label="New status" value={status} disabled={busy} onChange={(e) => setStatus(e.target.value)}>
        {STATUSES.map((s) => (
          <option key={s} value={s}>
            {label(s)}
          </option>
        ))}
      </select>
      {withRole && (
        <select className="form-select" aria-label="New role" value={role} disabled={busy} onChange={(e) => setRole(e.target.value)}>
          <option value="">Keep current role</option>
          {ROLES.map((r) => (
            <option key={r} value={r}>
              {label(r)}
            </option>
          ))}
        </select>
      )}
      <button className="btn btn-primary" disabled={busy} onClick={() => onApply(status, role || undefined)}>
        Apply to {selection.count}
      </button>
      <button className="btn btn-link" disabled={busy} onClick={selection.clear}>
        Clear
      </button>
    </div>
  );
}

function ExportButton({ path, run, busy, children = "Export CSV" }) {
  return (
    <button className="btn btn-outline-primary" disabled={busy} onClick={() => run(() => downloadFile(path))}>
      {children}
    </button>
  );
}

export default function Admin({ user, run, busy, onError }) {
  const [report, setReport] = useState(null);
  const [fees, setFees] = useState([]);
  const [logs, setLogs] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [owners, setOwners] = useState([]);
  const [result, setResult] = useState(null);
  const systemAdmin = user.role === "SYSTEM_ADMIN";
  const rtoAdmin = user.role === "RTO_ADMIN";

  const load = useCallback(async () => {
    const [r, f, l, e] = await Promise.all([api("/reports"), api("/fees"), api("/audit"), api("/employees")]);
    setReport(r);
    setFees(f);
    setLogs(l);
    setEmployees(e);
    if (systemAdmin) setOwners(await api("/owners"));
  }, [systemAdmin]);

  useEffect(() => {
    load().catch((failure) => onError(failure.message));
  }, [load, onError]);

  const feeSelection = useSelection(fees.map((f) => f.feeRuleId));
  const employeeSelection = useSelection(
    employees.filter((e) => e.employeeId !== user.id).map((e) => e.employeeId),
  );
  const ownerSelection = useSelection(owners.map((o) => o.ownerId));

  const save = (path, data) =>
    run(async () => {
      await api(path, "POST", data);
      await load();
    });

  const account = (kind, person, status, role) =>
    run(async () => {
      await api(`/accounts/${kind}/${person.employeeId || person.ownerId}`, "PATCH", {
        status,
        ...(role ? { role } : {}),
      });
      await load();
    });

  const bulk = (title, method, path, body, selection) =>
    run(async () => {
      setResult({ title, data: await api(path, method, body) });
      selection.clear();
      await load();
    });

  return (
    <div className="d-grid gap-4">
      <BulkResult result={result?.data} title={result?.title} onClose={() => setResult(null)} />
      {report && (
        <section className="card p-4">
          <div className="d-flex flex-wrap justify-content-between gap-2">
            <h2>Department overview</h2>
            <ExportButton path="/reports/applications.pdf" run={run} busy={busy}>
              Download report (PDF)
            </ExportButton>
          </div>
          <div className="row g-3 mt-1">
            {[
              ["Fees recorded", money(report.feeCollected)],
              ["Vehicle owners", report.owners],
              ["Registered vehicle records", report.vehicles],
              ["Expiry within 90 days", report.expiringWithin90Days],
            ].map(([name, value]) => (
              <div className="col-sm-6 col-xl-3" key={name}>
                <p className="small text-secondary mb-1">{name}</p>
                <strong className="fs-3">{value}</strong>
              </div>
            ))}
          </div>
          <div className="d-flex flex-wrap gap-3 mt-4">
            {Object.entries(report.counts).map(([k, v]) => (
              <span key={k}>
                {label(k)}: <strong>{v}</strong>
              </span>
            ))}
          </div>
        </section>
      )}

      {rtoAdmin && (
        <section className="card p-4">
          <div className="d-flex flex-wrap justify-content-between gap-2">
            <h2>Fee rules</h2>
            <ExportButton path="/export/fees.csv" run={run} busy={busy} />
          </div>
          <p className="text-secondary">New rules apply to future applications. Submitted fees remain fixed.</p>
          <Form
            busy={busy}
            fields={[
              { name: "vehicleCategory", options: categories },
              { name: "applicationType", options: ["NEW"] },
              { name: "feeAmount", type: "number", min: 0.01, step: 0.01 },
              { name: "effectiveFrom", type: "date" },
            ]}
            onSubmit={(x) => save("/fees", { ...x, feeAmount: Number(x.feeAmount) })}
          />
          <ImportPanel
            path="/import/fees"
            columns={FEE_COLUMNS}
            title="Import fee rules from CSV"
            run={run}
            busy={busy}
            onImported={() => run(load)}
          />
          {feeSelection.count > 0 && (
            <div className="bulk-bar mt-4" role="region" aria-label="Bulk fee rule actions">
              <strong>{feeSelection.count} selected</strong>
              <button
                className="btn btn-outline-danger"
                disabled={busy}
                onClick={() => {
                  if (window.confirm(`Delete ${feeSelection.count} fee rules? Rules used by applications are kept.`))
                    bulk("Delete fee rules", "POST", "/fees/bulk/delete", { ids: feeSelection.ids.map(Number) }, feeSelection);
                }}
              >
                Delete {feeSelection.count}
              </button>
              <button className="btn btn-link" disabled={busy} onClick={feeSelection.clear}>
                Clear
              </button>
            </div>
          )}
          <div className="table-responsive mt-4">
            <table className="table">
              <thead>
                <tr>
                  <th className="select-column">
                    <SelectionCheckbox
                      checked={feeSelection.allSelected}
                      indeterminate={feeSelection.someSelected}
                      disabled={busy || fees.length === 0}
                      onChange={feeSelection.toggleAll}
                      label="Select all fee rules"
                    />
                  </th>
                  <th>Category</th>
                  <th>Type</th>
                  <th>Fee</th>
                  <th>Effective from</th>
                </tr>
              </thead>
              <tbody>
                {fees.map((f) => (
                  <tr key={f.feeRuleId} className={feeSelection.has(f.feeRuleId) ? "table-active" : ""}>
                    <td className="select-column">
                      <SelectionCheckbox
                        checked={feeSelection.has(f.feeRuleId)}
                        disabled={busy}
                        onChange={() => feeSelection.toggle(f.feeRuleId)}
                        label={`Select ${label(f.vehicleCategory)} fee from ${f.effectiveFrom}`}
                      />
                    </td>
                    <td>{label(f.vehicleCategory)}</td>
                    <td>{f.applicationType}</td>
                    <td>{money(f.feeAmount)}</td>
                    <td>{f.effectiveFrom}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      <section className="card p-4">
        <div className="d-flex flex-wrap justify-content-between gap-2">
          <h2>RTO employees</h2>
          <ExportButton path="/export/employees.csv" run={run} busy={busy} />
        </div>
        <Form
          busy={busy}
          button="Create employee"
          fields={[
            { name: "firstName", maxLength: 30 },
            { name: "lastName", maxLength: 30 },
            { name: "emailAddress", type: "email" },
            { name: "phoneNumber", pattern: "[0-9+]{10,15}" },
            { name: "password", type: "password", minLength: 10 },
            { name: "designation", options: ["CLERK", "OFFICER", "SENIOR_OFFICER", "ADMIN"] },
            { name: "role", options: systemAdmin ? ROLES : ["RTO_OFFICER"] },
          ]}
          onSubmit={(x) => save("/employees", x)}
        />
        <ImportPanel
          path="/import/employees"
          columns={EMPLOYEE_COLUMNS}
          title={systemAdmin ? "Import employees from CSV" : "Import RTO officers from CSV"}
          run={run}
          busy={busy}
          onImported={() => run(load)}
        />
        {systemAdmin && (
          <div className="mt-4">
            <AccountBulkBar
              selection={employeeSelection}
              withRole
              busy={busy}
              onApply={(status, role) =>
                bulk("Update employees", "PATCH", "/accounts/employees/bulk",
                  { ids: employeeSelection.ids.map(Number), status, role }, employeeSelection)
              }
            />
          </div>
        )}
        <div className="table-responsive mt-4">
          <table className="table">
            <thead>
              <tr>
                {systemAdmin && (
                  <th className="select-column">
                    <SelectionCheckbox
                      checked={employeeSelection.allSelected}
                      indeterminate={employeeSelection.someSelected}
                      disabled={busy || employees.length <= 1}
                      onChange={employeeSelection.toggleAll}
                      label="Select all employees except yourself"
                    />
                  </th>
                )}
                <th>Employee</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {employees.map((e) => {
                const editable = systemAdmin && e.employeeId !== user.id;
                return (
                  <tr key={e.employeeId} className={employeeSelection.has(e.employeeId) ? "table-active" : ""}>
                    {systemAdmin && (
                      <td className="select-column">
                        {editable && (
                          <SelectionCheckbox
                            checked={employeeSelection.has(e.employeeId)}
                            disabled={busy}
                            onChange={() => employeeSelection.toggle(e.employeeId)}
                            label={`Select ${e.emailAddress}`}
                          />
                        )}
                      </td>
                    )}
                    <td>
                      {e.firstName} {e.lastName}
                    </td>
                    <td>{e.emailAddress}</td>
                    <td>
                      {editable ? (
                        <select
                          aria-label={`Role for ${e.emailAddress}`}
                          className="form-select"
                          disabled={busy}
                          value={e.role}
                          onChange={(event) => account("employees", e, e.status, event.target.value)}
                        >
                          {ROLES.map((r) => (
                            <option key={r}>{r}</option>
                          ))}
                        </select>
                      ) : (
                        label(e.role)
                      )}
                    </td>
                    <td>
                      {editable ? (
                        <select
                          aria-label={`Status for ${e.emailAddress}`}
                          className="form-select"
                          disabled={busy}
                          value={e.status}
                          onChange={(event) => account("employees", e, event.target.value)}
                        >
                          {STATUSES.map((s) => (
                            <option key={s}>{s}</option>
                          ))}
                        </select>
                      ) : (
                        e.status
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

      {systemAdmin && (
        <section className="card p-4">
          <div className="d-flex flex-wrap justify-content-between gap-2">
            <h2>Owner accounts</h2>
            <ExportButton path="/export/owners.csv" run={run} busy={busy} />
          </div>
          <p className="text-secondary">
            Manage access for registered vehicle owners. Exports show only the last four characters of
            identity numbers.
          </p>
          <AccountBulkBar
            selection={ownerSelection}
            busy={busy}
            onApply={(status) =>
              bulk("Update owners", "PATCH", "/accounts/owners/bulk", { ids: ownerSelection.ids.map(Number), status }, ownerSelection)
            }
          />
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th className="select-column">
                    <SelectionCheckbox
                      checked={ownerSelection.allSelected}
                      indeterminate={ownerSelection.someSelected}
                      disabled={busy || owners.length === 0}
                      onChange={ownerSelection.toggleAll}
                      label="Select all owners"
                    />
                  </th>
                  <th>Owner</th>
                  <th>Email</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {owners.map((o) => (
                  <tr key={o.ownerId} className={ownerSelection.has(o.ownerId) ? "table-active" : ""}>
                    <td className="select-column">
                      <SelectionCheckbox
                        checked={ownerSelection.has(o.ownerId)}
                        disabled={busy}
                        onChange={() => ownerSelection.toggle(o.ownerId)}
                        label={`Select ${o.emailAddress}`}
                      />
                    </td>
                    <td>
                      {o.firstName} {o.lastName}
                    </td>
                    <td>{o.emailAddress}</td>
                    <td>
                      <select
                        aria-label={`Status for ${o.emailAddress}`}
                        disabled={busy}
                        className="form-select"
                        value={o.status}
                        onChange={(e) => account("owners", o, e.target.value)}
                      >
                        {STATUSES.map((s) => (
                          <option key={s}>{s}</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      <section className="card p-4">
        <h2>Recent audit activity</h2>
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Source</th>
                <th>Action</th>
                <th>Application / remarks</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((l) => (
                <tr key={l.id}>
                  <td>{new Date(l.createdAt).toLocaleString()}</td>
                  <td>{l.actor}</td>
                  <td>{label(l.action)}</td>
                  <td className="text-break">
                    {l.applicationRefNo} {l.remarks}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
