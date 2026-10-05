import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import Detail from "./Detail";

export default function ApplicationDetailsPage({ user, run, busy, onChange }) {
  const { reference } = useParams();
  const navigate = useNavigate();
  const [state, setState] = useState({ application: null, error: "" });
  useEffect(() => {
    let active = true;
    setState({ application: null, error: "" });
    api(`/applications/${encodeURIComponent(reference)}`)
      .then((application) => {
        if (active) setState({ application, error: "" });
      })
      .catch((failure) => {
        if (active) setState({ application: null, error: failure.message });
      });
    return () => {
      active = false;
    };
  }, [reference, user.id]);
  const close = () => navigate("/applications");
  if (state.error)
    return (
      <section className="card p-4">
        <p className="alert alert-danger" role="alert">
          {state.error}
        </p>
        <button className="btn btn-outline-secondary" onClick={close}>
          Back to applications
        </button>
      </section>
    );
  if (!state.application)
    return <p role="status">Loading application details...</p>;
  return (
    <Detail
      key={reference}
      data={state.application}
      user={user}
      run={run}
      busy={busy}
      onClose={close}
      onChange={(application) => {
        setState({ application, error: "" });
        onChange();
      }}
    />
  );
}
