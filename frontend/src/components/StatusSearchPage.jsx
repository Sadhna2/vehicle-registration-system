import { useSearchParams } from "react-router-dom";
import { APPLICATION_STATUSES, label } from "../api";
import ApplicationsPage from "./ApplicationsPage";

// Status search: one tile per application status with its live count. Choosing a tile
// shows only the applications in that state (searched by the backend), ready for the
// bulk actions and CSV export of the applications page below.
export default function StatusSearchPage(props) {
  const { data } = props;
  const [searchParams, setSearchParams] = useSearchParams();
  const selected = searchParams.get("status") || "";
  const counts = Object.fromEntries(APPLICATION_STATUSES.map((status) => [status, 0]));
  data.applications.forEach((row) => {
    counts[row.status] = (counts[row.status] ?? 0) + 1;
  });

  const choose = (status) => setSearchParams(status ? { status } : {});

  return (
    <>
      <div className="status-tiles mb-4" role="group" aria-label="Search by application status">
        <button
          type="button"
          className={`status-tile ${selected === "" ? "selected" : ""}`}
          aria-pressed={selected === ""}
          onClick={() => choose("")}
        >
          <span>All applications</span>
          <strong>{data.applications.length}</strong>
        </button>
        {APPLICATION_STATUSES.map((status) => (
          <button
            key={status}
            type="button"
            className={`status-tile status-${status.toLowerCase()} ${selected === status ? "selected" : ""}`}
            aria-pressed={selected === status}
            onClick={() => choose(status)}
          >
            <span>{label(status)}</span>
            <strong>{counts[status]}</strong>
          </button>
        ))}
      </div>
      <ApplicationsPage {...props} />
    </>
  );
}
