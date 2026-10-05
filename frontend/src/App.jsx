import { useState } from "react";
import { api, label } from "./api";
import Auth from "./components/Auth";
import Admin from "./components/Admin";
import Detail from "./components/Detail";
import ApplicationsPage from "./components/ApplicationsPage";
import NewRegistrationPage from "./components/NewRegistrationPage";
import VehiclesPage from "./components/VehiclesPage";
import useAction from "./hooks/useAction";
import useSessionUser from "./hooks/useSessionUser";
import useRegistrationData from "./hooks/useRegistrationData";

export default function App() {
  const [user, setUser] = useSessionUser();
  const [tab, setTab] = useState("applications");
  const [page, setPage] = useState(0),
    [detail, setDetail] = useState(null),
    [refresh, setRefresh] = useState(0);
  const { run, busy, error, setError } = useAction();
  const data = useRegistrationData(user, page, refresh);
  const owner = user?.role === "OWNER";
  const navigation = [
    ["applications", owner ? "My applications" : "Review queue"],
    ...(owner
      ? [
          ["new", "New registration"],
          ["vehicles", "My vehicles"],
        ]
      : []),
    ...(["RTO_ADMIN", "SYSTEM_ADMIN"].includes(user?.role)
      ? [["admin", "Administration"]]
      : []),
  ];
  const refreshData = () => setRefresh((value) => value + 1);
  const changed = (result) => {
    setDetail(result);
    setTab("applications");
    refreshData();
  };
  const signOut = () => {
    setUser(null);
    setDetail(null);
    setPage(0);
    setTab("applications");
    setError("");
  };
  const open = (reference) =>
    run(async () =>
      setDetail(await api(`/applications/${encodeURIComponent(reference)}`)),
    );
  return (
    <>
      <header className="topbar">
        <div className="container-fluid px-4 d-flex justify-content-between align-items-center">
          <div className="brand">
            <span className="brand-mark">V</span>Vehicle Registration
          </div>
          {user && (
            <div className="d-flex align-items-center gap-3">
              <span>
                {user.name} · {label(user.role)}
              </span>
              <button
                className="btn btn-outline-secondary"
                disabled={busy}
                onClick={signOut}
              >
                Sign out
              </button>
            </div>
          )}
        </div>
      </header>
      {(error || data.error) && (
        <div className="container mt-3">
          <div className="alert alert-danger" role="alert">
            {error || data.error}
            <button
              className="btn-close float-end"
              aria-label="Dismiss error"
              onClick={() => {
                setError("");
                data.clearError();
              }}
            />
          </div>
        </div>
      )}
      {!user ? (
        <Auth
          run={run}
          busy={busy}
          onLogin={(account) => {
            setUser(account);
            setPage(0);
            setTab("applications");
          }}
        />
      ) : (
        <div className="workspace">
          <aside className="sidebar">
            {navigation.map(([id, name]) => (
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
            <p className="sidebar-note">
              Applications refresh every 15 seconds.
            </p>
          </aside>
          <main className="main-content">
            <h1 className="page-title">
              {navigation.find(([id]) => id === tab)?.[1]}
            </h1>
            {tab === "applications" &&
              (detail ? (
                <Detail
                  key={detail.reference}
                  data={detail}
                  user={user}
                  run={run}
                  busy={busy}
                  onChange={changed}
                  onClose={() => setDetail(null)}
                />
              ) : (
                <ApplicationsPage
                  data={data}
                  page={page}
                  setPage={setPage}
                  busy={busy}
                  onOpen={open}
                  onRefresh={refreshData}
                />
              ))}
            {tab === "new" && (
              <NewRegistrationPage
                ownerId={user.id}
                run={run}
                busy={busy}
                onChange={changed}
              />
            )}
            {tab === "vehicles" && <VehiclesPage vehicles={data.vehicles} />}
            {tab === "admin" && (
              <Admin user={user} run={run} busy={busy} onError={setError} />
            )}
          </main>
        </div>
      )}
    </>
  );
}
