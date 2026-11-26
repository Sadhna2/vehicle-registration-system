import { useState } from "react";
import { api, upload, viewFile } from "../api";
import { ACCEPTED_FILES, checkDocumentFile, fileSize, REQUIRED_DOCUMENTS } from "../documents";
import Status from "./Status";

const UPLOAD_STATUSES = ["SUBMITTED", "CORRECTION_REQUIRED"];

// Shows the three required documents of an application.
// Owners upload or replace them while the application is submitted or awaiting correction.
// RTO officers view each file and verify or reject it (with remarks) during verification.
export default function DocumentsPanel({ data, applicant, reviewer, run, busy, onChange }) {
  const [remarks, setRemarks] = useState({});
  const documents = Object.fromEntries(
    (data.documents ?? []).map((document) => [document.documentType, document]),
  );
  const reference = encodeURIComponent(data.reference);
  const ownerCanUpload = applicant && UPLOAD_STATUSES.includes(data.status);
  const officerCanReview = reviewer && data.status === "UNDER_VERIFICATION";

  const refresh = async () => onChange(await api(`/applications/${reference}`));

  const replace = (type, input) => {
    const file = input.files?.[0];
    input.value = "";
    if (!file) return;
    run(async () => {
      const problem = checkDocumentFile(file);
      if (problem) throw new Error(problem);
      await upload(`/applications/${reference}/documents`, { type, file });
      await refresh();
    });
  };

  const review = (document, action) =>
    run(async () => {
      await api(`/applications/${reference}/documents/${document.documentId}/review`, "POST", {
        action,
        remarks: remarks[document.documentType] || undefined,
      });
      setRemarks({ ...remarks, [document.documentType]: "" });
      await refresh();
    });

  return (
    <div className="border-top py-3">
      <h3>Supporting documents</h3>
      {ownerCanUpload && (
        <p className="small text-secondary">
          PDF, JPG or PNG, up to 5 MB. Replace any document marked rejected, then resubmit.
        </p>
      )}
      <div className="table-responsive">
        <table className="table align-middle documents-table">
          <thead>
            <tr>
              <th>Document</th>
              <th>File</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {REQUIRED_DOCUMENTS.map(({ type, title }) => {
              const document = documents[type];
              const remarkId = `remarks-${type}`;
              return (
                <tr key={type}>
                  <td>{title}</td>
                  <td className="text-break">
                    {document ? (
                      <>
                        {document.originalFileName}
                        <small className="d-block text-secondary">
                          {fileSize(document.sizeBytes)} · uploaded{" "}
                          {new Date(document.uploadedAt).toLocaleString()}
                        </small>
                      </>
                    ) : (
                      <span className="text-secondary">Not uploaded</span>
                    )}
                  </td>
                  <td>
                    {document ? <Status value={document.status} /> : <Status value="MISSING" />}
                    {document?.remarks && (
                      <small className="d-block mt-1 text-secondary">{document.remarks}</small>
                    )}
                  </td>
                  <td>
                    <div className="d-flex flex-wrap gap-2 align-items-center">
                      {document && (
                        <button
                          className="btn btn-outline-primary"
                          disabled={busy}
                          onClick={() =>
                            run(() =>
                              viewFile(
                                `/applications/${reference}/documents/${document.documentId}/file`,
                              ),
                            )
                          }
                        >
                          View
                        </button>
                      )}
                      {ownerCanUpload && document?.status !== "VERIFIED" && (
                        <label className={`btn btn-outline-primary mb-0 ${busy ? "disabled" : ""}`}>
                          {document ? "Replace" : "Upload"}
                          <input
                            type="file"
                            className="visually-hidden"
                            accept={ACCEPTED_FILES}
                            disabled={busy}
                            aria-label={`${document ? "Replace" : "Upload"} ${title}`}
                            onChange={(event) => replace(type, event.target)}
                          />
                        </label>
                      )}
                      {officerCanReview && document && (
                        <>
                          <input
                            id={remarkId}
                            className="form-control document-remarks"
                            placeholder="Remarks (required to reject)"
                            aria-label={`Remarks for ${title}`}
                            maxLength={500}
                            disabled={busy}
                            value={remarks[type] ?? ""}
                            onChange={(event) => setRemarks({ ...remarks, [type]: event.target.value })}
                          />
                          <button
                            className="btn btn-outline-primary"
                            disabled={busy || document.status === "VERIFIED"}
                            onClick={() => review(document, "VERIFY")}
                          >
                            Verify
                          </button>
                          <button
                            className="btn btn-outline-danger"
                            disabled={busy || !(remarks[type] ?? "").trim()}
                            onClick={() => review(document, "REJECT")}
                          >
                            Reject
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
