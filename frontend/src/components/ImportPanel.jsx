import { useState } from "react";
import { upload } from "../api";

// CSV import in two steps: "Check file" runs a dry run that reports every row
// error without saving; "Import" saves all rows, and only when the check found none.
export default function ImportPanel({ path, columns, title, run, busy, onImported }) {
  const [file, setFile] = useState(null);
  const [check, setCheck] = useState(null);
  const [done, setDone] = useState(null);
  const inputId = `import-${path.replace(/\W/g, "-")}`;

  const send = (dryRun) =>
    run(async () => {
      const result = await upload(`${path}?dryRun=${dryRun}`, { file });
      if (dryRun) {
        setCheck(result);
        setDone(null);
      } else {
        setCheck(null);
        setDone(result);
        if (result.imported > 0) onImported();
      }
    });

  return (
    <details className="import-panel mt-3">
      <summary>{title}</summary>
      <p className="small text-secondary mt-2 mb-2">
        CSV with a header row containing: <code>{columns.join(", ")}</code>. Up to 500 rows. Every
        row is checked first; nothing is saved if any row has an error.
      </p>
      <div className="d-flex flex-wrap gap-2 align-items-center">
        <input
          id={inputId}
          type="file"
          accept=".csv,text/csv"
          className="form-control import-file"
          disabled={busy}
          aria-label={title}
          onChange={(event) => {
            setFile(event.target.files?.[0] ?? null);
            setCheck(null);
            setDone(null);
          }}
        />
        <button className="btn btn-outline-primary" disabled={busy || !file} onClick={() => send(true)}>
          Check file
        </button>
        <button
          className="btn btn-primary"
          disabled={busy || !check || check.errors.length > 0 || check.validRows === 0}
          onClick={() => send(false)}
        >
          Import {check && check.errors.length === 0 ? `${check.validRows} rows` : ""}
        </button>
      </div>
      {check && (
        <div className={`alert mt-3 mb-0 alert-${check.errors.length ? "warning" : "success"}`} role="status">
          {check.validRows} of {check.totalRows} rows are valid.
          {check.errors.length > 0 ? " Fix these rows and check the file again:" : " Ready to import."}
          {check.errors.length > 0 && (
            <ul className="mb-0 mt-2 small">
              {check.errors.slice(0, 50).map((error, index) => (
                <li key={index}>
                  Line {error.row}, {error.field}: {error.message}
                </li>
              ))}
              {check.errors.length > 50 && <li>…and {check.errors.length - 50} more</li>}
            </ul>
          )}
        </div>
      )}
      {done && (
        <div className="alert alert-success mt-3 mb-0" role="status">
          Imported {done.imported} rows.
        </div>
      )}
    </details>
  );
}
