import { useState } from "react";
import { api, upload } from "../api";
import { REQUIRED_DOCUMENTS } from "../documents";
import VehicleForm from "./VehicleForm";
import FeeSchedule from "./FeeSchedule";
import DocumentUploadFields from "./DocumentUploadFields";

// One guided flow: vehicle details and the three documents are filled in on one
// screen. On submit the application is created first, then each document is uploaded.
export default function NewRegistrationPage({ run, busy, onChange }) {
  const [files, setFiles] = useState({});
  const [progress, setProgress] = useState("");

  const submit = (values) =>
    run(async () => {
      const missing = REQUIRED_DOCUMENTS.filter(({ type }) => !files[type]);
      if (missing.length) {
        throw new Error(`Attach: ${missing.map(({ title }) => title).join(", ")}.`);
      }
      setProgress("Submitting application…");
      const application = await api("/applications", "POST", values);
      const failed = [];
      for (const { type, title } of REQUIRED_DOCUMENTS) {
        setProgress(`Uploading ${title.toLowerCase()}…`);
        try {
          await upload(`/applications/${encodeURIComponent(application.reference)}/documents`, {
            type,
            file: files[type],
          });
        } catch (failure) {
          failed.push(`${title}: ${failure.message}`);
        }
      }
      setProgress("");
      onChange(application);
      if (failed.length) {
        throw new Error(
          `Application ${application.reference} was submitted, but some documents did not upload. ` +
            `Upload them from this page. ${failed.join(" ")}`,
        );
      }
    }).finally(() => setProgress(""));

  return (
    <section className="card p-4">
      <h2>Vehicle information</h2>
      <VehicleForm busy={busy} onSubmit={submit}>
        <DocumentUploadFields files={files} onChange={setFiles} busy={busy} />
        {progress && (
          <p className="col-12 mb-0 text-secondary" role="status">
            {progress}
          </p>
        )}
      </VehicleForm>
      <div className="mt-4">
        <FeeSchedule />
      </div>
    </section>
  );
}
