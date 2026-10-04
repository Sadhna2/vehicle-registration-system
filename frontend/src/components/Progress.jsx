export default function Progress({ data }) {
  const verified = ["INSPECTION_PENDING", "APPROVED"].includes(data.status);
  const steps = [
    ["Application submitted", true],
    ["Fee payment", data.paid],
    ["Verification", verified],
    ["Inspection passed", data.inspectionStatus === "PASSED"],
    ["RC issued", data.status === "APPROVED"],
  ];
  return (
    <ol className="registration-progress" aria-label="Registration progress">
      {steps.map(([name, completed], index) => (
        <li key={name} className={completed ? "completed" : ""}>
          <span className="step-number" aria-hidden="true">
            {completed ? "✓" : index + 1}
          </span>
          <span>
            {name}
            <small>
              {completed
                ? "Completed"
                : data.status === "REJECTED"
                  ? "Application closed"
                  : "Pending"}
            </small>
          </span>
        </li>
      ))}
    </ol>
  );
}
