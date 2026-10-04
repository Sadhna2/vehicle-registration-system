import { useCallback, useEffect, useState } from "react";
import { api, money, label } from "./api";
import Auth from "./components/Auth";
import Status from "./components/Status";
import Detail from "./components/Detail";
import Admin from "./components/Admin";
import VehicleForm from "./components/VehicleForm";
import FeeSchedule from "./components/FeeSchedule";
export default function App() {
  const [actor, setActor] = useState(null),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [ready, setReady] = useState(false);
  const [tab, setTab] = useState("applications"),
    [rows, setRows] = useState([]),
    [page, setPage] = useState(0),
    [totalPages, setTotalPages] = useState(0),
    [total, setTotal] = useState(0),
    [detail, setDetail] = useState(null),
    [vehicles, setVehicles] = useState([]),
    [query, setQuery] = useState(""),
    [loaded, setLoaded] = useState(false);
  const run = useCallback(async (task) => {
    setBusy(true);
    setError("");
    try {
      await task();
    } catch (e) {
      setError(e.message);
      if (e.status === 401) setActor(null);
    } finally {
      setBusy(false);
    }
  }, []);
  useEffect(() => {
    api("/auth/me")
      .then(setActor)
      .catch((e) => {
        if (e.status !== 401) setError(e.message);
      })
      .finally(() => setReady(true));
  }, []);
  const load = useCallback(async () => {
    if (!actor) return;
    const data = await api(`/applications?page=${page}`);
    setRows(data.content);
    setTotal(data.totalElements);
    setTotalPages(data.totalPages);
    setLoaded(true);
    if (actor.role === "OWNER") setVehicles(await api("/vehicles"));
  }, [actor, page]);
  useEffect(() => {
    if (!actor) return;
    run(load);
    const timer = setInterval(() => {
      if (!document.hidden)
        load().catch((e) => {
          setError(e.message);
          if (e.status === 401) setActor(null);
        });
    }, 15000);
    return () => clearInterval(timer);
  }, [actor, load, run]);
  const changed = async (data) => {
    setDetail(data);
    setTab("applications");
    await load();
  };
  const open = (ref) =>
    run(async () =>
      setDetail(await api(`/applications/${encodeURIComponent(ref)}`)),
    );
  if (!ready) return <main className="container py-5">Loading workspace…</main>;
  return (
    <>
      <header className="topbar">
        <div className="container-fluid px-4 d-flex justify-content-between align-items-center">
          <div className="brand">
            <span className="brand-mark">V</span> Vehicle Registration
            <span className="brand-sub">PORTAL</span>
          </div>
          {actor && (
            <div className="d-flex align-items-center gap-3">
              <span className="small d-none d-sm-inline">
                {actor.name} · {label(actor.role)}
              </span>
              <button
                className="btn btn-sm btn-outline-light"
                disabled={busy}
                onClick={() =>
                  run(async () => {
                    await api("/auth/logout", "POST");
                    setActor(null);
                    setDetail(null);
                    setPage(0);
                    setTab("applications");
                    setLoaded(false);
                    setRows([]);
                    setVehicles([]);
                  })
                }
              >
                Sign out
              </button>
            </div>
          )}
        </div>
      </header>
      {error && (
        <div className="container mt-3">
          <div className="alert alert-danger" role="alert">
            {error}
            <button
              className="btn-close float-end"
              aria-label="Dismiss error"
              onClick={() => setError("")}
            />
          </div>
        </div>
      )}
      {!actor ? (
        <Auth
          run={run}
          busy={busy}
          onLogin={(a) => {
            setActor(a);
            setLoaded(false);
            setRows([]);
            setVehicles([]);
            setPage(0);
            setTab("applications");
          }}
        />
      ) : (
        <div className="workspace">
          <aside className="sidebar">
            <span className="eyebrow px-3">WORKSPACE</span>
            {[
              [
                "applications",
                actor.role === "OWNER" ? "My applications" : "Review queue",
              ],
              ...(actor.role === "OWNER"
                ? [
                    ["new", "New registration"],
                    ["vehicles", "My vehicles"],
                  ]
                : []),
              ...(["RTO_ADMIN", "SYSTEM_ADMIN"].includes(actor.role)
                ? [["admin", "Administration"]]
                : []),
            ].map(([id, name]) => (
              <button
                key={id}
                className={`nav-item ${tab === id ? "active" : ""}`}
                onClick={() => {
                  setTab(id);
                  setDetail(null);
                }}
              >
                {name}
              </button>
            ))}
            <div className="sidebar-note">
              Vehicle services
              <br />
              <span>Applications refresh every 15 seconds.</span>
            </div>
          </aside>
          <main className="main-content">
            <div className="eyebrow">
              REGISTRATION SERVICES / {label(tab).toUpperCase()}
            </div>
            <h1 className="page-title">
              {tab === "new"
                ? "Register a new vehicle"
                : tab === "vehicles"
                  ? "Your vehicle records"
                  : tab === "admin"
                    ? "Administration"
                    : actor.role === "OWNER"
                      ? "My applications"
                      : "Application review queue"}
            </h1>
            {tab === "new" && (
              <section className="card p-4">
                <h2>Vehicle information</h2>
                <p className="text-secondary">
                  Your owner profile is linked automatically. The fee is
                  calculated when you submit.
                </p>
                <VehicleForm
                  key="new-vehicle"
                  busy={busy}
                  onSubmit={(x) =>
                    run(async () =>
                      changed(await api("/applications", "POST", x)),
                    )
                  }
                />
                <div className="mt-4">
                  <FeeSchedule />
                </div>
              </section>
            )}
            {tab === "applications" &&
              (detail ? (
                <Detail
                  key={detail.reference}
                  data={detail}
                  actor={actor}
                  run={run}
                  busy={busy}
                  onChange={changed}
                  onClose={() => setDetail(null)}
                />
              ) : (
                <>
                  {actor.role === "OWNER" && (
                    <div className="dashboard-welcome mb-4">
                      <div>
                        <span className="eyebrow">YOUR NEXT MILESTONE</span>
                        <h2>Ready to put your new vehicle on the road?</h2>
                        <p>
                          Start an application, complete payment, and track your
                          RC.
                        </p>
                      </div>
                      <button
                        disabled={busy}
                        className="btn btn-primary"
                        onClick={() => setTab("new")}
                      >
                        + New registration
                      </button>
                    </div>
                  )}
                  <div className="row g-3 mb-4">
                    <div className="col-md-4">
                      <div className="metric">
                        <span>Total applications</span>
                        <strong>{loaded ? total : "—"}</strong>
                      </div>
                    </div>
                    <div className="col-md-4">
                      <div className="metric">
                        <span>Awaiting review on this page</span>
                        <strong>
                          {
                            rows.filter(
                              (r) =>
                                !["APPROVED", "REJECTED"].includes(r.status),
                            ).length
                          }
                        </strong>
                      </div>
                    </div>
                    <div className="col-md-4">
                      <div className="metric">
                        <span>Approved on this page</span>
                        <strong>
                          {rows.filter((r) => r.status === "APPROVED").length}
                        </strong>
                      </div>
                    </div>
                  </div>
                  <section className="card p-4">
                    <div className="d-flex justify-content-between flex-wrap gap-3 mb-3">
                      <h2 className="mb-0">Application register</h2>
                      <button
                        className="btn btn-outline-primary"
                        disabled={busy}
                        onClick={() => run(load)}
                      >
                        Refresh
                      </button>
                    </div>
                    <label htmlFor="search" className="form-label">
                      Filter current page by reference, owner, or vehicle
                    </label>
                    <input
                      id="search"
                      className="form-control mb-4"
                      value={query}
                      onChange={(e) => setQuery(e.target.value)}
                      placeholder="Search applications…"
                    />
                    <div className="table-responsive">
                      <table className="table align-middle">
                        <thead>
                          <tr>
                            <th>Application</th>
                            <th>Vehicle / owner</th>
                            <th>Status</th>
                            <th>Fee</th>
                            <th></th>
                          </tr>
                        </thead>
                        <tbody>
                          {rows
                            .filter((r) =>
                              `${r.reference} ${r.owner} ${r.vehicle}`
                                .toLowerCase()
                                .includes(query.toLowerCase()),
                            )
                            .map((r) => (
                              <tr key={r.reference}>
                                <td>
                                  <span className="small text-break">
                                    {r.reference}
                                  </span>
                                  <div className="text-secondary small">
                                    {r.type} ·{" "}
                                    {new Date(
                                      r.submittedAt,
                                    ).toLocaleDateString()}
                                  </div>
                                </td>
                                <td>
                                  <strong>{r.vehicle}</strong>
                                  <div className="text-secondary small">
                                    {r.owner}
                                  </div>
                                </td>
                                <td>
                                  <Status value={r.status} />
                                </td>
                                <td>{money(r.amount)}</td>
                                <td>
                                  <button
                                    className="btn btn-sm btn-outline-primary"
                                    disabled={busy}
                                    onClick={() => open(r.reference)}
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
                      <p className="text-secondary py-4 text-center">
                        {!loaded
                          ? "Application records could not be loaded. Select Refresh to try again."
                          : "No applications yet."}{" "}
                        {loaded &&
                          (actor.role === "OWNER"
                            ? "Submit your first vehicle registration to begin."
                            : "Submitted applications will appear here.")}
                      </p>
                    )}
                    <div className="d-flex justify-content-between align-items-center mt-3">
                      <button
                        className="btn btn-sm btn-outline-secondary"
                        disabled={page === 0 || busy}
                        onClick={() => setPage(page - 1)}
                      >
                        Previous
                      </button>
                      <span className="small text-secondary">
                        Page {page + 1} of {Math.max(1, totalPages)}
                      </span>
                      <button
                        className="btn btn-sm btn-outline-secondary"
                        disabled={page + 1 >= totalPages || busy}
                        onClick={() => setPage(page + 1)}
                      >
                        Next
                      </button>
                    </div>
                  </section>
                </>
              ))}
            {tab === "vehicles" && (
              <div className="row g-4">
                {vehicles.map((v) => (
                  <div className="col-xl-6" key={v.temporaryregisterNo}>
                    <section className="card p-4">
                      <h2>
                        {v.manufacturerName} {v.modelName}
                      </h2>
                      <p className="text-secondary">
                        {v.registrationcertificateNumber ||
                          "Registration pending"}{" "}
                        · {label(v.vehicleCategory)}
                      </p>
                      <p>
                        Valid until: {v.registrationValidTill || "Not issued"}
                      </p>
                    </section>
                  </div>
                ))}
                {vehicles.length === 0 && (
                  <p className="text-secondary">
                    Your vehicles will appear after submitting an application.
                  </p>
                )}
              </div>
            )}
            {tab === "admin" && <Admin actor={actor} run={run} busy={busy} />}
          </main>
        </div>
      )}
    </>
  );
}
