import { useId, useState } from "react";
import { label } from "../api";
export default function Form({
  fields,
  initial = {},
  onSubmit,
  button = "Save",
  busy = false,
}) {
  const [values, setValues] = useState(initial);
  const formId = useId();
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit(values);
      }}
      className="row g-3"
    >
      {fields.map((field) => (
        <div className="col-md-6" key={field.name}>
          <label htmlFor={`${formId}-${field.name}`} className="form-label">
            {field.title ||
              label(field.name.replace(/([A-Z])/g, " $1")).replace(
                /^./,
                (letter) => letter.toUpperCase(),
              )}
          </label>
          {field.options ? (
            <select
              id={`${formId}-${field.name}`}
              disabled={busy || field.disabled}
              className="form-select"
              required={!field.optional}
              value={values[field.name] ?? ""}
              onChange={(e) =>
                setValues({ ...values, [field.name]: e.target.value })
              }
            >
              <option value="">Select…</option>
              {field.options.map((x) => (
                <option key={x} value={x}>
                  {label(x)}
                </option>
              ))}
            </select>
          ) : (
            <input
              id={`${formId}-${field.name}`}
              disabled={busy || field.disabled}
              className="form-control"
              type={field.type || "text"}
              required={!field.optional}
              min={field.min}
              max={field.max}
              step={field.step}
              inputMode={field.inputMode}
              minLength={field.minLength}
              maxLength={field.maxLength}
              pattern={field.pattern}
              autoComplete={field.autoComplete}
              value={values[field.name] ?? ""}
              onChange={(e) =>
                setValues({ ...values, [field.name]: e.target.value })
              }
            />
          )}
        </div>
      ))}
      <div className="col-12">
        <button disabled={busy} className="btn btn-primary" type="submit">
          {busy ? "Saving…" : button}
        </button>
      </div>
    </form>
  );
}
