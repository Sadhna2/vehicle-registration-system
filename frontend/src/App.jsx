import { useState } from "react";
import {
  NavLink,
  Navigate,
  Route,
  Routes,
  useNavigate,
} from "react-router-dom";
import { label } from "./api";
import Auth from "./components/Auth";
import Admin from "./components/Admin";
import ApplicationDetailsPage from "./components/ApplicationDetailsPage";
import ApplicationsPage from "./components/ApplicationsPage";
import NewRegistrationPage from "./components/NewRegistrationPage";
import VehiclesPage from "./components/VehiclesPage";
import useAction from "./hooks/useAction";
import useSessionUser from "./hooks/useSessionUser";
import useRegistrationData from "./hooks/useRegistrationData";

function Page({ title, children }) {
  return (
    <>
      <h1 className="page-title">{title}</h1>
      {children}
    </>
  );
}

export default function App() {
  const [user, setUser] = useSessionUser();
  const [refresh, setRefresh] = useState(0);
  const navigate = useNavigate();
  const { run, busy, error, setError } = useAction();
  const data = useRegistrationData(user, refresh);
  const owner = user?.role === "OWNER";
  const administrator = ["RTO_ADMIN", "SYSTEM_ADMIN"].includes(user?.role);
  const applicationsTitle = owner ? "My applications" : "Review queue";
  const navigation = [
    ["/applications", applicationsTitle],
    ...(owner
      ? [
          ["/new-registration", "New registration"],
          ["/vehicles", "My vehicles"],
        ]
      : []),
    ...(administrator ? [["/administration", "Administration"]] : []),
  ];
  const refreshData = () => setRefresh((value) => value + 1);
  const open = (reference) =>
    navigate(`/applications/${encodeURIComponent(reference)}`);
  const changed = (result) => {
    refreshData();
    open(result.reference);
  };
  const signOut = () => {
    setUser(null);
    setError("");
    navigate("/login", { replace: true });
  };
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
                {user.name} | {label(user.role)}
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
        <Routes>
          <Route
            path="/login"
            element={
              <Auth
                run={run}
                busy={busy}
                onLogin={(account) => {
                  setUser(account);
                  navigate("/applications", { replace: true });
                }}
              />
            }
          />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      ) : (
        <div className="workspace">
          <aside className="sidebar">
            {navigation.map(([path, name]) => (
              <NavLink
                key={path}
                to={path}
                className={({ isActive }) =>
                  `nav-item ${isActive ? "active" : ""}`
                }
              >
                {name}
              </NavLink>
            ))}
          </aside>
          <main className="main-content">
            <Routes>
              <Route
                path="/applications"
                element={
                  <Page title={applicationsTitle}>
                    <ApplicationsPage
                      data={data}
                      busy={busy}
                      onOpen={open}
                      onRefresh={refreshData}
                    />
                  </Page>
                }
              />
              <Route
                path="/applications/:reference"
                element={
                  <Page title="Application details">
                    <ApplicationDetailsPage
                      user={user}
                      run={run}
                      busy={busy}
                      onChange={refreshData}
                    />
                  </Page>
                }
              />
              <Route
                path="/new-registration"
                element={
                  owner ? (
                    <Page title="New registration">
                      <NewRegistrationPage
                        ownerId={user.id}
                        run={run}
                        busy={busy}
                        onChange={changed}
                      />
                    </Page>
                  ) : (
                    <Navigate to="/applications" replace />
                  )
                }
              />
              <Route
                path="/vehicles"
                element={
                  owner ? (
                    <Page title="My vehicles">
                      <VehiclesPage vehicles={data.vehicles} />
                    </Page>
                  ) : (
                    <Navigate to="/applications" replace />
                  )
                }
              />
              <Route
                path="/administration"
                element={
                  administrator ? (
                    <Page title="Administration">
                      <Admin
                        user={user}
                        run={run}
                        busy={busy}
                        onError={setError}
                      />
                    </Page>
                  ) : (
                    <Navigate to="/applications" replace />
                  )
                }
              />
              <Route
                path="*"
                element={<Navigate to="/applications" replace />}
              />
            </Routes>
          </main>
        </div>
      )}
    </>
  );
}
