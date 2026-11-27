import { useState } from "react";
import { ACCEPTED_FILES, checkDocumentFile, fileSize, REQUIRED_DOCUMENTS } from "../documents";

// File pickers for the three required documents on the new-registration form.
// The chosen files are kept by the parent and uploaded after the application is created.
export default function DocumentUploadFields({ files, onChange, busy }) {
  const [errors, setErrors] = useState({});
  return (
    <fieldset className="col-12 document-fields">
      <legend className="h3">Supporting documents</legend>
      <p className="small text-secondary">
        Attach all three documents. PDF, JPG or PNG, up to 5 MB each.
      </p>
      <div className="row g-3">
        {REQUIRED_DOCUMENTS.map(({ type, title }) => {
          const id = `document-${type}`;
          const file = files[type];
          return (
            <div className="col-md-4" key={type}>
              <label htmlFor={id} className="form-label">
                {title}
              </label>
              <input
                id={id}
                type="file"
                className={`form-control ${errors[type] ? "is-invalid" : ""}`}
                accept={ACCEPTED_FILES}
                disabled={busy}
                required
                aria-describedby={`${id}-help`}
                onChange={(event) => {
                  const chosen = event.target.files?.[0] ?? null;
                  const problem = checkDocumentFile(chosen);
                  setErrors({ ...errors, [type]: problem });
                  if (problem) event.target.value = "";
                  onChange({ ...files, [type]: problem ? null : chosen });
                }}
              />
              <div id={`${id}-help`} className={errors[type] ? "invalid-feedback" : "form-text"}>
                {errors[type] || (file ? `${file.name} · ${fileSize(file.size)}` : "No file chosen")}
              </div>
            </div>
          );
        })}
      </div>
    </fieldset>
  );
}
