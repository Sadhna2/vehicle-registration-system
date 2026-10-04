import { useCallback, useEffect, useState } from "react";
import { api, categories, label, money } from "../api";
import Form from "./Form";
export default function Admin({ actor, run, busy }) {
  const [report, setReport] = useState(null),
    [fees, setFees] = useState([]),
    [logs, setLogs] = useState([]),
    [employees, setEmployees] = useState([]),
    [owners, setOwners] = useState([]);
  const load = useCallback(async () => {
    const [r, f, l, e] = await Promise.all([
      api("/reports"),
      api("/fees"),
      api("/audit"),
      api("/employees"),
    ]);
    setReport(r);
    setFees(f);
    setLogs(l);
    setEmployees(e);
    if (actor.role === "SYSTEM_ADMIN") setOwners(await api("/owners"));
  }, [actor.role]);
  useEffect(() => {
    run(load);
  }, [load, run]);
  const save = (path, data) =>
    run(async () => {
      await api(path, "POST", data);
      await load();
    });
  const account = (kind, person, status, role) =>
    run(async () => {
      await api(
        `/accounts/${kind}/${person.employeeId || person.ownerId}`,
        "PATCH",
        { status, ...(role ? { role } : {}) },
      );
      await load();
    });
  return (
    <div className="d-grid gap-4">
      {report && (
        <section className="card p-4">
          <h2>Department overview</h2>
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
      {actor.role === "RTO_ADMIN" && (
        <section className="card p-4">
          <h2>Fee rules</h2>
          <p className="text-secondary">
            New rules apply to future applications. Submitted fees remain fixed.
          </p>
          <Form
            busy={busy}
            fields={[
              { name: "vehicleCategory", options: categories },
              {
                name: "applicationType",
                options: ["NEW"],
              },
              { name: "feeAmount", type: "number", min: 0.01, step: 0.01 },
              { name: "effectiveFrom", type: "date" },
            ]}
            onSubmit={(x) =>
              save("/fees", { ...x, feeAmount: Number(x.feeAmount) })
            }
          />
          <div className="table-responsive mt-4">
            <table className="table">
              <thead>
                <tr>
                  <th>Category</th>
                  <th>Type</th>
                  <th>Fee</th>
                  <th>Effective from</th>
                </tr>
              </thead>
              <tbody>
                {fees.map((f) => (
                  <tr key={f.feeRuleId}>
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
        <h2>RTO employees</h2>
        <Form
          busy={busy}
          button="Create employee"
          fields={[
            { name: "firstName", maxLength: 30 },
            { name: "lastName", maxLength: 30 },
            { name: "emailAddress", type: "email" },
            { name: "phoneNumber", pattern: "[0-9+]{10,15}" },
            { name: "password", type: "password", minLength: 10 },
            {
              name: "designation",
              options: ["CLERK", "OFFICER", "SENIOR_OFFICER", "ADMIN"],
            },
            {
              name: "role",
              options:
                actor.role === "SYSTEM_ADMIN"
                  ? ["RTO_OFFICER", "RTO_ADMIN", "SYSTEM_ADMIN"]
                  : ["RTO_OFFICER"],
            },
          ]}
          onSubmit={(x) => save("/employees", x)}
        />
        <div className="table-responsive mt-4">
          <table className="table">
            <thead>
              <tr>
                <th>Employee</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {employees.map((e) => (
                <tr key={e.employeeId}>
                  <td>
                    {e.firstName} {e.lastName}
                  </td>
                  <td>{e.emailAddress}</td>
                  <td>
                    {actor.role === "SYSTEM_ADMIN" &&
                    e.employeeId !== actor.id ? (
                      <select
                        aria-label={`Role for ${e.emailAddress}`}
                        className="form-select"
                        disabled={busy}
                        value={e.role}
                        onChange={(event) =>
                          account("employees", e, e.status, event.target.value)
                        }
                      >
                        {["RTO_OFFICER", "RTO_ADMIN", "SYSTEM_ADMIN"].map(
                          (r) => (
                            <option key={r}>{r}</option>
                          ),
                        )}
                      </select>
                    ) : (
                      label(e.role)
                    )}
                  </td>
                  <td>
                    {actor.role === "SYSTEM_ADMIN" &&
                    e.employeeId !== actor.id ? (
                      <select
                        aria-label={`Status for ${e.emailAddress}`}
                        className="form-select"
                        disabled={busy}
                        value={e.status}
                        onChange={(event) =>
                          account("employees", e, event.target.value)
                        }
                      >
                        {["ACTIVE", "INACTIVE", "SUSPENDED"].map((s) => (
                          <option key={s}>{s}</option>
                        ))}
                      </select>
                    ) : (
                      e.status
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
      {actor.role === "SYSTEM_ADMIN" && (
        <section className="card p-4">
          <h2>Owner accounts</h2>
          <p className="text-secondary">
            Manage access for registered vehicle owners.
          </p>
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Owner</th>
                  <th>Email</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {owners.map((o) => (
                  <tr key={o.ownerId}>
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
                        {["ACTIVE", "INACTIVE", "SUSPENDED"].map((s) => (
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
                <th>Actor</th>
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
